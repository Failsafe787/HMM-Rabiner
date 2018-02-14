/*
 * Released under MIT License (Expat)
 * @author Luca Banzato
 * @version 0.1
 */

package baumwelchTests;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import baumwelch.BWContainer;
import baumwelch.ContinuousModel;
import baumwelch.Formula;
import baumwelch.ObsSequence;
import exceptions.IllegalADefinitionException;
import exceptions.IllegalBDefinitionException;
import exceptions.IllegalPiDefinitionException;
import exceptions.IllegalStatesNamesSizeException;
import utils.TemporalSparseMatrix;

class FormulaTest {

	@Test
	public void alphaTest() {
		ContinuousModel test = null;
		try {
			test = new ContinuousModel(2, "C:\\Users\\Luca Banzato\\Desktop\\balls"); // load files from test1
		} catch (IllegalPiDefinitionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalADefinitionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBDefinitionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStatesNamesSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertTrue(test.getNumberOfStates() == 2);
		Double[] observations = { 0.384100, 3.022968, 1.920920, 0.384180 };
		ObsSequence sequence = new ObsSequence(observations);
		BWContainer container = new BWContainer(test.getNumberOfStates(), sequence.size());
		double alphaValue = Formula.alpha(test, container, sequence, true, true);
		TemporalSparseMatrix alphaMatrix = container.getAlphaMatrix();
		assertTrue(Double.compare(alphaValue, 1.334122388674474E-4) == 0);
		System.out.print(alphaMatrix.toStringMatrix());
		assertTrue(Double.compare(alphaMatrix.get(0, 0), 0.18528647858278147) == 0);
		assertTrue(Double.compare(alphaMatrix.get(0, 1), 0.026574007360815385) == 0);
		assertTrue(Double.compare(alphaMatrix.get(1, 0), 1.0959873053370272E-4) == 0);
		assertTrue(Double.compare(alphaMatrix.get(1, 1), 0.012074235774726142) == 0);
		assertTrue(Double.compare(alphaMatrix.get(2, 0), 2.2905729047711728E-4) == 0);
		assertTrue(Double.compare(alphaMatrix.get(2, 1), 6.861764822292593E-4) == 0);
		assertTrue(Double.compare(alphaMatrix.get(3, 0), 8.476917309795065E-5) == 0);
		assertTrue(Double.compare(alphaMatrix.get(3, 1), 4.8643065769496764E-5) == 0);
	}

	@Test
	public void betaTest() {
		ContinuousModel test = null;
		try {
			test = new ContinuousModel(2, "C:\\Users\\Luca Banzato\\Desktop\\test2\\balls"); // load files from test1
		} catch (IllegalPiDefinitionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalADefinitionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBDefinitionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStatesNamesSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertTrue(test.getNumberOfStates() == 2);
		Double[] observations = { 0.384100, 3.022968, 1.920920, 0.384180 };
		ObsSequence sequence = new ObsSequence(observations);
		BWContainer container = new BWContainer(test.getNumberOfStates(), sequence.size());
		double alphaValue = Formula.alpha(test, container, sequence, true, true);
		double betaValue = Formula.beta(test, container, sequence, true, true);
		TemporalSparseMatrix betaMatrix = container.getBetaMatrix();
		System.out.print(betaMatrix.toStringMatrix());
		assertTrue(Double.compare(betaMatrix.get(3, 0), 1.0) == 0);
		assertTrue(Double.compare(betaMatrix.get(3, 1), 1.0) == 0);
		assertTrue(Double.compare(betaMatrix.get(2, 0), 0.08488957445448916) == 0);
		assertTrue(Double.compare(betaMatrix.get(2, 1), 0.14837223995102172) == 0);
		assertTrue(Double.compare(betaMatrix.get(1, 0), 0.008055691879161754) == 0);
		assertTrue(Double.compare(betaMatrix.get(1, 1), 0.007454842452420935) == 0);
		assertTrue(Double.compare(betaMatrix.get(0, 0), 3.8570760353243675E-4) == 0);
		assertTrue(Double.compare(betaMatrix.get(0, 1), 3.073982852043911E-4) == 0);
		assertTrue(Double.compare(betaValue, 6.931058887368279E-4) == 0);
	}

	@Test
	public void gammaTest() {
		ContinuousModel test = null;
		try {
			test = new ContinuousModel(2, "C:\\Users\\Luca Banzato\\Desktop\\test2\\balls"); // load files from test1
		} catch (IllegalPiDefinitionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalADefinitionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBDefinitionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStatesNamesSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertTrue(test.getNumberOfStates() == 2);
		Double[] observations = { 0.384100, 3.022968, 1.920920, 0.384180 };
		ObsSequence sequence = new ObsSequence(observations);
		BWContainer container = new BWContainer(test.getNumberOfStates(), sequence.size());
		double alphaValue = Formula.alpha(test, container, sequence, false, false);
		double betaValue = Formula.beta(test, container, sequence, false, false);
		double gammaValue = Formula.gamma(test, container, 0, 3);
		double psiValue = Formula.psi(test, container, sequence, 0, 1, 2);
		assertTrue(Double.compare(alphaValue, 1.334122388674474E-4) == 0);
		assertTrue(Double.compare(betaValue, 6.931058887368279E-4) == 0);
		assertTrue(Double.compare(gammaValue, 0.6353927781856177) == 0);
		assertTrue(Double.compare(psiValue, 0.0903605851351128) == 0);
	}
}
