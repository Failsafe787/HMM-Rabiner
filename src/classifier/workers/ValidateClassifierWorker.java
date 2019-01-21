package classifier.workers;

import javax.swing.SwingWorker;

import classifier.core.Common;
import classifier.core.Console;
import classifier.core.HMMTestSeqResults;
import classifier.validation.CrossValidationBinary;

public class ValidateClassifierWorker extends SwingWorker<Integer, HMMTestSeqResults> {

	/** HMM file index */
	private final Console console;
	private final int nFolds;

	/**
	 * Creates an instance of the worker
	 * 
	 * @param sequencesFiles An array of files containing sequences
	 */
	public ValidateClassifierWorker(Console console, int nFolds) {
		this.console = console;
		this.nFolds = nFolds;
	}

	@Override
	protected Integer doInBackground() throws Exception {
		CrossValidationBinary cross_val = new CrossValidationBinary();
		console.addText(cross_val.evalutate(nFolds, Common.loaded));
		return 1;
	}
}