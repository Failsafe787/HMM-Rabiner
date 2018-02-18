/*
 * Released under MIT License (Expat)
 * @author Luca Banzato
 * @version 0.1
 */

package baumwelch;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import utils.Log;

public class ObsSequence {

	// NOTE: this class can only handle numeric values as observations

	private ArrayList<Double> sequence;
	public final static int VALUEANDSPACE = 1; // Format: V1 V2 V3 ... VN
	public final static int VALUEPERLINE = 2; // Format: V1\nV2\nV3...\nVN
	private Logger logger = Log.getLogger();

	public ObsSequence() { // Empty sequence
		sequence = new ArrayList<Double>();
	}

	public ObsSequence(ArrayList<Double> sequence) { // Takes a sequence and builds this object based on that
		this.sequence = sequence;
	}

	public ObsSequence(Double[] sequence) { // Takes an array and builds this object based on that

		this.sequence = new ArrayList<Double>(Arrays.asList(sequence));
	}

	public ObsSequence(String pathName, int fileFormat) { // Takes a pathname and file format, reads the specified file
															// and builds this object
		try (BufferedReader br = new BufferedReader(new FileReader(pathName))) {
			sequence = new ArrayList<Double>();
			boolean finished = false;
			boolean valid = false;
			String line;
			while ((line = br.readLine()) != null && !finished) {
				try {
					if (fileFormat == 2) {
						try {
							sequence.add(Double.parseDouble(line));
							if (!valid) {
								valid = true;
							}
						} catch (NumberFormatException e) {
							logger.log(Level.WARNING, "There was an error while parsing \"" + line + "\". Skipped!");
						}
					} else if (fileFormat == 1) {
						String[] values = line.split(" ");
						for (int i = 0; i < values.length; i++) {
							try {
								this.sequence.add(Double.parseDouble(values[i]));
								if (!finished) {
									finished = true; // stupid way of exiting here
								}
							} catch (NumberFormatException e) {
								logger.log(Level.WARNING,
										"There was an error while parsing \"" + values[i] + "\". Skipped!");
							}
						}
					}

				} catch (NumberFormatException e) {
					logger.log(Level.WARNING, "There was an error while parsing \"" + line + "\". Skipped!");
				}
			}
			if (!finished && fileFormat == 1) {
				logger.log(Level.WARNING,
						"The file provided is invalid! Are you sure if it's in the format V1 V2 ... VN?");
				sequence = null;
			} else if (!valid && fileFormat == 2) {
				logger.log(Level.WARNING,
						"The file provided is invalid! Are you sure if it's in the format V1\\nV2\\n ... VN?");
				sequence = null;
			}
		} catch (FileNotFoundException e) {
			logger.log(Level.WARNING, "File " + pathName + " has not been found!");
			sequence = null;
		} catch (IOException e) {
			logger.log(Level.WARNING, "There was an IO error while reading " + pathName);
			sequence = null;
		} catch (Exception e) {
			logger.log(Level.WARNING, "There was an error: " + e.getMessage());
			sequence = null;
		}
	}

	public void addObservation(double observation) { // Appends an observation at the end of this sequence
		sequence.add(observation);
	}

	public double getObservation(int time) { // Returns an observation observed at time t
		return sequence.get(time);
	}

	public boolean setObservation(int time, double value) { // Sets the value of an observation at time t
		try {
			sequence.set(time, value);
			return true;
		} catch (IndexOutOfBoundsException e) {
			return false;
		}
	}

	public boolean removeObservation(int time) { // Remove an observation observed at time t. NOTE: this will remove a
													// time unit from all the next observations (e.g t+1 -> t)
		try {
			sequence.remove(time);
			return true;
		} catch (IndexOutOfBoundsException e) {
			return false;
		}
	}

	public ArrayList<Double> getSequence() { // Returns the whole sequence as ArrayList
		return sequence;
	}

	public boolean setSequence(ArrayList<Double> sequence) { // Replaces the whole sequence with the one provided
		this.sequence = sequence;
		return true;
	}

	public double getMean() {
		if (sequence.size() == 0) {
			return 0.0;
		} else {
			double numerator = 0.0;
			for (double value : sequence) {
				numerator += value;
			}
			return numerator / sequence.size();
		}
	}

	public int size() {
		return sequence.size();
	}
}
