package edu.hendrix.ev3.webcamdemo.bsoc;

import edu.hendrix.ev3.gui.Quittable;
import edu.hendrix.ev3.util.Util;
import edu.hendrix.ev3.webcamdemo.ClusterableImage;
import edu.hendrix.ev3.webcamdemo.ColorChannel;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

import java.awt.image.BufferedImage;
import java.util.function.BiFunction;
import java.util.function.IntBinaryOperator;

public class BSOCDemoController implements Quittable {
	
	int numClusters() throws NumberFormatException {
		return Integer.parseInt(numNodes.getText());
	}
	
	int shrinkFactor() {
		return shrinkFactor.getSelectionModel().getSelectedItem();
	}
	
	int clusterNumber() {
		try {
			return Integer.parseInt(clusterNumber.getText());
		} catch (NumberFormatException nfe) {
			clusterNumber.setText("0");
			return 0;
		}
	}
	
	@FXML Canvas image;
	
	@FXML Canvas cluster;
	@FXML TextField clusterNumber;
	@FXML Button left;
	@FXML Button right;
	
	@FXML TextField frameRate, trainingTime;
	@FXML TextField width;
	@FXML TextField height;
	
	@FXML TextField numNodes;
	@FXML ChoiceBox<Integer> shrinkFactor;
	@FXML Button start;
	
	BSOCThread renderer;
	
	@FXML
	void initialize() {
		start.setOnAction(event -> {
			try {
				if (isGrabbing()) {
					stop();
				} else {
					if (renderer != null) {renderer.quit();}
					renderer = new BSOCThread(numClusters(), shrinkFactor(), 
							(img, cls, rate, train, node) -> {
								render(img, image);
								frameRate.setText(String.format("%4.2f", rate));
								trainingTime.setText(String.format("%4.2f", train));
								render(cls, cluster);
								clusterNumber.setText(Integer.toString(node));		
								width.setText(Integer.toString(renderer.getWidth()));
								height.setText(Integer.toString(renderer.getHeight()));
							});
					renderer.start();
					start.setText("Stop");
				}
			} catch (NumberFormatException nfe) {
				numNodes.setText("'" + numNodes.getText() + "' not integer");
			}
		});
		
		left.setOnAction(event -> updateCluster(Util::modDown));
		right.setOnAction(event -> updateCluster(Util::modUp));
		
		clusterNumber.setEditable(false);
		frameRate.setEditable(false);
		trainingTime.setEditable(false);
		width.setEditable(false);
		height.setEditable(false);
		
		for (int i: new int[]{1, 2, 4, 8, 16, 32}) {
			shrinkFactor.getItems().add(i);
		}
		shrinkFactor.getSelectionModel().select(0);
	}
	
	public boolean isGrabbing() {
		return renderer != null && renderer.isRunning();
	}
	
	public void stop() {
		if (isGrabbing()) {
			renderer.quit();
			start.setText("Start");
		}
	}
	
	public void updateCluster(IntBinaryOperator updater) {
		if (renderer != null && renderer.numNodes() > 0) {
			stop();
			int updated = updater.applyAsInt(clusterNumber(), renderer.numNodes());
			render(renderer.getIdealInputFor(updated), cluster);
			clusterNumber.setText(Integer.toString(updated));		
		}
	}

	@Override
	public void quit() {
		if (renderer != null) renderer.quit();
	}
	
	public static <T> void render(T img, BiFunction<Integer,Integer,Color> renderer, int width, int height, Canvas canv) {
		double cellWidth = canv.getWidth() / width;
		double cellHeight = canv.getHeight() / height;
		GraphicsContext g = canv.getGraphicsContext2D();
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				g.setFill(renderer.apply(x, y));
				g.fillRect(x * cellWidth, y * cellHeight, cellWidth, cellHeight);
			}
		}
	}
	
	public static void render(BufferedImage img, Canvas canv) {
		render(img, (x, y) -> ColorChannel.buildColorFrom(img.getRGB(x, y)), img.getWidth(), img.getHeight(), canv);
	}

	public static void render(ClusterableImage img, Canvas canv) {
		render(img, (x, y) -> ColorChannel.buildColorFrom(img.getRGB(x, y)), img.getWidth(), img.getHeight(), canv);
	}
}
