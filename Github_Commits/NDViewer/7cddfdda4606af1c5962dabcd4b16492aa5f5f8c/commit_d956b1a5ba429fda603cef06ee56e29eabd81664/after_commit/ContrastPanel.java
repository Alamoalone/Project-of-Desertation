///////////////////////////////////////////////////////////////////////////////
//FILE:          ContrastPanel.java
//PROJECT:       Micro-Manager
//SUBSYSTEM:     mmstudio
//-----------------------------------------------------------------------------
//
// AUTHOR:       Henry Pinkard, henry.pinkard@gmail.com, 2012
//
// COPYRIGHT:    University of California, San Francisco, 2012
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
//

package org.micromanager.ndviewer.internal.gui.contrast;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.HashMap;
import java.util.prefs.Preferences;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.micromanager.ndviewer.main.NDViewer;

/**
 * @author Henry Pinkard This class is a singleton instance of the component in
 *     the contrast tab of the metadata panel. It has a single instance of the
 *     controls on top, and changes which histograms are displayed based on the
 *     frontmost window
 */
public class ContrastPanel extends JPanel {

   private static final String PREF_AUTOSTRETCH = "stretch_contrast";
   private static final String PREF_REJECT_OUTLIERS = "reject_outliers";
   private static final String PREF_REJECT_FRACTION = "reject_fraction";
   private static final String PREF_LOG_HIST = "log_hist";
   private static final String PREF_SYNC_CHANNELS = "sync_channels";
   private static final String PREF_COMPOSITE = "composite";
   protected JScrollPane histDisplayScrollPane_;
   private JCheckBox compositeCheckBox_;
   private JCheckBox autostretchCheckBox_;
   private JCheckBox rejectOutliersCheckBox_;
   private JSpinner rejectPercentSpinner_;
   private JCheckBox logHistCheckBox_;
   private JCheckBox syncChannelsCheckBox_;
   private final Preferences prefs_;
   protected MultiChannelHistograms histograms_;
   //volatile because accessed by overlayer creation thread
   private NDViewer display_;
   private JPanel contentPanel_;
   private boolean initializing_ = true;

   public ContrastPanel(NDViewer display) {
      histograms_ = new MultiChannelHistograms(display, this);
      display_ = display;
      contentPanel_ = createGUI();
      this.setLayout(new BorderLayout());
      this.add(contentPanel_, BorderLayout.CENTER);
      prefs_ = display.getPreferences();
      initializeHistogramDisplayArea();
      showCurrentHistograms();
      initializing_ = false;
   }

   public void onDisplayClose() {
      histograms_.onDisplayClose();
      display_ = null;
      contentPanel_ = null;
      histograms_ = null;
   }

   public void addContrastControls(String channelName) {
      histograms_.addContrastControls(channelName);
   }

   public void removeContrastControls(String channelName) {
      histograms_.removeContrastControls(channelName);
   }

