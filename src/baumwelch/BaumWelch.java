/*
 * Released under MIT License (Expat)
 * @author Luca Banzato
 * @version 0.1
 */

package baumwelch;

import java.util.ArrayList;

import exceptions.IllegalADefinitionException;
import exceptions.IllegalBDefinitionException;
import exceptions.IllegalPiDefinitionException;
import exceptions.IllegalStatesNamesSizeException;
import utils.Couple;
import utils.GaussianCurve;
import utils.SparseArray;
import utils.SparseMatrix;
import utils.Triplet;

public class BaumWelch {

	// This class implements

	private ContinuousModel currentModel;
	private ArrayList<ObsSequence> sequences;
	private ArrayList<BWContainer> workingBench;
	private double currentLikelihood;

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
		this.sequences = sequences;
	}

	// Execute the BaumWelch algorithm
	public ContinuousModel execute(String outputPath, boolean debug) throws IllegalStatesNamesSizeException,
			IllegalADefinitionException, IllegalBDefinitionException, IllegalPiDefinitionException {
		workingBench = new ArrayList<BWContainer>(); // Initialize a new "Working Bench" (ArrayList of BWContainers)
		for (ObsSequence sequence : sequences) {
			workingBench.add(new BWContainer(currentModel.getNumberOfStates(), sequence.size()));
		}
		int round = 0;
		double likelihood = 1.0; // Used for model evalutation
		boolean convergent = false; // Cycle-breaker
		do { // START of Baum-Welch iterations

			if (round == 0) { // Randomize
				currentModel.randomize();
			}
			for (int i = 0; i < sequences.size(); i++) {
				BWContainer container = workingBench.get(i); // Keeps all the A/B/Pi values in an object
				ObsSequence sequence = sequences.get(i);
				System.out.println("Associated Gaussian: " + currentModel.getB()[i].toString());
				double alpha = Formula.alpha(currentModel, container, sequence, true, debug);
				likelihood *= alpha; // Updates the likelihood of the sequences created by this model
				container.setAlphaValue(alpha); // Used later for models union
				double beta = Formula.beta(currentModel, container, sequence, true, debug);

				// BEGIN of Pi re-estimation
				SparseArray pi = currentModel.getPi();
				SparseArray newPi = container.getPi();
				for (Couple cell : pi) {
					newPi.setToValue(cell.getX(), Formula.gamma(currentModel, container, cell.getX(), 0));
				}
				// END of Pi re-estimation

				// BEGIN of A re-estimation
				SparseMatrix a = currentModel.getA();
				SparseMatrix newA = container.getA();
				for (Triplet cell : a) {
					double value = 0.0;
					double numerator = 0.0;
					for (int t = 0; t < sequence.size() - 1; t++) {
						numerator += Formula.psi(currentModel, container, sequence, cell.getX(), cell.getY(), t);
					}
					if (Double.compare(numerator, 0.0) != 0) {
						double denominator = 0.0;
						for (int t = 0; t < sequence.size() - 1; t++) {
							numerator += Formula.gamma(currentModel, container, cell.getX(), t);
						}
						value = numerator / denominator;
					}
					newA.setToValue(cell.getX(), cell.getY(), value);
				}

				// END of A re-estimation

				// BEGIN of B re-estimation
				GaussianCurve[] b = currentModel.getB();
				GaussianCurve[] newB = container.getB();
				for (int k = 0; k < b.length; k++) {
					double muValue = 0.0;
					double sigmaValue = 0.0;
					double muNumerator = 0.0;
					double sigmaNumerator = 0.0;
					for (int t = 0; t < sequence.size(); t++) {
						muNumerator += Formula.gamma(currentModel, container, k, t) // Mu
																					// re-estimation
								* sequence.getObservation(t);
						sigmaNumerator += Formula.gamma(currentModel, container, k, t) // Sigma
																						// re-estimation
								* ((sequence.getObservation(t) - b[k].getMu())
										* (sequence.getObservation(t) - b[k].getMu()));
					}
					if (Double.compare(muNumerator, 0.0) != 0 || Double.compare(sigmaNumerator, 0.0) != 0) { // Common
																												// denominator
						double denominator = 0.0;
						for (int t = 0; t < sequence.size(); t++) {
							muNumerator += Formula.gamma(currentModel, container, k, t);
						}
						muValue = muNumerator / denominator;
						sigmaValue = sigmaNumerator / denominator;
					}
					newB[k].setMu(muValue);
					newB[k].setSigma(sigmaValue);
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
			if (!convergent) {
				currentModel = mergeModels(workingBench, currentModel.getNumberOfStates());
			}
			round++;
			currentModel.writeToFiles(outputPath + "." + round);
		} while (!convergent && round < 31);
		System.gc();
		return currentModel;
	}

	public ContinuousModel mergeModels(ArrayList<BWContainer> containers, int nStates)
			throws IllegalStatesNamesSizeException, IllegalADefinitionException, IllegalBDefinitionException,
			IllegalPiDefinitionException {
		SparseMatrix newA = new SparseMatrix(nStates, nStates);
		GaussianCurve[] newB = new GaussianCurve[nStates];
		for (int i = 0; i < newB.length; i++) {
			newB[i] = new GaussianCurve();
		}
		double weightedSum = 0.0;
		boolean first = true;
		for (BWContainer container : containers) {
			SparseMatrix a = container.getA();
			GaussianCurve[] b = container.getB();
			double alpha = container.getAlphaValue();
			weightedSum += alpha;

			// BEGIN of A merging
			for (Triplet cell : a) {
				int x = cell.getX();
				int y = cell.getY();
				newA.setToValue(x, y, cell.getValue() + alpha * a.getValue(x, y));
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
		}
		// A merging
		for (Triplet cell : newA) {
			cell.setValue(cell.getValue() / weightedSum);
		}
		// END of A merging

		for (int i = 0; i < newB.length; i++) {
			newB[i].setMu(newB[i].getMu() / weightedSum);
			newB[i].setSigma(newB[i].getSigma() / weightedSum);
		}

		return new ContinuousModel(nStates, newA, newB, currentModel.getPi(), currentModel.getStatesNames());

	}
}
