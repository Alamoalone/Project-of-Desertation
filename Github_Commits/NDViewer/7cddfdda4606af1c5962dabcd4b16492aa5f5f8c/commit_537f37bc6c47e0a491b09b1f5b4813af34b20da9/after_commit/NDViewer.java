// Copyright (C) 2015-2017 Open Imaging, Inc.
//           (C) 2015 Regents of the University of California
//
// LICENSE:      This file is distributed under the BSD license.
//               License text is included with the source distribution.
//
//               This file is distributed in the hope that it will be useful,
//               but WITHOUT ANY WARRANTY; without even the implied warranty
//               of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
//               IN NO EVENT SHALL THE COPYRIGHT OWNER OR
//               CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//               INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.

package org.micromanager.ndviewer.main;

import java.awt.Color;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import mmcorej.org.json.JSONException;
import mmcorej.org.json.JSONObject;
import org.micromanager.ndviewer.api.CanvasMouseListenerInterface;
import org.micromanager.ndviewer.api.ControlsPanelInterface;
import org.micromanager.ndviewer.api.DataSourceInterface;
import org.micromanager.ndviewer.api.OverlayerPlugin;
import org.micromanager.ndviewer.api.ViewerAcquisitionInterface;
import org.micromanager.ndviewer.api.ViewerInterface;
import org.micromanager.ndviewer.internal.gui.AxisScroller;
import org.micromanager.ndviewer.internal.gui.BaseOverlayer;
import org.micromanager.ndviewer.internal.gui.CoalescentExecutor;
import org.micromanager.ndviewer.internal.gui.CoalescentRunnable;
import org.micromanager.ndviewer.internal.gui.DataViewCoords;
import org.micromanager.ndviewer.internal.gui.DisplayCoalescentEDTRunnablePool;
import org.micromanager.ndviewer.internal.gui.DisplayWindow;
import org.micromanager.ndviewer.internal.gui.ImageMaker;
import org.micromanager.ndviewer.internal.gui.ViewerCanvas;
import org.micromanager.ndviewer.internal.gui.contrast.DisplaySettings;
import org.micromanager.ndviewer.overlay.Overlay;

public class NDViewer implements ViewerInterface {

   protected volatile DataSourceInterface dataSource_;
   private volatile DisplaySettings displaySettings_;

   private DisplayCoalescentEDTRunnablePool edtRunnablePool_ =
         DisplayCoalescentEDTRunnablePool.create();

   private CoalescentExecutor displayCalculationExecutor_ =
         new CoalescentExecutor("Display calculation executor");
   private CoalescentExecutor overlayCalculationExecutor_ =
         new CoalescentExecutor("Overlay calculation executor");

   private DisplayWindow displayWindow_;
   private ImageMaker imageMaker_;
   private BaseOverlayer overlayer_;
   private Timer animationTimer_;
   private double animationFPS_ = 7;

   protected DataViewCoords viewCoords_;
   private volatile ViewerAcquisitionInterface acq_;
   private JSONObject summaryMetadata_;
   private volatile boolean closed_ = false;
   // Axes may use integer or string positions. Keep track of which
   // uses which ones do this here, and which string values map to which
   // Integer positions (because these are needed for display)
   private ConcurrentHashMap<String, LinkedList<String>> stringAxes_ = new ConcurrentHashMap<>();

   private Function<JSONObject, Long> readTimeFunction_ = null;
   private Function<JSONObject, Double> readZFunction_ = null;

   private double pixelSizeUm_;
   private volatile JSONObject currentMetadata_;

   private OverlayerPlugin overlayerPlugin_;
   private String preferencesKey_ = "";

   public NDViewer(DataSourceInterface cache, ViewerAcquisitionInterface acq, JSONObject summaryMD,
                   double pixelSize, boolean rgb) {
      this(cache, acq, summaryMD, pixelSize, rgb, null);
   }

