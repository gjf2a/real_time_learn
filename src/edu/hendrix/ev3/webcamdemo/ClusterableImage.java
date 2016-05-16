package edu.hendrix.ev3.webcamdemo;

import java.awt.image.BufferedImage;
import java.util.EnumMap;

import edu.hendrix.ev3.ai.cluster.Clusterable;
import edu.hendrix.ev3.imgproc.ProcessableImage;
import edu.hendrix.ev3.util.DeepCopyable;
import edu.hendrix.ev3.util.Duple;
import edu.hendrix.ev3.util.Util;
import javafx.scene.paint.Color;

public class ClusterableImage implements ProcessableImage<ClusterableImage>, Clusterable<ClusterableImage>, DeepCopyable<ClusterableImage> {
	private BufferedImage img;
	
	private ClusterableImage() {}
	
	public static BufferedImage copy(BufferedImage src) {
		BufferedImage result = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
		for (int x = 0; x < src.getWidth(); x++) {
			for (int y = 0; y < src.getHeight(); y++) {
				result.setRGB(x, y, src.getRGB(x, y));
			}
		}
		return result;
	}
	
	public void addHighlight(int x, int y) {
		img.setRGB(x, y, 0xffff0000);
	}
	
	public ClusterableImage(BufferedImage img) {
		this.img = copy(img);
	}

	@Override
	public ClusterableImage weightedCentroidWith(ClusterableImage that, long thisCount, long thatCount) {
		Util.assertArgument(this.img.getWidth() == that.img.getWidth() && this.img.getHeight() == that.img.getHeight(), "Images are different sizes");
		BufferedImage target = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
		for (int x = 0; x < target.getWidth(); x++) {
			for (int y = 0; y < target.getHeight(); y++) {
				int thisPixel = this.img.getRGB(x, y);
				int thatPixel = that.img.getRGB(x, y);
				EnumMap<ColorChannel,Integer> colorCombos = new EnumMap<>(ColorChannel.class);
				for (ColorChannel c: ColorChannel.values()) {
					long den = thisCount + thatCount;
					long combo = thisCount * c.extractFrom(thisPixel) + thatCount * c.extractFrom(thatPixel);
					colorCombos.put(c, (int)(combo / den));
				}
				target.setRGB(x, y, ColorChannel.buildPixelFrom(colorCombos));
			}
		}
		return new ClusterableImage(target);
	}
	
	public javafx.scene.paint.Color getColor(int x, int y) {
		return ColorChannel.buildColorFrom(getRGB(x, y));
	}

	public int getRGB(int x, int y) {return img.getRGB(x, y);}
	public int getWidth() {return img.getWidth();}
	public int getHeight() {return img.getHeight();}
	
	public void setRGB(int x, int y, Color c) {
		img.setRGB(x, y, ColorChannel.buildPixelFrom(c));
	}
	
	public static long distance(ClusterableImage img1, ClusterableImage img2) {
		Util.assertArgument(img1.getWidth() == img2.getWidth() && img1.getHeight() == img2.getHeight(), "Images must be the same size");
		long total = 0;
		for (int x = 0; x < img1.getWidth(); x++) {
			for (int y = 0; y < img2.getHeight(); y++) {
				EnumMap<ColorChannel,Integer> colors1 = ColorChannel.buildChannelsFrom(img1.getRGB(x, y));
				EnumMap<ColorChannel,Integer> colors2 = ColorChannel.buildChannelsFrom(img2.getRGB(x, y));
				for (ColorChannel c: ColorChannel.values()) {
					int diff = colors1.get(c) - colors2.get(c);
					total += diff * diff;
				}
			}
		}
		return total;
	}

	@Override
	public ClusterableImage deepCopy() {
		return new ClusterableImage(img);
	}

	@Override
	public int getIntensity(int x, int y) {
		int pixel = img.getRGB(x, y);
		return (ColorChannel.RED.extractFrom(pixel) + ColorChannel.GREEN.extractFrom(pixel) + ColorChannel.BLUE.extractFrom(pixel)) / 3;
	}

	@Override
	public ClusterableImage shrunken(int shrink) {
		ClusterableImage result = new ClusterableImage();
		result.img = new BufferedImage(getWidth() / shrink, getHeight() / shrink, BufferedImage.TYPE_INT_ARGB);
		for (int x = 0; x < result.img.getWidth(); x++) {
			for (int y = 0; y < result.img.getHeight(); y++) {
				result.img.setRGB(x, y, img.getRGB(x * shrink, y * shrink));
			}
		}
		return result;
	}

	@Override
	public ClusterableImage xConvolve1D(final int[] kernel) {
		return convolve1D(kernel, (i, x, y) -> new Duple<>(x + i - kernel.length/2, y));
	}

	@Override
	public ClusterableImage yConvolve1D(final int[] kernel) {
		return convolve1D(kernel, (i, x, y) -> new Duple<>(x, y + i - kernel.length/2));
	}
	
	public ClusterableImage convolve1D(final int[] kernel, ConvolveFunction srcIndex) {
		ClusterableImage result = new ClusterableImage();
		result.img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
		for (int x = 0; x < result.img.getWidth(); x++) {
			for (int y = 0; y < result.img.getHeight(); y++) {
				result.img.setRGB(x, y, convolvePixel(x, y, kernel, srcIndex));
			}
		}
		return result;
	}
	
	private int convolvePixel(int x, int y, final int[] kernel, ConvolveFunction srcIndex) {
		int numerator = 0, denominator = 0;
		for (int i = 0; i < kernel.length; i++) {
			Duple<Integer,Integer> src = srcIndex.getIndex(i, x, y);
			if (inBounds(src.getFirst(), src.getSecond())) {
				int srcRGB = getRGB(src.getFirst(), src.getSecond());
				final int k = kernel[i];
				numerator += ColorChannel.processPixel(srcRGB, v -> k * v);
				denominator += kernel[i];
			}
		}
		return denominator == 0 ? getRGB(x, y) : numerator / denominator;
	}
	
	public boolean inBounds(int x, int y) {
		return x >= 0 && y >= 0 && x < getWidth() && y < getHeight();
	}
}
