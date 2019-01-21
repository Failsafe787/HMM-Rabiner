package classifier.workers;

import java.io.File;

import javax.swing.SwingWorker;

import classifier.core.Common;

public class SaveModelWorker extends SwingWorker<Integer, String> {

	/** HMM file index */
	private final File outDir;
	private final int index;

	/**
	 * Creates an instance of the worker
	 * 
	 * @param hmmFile An HMM file index
	 */
	public SaveModelWorker(final File outDir, final int index) {
		this.outDir = outDir;
		this.index = index;
	}

	@Override
	protected Integer doInBackground() throws Exception {
		synchronized(Common.loaded) {
			Common.loaded.get(index).hmm.writeToFile(outDir);
		}
		return 1;
	}
}