package utils;

public class TemporalSparseMatrix {

	SparseArray[] matrix;
	int x;
	int y;

	public TemporalSparseMatrix(int x, int y) {
		this.x = x;
		this.y = y;
		matrix = new SparseArray[x]; // Empty Array
		for (int i = 0; i < matrix.length; i++) { // Fill array with sparse vectors
			matrix[i] = new SparseArray(this.y);
		}
	}

	public void setToValue(int x, int y, double value) { // Sets the value of cell [X,Y] to value
		matrix[x].setToValue(y, value);
	}

	public void setToZero(int x, int y) { // Sets the value of cell [X,Y] to 0.0
		matrix[x].setToZero(y);
	}

	public double get(int x, int y) { // returns the value of cell [X,Y]
		return matrix[x].getValue(y);
	}

	public SparseArray getColumn(int column) { // returns a column
		return matrix[column];
	}

	@Override
	public String toString() { // Returns a printable representation of the matrix in a vectorial format
		StringBuilder stringbuilded = new StringBuilder("");
		stringbuilded.append("{");
		int i;
		for (i = 0; i < matrix.length - 1; i++) {
			stringbuilded.append(matrix[i].toString() + ",");
		}
		stringbuilded.append(matrix[i].toString());
		stringbuilded.append("}");
		return stringbuilded.toString();
	}

	public String toStringMatrix() { // Returns a printable, human-like representation of the matrix
		StringBuilder stringbuilded = new StringBuilder("");
		for (int i = 0; i < matrix.length; i++) {
			int j;
			for (j = 0; j < matrix[i].length - 1; j++) {
				stringbuilded.append(get(i, j) + "\t");
			}
			if (matrix[i].length > 1) {
				stringbuilded.append(get(i, j));
			}
			stringbuilded.append("\n");
		}
		return stringbuilded.toString();
	}

	public int rows() { // number of rows getter
		return x;
	}

	public int columns() { // number of columns getter
		return y;
	}

}