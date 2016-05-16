package edu.hendrix.ev3.gui.topoview;

import java.io.File;
import java.io.FileNotFoundException;

import edu.hendrix.ev3.ai.topological.image.ImageStateMap;
import edu.hendrix.ev3.gui.videoview.AdaptedYUYVRenderer;
import edu.hendrix.ev3.remote.Move;
import edu.hendrix.ev3.util.Util;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

public class TopoViewController {
	@FXML
	MenuItem open;
	@FXML
	MenuItem close;
	
	@FXML
	ChoiceBox<Integer> nodeNum;
	@FXML
	ChoiceBox<Move> moves;
	@FXML
	ChoiceBox<Integer> successors;
	@FXML
	TextField transitions;
	@FXML
	Button jump;
	
	@FXML
	Canvas image;
	
	FileChooser chooser = new FileChooser();
	ImageStateMap map;
	
	@FXML
	void initialize() {
		nodeNum.selectionModelProperty().addListener((obs, oldVal, newVal) -> switchNode(newVal.getSelectedItem()));
		moves.selectionModelProperty().addListener((obs, oldVal, newVal) -> switchMove(newVal.getSelectedItem()));
		successors.selectionModelProperty().addListener((obs, oldVal, newVal) -> switchSuccessor(newVal.getSelectedItem()));
		transitions.setEditable(false);
	}
	
	@FXML
	void open() {
		File file = chooser.showOpenDialog(null);
		if (file != null) {
			try {
				map = ImageStateMap.fromString(Util.fileToString(file));
				nodeNum.getItems().clear();
				for (int node: map.allNodes()) {
					nodeNum.getItems().add(node);
				}
				nodeNum.getSelectionModel().select(0);
				switchNode(nodeNum.getValue());
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	void switchNode(int updated) {
		AdaptedYUYVRenderer.placeOnCanvas(map.getIdealInputFor(updated), image);
		moves.getItems().clear();
		for (Move move: map.movesFrom(updated)) {
			moves.getItems().add(move);
		}
		moves.getSelectionModel().select(0);
		switchMove(moves.getValue());
	}
	
	void switchMove(Move updated) {
		successors.getItems().clear();
		for (int successor: map.successorsTo(nodeNum.getValue(), updated)) {
			successors.getItems().add(successor);
		}
		successors.getSelectionModel().select(0);
		switchSuccessor(successors.getValue());
	}
	
	void switchSuccessor(int updated) {
		int total = map.numCountsFor(nodeNum.getValue(), moves.getValue());
		int toThisOne = map.numCountsTo(nodeNum.getValue(), moves.getValue(), updated);
		double fraction = (double)toThisOne / total;
		transitions.setText(String.format("%d/%d (%4.1f%%)", total, toThisOne, fraction));
	}
	
	@FXML
	void jump() {
		nodeNum.getSelectionModel().select(successors.getValue());
		switchNode(successors.getValue());
	}
}
