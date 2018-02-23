/*
 * Released under MIT License (Expat)
 * @author Luca Banzato
 * @version 0.1.8
 */

package baumwelchTests;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import baumwelch.BaumWelch;
import baumwelch.ContinuousModel;
import baumwelch.ObsSequence;
import exceptions.IllegalADefinitionException;
import exceptions.IllegalBDefinitionException;
import exceptions.IllegalPiDefinitionException;
import exceptions.IllegalStatesNamesSizeException;
import samples.heater.Rescale;

class BaumWelchTest {

	@Test
	public void testCreateC1() {
		ContinuousModel cm_test = null;
		try {
			cm_test = new ContinuousModel(5, "C:\\Users\\Luca Banzato\\Desktop\\test2\\heater"); // load files from
																									// test1
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
		assertTrue(cm_test.getNumberOfStates() == 5);
		Double[] values = { 0.0, 6.5, 17.8, 4.4 };
		ObsSequence observation = new ObsSequence(values);
		ArrayList<ObsSequence> al_observations = new ArrayList<ObsSequence>();
		al_observations.add(observation);
		BaumWelch bw_test = new BaumWelch(cm_test, al_observations);
		assertTrue(bw_test != null);
	}

	@Test
	public void testCreateC2() {
		String path = "C:\\Users\\Luca Banzato\\Desktop\\test2\\heater";
		Rescale.run("C:\\Users\\Luca Banzato\\Desktop\\test2\\data.2017_01_05.hst",
				"C:\\Users\\Luca Banzato\\Desktop\\test2\\data.2017_01_05.resc", 4);
		ObsSequence observation = new ObsSequence("C:\\Users\\Luca Banzato\\Desktop\\test2\\data.2017_01_05.resc", 2);
		ArrayList<ObsSequence> al_observations = new ArrayList<ObsSequence>();
		al_observations.add(observation);
		BaumWelch bw_test = null;
		try {
			bw_test = new BaumWelch(5, path, al_observations, true);
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
		assertTrue(bw_test != null);
		try {
			while (!bw_test.isStopSuggested())
				bw_test.step(path, true, true);
		} catch (IllegalStatesNamesSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalADefinitionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBDefinitionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalPiDefinitionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
