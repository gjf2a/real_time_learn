package edu.hendrix.ev3.remote.net.topological.sonar;

import edu.hendrix.ev3.remote.Move;

public enum Command {
	FORWARD {
		public Move getMove() {return Move.FORWARD;}
	}, BACKWARD {
		public Move getMove() {return Move.BACKWARD;}
	}, LEFT {
		public Move getMove() {return Move.SPIN_LEFT;}
	}, RIGHT {
		public Move getMove() {return Move.SPIN_RIGHT;}
	}, STOP, NAVIGATE, NEW_BSOC, ARCHIVES, LOAD_BSOC, QUIT;
	
	public Move getMove() {return Move.STOP;}
}
