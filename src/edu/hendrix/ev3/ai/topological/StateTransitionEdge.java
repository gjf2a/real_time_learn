package edu.hendrix.ev3.ai.topological;

import edu.hendrix.ev3.util.DeepCopyable;

public class StateTransitionEdge implements DeepCopyable<StateTransitionEdge> {
	private int start, end, count;
	
	public StateTransitionEdge(int start, int end) {
		this(start, end, 0);
	}
	
	private StateTransitionEdge(int start, int end, int count) {
		this.start = start;
		this.end = end;
		this.count = count;
	}
	
	public StateTransitionEdge(String src) {
		String[] parts = src.split(";");
		this.start = Integer.parseInt(parts[0]);
		this.end = Integer.parseInt(parts[1]);
		this.count = Integer.parseInt(parts[2]);
	}
	
	public void bump() {
		count += 1;
	}
	
	public void absorb(StateTransitionEdge other) {
		this.count += other.count;
	}
	
	public int getCount() {return count;}
	public int getStart() {return start;}
	public int getEnd() {return end;}
	
	@Override
	public String toString() {
		return String.format("%d;%d;%d", start, end, count);
	}
	
	@Override
	public int hashCode() {return start * 10000 + end * 100 + count;}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof StateTransitionEdge) {
			StateTransitionEdge that = (StateTransitionEdge)other;
			return this.start == that.start && this.end == that.end && this.count == that.count;
		} else {
			return false;
		}
	}

	@Override
	public StateTransitionEdge deepCopy() {
		return new StateTransitionEdge(start, end, count);
	}
}
