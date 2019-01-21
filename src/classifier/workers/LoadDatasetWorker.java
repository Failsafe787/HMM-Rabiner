package classifier.workers;

import java.io.File;
import javax.swing.SwingWorker;

import classifier.core.Common;
import utils.DatasetUtils;

public class LoadDatasetWorker extends SwingWorker<Integer, String> {

	/** HMM file index */
	private final File datasetDir;
	private final int index;

	/**
	 * Creates an instance of the worker
	 * 
	 * @param hmmFile An HMM file index
	 */
	public LoadDatasetWorker(final File datasetDir, final int index) {
		this.datasetDir = datasetDir;
		this.index = index;
	}

	@Override
	protected Integer doInBackground() throws Exception {
		synchronized(Common.loaded) {
			Common.loaded.get(index).datasetDirectory = datasetDir;
			Common.loaded.get(index).dataset = DatasetUtils.loadDatasetFromDir(datasetDir);
			Common.datasetsLoaded++;
		}
		return 1;
	}
}