package edu.hendrix.ev3.util;

import java.util.ArrayList;

public class CircularList<T> {
	private ArrayList<T> items = new ArrayList<>();
	private int index = 0;
	
	public T getCurrentItem() {return items.get(index);}
	public int getCurrentIndex() {return index;}
	public int size() {return items.size();}
	
	public void forward() {if (items.size() > 0) index = (index + 1) % items.size();}
	public void backward() {if (items.size() > 0) index = (index - 1 + items.size()) % items.size();}
	
	public void add(T item) {
		items.add(item);
		index = items.size() - 1;
	}
}
