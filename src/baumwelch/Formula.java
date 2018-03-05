/*
 * Released under MIT License (Expat)
 * @author Luca Banzato
 * @version 0.1.8
 */

package baumwelch;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import utils.Couple;
import utils.GaussianCurve;
import utils.Log;
import utils.SparseMatrix;
import utils.SparseArray;

public class Formula {

	private static Logger logger = Log.getLogger();

	public static void alpha(ContinuousModel model, BWContainer container, ObsSequence sequence, boolean scaled,
			boolean debug) {
		if (debug) {
			logger.log(Level.INFO, "\n\n[Initialization of alpha]");
		}
		alphaInitialization(model, container, sequence, scaled, debug);
		if (debug) {
			logger.log(Level.INFO, "\n\n[Induction of alpha]");
		}
		alphaInduction(model,container,sequence,scaled,debug);
		if (debug) {
			logger.log(Level.INFO, "\n\n[Termination of alpha]");
		}
		alphaTermination(container,sequence.size(),scaled,debug);
		
	}

	private static void alphaInitialization(ContinuousModel model, BWContainer container, ObsSequence sequence,
			boolean scaled, boolean debug) {
		SparseArray pi = model.getPi();
		GaussianCurve[] b = model.getB();
		SparseMatrix alphaMatrix = container.getAlphaMatrix();
		double factor = 0.0;
		double[] factors = null;
		ArrayList<String> statesNames = null;
		if (debug) {
			statesNames = model.getStatesNames();
		}
		for (Couple cell : pi) { // Initialization - Implementation of formula 19
			int state = cell.getX();
			double pi1 = cell.getValue();
			double bi1 = b[state].fi(sequence.getObservation(0));
			alphaMatrix.setToValue(0, state, pi1 * bi1);
			if (scaled) {
				factor += pi1 * bi1;
			}
			if (debug) {
				logger.log(Level.INFO,
						"alpha(0)(" + statesNames.get(state) + ") = " + pi1 + " x " + bi1 + " = " + pi1 * bi1);
			}
		}

		if (scaled) { // Scale all the values inside the 1st column of the alpha matrix
			factors = container.getScalingFactors();
			factors[0] = 1 / factor;
			SparseArray currentColumn = alphaMatrix.getColumn(0);
			for (Couple cell : currentColumn) { // Initialization
				int state = cell.getX();
				cell.setValue(cell.getValue() * factors[0]);
				if (debug) {
					logger.log(Level.INFO, "alpha(0)(" + statesNames.get(state) + ") scaled by a factor C = "
							+ factors[0] + " --> " + cell.getValue());
				}
			}
		}
	}

	private static void alphaInduction(ContinuousModel model, BWContainer container, ObsSequence sequence, boolean scaled,
			boolean debug) {
		SparseMatrix a = model.getA();
		GaussianCurve[] b = model.getB();
		SparseMatrix alphaMatrix = container.getAlphaMatrix();
		int time = sequence.size();
		int numberOfStates = model.getNumberOfStates();
		double[] factors = null;
		if(scaled) {
			factors = container.getScalingFactors();
		}
		ArrayList<String> statesNames = null;
		if (debug) {
			statesNames = model.getStatesNames();
		}
		for (int t = 1; t < time; t++) { // Induction - Implementation of formula 20
			SparseArray previousColumn = alphaMatrix.getColumn(t - 1);
			double alphaInduction = 0.0;
			double factor = 0.0;
			for (int j = 0; j < numberOfStates; j++) {
				StringBuilder summatory = null;
				if (debug) {
					summatory = new StringBuilder("");
					summatory.append("alpha(" + t + ")(" + statesNames.get(j) + ") = ");
					summatory.append("(");
				}
				for (Couple cell : previousColumn) {
					alphaInduction += cell.getValue() * a.getValue(cell.getX(), j);
					if (debug) {
						summatory.append("alpha(" + (t - 1) + ")(" + statesNames.get(cell.getX()) + ") ["
								+ cell.getValue() + "] x " + a.getValue(cell.getX(), j) + " + ");
					}
				}
				if (debug) {
					if (previousColumn.effectiveLength() == 0) {
						summatory.append("0.0");
					}
					if (summatory.charAt(summatory.length() - 2) == '+') {
						summatory.deleteCharAt(summatory.length() - 1);
						summatory.deleteCharAt(summatory.length() - 1);
						summatory.deleteCharAt(summatory.length() - 1);
					}
					summatory.append(")");
				}
				double bj = b[j].fi(sequence.getObservation(t));
				alphaMatrix.setToValue(t, j, alphaInduction * bj);
				if (scaled) {
					factor += alphaInduction * bj;
				}
				if (debug) {
					summatory.append(" x " + bj + " = " + alphaInduction * bj);
					logger.log(Level.INFO, summatory.toString());
				}
			}

			if (scaled) { // Scale all the values inside the Tth (st/nd/rd) column of the alpha matrix
				factors[t] = 1 / factor;
				SparseArray currentColumn = alphaMatrix.getColumn(t);
				for (Couple cell : currentColumn) {
					int state = cell.getX();
					cell.setValue(cell.getValue() * factors[t]);
					if (debug) {
						logger.log(Level.INFO, "alpha(" + t + ")(" + statesNames.get(state)
								+ ") scaled by a factor C = " + factors[t] + " --> " + cell.getValue());
					}
				}
			}
		}
	}
	
