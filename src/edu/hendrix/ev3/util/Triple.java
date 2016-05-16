package edu.hendrix.ev3.util;

public class Triple<X,Y,Z> extends Duple<X,Y> {
	private Z z;
	
	public Triple(X x, Y y, Z z) {
		super(x, y);
		this.z = z;
	}
	
	public Z getThird() {return z;}
}
