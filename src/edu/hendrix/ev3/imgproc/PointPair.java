package edu.hendrix.ev3.imgproc;

public class PointPair {
	 private int x1, y1, x2, y2;
	 
	 public PointPair(int x1, int y1, int x2, int y2) {
		 this.x1 = x1;
		 this.y1 = y1;
		 this.x2 = x2;
		 this.y2 = y2;
	 }
	 
	 public <T extends ProcessableImage<T>> boolean test(ProcessableImage<T> img, int keyX, int keyY) {
		 int xk1 = keyX + x1;
		 int yk1 = keyY + y1;
		 int xk2 = keyX + x2;
		 int yk2 = keyY + y2;
		 return img.inBounds(xk1, yk1) && img.inBounds(xk2, yk2) 
				 ? img.getIntensity(xk1, yk1) < img.getIntensity(xk2, yk2)
				 : true;
	 }
}
