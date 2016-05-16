package edu.hendrix.ev3.ai.topological.sonar;

public enum MinMax {
	MIN {
		@Override
		public boolean meets(float target, float value) {
			return value >= target;
		}
		
		@Override
		public float op(float a, float b) {
			return Math.min(a, b);
		}

		@Override
		public MinMax other() {
			return MAX;
		}
	}, MAX {
		@Override
		public boolean meets(float target, float value) {
			return value <= target;
		}
		
		@Override
		public float op(float a, float b) {
			return Math.max(a, b);
		}

		@Override
		public MinMax other() {
			return MIN;
		}
	};
	
	abstract public float op(float a, float b);
	abstract public MinMax other();
	abstract public boolean meets(float target, float value);
}
