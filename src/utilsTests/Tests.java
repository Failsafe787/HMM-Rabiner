/*
 * Released under MIT License (Expat)
 * @author Luca Banzato
 * @version 0.1
 */

package utilsTests;

import java.util.ArrayList;

import utils.GaussianCurve;

public class Tests {

	public static void main(String[] argv) {
		ArrayList<Integer> val = new ArrayList<Integer>();
		val.add(2);
		System.out.println(val);
		GaussianCurve test = new GaussianCurve();
		ArrayList<GaussianCurve> a = new ArrayList<GaussianCurve>();
		ArrayList<GaussianCurve> b = new ArrayList<GaussianCurve>();
		a.add(test);
		b.add(test);
		test.setMu(5.0);
		System.out.println(a.get(0).getMu());
		System.out.println(b.get(0).getMu());
	}
}
