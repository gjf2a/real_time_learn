package edu.hendrix.ev3.ai.topological;

import static org.junit.Assert.*;

import org.junit.Test;

public class PatientMoveSelectorTest {
	
	public static final double EPSILON = 0.0001;
	
	public static double failMin(double probSuccess, int expectedSteps) {
		return 1.0 - Math.pow(1.0 - probSuccess, expectedSteps);
	}
	
	public void estimatorOk(int target, double probSuccess) { 
		int result = PatientMoveSelector.estimatedSteps(probSuccess, failMin(probSuccess, target));
		assertTrue(result == target || result == target - 1);
	}

	@Test
	public void stepEstimatorTest1() {
		estimatorOk(1, 0.7);
	}
	
	@Test
	public void stepEstimatorTest2() {
		estimatorOk(2, 0.7);
	}

	@Test
	public void stepEstimatorTest3() {
		estimatorOk(3, 0.7);
	}

	@Test
	public void stepEstimatorTest10() {
		estimatorOk(10, 0.7);
	}

}
