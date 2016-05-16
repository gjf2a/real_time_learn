package edu.hendrix.ev3.ai.topological.image;

import java.util.ArrayList;

import edu.hendrix.ev3.ai.cluster.yuv.AdaptedYUYVImage;
import edu.hendrix.ev3.ai.cluster.yuv.YUYVDistanceFuncs;
import edu.hendrix.ev3.ai.topological.PathsFrom;
import edu.hendrix.ev3.ai.topological.StateMap;
import edu.hendrix.ev3.remote.Move;
import edu.hendrix.ev3.util.Util;

public class ImageStateMap extends StateMap<AdaptedYUYVImage> {
	private int shrinkFactor;
	
	public ImageStateMap(int numNodes, int shrinkFactor) {
		super(numNodes, YUYVDistanceFuncs::euclideanAllChannels);
		this.shrinkFactor = shrinkFactor;
	}
	
	public static ImageStateMap fromString(String src) {
		ArrayList<String> parts = Util.debrace(src);
		return new ImageStateMap(Integer.parseInt(parts.get(0)), parts.get(1));
	}
	
	private ImageStateMap(int shrinkFactor, String superSrc) {
		super(superSrc, AdaptedYUYVImage::fromString, YUYVDistanceFuncs::euclideanAllChannels);
		this.shrinkFactor = shrinkFactor;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append('{');
		sb.append(shrinkFactor);
		sb.append("}{");
		sb.append(super.toString());
		sb.append('}');
		return sb.toString();
	}
	
	@Override
	public int addTransition(int currentState, Move transition, AdaptedYUYVImage currentImg) {
		return super.addTransition(currentState, transition, currentImg.shrunken(shrinkFactor));
	}
	
	@Override
	public int getStateFor(AdaptedYUYVImage img) {
		return super.getStateFor(img.shrunken(shrinkFactor));
	}
	
	@Override
	public PathsFrom bestPathsFrom(AdaptedYUYVImage currentImg) {
		return super.bestPathsFrom(currentImg.shrunken(shrinkFactor));
	}
}