   public NDViewer(DataSourceInterface cache, ViewerAcquisitionInterface acq, JSONObject summaryMD,
           double pixelSize, boolean rgb, String preferencesKey) {
      pixelSizeUm_ = pixelSize; //TODO: Could be replaced later with per image pixel size
      summaryMetadata_ = summaryMD;
      dataSource_ = cache;
      acq_ = acq;
      preferencesKey_ = preferencesKey;
      if (preferencesKey_ == null || preferencesKey_.equals("")) {
         preferencesKey_ = "Default";
      }
      displaySettings_ = new DisplaySettings(getPreferences());
      int[] bounds = cache.getBounds();
      viewCoords_ = new DataViewCoords(cache, 0, 0,
              bounds == null ? null : (double) (bounds[2] - bounds[0]),
              bounds == null ? null : (double) (bounds[3] - bounds[1]),
              dataSource_.getBounds(), rgb);
      displayWindow_ = new DisplayWindow(this, acq == null);
      overlayer_ = new BaseOverlayer(this);
      imageMaker_ = new ImageMaker(this, cache);

      // create a default display channel so something shows (needed for explore acq)
         // no channels have been added, so make a default one for monochrome display
      displaySettings_.addChannel("", 16); // Guess for now, remove later
      displayWindow_.addContrastControls("", false);
   }

   /**
    * Convert the position of a string axis to an integer position
    * @param axisPosition
    */
   public int getIntegerPositionFromStringPosition(String axisName, String axisPosition) {
      return stringAxes_.get(axisName).indexOf(axisPosition);
   }

   public String getStringPositionFromIntegerPosition(String axisName, int axisPosition) {
      return stringAxes_.get(axisName).get(axisPosition);
   }

   public void setReadTimeMetadataFunction(Function<JSONObject, Long> fn) {
      readTimeFunction_ = fn;
   }

   public void setReadZMetadataFunction(Function<JSONObject, Double> fn) {
      readZFunction_ = fn;
   }

   public void setChannelColor(String channel, Color c) {
      displaySettings_.setColor(channel, c);
   }

   public boolean isImageXYBounded() {
      return dataSource_.getBounds() != null;
   }

   public JSONObject getDisplaySettingsJSON() {
      if (displaySettings_ == null) {
         return null;
      }
      return displaySettings_.toJSON();
   }

   public Preferences getPreferences() {
      return Preferences.userNodeForPackage(NDViewer.class).node(preferencesKey_);
   }

   public void pan(int dx, int dy) {
      Point2D.Double offset = viewCoords_.getViewOffset();
      double newX = offset.x + (dx / viewCoords_.getMagnificationFromResLevel())
            * viewCoords_.getDownsampleFactor();
      double newY = offset.y + (dy / viewCoords_.getMagnificationFromResLevel())
            * viewCoords_.getDownsampleFactor();

      if (isImageXYBounded()) {
         viewCoords_.setViewOffset(
                 Math.max(viewCoords_.xMin_, Math.min(newX, viewCoords_.xMax_
                       - viewCoords_.getFullResSourceDataSize().x)),
                 Math.max(viewCoords_.yMin_, Math.min(newY, viewCoords_.yMax_
                       - viewCoords_.getFullResSourceDataSize().y)));
      } else {
         viewCoords_.setViewOffset(newX, newY);
      }
      update();
   }

   public void onScollersAdded() {
      displayWindow_.onScrollersAdded();
   }

   public void onScollPositionChanged(AxisScroller scroller, int value) {
      displayWindow_.onScollPositionChanged(scroller, value);
   }

