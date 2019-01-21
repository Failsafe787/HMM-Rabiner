package classifier.validation;

import java.io.IOException;
import java.util.ArrayList;

import hmm.TrainableHMM;
import struct.Struct_HMMDataset;
import struct.Struct_LabeledDataset;
import utils.DatasetUtils;
import utils.Observation;

public class CrossValidationBinary {

	public String evalutate(int nFolds, ArrayList<Struct_HMMDataset> data) throws IOException {

		// Cumulative Measures (must be divided by the number of folds at the end)
		double error_rate = 0.0, accuracy = 0.0, precision = 0.0, recall = 0.0;
		StringBuilder resultsBuilder = new StringBuilder("");

		// Dataset shuffling
		// System.out.println("SHUFFLING");
		Observation[][][] shuffledDatasets = shuffleDatasets(data);

		// Folds
		for (int i = 0; i < nFolds; i++) {

			// Data structures needed for folds
			Observation[][][] testDataset = new Observation[2][][];
			ModelAnalysis[] models = new ModelAnalysis[2];

			// System.out.println(i + " TRAINING");
			// Learning step
			for (int j = 0; j < 2; j++) {

				// Create dataset split for each model (learning - training)
				int stepSize = (int) (Math.ceil((1.0 * shuffledDatasets[j].length) / nFolds));
				int begin = i * stepSize;
				int end = i * stepSize + stepSize <= shuffledDatasets[j].length ? i * stepSize + stepSize
						: shuffledDatasets[j].length;
				// System.out
				// .println("TEST DATA: " + begin + " to " + end + ". TOTAL DATA = " +
				// shuffledDatasets[j].length);
				Struct_LabeledDataset hmm_dataset = split(shuffledDatasets[j], data.get(j).hmm.getModelName(), begin,
						end);
				// System.out.println(hmm_dataset.learningSet[0][0]==null);
				// System.out.println(hmm_dataset.testSet[0][0]==null);
				models[j] = new ModelAnalysis(new TrainableHMM(data.get(j).hmmFile), hmm_dataset.learningSet);

				// Add a labeled dataset to the arraylist
				testDataset[j] = hmm_dataset.testSet;
				models[j].train();
			}

			// System.out.println(i + " TESTING");
			// Test step
			int true_positive = 0, false_positive = 0, true_negative = 0, false_negative = 0, testSetSize = 0;
			for (int datasetPosition = 0; datasetPosition < testDataset.length; datasetPosition++) {
				for (int seqNum = 0; seqNum < testDataset[datasetPosition].length; seqNum++) {
					// Detecting the expected best model for the sequence
					testSetSize++;
					double bestValue = -1;
					int bestModelIndex = -1;
					for (int j = 0; j < 2; j++) {
						double currentValue = models[j].test(testDataset[datasetPosition][seqNum]);
						System.out.println(bestValue + " ..... " + currentValue + "....." + j);
						if (bestValue == -1 || bestModelIndex == -1 || currentValue < bestValue) {
							bestValue = currentValue;
							bestModelIndex = j;
						}
					}
					System.out.println(bestValue + " ..... " + bestModelIndex);
					System.out.println(bestModelIndex + " >>>>> " + datasetPosition);
					// Checking if the expected value corresponds with the labeled one
					if ((bestModelIndex == datasetPosition) && (datasetPosition == 0)) {
						// System.out.println("TP");
						true_positive++;
					}
					if ((datasetPosition == 0) && (bestModelIndex != datasetPosition)) {
						// System.out.println("FP");
						false_positive++;
					}
					if ((bestModelIndex == datasetPosition) && (datasetPosition == 1)) {
						// System.out.println("TN");
						true_negative++;
					}
					if ((datasetPosition == 1) && (bestModelIndex != datasetPosition)) {
						// System.out.println("FN");
						false_negative++;
					}
					// System.out.println(true_positive + " - " + false_positive + " - " +
					// true_negative + " - " + false_negative + " - TT " + testSetSize);
				}
			}
			// Saving fold results
			double error_rate_fold = 1.0 * (false_positive + false_negative) / testSetSize;
			double accuracy_fold = 1.0 * (true_positive + true_negative) / testSetSize;
			double precision_fold = 1.0 * true_positive / (true_positive + false_positive);
			double recall_fold = 1.0 * true_positive / (true_positive + false_negative);
			print_stats(resultsBuilder, i, error_rate_fold, accuracy_fold, precision_fold, recall_fold);
			print_counters(resultsBuilder, true_positive, false_positive, true_negative, false_negative);

			// Updating measurements
			error_rate += 1.0 * (false_positive + false_negative) / testSetSize;
			accuracy += 1.0 * (true_positive + true_negative) / testSetSize;
			precision += 1.0 * true_positive / (true_positive + false_positive);
			recall += 1.0 * true_positive / (true_positive + false_negative);
			// System.out.println(error_rate + " " + accuracy + " " + precision + " " +
			// recall);
		}
		error_rate /= nFolds;
		accuracy /= nFolds;
		precision /= nFolds;
		recall /= nFolds;
		return print_final_stats(resultsBuilder, error_rate, accuracy, precision, recall);
	}