   private void initializeHistogramDisplayArea() {
      histDisplayScrollPane_.setHorizontalScrollBarPolicy(
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
      histDisplayScrollPane_.getVerticalScrollBar().setUnitIncrement(8);
      showCurrentHistograms();
      configureControls();
   }

   private void showCurrentHistograms() {
      histDisplayScrollPane_.setViewportView(
              histograms_ != null ? histograms_ : new JPanel());
      if (histograms_ != null) {
         histDisplayScrollPane_.setVerticalScrollBarPolicy(
               ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
      } else {
         histDisplayScrollPane_.setVerticalScrollBarPolicy(
               ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
      }
      this.repaint();
   }

   public HistogramControlsState createDefaultControlsState() {
      HistogramControlsState state = new HistogramControlsState();
      state.autostretch = prefs_.getBoolean(PREF_AUTOSTRETCH, true);
      state.percentToIgnore = prefs_.getDouble(PREF_REJECT_FRACTION, 2);
      state.logHist = prefs_.getBoolean(PREF_LOG_HIST, false);
      state.composite = prefs_.getBoolean(PREF_COMPOSITE, true);
      state.ignoreOutliers = prefs_.getBoolean(PREF_REJECT_OUTLIERS, false);
      state.syncChannels = prefs_.getBoolean(PREF_SYNC_CHANNELS, false);
      return state;
   }

   private void configureControls() {
      logHistCheckBox_.setEnabled(true);
      syncChannelsCheckBox_.setEnabled(true);
      //load control state from prefs
      HistogramControlsState state = createDefaultControlsState();
      autostretchCheckBox_.setSelected(state.autostretch);
      logHistCheckBox_.setSelected(state.logHist);
      rejectPercentSpinner_.setValue(state.percentToIgnore);
      rejectPercentSpinner_.setEnabled(state.ignoreOutliers);
      rejectOutliersCheckBox_.setSelected(state.ignoreOutliers);
      syncChannelsCheckBox_.setSelected(state.syncChannels);
      compositeCheckBox_.setSelected(state.composite);
      //sync display settings with this
      display_.getDisplaySettingsObject().setAutoscale(autostretchCheckBox_.isSelected());
      display_.getDisplaySettingsObject().setIgnoreOutliers(rejectOutliersCheckBox_.isSelected());
      display_.getDisplaySettingsObject().setLogHist(logHistCheckBox_.isSelected());
      display_.getDisplaySettingsObject().setIgnoreOutliersPercentage(
            (double) rejectPercentSpinner_.getValue());
      display_.getDisplaySettingsObject().setSyncChannels(syncChannelsCheckBox_.isSelected());
   }

   private void saveCheckBoxStates() {
      if (initializing_) {
         return;
      }
      prefs_.putBoolean(PREF_AUTOSTRETCH, autostretchCheckBox_.isSelected());
      prefs_.putBoolean(PREF_LOG_HIST, logHistCheckBox_.isSelected());
      prefs_.putBoolean(PREF_COMPOSITE, compositeCheckBox_.isSelected());
      prefs_.putDouble(PREF_REJECT_FRACTION, (Double) rejectPercentSpinner_.getValue());
      prefs_.putBoolean(PREF_REJECT_OUTLIERS, rejectOutliersCheckBox_.isSelected());
      prefs_.putBoolean(PREF_SYNC_CHANNELS, syncChannelsCheckBox_.isSelected());

      if (display_ == null) {
         return;
      }
      display_.getDisplaySettingsObject().setAutoscale(autostretchCheckBox_.isSelected());
      display_.getDisplaySettingsObject().setIgnoreOutliers(rejectOutliersCheckBox_.isSelected());
      display_.getDisplaySettingsObject().setLogHist(logHistCheckBox_.isSelected());
      display_.getDisplaySettingsObject().setIgnoreOutliersPercentage(
            (double) rejectPercentSpinner_.getValue());
      display_.getDisplaySettingsObject().setSyncChannels(syncChannelsCheckBox_.isSelected());
   }

   private JPanel createGUI() {
      final JPanel controlPanel = new JPanel();
      compositeCheckBox_ = new JCheckBox("Composite");
      autostretchCheckBox_ = new JCheckBox();
      rejectOutliersCheckBox_ = new JCheckBox();
      rejectPercentSpinner_ = new JSpinner();
      logHistCheckBox_ = new JCheckBox();

      histDisplayScrollPane_ = new JScrollPane();

      this.setPreferredSize(new Dimension(400, 594));

      compositeCheckBox_.setSelected(display_.isCompositMode());
      compositeCheckBox_.addChangeListener(new ChangeListener() {
         @Override
         public void stateChanged(ChangeEvent e) {
            display_.setCompositeMode(compositeCheckBox_.isSelected());
         }
      });

      compositeCheckBox_.setToolTipText("Show multiple channels at once or one at a time");

      autostretchCheckBox_.setText("Autostretch");
      autostretchCheckBox_.addChangeListener(new ChangeListener() {

         @Override
         public void stateChanged(ChangeEvent evt) {
            autostretchCheckBoxStateChanged();
         }
      });
      rejectOutliersCheckBox_.setText("ignore %");
      rejectOutliersCheckBox_.addActionListener(new java.awt.event.ActionListener() {

         @Override
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            rejectOutliersCheckBoxAction();
         }
      });

      rejectPercentSpinner_.setFont(new java.awt.Font("Lucida Grande", 0, 10));
      rejectPercentSpinner_.addChangeListener(new ChangeListener() {

         @Override
         public void stateChanged(ChangeEvent evt) {
            rejectPercentageChanged();
         }
      });
      rejectPercentSpinner_.addKeyListener(new java.awt.event.KeyAdapter() {

         @Override
         public void keyPressed(java.awt.event.KeyEvent evt) {
            rejectPercentageChanged();
         }
      });
      rejectPercentSpinner_.setModel(new SpinnerNumberModel(0.02, 0., 100., 0.1));

      logHistCheckBox_.setText("Log hist");
      logHistCheckBox_.addActionListener(new java.awt.event.ActionListener() {

         @Override
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            logScaleCheckBoxActionPerformed();
         }
      });

      syncChannelsCheckBox_ = new JCheckBox("Sync channels");
      syncChannelsCheckBox_.addChangeListener(new ChangeListener() {

         @Override
         public void stateChanged(ChangeEvent e) {
            syncChannelsCheckboxAction();
         }
      });
      final JPanel outerPanel = new JPanel(new BorderLayout());

      controlPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
      controlPanel.add(compositeCheckBox_);
      controlPanel.add(autostretchCheckBox_);
      controlPanel.add(syncChannelsCheckBox_);
      controlPanel.add(rejectOutliersCheckBox_);
      controlPanel.add(rejectPercentSpinner_);
      controlPanel.add(logHistCheckBox_);

      outerPanel.add(controlPanel, BorderLayout.PAGE_START);
      outerPanel.add(histDisplayScrollPane_, BorderLayout.CENTER);

      return outerPanel;
   }

   private void syncChannelsCheckboxAction() {
      if (!syncChannelsCheckBox_.isEnabled()) {
         return;
      }
      boolean synced = syncChannelsCheckBox_.isSelected();
      if (synced) {
         autostretchCheckBox_.setSelected(false);
         if (histograms_ != null) {
            display_.getDisplaySettingsObject().setChannelContrastFromFirst();
         }
      } else {
         autostretchCheckBox_.setEnabled(true);
      }
      saveCheckBoxStates();
      display_.onContrastUpdated();
   }

   private void autostretchCheckBoxStateChanged() {
      rejectOutliersCheckBox_.setEnabled(autostretchCheckBox_.isSelected());
      boolean rejectem = rejectOutliersCheckBox_.isSelected() && autostretchCheckBox_.isSelected();
      rejectPercentSpinner_.setEnabled(rejectem);
      saveCheckBoxStates();
      if (autostretchCheckBox_.isSelected()) {
         syncChannelsCheckBox_.setSelected(false);
         if (histograms_ != null) {
            histograms_.autoscaleAllChannels();
         }
      } else {
         rejectOutliersCheckBox_.setSelected(false);
      }
   }

   private void rejectOutliersCheckBoxAction() {
      saveCheckBoxStates();
      rejectPercentSpinner_.setEnabled(rejectOutliersCheckBox_.isSelected());
      if (histograms_ != null) {
         histograms_.rejectOutliersChangeAction();
      }
   }

   private void rejectPercentageChanged() {
      saveCheckBoxStates();
      if (histograms_ != null) {
         histograms_.rejectOutliersChangeAction();
      }
   }

   private void logScaleCheckBoxActionPerformed() {
      display_.onContrastUpdated();
      saveCheckBoxStates();
   }

   public void disableAutostretch() {
      display_.getDisplaySettingsObject().setAutoscale(false);
      autostretchCheckBox_.setSelected(false);
      saveCheckBoxStates();
   }

   public void updateHistogramData(HashMap<String, int[]> hists) {
      histograms_.updateHistogramData(hists);
   }

   public void displaySettingsChanged() {
      histograms_.displaySettingsChanged();
   }


}
