package edu.hendrix.ev3.remote.net.topological;

import java.io.IOException;
import java.net.SocketException;
import java.time.LocalDateTime;
import java.util.HashMap;

import edu.hendrix.ev3.ai.cluster.yuv.AdaptedYUYVImage;
import edu.hendrix.ev3.gui.Quittable;
import edu.hendrix.ev3.gui.videoview.AdaptedYUYVRenderer;
import edu.hendrix.ev3.remote.net.ClientSender;
import edu.hendrix.ev3.util.CircularList;
import edu.hendrix.ev3.util.Logger;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

public class TopoClientController implements Quittable {
	@FXML
	Polygon forward;
	@FXML
	Polygon backward;
	@FXML
	Polygon left;
	@FXML
	Polygon right;
	@FXML
	Rectangle stop;
	@FXML
	AnchorPane keyPane;
	
	@FXML
	TextField numNodes;
	@FXML
	TextField shrink;
	@FXML
	Button start;
	@FXML
	Shape photo;
	@FXML
	Shape wander;
	@FXML
	Shape navigate;
	@FXML
	Shape prev;
	@FXML
	Shape next;
	@FXML
	Canvas image;
	@FXML
	Label messages;
	
	HashMap<KeyCode,Runnable> key2msg = new HashMap<>();
	HashMap<KeyCode,Shape> key2shape = new HashMap<>();
	HashMap<KeyCode,Paint> key2color = new HashMap<>();
	CircularList<AdaptedYUYVImage> photos = new CircularList<>();
	
	ClientSender<CommandMessage,RobotMessage> sender;
	
	@FXML
	ChoiceBox<LocalDateTime> archives;
	@FXML
	Button open;
	@FXML
	Button requestArchives;
	
	@FXML
	void initialize() {
		shrink.setText("10");
		numNodes.setText("100");
		setupSender();
		setupKeys();
		setupPane();
	}
	
	@FXML
	void start() {
		try {
			send(CommandMessage.makeStartup(Integer.parseInt(numNodes.getText()), Integer.parseInt(shrink.getText())));
		} catch (NumberFormatException nfe) {
			nfe.printStackTrace();
		}
		keyPane.requestFocus();
	}
	
	@Override
	public void quit() {
		send(new CommandMessage(Command.QUIT));
	}
	
	void setupSender() {
		try {
			sender = new ClientSender<>(RobotMessage::new, RobotMessage.NUM_BYTES);
			sender.addUpdateListener(tag -> Platform.runLater(() -> {
				if (tag.hasArchives()) {
					messages.setText("Archives received");
					for (int i = 0; i < tag.numArchives(); i++) {
						archives.getItems().add(tag.getArchive(i));
					}
					archives.getSelectionModel().selectLast();
				} else if (!tag.isInitialized()) {
					messages.setText("Not initialized");
				} else if (tag.atGoal()) {
					messages.setText("Goal reached");
				} else {
					messages.setText("Acknowledged");
				}}));
			sender.addUpdateListener(tag -> {if (tag.hasPhoto()) {photos.add(tag.getPhoto()); updatePhoto();}});
		} catch (SocketException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	void setupKeys() {
		setupKey(KeyCode.I, forward, () -> send(Command.FORWARD));
		setupKey(KeyCode.J, left, () -> send(Command.LEFT));
		setupKey(KeyCode.K, stop, () -> send(Command.STOP));
		setupKey(KeyCode.L, right, () -> send(Command.RIGHT));
		setupKey(KeyCode.M, backward, () -> send(Command.BACKWARD));
		
		setupKey(KeyCode.P, photo, () -> send(Command.PHOTO));
		setupKey(KeyCode.N, navigate, () -> send(CommandMessage.makeNavigate(photos.getCurrentIndex())));
		setupKey(KeyCode.W, wander, () -> send(Command.WANDER));
		setupKey(KeyCode.A, prev, () -> {photos.backward();updatePhoto();});
		setupKey(KeyCode.D, next, () -> {photos.forward();updatePhoto();});
	}
	
	void setupPane() {
		keyPane.setOnKeyPressed(event -> {
			KeyCode key = event.getCode();
			if (key2msg.containsKey(key)) {
				Logger.ClientLog.log(key + " pressed");
				key2shape.get(key).setFill(Color.YELLOW);
				key2msg.get(key).run();
			}
		});
		
		keyPane.setOnKeyReleased(event -> {
			KeyCode key = event.getCode();
			if (key2shape.containsKey(key)) {
				key2shape.get(key).setFill(key2color.get(key));
			}
		});
		
		keyPane.setOnMouseEntered(m -> keyPane.requestFocus());		
	}
	
	void setupKey(KeyCode key, Shape target, Runnable cmd) {
		key2shape.put(key, target);
		key2color.put(key, target.getFill());
		key2msg.put(key, cmd);
		target.setOnMouseClicked(event -> cmd.run());
		plotLetter(key, target);
	}
	
	void plotLetter(KeyCode key, Shape target) {
		Label keyLabel = new Label(key.getName());
		keyPane.getChildren().add(keyLabel);
		keyLabel.setLayoutX(target.getLayoutX() - 5);
		keyLabel.setLayoutY(target.getLayoutY() - 15);
	}
	
	void send(CommandMessage msg) {
		try {
			//System.out.println("Sending: [" + msg.toString() + "]");
			sender.send(msg);
			messages.setText("Waiting...");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	void send(Command cmd) {
		send(new CommandMessage(cmd));
	}
	
	void updatePhoto() {
		if (photos.size() > 0) {
			Platform.runLater(() -> AdaptedYUYVRenderer.placeOnCanvas(photos.getCurrentItem(), image));
		}
	}
	
	@FXML
	void requestArchives() {
		send(CommandMessage.requestArchives(LocalDateTime.now()));
	}
	
	@FXML
	void openArchive() {
		if (archives.getSelectionModel().getSelectedItem() != null)
			send(CommandMessage.makeFromArchive(archives.getSelectionModel().getSelectedItem()));
	}
}
