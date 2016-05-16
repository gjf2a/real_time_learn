package edu.hendrix.ev3.ai.topological.sonar.visualize;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import edu.hendrix.ev3.ai.topological.HierarchicalStateMap;
import edu.hendrix.ev3.ai.topological.sonar.ClusterableSonarState;
import edu.hendrix.ev3.ai.topological.sonar.SonarConstraints;
import edu.hendrix.ev3.remote.sonar.SonarPosition;
import edu.hendrix.ev3.util.Util;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

public class VisualizerController {
	@FXML
	TextField leftMin;
	@FXML
	TextField leftMax;
	@FXML
	TextField leftCur;
	@FXML
	TextField rightMin;
	@FXML
	TextField rightMax;
	@FXML
	TextField rightCur;
	@FXML
	TextField centerMin;
	@FXML
	TextField centerMax;
	@FXML
	TextField centerCur;
	
	@FXML
	TextField state;
	@FXML
	TextField level;
	
	@FXML
	Button prevState;
	@FXML
	Button nextState;
	@FXML
	Button prevLevel;
	@FXML
	Button nextLevel;
	
	@FXML
	Button bestLevel;
	
	@FXML
	MenuItem open;
	
	@FXML
	TextField maxNodes;
	
	@FXML
	TextField numDescendants;
	
	@FXML
	TextField numOutgoing;
	
	FileChooser chooser;
	HierarchicalStateMap<ClusterableSonarState> map;
	SonarPositionGrid sonarGrid;
	
	@FXML
	void initialize() {
		for (TextField no: new TextField[]{leftCur, rightCur, centerCur, state, level}) {
			no.setEditable(false);
		}
		
		chooser = new FileChooser();
		maxNodes.setText("256");
		
		sonarGrid = new SonarPositionGrid();
		sonarGrid.addMatchingFields(SonarPosition.LEFT, leftMin, leftMax);
		sonarGrid.addMatchingFields(SonarPosition.CENTER, centerMin, centerMax);
		sonarGrid.addMatchingFields(SonarPosition.RIGHT, rightMin, rightMax);
		Util.assertState(sonarGrid.allTextFieldsPresent(), "Missing fields");
	}
	
	@FXML
	void open() {
		File target = chooser.showOpenDialog(null);
		if (target != null) {
			try {
				map = CmdLineVisualizer.makeHierarchyFrom(target, Integer.parseInt(maxNodes.getText()));
				setLevel(map.getNumLevels() - 1);
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public int getCurrentLevel() {
		return Integer.parseInt(level.getText());
	}
	
	void setLevel(int lv) {
		level.setText(Integer.toString(lv));
		ArrayList<Integer> nodes = map.allNodesFor(getCurrentLevel());
		if (!nodes.isEmpty())
			setState(nodes.get(0));
	}
	
	public int getCurrentState() {
		return Integer.parseInt(state.getText());
	}
	
	void setState(int s)  {
		state.setText(Integer.toString(s));
		
		ClusterableSonarState values = map.getIdealInputFor(getCurrentLevel(), getCurrentState());
		leftCur.setText(Float.toString(values.getReading(SonarPosition.LEFT)));
		centerCur.setText(Float.toString(values.getReading(SonarPosition.CENTER)));
		rightCur.setText(Float.toString(values.getReading(SonarPosition.RIGHT)));
		
		numDescendants.setText(Integer.toString(map.getDescendantsAt(getCurrentLevel(), getCurrentState()).size()));
		
		numOutgoing.setText(Integer.toString(map.totalOutgoingFor(getCurrentLevel(), getCurrentState())));
	}
	
	@FXML
	void nextState() {
		int next = getCurrentState() + 1;
		if (next == map.allNodesFor(getCurrentLevel()).size()) {
			next = 0;
		}
		setState(next);
	}
	
	@FXML
	void prevState() {
		int prev = getCurrentState() - 1;
		if (prev < 0) {
			prev = map.allNodesFor(getCurrentLevel()).size() - 1;
		}
		setState(prev);
	}
	
	@FXML
	void nextLevel() {
		int next = getCurrentLevel() + 1;
		if (next == map.getNumLevels()) {next = 0;}
		setLevel(next);
	}
	
	@FXML
	void prevLevel() {
		int prev = getCurrentLevel() - 1;
		if (prev < 0) {prev = map.getNumLevels() - 1;}
		setLevel(prev);
	}
	
	@FXML
	void bestLevel() {
		setLevel(map.bestLevelFor(sonarGrid.getAllConstraints()));
	}
	
	@FXML
	void constraintsLevelBelow() {
		SonarConstraints below = new SonarConstraints(map.getIdealInputFor(getCurrentLevel(), getCurrentState()));
		for (int bottomNode: map.getDescendantsAt(getCurrentLevel(), getCurrentState())) {
			below.loosen(new SonarConstraints(map.getIdealInputFor(0, bottomNode)));
		}
		sonarGrid.setAll(below);
	}
	
	@FXML
	void clearConstraints() {
		sonarGrid.clearAll();
	}
}