   public void zoom(double factor, Point mouseLocation) {
      //get zoom center in full res pixel coords
      Point2D.Double viewOffset = viewCoords_.getViewOffset();
      Point2D.Double sourceDataSize = viewCoords_.getFullResSourceDataSize();
      Point2D.Double zoomCenter;
      //compute centroid of the zoom in full res coordinates
      if (mouseLocation == null) {
         //if mouse not over image zoom to center
         zoomCenter = new Point2D.Double(viewOffset.x + sourceDataSize.y / 2,
               viewOffset.y + sourceDataSize.y / 2);
      } else {
         zoomCenter = new Point2D.Double(
                 (long) viewOffset.x + mouseLocation.x
                       / viewCoords_.getMagnificationFromResLevel()
                       * viewCoords_.getDownsampleFactor(),
                 (long) viewOffset.y + mouseLocation.y
                       / viewCoords_.getMagnificationFromResLevel()
                       * viewCoords_.getDownsampleFactor());
      }

      //Do zooming--update size of source data
      double newSourceDataWidth = sourceDataSize.x * factor;
      double newSourceDataHeight = sourceDataSize.y * factor;
      if (newSourceDataWidth < 5 || newSourceDataHeight < 5) {
         return; //constrain maximum zoom
      }
      if (isImageXYBounded()) {
         //don't let either of these go bigger than the actual data
         double overzoomXFactor = newSourceDataWidth / (viewCoords_.xMax_ - viewCoords_.xMin_);
         double overzoomYFactor = newSourceDataHeight / (viewCoords_.yMax_ - viewCoords_.yMin_);
         if (overzoomXFactor > 1 || overzoomYFactor > 1) {
            newSourceDataWidth = newSourceDataWidth / Math.max(overzoomXFactor, overzoomYFactor);
            newSourceDataHeight = newSourceDataHeight / Math.max(overzoomXFactor, overzoomYFactor);
         }
      }
      viewCoords_.setFullResSourceDataSize(newSourceDataWidth, newSourceDataHeight);

      double xOffset = (zoomCenter.x - (zoomCenter.x - viewOffset.x)
            * newSourceDataWidth / sourceDataSize.x);
      double yOffset = (zoomCenter.y - (zoomCenter.y - viewOffset.y)
            * newSourceDataHeight / sourceDataSize.y);
      //make sure view doesn't go outside image bounds
      if (isImageXYBounded()) {
         viewCoords_.setViewOffset(
                 Math.max(viewCoords_.xMin_, Math.min(xOffset,
                       viewCoords_.xMax_ - viewCoords_.getFullResSourceDataSize().x)),
                 Math.max(viewCoords_.yMin_, Math.min(yOffset,
                       viewCoords_.yMax_ - viewCoords_.getFullResSourceDataSize().y)));
      } else {
         viewCoords_.setViewOffset(xOffset, yOffset);
      }

      update();
   }

   public void onCanvasResize(int w, int h) {
      if (displayWindow_ == null) {
         return; // during startup
      }
      displayWindow_.onCanvasResized(w, h);

      Point2D.Double displaySizeOld = viewCoords_.getDisplayImageSize();
      //reshape the source image to match canvas aspect ratio
      //expand it, unless it would put it out of range
      double canvasAspect = w / (double) h;
      Point2D.Double source = viewCoords_.getFullResSourceDataSize();
      double sourceAspect = source.x / source.y;
      double newSourceX;
      double newSourceY;
      if (isImageXYBounded()) {
         if (canvasAspect > sourceAspect) {
            newSourceX = canvasAspect / sourceAspect * source.x;
            newSourceY = source.y;
            //check that still within image bounds
         } else {
            newSourceX = source.x;
            newSourceY = source.y / (canvasAspect / sourceAspect);
         }

         double overzoomXFactor = newSourceX / (viewCoords_.xMax_ - viewCoords_.xMin_);
         double overzoomYFactor = newSourceY / (viewCoords_.yMax_ - viewCoords_.yMin_);
         if (overzoomXFactor > 1 || overzoomYFactor > 1) {
            newSourceX = newSourceX / Math.max(overzoomXFactor, overzoomYFactor);
            newSourceY = newSourceY / Math.max(overzoomXFactor, overzoomYFactor);
         }
      } else if (displaySizeOld.x != 0 && displaySizeOld.y != 0) {
         newSourceX = source.x * (w / (double) displaySizeOld.x);
         newSourceY = source.y * (h / (double) displaySizeOld.y);
      } else {
         newSourceX = source.x / sourceAspect * canvasAspect;
         newSourceY = source.y;
      }
      //move into visible area
      viewCoords_.setViewOffset(
              Math.max(viewCoords_.xMin_, Math.min(viewCoords_.xMax_
                      - newSourceX, viewCoords_.getViewOffset().x)),
              Math.max(viewCoords_.yMin_, Math.min(viewCoords_.yMax_
                      - newSourceY, viewCoords_.getViewOffset().y)));

      //set the size of the display iamge
      viewCoords_.setDisplayImageSize(w, h);
      //and the size of the source pixels from which it derives
      viewCoords_.setFullResSourceDataSize(newSourceX, newSourceY);
      update();
   }