	/**
	 * Split into Learning/Training Set the labeled dataset passed as input
	 *
	 * @param sequences the initial dataset
	 * @param label     the name of the model that produced this dataset
	 * @param testBegin the index of the beginning part of the test dataset
	 * @param testEnd   the index of the final part of the test dataset
	 * 
	 */
	private Struct_LabeledDataset split(Observation[][] sequences, String label, int testBegin, int testEnd) {
		Observation[][] learningSequences = new Observation[sequences.length - (testEnd - testBegin)][];
		Observation[][] trainingSequences = new Observation[testEnd - testBegin][];
		int learn_index = 0; // sequences idexes
		int train_index = 0;
		if (testBegin != 0) {
			for (int i = 0; i < testBegin; i++) {
				learningSequences[learn_index++] = sequences[i];
			}
		}
		for (int i = testBegin; i < testEnd; i++) {
			trainingSequences[train_index++] = sequences[i];
		}
		if (testEnd < sequences.length) {
			for (int i = testEnd; i < sequences.length; i++) {
				learningSequences[learn_index++] = sequences[i];
			}
		}
		Struct_LabeledDataset result = new Struct_LabeledDataset();
		{
			result.learningSet = learningSequences;
			result.testSet = trainingSequences;
			result.label = label;
		}
		return result;
	}

	private Observation[][][] shuffleDatasets(ArrayList<Struct_HMMDataset> data) {
		Observation[][][] shuffledDatasets = new Observation[data.size()][][];
		for (int d = 0; d < data.size(); d++) {
			shuffledDatasets[d] = DatasetUtils.shuffleDataset(data.get(d).dataset);
		}
		return shuffledDatasets;
	}

	private void print_stats(StringBuilder builder, int foldNum, double error_rate, double accuracy, double precision,
			double recall) {
		builder.append("\nEvalutation results (Fold " + (foldNum + 1) + ")\n=========================\n\n");
		builder.append("Error rate: " + error_rate * 100 + "%\n");
		builder.append("Accuracy: " + accuracy * 100 + "%\n");
		builder.append("Precision: " + precision * 100 + "%\n");
		builder.append("Recall: " + recall * 100 + "%\n\n");
	}

	private void print_counters(StringBuilder builder, int truPos, int falPos, int truNeg, int falNeg) {
		builder.append("\nConfusion Matrix\n");
		builder.append("True Positive: " + truPos + "\n");
		builder.append("False Positive: " + falPos + "\n");
		builder.append("True Negative: " + truNeg + "\n");
		builder.append("False Negative: " + falNeg + "\n");
	}

	private String print_final_stats(StringBuilder builder, double error_rate, double accuracy, double precision,
			double recall) {
		builder.append("\nEvalutation results\n===================\n\n");
		builder.append("Average Error rate: " + error_rate * 100 + "%\n");
		builder.append("Average Accuracy: " + accuracy * 100 + "%\n");
		builder.append("Average Precision: " + precision * 100 + "%\n");
		builder.append("Average Recall: " + recall * 100 + "%\n\n");
		return builder.toString();
	}

}
