package edu.hendrix.ev3.gui.videoview;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.concurrent.BlockingQueue;
import java.util.function.Function;
import java.util.function.BiFunction;
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
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import edu.hendrix.ev3.ai.cluster.EdgeImage;
import edu.hendrix.ev3.ai.cluster.yuv.AdaptedYUYVImage;
import edu.hendrix.ev3.imgproc.BitImage;
import edu.hendrix.ev3.imgproc.BitImageClusters;
import edu.hendrix.ev3.imgproc.FAST;
import edu.hendrix.ev3.imgproc.FeatureFlow;
import edu.hendrix.ev3.imgproc.Normal_BRIEF_256_31_a;
import edu.hendrix.ev3.imgproc.PointPairList;
import edu.hendrix.ev3.imgproc.RANSAC;
import edu.hendrix.ev3.imgproc.Vector2D;
import edu.hendrix.ev3.util.Duple;
import edu.hendrix.ev3.util.Stdev;
import edu.hendrix.ev3.util.StdevType;
import edu.hendrix.ev3.util.Util;

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
	TextField differences;
	
	@FXML
	Button animate;
	
	@FXML
	Button stopAnimation;
	
	@FXML
	Slider frameRate;
	
	@FXML
	ChoiceBox<Integer> shrinkAmount;
	
	@FXML
	CheckBox viewFAST;
	
	@FXML
	CheckBox filterFAST;
	
	@FXML
	CheckBox viewBriefFlow;
	
	@FXML
	CheckBox viewSmoothing;
	
	@FXML
	CheckBox viewRANSACFlow;
	
	@FXML
	CheckBox viewBriefWindow;
	
	@FXML
	CheckBox filterMagnitudes;
	
	@FXML
	CheckBox filterDistances;
	
	@FXML
	CheckBox filterVectors;
	
	@FXML
	CheckBox filterNeighbors;
	
	@FXML
	RadioButton fastFilter;
	
	@FXML
	RadioButton fastCluster1;
	
	@FXML
	CheckBox distanceDiscount;
	
	@FXML
	Button findAverageDifferences;
	
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
		
		findAverageDifferences.setOnAction(event -> {
			System.out.println("starting...");
			ArrayList<Double> magDiffs = new ArrayList<>();
			ArrayList<Double> thetaDiffs = new ArrayList<>();
			for (int frame = 2; frame < examples.size(); frame++) {
				if (frame % 10 == 0) System.out.println("Frame: " + frame);
				FeatureFlow ff = flowProc(frame, (prev, img1) -> briefFlow(prev, img1));
				FeatureFlow old = flowProc(frame - 1, (prev, img1) -> briefFlow(prev, img1));
				if (ff.vectors().size() > 0 && old.vectors().size() > 0) {
					Vector2D mean = Vector2D.mean(ff.vectors());
					Vector2D oldMean = Vector2D.mean(old.vectors());
					magDiffs.add(Math.abs(mean.R() - oldMean.R()));
					thetaDiffs.add(Math.abs(Util.angleDiff(mean.theta(), oldMean.theta())));
				}
			}
			Stdev magStats = new Stdev(magDiffs, StdevType.POPULATION);
			Stdev thetaStats = new Stdev(thetaDiffs, StdevType.POPULATION);
			differences.setText(String.format("Mag: %s Theta: %s", magStats.toString(), thetaStats.toString()));
		});
		
		ToggleGroup fastGroup = new ToggleGroup();
		fastGroup.getToggles().add(fastFilter);
		fastGroup.getToggles().add(fastCluster1);
		fastFilter.setSelected(true);
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
		
		result.add(new AdaptedYUYVRenderer(img));
		
		if (current > 0 && (viewBriefWindow.isSelected() || viewBriefFlow.isSelected() || viewRANSACFlow.isSelected())) {
			result = flowViewer(current, (prev, img1) -> briefFlow(prev, img1));
		} 
		else if (viewFAST.isSelected()) {
			FAST features = new FAST(img);
			if (filterFAST.isSelected()) {
				features.retainBestFeatures(img, NUM_FEATURES);
			}
			result.add(new HighlightRenderer(features));
		}
		return result;
	}

	Function<AdaptedYUYVImage,BitImage> getFeatureFinder() {
		if (filterFAST.isSelected()) {
			if (fastFilter.isSelected()) {
				return img -> FAST.nFeatures(img, 500);
			} else if (fastCluster1.isSelected()) {
				return img -> {
					FAST features = new FAST(img);
					BitImageClusters bic = new BitImageClusters(features);
					bic.combineAllZones(4);
					return bic.getBitImage();
				};
			} else {
				throw new IllegalStateException("No feature finder!");
			}
		} else {
			return img -> new FAST(img);
		}
	}

	FeatureFlow briefFlow(AdaptedYUYVImage prev, AdaptedYUYVImage img1) {
		if (viewBriefWindow.isSelected()) {
			FeatureFlow ff = FeatureFlow.makePatchMatches(getFeatureFinder(), patcher, prev, img1, 30);
			filterFlow(ff);
			return ff;
		} else {
			FeatureFlow ff = distanceDiscount.isSelected() 
					? FeatureFlow.makePatchMatches2(getFeatureFinder(), patcher, prev, img1)
					: FeatureFlow.makePatchMatches(getFeatureFinder(), patcher, prev, img1);
			if (viewBriefFlow.isSelected()) {
				filterFlow(ff);
			} else if (viewRANSACFlow.isSelected()) {
				ff.filterWith(map -> RANSAC.filter(map, 100, 7, 5.0, 10));
			}
			return ff;
		}
	}
	
	public static final double FILTER_STDEVS = 1.0;
	
	void filterFlow(FeatureFlow ff) {
		if (filterMagnitudes.isSelected()) ff.keepOnlyMagnitude(FILTER_STDEVS);
		if (filterDistances.isSelected()) ff.keepOnlyDistance(FILTER_STDEVS);
		if (filterVectors.isSelected()) ff.keepOnlyVectors(FILTER_STDEVS);		
		if (filterNeighbors.isSelected()) {
			ff.neighborFilterWith(40, (value, rMean, thetaMean) -> {
				double v2mRatio = value.R() / rMean;
				double angleDiff = Util.angleDiff(value.theta(), thetaMean);
				return v2mRatio < 2.0 && angleDiff < Math.PI/12;
			});
		}
	}
	
	FeatureFlow flowProc(int frame, BiFunction<AdaptedYUYVImage,AdaptedYUYVImage,FeatureFlow> flower) {
		Util.assertArgument(frame > 0 && frame < examples.size(), "Bad frame index " + frame);
		AdaptedYUYVImage prev = basicProc(frame - 1);
		AdaptedYUYVImage img = basicProc(frame);
		return flower.apply(prev, img);
	}
	
	RenderList flowViewer(int frame, BiFunction<AdaptedYUYVImage,AdaptedYUYVImage,FeatureFlow> flower) {
		FeatureFlow ff = flowProc(frame, flower);
		Vector2D mean = Vector2D.mean(ff.vectors());
		messages.setText(String.format("Mean:(%5.2f,%5.2f) Sep:(%5.2f,%5.2f) (%d features)", mean.R(), mean.theta(), Vector2D.rMean(ff.vectors()), Vector2D.thetaMean(ff.vectors()), ff.asMap().size()));
		
		if (frame > 1) {
			FeatureFlow oldFF = flowProc(frame - 1, flower);
			Vector2D oldMean = Vector2D.mean(oldFF.vectors());
			differences.setText(String.format("MeanDiff:(%5.2f,%5.2f)", Math.abs(oldMean.R() - mean.R()), Math.abs(Util.angleDiff(oldMean.theta(), mean.theta()))));
		} else {
			differences.setText("");
		}
		
		RenderList result = new RenderList();
		result.add(new AdaptedYUYVRenderer(basicProc(frame)));
		result.add(new FlowRenderer(basicProc(frame - 1), ff));
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