	private static void alphaTermination(BWContainer container, int time, boolean scaled, boolean debug) {
		SparseMatrix alphaMatrix = container.getAlphaMatrix();
		SparseArray currentColumn = alphaMatrix.getColumn(time - 1);
		double[] factors = null;
		StringBuilder operationLog = null;
		if (debug) {
			operationLog = new StringBuilder("");
			operationLog.append("alpha = ");
			if (currentColumn.effectiveLength() == 0) {
				operationLog.append("0.0");
			}
		}
		double alpha = 0.0; // Termination - Implementation of formula 21 (base formula) or 103 (scaled formula)
		if (scaled) { // alpha scaled is 1/(product of all the scaling factors), as stated on formula
						// 102 in Rabiner's paper, but due to the limited dynamic range of the machine
						// formula 103 is used instead of the first one
			factors = container.getScalingFactors();
			for (double factor : factors) {
				alpha += Math.log(factor);
				if (debug) {
					operationLog.append("log(" + factor + ") [ " + Math.log(factor) + "] + ");
				}
			}
			alpha *= -1.0;
		} else {
			if (debug) {
				operationLog.append("(");
			}
			for (Couple cell : currentColumn) {
				alpha += cell.getValue();
				if (debug) {
					operationLog.append(cell.getValue() + " + ");
				}
			}
		}
		if (debug) {
			operationLog.deleteCharAt(operationLog.length() - 1);
			operationLog.deleteCharAt(operationLog.length() - 1);
			operationLog.deleteCharAt(operationLog.length() - 1);
			operationLog.append(") = " + alpha);
			logger.log(Level.INFO, operationLog.toString());
		}
		container.setAlphaValue(alpha);
	}

	public static void beta(ContinuousModel model, BWContainer container, ObsSequence sequence, boolean scaled,
			boolean debug) {
		if (debug) {
			logger.log(Level.INFO, "\n\n[Initialization of beta]");
		}
		betaInitialization(model,container,sequence.size(),scaled,debug);
		if (debug) {
			logger.log(Level.INFO, "\n\n[Induction of beta]");
		}
		betaInduction(model,container,sequence, scaled, debug);
	}
	
	private static void betaInitialization(ContinuousModel model, BWContainer container, int sequenceSize, boolean scaled, boolean debug) {
		int numberOfStates = model.getNumberOfStates();
		SparseMatrix betaMatrix = container.getBetaMatrix();
		double[] factors = container.getScalingFactors(); // Alpha method must be executed before this!
		int time = sequenceSize;
		ArrayList<String> statesNames = null;
		if (debug) {
			statesNames = model.getStatesNames();
		}
		for (int state = 0; state < numberOfStates; state++) { // Initialization - Implementation of formula 24
			betaMatrix.setToValue(time - 1, state, 1.0);
			if (debug) {
				logger.log(Level.INFO, "beta(" + (time - 1) + ")(" + statesNames.get(state) + ") = 1.0");
			}
		}
		if (scaled) {
			SparseArray currentColumn = betaMatrix.getColumn(time - 1);
			for (Couple cell : currentColumn) {
				cell.setValue(cell.getValue() * factors[time - 1]);
				if (debug) {
					logger.log(Level.INFO, "beta(" + (time - 1) + ")(" + statesNames.get(cell.getX())
							+ ") scaled by a factor C = " + factors[time - 1] + " --> " + cell.getValue());
				}
			}
		}
	}
	
