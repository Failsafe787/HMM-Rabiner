/*
 * Released under MIT License (Expat)
 * @author Luca Banzato
 * @version 0.1.8
 */

package utils;

import java.util.ArrayList;
import java.util.Iterator;

public class SparseMatrix implements Iterable<SparseArray>{

	private ArrayList<SparseArray> matrix;
	private int x;
	private int y;

	public SparseMatrix(int x, int y) {
		this.x = x;
		this.y = y;
		matrix = new ArrayList<SparseArray>(); // Empty Array
		for (int i = 0; i < x; i++) { // Fill array with sparse vectors
			matrix.add(new SparseArray(this.y));
		}
	}

	public void setToValue(int x, int y, double value) { // Sets the value of cell [X,Y] to value
		matrix.get(x).setToValue(y, value);
	}

	public void setToZero(int x, int y) { // Sets the value of cell [X,Y] to 0.0
		matrix.get(x).setToZero(y);
	}

	public double getValue(int x, int y) { // returns the value of cell [X,Y]
		return matrix.get(x).getValue(y);
	}

	public SparseArray getColumn(int column) { // returns a column
		return matrix.get(column);
	}

	@Override
	public String toString() { // Returns a printable representation of the matrix in a vectorial format
		StringBuilder stringbuilded = new StringBuilder("");
		stringbuilded.append("{");
		int i;
		for (i = 0; i < matrix.size(); i++) {
			stringbuilded.append(matrix.get(i).toString() + ",");
		}
		if (i > 0) {
			stringbuilded.deleteCharAt(stringbuilded.length() - 1);
		}
		stringbuilded.append("}");
		return stringbuilded.toString();
	}

	public String toStringMatrix() { // Returns a printable, human-like representation of the matrix
		StringBuilder stringbuilded = new StringBuilder("");
		for (int i = 0; i < matrix.size(); i++) {
			int j;
			for (j = 0; j < matrix.get(i).size(); j++) {
				stringbuilded.append(getValue(i, j) + "\t");
			}
			if (j > 0) {
				stringbuilded.deleteCharAt(stringbuilded.length() - 1);
			}
			stringbuilded.append("\n");
		}
		return stringbuilded.toString();
	}

	public int getRowsNumber() { // number of rows getter
		return x;
	}

	public int getColumnsNumber() { // number of columns getter
		return y;
	}
	
	@Override
	public Iterator<SparseArray> iterator() { // Used to allow the for-each paradigm
		return matrix.iterator();
	}

}