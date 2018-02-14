/*
 * Released under MIT License (Expat)
 * @author Luca Banzato
 * @version 0.1
 */

package utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

public class SparseMatrix implements Iterable<Triplet> {

	ArrayList<Triplet> matrix;
	Comparator<Triplet> sorter;
	int rowsNumber;
	int columnsNumber;

	public SparseMatrix(int x, int y) {
		rowsNumber = x;
		columnsNumber = y;
		matrix = new ArrayList<Triplet>(); // Empty ArrayList, all the values are equal to 0
		sorter = new Comparator<Triplet>() { // Method used to keep the ArrayList ordered
			@Override
			public int compare(Triplet cell1, Triplet cell2) {
				if (cell1.getX() < cell2.getX()) {
					return -1;
				} else if (cell1.getX() > cell2.getX()) {
					return 1;
				} else {
					if (cell1.getY() < cell2.getY()) {
						return -1;
					} else if (cell1.getY() > cell2.getY()) {
						return 1;
					} else {
						return 0;
					}
				}
			}
		};
	}

	public int getRowsNumber() { // rows number getter
		return rowsNumber;
	}

	public int getColumnsNumber() { // columns number getter
		return columnsNumber;
	}

	public int effectiveSize() { // returns the number of elements != 0 inside the SparseMatrix
		return matrix.size();
	}

	public void setToValue(int x, int y, double value) { // Edit a value inside the matrix
		if (x >= rowsNumber || y >= columnsNumber) {
			throw new IndexOutOfBoundsException();
		}
		if (Double.compare(value, 0.0) == 0) { // If value is 0.0, a remove operation must be executed instead of
												// insertion
			setToZero(x, y);
		} else {
			if (isZero(x, y)) {
				matrix.add(new Triplet(x, y, value)); // Adds the value
				Collections.sort(matrix, sorter); // Sorts the elements of the matrix
			} else {
				getCell(x, y).setValue(value);
			}
		}
	}

	public boolean isZero(int x, int y) {
		if (Double.compare(getValue(x, y), 0.0) == 0) {
			return true;
		}
		return false;
	}

	public boolean setToZero(int x, int y) { // Sets the cell in position [X,Y] to 0.0 (equals to removing it from the
												// array)
		if (x >= rowsNumber || y >= columnsNumber) {
			throw new IndexOutOfBoundsException();
		}
		if (!isZero(x, y)) { // if the cell exists, remove it
			return matrix.remove(getCell(x, y));
		}

		return true; // Element not found (cell value is already 0)
	}

	public double getValue(int x, int y) {
		if (x >= rowsNumber || y >= columnsNumber) {
			throw new IndexOutOfBoundsException();
		}
		for (Triplet cell : matrix) {
			if (cell.getX() == x && cell.getY() == y) {
				return cell.getValue(); // Cell [X,Y] has value different from 0 (it's inside the ArrayList)
			} else if (cell.getX() >= x && cell.getY() > y) {
				return 0.0;
			}
		}
		return 0.0; // Cell [X,Y] has value equal to 0 (it's not inside the ArrayList)
	}

	public Triplet getCell(int x, int y) {
		if (x >= rowsNumber || y >= columnsNumber) {
			throw new IndexOutOfBoundsException();
		}
		for (Triplet cell : matrix) {
			if (cell.getX() == x && cell.getY() == y) {
				return cell; // Cell [X,Y] has value different from 0 (it's inside the ArrayList)
			} else if (cell.getX() >= x && cell.getY() > y) {
				return null;
			}
		}
		return null; // Cell [X,Y] has value equal to 0 (it's not inside the ArrayList)
	}

	@Override
	public String toString() { // Print the values != 0.0 inside the matrix alongside their coordinates (x,y)
		StringBuilder stringbuilded = new StringBuilder("");
		stringbuilded.append("{");
		int i;
		for (i = 0; i < matrix.size() - 1; i++) {
			stringbuilded.append(matrix.get(i).toString() + ",");
		}
		stringbuilded.append(matrix.get(i).toString());
		stringbuilded.append("}");
		return stringbuilded.toString();
	}

	@Override
	public Iterator<Triplet> iterator() { // Used to allow the for-each paradigm
		return matrix.iterator();
	}

}
