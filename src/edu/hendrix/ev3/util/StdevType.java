package edu.hendrix.ev3.util;

public enum StdevType {
	POPULATION {
		@Override
		public int varianceDenom(int count) {
			return count;
		}
	}, SAMPLE {
		@Override
		public int varianceDenom(int count) {
			return count - 1;
		}
	};
	
	abstract public int varianceDenom(int count);
}
