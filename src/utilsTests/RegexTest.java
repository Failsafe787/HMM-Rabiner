/*
 * Released under MIT License (Expat)
 * @author Luca Banzato
 * @version 0.1.8
 */

package utilsTests;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class RegexTest {

	@Test
	void test() {
		String test = "State1 State2 0.8";
		String pattern = "([a-zA-Z0-9]+)\\s([a-zA-Z0-9]+)\\s(\\d+(\\.\\d+)*)";
		assertTrue(test.matches(pattern));
		String line = "INIT	Onset	1";
		String multiSpaceLine = line.replaceAll("\t", " "); // Transforms the whole line in format "State1 State2
															// Probability" (removes all unused tabs/spaces)
		String singleSpaceLine = multiSpaceLine.trim().replaceAll("\\s+", " ");
		System.out.println(singleSpaceLine);
		assertTrue(singleSpaceLine.matches(pattern));
	}

}
