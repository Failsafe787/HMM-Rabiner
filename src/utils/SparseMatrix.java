/*
 * Released under MIT License (Expat)
 * @author Luca Banzato
 * @version 0.1.8
 */

package utils;

import java.util.ArrayList;
import java.util.Iterator;

public class SparseMatrix implements Iterable<SparseArray> {

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
		StringBuilder finalString = new StringBuilder("");
		finalString.append("{");
		int i;
		for (i = 0; i < matrix.size(); i++) {
			finalString.append(matrix.get(i).toString() + ",");
		}
		if (i > 0) {
			finalString.deleteCharAt(finalString.length() - 1);
		}
		finalString.append("}");
		return finalString.toString();
	}

	public String toStringMatrix() { // Returns a printable, human-like representation of the matrix
		StringBuilder finalString = new StringBuilder("");
		for (int i = 0; i < matrix.size(); i++) {
			int j;
			for (j = 0; j < matrix.get(i).size(); j++) {
				finalString.append(getValue(i, j) + "\t");
			}
			if (j > 0) {
				finalString.deleteCharAt(finalString.length() - 1);
			}
			finalString.append("\n");
		}
		return finalString.toString();
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