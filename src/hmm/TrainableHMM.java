package hmm;

import java.awt.Point;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import com.google.gson.stream.MalformedJsonException;

import cern.colt.list.tdouble.DoubleArrayList;
import cern.colt.list.tint.IntArrayList;
import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix1D;
import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix2D;
import cern.colt.matrix.tint.impl.SparseIntMatrix1D;
import struct.Struct_TimeSlot;
import struct.Struct_ViterbiResults;
import utils.Enumerator;
import utils.Observation;
import utils.SysUtils;
import utils.WeightedNormal;

public class TrainableHMM {

	private String name;
	private SparseDoubleMatrix2D A;
	private WeightedNormal[][] B;
	private SparseDoubleMatrix1D PI;
	private int nStates;
	private Struct_TimeSlot[] timeFrames;
	private SparseDoubleMatrix2D B_pdfs;
	private Enumerator statesNames;

	public TrainableHMM(String name, SparseDoubleMatrix2D A, WeightedNormal[][] B, SparseDoubleMatrix1D PI,
			Enumerator enumeratedStates) {
		this.name = name;
		this.A = A;
		this.B = B;
		this.PI = PI;
		this.statesNames = enumeratedStates;
		this.nStates = enumeratedStates.size();
		this.timeFrames = null;
	}

	public TrainableHMM(File hmmFile) throws IOException {
		readFromFile(hmmFile);
	}

	// Public methods

	/**
	 * Given the observation sequence O = O1 O2 ... OT, returns the probability of
	 * the observation sequence given this model.
	 *
	 * @param sequence an array of Observation (a sequence of observations)
	 * @return a double representing the probability of the observation sequence
	 *         given this model
	 */
	public double likelihood(Observation[] sequence) {
		init(sequence);
		return alpha(sequence);
	}

	/**
	 * Given the observation sequence O = O1 O2 ... OT, returns a couple of results:
	 * 
	 * 1. The most likely sequence of states which led the model to generate the
	 * input observations sequence 2. The probability of choosing the sequence
	 *
	 * @param sequence an array of Observation (a sequence of observations)
	 * @return a struct representing the couple of results described above
	 */
	public Struct_ViterbiResults viterbi(Observation[] sequence) {
		init(sequence);
		return delta(sequence);
	}

	/**
	 * Given the observation sequence O = O1 O2 ... OT, performs Viterbi algoritm
	 * and writes each observation to a file following this format:
	 * 
	 * PROBABILITY = XXX.XX% STATE TIME VALUE STATE TIME VALUE ....
	 *
	 * @param sequence an array of Observation (a sequence of observations)
	 * @param outFile  an object representing a file where data will be written
	 */
	public void viterbiToFile(Observation[] sequence, File outFile) throws IOException {
		if (sequence == null) {
			throw new NullPointerException("Observations are missing!");
		}
		if (outFile == null) {
			throw new IllegalArgumentException("No valid output file object provided!");
		}
		Struct_ViterbiResults result = viterbi(sequence);
		BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
		String[] states = result.statesSequence;
		double probability = result.probability;
		bw.write("Log Probability = " + probability + "\n");
		for (int i = 0; i < sequence.length; i++) {
			bw.write(states[i] + "\t" + sequence[i].time + "\t" + sequence[i].value + "\n");
		}
		bw.close();
	}

	/**
	 * Returns the HMM model name
	 *
	 * @return the HMM model name
	 */
	public String getModelName() {
		return name;
	}

	/**
	 * Change current HMM model name
	 *
	 * @param name the new name of this HMM
	 */
	public void setModelName(String name) {
		this.name = name;
	}