   public void initializeViewerToLoaded(List<String> channelNames, JSONObject dispSettings,
           HashMap<String, Object> axisMins, HashMap<String, Object> axisMaxs) {
      // remove the defaut one added as a placeholder
      displayWindow_.removeContrastControls("");
      imageMaker_.removeImageProcessor("");
      displaySettings_ = new DisplaySettings(dispSettings, getPreferences());
      if (channelNames.size() != 0) {
         stringAxes_.put("channel", new LinkedList<String>());
         for (int c = 0; c < channelNames.size(); c++) {
            stringAxes_.get("channel").add(channelNames.get(c));
            displayWindow_.addContrastControls(channelNames.get(c), true);
            if (c == 0) {
               axisMins.put("channel", channelNames.get(c));
            } else if (c == channelNames.size() - 1) {
               axisMaxs.put("channel", channelNames.get(c));
            }
         }
      }
      if (!displayWindow_.contrastControlsInitialized()) {
         // no channels have been added, so make a default one for monochrome display
//         displaySettings_.addChannel("");
         displayWindow_.addContrastControls("", true);
      }
      //maximum scrollbar extents
      edtRunnablePool_.invokeLaterWithCoalescence(
              new NDViewer.ExpandDisplayRangeCoalescentRunnable(axisMaxs));
      edtRunnablePool_.invokeLaterWithCoalescence(
              new NDViewer.ExpandDisplayRangeCoalescentRunnable(axisMins));
   }

   public void channelSetActive(String channelName, boolean selected) {
      if (!displaySettings_.isCompositeMode()) {
         if (selected) {
            viewCoords_.setActiveChannel(channelName);

            //only one channel can be active so inacivate others
            for (String channel : stringAxes_.get("channel")) {
               displaySettings_.setActive(channel, channel.equals(viewCoords_.getActiveChannel()));
            }
         } else {
            //if channel turns off, nothing will show, so dont let this happen
         }
         //make sure other checkboxes update if they autochanged
         displayWindow_.displaySettingsChanged();
      } else {
         //composite mode
         displaySettings_.setActive(channelName, selected);
      }
      update();
   }

   public void setWindowTitle(String s) {
      if (displayWindow_ != null) {
         displayWindow_.setTitle(s);
      }
   }

