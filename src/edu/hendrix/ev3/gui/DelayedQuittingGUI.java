package edu.hendrix.ev3.gui;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class DelayedQuittingGUI extends Application {
	private static int delay;
	private static String fxmlFilename;
	
	public static void setup(int delay, String fxmlFilename) {
		DelayedQuittingGUI.delay = delay;
		DelayedQuittingGUI.fxmlFilename = fxmlFilename;
	}
	
	@Override
	public void start(Stage primaryStage) {
		try {
			FXMLLoader loader = new FXMLLoader();
			BorderPane root = (BorderPane) loader.load(getClass().getResource(fxmlFilename).openStream());
			primaryStage.setScene(new Scene(root));
			primaryStage.show();
			
			Quittable controller = loader.getController();
			primaryStage.setOnCloseRequest(we -> {
				controller.quit(); 
				try {
					Thread.sleep(delay);
				} catch (Exception e) { 
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					System.exit(0);
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
