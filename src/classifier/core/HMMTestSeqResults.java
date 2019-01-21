package classifier.core;

public class HMMTestSeqResults {
	
	public String modelName;
	public String sequenceFileName;
	public double value;
	public boolean bestSolution;

	public HMMTestSeqResults(String modelName, String sequenceFileName, double value) {
		this.modelName = modelName;
		this.sequenceFileName = sequenceFileName;
		this.value = value;
		this.bestSolution = false;
	}
	
	public HMMTestSeqResults(String modelName, String sequenceFileName, double value, boolean bestSolution) {
		this.modelName = modelName;
		this.sequenceFileName = sequenceFileName;
		this.value = value;
		this.bestSolution = bestSolution;
	}
}
