package baumwelch;

import utils.GaussianCurve;
import utils.SparseArray;
import utils.SparseMatrix;
import utils.TemporalSparseMatrix;

public class BWContainer {

	// Class used for instantiating objects containing what is
	// required for the Baum-Welch algorithm. Each container provides
	// structures where it is possible to store all the alpha and beta computations,
	// alongside all the partial models structures ( pi, a and b matrices). 
	// Everything is dependent from the observed sequence, which must be provided
	
	private TemporalSparseMatrix alphaMatrix;
	private TemporalSparseMatrix betaMatrix;
	private SparseArray pi;
	private SparseMatrix a;
	private GaussianCurve[] b;
	private double alphaValue = 0.0;
	
	
	public BWContainer(int nStates, int sequenceSize) { // Builds an set of empty structures in order to save them
		alphaMatrix = new TemporalSparseMatrix(sequenceSize,nStates);
		betaMatrix = new TemporalSparseMatrix(sequenceSize,nStates);
		pi = new SparseArray(nStates);
		a = new SparseMatrix(nStates,nStates);
		b = new GaussianCurve[nStates];
		for(int i=0; i<b.length; i++) {
			b[i] = new GaussianCurve();
		}
	}
	
	public TemporalSparseMatrix getAlphaMatrix() {
		return alphaMatrix;
	}
	
	public TemporalSparseMatrix getBetaMatrix() {
		return betaMatrix;
	}
	
	public SparseArray getPi() {
		return pi;
	}
	
	public SparseMatrix getA() {
		return a;
	}
	
	public GaussianCurve[] getB() {
		return b;
	}
	
	public void setAlphaValue(double value) {
		alphaValue = value;
	}
	
	public double getAlphaValue() {
		return alphaValue;
	}
	
}
