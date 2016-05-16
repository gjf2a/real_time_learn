package edu.hendrix.ev3.util;

public class Stopwatch {
	private long start = 0, end = 1;
	
	public void start() {stop(); start = System.currentTimeMillis();}
	public void stop() {end = System.currentTimeMillis();}
	public long getDurationMillis() {return end - start;}
	public boolean isRunning() {return end <= start;}
}
