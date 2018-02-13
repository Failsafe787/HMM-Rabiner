package baumwelchTests;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import baumwelch.ContinuousModel;
import exceptions.IllegalADefinitionException;
import exceptions.IllegalBDefinitionException;
import exceptions.IllegalPiDefinitionException;
import exceptions.IllegalStatesNamesSizeException;
import utils.GaussianCurve;
import utils.SparseMatrix;
import utils.SparseArray;

class ContinuousModelTest {

//	@Test
//	public void readFromFileTest() {
//		Model test = null;
//		try {
//			test = new Model(5,"C:\\Users\\Luca Banzato\\Desktop\\phone");
//		} catch (IllegalPiDefinitionException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IllegalADefinitionException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IllegalBDefinitionException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		SparseMatrix a = test.getA();
//		GaussianCurve[] b = test.getB();
//		ArrayList<String> state = test.getStatesNames();
//		SparseArray pi = test.getPi();
//		System.out.println(a.toString());
//		System.out.println(b.toString());
//		System.out.println(pi.toString());
//		System.out.println(state.toString());
//		test.writeToFiles("C:\\Users\\Luca Banzato\\Desktop\\t2\\phone");
//	}
	
	@Test
	public void testCreateAndPrint() {
		ContinuousModel test = null;
		try {
			test = new ContinuousModel(2,"C:\\Users\\Luca Banzato\\Desktop\\balls");
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
		SparseMatrix a = test.getA();
		GaussianCurve[] b = test.getB();
		ArrayList<String> state = test.getStatesNames();
		SparseArray pi = test.getPi();
		assertTrue(test.getNumberOfStates() ==2);
		assertTrue(a.toString().equals("{[0, 0, 0.1],[0, 1, 0.9],[1, 0, 0.3],[1, 1, 0.7]}"));
		System.out.println(a.toString());
		System.out.println(Arrays.toString(b));
		assertTrue(Arrays.toString(b).equals("[G(0.0,1.0), G(3.0,7.0)]"));
		System.out.println(pi.toString());
		assertTrue(pi.toString().equals("{[0, 0.5],[1, 0.5]}"));
		System.out.println(state.toString());
		assertTrue(state.toString().equals("[A, B]"));
		test.writeToFiles("C:\\Users\\Luca Banzato\\Desktop\\t2\\phone");
	}

}