	/**
	 * Returns a String representation of this model, including PI, A and B tables
	 *
	 * @return a String representing this HMM
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("\n\nINITIAL STATE\n\n");
		builder.append(String.format("%-15s %-11s", "State", "Probability") + "\n");
		builder.append(new String(new char[27]).replace("\0", "=") + "\n");
		for (int i = 0; i < PI.size(); i++) { // Write initial state (Pi)
			builder.append(String.format("%-15s %-11s", statesNames.getValueAt(i), PI.getQuick(i)) + "\n");
		}
		builder.append("\n\nTRANSITION MATRIX\n\n");
		builder.append(String.format("%-15s %-15s %-11s", "From", "To", "Probability") + "\n");
		builder.append(new String(new char[43]).replace("\0", "=") + "\n");
		IntArrayList indexi = new IntArrayList(); // rowList = (0,1)
		IntArrayList indexj = new IntArrayList(); // columnList = (2,1)
		DoubleArrayList values = new DoubleArrayList(); // valueList = (8,7)
		// is equal to write get(0,2)==8, get(1,1)==7
		A.getNonZeros(indexi, indexj, values);
		for (int j = 0; j < indexi.size(); j++) {
			builder.append(String.format("%-15s %-15s %-11s", statesNames.getValueAt(indexi.getQuick(j)),
					statesNames.getValueAt(indexj.getQuick(j)), values.getQuick(j)) + "\n");
		}
		builder.append("\n\nEMISSION PDFS\n\n");
		builder.append(String.format("%-15s %-15s %-19s %-6s", "State", "Mean", "St. Deviation", "Weight") + "\n");
		builder.append(new String(new char[54]).replace("\0", "=") + "\n");
		for (int i = 0; i < B.length; i++) {
			for (int j = 0; j < B[i].length; j++) {
				builder.append(String.format("%-15s %-15s %-15s %-6s", statesNames.getValueAt(i), B[i][j].getMean(),
						B[i][j].getStandardDeviation(), B[i][j].getWeight()) + "\n");
			}
		}
		return builder.toString();
	}

	/**
	 * Performs a fixed number of steps of Baum-Welch algorithm on this model
	 *
	 * @param steps       the number of steps of Baum-Welch algorithm to be
	 *                    performed on this HMM
	 * @param learningSet sequences of observations
	 * @throws IOException
	 */

	public void bw_fixed(int steps, Observation[][] learningSet, File... directory) throws IOException {
		for (int step = 0; step < steps; step++) {
			bw_step(learningSet);
			if (directory.length == 1) {
				writeToFile(
						new File(SysUtils.osFilePath(directory[0].getParent(), name + "_partial_" + step + ".hmm")));
			}
		}
	}

	/**
	 * Performs a certain number of steps of Baum-Welch algorithm on this model in
	 * order to maximize the likelihood of the dataset
	 *
	 * @param learningSet sequences of observations
	 * @param pruningSet  sequences of observations
	 * @throws IOException
	 */

	public void bw_adaptive(Observation[][] learningSet, Observation[][] pruningSet, File... directory)
			throws IOException {
		// initial accuracy must be MAX_VALUE, we're reasoning with logarithms here (the
		// more the value is tiny, the bigger its log will be)
		double accuracy = Integer.MAX_VALUE;
		int step = 0; // # of steps

		SparseDoubleMatrix2D old_A;
		WeightedNormal[][] old_B;
		SparseDoubleMatrix1D old_PI;
		while (true && step < 200) {
			old_A = A;
			old_B = B;
			old_PI = PI;
			bw_step(learningSet);
			double newAccuracy = 0.0;
			for (Observation[] sequence : pruningSet) {
				newAccuracy += likelihood(sequence);
			}
			newAccuracy /= pruningSet.length;
			if (accuracy > newAccuracy) {
				accuracy = newAccuracy;
				if (directory.length == 1) {
					writeToFile(new File(
							SysUtils.osFilePath(directory[0].getParent(), name + "_partial_" + step + ".hmm")));
				}
				step++;
			} else {
				A = old_A;
				B = old_B;
				PI = old_PI;
				break;
			}

		}
	}

	/**
	 * Performs a single step of Baum-Welch algorithm on this model
	 *
	 * @param sequences sequences of observations
	 */
	private void bw_step(Observation[][] sequences) {
		if (sequences == null) {
			throw new NullPointerException("Sequences of observations are missing!");
		}
		int N = sequences.length;
		Struct_TimeSlot[][] timeFramesSequences = new Struct_TimeSlot[N][];

		for (int k = 0; k < N; k++) {

			// Initialize data structures
			init(sequences[k]);

			// Alpha and Beta
			alpha(sequences[k]);
			beta(sequences[k]);

			// Gamma
			gamma_k(sequences[k]);

			// Xi
			xi(sequences[k]);

			timeFramesSequences[k] = timeFrames;
		}

		// Reestimation of initial state probabilities (PI)
		reestimatePI(timeFramesSequences, N);

		// Reestimation of transition probabilities (A)
		reestimateA(timeFramesSequences, sequences);

		// Reestimation of distributions parameters (B)
		reestimateB(timeFramesSequences, sequences);

	}

