package edu.hendrix.ev3.gui.videoview;

import java.util.EnumSet;
import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.concurrent.ArrayBlockingQueue;

import edu.hendrix.ev3.ai.TrainingList;
import edu.hendrix.ev3.remote.Move;
import edu.hendrix.ev3.storage.VideoStorage;
import javafx.util.Duration;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import edu.hendrix.ev3.ai.cluster.EdgeImage;
import edu.hendrix.ev3.ai.cluster.yuv.AdaptedYUYVImage;
import edu.hendrix.ev3.imgproc.FAST;
import edu.hendrix.ev3.imgproc.FeatureFlow;
import edu.hendrix.ev3.imgproc.Normal_BRIEF_256_31_a;
import edu.hendrix.ev3.imgproc.PointPairList;
import edu.hendrix.ev3.imgproc.RANSAC;
import edu.hendrix.ev3.imgproc.Vector2D;
import edu.hendrix.ev3.util.Duple;

public class VideoViewController {
	public static final int NUM_FEATURES = 500;
	
	@FXML
	ChoiceBox<Move> moveChoice;
	
	@FXML
	ChoiceBox<String> sessionChoice;
	
	@FXML
	CheckBox useAllMoves;
	
	@FXML
	Label positionInList;
	
	@FXML
	Label currentMove;
	
	@FXML
	Button goLeft;
	
	@FXML
	Button goRight;
	
	@FXML
	Slider howFar;
	
	@FXML
	Button reset;
	
	@FXML
	Canvas canv;
	
	@FXML
	TextField messages;
	
	@FXML
	Button animate;
	
	@FXML
	Button stopAnimation;
	
	@FXML
	Slider frameRate;
	
	@FXML
	ChoiceBox<Integer> shrinkAmount;
	
	@FXML
	CheckBox viewEdges;
	
	@FXML
	CheckBox viewFAST;
	
	@FXML
	CheckBox filterFAST;
	
	@FXML
	CheckBox viewStableFlow;
	
	@FXML
	CheckBox viewGreedyFlow;
	
	@FXML
	CheckBox viewBriefFlow;
	
	@FXML
	CheckBox viewSmoothing;
	
	@FXML
	CheckBox viewRANSACFlow;
	
	@FXML
	Label thresholdValue;
	
	PointPairList patcher = new Normal_BRIEF_256_31_a();

	VideoStorage videos;
	private TrainingList examples;
	private int current;
	private Timeline animator;

	@FXML
	void initialize() {
		useAllMoves.setSelected(true);
		
		videos = VideoStorage.getPCStorage();
		setUpChoice(sessionChoice, videos.getVideoChoices());

		for (Move m: Move.values()) {
			moveChoice.getItems().add(m);
		}

		sessionChoice.getSelectionModel().selectLast();

		goLeft.setOnAction(event -> goLeft());
		goRight.setOnAction(event -> goRight());

		reset.setOnAction(event -> reset());
		
		animate.setOnAction(event -> animate());
		frameRate.valueProperty().addListener((obs,oldV,newV) -> animate());
		stopAnimation.setOnAction(event -> pause());
		
		for (int i = 1; i <= 8; i *= 2) {
			shrinkAmount.getItems().add(i);
		}
		for (int i = 10; i <= 40; i *= 2) {
			shrinkAmount.getItems().add(i);
		}
		shrinkAmount.getSelectionModel().select(0);
	}
	
	void setUpChoice(ChoiceBox<String> choices, String[] src) {
		for (String t: src) {
			choices.getItems().add(t);
		}
		choices.getSelectionModel().selectLast();
	}
	
	void reset() {
		current = 0;
		examples = new TrainingList();
		BlockingQueue<Duple<Move,AdaptedYUYVImage>> imgs = new ArrayBlockingQueue<>(1);
		startProducerThread(sessionChoice.getSelectionModel().getSelectedItem(), imgs);
		startConsumerThread(imgs, mImg -> Platform.runLater(() -> loadImage(mImg)));
	}
	
	private void loadImage(Duple<Move,AdaptedYUYVImage> mImg) {
		examples.add(mImg);
		if (examples.size() == 1) {
			renderCurrent();
		} else {
			updateCurrentMsg();
		}
	}

	void animate() {
		pause();
		animator = new Timeline(new KeyFrame(Duration.ZERO, (e -> goRight())), 
								new KeyFrame(Duration.millis(1000.0 / frameRate.getValue())));		
		animator.setCycleCount(Timeline.INDEFINITE);
		animator.play();		
	}
	
	void pause() {
		if (animator != null) {
			animator.stop();
		}
	}
	
	void go(IntSupplier currentUpdater) {
		current = currentUpdater.getAsInt();
		renderCurrent();
	}
	
