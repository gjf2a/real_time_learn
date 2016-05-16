package edu.hendrix.ev3.ai.topological.image;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.Test;

import edu.hendrix.ev3.util.Util;

public class ImageStateMapTest {

	@Test
	public void test() throws FileNotFoundException {
		File dir = new File(".");
		System.out.println(dir.getAbsolutePath());
		File input = new File("2016-01-26T16:12:54.029");
		String text = Util.fileToString(input).trim();
		ImageStateMap map = ImageStateMap.fromString(text);
		map.assertInvariant();
		assertEquals(text, map.toString());
	}

}
