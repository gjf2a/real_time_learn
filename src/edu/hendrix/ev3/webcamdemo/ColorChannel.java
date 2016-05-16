package edu.hendrix.ev3.webcamdemo;

import java.util.EnumMap;
import java.util.function.IntUnaryOperator;

import javafx.scene.paint.Color;

public enum ColorChannel {
	ALPHA {
		@Override
		public int shift() {
			return 24;
		}

		@Override
		public double get(Color src) {
			return src.getOpacity();
		}
	}, RED {
		@Override
		public int shift() {
			return 16;
		}

		@Override
		public double get(Color src) {
			return src.getRed();
		}
	}, GREEN {
		@Override
		public int shift() {
			return 8;
		}

		@Override
		public double get(Color src) {
			return src.getGreen();
		}
	}, BLUE {
		@Override
		public int shift() {
			return 0;
		}

		@Override
		public double get(Color src) {
			return src.getBlue();
		}
	};
	
	abstract public int shift();
	abstract public double get(Color src);
	
	public int extractFrom(int pixel) {
		return (pixel >> shift()) & 0xFF;
	}
	
	public static int buildPixelFrom(Color c) {
		int pixel = 0;
		for (ColorChannel cc: values()) {
			pixel += cc.get(c);
		}
		return pixel;
	}
	
	public static int buildPixelFrom(EnumMap<ColorChannel,Integer> colors) {
		int pixel = 0;
		for (ColorChannel c: values()) {
			pixel += (colors.containsKey(c) ? colors.get(c) : 0) << c.shift();
		}
		return pixel;
	}
	
	public static Color buildColorFrom(int argb) {
		return Color.rgb(ColorChannel.RED.extractFrom(argb), 
				ColorChannel.GREEN.extractFrom(argb), 
				ColorChannel.BLUE.extractFrom(argb), 
				ColorChannel.ALPHA.extractFrom(argb) / 255.0);
	}
	
	public static EnumMap<ColorChannel,Integer> buildChannelsFrom(int argb) {
		EnumMap<ColorChannel,Integer> channels = new EnumMap<>(ColorChannel.class);
		for (ColorChannel c: values()) {
			channels.put(c, c.extractFrom(argb));
		}
		return channels;
	}
	
	public static int processPixel(int argb, IntUnaryOperator op) {
		EnumMap<ColorChannel,Integer> channels = buildChannelsFrom(argb);
		for (ColorChannel c: channels.keySet()) {
			channels.put(c, op.applyAsInt(channels.get(c)));
		}
		return buildPixelFrom(channels);
	}
}