	private static void betaInduction(ContinuousModel model, BWContainer container, ObsSequence sequence, boolean scaled, boolean debug) {
		SparseMatrix a = model.getA();
		GaussianCurve[] b = model.getB();
		int numberOfStates = model.getNumberOfStates();
		SparseMatrix betaMatrix = container.getBetaMatrix();
		double[] factors = null; // Alpha method must be executed before this!
		if(scaled) {
			factors = container.getScalingFactors();
		}
		int time = sequence.size();
		ArrayList<String> statesNames = null;
		StringBuilder betaLog = null;
		if (debug) {
			statesNames = model.getStatesNames();
			betaLog = new StringBuilder();
		}
		double betaInduction;
		for (int t = time - 2; t >= 0; t--) { // Induction - Implementation of formula 25
			SparseArray previousColumn = betaMatrix.getColumn(t + 1);
			for (int i = 0; i < numberOfStates; i++) {
				betaInduction = 0.0;
				if (debug) {
					betaLog.setLength(0);
					betaLog.append("beta(" + t + ")(" + statesNames.get(i) + ") = ");
				}
				for (Couple cell : previousColumn) {
					int state = cell.getX();
					betaInduction += a.getValue(i, state) * b[state].fi(sequence.getObservation(t + 1))
							* betaMatrix.getValue(t + 1, state);

					if (debug) {
						betaLog.append("(" + a.getValue(i, state) + " x "
								+ b[state].fi(sequence.getObservation(t + 1)) + " x beta(" + (t + 1) + ")("
								+ statesNames.get(state) + ") [" + betaMatrix.getValue(t + 1, state) + "]) + ");
					}
				}
				if (debug) {
					if (previousColumn.effectiveLength() == 0) {
						betaLog.append("0.0");
					} else {
						betaLog.deleteCharAt(betaLog.length() - 1);
						betaLog.deleteCharAt(betaLog.length() - 1);
						betaLog.deleteCharAt(betaLog.length() - 1);
						betaLog.append(" = " + betaInduction);
					}
					logger.log(Level.INFO, betaLog.toString());
				}
				betaMatrix.setToValue(t, i, betaInduction);
			}
			if (scaled) {
				SparseArray currentColumn = betaMatrix.getColumn(t);
				for (Couple cell : currentColumn) {
					cell.setValue(cell.getValue() * factors[t]);
					if (debug) {
						logger.log(Level.INFO, "beta(" + t + ")(" + statesNames.get(cell.getX())
								+ ") scaled by a factor C = " + factors[t] + " --> " + cell.getValue());
					}
				}
			}
		}
	}

	public static double gamma(ContinuousModel model, BWContainer container, int time, int state, boolean debug) {
		StringBuilder gammaLog = null;
		;
		int numberOfStates = model.getNumberOfStates();
		SparseMatrix alphaMatrix = container.getAlphaMatrix();
		SparseMatrix betaMatrix = container.getBetaMatrix();
		double numerator = alphaMatrix.getValue(time, state) * betaMatrix.getValue(time, state);
		if (debug) {
			gammaLog = new StringBuilder();
			gammaLog.append("gamma(" + time + ")(" + model.getStatesNames().get(state) + ") = "
					+ alphaMatrix.getValue(time, state) + " * " + betaMatrix.getValue(time, state));
		}
		double denominator = 0.0;
		if (debug) {
			gammaLog.append(" / (");
		}
		for (int i = 0; i < numberOfStates; i++) {
			denominator += alphaMatrix.getValue(time, i) * betaMatrix.getValue(time, i);
			if (debug) {
				gammaLog
						.append("(" + alphaMatrix.getValue(time, i) + " * " + betaMatrix.getValue(time, i) + ") + ");
			}
		}
		if (debug) {
			gammaLog.deleteCharAt(gammaLog.length() - 1);
			gammaLog.deleteCharAt(gammaLog.length() - 1);
			gammaLog.deleteCharAt(gammaLog.length() - 1);
			gammaLog.append(") = ");
		}
		if (Double.compare(denominator, 0.0) == 0) {
			throw new ArithmeticException("Can't divide by zero");
		}
		if (debug) {
			gammaLog.append(numerator / denominator);
			logger.log(Level.INFO, gammaLog.toString());
		}
		return numerator / denominator;
	}

