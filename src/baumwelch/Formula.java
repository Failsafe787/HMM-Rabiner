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
		SparseArray pi = model.getPi();
		SparseMatrix a = model.getA();
		GaussianCurve[] b = model.getB();
		int numberOfStates = model.getNumberOfStates();
		SparseMatrix alphaMatrix = container.getAlphaMatrix();
		double[] factors = container.getScalingFactors();
		double denominator = 0.0; // Used for scaling factor computation
		int time = sequence.size();
		ArrayList<String> statesNames = null; // Used for debug
		if (debug) {
			statesNames = model.getStatesNames();
			logger.log(Level.INFO, "\n\n[Initialization of alpha]");
		}
		for (Couple cell : pi) { // Initialization
			int state = cell.getX();
			double pi1 = cell.getValue();
			double bi1 = b[state].fi(sequence.getObservation(0));
			alphaMatrix.setToValue(0, state, pi1 * bi1);
			if (scaled) {
				denominator += pi1 * bi1;
			}
			if (debug) {
				logger.log(Level.INFO,
						"alpha(0)(" + statesNames.get(state) + ") = " + pi1 + " x " + bi1 + " = " + pi1 * bi1);
			}
		}

		if (scaled) { // Scale all the values inside the 1st column of the alpha matrix
			factors[0] = 1 / denominator;
			SparseArray currentColumn = alphaMatrix.getColumn(0);
			for (Couple cell : currentColumn) { // Initialization
				int state = cell.getX();
				cell.setValue(cell.getValue() * factors[0]);
				if (debug) {
					logger.log(Level.INFO, "alpha(0)(" + statesNames.get(state) + ") scaled by a factor C = "
							+ factors[0] + " --> " + cell.getValue());
				}
			}
			denominator = 0.0; // Reset for the inductive phase
		}

		if (debug) {
			logger.log(Level.INFO, "\n\n[Induction of alpha]");
		}
		for (int t = 1; t < time; t++) { // Induction
			SparseArray previousColumn = alphaMatrix.getColumn(t - 1);
			double alphaInduction = 0.0;
			for (int j = 0; j < numberOfStates; j++) {
				StringBuilder summatory = null;
				if (debug) {
					summatory = new StringBuilder("");
					summatory.append("alpha(" + t + ")(" + statesNames.get(j) + ") = ");
					summatory.append("(");
				}
				for (Couple cell : previousColumn) {
					// System.out.println(a.toString());
					// System.out.println(cell.getX() + " has value " + cell.getValue() + " and A("
					// + cell.getX() + ")("
					// + j + ") has value " + a.getValue(cell.getX(), j));
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
					denominator += alphaInduction * bj;
				}
				if (debug) {
					summatory.append(" x " + bj + " = " + alphaInduction * bj);
					logger.log(Level.INFO, summatory.toString());
				}
			}

			if (scaled) { // Scale all the values inside the Tth (st/nd/rd) column of the alpha matrix
				factors[t] = 1 / denominator;
				SparseArray currentColumn = alphaMatrix.getColumn(t);
				for (Couple cell : currentColumn) {
					int state = cell.getX();
					cell.setValue(cell.getValue() * factors[t]);
					if (debug) {
						logger.log(Level.INFO, "alpha(" + t + ")(" + statesNames.get(state)
								+ ") scaled by a factor C = " + factors[t] + " --> " + cell.getValue());
					}
				}
				denominator = 0.0;
			}
		}
		if (debug) {
			logger.log(Level.INFO, "\n\n[Termination of alpha]");
		}
		SparseArray currentColumn = alphaMatrix.getColumn(time - 1);
		StringBuilder summatory = null;
		if (debug) {
			summatory = new StringBuilder("");
			summatory.append("alpha = ");
			if (currentColumn.effectiveLength() == 0) {
				summatory.append("0.0");
			}
		}
		double alpha = 0.0; // Termination
		if (scaled) { // alpha scaled is 1/(product of all the scaling factors), as stated on formula
						// 102 in Rabiner's paper, but due to the limited dynamic range of the machine
						// formula 103 is used instead of the first one
			for (double factor : factors) {
				alpha += Math.log(factor);
				if (debug) {
					summatory.append("log(" + factor + ") [ " + Math.log(factor) + "] + ");
				}
			}
			alpha *= -1.0;
		} else {
			if (debug) {
				summatory.append("(");
			}
			for (Couple cell : currentColumn) {
				alpha += cell.getValue();
				if (debug) {
					summatory.append(cell.getValue() + " + ");
				}
			}
		}
		if (debug) {
			summatory.deleteCharAt(summatory.length() - 1);
			summatory.deleteCharAt(summatory.length() - 1);
			summatory.deleteCharAt(summatory.length() - 1);
			summatory.append(") = " + alpha);
			logger.log(Level.INFO, summatory.toString());
		}
		container.setAlphaValue(alpha);
	}

	public static void beta(ContinuousModel model, BWContainer container, ObsSequence sequence, boolean scaled,
			boolean debug) {
		SparseMatrix a = model.getA();
		GaussianCurve[] b = model.getB();
		int numberOfStates = model.getNumberOfStates();
		SparseMatrix betaMatrix = container.getBetaMatrix();
		double[] factors = container.getScalingFactors(); // Alpha method must be executed before this!
		int time = sequence.size();
		ArrayList<String> statesNames = null;
		if (debug) {
			logger.log(Level.INFO, "\n\n[Initialization of beta]");
			statesNames = model.getStatesNames();
		}
		for (int state = 0; state < numberOfStates; state++) { // Initialization
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
		if (debug) {
			logger.log(Level.INFO, "\n\n[Induction of beta]");
		}
		for (int t = time - 2; t >= 0; t--) { // Induction
			SparseArray previousColumn = betaMatrix.getColumn(t + 1);
			for (int i = 0; i < numberOfStates; i++) {
				double betaInduction = 0.0;
				StringBuilder summatory = null;
				if (debug) {
					summatory = new StringBuilder("");
					summatory.append("beta(" + t + ")(" + statesNames.get(i) + ") = ");
				}
				for (Couple cell : previousColumn) {
					int state = cell.getX();
					betaInduction += a.getValue(i, state) * b[state].fi(sequence.getObservation(t + 1))
							* betaMatrix.getValue(t + 1, state);

					if (debug) {
						summatory.append("(" + a.getValue(i, state) + " x "
								+ b[state].fi(sequence.getObservation(t + 1)) + " x beta(" + (t + 1) + ")("
								+ statesNames.get(state) + ") [" + betaMatrix.getValue(t + 1, state) + "]) + ");
					}
				}
				if (debug) {
					if (previousColumn.effectiveLength() == 0) {
						summatory.append("0.0");
					} else {
						summatory.deleteCharAt(summatory.length() - 1);
						summatory.deleteCharAt(summatory.length() - 1);
						summatory.deleteCharAt(summatory.length() - 1);
						summatory.append(" = " + betaInduction);
					}
					logger.log(Level.INFO, summatory.toString());
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
		StringBuilder stringbuilded = null;
		;
		int numberOfStates = model.getNumberOfStates();
		SparseMatrix alphaMatrix = container.getAlphaMatrix();
		SparseMatrix betaMatrix = container.getBetaMatrix();
		double numerator = alphaMatrix.getValue(time, state) * betaMatrix.getValue(time, state);
		if (debug) {
			stringbuilded = new StringBuilder();
			stringbuilded.append("gamma(" + time + ")(" + model.getStatesNames().get(state) + ") = "
					+ alphaMatrix.getValue(time, state) + " * " + betaMatrix.getValue(time, state));
		}
		double denominator = 0.0;
		if (debug) {
			stringbuilded.append(" / (");
		}
		for (int i = 0; i < numberOfStates; i++) {
			denominator += alphaMatrix.getValue(time, i) * betaMatrix.getValue(time, i);
			if (debug) {
				stringbuilded
						.append("(" + alphaMatrix.getValue(time, i) + " * " + betaMatrix.getValue(time, i) + ") + ");
			}
		}
		if (debug) {
			stringbuilded.deleteCharAt(stringbuilded.length() - 1);
			stringbuilded.deleteCharAt(stringbuilded.length() - 1);
			stringbuilded.deleteCharAt(stringbuilded.length() - 1);
			stringbuilded.append(") = ");
		}
		if (Double.compare(denominator, 0.0) == 0) {
			throw new ArithmeticException("Can't divide by zero");
		}
		if (debug) {
			stringbuilded.append(numerator / denominator);
			logger.log(Level.INFO, stringbuilded.toString());
		}
		return numerator / denominator;
	}

	public static double psi(ContinuousModel model, BWContainer container, ObsSequence sequence, int statei, int statej,
			int time, boolean debug) {
		SparseMatrix alphaMatrix = container.getAlphaMatrix();
		SparseMatrix betaMatrix = container.getBetaMatrix();
		if (time >= sequence.size() - 1) {
			logger.log(Level.WARNING, "Psi formula used with an inexistent state j, 0.0 returned!");
			return 0.0;
		}
		int numberOfStates = model.getNumberOfStates();
		SparseMatrix a = model.getA();
		GaussianCurve[] b = model.getB();
		StringBuilder formulaLog = null;
		if (debug) {
			formulaLog = new StringBuilder();
			formulaLog.append(
					"Psi(" + model.getStatesNames().get(statei) + ")(" + model.getStatesNames().get(statej) + ") = ");
			formulaLog.append("alpha(" + time + ")(" + model.getStatesNames().get(statei) + ") ["
					+ alphaMatrix.getValue(time, statei) + "] * " + "A (" + model.getStatesNames().get(statei) + ")("
					+ model.getStatesNames().get(statej) + ") [" + a.getValue(statei, statej) + "] *"
					+ b[statej].fi(sequence.getObservation(time + 1)) + " *" + "beta(" + (time + 1) + ")("
					+ model.getStatesNames().get(statej) + ") [" + betaMatrix.getValue(time + 1, statej) + "]");
		}
		double numerator = alphaMatrix.getValue(time, statei) * a.getValue(statei, statej)
				* b[statej].fi(sequence.getObservation(time + 1)) * betaMatrix.getValue(time + 1, statej);
		if (debug) {
			formulaLog.append(" / ");
		}
		double denominator = 0.0;
		for (int i = 0; i < numberOfStates; i++) {
			for (int j = 0; j < numberOfStates; j++) {
				denominator += alphaMatrix.getValue(time, i) * a.getValue(i, j)
						* b[j].fi(sequence.getObservation(time + 1)) * betaMatrix.getValue(time + 1, j);
				if (debug) {
					formulaLog.append("alpha(" + time + ")(" + model.getStatesNames().get(i) + ") ["
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
			formulaLog.deleteCharAt(formulaLog.length() - 1);
			formulaLog.deleteCharAt(formulaLog.length() - 1);
			formulaLog.deleteCharAt(formulaLog.length() - 1);
			formulaLog.append(" = " + (numerator / denominator));
			logger.log(Level.INFO, formulaLog.toString());
		}
		return numerator / denominator;
	}

}
