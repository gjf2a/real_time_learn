package edu.hendrix.ev3.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.function.Function;

public class StampedStorage {
	public static Duple<LocalDateTime,Integer> colonify(String src) {
		String[] parts = src.split(";");
		int suffix = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
		return new Duple<>(LocalDateTime.parse(parts[0].replace('_', ':')), suffix);
	}
	
	public static String decolonify(LocalDateTime when, int suffix) {
		String result = when.toString().replace(':', '_');
		if (suffix > 0) {
			result += ";" + Integer.toString(suffix);
		}
		return result;
	}
	
	public static void save(Object obj, LocalDateTime when) throws FileNotFoundException {
		save(obj, decolonify(when, 0));
	}
	
	public static void revise(Object obj, LocalDateTime when, int suffix) throws FileNotFoundException {
		save(obj, decolonify(when, suffix));
	}
	
	private static void save(Object obj, String name) throws FileNotFoundException {
		File dir = new File(obj.getClass().getName());
		if (!dir.exists()) {
			if (!dir.mkdir()) {
				throw new IllegalStateException("Can't create directory " + dir.getName());
			}
		}
		File output = new File(dir, name);
		PrintWriter out = new PrintWriter(output);
		out.write(obj.toString());
		out.close();		
	}
	
	public static ArrayList<Duple<LocalDateTime,Integer>> getAvailableFor(Class<?> cl) {
		File dir = new File(cl.getName());
		String[] filenames = dir.list();
		ArrayList<Duple<LocalDateTime,Integer>> result = new ArrayList<>();
		for (int i = 0; i < filenames.length; i++) {
			result.add(colonify(filenames[i]));
		}
		return result;
	}
	
	public static <T> T open(Class<T> cl, LocalDateTime when, int suffix, Function<String,T> extractor) throws FileNotFoundException {
		String className = cl.getName();
		File dir = new File(className);
		if (!dir.exists() || !dir.isDirectory()) {
			throw new IllegalStateException("No such directory: " + className);
		}
		Scanner input = new Scanner(new File(dir, decolonify(when, suffix)));
		String src = input.useDelimiter("\\Z").next();
		input.close();
		return extractor.apply(src);
	}
	
	public final static int DATE_TIME_BYTES = 5 + 2 * Integer.BYTES;
	
	public static byte[] localDateTime2bytes(LocalDateTime stamp) {
		ByteBuffer buffer = ByteBuffer.allocate(DATE_TIME_BYTES);
		putInto(stamp, buffer);
		return buffer.array();
	}
	
	public static LocalDateTime bytes2LocalDateTime(byte[] bytes) {
		Util.assertArgument(DATE_TIME_BYTES == bytes.length, "Wrong number of bytes");
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		return getFrom(buffer);
	}
	
	public static void putInto(LocalDateTime stamp, ByteBuffer buffer) {
		buffer.put((byte)stamp.getDayOfMonth());
		buffer.put((byte)stamp.getMonthValue());
		buffer.put((byte)stamp.getHour());
		buffer.put((byte)stamp.getMinute());
		buffer.put((byte)stamp.getSecond());
		buffer.putInt(stamp.getYear());
		buffer.putInt(stamp.getNano());
	}
	
	public static LocalDateTime getFrom(ByteBuffer buffer) {
		byte day = buffer.get();
		byte month = buffer.get();
		byte hour = buffer.get();
		byte minute = buffer.get();
		byte second = buffer.get();
		int year = buffer.getInt();
		int nano = buffer.getInt();
		return LocalDateTime.of(year, month, day, hour, minute, second, nano);
	}
}