   /**
    * Signal to viewer that a new image is available.
    *
    * @param axesPositions Hashmap of axis labels to positions
    */
   public void newImageArrived(HashMap<String, Object> axesPositions) {
      try {
         if (!displayWindow_.contrastControlsInitialized()) {
            // remove the default one that was added as a placeholder
            displayWindow_.removeContrastControls("");
            imageMaker_.removeImageProcessor("");
         }

         if (isImageXYBounded()) {
            int[] newBounds = dataSource_.getBounds();
            int[] oldBounds = viewCoords_.getBounds();
            double xResize = (oldBounds[2] - oldBounds[0]) / (double) (newBounds[2] - newBounds[0]);
            double yResize = (oldBounds[3] - oldBounds[1]) / (double) (newBounds[3] - newBounds[1]);
            viewCoords_.setImageBounds(newBounds);
            if (xResize < 1 || yResize < 1) {
               zoom(1 / Math.min(xResize, yResize), null);
            }
         }

         if (axesPositions.containsKey("channel")) {
            String channelName = (String) axesPositions.get("channel");
            if (viewCoords_.getActiveChannel() == null) {
               viewCoords_.setActiveChannel(channelName);
            }

         }

         // Keep track of axes with String values
         for (String axis : axesPositions.keySet()) {
            if (!(axesPositions.get(axis) instanceof String) ) {
               continue;
            }
             if (!stringAxes_.containsKey(axis)) {
               stringAxes_.put(axis, new LinkedList<String>());
             }
             if (!stringAxes_.get(axis).contains(axesPositions.get(axis))) {
                stringAxes_.get(axis).add((String) axesPositions.get(axis));
                if (axis.equals("channel")) {
                   String channelName = (String) axesPositions.get("channel");
                   int bitDepth = dataSource_.getImageBitDepth(axesPositions);
                   //Add contrast controls and display settings
                   displaySettings_.addChannel(channelName, bitDepth);
                   displayWindow_.addContrastControls(channelName, true);
                }
             }
         }

         if (!displayWindow_.contrastControlsInitialized()) {
            // no channels have been added, so make a default one for monochrome display
            int bitDepth = dataSource_.getImageBitDepth(axesPositions);
            displaySettings_.addChannel("", bitDepth);
            displayWindow_.addContrastControls("", true);
         }

         //expand the scrollbars with new images
         edtRunnablePool_.invokeLaterWithCoalescence(
                 new NDViewer.ExpandDisplayRangeCoalescentRunnable(axesPositions));
         //move scrollbars to new position
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   public void setAxisPosition(String axis, int position) {
      HashMap<String, Object> axes = new HashMap<>();
      axes.put(axis, position);
      setImageEvent(axes, true);
   }

   /**
    * Called when scrollbars move
    */
   public void setImageEvent(HashMap<String, Object> axes, boolean fromHuman) {
      if (axes != null && displayWindow_ != null) {
         for (String axis : axes.keySet()) {
            if (!displayWindow_.isScrollerAxisLocked(axis) || fromHuman) {
               viewCoords_.setAxisPosition(axis, axes.get(axis));
            }
         }
      }
      //Set channel
      if (axes.containsKey("channel")) {
         viewCoords_.setActiveChannel((String) axes.get("channel"));
      }
      //Update other channels if in single channel view mode
      if (!displaySettings_.isCompositeMode()) {
         //set all channels inactive except current one
         for (String c : stringAxes_.get("channel")) {
            displaySettings_.setActive(c, c.equals(viewCoords_.getActiveChannel()));
            displayWindow_.displaySettingsChanged();
         }
      }

      update();
   }

   public void onContrastUpdated() {
      update();
   }

   public void onAnimationToggle(AxisScroller scoller, boolean animate) {
      if (animationTimer_ != null) {
         animationTimer_.stop();
      }
      if (animate) {
         animationTimer_ = new Timer((int) (1000 / animationFPS_), new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               int newPos = (scoller.getPosition() + 1)
                       % (scoller.getMaximum() - scoller.getMinimum());
               HashMap<String, Integer> posMap = new HashMap<String, Integer>();
               setAxisPosition(scoller.getAxis(), newPos);
            }
         });
         animationTimer_.start();
      }
   }

   public void update() {
      if (displayCalculationExecutor_ == null) {
         return; // Not yet initialized
      }
      displayCalculationExecutor_.invokeAsLateAsPossibleWithCoalescence(new DisplayImageComputationRunnable());
   }

   public ViewerCanvas getCanvas() {
      return displayWindow_.getCanvas();
   }

