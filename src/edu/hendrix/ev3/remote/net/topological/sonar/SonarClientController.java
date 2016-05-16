package edu.hendrix.ev3.remote.net.topological.sonar;

import java.io.IOException;
import java.net.SocketException;
import java.util.HashMap;

import edu.hendrix.ev3.ai.topological.sonar.visualize.SonarPositionGrid;
import edu.hendrix.ev3.gui.Quittable;
import edu.hendrix.ev3.remote.net.ClientSender;
import edu.hendrix.ev3.remote.sonar.SonarPosition;
import edu.hendrix.ev3.util.Logger;
import edu.hendrix.ev3.util.Util;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

public class SonarClientController implements Quittable {
	@FXML
	Rectangle stop;
	
	@FXML
	Polygon forward;
	
	@FXML
	Polygon backward;
	
	@FXML
	Polygon left;
	
	@FXML
	Polygon right;
	
	@FXML
	Ellipse navigate;
	
	@FXML
	Label messages;
	
	@FXML
	TextField nodes;
	
	@FXML
	Button start;
	
	@FXML
	Button requestArchives;
	
	@FXML
	Button open;
	
	@FXML
	ChoiceBox<String> archives;
	
	@FXML
	AnchorPane keyPane;
	
	@FXML
	TextField leftMin;
	@FXML
	TextField leftMax;
	@FXML
	TextField centerMin;
	@FXML
	TextField centerMax;
	@FXML
	TextField rightMin;
	@FXML
	TextField rightMax;
	
	@FXML
	TextField leftCurrent;
	@FXML
	TextField centerCurrent;
	@FXML
	TextField rightCurrent;
	
	ClientSender<CommandMessage,RobotMessage> sender;
	HashMap<KeyCode,Runnable> key2msg = new HashMap<>();
	HashMap<KeyCode,Shape> key2shape = new HashMap<>();
	HashMap<KeyCode,Paint> key2color = new HashMap<>();
	SonarPositionGrid sonarGrid = new SonarPositionGrid();
	
	@FXML
	public void initialize() {
		nodes.setText("100");
		setupSender();
		setupKeys();
		setupPane();
		setupSonar();
	}
	
	void setupSonar() {
		sonarGrid.addMatchingFields(SonarPosition.LEFT, leftMin, leftMax);
		sonarGrid.addMatchingFields(SonarPosition.CENTER, centerMin, centerMax);
		sonarGrid.addMatchingFields(SonarPosition.RIGHT, rightMin, rightMax);
		Util.assertState(sonarGrid.allTextFieldsPresent(), "Missing fields");		
	}

	void setupSender() {
		try {
			sender = new ClientSender<>(RobotMessage::new, RobotMessage.NUM_BYTES);
			sender.addUpdateListener(tag -> Platform.runLater(() -> {
				if (!tag.isInitialized()) {
					messages.setText("Not initialized");
				} else {
					messages.setText("Acknowledged");
				}}));
		} catch (SocketException e) {
			e.printStackTrace();
			System.exit(1);
		}		
	}
	
	void send(Command cmd) {
		send(new CommandMessage(cmd));
	}
	
	void send(CommandMessage msg) {
		keyPane.requestFocus();
		try {
			sender.send(msg);
			System.out.println("Sending " + msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
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

	void setupKeys() {
		setupKey(KeyCode.I, forward, () -> send(Command.FORWARD));
		setupKey(KeyCode.J, left, () -> send(Command.LEFT));
		setupKey(KeyCode.L, right, () -> send(Command.RIGHT));
		setupKey(KeyCode.K, stop, () -> send(Command.STOP));
		setupKey(KeyCode.M, backward, () -> send(Command.BACKWARD));
		setupKey(KeyCode.N, navigate, () -> CommandMessage.makeNavigate(sonarGrid.getAllConstraints()));
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
	
	@FXML
	public void start() {
		send(CommandMessage.makeStartup(Integer.parseInt(nodes.getText())));
	}
	
	@FXML
	public void requestArchives() {
		
	}
	
	@FXML
	public void openArchive() {
		
	}

	@Override
	public void quit() {
		send(Command.QUIT);
	}
}