	/**
	 * Given an observation sequence, initialize all the data structures used to
	 * train this HMM model and precomputes all the values of Bj (Ot), due to the
	 * fact this is a weighted summation of the probabilities a set of Normal
	 * Distributions can generate a specific value in a Mixture Model
	 *
	 * @param sequence an array of Observation (a sequence of observations)
	 */
	private void init(Observation[] sequence) {
		int T = sequence.length;
		timeFrames = new Struct_TimeSlot[sequence.length];
		B_pdfs = new SparseDoubleMatrix2D(nStates, sequence.length);
		for (int t = 0; t < T; t++) {
			Struct_TimeSlot timeslot = new Struct_TimeSlot();
			{
				timeslot.alpha = new SparseDoubleMatrix1D(nStates);
				timeslot.beta = new SparseDoubleMatrix1D(nStates);
				timeslot.gamma = new SparseDoubleMatrix1D(nStates);
				timeslot.gamma_k = new SparseDoubleMatrix2D(nStates, nStates);
				timeslot.delta = new SparseDoubleMatrix1D(nStates);
				timeslot.state = new SparseIntMatrix1D(nStates);
				timeslot.xi = new SparseDoubleMatrix2D(nStates, nStates);
				timeslot.c = 0.0;
				timeslot.time = sequence[t].time;
			}
			timeFrames[t] = timeslot;

			// Precompute B (Considering all the Normal Distributions for each state at each
			// time (Formula 49)
			for (int j = 0; j < nStates; j++) {
				double value = 0.0;
				for (int m = 0; m < B[j].length; m++) {
					value += B[j][m].getWeight() * B[j][m].density(sequence[t].value);
				}
				B_pdfs.setQuick(j, t, value);
			}
		}
		B_pdfs.trimToSize();
	}

	// Private methods

	/**
	 * Given a set of sets of Time Slots and the number of sequences, reestimates
	 * the value of PI
	 *
	 * @param timeFramesSequences a matrix of TimeSlot
	 * @param N                   the numer of sequences associated to this model
	 */
	private void reestimatePI(Struct_TimeSlot[][] timeFramesSequences, int N) {
		// No need to reestimate PI, since PI(1) = 1 and PI(i) = 0, i =! 1
		for (int i = 0; i < nStates; i++) {
			PI.setQuick(i, timeFramesSequences[0][0].gamma.getQuick(i));
		}
	}

	/**
	 * Given a set of sets of Time Slots and the sequences, reestimates the values
	 * of A
	 *
	 * @param timeFramesSequences a matrix of TimeSlot
	 * @param sequences           sequences associated to this model
	 */
	private void reestimateA(Struct_TimeSlot[][] timeFramesSequences, Observation[][] sequences) {

		// Implementation of formula 13 (errata)
		for (int i = 0; i < nStates; i++) {
			for (int j = 0; j < nStates; j++) {
				double denominator = 0.0, numerator = 0.0;
				for (int k = 0; k < sequences.length; k++) {
					int T = sequences[k].length;
					for (int t = 0; t < T - 1; t++) {
						numerator += timeFramesSequences[k][t].xi.getQuick(i, j);
						denominator += timeFramesSequences[k][t].gamma.getQuick(i);
					}
				}

				// Set values to 0 if numerator or denominator is == 0:
				// it means it's impossible to be in state Si at
				// time t given the observation sequence and the model
				if (Double.compare(denominator, 0.0) != 0) {
					double result = numerator / denominator;
					if (Double.compare(result, 0.0) != 0) {
						A.setQuick(i, j, result);
					} else {
						A.setQuick(i, j, 0.0);
					}
				} else {
					A.setQuick(i, j, 0.0);
				}
			}
		}
		// deletes all the values == 0 inside the transition
		// matrix, if added during the previous step
		A.trimToSize();
	}

	/**
	 * Given a set of sets of Time Slots and the sequences, reestimates the values
	 * of B
	 *
	 * @param timeFramesSequences a matrix of TimeSlot
	 * @param sequences           sequences associated to this model
	 */
	private void reestimateB(Struct_TimeSlot[][] timeFramesSequences, Observation[][] sequences) {

		// Implementations of formula 52, 53 and 54
		for (int j = 0; j < nStates; j++) {
			for (int k = 0; k < B[j].length; k++) {
				double c_num = 0.0, mu_num = 0.0, sigma_num = 0.0, c_den = 0.0, mu_den = 0.0; // sigma_den == mu_den
				double cjk = 0.0, mujk = 0.0, sigmajk = 0.0;
				for (int nthSequence = 0; nthSequence < sequences.length; nthSequence++) {
					for (int t = 0; t < sequences[nthSequence].length; t++) {
						c_num += timeFramesSequences[nthSequence][t].gamma_k.getQuick(j, k);
						mu_num += timeFramesSequences[nthSequence][t].gamma_k.getQuick(j, k)
								* sequences[nthSequence][t].value;
						sigma_num += timeFramesSequences[nthSequence][t].gamma_k.getQuick(j, k)
								* (sequences[nthSequence][t].value - B[j][k].getMean())
								* (sequences[nthSequence][t].value - B[j][k].getMean());
						for (int m = 0; m < B[j].length; m++) {
							c_den += timeFramesSequences[nthSequence][t].gamma_k.getQuick(j, m);
						}
						mu_den += timeFramesSequences[nthSequence][t].gamma_k.getQuick(j, k);
					}
					cjk += c_num / c_den;
					mujk += mu_num / mu_den;
					sigmajk += sigma_num / mu_den;
				}
				cjk /= sequences.length;
				mujk /= sequences.length;
				sigmajk /= sequences.length;
				sigmajk = Math.sqrt(sigmajk); // Formula 54 computes the variance, but standard deviation is required
				if (Double.compare(sigmajk, 0.0) != 0) {
					B[j][k] = new WeightedNormal(cjk, mujk, sigmajk);
				} else {
					B[j][k] = new WeightedNormal(0.0);
				}
			}
		}
	}