   public void superlockAllScrollers() {
      displayWindow_.superlockAllScrollers();
   }

   public void unlockAllScroller() {
      displayWindow_.unlockAllScrollers();
   }

   public void showFolder() {
      try {
         File location = new File(dataSource_.getDiskLocation());
         if (isWindows()) {
            Runtime.getRuntime().exec("Explorer /n,/select," + location.getAbsolutePath());
         } else if (isMac()) {
            if (!location.isDirectory()) {
               location = location.getParentFile();
            }
            Runtime.getRuntime().exec(new String[]{"open", location.getAbsolutePath()});
         }
      } catch (IOException ex) {
         throw new RuntimeException(ex);
      }
   }

   static boolean isWindows() {
      String os = System.getProperty("os.name").toLowerCase();
      return (os.contains("win"));
   }

   static boolean isMac() {
      String os = System.getProperty("os.name").toLowerCase();
      return (os.contains("mac"));
   }

   public void setAnimateFPS(double doubleValue) {
      animationFPS_ = doubleValue;
      if (animationTimer_ != null) {
         ActionListener action = animationTimer_.getActionListeners()[0];
         animationTimer_.stop();
         animationTimer_ = new Timer((int) (1000 / animationFPS_), action);
         animationTimer_.start();
      }
   }

   public void abortAcquisition() {
      if (acq_ != null && !acq_.isFinished()) {
         int result = JOptionPane.showConfirmDialog(null, "Finish acquisition?",
                 "Finish Current Acquisition", JOptionPane.OK_CANCEL_OPTION);
         if (result == JOptionPane.OK_OPTION) {
            acq_.abort();
         }
      }
   }

   public void togglePauseAcquisition() {
      acq_.togglePaused();
   }

   public boolean isAcquisitionPaused() {
      return acq_.isPaused();
   }

   public void setOverlay(Overlay overlay) {
      displayWindow_.displayOverlay(overlay);
   }

   public void redrawOverlay() {
      //this will automatically trigger overlay redrawing in a coalescent fashion
      displayCalculationExecutor_.invokeAsLateAsPossibleWithCoalescence(
            new DisplayImageComputationRunnable());
   }

   public double getMagnification() {
      return viewCoords_.getMagnification();
   }

   public double getPixelSize() {
      //TODO: replace with pixel size read from image in case different pixel sizes
      return pixelSizeUm_;
   }

   public void showScaleBar(boolean selected) {
      overlayer_.setShowScaleBar(selected);
   }

   public void setCompositeMode(boolean selected) {
      if (stringAxes_ == null || stringAxes_.size() == 0) {
         // this seems to happen in a not reproducible way. not sure why, but seems safe to ignore
         return;
      }
      displaySettings_.setCompositeMode(selected);
      //select all channels if composite mode is being turned on
      if (selected) {
         for (String channel : stringAxes_.get("channel")) {
            displaySettings_.setActive(channel, true);
            displayWindow_.displaySettingsChanged();
         }
      } else {
         for (String channel : stringAxes_.get("channel")) {
            displaySettings_.setActive(channel, viewCoords_.getActiveChannel().equals(channel));
            displayWindow_.displaySettingsChanged();
         }
      }
      update();
   }

   public boolean isCompositMode() {
      return displaySettings_.isCompositeMode();
   }

   public DisplaySettings getDisplaySettingsObject() {
      return displaySettings_;
   }

   public List<String> getChannels() {
      if (stringAxes_ == null) {
         return null;
      }
      return stringAxes_.get("channel") == null ?
              Stream.of("").collect(Collectors.toList()) : stringAxes_.get("channel");
   }

   public JPanel getCanvasJPanel() {
      return getCanvas().getCanvas();
   }

   @Override
   public Object getAxisPosition(String axis) {
      return viewCoords_.getAxisPosition(axis);
   }

   @Override
   public Point2D.Double getViewOffset() {
      return viewCoords_.getViewOffset();
   }

