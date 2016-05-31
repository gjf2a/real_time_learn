package edu.hendrix.ev3.ai.supervised.evaluable;
import libsvm.*;

import java.util.ArrayList;
import java.util.HashMap;

import edu.hendrix.ev3.ai.cluster.yuv.AdaptedYUYVImage;
import edu.hendrix.ev3.ai.supervised.RobotLearner;
import edu.hendrix.ev3.remote.Move;

public class Support_Vector_Machine implements RobotLearner {
	// We are using LIBSVM 3.21 from http://www.csie.ntu.edu.tw/~cjlin/libsvm/
	private ArrayList<AdaptedYUYVImage> images;
	private ArrayList<Move> moves;
	private HashMap<Integer,Move> intToMove;
	private HashMap<Move,Integer> moveToInt;
	private boolean trained;
	private svm_model model;
	private int shrinkValue = 4;
	// Y' values are conventionally shifted and scaled to the range [16, 235] 
	private int scaleValue = 219;
	private int floorValue = 16;
	public Support_Vector_Machine(){
		intToMove = new HashMap<Integer,Move>();
		moveToInt = new HashMap<Move,Integer>();
		fillMaps();
		images = new ArrayList<AdaptedYUYVImage>();
		moves = new ArrayList<Move>();
		model = new svm_model();
	}
	private void fillMaps(){
		int count = 0;
		for (Move m: Move.values()){
			moveToInt.put(m, count);
			intToMove.put(count, m);
			count += 1;
		}
	}
	@Override
	public void train(AdaptedYUYVImage img, Move current) {
		images.add(img.shrunken(shrinkValue));
		moves.add(current);
		trained = false;
	}
	
	
	@Override
	public Move bestMatchFor(AdaptedYUYVImage img) {
		if (!trained){
			model = trainSVM();
			trained = true;
		}
		
		svm_node[] imageAsNode = imageToNode(img);
		double answer = svm.svm_predict(model, imageAsNode);
		return intToMove.get((int)answer);
		// will casting to int cause issues when indexing? 
		
	}
	
	private svm_node[] imageToNode(AdaptedYUYVImage img){
		ArrayList<svm_node> nodes = new ArrayList<>();
		img = img.shrunken(shrinkValue);
		for (int x = 0; x < img.getWidth(); x++){
			int count = 0;
			for (int y = 0; y < img.getHeight(); y++){
				svm_node node = new svm_node();
				node.index = count;
				node.value = (double) (img.getY(x, y) - floorValue)/scaleValue;
				nodes.add(node);
				count++;
			}
		}
		svm_node[] nodeListOut = new svm_node[nodes.size()-1];
		for (int i = 0; i < nodes.size()-1; i++){
			nodeListOut[i] = nodes.get(i);
		}
		return nodeListOut;
	}
	private svm_model trainSVM(){
		/* By looking at the Y values in each of the images, I create a gray color histogram
		 * theoretically this histogram can be used to represent the entire image.
		 * I have scaled the image down by the Shrink Value, however, this does not seem to change
		 * the svm's output. I need to further analyze/read/guess which parameters to use. 
		 * Supposedly Weka has a function which performs a grid search to modify the C and gamma
		 * Values. I may look into saving this current file as a format to work in Weka, retrieve
		 * the C and Gamma values for the data set. But, there is a possibility there are different
		 * C and Gamma values for different videos, and thus, the weka grid search for C and Gamma 
		 * would have to be repeated every training. This may push a redesign of the class to being 
		 * written in Weka. 
		 * 
		 * Another addition I could do, would be to incorporate the U and V values as features for 
		 * the SVM and create a larger feature set for the SVM to train on.
		 */
		svm_problem problem = new svm_problem();
		int features = (images.get(0).getHeight())*(images.get(0).getWidth());
		int dataSize = images.size();
		problem.y = new double[dataSize];
		problem.l = dataSize;
		problem.x = new svm_node[dataSize][features];
		for(int i = 0; i < dataSize; i++){
			AdaptedYUYVImage image = images.get(i);
			int count = 0;
			for (int x = 0; x < images.get(0).getWidth(); x++){
				for (int y = 0; y < images.get(0).getHeight(); y++){
					svm_node node = new svm_node();
					node.index = count;
					node.value = (double) (image.getY(x, y) - floorValue)/scaleValue;
					problem.x[i][count] = node;
					count++;
				}
			}
			problem.y[i] = moveToInt.get(moves.get(i));
		}
		/* the pdf at http://www.csie.ntu.edu.tw/~cjlin/papers/guide/guide.pdf tells us to
		 * modify the C values and the Gamma values if we use svm_parameter.RBF as the kernel.
		 * However, I do not know which kernel to use / which svm_type to use / which values
		 * to use as the overall uses. I am also using as reference 
		 * https://www.csie.ntu.edu.tw/~r94100/libsvm-2.8/README
		 * 
		 */
		svm_parameter param = new svm_parameter();
	    param.probability = 0;
	    param.gamma = .01;
	    param.nu = 0.5;
	    param.degree = 10;
	    param.C = 10;
	    param.svm_type = svm_parameter.C_SVC;
	    param.kernel_type = svm_parameter.RBF;       
	    param.cache_size = 20000;
	    param.eps = 0.001; 
	    svm_model model2 = svm.svm_train(problem, param);
	    return model2;
	}
	
	@Override
	public boolean isTrained() {
		return trained;
	}

}
