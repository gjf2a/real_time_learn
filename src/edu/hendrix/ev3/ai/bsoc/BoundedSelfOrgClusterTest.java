package edu.hendrix.ev3.ai.bsoc;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class BoundedSelfOrgClusterTest {
	BoundedSelfOrgCluster<BSOCTestee> bsoc1;
	final int MAX_NODES = 3;
	
	@Before
	public void setup() {
		bsoc1 = new BoundedSelfOrgCluster<BSOCTestee>(MAX_NODES, BSOCTestee::distance);
		stringTest("{3}\n{}\n{}");
		for (long value = 0; value < MAX_NODES; value++) {
			bsoc1.train(new BSOCTestee(value*value));
		}
	}

	@Test
	public void stringTest1() {
		stringTest("{3}\n{{{0}{1}{0}}{{1}{1}{1}}{{2}{1}{4}}}\n{{0;1;1}{1;2;3}{0;2;4}}");
	}

	@Test
	public void stringTest2() {
		bsoc1.train(new BSOCTestee(MAX_NODES * MAX_NODES));
		stringTest("{3}\n{{{0}{2}{0}}{{1}{1}{9}}{{2}{1}{4}}}\n{{1;2;5}{0;2;8}{0;1;18}}");
	}
	
	@Test
	public void stringTest3() {
		stringTest2();
		bsoc1.train(new BSOCTestee(3));
		stringTest("{3}\n{{{0}{2}{0}}{{1}{1}{9}}{{2}{2}{3}}}\n{{0;2;6}{1;2;12}{0;1;18}}");
	}
	
	public void stringTest(String target) {
		assertTrue(bsoc1.edgeRepresentationConsistent());
		assertEquals(target, bsoc1.toString());
		assertEquals(bsoc1, new BoundedSelfOrgCluster<>(bsoc1.toString(), BSOCTestee::new, BSOCTestee::distance));
	}
}