	/**
	 * Solves the first basic problem of HMM. Given the observation sequence O = O1
	 * O2 ... OT, and a model Lambda = (A,B,PI), we efficiently compute P(O|Lambda),
	 * the probability of the observation sequence given the model.
	 *
	 * @param observations an array of Observation (a sequence of observations)
	 * @param bwstruct     a BaumWelch struct containing PI, A, B, etc.
	 * @return a double value representing the probability of the observation
	 *         sequence given the model
	 * @throws NullPointerException if the sequence or the struct are not
	 *                              initialized
	 */
	private double alpha(Observation[] observations) {
		if (observations == null) {
			throw new NullPointerException("Observations are missing!");
		}
		if (timeFrames == null) {
			throw new NullPointerException("Time frames are not initialized!");
		}
		int T = observations.length;
		IntArrayList index;
		DoubleArrayList values;

		// ==========================================================================================================

		// 1. Initialization (Formula 19)
		for (int i = 0; i < nStates; i++) {
			double value = PI.getQuick(i) * B_pdfs.getQuick(i, 0);
			timeFrames[0].alpha.setQuick(i, value);
			timeFrames[0].c += value;
		}

		// Perform scaling only if there's an alpha value != 0.0
		if (Double.compare(timeFrames[0].c, 0.0) != 0) {
			index = new IntArrayList();
			values = new DoubleArrayList();
			timeFrames[0].alpha.getNonZeros(index, values);
			for (int i = 0; i < index.size(); i++) {
				int ef_indexi = index.get(i);
				timeFrames[0].alpha.setQuick(ef_indexi, timeFrames[0].alpha.getQuick(ef_indexi) / timeFrames[0].c);
			}
		}

		// ==========================================================================================================

		// ==========================================================================================================

		// 2. Induction (Formula 20)
		for (int t = 1; t < T; t++) {
			index = new IntArrayList();
			values = new DoubleArrayList();
			timeFrames[t - 1].alpha.getNonZeros(index, values); // Detect what values were non-0 in the
																// previous time frame
			for (int j = 0; j < nStates; j++) {
				double sum = 0.0;
				for (int i = 0; i < index.size(); i++) {
					int ef_indexi = index.get(i); // indexes of states which value is != 0
					sum += timeFrames[t - 1].alpha.getQuick(ef_indexi) * A.getQuick(ef_indexi, j);
				}
				timeFrames[t].alpha.setQuick(j, sum * B_pdfs.getQuick(j, t));
				timeFrames[t].c += timeFrames[t].alpha.getQuick(j); // Scaling factor
			}
			// Perform scaling only if there's an alpha value != 0.0
			if (Double.compare(timeFrames[t].c, 0.0) != 0) {
				index = new IntArrayList();
				values = new DoubleArrayList();
				timeFrames[t].alpha.getNonZeros(index, values);
				for (int i = 0; i < index.size(); i++) {
					int ef_indexi = index.get(i);
					timeFrames[t].alpha.setQuick(ef_indexi, timeFrames[t].alpha.getQuick(ef_indexi) / timeFrames[t].c);
				}
			}
			// Optimize space
			timeFrames[t].alpha.trimToSize();
		}

		// ==========================================================================================================

		// ==========================================================================================================

		// 3. Termination (Formula 103 instead of Formula 21 due to underflow and 102
		// due to overflow)
		// Note: summation is already computed during the other steps?
		double log_c = 0.0;
		for (int t = 0; t < T; t++) {
			log_c += Math.log(timeFrames[t].c);
		}
		return -log_c;

		// ==========================================================================================================
	}