   @Override
   public Point2D.Double getFullResSourceDataSize() {
      return viewCoords_.getFullResSourceDataSize();
   }

   @Override
   public void setViewOffset(double newX, double newY) {
      viewCoords_.setViewOffset(newX, newY);
   }

   public void showTimeLabel(boolean selected) {
      overlayer_.setShowTimeLabel(selected);
   }

   public void showZPositionLabel(boolean selected) {
      overlayer_.setShowZPosition(selected);
   }

   public String getCurrentT() {
      if (readTimeFunction_ == null) {
         return "Time metadata reader undefined";
      } else {
         long elapsed = readTimeFunction_.apply(currentMetadata_);
         long hours = elapsed / 60 / 60 / 1000;
         long minutes = elapsed / 60 / 1000;
         long seconds = elapsed / 1000;

         minutes = minutes % 60;
         seconds = seconds % 60;
         double s_frac = (elapsed % 1000) / 1000.0;
         String h = ("0" + hours).substring(("0" + hours).length() - 2);
         String m = ("0" + (minutes)).substring(("0" + minutes).length() - 2);
         String s = ("0" + (seconds)).substring(("0" + seconds).length() - 2);
         String label = h + ":" + m + ":" + s + String.format("%.3f", s_frac).substring(1)
               + " (H:M:S)";

         return label;
      }
   }

   public String getCurrentZPosition() {
      if (readZFunction_ == null) {
         return "Z metadata reader undefined";
      } else {
         try {
            return "" + readZFunction_.apply(currentMetadata_) + " \u00B5" + "m";
         } catch (Exception e) {
            return  "";
         }
      }
   }

   @Override
   public void setCustomCanvasMouseListener(CanvasMouseListenerInterface m) {
      displayWindow_.setCustomCanvasMouseListener(m);
   }

   @Override
   public Point2D.Double getDisplayImageSize() {
      return viewCoords_.getDisplayImageSize();
   }

   @Override
   public void setOverlayerPlugin(OverlayerPlugin overlayer) {
      overlayerPlugin_ = overlayer;
   }

   @Override
   public void addControlPanel(ControlsPanelInterface panel) {
      displayWindow_.addControlPanel(panel);
   }

   public boolean isIntegerAxis(String axis) {
      return !stringAxes_.containsKey(axis);
   }

   /**
    * A coalescent runnable to avoid excessively frequent update of the data
    * coords range in the UI
    */
   private class ExpandDisplayRangeCoalescentRunnable
           implements CoalescentRunnable {

      private final List<HashMap<String, Object>> newIamgeEvents = new ArrayList<>();
      private final List<String> activeChannels = new ArrayList<String>();

      ExpandDisplayRangeCoalescentRunnable(HashMap<String, Object> axisPosisitons) {
         newIamgeEvents.add(axisPosisitons);
         if (axisPosisitons.containsKey("channel")) {
            activeChannels.add((String) axisPosisitons.get("channel"));
         }
      }

      @Override
      public Class<?> getCoalescenceClass() {
         return getClass();
      }

      @Override
      public CoalescentRunnable coalesceWith(CoalescentRunnable another) {
         newIamgeEvents.addAll(
                 ((ExpandDisplayRangeCoalescentRunnable) another).newIamgeEvents);
         activeChannels.addAll(((ExpandDisplayRangeCoalescentRunnable) another).activeChannels);
         return this;
      }

      @Override
      public void run() {
         if (displayWindow_ != null) {
            displayWindow_.expandDisplayedRangeToInclude(newIamgeEvents, activeChannels);
         }
         setImageEvent(newIamgeEvents.get(newIamgeEvents.size() - 1), false);
         newIamgeEvents.clear();
      }
   }

