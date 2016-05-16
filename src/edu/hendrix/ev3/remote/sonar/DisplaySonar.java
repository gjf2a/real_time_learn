package edu.hendrix.ev3.remote.sonar;

import java.util.EnumMap;

import lejos.hardware.Button;
import lejos.hardware.lcd.LCD;

public class DisplaySonar {
	private EnumMap<SonarPosition,Float> maxValues = new EnumMap<>(SonarPosition.class);
	private ThreeSonarBot bot;
	
	public DisplaySonar() {this(new ThreeSonarBot());}
	public DisplaySonar(ThreeSonarBot bot) {this.bot = bot;}
	
	public void cycle() {
		bot.pollSonars();
		int row = 0;
		for (SonarPosition pos: SonarPosition.values()) {
			float dist = bot.getLastDistance(pos);
			if (Float.isFinite(dist) && (!maxValues.containsKey(pos) || dist > maxValues.get(pos))) {
				maxValues.put(pos, dist);
			}
			display(row, pos, dist);
			row += 1;
		}
	}
	
	public void display(int row, SonarPosition pos, float value) {
		LCD.drawString(format(pos, value), 0, row);
	}
	
	public String format(SonarPosition pos, float value) {
		return String.format("%s:%3.2f    ", pos.toString(), value);
	}
	
	public void displayMax() {
		int row = 0;
		for (SonarPosition pos: maxValues.keySet()) {
			display(row, pos, maxValues.get(pos));
			row += 1;
		}
	}
	
	public void loop() {
		while (!Button.ESCAPE.isDown()) {
			cycle();
		}
		displayMax();
		while (!Button.ENTER.isDown());
	}
	
	public static void main(String[] args) {
		new DisplaySonar().loop();
	}
}
