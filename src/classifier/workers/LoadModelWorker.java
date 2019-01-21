package classifier.workers;

import java.io.File;

import javax.swing.SwingWorker;

import classifier.core.Common;
import hmm.TrainableHMM;
import struct.Struct_HMMDataset;

public class LoadModelWorker extends SwingWorker<Integer, String> {

	/** HMM file index */
	private final File hmmFile;

	/**
	 * Creates an instance of the worker
	 * 
	 * @param hmmFile An HMM file index
	 */
	public LoadModelWorker(final File hmmFile) {
		this.hmmFile = hmmFile;
	}

	@Override
	protected Integer doInBackground() throws Exception {
		Struct_HMMDataset hmm_dataset = new Struct_HMMDataset();
		{
			hmm_dataset.hmm = new TrainableHMM(hmmFile);
			hmm_dataset.hmmFile = hmmFile;
			hmm_dataset.dataset = null;
			hmm_dataset.datasetDirectory = null;
		}
		synchronized(Common.loaded) {
			Common.loaded.add(hmm_dataset);
			Common.modelsLoaded++;
		}
		return 1;
	}
}