	public static double xi(ContinuousModel model, BWContainer container, ObsSequence sequence, int statei, int statej,
			int time, boolean debug) {
		SparseMatrix alphaMatrix = container.getAlphaMatrix();
		SparseMatrix betaMatrix = container.getBetaMatrix();
		if (time >= sequence.size() - 1) {
			logger.log(Level.WARNING, "Xi formula used with an inexistent state j, 0.0 returned!");
			return 0.0;
		}
		int numberOfStates = model.getNumberOfStates();
		SparseMatrix a = model.getA();
		GaussianCurve[] b = model.getB();
		StringBuilder xiLog = null;
		if (debug) {
			xiLog = new StringBuilder();
			xiLog.append(
					"Xi(" + model.getStatesNames().get(statei) + ")(" + model.getStatesNames().get(statej) + ") = ");
			xiLog.append("alpha(" + time + ")(" + model.getStatesNames().get(statei) + ") ["
					+ alphaMatrix.getValue(time, statei) + "] * " + "A (" + model.getStatesNames().get(statei) + ")("
					+ model.getStatesNames().get(statej) + ") [" + a.getValue(statei, statej) + "] *"
					+ b[statej].fi(sequence.getObservation(time + 1)) + " *" + "beta(" + (time + 1) + ")("
					+ model.getStatesNames().get(statej) + ") [" + betaMatrix.getValue(time + 1, statej) + "]");
		}
		double numerator = alphaMatrix.getValue(time, statei) * a.getValue(statei, statej)
				* b[statej].fi(sequence.getObservation(time + 1)) * betaMatrix.getValue(time + 1, statej);
		if (debug) {
			xiLog.append(" / ");
		}
		double denominator = 0.0;
		for (int i = 0; i < numberOfStates; i++) {
			for (int j = 0; j < numberOfStates; j++) {
				denominator += alphaMatrix.getValue(time, i) * a.getValue(i, j)
						* b[j].fi(sequence.getObservation(time + 1)) * betaMatrix.getValue(time + 1, j);
				if (debug) {
					xiLog.append("alpha(" + time + ")(" + model.getStatesNames().get(i) + ") ["
							+ alphaMatrix.getValue(time, i) + "] * " + "A (" + model.getStatesNames().get(i) + ")("
							+ model.getStatesNames().get(j) + ") [" + a.getValue(i, j) + "] *"
							+ b[statej].fi(sequence.getObservation(time + 1)) + " *" + "beta(" + (time + 1) + ")("
							+ model.getStatesNames().get(j) + ") [" + betaMatrix.getValue(time + 1, j) + "] + ");
				}
			}
		}
		if (Double.compare(denominator, 0.0) == 0) {
			throw new ArithmeticException("Can't divide by zero");
		}
		if (debug) {
			xiLog.deleteCharAt(xiLog.length() - 1);
			xiLog.deleteCharAt(xiLog.length() - 1);
			xiLog.deleteCharAt(xiLog.length() - 1);
			xiLog.append(" = " + (numerator / denominator));
			logger.log(Level.INFO, xiLog.toString());
		}
		return numerator / denominator;
	}
	
	public static double psi(BWContainer container) { // Viterbi Algorithm, alpha method must be executed before than this
		SparseMatrix alphaMatrix = container.getAlphaMatrix();
		Couple[] psiStates = container.getPsiArray();
		int columnNumber = 0; // Keep track of the column number while using "for-each"
		for(SparseArray column : alphaMatrix) {
			if(column.effectiveLength()==0) { // All the values in the column are 0
				psiStates[columnNumber] = new Couple(0,0.0); // A placeholder cell C(x=column,y=0,value=0.0) is added to the sequence
			}
			else {
				Couple best = null;
				for(Couple cell : column) {
					if(best==null) { // First cell visited
						best = cell; // and it's considered the best at the moment
					}
					else { // It is possible to compare cells
						if(Double.compare(best.getValue(),cell.getValue())<0) { // If best.value < current.value
							best = cell; // Replace best with current visited cell
						}
					}
				}
				psiStates[columnNumber] = best; // Assign the best cell to the sequence
			}
			columnNumber++;
		}
		return psiStates[(columnNumber -1)].getValue(); // Returns the value of the best cell at time T
	}

}
