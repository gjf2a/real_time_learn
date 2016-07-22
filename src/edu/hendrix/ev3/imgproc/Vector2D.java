package edu.hendrix.ev3.imgproc;

import java.util.ArrayList;

public class Vector2D {
	private double x, y;
	private double r, theta;
	
	public Vector2D(double x, double y) {
		this.x = x;
		this.y = y;
		this.r = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
		this.theta = Math.atan2(y, x);
	}
	
	public Vector2D(Feature src, Feature dest) {
		this(dest.X() - src.X(), dest.Y() - src.Y());
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof Vector2D) {
			Vector2D that = (Vector2D)other;
			return this.x == that.x && this.y == that.y;
		} else {
			return false;
		}
	}
	
	@Override
	public String toString() {
		return "(" + r + "," + theta + ")";
	}
	
	@Override
	public int hashCode() {
		return (int)(x * 10000 + y);
	}
	
	public double X() {return x;}
	public double Y() {return y;}
	public double R() {return r;}
	public double theta() {return theta;}
	
	public Vector2D unit() {
		return new Vector2D(Math.cos(theta), Math.sin(theta));
	}
	
	public Vector2D add(Vector2D other) {
		return new Vector2D(this.x + other.x, this.y + other.y);
	}
	
	public Vector2D scalarMultiply(double scalar) {
		return new Vector2D(scalar * x, scalar * y);
	}
	
	public static Vector2D mean(Iterable<Vector2D> vecs) {
		Vector2D sum = new Vector2D(0, 0);
		int denom = 0;
		for (Vector2D vec: vecs) {
			sum = sum.add(vec);
			denom += 1;
		}
		return sum.scalarMultiply(1.0 / (double)denom);
	}
	
	public static double rMean(Iterable<Vector2D> vecs) {
		double rSum = 0.0;
		int numR = 0;
		for (Vector2D v: vecs) {
			rSum += v.R();
			numR += 1;
		}
		return rSum / numR;
	}
	
	public static double thetaMean(Iterable<Vector2D> vecs) {
		ArrayList<Vector2D> units = new ArrayList<>();
		for (Vector2D v: vecs) {units.add(v.unit());}
		return mean(units).theta();
	}
}
