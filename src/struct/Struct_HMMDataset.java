package struct;

import java.io.File;

import hmm.TrainableHMM;
import utils.Observation;

public class Struct_HMMDataset {

	public TrainableHMM hmm;
	public File hmmFile;
	public Observation[][] dataset;
	public File datasetDirectory;
	
}
