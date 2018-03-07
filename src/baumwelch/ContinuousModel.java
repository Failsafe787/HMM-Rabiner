/*
 * Released under MIT License (Expat)
 * @author Luca Banzato
 * @version 0.1.8
 */

package baumwelch;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import exceptions.IllegalADefinitionException;
import exceptions.IllegalBDefinitionException;
import exceptions.IllegalPiDefinitionException;
import exceptions.IllegalStatesNamesSizeException;
import utils.Couple;
import utils.GaussianCurve;
import utils.Log;
import utils.SparseMatrix;
import utils.SparseArray;

public class ContinuousModel {

	private SparseMatrix a; // A
	private GaussianCurve[] b; // B, or better, the gaussians that generate B
	private SparseArray pi; // Pi
	private ArrayList<String> statesName; // States Names (used for logging and save-to-file purposes)
	private int nStates; // Number of states the model has
	private Logger logger = Log.getLogger();

	// Constructor used if A, B, Pi, number of states and names of the states are already available
	public ContinuousModel(int nStates, SparseMatrix a, GaussianCurve[] b, SparseArray pi,
			ArrayList<String> statesNames) throws IllegalStatesNamesSizeException, IllegalADefinitionException,
			IllegalBDefinitionException, IllegalPiDefinitionException {
		// Consistency checks between the number of the states and the structures provided
		if (a.getRowsNumber() != nStates && a.getColumnsNumber() != nStates) {
			throw new IllegalADefinitionException();
		}
		if (nStates != b.length) {
			throw new IllegalBDefinitionException();
		}
		if (nStates != pi.size()) {
			throw new IllegalPiDefinitionException();
		}
		if (nStates != statesNames.size()) {
			throw new IllegalStatesNamesSizeException();
		}
		this.nStates = nStates;
		this.a = a;
		this.b = b;
		this.pi = pi;
		this.statesName = statesNames;

	}

	// Constructor used if the model has to be read from a file
	public ContinuousModel(int nStates, String pathName) throws IllegalPiDefinitionException,
			IllegalADefinitionException, IllegalBDefinitionException, IllegalStatesNamesSizeException {
		// Prepare all the structures in order to store the values
		this.nStates = nStates;
		a = new SparseMatrix(nStates, nStates);
		b = new GaussianCurve[nStates];
		pi = new SparseArray(nStates);
		statesName = new ArrayList<String>();
		// Read from file
		readFromFile(pathName);
	}

	public SparseMatrix getA() { // Returns A matrix
		return a;
	}

	public GaussianCurve[] getB() { // Returns B vector
		return b;
	}

	public SparseArray getPi() { // Returns Pi vector
		return pi;
	}

	public ArrayList<String> getStatesNames() { //  Returns all the states names stored in an array
		return statesName;
	}

	public void setA(SparseMatrix a) throws IllegalADefinitionException { // Sets A
		if (this.a.getRowsNumber() >= a.getRowsNumber() || this.a.getColumnsNumber() >= a.getColumnsNumber()) {
			throw new IllegalADefinitionException(); // Consistency check
		}
		this.a = a;
	}
	
	public void setPi(SparseArray pi) { // Sets Pi
		this.pi = pi;
	}

	public void setB(GaussianCurve[] b) throws IllegalBDefinitionException { // Sets B
		if (b.length != nStates) {
			throw new IllegalBDefinitionException(); // Consistency check
		}
		this.b = b;
	}

	public int getNumberOfStates() { // Returns the number of states of the model
		return nStates;
	}

	public void randomizePi() { // Randomize Pi vector (used in randomize())
		probabilityFiller(pi);
	}

	public void randomizeA() { // Randomize A matrix (used in randomize())
		for (SparseArray column : a) {
			probabilityFiller(column);
		}
	}

	public void randomizeB(double polarizedValue) {
		// Takes a polarizedValue for Mu and Sigma values generator (this value is used
		// as base for Mu and Sigma)
		Random randomgen = new Random(); // This need to be replaced with a probability generator
		for (GaussianCurve curve : b) {
			if (polarizedValue > 1) {
				int sign = randomgen.nextBoolean() ? -1 : 1; // Used to generate +-
				curve.setMu(polarizedValue + sign * randomgen.nextDouble()); // java.util.random doesn't provide a
																				// nextDouble(min_value, max_value)
																				// method
																				// in Java 9
				sign = randomgen.nextBoolean() ? -1 : 1;
				curve.setSigma(polarizedValue + sign * randomgen.nextDouble());
			} else {
				curve.setMu(randomgen.nextDouble());
				curve.setSigma(randomgen.nextDouble());
			}
		}
	}

