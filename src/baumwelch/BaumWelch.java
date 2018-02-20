/*
 * Released under MIT License (Expat)
 * @author Luca Banzato
 * @version 0.1
 */

package baumwelch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import exceptions.IllegalADefinitionException;
import exceptions.IllegalBDefinitionException;
import exceptions.IllegalPiDefinitionException;
import exceptions.IllegalStatesNamesSizeException;
import utils.Couple;
import utils.GaussianCurve;
import utils.Log;
import utils.SparseArray;
import utils.SparseMatrix;

public class BaumWelch {

	// This class implements

	private ContinuousModel currentModel;
	private ArrayList<ObsSequence> sequences;
	private ArrayList<BWContainer> workingBench;
	private double currentLikelihood;
	private Logger logger = Log.getLogger();
	private int round = 0;
	private boolean convergent = false;

	// Constructor without a model already prepared
	public BaumWelch(int nStates, String path, ArrayList<ObsSequence> sequences) throws IllegalPiDefinitionException,
			IllegalADefinitionException, IllegalBDefinitionException, IllegalStatesNamesSizeException {
		currentModel = new ContinuousModel(nStates, path);
		if (sequences != null && sequences.size() > 0) {
			this.sequences = sequences;
		} else {
			throw new IllegalArgumentException("At least one observations sequence must be provided!");
		}
	}

	// Constructor for a model already prepared
	public BaumWelch(ContinuousModel startModel, ArrayList<ObsSequence> sequences) {
		currentModel = startModel;
		if (sequences != null && sequences.size() > 0) {
			this.sequences = sequences;
		} else {
			throw new IllegalArgumentException("At least one observations sequence must be provided!");
		}
	}

	private void initialize() {
		workingBench = new ArrayList<BWContainer>(); // Initialize a new "Working Bench" (ArrayList of BWContainers)
		for (ObsSequence sequence : sequences) {
			workingBench.add(new BWContainer(currentModel.getNumberOfStates(), sequence.size()));
		}
	}

	// Execute the BaumWelch algorithm
	public ContinuousModel step(String outputPath, boolean scaled, boolean debug)
			throws IllegalStatesNamesSizeException, IllegalADefinitionException, IllegalBDefinitionException,
			IllegalPiDefinitionException {
		if (round == 0) {
			initialize();
		}
		double likelihood = 1.0; // Used for model evalutation
		boolean convergent = false; // Cycle-breaker

		if (round == 0) { // Randomize
			currentModel.randomizePi();
			currentModel.randomizeA();
		}
		for (int i = 0; i < sequences.size(); i++) {
			BWContainer container = workingBench.get(i); // Keeps all the A/B/Pi values in an object
			ObsSequence sequence = sequences.get(i);
			if (round == 0) {
				currentModel.randomizeB(sequence.getMean()); // Gaussians randomic values need to be polarized to
																// the set (otherwise this approach produces many 0
																// values)
			}
			logger.log(Level.INFO, "Associated Gaussian: " + Arrays.toString(currentModel.getB()));
			double alpha = Formula.alpha(currentModel, container, sequence, scaled, true);
			likelihood *= alpha; // Updates the likelihood of the sequences created by this model
			container.setAlphaValue(alpha); // Used later for models union
			double beta = Formula.beta(currentModel, container, sequence, scaled, true);

			// BEGIN of Pi re-estimation
			SparseArray pi = currentModel.getPi();
			SparseArray newPi = container.getPi();
			System.out.println("Alpha matrix before pi: " + container.getAlphaMatrix().toString());
			System.out.println("Beta matrix before pi: " + container.getBetaMatrix().toString());
			for (Couple cell : pi) {
				newPi.setToValue(cell.getX(), Formula.gamma(currentModel, container, 0, cell.getX(), debug));
				System.out.println("Value of Pi(" + cell.getX() + ") = "
						+ Formula.gamma(currentModel, container, 0, cell.getX(), false));
			}
			// END of Pi re-estimation

			// BEGIN of A re-estimation
			SparseMatrix a = currentModel.getA();
			// System.out.println("A getted:" + a.toString());
			SparseMatrix newA = container.getA();
			int columnNumber = 0;
			for (SparseArray column : a) {
				for (Couple cell : column) {
					double value = 0.0;
					double numerator = 0.0;
					for (int t = 0; t < sequence.size() - 2; t++) { // From 1 to T-1 (in Rabiner's formula, 40b)
						numerator += Formula.psi(currentModel, container, sequence, columnNumber, cell.getX(), t);
					}
					double denominator = 0.0;
					for (int t = 0; t < sequence.size() - 2; t++) { // From 1 to T-1 (in Rabiner's formula, 40b)
						denominator += Formula.gamma(currentModel, container, t, columnNumber, debug);
					}
					if (Double.compare(denominator, 0.0) == 0) {
						throw new ArithmeticException("Can't divide by zero");
					}
					value = numerator / denominator;
					if (scaled) {
						// logger.log(Level.INFO, "Scaling down value of A(" + columnNumber + ")(" +
						// cell.getX() + ") = " + value);
						// value /= container.getScaledProduct();
					}
					logger.log(Level.INFO, "Value of A(" + columnNumber + ")(" + cell.getX() + ") = " + value);
					newA.setToValue(columnNumber, cell.getX(), value);
				}
				columnNumber++;
			}
			// END of A re-estimation

			// BEGIN of B re-estimation
			GaussianCurve[] b = currentModel.getB();
			GaussianCurve[] newB = container.getB();
			for (int j = 0; j < b.length; j++) {
				double muValue = 0.0;
				double sigmaValue = 0.0;
				double muNumerator = 0.0;
				double sigmaNumerator = 0.0;
				for (int t = 0; t < sequence.size(); t++) {
					muNumerator += Formula.gamma(currentModel, container, t, j, debug) // Mu re-estimation, formula
																						// 53
							* sequence.getObservation(t);
					sigmaNumerator += Formula.gamma(currentModel, container, t, j, debug) // Sigma re-estimation,
																							// formula 54
							* ((sequence.getObservation(t) - b[j].getMu())
									* (sequence.getObservation(t) - b[j].getMu()));
				}
				// common denominator
				double denominator = 0.0;
				for (int t = 0; t < sequence.size(); t++) {
					denominator += Formula.gamma(currentModel, container, t, j, debug);
				}
				if (Double.compare(denominator, 0.0) == 0) {
					throw new ArithmeticException("Can't divide by zero");
				}
				muValue = muNumerator / denominator;
				sigmaValue = sigmaNumerator / denominator;
				newB[j].setMu(muValue);
				newB[j].setSigma(sigmaValue);
			}

			// END of B re-estimation
		}
		if (round > 0) { // likelihood of the test sequence must be evaluated with the previous one
			if (currentLikelihood < likelihood) {
				convergent = true;
			}
		} else { // first likelihood value assigned
			likelihood = currentLikelihood;
		}
		currentModel = mergeModels(workingBench, currentModel.getNumberOfStates(), scaled);
		round++;
		currentModel.writeToFiles(outputPath + "." + round); // Save all the models in files which name is "example.round_number" (e.g. test.26.trans/curves/start)
		System.gc(); // Garbage collection suggested
		return currentModel;
	}

