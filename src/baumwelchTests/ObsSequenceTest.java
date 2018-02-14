/*
 * Released under MIT License (Expat)
 * @author Luca Banzato
 * @version 0.1
 */

package baumwelchTests;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import baumwelch.ObsSequence;

public class ObsSequenceTest {

	@Test
	public void testCreation() {
		ObsSequence observations = new ObsSequence();
		assertTrue(observations != null);
	}

	@Test
	public void emptySequence() {
		ObsSequence observations = new ObsSequence();
		observations.addObservation(6);
		observations.addObservation(3);
		assertTrue(Double.compare(observations.getObservation(0), 6.0) == 0
				&& Double.compare(observations.getObservation(1), 3) == 0);
		assertFalse(observations.removeObservation(2));
		assertTrue(observations.removeObservation(0));
		assertTrue(Double.compare(observations.getObservation(0), 3.0) == 0);
	}

	@Test
	public void arraySequence() {
		Double[] values = { 0.0, 6.5, 17.8, 4.4 };
		ObsSequence observations = new ObsSequence(values);
		assertTrue(Double.compare(observations.getObservation(0), 0.0) == 0);
		assertTrue(Double.compare(observations.getObservation(1), 6.5) == 0);
		assertTrue(Double.compare(observations.getObservation(2), 17.8) == 0);
		assertTrue(Double.compare(observations.getObservation(3), 4.4) == 0);
		Double[] values2 = { 6.0 };
		ObsSequence observations2 = new ObsSequence(values2);
		assertTrue(Double.compare(observations2.getObservation(0), 6.0) == 0);
	}

	@Test
	public void fileSequenceVAS() {
		ObsSequence observations = new ObsSequence(
				"C:\\Users\\Luca Banzato\\Desktop\\Università\\ERC - Giordana\\Baum-Welch\\obs.txt",
				ObsSequence.VALUEANDSPACE);
		assertTrue(Double.compare(observations.getObservation(0), 0.214) == 0);
		assertTrue(Double.compare(observations.getObservation(1), 0.141) == 0);
		assertTrue(Double.compare(observations.getObservation(2), 1.55) == 0);
		assertTrue(Double.compare(observations.getObservation(3), 7.0) == 0);
		assertTrue(Double.compare(observations.getObservation(4), 4.02) == 0);
	}

	@Test
	public void fileSequenceVPL() {
		ObsSequence observations = new ObsSequence(
				"C:\\Users\\Luca Banzato\\Desktop\\Università\\ERC - Giordana\\Baum-Welch\\obs2.txt",
				ObsSequence.VALUEPERLINE);
		assertTrue(Double.compare(observations.getObservation(0), 0.214) == 0);
		assertTrue(Double.compare(observations.getObservation(1), 0.141) == 0);
		assertTrue(Double.compare(observations.getObservation(2), 1.55) == 0);
		assertTrue(Double.compare(observations.getObservation(3), 7) == 0);
		assertTrue(Double.compare(observations.getObservation(4), 4.02) == 0);
		assertTrue(Double.compare(observations.getObservation(5), 1.0) == 0);
	}

}
