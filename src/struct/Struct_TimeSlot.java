package struct;

import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix1D;
import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix2D;
import cern.colt.matrix.tint.impl.SparseIntMatrix1D;

public class Struct_TimeSlot {

	public double c; // Scaling factor
	public int time; // Observed at this time
	public SparseDoubleMatrix1D alpha; // Forward variable
	public SparseDoubleMatrix1D beta; // Backward variable
	public SparseDoubleMatrix1D gamma; // Gamma
	public SparseDoubleMatrix2D gamma_k; // Gamma, separated for each component of GMM
	public SparseDoubleMatrix1D delta; // Viterbi
	public SparseIntMatrix1D state; // State sequences (Used for Viterbi)
	public SparseDoubleMatrix2D xi; // Xi

}
