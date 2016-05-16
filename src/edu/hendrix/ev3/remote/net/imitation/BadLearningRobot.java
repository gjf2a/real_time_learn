package edu.hendrix.ev3.remote.net.imitation;

import java.io.IOException;

import edu.hendrix.ev3.remote.Move;
import edu.hendrix.ev3.util.Duple;

// The purpose of this class is to serve as an experimental control.
// The user's movement commands are intercepted and replaced with
// randomized commands.

public class BadLearningRobot extends LearningRobot {
	public static void main(String[] args) throws IOException {
		new BadLearningRobot().mainLoop();
	}

	public BadLearningRobot() throws IOException {
		super();
	}
	
	@Override
	void setMoveMode(Duple<Move,Mode> fromCmd) {
		Duple<Move,Mode> bogus = new Duple<>(fromCmd.getFirst(), 
				fromCmd.getSecond() == Mode.TRAIN ? Mode.BAD_TRAIN : fromCmd.getSecond());
		super.setMoveMode(bogus);
	}
}
