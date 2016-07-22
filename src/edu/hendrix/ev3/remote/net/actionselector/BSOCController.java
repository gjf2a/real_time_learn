package edu.hendrix.ev3.remote.net.actionselector;

import java.util.ArrayList;
import java.util.HashMap;

import edu.hendrix.ev3.ai.cluster.yuv.AdaptedYUYVImage;
import edu.hendrix.ev3.ai.cluster.yuv.ImageBSOC;
import edu.hendrix.ev3.remote.Move;
import edu.hendrix.ev3.util.Duple;
import edu.hendrix.ev3.util.EnumHistogram;
import edu.hendrix.ev3.util.FixedSizeArray;
import edu.hendrix.ev3.util.Logger;
import edu.hendrix.ev3.util.Util;

public class BSOCController {
	private ImageBSOC bsoc;
	private FixedSizeArray<Move> moves;
	private int clustNum, shrinkNum;
	private HashMap<Integer, EnumHistogram<Move>> nodes;
	
	public BSOCController(int size, int shrinkFactor) {
		clustNum = size;
		shrinkNum = shrinkFactor;
		bsoc = new ImageBSOC(size, shrinkFactor);
		moves = FixedSizeArray.makeImmutableType(size);
		resetMoves(moves);
		nodes = new HashMap<Integer, EnumHistogram<Move>>();
	}
	
	private static void resetMoves(FixedSizeArray<Move> moves) {
		for (int i = 0; i < moves.capacity(); i++) {
			moves.put(i, Move.NONE);
		}
	}
	
	private BSOCController(ImageBSOC bsoc, FixedSizeArray<Move> moves) {
		this.bsoc = bsoc;
		this.moves = moves;
	}
	
	public static BSOCController fromString(String input) {
		ArrayList<String> parts1 = Util.debrace(input);
		ImageBSOC bsoc = ImageBSOC.fromString(parts1.get(0));
		FixedSizeArray<Move> moves = FixedSizeArray.makeImmutableType(bsoc.size());
		resetMoves(moves);
		if (parts1.size() > 1) {
			for (String moveSpec: Util.debrace(parts1.get(1))) {
				ArrayList<String> parts2 = Util.debrace(moveSpec);
				int i = Integer.parseInt(parts2.get(0));
				Move m = Move.valueOf(parts2.get(1));
				moves.put(i, m);
			}
		}
		return new BSOCController(bsoc, moves);
	}
	
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append('{');
		result.append(bsoc.toString());
		result.append("}{");
		for (int i = 0; i < moves.capacity(); i++) {
			if (moves.containsKey(i) && moves.get(i) != Move.NONE) {
				result.append("{{");
				result.append(i);
				result.append("}{");
				result.append(moves.get(i));
				result.append("}}");
			}
		}
		result.append("}");
		return result.toString();
	}
	
	public int train(AdaptedYUYVImage example) {
		return bsoc.train(example);
	}
	
	public AdaptedYUYVImage getIdealInputFor(int node) {
		return bsoc.getIdealInputFor(node);
	}
	
	public Move getMoveFor(int node) {
		return moves.get(node);
	}
	
	public boolean nodeExists(int i) {
		return bsoc.nodeExists(i);
	}
	
	public int getNumMergesFor(int node) {
		return bsoc.getNumMergesFor(node);
	}
	
	public int getTotalSourceInputs() {
		return bsoc.getTotalSourceInputs();
	}
	
	public ArrayList<Integer> getClusterIds() {
		return bsoc.getClusterIds();
	}
	
	public int size() {return bsoc.size();}
	
	public void assignMoveFor(int node, Move m) {
		if (nodeExists(node)) {
			moves.put(node, m);
			updateNodes(node, m);
		}
	}
	
	private void updateNodes(int node, Move m){
		if (nodes.containsKey(node)){
			nodes.get(node).bump(m);
		} else {
			EnumHistogram<Move> value = new EnumHistogram<Move>(Move.class);
			value.bump(m);
			nodes.put(node,value);
		}
	}
	
	public Move pickMoveFor(AdaptedYUYVImage input) {
		Logger.EV3Log.log("input size" + bsoc.getNodeRanking(input).size());
		for (Duple<Integer,Long> candidate: bsoc.getNodeRanking(input)) {
			Move result = nodes.get(candidate.getFirst()).getHighestCounted();
//			Move result = getMoveFor(candidate.getFirst());
			Logger.EV3Log.log("getMoveFor() result: " + result.name());
			if (result != Move.NONE) {
				return result;
			}
 		}
		Logger.EV3Log.log("Nothing found");
		return Move.NONE;
	}
	public int getShrinkNum(){return shrinkNum;}
	public int getClustNum(){return clustNum;}
}
