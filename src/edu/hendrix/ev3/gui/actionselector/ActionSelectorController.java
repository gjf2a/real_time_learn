package edu.hendrix.ev3.gui.actionselector;

import edu.hendrix.ev3.ai.cluster.yuv.AdaptedYUYVImage;
import edu.hendrix.ev3.gui.Quittable;
import edu.hendrix.ev3.remote.Move;
import edu.hendrix.ev3.remote.net.actionselector.BSOCController;
import edu.hendrix.ev3.util.Duple;
import edu.hendrix.ev3.util.StampedStorage;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import java.util.ArrayList;

import org.joda.time.LocalDateTime;

public class ActionSelectorController implements Quittable {
	@FXML
	ChoiceBox<Duple<LocalDateTime,Integer>> networkChoices;
	@FXML
	Button open;
	@FXML
	Button update;
	@FXML
	TextField numNodes;
	
	@FXML
	ChoiceBox<Move> nodeMove;
	@FXML
	Button prevNode;
	@FXML
	Button nextNode;
	@FXML
	TextField current;
	@FXML
	TextField numSources;
	@FXML
	TextField totalInputs;
	
	@FXML
	Canvas nodeImage;
	
	BSOCController bsoc;
	ArrayList<Integer> ids;
	int currentNode;
	
	@FXML
	void initialize() {
		for (Move move: Move.values()) {
			nodeMove.getItems().add(move);
		}
		nodeMove.getSelectionModel().select(0);
		
		nodeMove.getSelectionModel().selectedItemProperty().addListener((o, vOld, vNew) -> {
			bsoc.assignMoveFor(currentNode(), vNew);
		});
		
		numNodes.setEditable(false);
		current.setEditable(false);
		numSources.setEditable(false);
		totalInputs.setEditable(false);
		
		for (Duple<LocalDateTime, Integer> option: StampedStorage.getAvailableFor(BSOCController.class)) {
			networkChoices.getItems().add(option);
			networkChoices.getSelectionModel().select(networkChoices.getItems().size() - 1);
		}
		
		open.setOnAction(event -> {
			try {
				Duple<LocalDateTime, Integer> option = networkChoices.getSelectionModel().getSelectedItem();
				bsoc = StampedStorage.open(BSOCController.class, option.getFirst(), option.getSecond(), BSOCController::fromString);
				currentNode = 0;
				ids = bsoc.getClusterIds();
				numNodes.setText(Integer.toString(bsoc.size()));
				totalInputs.setText(Integer.toString(bsoc.getTotalSourceInputs()));
				showCurrent();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		
		update.setOnAction(event -> {
			try {
				Duple<LocalDateTime, Integer> option = networkChoices.getSelectionModel().getSelectedItem();
				StampedStorage.revise(bsoc, option.getFirst(), maxSuffixFor(option.getFirst()) + 1);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		
		prevNode.setOnAction(event -> {currentNode = (currentNode - 1 + ids.size()) % ids.size(); showCurrent();});
		nextNode.setOnAction(event -> {currentNode = (currentNode + 1) % ids.size(); showCurrent();});
	}
	
	int maxSuffixFor(LocalDateTime key) {
		int max = 0;
		for (Duple<LocalDateTime,Integer> option: networkChoices.getItems()) {
			if (option.getFirst().equals(key) && max < option.getSecond()) {
				max = option.getSecond();
			}
		}
		return max;
	}
	
	int currentNode() {
		return ids.get(currentNode);
	}
	
	void showCurrent() {
		int node = currentNode();
		current.setText(Integer.toString(node));
		AdaptedYUYVImage currentImg = bsoc.getIdealInputFor(node);
		placeOnCanvas(currentImg, nodeImage);
		nodeMove.getSelectionModel().select(bsoc.getMoveFor(node));
		numSources.setText(Integer.toString(bsoc.getNumMergesFor(node)));
	}
	
	public static void placeOnCanvas(AdaptedYUYVImage img, Canvas canv) {
		double cellWidth = canv.getWidth() / img.getWidth();
		double cellHeight = canv.getHeight() / img.getHeight();
		GraphicsContext g = canv.getGraphicsContext2D();
		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				g.setFill(img.getRGBColor(x, y));
				g.fillRect(x * cellWidth, y * cellHeight, cellWidth, cellHeight);
			}
		}
	}

	@Override
	public void quit() {
		// TODO Auto-generated method stub
		
	}
}
