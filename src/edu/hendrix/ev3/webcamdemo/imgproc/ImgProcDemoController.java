package edu.hendrix.ev3.webcamdemo.imgproc;

import edu.hendrix.ev3.gui.Quittable;
import edu.hendrix.ev3.imgproc.FAST;
import edu.hendrix.ev3.imgproc.Feature;
import edu.hendrix.ev3.webcamdemo.ClusterableImage;
import edu.hendrix.ev3.webcamdemo.bsoc.BSOCDemoController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;

public class ImgProcDemoController implements Quittable {
	public static final int NUM_FAST = 1000;
	
	@FXML TextField height;
	@FXML TextField width;
	
	@FXML TextField frameRate;
	@FXML TextField procTime;
	
	@FXML Canvas display;
	
	@FXML CheckBox findFAST;
	@FXML CheckBox clusterFAST;
	@FXML CheckBox justFAST;
	
	ImageThread renderer;
	
	@FXML
	void initialize() {
		renderer = new ImageThread((img, rate) -> {
			long start = System.currentTimeMillis();
			ClusterableImage procImg = new ClusterableImage(img);
			if (justFAST.isSelected() || findFAST.isSelected() || clusterFAST.isSelected()) {
				FAST fast = findFAST.isSelected() 
						? FAST.nFeatures(procImg, NUM_FAST) 
								: clusterFAST.isSelected() 
								? FAST.nClusters(procImg, NUM_FAST)
										: new FAST(procImg);
				for (Feature f: fast.allSet()) {
					procImg.addHighlight(f.X(), f.Y());
				}
			} 
			long duration = System.currentTimeMillis() - start;

			Platform.runLater(() -> {
				BSOCDemoController.render(procImg, display);
				frameRate.setText(String.format("%4.2f", rate));
				height.setText(Integer.toString(img.getHeight()));
				width.setText(Integer.toString(img.getWidth()));
				procTime.setText(Long.toString(duration));
			});});

		renderer.start();
		frameRate.setEditable(false);
		height.setEditable(false);
		width.setEditable(false);
		procTime.setEditable(false);
	}

	@Override
	public void quit() {
		renderer.quit();
	}
}
