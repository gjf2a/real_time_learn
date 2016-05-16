package edu.hendrix.ev3.gui.actionselector;

import java.net.SocketException;
import java.time.LocalDateTime;

import edu.hendrix.ev3.gui.Quittable;
import edu.hendrix.ev3.remote.Move;
import edu.hendrix.ev3.remote.net.ClientSender;
import edu.hendrix.ev3.remote.net.actionselector.ActionSelectorCommand;
import edu.hendrix.ev3.remote.net.actionselector.ActionSelectorReply;
import edu.hendrix.ev3.remote.net.actionselector.Mode;
import edu.hendrix.ev3.util.Duple;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class RemoteControlController implements Quittable {
	@FXML
	VBox moveButtons;
	
	@FXML
	Button create;
	@FXML
	TextField numNodes;
	@FXML
	ChoiceBox<Integer> shrinkFactor;
	
	@FXML
	Button retrieveStored;
	@FXML
	ChoiceBox<Duple<LocalDateTime,Integer>> storedNames;
	@FXML
	Button applyStored;
	
	ClientSender<ActionSelectorCommand,ActionSelectorReply> sender;
	
	private byte tag = 0;
	
	@FXML
	void initialize() {
		try {
			for (int s: new int[]{1, 2, 4, 8, 10, 20}) {
				shrinkFactor.getItems().add(s);
			}
			shrinkFactor.getSelectionModel().select(0);
			numNodes.setText("16");
			
			sender = new ClientSender<>(ActionSelectorReply::fromBytes, ActionSelectorReply.SIZE);
			sender.addUpdateListener(reply -> Platform.runLater(() -> {
				storedNames.getItems().clear();
				for (Duple<LocalDateTime, Integer> name: reply.getNames()) {
					storedNames.getItems().add(name);
				}
			}));
			
			for (Move move: Move.values()) {
				Button moveButton = new Button(move.name());
				moveButtons.getChildren().add(moveButton);
				moveButton.setOnAction(event -> send(new ActionSelectorCommand(Mode.LEARNING, move, LocalDateTime.now(), 0, tag++, 0, 0)));
			}
			
			create.setOnAction(event -> {
				try {
					int nodes = Integer.parseInt(numNodes.getText());
					int shrink = shrinkFactor.getValue();
					send(new ActionSelectorCommand(Mode.STARTING, Move.STOP, LocalDateTime.now(), 0, tag++, nodes, shrink));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
			});
			
			retrieveStored.setOnAction(event -> send(new ActionSelectorCommand(Mode.RETRIEVING, Move.STOP, LocalDateTime.now(), 0, tag++, 0, 0)));

			applyStored.setOnAction(event -> {
				Duple<LocalDateTime,Integer> appliee = storedNames.getSelectionModel().getSelectedItem();
				send(new ActionSelectorCommand(Mode.APPLYING, Move.STOP, appliee.getFirst(), appliee.getSecond(), tag++, 0, 0));			
			});
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	void send(ActionSelectorCommand cmd) {
		try {
			sender.send(cmd);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void quit() {
		send(new ActionSelectorCommand(Mode.QUIT, Move.STOP, LocalDateTime.now(), 0, tag++, 0, 0));
	}
}
