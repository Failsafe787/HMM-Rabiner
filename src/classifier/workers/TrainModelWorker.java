package classifier.workers;

import javax.swing.SwingWorker;

import classifier.core.Common;
import struct.Struct_HMMDataset;
import utils.DatasetUtils;
import utils.Observation;

public class TrainModelWorker extends SwingWorker<Integer, String> {

	/** HMM file index */
	private final int index;
	private final int iterations;
	private final boolean savePartialModels;

	/**
	 * Creates an instance of the worker
	 * 
	 * @param hmmFile An HMM file index
	 */
	public TrainModelWorker(final int index, final int iterations, final boolean savePartialModels) {
		this.index = index;
		this.iterations = iterations;
		this.savePartialModels = savePartialModels;
	}

	@Override
	protected Integer doInBackground() throws Exception {
		synchronized (Common.loaded) {
			Struct_HMMDataset data = Common.loaded.get(index);

			if (iterations > 0) { // Fixed number of iterations
				if (savePartialModels) {
					data.hmm.bw_fixed(iterations, data.dataset, data.hmmFile);
				} else {
					data.hmm.bw_fixed(iterations, data.dataset);
				}
			} else { // Use pruning set
				Observation[][][] datasets = DatasetUtils.shuffleAndSplit(data.dataset);
				Observation[][] learningSet = datasets[0];
				Observation[][] pruningSet = datasets[1];
				if (savePartialModels) {
					data.hmm.bw_adaptive(learningSet, pruningSet, data.hmmFile);
				} else {
					data.hmm.bw_adaptive(learningSet, pruningSet);
				}
			}
		}
		return 1;
	}
}
