package utils;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

public class Enumerator {

	LinkedHashMap<Integer, String> dictionary;

	public Enumerator() {
		dictionary = new LinkedHashMap<Integer, String>();
	}

	/**
	 * Returns the current number of enumerated objects
	 *
	 * @return an integer representing the number of enumerated objects
	 */
	public int size() {
		return dictionary.size();
	}

	/**
	 * Associate a natural value to an object
	 *
	 * @param string the object that must be enumerated
	 * 
	 * @return
	 */
	public int add(String string) {
		if (!dictionary.containsValue(string)) {
			dictionary.put(size(), string);
		}
		return getIndex(string);
	}

	/**
	 * Returns the i-th object
	 *
	 * @param order the index of the value to be returned
	 * @return the i-th object
	 */
	public String getValueAt(int order) {
		return dictionary.get(order);
	}

	/**
	 * Returns the index of an object, or -1 if the object isn't defined
	 *
	 * @param string the object you need to get its index
	 */
	public int getIndex(String string) {
		for (Entry<Integer, String> entry : dictionary.entrySet()) {
			if (entry.getValue().equals(string)) {
				return entry.getKey();
			}
		}
		return -1;
	}
}
