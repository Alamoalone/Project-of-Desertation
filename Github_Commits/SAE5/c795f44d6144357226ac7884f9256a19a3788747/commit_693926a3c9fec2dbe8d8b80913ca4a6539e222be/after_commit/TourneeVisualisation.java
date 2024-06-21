package org.SAE.Tournee;

import org.SAE.Adresse.Adresse;
import org.SAE.Main.UButton;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Objects;

public class TourneeVisualisation extends JXMapViewer {
	ArrayList<double[]> points = new ArrayList<>();
	ArrayList<double[]> tmpPoints = new ArrayList<>();

	ArrayList<String> names = new ArrayList<>();

	public TourneeVisualisation() {
		super();
		JFrame frame = new JFrame("JXMapviewer2 Example 1");

		frame.add(this, BorderLayout.CENTER);
		setTileFactory(new DefaultTileFactory(new OSMTileFactoryInfo()));

		UButton button = new UButton("Save as image");
		button.addActionListener(e -> {
			try {
				Image image = createImage(getWidth(), getHeight());
				Graphics g = image.getGraphics();
				paint(g);
				ImageIO.write((RenderedImage) image, "png", new File("map.png"));
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		});
		frame.add(button, BorderLayout.SOUTH);
		frame.getContentPane().add(this);
		frame.setSize(800, 600);
		frame.setVisible(true);
	}


	public static double[] getGeo(String address) {
		try {
			String apiURL = "https://nominatim.openstreetmap.org/search?format=json&q=" + address.replace(" ", "+");
			JSONArray jsonArray = getJsonArray(apiURL);
			if (jsonArray.length() > 0) {
				JSONObject jsonObject = jsonArray.getJSONObject(0);
				double lat = jsonObject.getDouble("lat");
				double lon = jsonObject.getDouble("lon");
				return new double[]{lat, lon};
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null; // Or handle error appropriately
	}

	private static JSONArray getJsonArray(String apiURL) throws IOException, JSONException {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
						.uri(URI.create(apiURL))
						.header("User-Agent", "Mozilla/5.0")
						.build();

		HttpResponse<String> response = null;
		try {
			response = client.send(request, HttpResponse.BodyHandlers.ofString());
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		return new JSONArray(response.body());
	}

	public void addMarker(){
		// get max distance between points
		double maxDistance = 0;
		double latCenter = 0;
		double lonCenter = 0;
		// distance for each couple of points
		double[][] distances = new double[points.size()][points.size()];
		for (int i = 0; i < points.size(); i++) {
			double[] point = points.get(i);
			for (int j = i + 1; j < points.size(); j++) {
				double[] point2 = points.get(j);
				Point2D point2D = convertGeoPositionToPoint(new GeoPosition(point[0], point[1]));
				Point2D point2D2 = convertGeoPositionToPoint(new GeoPosition(point2[0], point2[1]));
				double distance = Math.sqrt(Math.pow(point2D.getX() - point2D2.getX(), 2) + Math.pow(point2D.getY() - point2D2.getY(), 2));
				distances[i][j] = distance;
				if (distance > maxDistance) maxDistance = distance;
			}
			latCenter += point[0];
			lonCenter += point[1];
		}
		int size = 0;
		int offset = 0;
		// ignore points that are too close to each other (1% of max distance)
		tmpPoints = new ArrayList<>(points);
		names = new ArrayList<>();
		for (int i = 0; i < tmpPoints.size(); i++) {
			double[] point = tmpPoints.get(i);
			boolean ignore = false;
			for (int j = i + 1; j < tmpPoints.size(); j++) {
				if (distances[i][j] < maxDistance * 0.01) {
					names.add((i+offset) + " " + (j+offset));
					offset++;
					ignore = true;
					break;
				}
			}
			if (!ignore) {
				tmpPoints.set(size,point);
				size++;
				names.add(String.valueOf(i+offset));
			}
		}
		tmpPoints = new ArrayList<>(tmpPoints.subList(0, size));
		latCenter /= points.size();
		lonCenter /= points.size();
		setAddressLocation(new GeoPosition(latCenter, lonCenter));
		setZoom((int) (Math.log10(maxDistance) * 2));
		repaint();
	}
	public void addMarker(double lat, double lon) {
		// add marker
		points.add(new double[]{lat, lon});
		addMarker();
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		double width = getWidth();
		double height = getHeight();
		g.setFont(new Font("TimesRoman", Font.PLAIN, 20));
		for (int i = 0; i < tmpPoints.size(); i++) {
			double[] point = tmpPoints.get(i);
			Point2D point2D = convertGeoPositionToPoint(new GeoPosition(point[0], point[1]));
			double x = point2D.getX() - getOffset().getX();
			double y = point2D.getY() - getOffset().getY();
			g.drawOval((int) x + (int) width / 2 - 25, (int) y + (int) height / 2 - 25, 50, 50);
			g.drawString(names.get(i), (int) x + (int) width / 2 - 30, (int) y + (int) height / 2 - 30);
		}
		// draw arrow
		for (int i = 0; i < tmpPoints.size(); i++) {
			double[] point = tmpPoints.get(i);
			Point2D point2D = convertGeoPositionToPoint(new GeoPosition(point[0], point[1]));
			double x = point2D.getX() - getOffset().getX();
			double y = point2D.getY() - getOffset().getY();
			double[] point2 = tmpPoints.get((i+1) % ( tmpPoints.size()));
			Point2D point2D2 = convertGeoPositionToPoint(new GeoPosition(point2[0], point2[1]));
			int x2 = (int) (point2D2.getX() - getOffset().getX());
			int y2 = (int) (point2D2.getY() - getOffset().getY());
			g.drawLine((int) x + (int) width / 2, (int) y + (int) height / 2, x2 + (int) width / 2, y2 + (int) height / 2);
		}
	}

	private Point2D getOffset() {
		return convertGeoPositionToPoint(getAddressLocation());
	}
}
