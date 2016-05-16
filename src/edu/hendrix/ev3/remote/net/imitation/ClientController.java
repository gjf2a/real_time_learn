package edu.hendrix.ev3.remote.net.imitation;

import java.io.IOException;
import java.net.SocketException;
import java.util.HashMap;
import java.util.function.Supplier;

import edu.hendrix.ev3.gui.Quittable;
import edu.hendrix.ev3.remote.Move;
import edu.hendrix.ev3.remote.net.ClientSender;
import edu.hendrix.ev3.remote.net.RobotConstants;
import edu.hendrix.ev3.util.Logger;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;

public class ClientController implements Quittable {
	@FXML
	Label messages;
	
	@FXML
	Button startRobot;
	
	@FXML
	ChoiceBox<Integer> shrinkFactor;
	
	@FXML
	ChoiceBox<Integer> maxNodes;
	
	@FXML
	Polygon up;
	
	@FXML
	Polygon left;
	
	@FXML
	Polygon right;
	
	@FXML
	Circle stop;
	
	@FXML
	Circle auto;
	
	@FXML
	Pane pane;
	
	@FXML
	TableView<MoveStats> statTable;
	
	@FXML
	TableColumn<MoveStats,String> moveColumn;
	
	@FXML
	TableColumn<MoveStats,Integer> successColumn;
	
	@FXML
	TableColumn<MoveStats,Integer> attemptColumn;
	
	@FXML
	TableColumn<MoveStats,Double> percentColumn;
	
	HashMap<KeyCode,Supplier<CommandMessage>> key2msg = new HashMap<>();
	HashMap<KeyCode,Shape> key2shape = new HashMap<>();
	HashMap<KeyCode,Paint> key2color = new HashMap<>();
	ClientSender<UserMessage,RobotMessage> sender;
	ObservableLearningPerformanceStats stats;
	
	@FXML
	void initialize() {
		setupKeys();
		setupPane();
		setupChoices();
		setupSender();
		setupTable();
	}
	
	void setupSender() {
		try {
			sender = new ClientSender<>(RobotMessage::new, RobotMessage.NUM_BYTES);
			sender.addUpdateListener(tag -> Platform.runLater(() -> messages.setText("Acknowledged")));
		} catch (SocketException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	void setupKeys() {
		setupKey(KeyCode.I, up, Mode.TRAIN, Move.FORWARD);
		setupKey(KeyCode.J, left, Mode.TRAIN, Move.LEFT);
		setupKey(KeyCode.L, right, Mode.TRAIN, Move.RIGHT);
		setupKey(KeyCode.K, stop, Mode.SHOW, Move.STOP);
		setupKey(KeyCode.N, auto, Mode.AUTO, Move.NONE);
	}
	
	void setupKey(KeyCode key, Shape target, Mode mode, Move move) {
		key2shape.put(key, target);
		key2color.put(key, target.getFill());
		key2msg.put(key, () -> new CommandMessage(mode, move));
		target.setOnMouseClicked(event -> send(new CommandMessage(mode, move)));
		plotLetter(key, target);
	}
	
	void plotLetter(KeyCode key, Shape target) {
		Label keyLabel = new Label(key.getName());
		pane.getChildren().add(keyLabel);
		keyLabel.setLayoutX(target.getLayoutX() - 5);
		keyLabel.setLayoutY(target.getLayoutY() - 15);
	}
	
	void setupPane() {
		pane.setOnKeyPressed(event -> {
			KeyCode key = event.getCode();
			if (key2msg.containsKey(key)) {
				Logger.ClientLog.log(key + " pressed");
				key2shape.get(key).setFill(Color.YELLOW);
				send(key2msg.get(key).get());
			}
		});
		
		pane.setOnKeyReleased(event -> {
			KeyCode key = event.getCode();
			if (key2shape.containsKey(key)) {
				key2shape.get(key).setFill(key2color.get(key));
			}
		});
		
		pane.setOnMouseEntered(m -> pane.requestFocus());		
	}
	
	void setupTable() {
		stats = new ObservableLearningPerformanceStats();
		sender.addUpdateListener(stats);
		statTable.setItems(stats.getObservableList());
		moveColumn.setCellValueFactory(new PropertyValueFactory<>("moveName"));
		successColumn.setCellValueFactory(new PropertyValueFactory<>("correct"));
		attemptColumn.setCellValueFactory(new PropertyValueFactory<>("attempts"));
		percentColumn.setCellValueFactory(new PropertyValueFactory<>("percent"));
	}
	
	void send(UserMessage msg) {
		try {
			sender.send(msg);
			messages.setText("Waiting...");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void quit() {
		send(new CommandMessage(Mode.QUIT, Move.STOP));
	}
	
	@FXML
	void start() {
		send(new StartupMessage(maxNodes.getSelectionModel().getSelectedItem(), 
				shrinkFactor.getSelectionModel().getSelectedItem()));
	}
	
	void setupChoices() {
		setupShrinks();
		setupNodes();
	}
	
	void setupShrinks() {
		for (int i = 1; i < Math.min(RobotConstants.HEIGHT, RobotConstants.WIDTH); i++) {
			if (RobotConstants.HEIGHT % i == 0 && RobotConstants.WIDTH % i == 0) {
				shrinkFactor.getItems().add(i);
			}
		}
		shrinkFactor.getSelectionModel().select(0);
	}
	
	void setupNodes() {
		for (int n: new int[]{10, 25, 50, 100, 250, 500, 1000, 1500, 2000}) {
			maxNodes.getItems().add(n);
		}
		maxNodes.getSelectionModel().select(maxNodes.getItems().size() - 1);
	}
}
