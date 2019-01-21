package classifier.workers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import javax.swing.SwingWorker;

public class ConsoleSaveLogWorker extends SwingWorker<Integer, String> {

	/** HMM file index */
	private final File outFile;
	private final String text;

	/**
	 * Creates an instance of the worker
	 * 
	 * @param hmmFile An HMM file index
	 */
	public ConsoleSaveLogWorker(final File outFile, final String text) {
		this.outFile = outFile;
		this.text = text;
	}

	@Override
	protected Integer doInBackground() throws Exception {
			BufferedWriter bw = new BufferedWriter(new FileWriter(outFile.getPath()));
			bw.write(text);
			bw.close();
			return 1;
	}
}