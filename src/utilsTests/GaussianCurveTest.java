/*
 * Released under MIT License (Expat)
 * @author Luca Banzato
 * @version 0.1.8
 */

package utilsTests;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import utils.GaussianCurve;

class GaussianCurveTest {

	@Test
	public void testCreation() {
		GaussianCurve standard = new GaussianCurve();
		assertTrue(standard != null);
		try {
			GaussianCurve custom = new GaussianCurve(4, 7.0);
			assertTrue(custom != null);
		} catch (IllegalArgumentException e) {
			assertFalse(true); // This instruction shouldn't be executed
		}
		try {
			GaussianCurve custom = new GaussianCurve(4, 1.0);
			assertTrue(custom != null);
		} catch (IllegalArgumentException e) {
			assertTrue(false); // This instruction shouldn't be executed
		}
		// NOTE: the above tests are partially faulty, the Rule/ExpectedException
		// statement
		// of JUnit doesn't work for some mysterious reason on my system
	}

	@Test
	public void testStandardValues() {
		GaussianCurve standard = new GaussianCurve();
		assertTrue(standard != null);
		assertTrue(standard.getMu() == 0.0);
		assertTrue(standard.getSigma() == 1.0);
		assertTrue(standard.toString().equals("G(0.0,1.0)"));
		assertFalse(standard.toString().equals("G(0.0,1.1)"));
	}

	@Test
	public void testCustomValues() {
		GaussianCurve custom = new GaussianCurve(4, 7);
		assertTrue(custom.getMu() == 4.0);
		assertTrue(custom.getSigma() == 7.0);
		assertTrue(custom.toString().equals("G(4.0,7.0)"));
		assertFalse(custom.toString().equals("G(0.0,1.1)"));
	}

	@Test
	public void testStandardPi() {
		GaussianCurve standard = new GaussianCurve();
		assertTrue(standard != null);
		assertTrue(Double.compare(standard.fi(3.0), 0.004431848411938008) == 0);
		assertTrue(Double.compare(standard.fi(2.0), 0.05399096651318806) == 0);
	}

	@Test
	public void testCustomPi() {
		GaussianCurve custom = new GaussianCurve(4, 7);
		assertTrue(custom != null);
		assertTrue(Double.compare(custom.getMu(), 4.0) == 0);
		assertTrue(Double.compare(custom.getSigma(), 7.0) == 0);
		assertTrue(Double.compare(custom.fi(2.0), 0.0547123942777446) == 0);
	}

}
