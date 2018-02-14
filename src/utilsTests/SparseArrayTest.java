/*
 * Released under MIT License (Expat)
 * @author Luca Banzato
 * @version 0.1
 */

package utilsTests;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import utils.SparseArray;

class SparseArrayTest {

	@Test
	public void testCreation() {
		SparseArray test = new SparseArray(70);
		assertTrue(test != null);
	}

	@Test
	public void addElement() {
		SparseArray test = new SparseArray(6);
		test.setToValue(1, 0.345);
		assertTrue(Double.compare(test.getValue(1), 0.345) == 0);
		test.setToValue(5, 0.1);
		assertTrue(Double.compare(test.getValue(5), 0.1) == 0);
		assertTrue(Double.compare(test.getValue(0), 0.0) == 0);
		assertTrue(Double.compare(test.getValue(1), 0.0) > 0);
		assertTrue(Double.compare(test.getValue(2), 0.0) == 0);
		assertTrue(Double.compare(test.getValue(3), 0.0) == 0);
		assertTrue(Double.compare(test.getValue(4), 0.0) == 0);
	}

	@Test
	public void cellIsSetToZero() {
		SparseArray test = new SparseArray(6);
		test.setToValue(1, 0.345);
		assertTrue(Double.compare(test.getValue(1), 0.345) == 0);
		test.setToValue(5, 0.1);
		assertTrue(Double.compare(test.getValue(5), 0.1) == 0);
		test.setToZero(5);
		assertTrue(Double.compare(test.getValue(5), 0.0) == 0);
		assertTrue(Double.compare(test.getValue(0), 0.0) == 0);
		assertTrue(Double.compare(test.getValue(1), 0.0) > 0);
		assertTrue(Double.compare(test.getValue(2), 0.0) == 0);
		assertTrue(Double.compare(test.getValue(3), 0.0) == 0);
		assertTrue(Double.compare(test.getValue(4), 0.0) == 0);
		test.setToValue(5, 515);
		assertTrue(Double.compare(test.getValue(5), 515) == 0);
		assertTrue(Double.compare(test.getValue(5), 0.0) != 0);
		test.setToValue(5, 0.0);
		assertTrue(Double.compare(test.getValue(5), 0.0) == 0);
	}

	@Test
	public void testOrder() {
		SparseArray test = new SparseArray(6);
		test.setToValue(1, 0.345);
		assertTrue(Double.compare(test.getValue(1), 0.345) == 0);
		test.setToValue(5, 0.1);
		assertTrue(Double.compare(test.getValue(5), 0.1) == 0);
		assertTrue(test.toString().equals("{[1, 0.345],[5, 0.1]}"));
		test.setToZero(5);
		test.setToZero(1);
		assertTrue(Double.compare(test.getValue(5), 0.0) == 0);
		assertTrue(Double.compare(test.getValue(1), 0.0) == 0);
		test.setToValue(5, 0.1);
		assertTrue(Double.compare(test.getValue(5), 0.1) == 0);
		test.setToValue(1, 0.345);
		assertTrue(Double.compare(test.getValue(1), 0.345) == 0);
		assertTrue(test.toString().equals("{[1, 0.345],[5, 0.1]}"));
	}

}