	/**
	 * Given the observation sequence O = Ot+1 Ot+2 ... OT, a state qt = Si and a
	 * model Lambda = (A,B,PI), determines the probability of the partial
	 * observation sequence from t+1 to the end, givem state Si at time t and the
	 * model Lambda.
	 *
	 * @param observations an array of Observation (a sequence of observations)
	 * @param bwstruct     a BaumWelch struct containing PI, A, B, etc.
	 * @throws NullPointerException if the sequence or TimeFrames aren't initialized
	 * 
	 */
	private void beta(Observation[] observations) {

		if (observations == null) {
			throw new NullPointerException("Observations are missing!");
		}
		if (timeFrames == null) {
			throw new NullPointerException("Time frames are not initialized!");
		}
		int T = observations.length;
		IntArrayList index;
		DoubleArrayList values;

		// For beta variables, I use the same scale factors
		// for each time t as I used for alpha variables.

		// ==========================================================================================================

		// 1. Initialization (Formula 24)
		double initBetaValue = 1.0 / timeFrames[T - 1].c;
		for (int i = 0; i < nStates; i++) { // No need to get non-0 values, all the values must be 1 /
											// scaling_factor
			timeFrames[T - 1].beta.setQuick(i, initBetaValue);
			// System.out.println("!" + timeFrames[T-1].beta.getQuick(i));
		}

		// ==========================================================================================================

		// ==========================================================================================================

		// 2. Induction (Formula 25)
		for (int t = T - 2; t >= 0; t--) {
			index = new IntArrayList();
			values = new DoubleArrayList();
			timeFrames[t + 1].beta.getNonZeros(index, values); // get non-0 values obtained before (t+1)
			for (int i = 0; i < nStates; i++) {
				double sum = 0.0;
				for (int j = 0; j < index.size(); j++) {
					int ef_indexj = index.get(j);
					sum += A.getQuick(i, ef_indexj) * B_pdfs.getQuick(ef_indexj, t + 1)
							* timeFrames[t + 1].beta.getQuick(ef_indexj);
				}
				timeFrames[t].beta.setQuick(i, sum / timeFrames[t].c);
			}
			// Optimize space
			timeFrames[t].beta.trimToSize();
		}

		// ==========================================================================================================
	}

	/**
	 * "Solves" the second basic problem of HMM. Given the observation sequence O =
	 * O1 O2 ... OT, and a model Lambda = (A,B,PI), a corresponding state sequence Q
	 * = q1 q2 ... qT is chosen to be the optimal sequence which explains the
	 * observations.
	 *
	 * @param T the maximum time value reached by a sequence
	 * @throws NullPointerException if TimeFrames aren't initialized or alpha/beta
	 *                              aren't computed
	 */
	private void gamma(int T) {
		if (timeFrames == null) {
			throw new NullPointerException("Time frames are not initialized!");
		}
		for (int t = 0; t < T; t++) {
			if (timeFrames[t].alpha == null || timeFrames[t].beta == null)
				throw new NullPointerException("Alpha and Beta aren't set");
			// Formula 27
			double summation = 0.0;
			for (int i = 0; i < nStates; i++) {
				double value = timeFrames[t].alpha.getQuick(i) * timeFrames[t].beta.getQuick(i);
				timeFrames[t].gamma.setQuick(i, value);
				summation += value;
			}
			if (Double.compare(summation, 0.0) != 0) { // summation == 0 means no value to rescale (all the values are
														// zeros)
				for (int i = 0; i < nStates; i++)
					timeFrames[t].gamma.setQuick(i, timeFrames[t].gamma.getQuick(i) / summation);
			}
			// Optimize space
			timeFrames[t].gamma.trimToSize();
		}
	}

	/**
	 * "Solves" the second problem of HMM in presence of a Gaussian Mixture.
	 * 
	 * Given the observation sequence O = O1 O2 ... OT, and a model Lambda =
	 * (A,B,PI), a corresponding state sequence Q = q1 q2 ... qT is chosen to be the
	 * optimal sequence which explains the observations.
	 *
	 * @param sequence an observations sequence
	 * @throws NullPointerException if TimeFrames aren't initialized or alpha/beta
	 *                              aren't computed
	 */
	private void gamma_k(Observation[] sequence) {
		gamma(sequence.length);
		for (int t = 0; t < sequence.length; t++) {
			// Formula 54b
			for (int j = 0; j < nStates; j++) {
				// Computing the multiplier (rightmost part of the formula concerning normal
				// distributions)
				double summation = 0.0;
				for (int k = 0; k < B[j].length; k++) {
					double value = B[j][k].getWeight() * B[j][k].density(sequence[t].value);
					timeFrames[t].gamma_k.setQuick(j, k, value);
					summation += value;
				}
				// Computing the product between the multiplicand and multiplier
				if (Double.compare(summation, 0.0) != 0) { // summation == 0 means no value to rescale (all the values
															// are zeros)
					for (int m = 0; m < B[j].length; m++) {
						timeFrames[t].gamma_k.setQuick(j, m,
								timeFrames[t].gamma.getQuick(j) * (timeFrames[t].gamma_k.getQuick(j, m) / summation));
					}
				}
			}
			// Optimize space
			timeFrames[t].gamma_k.trimToSize();
		}
	}

