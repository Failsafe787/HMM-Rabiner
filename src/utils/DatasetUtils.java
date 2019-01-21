package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class DatasetUtils {

	/**
	 * Returns a sequence of observations read from the file which path is passed as
	 * argument
	 *
	 * @param filePath an absolute path of a file
	 * @return an array of observations (a sequence)
	 * @throws IOException              if there's a problem reading the file stored
	 *                                  at the provided path
	 * @throws IllegalArgumentException if this file is in a bad format
	 */
	public static Observation[] loadObservationsFromFile(File obsFile) throws IOException {
		ArrayList<Observation> tempObservations = new ArrayList<Observation>();
		BufferedReader br = new BufferedReader(new FileReader(obsFile));
		String line;
		while ((line = br.readLine()) != null) {
			String formattedLine = SysUtils.formatString(line);
			if (formattedLine.matches("([0-9]+)\t(\\d+(\\.\\d+)*)")) {
				String[] splittedLine = formattedLine.split("\t");
				int time = Integer.parseInt(splittedLine[0]);
				double value = Double.parseDouble(splittedLine[1]);
				tempObservations.add(new Observation(time, value));
			}
		}
		br.close();
		if (tempObservations.size() == 0) {
			throw new IllegalArgumentException("No observations were loaded! (" + obsFile.getName() + ", bad sequence file)");
		}
		return tempObservations.toArray(new Observation[tempObservations.size()]);
	}

	/**
	 * Returns an array of sequences of observations read from all the files found
	 * in the directory which path is passed as argument
	 *
	 * @param dirPath an absolute path of a directory
	 * @return an array of array of observations (a set of sequences)
	 * @throws IOException              if there's a problem reading a file inside
	 *                                  this directory
	 * @throws IllegalArgumentException if the folder is empty or a file is in a bad
	 *                                  format
	 */
	public static Observation[][] loadDatasetFromDir(File datasetIndex) throws IOException {
		if (datasetIndex == null) {
			throw new IllegalArgumentException(
					"No sequences were loaded! (The specified index isn't a valid file or there was an I/O error)");
		}
		ArrayList<Observation[]> sequencesRead = new ArrayList<Observation[]>();
		BufferedReader br = new BufferedReader(new FileReader(datasetIndex));
		String line;
		while ((line = br.readLine()) != null) {
			File sequence = new File(SysUtils.osFilePath(datasetIndex.getParent(), line));
			if (sequence.isFile()) {
				sequencesRead.add(loadObservationsFromFile(sequence));
			}
		}
		br.close();
		if (sequencesRead.size() == 0) {
			throw new IllegalArgumentException("No sequences were loaded! (The specified directory is empty)");
		}
		return sequencesRead.toArray(new Observation[sequencesRead.size()][]);
	}
	
	/**
	 * Randomize and split into Learning/Pruning Set (80%/20%) the dataset passed as input
	 *
	 * @param dataset the initial dataset
	 * @param learningSet a variable where the learning set will be stored
	 * @param pruningSet a variabile where the pruning set will be stored
	 * 
	 */
	public static Observation[][][] shuffleAndSplit(Observation[][] dataset) {
		Observation[][] shuffledDataset = shuffleDataset(dataset);
		int splitPoint = (int)(shuffledDataset.length * 0.8);
		return new Observation[][][] {Arrays.copyOfRange(shuffledDataset, 0, splitPoint), Arrays.copyOfRange(shuffledDataset, splitPoint, shuffledDataset.length)};
	}
	
	/**
	 * Randomize the dataset passed as input using the Fisher–Yates algorithm
	 *
	 * @param dataset the dataset
	 * 
	 */
	public static Observation[][] shuffleDataset(Observation[][] dataset) {
		// Thanks to PhiLho @ stackoverflow for the suggestion
		// https://stackoverflow.com/questions/1519736/random-shuffling-of-an-array
		Observation[][] dataset_copy = copy_array(dataset);
		Random rnd = new Random();
		for (int i = dataset.length - 1; i > 0; i--){
			int index = rnd.nextInt(i + 1);
			Observation[] sequence = dataset_copy[index];
			dataset_copy[index] = dataset_copy[i];
			dataset_copy[i] = sequence;
		}
		return dataset_copy;
	}
	
	public static Observation[][] copy_array(Observation[][] dataset) {
		Observation[][] copied = new Observation[dataset.length][];
		for(int i=0; i< dataset.length; i++) {
			copied[i] = dataset[i];
		}
		return copied;
	}
	
	/**
	 * Writes to outpath a reduced sequence of observations read from inpath. 
	 * At a certain time T, each value is the mean of the N values observed 
	 * from time T to T + INTERVAL SIZE*N
	 *
	 * @param inPath	absolute path of input file
	 * @param outPath	absolute path of output file
	 * @param n	the number of observations considered for calculating the mean
	 * @throws IOException              if there's a problem reading or writing a file
	 * @throws IllegalArgumentException if this file is in a bad format
	 */
	public static void meanNRescale(File indexFile, File outDir, int n) throws IOException {
		ArrayList<Observation> buffer = new ArrayList<Observation>();
		if(!indexFile.isFile()) {
			throw new IllegalArgumentException("Invalid! source dataset index (or it isn't a file)");
		}
		if(!outDir.isDirectory()) {
			throw new IllegalArgumentException("Invalid Output directory! (or it isn't a directory)");
		}
		BufferedReader br = new BufferedReader(new FileReader(indexFile));
		String line;
		while ((line = br.readLine()) != null) {
			File sequenceFile = new File(SysUtils.osFilePath(indexFile.getParent(), line));
			if (sequenceFile.isFile()) {
				BufferedReader brseq = new BufferedReader(new FileReader(sequenceFile));
				String seqline;
				while ((seqline = brseq.readLine()) != null) {
					String formattedLine = SysUtils.formatString(seqline);
					if (formattedLine.matches("([a-zA-Z0-9]+)\t(\\d+(\\.\\d+)*)")) {
						String[] splittedLine = formattedLine.split("\t");
						int time = Integer.parseInt(splittedLine[0]);
						double value = Double.parseDouble(splittedLine[1]);
						buffer.add(new Observation(time, value));
					}
				}
				brseq.close();
				if(buffer.size()==0) {
					throw new IllegalArgumentException("No observations were processed! (" + sequenceFile.getName() + ", bad sequence file)");
				}
				BufferedWriter bw = new BufferedWriter(new FileWriter(SysUtils.osFilePath(outDir.getAbsolutePath(), sequenceFile.getName())));
				for (int i = 0; i < buffer.size(); i = i + n) {
					bw.write(accumulate(buffer, i, n).toString() + "\n");
				}
				bw.close();
				BufferedWriter bw_index = new BufferedWriter(new FileWriter(SysUtils.osFilePath(outDir.getAbsolutePath(), indexFile.getName()), true));
				bw_index.write(sequenceFile.getName()+"\n");
				bw_index.close();
				buffer.clear();
			}
		}
		br.close();
	}

	/**
	 * Returns a an Observation with time T and value equal to the mean 
	 * of the N values observed from time T to T + INTERVAL SIZE*N
	 *
	 * @param data	the original dataset
	 * @param start	index of the data array where to start processing 
	 * @param n	size of the original dataset
	 */
	private static Observation accumulate(ArrayList<Observation> data, int start, int n) {
		Observation res = new Observation();
		for (int i = start; i < start + n && i < data.size(); i++) {
			res.value += data.get(i).value;
		}
		res.time = data.get(start).time;
		res.value /= (data.size() - start < n ? data.size() - start : n);
		return res;
	}
}
