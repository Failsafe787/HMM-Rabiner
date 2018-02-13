package baumwelch;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import exceptions.IllegalADefinitionException;
import exceptions.IllegalBDefinitionException;
import exceptions.IllegalPiDefinitionException;
import exceptions.IllegalStatesNamesSizeException;
import utils.Couple;
import utils.GaussianCurve;
import utils.SparseMatrix;
import utils.SparseArray;
import utils.Triplet;

public class ContinuousModel {

	private SparseMatrix a;
	private GaussianCurve[] b;
	private SparseArray pi;
	private ArrayList<String> statesName;
	private int nStates;

	public ContinuousModel(int nStates, SparseMatrix a, GaussianCurve[] b, SparseArray pi,
			ArrayList<String> statesNames) throws IllegalStatesNamesSizeException, IllegalADefinitionException,
			IllegalBDefinitionException, IllegalPiDefinitionException {
		if (a.getRowsNumber() != nStates && a.getColumnsNumber() != nStates) {
			throw new IllegalADefinitionException();
		}
		if (nStates != b.length) {
			throw new IllegalBDefinitionException();
		}
		if (nStates != pi.length()) {
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

	public ContinuousModel(int nStates, String pathName) throws IllegalPiDefinitionException,
			IllegalADefinitionException, IllegalBDefinitionException, IllegalStatesNamesSizeException {
		this.nStates = nStates;
		a = new SparseMatrix(nStates, nStates);
		b = new GaussianCurve[nStates];
		pi = new SparseArray(nStates);
		statesName = new ArrayList<String>();
		readFromFile(pathName);
	}

	public SparseMatrix getA() {
		return a;
	}

	public GaussianCurve[] getB() {
		return b;
	}

	public SparseArray getPi() {
		return pi;
	}

	public ArrayList<String> getStatesNames() {
		return statesName;
	}

	public void setA(SparseMatrix a) throws IllegalADefinitionException {
		if (this.a.getRowsNumber() >= a.getRowsNumber() || this.a.getColumnsNumber() >= a.getColumnsNumber()) {
			throw new IllegalADefinitionException();
		}
		this.a = a;
	}

	public void setB(GaussianCurve[] b) throws IllegalBDefinitionException {
		if (b.length != nStates) {
			throw new IllegalBDefinitionException();
		}
		this.b = b;
	}

	public int getNumberOfStates() {
		return nStates;
	}

	public void setPi(SparseArray pi) {
		this.pi = pi;
	}

	public void randomize() {
		Random randomgen = new Random();
		for (Couple cell : pi) {
			cell.setValue(randomgen.nextDouble());
		}
		for (Triplet cell : a) {
			cell.setValue(randomgen.nextDouble());
		}
		for (GaussianCurve curve : b) {
			curve.setMu(randomgen.nextDouble());
			curve.setSigma(randomgen.nextDouble());
		}
	}

	public void readFromFile(String pathName) throws IllegalPiDefinitionException, IllegalADefinitionException,
			IllegalBDefinitionException, IllegalStatesNamesSizeException {
		readStartState(pathName + ".start");
		readStatesTransitions(pathName + ".trans");
		readCurves(pathName + ".curves");
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
							throw new IllegalStatesNamesSizeException();
						}
						pi.setToValue(statesName.indexOf(tuple[0]), Double.parseDouble(tuple[1]));
					} else {
						throw new IllegalPiDefinitionException(); // State is already present, but that means a double
																	// definition on pi!
					}
					if (!valid) {
						valid = true;
					}
				}
			}
			if (!valid) { // Cannot read any sort of initial state or rule
				throw new IllegalPiDefinitionException();
			}
		} catch (FileNotFoundException e) {
			System.out.println("File " + path + " has not been found!");
		} catch (IOException e) {
			System.out.println("There was an IO error while reading " + path);
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
							throw new IllegalStatesNamesSizeException();
						}
					}
					if (!statesName.contains(tuple[1])) { // Add the state to the states set (Second state of rule)
						statesName.add(tuple[1]);
						if (statesName.size() > nStates) {
							throw new IllegalStatesNamesSizeException();
						}
					}
					int x = statesName.indexOf(tuple[0]); // x lines (current state) for y columns (next state)
					int y = statesName.indexOf(tuple[1]);
					a.setToValue(x, y, Double.parseDouble(tuple[2])); // x-1 and y-1 means we've to consider the
																		// presence of the INIT state
				} else {
					System.out
							.println("[Skipped] There was an error while reading line " + linePosition + " in " + path);
				}
			}
		} catch (FileNotFoundException e) {
			System.out.println("File " + path + " has not been found!");
		} catch (IOException e) {
			System.out.println("There was an IO error while reading " + path);
		}
	}

	private boolean readCurves(String path) throws IllegalBDefinitionException, IllegalStatesNamesSizeException { // Path
																													// +
																													// project
																													// name
																													// (e.g.
		// /home/user/hmm/models/phone,
		// without .pi, .trans
		// or .curves)
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
							throw new IllegalStatesNamesSizeException();
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
				throw new IllegalBDefinitionException();
			}
			return valid;
		} catch (FileNotFoundException e) {
			System.out.println("File " + path + " has not been found!");
			return false;
		} catch (IOException e) {
			System.out.println("There was an IO error while reading " + path);
			return false;
		}
	}

	public void writeToFiles(String pathName) {
		writePi(pathName + ".start");
		writeStatesTransitions(pathName + ".trans");
		writeCurves(pathName + ".curves");
	}

	private void writePi(String path) {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
			for (int i = 0; i < statesName.size(); i++) { // Writes A
				bw.write(statesName.get(i) + "\t" + pi.getValue(statesName.indexOf(statesName.get(i))) + "\n");
			}
		} catch (FileNotFoundException e) {
			System.out.println("File " + path + " has not been found!");
		} catch (IOException e) {
			System.out.println("There was an IO error while writing " + path);
		}
	}

	private void writeStatesTransitions(String path) {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
			for (Triplet matrixState : a) { // Writes A
				bw.write(statesName.get(matrixState.getX()) + "\t" + statesName.get(matrixState.getY()) + "\t"
						+ matrixState.getValue() + "\n");
			}
		} catch (FileNotFoundException e) {
			System.out.println("File " + path + " has not been found!");
		} catch (IOException e) {
			System.out.println("There was an IO error while writing " + path);
		}
	}

	private void writeCurves(String path) {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
			for (int i = 0; i < statesName.size(); i++) { // Writes B
				GaussianCurve curve = b[i];
				bw.write(statesName.get(i) + "\t" + curve.getMu() + "\t" + curve.getSigma() + "\n");
			}
		} catch (FileNotFoundException e) {
			System.out.println("File " + path + " has not been found!");
		} catch (IOException e) {
			System.out.println("There was an IO error while writing " + path);
		}

	}

}