	/**
	 * Solves the second basic problem of HMM.
	 * 
	 * Given the observation sequence O = O1 O2 ... OT, and a model Lambda =
	 * (A,B,PI), calculates the most likely sequence of hidden states that produced
	 * the sequence provided in input using Viterbi algorithm.
	 *
	 * @param sequence an observations sequence
	 * @return result a couple of results (states sequence and probability)
	 * @throws NullPointerException if the sequence or TimeFrames aren't initialized
	 */
	private Struct_ViterbiResults delta(Observation[] sequence) {
		if (sequence == null) {
			throw new NullPointerException("Observations are missing!");
		}

		if (timeFrames == null) {
			throw new NullPointerException("Time frames are not initialized!");
		}

		// No observations provided
		if (sequence.length == 0) {
			Struct_ViterbiResults result = new Struct_ViterbiResults();
			{
				result.probability = 0.0;
				result.statesSequence = new String[] { "Invalid sequence" };
			}
			return result;
		}

		int T = sequence.length;
		double maxWeight;
		int maxState;

		// 1. Initialization (Formula 105a)
		for (int i = 0; i < nStates; i++) {
			timeFrames[0].delta.setQuick(i, Math.log(PI.getQuick(i)) + Math.log(B_pdfs.getQuick(i, 0)));
		}

		// ==========================================================================================================

		// ==========================================================================================================

		// 2. Induction (Formula 105b)
		for (int t = 1; t < T; t++) {
			for (int j = 0; j < nStates; j++) {
				maxWeight = Double.NEGATIVE_INFINITY;
				maxState = 0;

				// Max value computation
				for (int i = 0; i < nStates; i++) {
					double weight = timeFrames[t - 1].delta.getQuick(i) + Math.log(A.getQuick(i, j));
					if (weight > maxWeight) {
						maxWeight = weight;
						maxState = i;
					}
				}
				timeFrames[t].delta.setQuick(j, maxWeight + Math.log(B_pdfs.getQuick(j, t)));
				timeFrames[t].state.setQuick(j, maxState);
				
			}
		}

		// ==========================================================================================================

		// ==========================================================================================================

		// 3. Termination (Formula 34a / 34b)
		maxWeight = timeFrames[T - 1].delta.getQuick(0);
		maxState = 0;

		for (int k = 1; k < nStates; k++) {
			if (timeFrames[T - 1].delta.getQuick(k) > maxWeight) {
				maxWeight = timeFrames[T - 1].delta.getQuick(k);
				maxState = k;
			}
		}

		// ==========================================================================================================

		// ==========================================================================================================

		// 4. Backtracking (Formula 35)
		int[] path = new int[T];
		path[T - 1] = maxState;

		for (int t = T - 2; t >= 0; t--) {
			path[t] = timeFrames[t + 1].state.getQuick(path[t + 1]);
		}

		// State ID to State Name conversion
		String[] stringPath = new String[T];
		for (int t = 0; t < path.length; t++) {
			stringPath[t] = statesNames.getValueAt(path[t]);
		}

		// ==========================================================================================================

		// ==========================================================================================================

		// Returns the sequence probability and the most likely states sequence for the
		// given observations sequence
		Struct_ViterbiResults result = new Struct_ViterbiResults(); {
			result.probability = maxWeight;
			result.statesSequence = stringPath;
		}
		return result;
	}

	/**
	 * Helps to solve the third basic problem of HMM. Given the observation sequence
	 * O = O1 O2 ... OT, and a model Lambda = (A,B,PI), determines the probability
	 * of being in a state Si at time t, and state Sj at time t+1
	 *
	 * @param t               the current time value
	 * @param nextObservation the value observed at time t+1
	 * @param bwstruct        a BaumWelch struct containing PI, A, B, etc.
	 * @throws NullPointerException if the struct isn't initialized or alpha/beta
	 *                              aren't computed
	 */
	private void xi(Observation[] sequence) {

		if (timeFrames == null) {
			throw new NullPointerException("Time frames are not initialized!");
		}
		for (int t = 0; t < sequence.length - 1; t++) {

			if (timeFrames[t].alpha == null || timeFrames[t].beta == null) {
				throw new NullPointerException("Alpha and Beta aren't set");
			}

			// Formula 10 (HMM errata)
			timeFrames[t].xi = new SparseDoubleMatrix2D(nStates, nStates);
			for (int i = 0; i < nStates; i++) {
				for (int j = 0; j < nStates; j++) {
					double value = timeFrames[t].alpha.getQuick(i) * A.getQuick(i, j) * B_pdfs.getQuick(j, t + 1)
							* timeFrames[t + 1].beta.getQuick(j);
					if (Double.compare(value, 0.0) != 0) {
						timeFrames[t].xi.setQuick(i, j, value);
					}
				}
			}
			timeFrames[t].xi.trimToSize();
			// No need to divide for (P(O|lambda), scaled Alpha and Beta
			// Variables are used instead of normal ones
		}

	}

