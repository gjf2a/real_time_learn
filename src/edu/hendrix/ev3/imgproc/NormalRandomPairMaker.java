package edu.hendrix.ev3.imgproc;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

import edu.hendrix.ev3.util.Distribution;

public class NormalRandomPairMaker {
	public static void main(String[] args) throws IOException {
		if (args.length != 3) {
			System.err.println("Usage: NormalRandomPairMaker filetag numPairs patchSize");
			System.exit(1);
		}
		
		int numPairs = Integer.parseInt(args[1]);
		int patchSize = Integer.parseInt(args[2]);
		String filename = "Normal_BRIEF_" + numPairs + "_" + patchSize + "_" + args[0];
		PrintWriter out = new PrintWriter(new FileWriter(new File("src/edu/hendrix/ev3/imgproc/" + filename + ".java")));
		
		Distribution firstPoint = Distribution.makeNormal(patchSize, 0, patchSize / 5.0);
		Distribution[] secondPoints = new Distribution[patchSize];
		for (int i = 0; i < patchSize; i++) {
			secondPoints[i] = Distribution.makeNormal(patchSize, i - Distribution.normalMidpointIndex(patchSize), 0.08 * patchSize);
		}
		out.println("package edu.hendrix.ev3.imgproc;");
		out.println("public class " + filename + " extends PointPairList {");
		out.println("    private static PointPair[] points = new PointPair[]{");
		for (int i = 0; i < numPairs; i++) {
			int x1 = firstPoint.pick();
			int y1 = firstPoint.pick();
			int x2 = secondPoints[x1].pick();
			int y2 = secondPoints[y1].pick();
			while (x2 == x1 && y2 == y1) {
				x2 = secondPoints[x1].pick();
				y2 = secondPoints[y1].pick();
			}
			out.print("        new PointPair(" + x1 + "," + y1 + "," + x2 + "," + y2 + ")");
			out.println(i < numPairs - 1 ? ", " : " ");
		}
		out.println("    };");
		out.println("    public " + filename + "() {super(" + numPairs + ", i -> points[i]);}");
		out.println("}");
		out.close();
	}
	
	public static int randomOffset(Random rand, int patchSize) {
		return rand.nextInt(patchSize) - patchSize/2;
	}
}
