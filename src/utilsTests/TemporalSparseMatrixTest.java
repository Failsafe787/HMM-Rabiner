/*
 * Released under MIT License (Expat)
 * @author Luca Banzato
 * @version 0.1
 */

package utilsTests;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import utils.SparseArray;
import utils.SparseMatrix;

class TemporalSparseMatrixTest {

	@Test
	public void testCreate() {
		SparseMatrix tspm = new SparseMatrix(0, 0);
		assertTrue(tspm != null);
	}

	@Test
	public void testSize() {
		SparseMatrix tspm = new SparseMatrix(5, 4);
		assertTrue(tspm.getRowsNumber() == 5 && tspm.getRowsNumber() == 4, "Size isn't correct!");
	}

	@Test
	public void testInsertion() {
		SparseMatrix tspm = new SparseMatrix(5, 4);
		tspm.setToValue(4, 3, 5.4);
		assertTrue(Double.compare(tspm.getValue(4, 3), 5.4) == 0, "Value insertion failed!");
		tspm.setToValue(1, 3, 0.97);
		assertTrue(Double.compare(tspm.getValue(1, 3), 0.97) == 0, "Value insertion failed!");
	}

	@Test
	public void testSetToZero() {
		SparseMatrix tspm = new SparseMatrix(5, 4);
		tspm.setToValue(4, 3, 5.4);
		tspm.setToValue(1, 3, 0.97);
		tspm.setToZero(4, 3);
		tspm.setToZero(1, 3);
		assertTrue(Double.compare(tspm.getValue(4, 3), 0.0) == 0, "Value deletion failed!");
		assertTrue(Double.compare(tspm.getValue(1, 3), 0.0) == 0, "Value deletion failed!");
	}

	@Test
	public void testGetColumn() {
		SparseMatrix tspm = new SparseMatrix(5, 4);
		tspm.setToValue(4, 3, 5.3);
		tspm.setToValue(4, 1, 5.1);
		tspm.setToValue(4, 0, 5.0);
		tspm.setToValue(1, 3, 0.97);
		SparseArray column = tspm.getColumn(4);
		assertTrue(column.toString().equals("{[0, 5.0],[1, 5.1],[3, 5.3]}"));
	}

	@Test
	public void testToStringMatrix() {
		SparseMatrix tspm = new SparseMatrix(5, 4);
		tspm.setToValue(4, 3, 5.3);
		tspm.setToValue(4, 1, 5.1);
		tspm.setToValue(4, 0, 5.0);
		tspm.setToValue(1, 3, 0.97);
		assertTrue(tspm.toStringMatrix().equals(
				"0.0\t0.0\t0.0\t0.0\n0.0\t0.0\t0.0\t0.97\n0.0\t0.0\t0.0\t0.0\n0.0\t0.0\t0.0\t0.0\n5.0\t5.1\t0.0\t5.3\n"));
	}

}
