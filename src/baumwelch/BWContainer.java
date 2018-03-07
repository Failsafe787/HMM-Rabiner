/*
 * Released under MIT License (Expat)
 * @author Luca Banzato
 * @version 0.1.8
 */

package baumwelch;

import java.util.logging.Level;
import java.util.logging.Logger;

import utils.Couple;
import utils.GaussianCurve;
import utils.Log;
import utils.SparseArray;
import utils.SparseMatrix;

public class BWContainer {

	// Class used for instantiating objects containing what is
	// required for the Baum-Welch algorithm. Each container provides
	// structures where it is possible to store all the alpha and beta computations,
	// alongside all the partial models structures ( pi, a and b matrices).

	private SparseMatrix alphaMatrix;
	private SparseMatrix betaMatrix;
	private SparseArray pi;
	private SparseMatrix a;
	private GaussianCurve[] b;
	private double[] factors;
	private Couple[] psiStates;
	private double alphaValue = 0.0;
	private boolean isAlphaProcessed = false;
	private Logger logger = Log.getLogger();

	public BWContainer(int nStates, int sequenceSize) { // Builds an set of empty structures in order to save them
		alphaMatrix = new SparseMatrix(sequenceSize, nStates);
		betaMatrix = new SparseMatrix(sequenceSize, nStates);
		pi = new SparseArray(nStates);
		a = new SparseMatrix(nStates, nStates);
		b = new GaussianCurve[nStates];
		for (int i = 0; i < b.length; i++) {
			b[i] = new GaussianCurve();
		}
		factors = new double[sequenceSize];
		for (int i = 0; i < factors.length; i++) {
			factors[i] = 0.0;
		}
		psiStates = new Couple[sequenceSize];
	}

	public SparseMatrix getAlphaMatrix() { // Returns the alpha matrix
		return alphaMatrix;
	}

	public SparseMatrix getBetaMatrix() { // Returns the beta matrix
		return betaMatrix;
	}

	public SparseArray getPi() { // Returns the pi vector
		return pi;
	}

	public SparseMatrix getA() { // Returns the A matrix
		return a;
	}

	public GaussianCurve[] getB() { // Returns the B vector
		return b;
	}

	public double[] getScalingFactors() { // Returns the scaling factors in an array
		return factors;
	}

	public double getScaledProduct() { // Returns the product of all the scaling factors
		double product = 1.0;
		for (double factor : factors) {
			product *= factor;
		}
		return product;
	}

	public void setAlphaValue(double value) { // Set the alpha value with the one provided, used in Formula.alpha()
		alphaValue = value;
		isAlphaProcessed = true;
	}

	public double getAlphaValue() { // Returns the probability of the model to be the generator of a sequence
		if (!isAlphaProcessed) {
			logger.log(Level.WARNING, "Alpha matrix hasn't been computed, -1 is returned!");
			return -1.0; // -1 is returned and a warning issued if alpha hasn't been calculated previously
		}
		return alphaValue;
	}
	
	public Couple[] getPsiArray() { // Returns the most likely states sequence the model has done to produce a sequence (Viterbi)
		return psiStates;
	}

}