	/**
	 * Given a File object representing a HMM file, loads the model defined inside
	 * this file
	 *
	 * @param hmmFile a File object representing a HMM file (JSON format)
	 * @throws MalformedJsonException if hmmFile isn't in valid format
	 * @throws IOException            if there's an error while reading this file
	 */
	private void readFromFile(File hmmFile) throws JsonParseException, IOException {

		// Creating temporary structures (let's suppose an exception is raised while
		// reading the model file...)
		Enumerator temporary_statesNames = new Enumerator();
		ArrayList<Double> temporary_PI;
		LinkedHashMap<Point, Double> temporary_A;
		LinkedHashMap<Integer, ArrayList<WeightedNormal>> temporary_B;

		try {
			// Open reader and parse file content
			FileReader br = new FileReader(hmmFile);
			JsonObject jobject = new JsonParser().parse(br).getAsJsonObject();

			// Reading PI
			JsonArray pi_array = jobject.getAsJsonArray("PI");
			temporary_PI = new ArrayList<Double>();
			double pi_prob_value, totalSum = 0.0;
			for (JsonElement pi_element : pi_array) {
				JsonObject pi_object = pi_element.getAsJsonObject();
				temporary_statesNames.add(pi_object.get("state").getAsString());
				pi_prob_value = pi_object.get("probability").getAsDouble();
				if (pi_prob_value < 0.0 || pi_prob_value > 1.0) {
					throw new IllegalArgumentException(
							"The file provided has a bad probability definition! (Probability interval is [0,1])");
				}
				temporary_PI.add(pi_prob_value);
				totalSum += pi_prob_value;
			}
			// Approximation (10e-5)
			totalSum = Math.round(totalSum * 100000) / 100000;
			if (Double.compare(totalSum, 1.0) != 0) {
				throw new IllegalArgumentException(
						"The file provided has a bad probability definition! (Sum must be 1.0)");
			}

			// Reading A
			JsonArray a_array = jobject.getAsJsonArray("A");
			temporary_A = new LinkedHashMap<Point, Double>();
			for (JsonElement a_element : a_array) {
				JsonObject a_object = a_element.getAsJsonObject();
				int x, y;
				double a_prob_value;
				if ((x = temporary_statesNames.getIndex(a_object.get("state").getAsString())) == -1) {
					x = temporary_statesNames.add(a_object.get("state").getAsString());
				}
				if ((y = temporary_statesNames.getIndex(a_object.get("to").getAsString())) == -1) {
					y = temporary_statesNames.add(a_object.get("to").getAsString());
				}
				a_prob_value = a_object.get("probability").getAsDouble();
				if (a_prob_value < 0.0 || a_prob_value > 1.0) {
					throw new IllegalArgumentException(
							"The file provided has a bad probability definition! (Probability interval is [0,1])");
				}
				temporary_A.put(new Point(x, y), a_prob_value);
			}

			// Reading B
			JsonArray b_array = jobject.getAsJsonArray("B");
			temporary_B = new LinkedHashMap<Integer, ArrayList<WeightedNormal>>();
			for (JsonElement b_element : b_array) {
				JsonObject b_object = b_element.getAsJsonObject();
				int x;
				if ((x = temporary_statesNames.getIndex(b_object.get("state").getAsString())) == -1) {
					x = temporary_statesNames.add(b_object.get("state").getAsString());
				}
				ArrayList<WeightedNormal> distributions = new ArrayList<WeightedNormal>();
				JsonArray distributions_array = b_object.getAsJsonArray("distributions");
				double total_weight = 0.0;
				for (JsonElement distribution_element : distributions_array) {
					JsonObject distribution_object = distribution_element.getAsJsonObject();
					double mean = distribution_object.get("mu").getAsDouble();
					double deviation = distribution_object.get("sigma").getAsDouble();
					double weight = distribution_object.get("weight").getAsDouble();
					if (Double.compare(deviation, 0.0) == 0) {
						throw new IllegalArgumentException(
								"The file provided has a bad standard deviation definition! (Standard Deviation must be > 0)");
					}
					if (weight < 0) {
						throw new IllegalArgumentException(
								"The file provided has a bad weight definition! (Weight must be >= 0)");
					}
					total_weight += weight;
					distributions.add(new WeightedNormal(weight, mean, deviation));
				}
				// Approximation (10e-5)
				total_weight = Math.round(total_weight * 100000) / 100000;
				if (Double.compare(total_weight, 0.0) != 0 && Double.compare(total_weight, 1.0) != 0) {
					System.out.println(total_weight);
					throw new IllegalArgumentException(
							"The file provided has a bad weight definition! (Weight sum for gaussians in a state must be 1.0 (or 0.0 if no emissions)");
				}
				temporary_B.put(x, distributions);
			}
		} catch (IllegalStateException ise) {
			throw new IllegalArgumentException("The file provided isn't in the correct format (JSON syntax error)");
		} catch (Exception e) {
			throw new IllegalArgumentException(e.getMessage());
		}

		// Updating values
		statesNames = temporary_statesNames;
		nStates = statesNames.size();
		name = hmmFile.getName().replaceFirst("[.][^.]+$", "");
		PI = new SparseDoubleMatrix1D(temporary_PI.stream().filter(i -> i != null).mapToDouble(i -> i).toArray());
		A = new SparseDoubleMatrix2D(nStates, nStates);
		for (Entry<Point, Double> element : temporary_A.entrySet()) {
			Point coords = element.getKey();
			double value = element.getValue();
			A.setQuick(coords.x, coords.y, value);
		}
		B = new WeightedNormal[nStates][];
		for (int i = 0; i < B.length; i++) {
			B[i] = new WeightedNormal[] { new WeightedNormal(0.0) };
		}
		for (Entry<Integer, ArrayList<WeightedNormal>> element : temporary_B.entrySet()) {
			int state = element.getKey();
			ArrayList<WeightedNormal> distributions = element.getValue();
			B[state] = new WeightedNormal[distributions.size()];
			for (int i = 0; i < distributions.size(); i++) {
				B[state][i] = distributions.get(i);
			}
		}
	}

