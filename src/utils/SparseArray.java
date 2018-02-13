package utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

public class SparseArray implements Iterable<Couple> {

	ArrayList<Couple> array;
	Comparator<Couple> sorter;
	int length;

	public SparseArray(int length) {
		this.length = length;
		array = new ArrayList<Couple>(); // Empty ArrayList, all the values are equal to 0
		sorter = new Comparator<Couple>() { // Method used to keep the ArrayList ordered
			@Override
			public int compare(Couple cell1, Couple cell2) {
				if (cell1.getX() < cell2.getX()) {
					return -1;
				} else if (cell1.getX() > cell2.getX()) {
					return 1;
				} else {
					return 0;
				}
			}
		};
	}

	public int length() { // length getter
		return length;
	}

	public int effectiveLength() { // returns the number of elements != 0 inside the SparseArray
		return array.size();
	}

	public void setToValue(int x, double value) {
		if (x >= length) {
			throw new IndexOutOfBoundsException();
		}
		if (Double.compare(value, 0.0) == 0) { // If value is 0.0, a remove operation must be executed instead of
			// insertion
			setToZero(x);
		} else {
			if (isZero(x)) {
				array.add(new Couple(x, value)); // Adds the value
				Collections.sort(array, sorter); // Sorts the elements of the matrix
			} else {
				getCell(x).setValue(value);
			}
		}
	}

	public boolean isZero(int x) { // Checks if the cell in position x has value equals to 0.0
		if (Double.compare(getValue(x), 0.0) == 0) {
			return true;
		}
		return false;
	}

	public boolean setToZero(int x) { // Sets the cell in position [X] to 0.0 (equals to removing it from the array)
		if (x >= length) {
			throw new IndexOutOfBoundsException();
		}
		if (!isZero(x)) { // if the cell exists, remove it
			return array.remove(getCell(x));
		}

		return true; // Element not found (cell value is already 0)
	}

	public double getValue(int x) {
		if (x >= length) {
			throw new IndexOutOfBoundsException();
		}
		for (Couple cell : array) {
			if (cell.getX() == x) {
				return cell.getValue(); // Cell [X] has value different from 0 (it's inside the ArrayList)
			} else if (cell.getX() > x) {
				return 0.0;
			}
		}
		return 0.0; // Cell [X] has value equal to 0 (it's not inside the ArrayList)
	}

	public Couple getCell(int x) {
		if (x >= length) {
			throw new IndexOutOfBoundsException();
		}
		for (Couple cell : array) {
			if (cell.getX() == x) {
				return cell; // Cell [X] has value different from 0 (it's inside the ArrayList)
			} else if (cell.getX() > x) {
				return null;
			}
		}
		return null; // Cell [X] has value equal to 0 (it's not inside the ArrayList)
	}

	public ArrayList<Couple> getNonZeroArray() { // Returns an array filled with only cells which value is != 0.0
		return array;
	}

	@Override
	public String toString() {
		StringBuilder stringbuilded = new StringBuilder("");
		stringbuilded.append("{");
		int i;
		for (i = 0; i < array.size() - 1; i++) {
			stringbuilded.append(array.get(i).toString() + ",");
		}
		stringbuilded.append(array.get(i).toString());
		stringbuilded.append("}");
		return stringbuilded.toString();
	}

	@Override
	public Iterator<Couple> iterator() { // Used to allow the for-each paradigm
		return array.iterator();
	}

}
