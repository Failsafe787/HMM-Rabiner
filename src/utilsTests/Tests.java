/*
 * Released under MIT License (Expat)
 * @author Luca Banzato
 * @version 0.1
 */

package utilsTests;

import java.util.ArrayList;

import utils.GaussianCurve;

public class Tests {
	
	// Draft test class for stupid matters

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
		int[] values = {0,1,2,3,4};
		for(int value:values) {
			System.out.println(value);
		}
	}
}