	/**
	 * Given a File object representing a HMM file, writes the current model to this
	 * file
	 *
	 * @param hmmFile a File object representing a HMM file
	 * @throws IOException if there's an error while writing this file
	 */
	public void writeToFile(File hmmFile) throws IOException {

		// Initialize containers for Sparse Matrixes
		IntArrayList indexList_x = new IntArrayList();
		IntArrayList indexList_y = new IntArrayList();
		DoubleArrayList valueList = new DoubleArrayList();

		// Open writer and begin JSON object
		JsonWriter writer = new JsonWriter(new FileWriter(hmmFile));
		writer.beginObject();

		// Write Initial State Probabilities
		writer.name("PI");
		writer.beginArray();
		PI.getNonZeros(indexList_x, valueList);
		for (int i = 0; i < indexList_x.size(); i++) {
			writer.beginObject();
			writer.name("state").value(statesNames.getValueAt(indexList_x.getQuick(i)));
			try {
				writer.name("probability").value(valueList.getQuick(i));
			} catch (IllegalArgumentException e) {
				writer.value("NaN");
			}
			writer.endObject();
		}
		writer.endArray();

		// Write Transition Probabilities
		writer.name("A");
		writer.beginArray();
		A.getNonZeros(indexList_x, indexList_y, valueList);
		for (int i = 0; i < indexList_x.size(); i++) {
			writer.beginObject();
			writer.name("state").value(statesNames.getValueAt(indexList_x.getQuick(i)));
			writer.name("to").value(statesNames.getValueAt(indexList_y.getQuick(i)));
			try {
				writer.name("probability").value(valueList.getQuick(i));
			} catch (IllegalArgumentException e) {
				writer.value("NaN");
			}
			writer.endObject();
		}
		writer.endArray();

		// Write Weighted Normal Distributions
		writer.name("B");
		writer.beginArray();
		for (int i = 0; i < B.length; i++) {
			writer.beginObject();
			writer.name("state").value(statesNames.getValueAt(i));
			writer.name("distributions").beginArray();
			for (WeightedNormal distribution : B[i]) {
				writer.beginObject();
				try {
					writer.name("mu").value(distribution.getMean());
				} catch (IllegalArgumentException e) {
					writer.value("NaN");
				}
				try {
					writer.name("sigma").value(distribution.getStandardDeviation());
				} catch (IllegalArgumentException e) {
					writer.value("NaN");
				}
				try {
					writer.name("weight").value(distribution.getWeight());
				} catch (IllegalArgumentException e) {
					writer.value("NaN");
				}
				writer.endObject();
			}
			writer.endArray();
			writer.endObject();
		}
		writer.endArray();

		// End object and close writer
		writer.endObject();
		writer.close();
	}
}
