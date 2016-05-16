package edu.hendrix.ev3.remote.net.topological.autosonar;

import java.io.IOException;
import java.net.SocketException;
import java.time.LocalDateTime;
import java.util.EnumSet;

import edu.hendrix.ev3.ai.topological.sonar.visualize.SonarPositionGrid;
import edu.hendrix.ev3.gui.Quittable;
import edu.hendrix.ev3.remote.Move;
import edu.hendrix.ev3.remote.net.ClientSender;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class AutoSonarController implements Quittable {
	@FXML
	VBox moves;
	
	@FXML
	VBox overrides;
	
	@FXML
	GridPane constraints;
	
	@FXML
	Button start;
	
	@FXML
	TextField nodes;
	
	@FXML
	TextField exploreConst;
	
	@FXML
	ChoiceBox<Move> moveAtGoal;
	
	MoveApprovalBoxes movesToUse;
	MoveOverrideButtons overridesToUse;
	SonarPositionGrid grid;
	ClientSender<Message,Response> sender;
	
	public static final int DEFAULT_NODES = 2;
	public static final double DEFAULT_CHANGE_PROB = 0.05;
	public static final int DEFAULT_EXPLORE = 30;
	
	@FXML
	void start() {
		send(Cmd.START);
	}
	
	@FXML
	void initialize() {
		setupMoves();
		setupOverrides();
		setupSender();
		setupGoalMove();
		grid = SonarPositionGrid.makeFromScratch(constraints);
		nodes.setText(Integer.toString(DEFAULT_NODES));
		exploreConst.setText(Integer.toString(DEFAULT_EXPLORE));
	}
	
	void setupMoves() {
		moves.getChildren().add(new Label("Available moves"));
		movesToUse = new MoveApprovalBoxes(moves, EnumSet.of(Move.FORWARD, Move.BACKWARD, Move.LEFT, Move.RIGHT, Move.SPIN_LEFT, Move.SPIN_RIGHT));
	}
	
	void setupOverrides() {
		overrides.getChildren().add(new Label("Override move"));
		overridesToUse = new MoveOverrideButtons(overrides);
		Button overrider = new Button("Override");
		overrider.setOnAction(event -> send(Message.makeOverride(overridesToUse.getSelectedMove())));
		overrides.getChildren().add(overrider);
		Button releaser = new Button("Release");
		releaser.setOnAction(event -> send(Message.makeRelease()));
		overrides.getChildren().add(releaser);
	}
	
	void setupSender() {
		try {
			sender = new ClientSender<>(Response::new, Response.NUM_BYTES);
		} catch (SocketException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	void setupGoalMove() {
		for (Move move: Move.values()) {
			moveAtGoal.getItems().add(move);
		}
		moveAtGoal.getSelectionModel().select(Move.STOP);
	}

	@Override
	public void quit() {
		send(Cmd.END);
	}
	
	void send(Cmd cmd) {
		send(makeMessage(cmd));
	}
	
	void send(Message msg) {
		try {
			sender.send(msg);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	int fromText(TextField intText, int noText) {
		try {
			return Integer.parseInt(intText.getText());
		} catch (NumberFormatException nfe) {
			return noText;
		}
	}
	
	double fromText(TextField doubleText, double noText) {
		try {
			return Double.parseDouble(doubleText.getText());
		} catch(NumberFormatException nfe) {
			return noText;
		}
	}
	
	Message makeMessage(Cmd cmd) {
		return new Message(cmd, 
				fromText(nodes, DEFAULT_NODES), 
				fromText(exploreConst, DEFAULT_EXPLORE), 
				movesToUse.getSelectedMoves(), 
				moveAtGoal.getValue(),
				LocalDateTime.now(),
				grid.getAllConstraints());
	}
}