   /**
    * Called when window is x-ed out by user.
    */
   public void requestToClose() {
      if (!SwingUtilities.isEventDispatchThread()) {
         SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
               requestToClose();
            }
         });
      } else {
         //check to stop acquisiton?, return here if the attempt to close window unsuccesslful
         if (acq_ != null && !acq_.isFinished()) {
            int result = JOptionPane.showConfirmDialog(null, "Finish acquisition?",
                    "Finish Current Acquisition", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
               acq_.abort();
            } else {
               return;
            }
         }

         close();
      }
   }

   /**
    *
    */
   public void close() {
      new Thread(new Runnable() {
         @Override
         public void run() {
            try {
               if (acq_ != null) {
                  //Finish acquisition on different thread to not slow EDT
                  acq_.abort(); //it may already be aborted but call this again to be sure
                  acq_.waitForCompletion();
                  dataSource_.close();
                  dataSource_ = null;
               }

            } catch (Exception e) {
               //not ,uch to do at this point
               e.printStackTrace();
            } finally {
               //Now all resources should be released, so evertthing can be shut down

               //make everything else close
               displayWindow_.onDisplayClose();

               displayCalculationExecutor_.shutdownNow();
               overlayCalculationExecutor_.shutdownNow();

               imageMaker_.close();
               imageMaker_ = null;

               overlayer_.shutdown();
               overlayer_ = null;

               if (animationTimer_ != null) {
                  animationTimer_.stop();
               }
               animationTimer_ = null;
               dataSource_ = null;
               displayWindow_ = null;
               viewCoords_ = null;

               edtRunnablePool_ = null;
               displaySettings_ = null;
               displayCalculationExecutor_ = null;
               overlayCalculationExecutor_ = null;
               acq_ = null;
               closed_ = true;
            }
         }
      }, "NDViewer closing thread").start();
   }

   public JSONObject getSummaryMD() {
      try {
         return new JSONObject(summaryMetadata_.toString());
      } catch (JSONException ex) {
         return null; //this shouldnt happen
      }
   }

   private class DisplayImageComputationRunnable implements CoalescentRunnable {

      DataViewCoords view_ = null;

      public DisplayImageComputationRunnable() {
         if (viewCoords_ != null) {
            view_ = viewCoords_.copy();
         }
      }

      @Override
      public Class<?> getCoalescenceClass() {
         return this.getClass();
      }

      @Override
      public CoalescentRunnable coalesceWith(CoalescentRunnable later) {
         return later; //Always update with newest image 
      }

      @Override
      public void run() {
         if (view_ == null) {
            return;
         }
         //This is where most of the calculation of creating a display image happens
         Image img = imageMaker_.makeOrGetImage(view_);
         JSONObject tags = imageMaker_.getLatestTags();
         currentMetadata_ = tags;

         HashMap<String, int[]> channelHistograms = imageMaker_.getHistograms();
         edtRunnablePool_.invokeAsLateAsPossibleWithCoalescence(new CanvasRepaintRunnable(img,
                 channelHistograms, view_, tags));
         //now send expensive overlay computation to overlay creation thread
      }
   }

   private class CanvasRepaintRunnable implements CoalescentRunnable {

      final Image img_;
      DataViewCoords view_;
      HashMap<String, int[]> hists_;
      JSONObject imageMD_;

      public CanvasRepaintRunnable(Image img, HashMap<String, int[]> hists,
                                   DataViewCoords view, JSONObject imageMD) {
         img_ = img;
         view_ = view;
         hists_ = hists;
         imageMD_ = imageMD;
      }

      @Override
      public Class<?> getCoalescenceClass() {
         return this.getClass();
      }

      @Override
      public CoalescentRunnable coalesceWith(CoalescentRunnable later) {
         return later;
      }

      @Override
      public void run() {
         displayWindow_.displayImage(img_, hists_, view_);
         displayWindow_.setImageMetadata(imageMD_);
         overlayer_.createOverlay(view_, overlayerPlugin_);
         displayWindow_.repaintCanvas();
      }

   }

}
