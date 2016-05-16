package edu.hendrix.ev3.remote.net.topological;

import edu.hendrix.ev3.remote.Move;

public enum Command {
	FORWARD {
		public Move getMove() {return Move.FORWARD;}
		public boolean isTraining() {return true;}
	}, BACKWARD {
		public Move getMove() {return Move.BACKWARD;}
		public boolean isTraining() {return true;}
	}, LEFT {
		public Move getMove() {return Move.SPIN_LEFT;}
		public boolean isTraining() {return true;}
	}, RIGHT {
		public Move getMove() {return Move.SPIN_RIGHT;}
		public boolean isTraining() {return true;}
	}, STOP {
		public boolean isTraining() {return false;}
	}, PHOTO {
		public boolean isTraining() {return false;}
	}, WANDER {
		public boolean isTraining() {return true;}
	}, NAVIGATE {
		public boolean isTraining() {return true;}
	}, NEW_BSOC {
		public boolean isTraining() {return false;}
	}, ARCHIVES {
		public boolean isTraining() {return false;}
	}, LOAD_BSOC {
		public boolean isTraining() {return false;}
	}, QUIT {
		public boolean isTraining() {return false;}
	};
	
	public Move getMove() {return Move.STOP;}
	abstract public boolean isTraining();
}
