package classifier.validation;

import java.io.IOException;

import hmm.TrainableHMM;
import utils.DatasetUtils;
import utils.Observation;

public class ModelAnalysis {

	private Observation[][] learningSet;
	private TrainableHMM model;
	
	public ModelAnalysis(TrainableHMM model, Observation[][] learningSet) {
		this.model = model;
		this.learningSet = learningSet;
	}
	
	public void train() throws IOException {
		Observation[][][] learning_pruning = DatasetUtils.shuffleAndSplit(learningSet);
		model.bw_adaptive(learning_pruning[0], learning_pruning[1]);
	}
	
	public double test(Observation[] sequence) {
		return model.likelihood(sequence);
	}
}