	public ContinuousModel mergeModels(ArrayList<BWContainer> containers, int nStates, boolean scaled)
			throws IllegalStatesNamesSizeException, IllegalADefinitionException, IllegalBDefinitionException,
			IllegalPiDefinitionException {
		if (containers.size() == 0) {
			throw new IllegalArgumentException("There're no containers to merge, something is wrong!");
		}
		SparseArray newPi = null;
		SparseMatrix newA = new SparseMatrix(nStates, nStates);
		GaussianCurve[] newB = new GaussianCurve[nStates];
		for (int i = 0; i < newB.length; i++) {
			newB[i] = new GaussianCurve();
		}
		boolean first = true;
		for (BWContainer container : containers) {
			SparseMatrix a = container.getA();
			GaussianCurve[] b = container.getB();
			double alpha = container.getAlphaValue();
			// System.out.println("!" + containers.size() + " " + weightedSum);

			// BEGIN of PI merging
			if (first) {
				newPi = container.getPi(); // Pi isn't re-estimated, as stated on page 273 of Rabiner's paper
			}
			// END of PI merging

			// BEGIN of A merging
			int columnNumber = 0;
			for (SparseArray column : a) {
				for (Couple cell : column) {
					int x = columnNumber;
					int y = cell.getX();
					logger.log(Level.INFO, cell.getValue() + " " + alpha);
					newA.setToValue(x, y, newA.getValue(x, y) + alpha * a.getValue(x, y));
				}
				columnNumber++;
			}
			// END of A merging

			// BEGIN of B merging
			for (int i = 0; i < b.length; i++) {
				if (first) {
					newB[i].setMu(alpha * b[i].getMu());
					newB[i].setSigma(alpha * b[i].getSigma());
				} else {
					newB[i].setMu(newB[i].getMu() + alpha * b[i].getMu());
					newB[i].setSigma(newB[i].getSigma() + alpha * b[i].getSigma());
				}
			}
			// END of B merging
			first = false;
		}
		return new ContinuousModel(nStates, newA, newB, newPi, currentModel.getStatesNames());

	}

	public boolean isStopSuggested() { // Returns true if the model is convergent or more than a certain number of BW
		// steps has been done (currently 30)
		return (convergent || round > 29);
	}
	
	public int getCurrentRound() { // Returns the current round number
		return round;
	}
	
	public double getCurrentLikelihood() {
		return currentLikelihood;
	}
	
	public ContinuousModel getCurrentModel() {
		return currentModel;
	}
}
