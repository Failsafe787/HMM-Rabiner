package utilsTests;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import utils.SparseMatrix;

public class SparseMatrixTest {

	@Test
	public void testCreation() {
		SparseMatrix test = new SparseMatrix(35, 20);
		assertTrue(test != null);
	}

	@Test
	public void addCellValue() {
		SparseMatrix test = new SparseMatrix(20, 20);
		test.setToValue(1, 0, 0.345);
		assertTrue(Double.compare(test.getValue(1, 0), 0.345) == 0);
		test.setToValue(5, 17, 0.1);
		assertTrue(Double.compare(test.getValue(5, 17), 0.1) == 0);
	}

	@Test
	public void cellValueIsZero() {
		SparseMatrix test = new SparseMatrix(20, 20);
		test.setToValue(1, 0, 0.345);
		assertTrue(Double.compare(test.getValue(1, 0), 0.345) == 0);
		test.setToValue(5, 17, 0.1);
		assertTrue(Double.compare(test.getValue(5, 17), 0.1) == 0);
		assertTrue(Double.compare(test.getValue(5, 4), 0.0) == 0);
	}

	@Test
	public void cellIsSetToZero() {
		SparseMatrix test = new SparseMatrix(20, 20);
		test.setToValue(1, 0, 0.345);
		assertTrue(Double.compare(test.getValue(1, 0), 0.345) == 0);
		test.setToValue(5, 17, 0.1);
		assertTrue(Double.compare(test.getValue(5, 17), 0.1) == 0);
		test.setToZero(5, 17);
		assertTrue(Double.compare(test.getValue(5, 17), 0.0) == 0);
		test.setToValue(5, 17, 515);
		assertTrue(Double.compare(test.getValue(5, 17), 515) == 0);
		assertTrue(Double.compare(test.getValue(5, 17), 0.0) != 0);
		test.setToValue(5, 17, 0.0);
		assertTrue(Double.compare(test.getValue(5, 17), 0.0) == 0);
	}
	

	@Test
	public void testOrder() {
		SparseMatrix test = new SparseMatrix(20, 20);
		test.setToValue(1, 0, 0.345);
		assertTrue(Double.compare(test.getValue(1, 0), 0.345) == 0);
		test.setToValue(5, 17, 0.1);
		assertTrue(Double.compare(test.getValue(5, 17), 0.1) == 0);
		assertTrue(test.toString().equals("{[1, 0, 0.345],[5, 17, 0.1]}"));
		test.setToZero(5, 17);
		test.setToZero(1, 0);
		test.setToValue(5, 17, 0.1);
		assertTrue(Double.compare(test.getValue(5, 17), 0.1) == 0);
		test.setToValue(1, 0, 0.345);
		assertTrue(Double.compare(test.getValue(1, 0), 0.345) == 0);
		assertTrue(test.toString().equals("{[1, 0, 0.345],[5, 17, 0.1]}"));
	}

}