	void goLeft() {
		go(() -> (examples.size() + current - howFar()) % examples.size());
	}
	
	void goRight() {
		go(() -> (current + howFar()) % examples.size());
	}
	
	int howFar() {
		return (int)howFar.getValue();
	}
	
	void startProducerThread(String session, BlockingQueue<Duple<Move,AdaptedYUYVImage>> imgs) {
		new Thread(() -> {
			try {
				videos.threadedOpen(session, getPermittedSet(), imgs);
			} catch (Exception e) {
				reportProblem(e);
			} 
		}).start();
	}
	
	void startConsumerThread(BlockingQueue<Duple<Move,AdaptedYUYVImage>> imgs, Consumer<Duple<Move,AdaptedYUYVImage>> consumer) {
		new Thread(() -> {
			try {
				for (;;) {
					Duple<Move,AdaptedYUYVImage> mImg = imgs.take();
					if (mImg.getFirst() == Move.NONE) {
						break;
					} else {
						consumer.accept(mImg);
					}
				}
			} catch (Exception e) {
				reportProblem(e);
			}
		}).start();
	}
	
	void reportProblem(Exception e) {
		Platform.runLater(() -> messages.setText(e.getMessage()));
	}
	
	EnumSet<Move> getPermittedSet() {
		return useAllMoves.isSelected() 
				? EnumSet.allOf(Move.class) 
				: EnumSet.of(moveChoice.getSelectionModel().getSelectedItem());
	}
	
	Renderer currentFrame() {
		RenderList result = new RenderList();
		AdaptedYUYVImage img = basicProc(current);
		
		if (viewEdges != null && viewEdges.isSelected()) {
			img = applyEdgeProcessing(img);
		}
		
		result.add(new AdaptedYUYVRenderer(img));
		
		if (current > 0 && viewBriefFlow.isSelected()) {
			AdaptedYUYVImage prev = basicProc(current - 1);
			FeatureFlow ff = FeatureFlow.makePatchMatches(patcher, 500, prev, img);
			ff.keepOnly(1);
			result.add(new FlowRenderer(prev, ff));
			messages.setText(Vector2D.mean(ff.vectors()).toString());
		} else if (current > 0 && viewRANSACFlow.isSelected()) {
			AdaptedYUYVImage prev = basicProc(current - 1);
			FeatureFlow ff = FeatureFlow.makePatchMatches(patcher, 500, prev, img);
			ff.filterWith(map -> RANSAC.filter(map, 100, 7, 5.0, 10));
			result.add(new FlowRenderer(prev, ff));
			Vector2D mean = Vector2D.mean(ff.vectors());
			messages.setText(String.format("Mean:(%5.2f,%5.2f) (%d features)", mean.R(), mean.theta(), ff.asMap().size()));
		}
		else if (current > 0 && (viewStableFlow.isSelected() || viewGreedyFlow.isSelected())) {
			AdaptedYUYVImage prev = basicProc(current - 1);
			FAST cf = FAST.nFeatures(img, NUM_FEATURES);
			FAST pf = FAST.nFeatures(prev, NUM_FEATURES);
			FeatureFlow ff = viewStableFlow.isSelected() ? FeatureFlow.makeStableFAST(pf, cf) : FeatureFlow.makeGreedyFAST(pf, cf);
			ff.keepOnly(1);
			result.add(new FlowRenderer(prev, ff));
			messages.setText(Vector2D.mean(ff.vectors()).toString());
		}
		else if (viewFAST.isSelected()) {
			FAST features = new FAST(img.getWidth(), img.getHeight());
			features.scalePyramid(img);
			if (filterFAST.isSelected()) {
				features.retainBestFeatures(img, NUM_FEATURES);
			}
			result.add(new HighlightRenderer(features));
		}
		return result;
	}
	
	AdaptedYUYVImage basicProc(int imgIndex) {
		AdaptedYUYVImage img = examples.get(imgIndex).getSecond();
		img = img.shrunken(shrinkAmount.getValue());
		if (viewSmoothing.isSelected()) {
			img = img.gaussianSmoothed();
		}
		return img;
	}
	
	AdaptedYUYVImage applyEdgeProcessing(AdaptedYUYVImage img) {
		EdgeImage edges = new EdgeImage(img);
		return edges.toYUYV();
	}
	
	Move currentMove() {
		return examples.get(current).getFirst();
	}

	@FXML
	void renderCurrent() {
		if (current < examples.size()) {
			currentFrame().draw(canv);
			updateCurrentMsg();
			currentMove.setText(currentMove().toString());
		}
	}
	
	void updateCurrentMsg() {
		positionInList.setText(String.format("%d / %d   ", current + 1, examples.size()));		
	}
}
