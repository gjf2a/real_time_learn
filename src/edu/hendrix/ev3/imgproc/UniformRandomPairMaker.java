package edu.hendrix.ev3.imgproc;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

public class UniformRandomPairMaker {
	public static void main(String[] args) throws IOException {
		if (args.length != 3) {
			System.err.println("Usage: UniformRandomPairMaker filetag numPairs patchSize");
			System.exit(1);
		}
		
		int numPairs = Integer.parseInt(args[1]);
		int patchSize = Integer.parseInt(args[2]);
		String filename = "Uniform_BRIEF_" + numPairs + "_" + patchSize + "_" + args[0];
		PrintWriter out = new PrintWriter(new FileWriter(new File("src/edu/hendrix/ev3/imgproc/" + filename + ".java")));
		
		Random rand = new Random();
		out.println("package edu.hendrix.ev3.imgproc;");
		out.println("public class " + filename + " extends PointPairList {");
		out.println("    private static PointPair[] points = new PointPair[]{");
		for (int i = 0; i < numPairs; i++) {
			out.print("        new PointPair(" + randomOffset(rand, patchSize) + "," + randomOffset(rand, patchSize) + "," + randomOffset(rand, patchSize) + "," + randomOffset(rand, patchSize) + ")");
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
