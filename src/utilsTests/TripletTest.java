/*
 * Released under MIT License (Expat)
 * @author Luca Banzato
 * @version 0.1
 */

package utilsTests;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import utils.Triplet;

class TripletTest {

	@Test
	public void testCreate() {
		Triplet test = new Triplet(1, 2, 3.0);
		assertTrue(test != null);
	}

	@Test
	public void testGet() {
		Triplet test = new Triplet(0, 0, 1.0);
		assertTrue(test != null);
		assertTrue(test.getX() == 0);
		assertTrue(test.getY() == 0);
		assertTrue(Double.compare(test.getValue(), 1.0) == 0);
	}

	@Test
	public void testSet() {
		Triplet test = new Triplet(0, 0, 1.0);
		assertTrue(test != null);
		assertTrue(test.getX() == 0);
		assertTrue(test.getY() == 0);
		assertTrue(Double.compare(test.getValue(), 1.0) == 0);
		test.setValue(17.5);
		assertTrue(Double.compare(test.getValue(), 17.5) == 0);
	}

	@Test
	public void testToString() {
		Triplet test = new Triplet(0, 0, 1.0);
		assertTrue(test != null);
		assertTrue(test.getX() == 0);
		assertTrue(test.getY() == 0);
		assertTrue(Double.compare(test.getValue(), 1.0) == 0);
		assertTrue(test.toString().equals("[0, 0, 1.0]"));
	}
}
