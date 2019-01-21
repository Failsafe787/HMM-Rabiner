package classifier.workers;

import java.io.File;
import javax.swing.SwingWorker;

import utils.DatasetUtils;

public class PreprocessDatasetWorker extends SwingWorker<Integer, String> {

	/** HMM file index */
	private final File sourceIndex;
	private final File outDir;
	private final int n;

	/**
	 * Creates an instance of the worker
	 * 
	 * @param hmmFile An HMM file index
	 */
	public PreprocessDatasetWorker(final File sourceDir, final File outDir, final int n) {
		this.sourceIndex = sourceDir;
		this.outDir = outDir;
		this.n = n;
	}

	@Override
	protected Integer doInBackground() throws Exception {
		DatasetUtils.meanNRescale(sourceIndex, outDir, n);
		return 1;
	}
}