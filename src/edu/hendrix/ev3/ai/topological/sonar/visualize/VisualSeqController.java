package edu.hendrix.ev3.ai.topological.sonar.visualize;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.function.IntConsumer;

import edu.hendrix.ev3.ai.topological.HierarchicalStateMap;
import edu.hendrix.ev3.ai.topological.sonar.ClusterableSonarState;
import edu.hendrix.ev3.remote.sonar.SonarPosition;
import edu.hendrix.ev3.util.Duple;
import edu.hendrix.ev3.util.Util;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

public class VisualSeqController {
	@FXML
	TextField leftMin;
	@FXML
	TextField leftMax;
	@FXML
	TextField leftCur;
	@FXML
	TextField leftInput;
	@FXML
	TextField rightMin;
	@FXML
	TextField rightMax;
	@FXML
	TextField rightCur;
	@FXML
	TextField rightInput;
	@FXML
	TextField centerMin;
	@FXML
	TextField centerMax;
	@FXML
	TextField centerCur;
	@FXML
	TextField centerInput;
	
	@FXML
	CheckBox inputMeetsConstraint;
	
	@FXML
	TextField state;
	@FXML
	TextField level;
	@FXML
	TextField input;
	
	@FXML
	Button prevState;
	@FXML
	Button nextState;
	@FXML
	Button prevLevel;
	@FXML
	Button nextLevel;
	@FXML
	Button prevInput;
	@FXML
	Button nextInput;
	
	@FXML
	Button bestLevel;
	
	@FXML
	MenuItem open;
	
	@FXML
	TextField maxNodes;
	
	@FXML
	TextField numSourceInputs;
	
	FileChooser chooser;
	ArrayList<HierarchicalStateMap<ClusterableSonarState>> maps;
	ArrayList<ClusterableSonarState> inputs;
	SonarPositionGrid sonarGrid;
	
	@FXML
	void initialize() {
		for (TextField no: new TextField[]{numSourceInputs, leftCur, rightCur, centerCur, leftInput, rightInput, centerInput, state, level}) {
			no.setEditable(false);
		}
		
		chooser = new FileChooser();
		maxNodes.setText("16");
		
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
				Duple<ArrayList<ClusterableSonarState>,ArrayList<HierarchicalStateMap<ClusterableSonarState>>> data = CmdLineVisualizer.makeHierarchySeqFrom(target,  Integer.parseInt(maxNodes.getText()));
				inputs = data.getFirst();
				maps = data.getSecond();
				setInput(0);
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
	
	public int getCurrentState() {
		return Integer.parseInt(state.getText());
	}
	
	public int getCurrentInput() {
		return Integer.parseInt(input.getText());
	}
	
	public ClusterableSonarState getCurrentInputValue() {
		return inputs.get(getCurrentInput());
	}
	
	public HierarchicalStateMap<ClusterableSonarState> getCurrentMap() {
		return maps.get(getCurrentInput());
	}
	
	void setLevel(int lv) {
		level.setText(Integer.toString(lv));
		ArrayList<Integer> nodes = getCurrentMap().allNodesFor(getCurrentLevel());
		if (!nodes.isEmpty())
			setState(nodes.get(0));
	}
	
	void setState(int s)  {
		state.setText(Integer.toString(s));
		numSourceInputs.setText(Integer.toString(getCurrentMap().totalOutgoingFor(getCurrentLevel(), getCurrentState())));
		
		ClusterableSonarState values = getCurrentMap().getIdealInputFor(getCurrentLevel(), getCurrentState());
		leftCur.setText(Float.toString(values.getReading(SonarPosition.LEFT)));
		centerCur.setText(Float.toString(values.getReading(SonarPosition.CENTER)));
		rightCur.setText(Float.toString(values.getReading(SonarPosition.RIGHT)));
	}
	
	void setInput(int in) {
		input.setText(Integer.toString(in));
		ClusterableSonarState input = getCurrentInputValue();
		leftInput.setText(Float.toString(input.getReading(SonarPosition.LEFT)));
		centerInput.setText(Float.toString(input.getReading(SonarPosition.CENTER)));
		rightInput.setText(Float.toString(input.getReading(SonarPosition.RIGHT)));
		bestLevel();
	}
	
	@FXML
	void nextState() {
		next(getCurrentState(), getCurrentMap().allNodesFor(getCurrentLevel()).size(), x -> setState(x));
	}
	
	@FXML
	void prevState() {
		prev(getCurrentState(), getCurrentMap().allNodesFor(getCurrentLevel()).size(), x -> setState(x));
	}
	
	@FXML
	void nextLevel() {
		next(getCurrentLevel(), getCurrentMap().getNumLevels(), x -> setLevel(x));
	}
	
	@FXML
	void prevLevel() {
		prev(getCurrentLevel(), getCurrentMap().getNumLevels(), x -> setLevel(x));
	}
	
	@FXML
	void nextInput() {
		next(getCurrentInput(), inputs.size(), x -> setInput(x));
	}
	
	@FXML
	void prevInput() {
		prev(getCurrentInput(), inputs.size(), x -> setInput(x));
	}
	
	void next(int current, int topBound, IntConsumer setter) {
		int result = current + 1;
		if (result == topBound) {result = 0;}
		setter.accept(result);
	}
	
	void prev(int current, int topBound, IntConsumer setter) {
		int result = current - 1;
		if (result < 0) {result = topBound - 1;}
		setter.accept(result);
	}
	
	@FXML
	void bestLevel() {
		int[] states = getCurrentMap().getStateFor(getCurrentInputValue());
		int bestLevel = getCurrentMap().highestSeparableLevel(getCurrentInputValue(), sonarGrid.getAllConstraints());
		level.setText(Integer.toString(bestLevel));
		setState(states[bestLevel]);
	}
	
	@FXML
	void clearConstraints() {
		sonarGrid.clearAll();
	}
	
	Duple<Boolean,Float> decodeConstraintField(TextField field) {
		try {
			return new Duple<>(true, Float.parseFloat(field.getText()));
		} catch (NumberFormatException nfe) {
			return new Duple<>(false, 0.0f);
		}
	}
}
