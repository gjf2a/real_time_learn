package edu.hendrix.ev3.util;


import java.io.File;
import java.io.FilenameFilter;
import java.util.*;

@SuppressWarnings("rawtypes")
public class AIReflector<T> {
	private final static String suffix = ".class";
	
	private Map<String,Class> name2type;
	
	private FilenameFilter filter = new FilenameFilter(){public boolean accept(File dir, String name) {
			return name.endsWith(suffix);
		}};
		
	private void addFrom(Class superType, String packageName) {
		String targetDirName = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
		File targetDir = new File(targetDirName + packageName.replace('.', File.separatorChar));
		if (!targetDir.isDirectory()) {throw new IllegalArgumentException(targetDir + " is not a directory");}
		for (File f: targetDir.listFiles(filter)) {
			String name = f.getName();
			name = name.substring(0, name.length() - suffix.length());
			try {
				Class type = Class.forName(packageName + "." + name);
				Object obj = type.newInstance();
				if (superType.isInstance(obj)) {
					name2type.put(name, type);
				}
				// If an exception is thrown, we omit the type.
				// Hence, ignore all three exceptions.
			} catch (ClassNotFoundException e) {
			} catch (InstantiationException e) {
			} catch (IllegalAccessException e) {
			}
		}		
	}
		
	public AIReflector(Class superType, String... packageNames) {
		this.name2type = new TreeMap<String,Class>();
		for (String packageName: packageNames) {
			addFrom(superType, packageName);
		}
	}
	
	public ArrayList<String> getTypeNames() {
		return new ArrayList<String>(name2type.keySet());
	}
	
	public String toString() {
		String result = "Available:";
		for (String s: name2type.keySet()) {
			result += " " + s;
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public T newInstanceOf(String typeName) throws InstantiationException, IllegalAccessException {
		return (T)name2type.get(typeName).newInstance();
	}
}
