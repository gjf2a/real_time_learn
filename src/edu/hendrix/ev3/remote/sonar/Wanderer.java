package edu.hendrix.ev3.remote.sonar;

import edu.hendrix.ev3.remote.Move;
import edu.hendrix.ev3.remote.Mover;
import lejos.hardware.Button;

public class Wanderer extends ThreeSonarBot {
	private float minDistance;
	
	public Wanderer(float minDistance) {
		super();
		this.minDistance = minDistance;
	}
	
	public Move selectMove() {
		pollSonars();
		if (closestDistance() < minDistance) {
			if (getLastDistance(SonarPosition.LEFT) > getLastDistance(SonarPosition.RIGHT)) {
				return Move.LEFT;
			} else {
				return Move.RIGHT;
			}
		} else {
			return Move.FORWARD;
		}
	}
	
	public float closestDistance() {
		float result = Float.MAX_VALUE;
		for (SonarPosition sonar: SonarPosition.values()) {
			result = Math.min(result, getLastDistance(sonar));
		}
		return result;
	}
	
	public static void main(String[] args) {
		Wanderer wanderer = new Wanderer(0.3f);
		while (!Button.ESCAPE.isDown()) {
			Mover.move(wanderer.selectMove());
		}
	}
}