	private void probabilityFiller(SparseArray array) {
		Random randomgen = new Random(); // This need to be replaced with a probability generator
		double probabilitySum = 1.0; // Probability left to assign
		for (int i = 0; i < array.effectiveLength(); i++) {
			Couple cell = array.getCell(i);
			if (i < array.effectiveLength() - 1) {
				double generatedProbability;
				while ((generatedProbability = randomgen.nextDouble()) > probabilitySum)
					;
				// Keeps generating values compatible with the probability left
				cell.setValue(generatedProbability);
				probabilitySum -= generatedProbability;
			} else {
				cell.setValue(probabilitySum); // Last element, assign remaining percentage of probability
			}
		}
	}

	public void readFromFile(String pathName) throws IllegalPiDefinitionException, IllegalADefinitionException,
			IllegalBDefinitionException, IllegalStatesNamesSizeException {
		readStartState(pathName + ".start"); // Pi
		readStatesTransitions(pathName + ".trans"); // A
		readCurves(pathName + ".curves"); // B
	}

	private void readStartState(String path) throws IllegalPiDefinitionException, IllegalStatesNamesSizeException {
		boolean valid = false;
		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
			String line;
			while ((line = br.readLine()) != null) {
				String multiSpaceLine = line.replaceAll("\t", " "); // Transforms the whole line in format "State1
																	// State2 Probability" (removes all unused
																	// tabs/spaces)
				String singleSpaceLine = multiSpaceLine.trim().replaceAll("\\s+", " ");
				String[] tuple = singleSpaceLine.split(" ");
				if (singleSpaceLine.matches("([a-zA-Z0-9]+)\\s(\\d+(\\.\\d+)*)")) {
					if (!statesName.contains(tuple[0])) { // Add the state to the states set (First state of rule)
						statesName.add(tuple[0]);
						if (statesName.size() > nStates) {
							throw new IllegalStatesNamesSizeException(
									"The number of the states in PI isn't matching the number passed to the constructor!");
						}
						pi.setToValue(statesName.indexOf(tuple[0]), Double.parseDouble(tuple[1]));
					} else {
						throw new IllegalPiDefinitionException("A state in PI was declared more than one time!"); 
						// State is already present, but that means  a double definition inside the file defining PI states
					}
					if (!valid) {
						valid = true;
					}
				}
			}
			br.close();
			if (!valid) { // Cannot read any sort of initial state or rule
				throw new IllegalPiDefinitionException();
			}
		} catch (FileNotFoundException e) {
			logger.log(Level.WARNING, "File " + path + " has not been found!");
		} catch (IOException e) {
			logger.log(Level.WARNING, "There was an IO error while reading " + path);
		}

	}

	private void readStatesTransitions(String path)
			throws IllegalADefinitionException, IllegalStatesNamesSizeException { // Path + project name (e.g.
		// /home/user/hmm/models/phone,
		// without .pi, .trans or .curves)
		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
			String line;
			int linePosition = 0;
			while ((line = br.readLine()) != null) {
				linePosition++;
				String multiSpaceLine = line.replaceAll("\t", " "); // Transforms the whole line in format "State1
																	// State2 Probability" (removes all unused
																	// tabs/spaces)
				String singleSpaceLine = multiSpaceLine.trim().replaceAll("\\s+", " ");
				if (singleSpaceLine.matches("([a-zA-Z0-9]+)\\s([a-zA-Z0-9]+)\\s(\\d+(\\.\\d+)*)")) { // Transition
																										// Rule
																										// definition
					String[] tuple = singleSpaceLine.split(" ");
					if (!statesName.contains(tuple[0])) { // Add the state to the states set (First state of rule)
						statesName.add(tuple[0]);
						if (statesName.size() > nStates) {
							throw new IllegalStatesNamesSizeException(
									"The number of the states in A isn't matching the number passed to the constructor!");
						}
					}
					if (!statesName.contains(tuple[1])) { // Add the state to the states set (Second state of rule)
						statesName.add(tuple[1]);
						if (statesName.size() > nStates) {
							throw new IllegalStatesNamesSizeException(
									"The number of the states in A isn't matching the number passed to the constructor!");
						}
					}
					int x = statesName.indexOf(tuple[0]); // x lines (current state) for y columns (next state)
					int y = statesName.indexOf(tuple[1]);
					a.setToValue(x, y, Double.parseDouble(tuple[2])); // x-1 and y-1 means we've to consider the
																		// presence of the INIT state
				} else {
					logger.log(Level.WARNING, "There was an error while reading line " + linePosition + " in " + path);
				}
			}
		} catch (FileNotFoundException e) {
			logger.log(Level.WARNING, "File " + path + " has not been found!");
		} catch (IOException e) {
			logger.log(Level.WARNING, "There was an IO error while reading " + path);
		}
	}

	private boolean readCurves(String path) throws IllegalBDefinitionException, IllegalStatesNamesSizeException { 
		// Path + project name (e.g. /home/user/hmm/models/phone, without .pi, .trans or .curves extensions)
		boolean valid = false;
		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
			String line;
			while ((line = br.readLine()) != null) {
				String multiSpaceLine = line.replaceAll("\t", " "); // Transforms the whole line in format "State1
																	// State2 Probability" (removes all unused
																	// tabs/spaces)
				String singleSpaceLine = multiSpaceLine.trim().replaceAll("\\s+", " ");
				if (singleSpaceLine.matches("([a-zA-Z0-9]+)\\s(\\d+(\\.\\d+)*)\\s(\\d+(\\.\\d+)*)")) { // Emission Rule
																										// definition
					String[] tuple = singleSpaceLine.split(" ");
					if (!statesName.contains(tuple[0])) { // Add the state to the states set (First state of rule). Used
															// only if a state emits, but it's not reachable.
						statesName.add(tuple[0]);
						if (statesName.size() > nStates) {
							throw new IllegalStatesNamesSizeException(
									"The number of the states in B isn't matching the number passed to the constructor!");
						}
					}
					int x = statesName.indexOf(tuple[0]); // x lines (current state) for y columns (possible outcomes)
					b[x] = new GaussianCurve(Double.parseDouble(tuple[1]), Double.parseDouble(tuple[2]));
					if (!valid) { // A valid line has been found
						valid = true;
					}
				}
			}
			if (!valid) { // Cannot read any sort of initial state or rule
				throw new IllegalBDefinitionException("Cannot read any valid curve inside the specified file for B");
			}
		} catch (FileNotFoundException e) {
			logger.log(Level.WARNING, "File " + path + " has not been found!");
			return false;
		} catch (IOException e) {
			logger.log(Level.WARNING, "There was an IO error while reading " + path);
			return false;
		}
		for (GaussianCurve curve : b) {
			if (curve == null) {
				logger.log(Level.WARNING,
						"The number of the curves declared in B isn't matching the number of states passed to the constructor!");
				return false;
			}
		}
		return true;
	}

	public void writeToFiles(String pathName) {
		writePi(pathName + ".start"); // Pi
		writeStatesTransitions(pathName + ".trans"); // A
		writeCurves(pathName + ".curves"); // B
	}

	private void writePi(String path) {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
			for (int i = 0; i < statesName.size(); i++) { // Writes Pi
				bw.write(statesName.get(i) + "\t" + pi.getValue(statesName.indexOf(statesName.get(i))) + "\n");
			}
			bw.close();
		} catch (FileNotFoundException e) {
			logger.log(Level.WARNING, "File " + path + " has not been found!");
		} catch (IOException e) {
			logger.log(Level.WARNING, "There was an IO error while writing " + path);
		}
	}

	private void writeStatesTransitions(String path) {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
			int columnNumber = 0;
			for (SparseArray column : a) { // Writes A
				for (Couple matrixState : column) {
					bw.write(statesName.get(columnNumber) + "\t" + statesName.get(matrixState.getX()) + "\t"
							+ matrixState.getValue() + "\n");
				}
				columnNumber++;
			}
		} catch (FileNotFoundException e) {
			logger.log(Level.WARNING, "File " + path + " has not been found!");
		} catch (IOException e) {
			logger.log(Level.WARNING, "There was an IO error while writing " + path);
		}
	}

	private void writeCurves(String path) {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
			for (int i = 0; i < statesName.size(); i++) { // Writes B
				GaussianCurve curve = b[i];
				bw.write(statesName.get(i) + "\t" + curve.getMu() + "\t" + curve.getSigma() + "\n");
			}
		} catch (FileNotFoundException e) {
			logger.log(Level.WARNING, "File " + path + " has not been found!");
		} catch (IOException e) {
			logger.log(Level.WARNING, "There was an IO error while writing " + path);
		}

	}

}
