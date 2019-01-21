package classifier.actions;

import java.awt.BorderLayout;
import java.awt.Dialog.ModalityType;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileFilter;

import classifier.core.Common;
import classifier.core.Console;
import classifier.workers.LoadDatasetWorker;

public class LoadDatasetAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3891044661472141018L;
	protected Console console;
	protected JLabel lblDatasetPath;
	protected JButton btnRemoveDataset, btnTrainModel, btnValidateClassifier;
	protected JList<String> list;
	protected int index;

	public LoadDatasetAction(JList<String> list, Console console, JLabel lblDatasetPath, JButton btnRemoveDataset,
			JButton btnTrainModel, JButton btnValidateClassifier) {
		this.list = list;
		this.console = console;
		this.lblDatasetPath = lblDatasetPath;
		this.btnRemoveDataset = btnRemoveDataset;
		this.btnValidateClassifier = btnValidateClassifier;
		this.btnTrainModel = btnTrainModel;

	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		try {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setDialogTitle("Load Dataset");
			fileChooser.setApproveButtonText("Load");
			fileChooser.setFileFilter(new TxtFileFilter());
			int n = fileChooser.showOpenDialog(null);
			if (n == JFileChooser.APPROVE_OPTION) {
				final File datasetDir = fileChooser.getSelectedFile();
				index = list.getSelectedIndex();
				LoadDatasetWorker worker = new LoadDatasetWorker(datasetDir, index);
				console.addText("Loading dataset for " + list.getSelectedValue() + " model...");
				Window win = SwingUtilities.getWindowAncestor((AbstractButton) evt.getSource());
				JDialog dialog = new JDialog(win, "Dialog", ModalityType.APPLICATION_MODAL);

				worker.addPropertyChangeListener(new PropertyChangeListener() {

					@Override
					public void propertyChange(PropertyChangeEvent evt) {
						if (evt.getPropertyName().equals("state")) {
							if (evt.getNewValue() == SwingWorker.StateValue.DONE) {
								try {
									worker.get();
									lblDatasetPath.setText(datasetDir.getAbsolutePath());
									btnRemoveDataset.setEnabled(true);
									btnTrainModel.setEnabled(true);
									synchronized(Common.loaded) {
										if(Common.modelsLoaded > 1 && Common.modelsLoaded==Common.datasetsLoaded) {
											btnValidateClassifier.setEnabled(true);
										}
									}
									console.addText("Dataset loaded: " + datasetDir.getAbsolutePath());
								} catch (Exception ex) {
									console.addText("Error while loading dataset: " + ex.getCause().getMessage());
									JOptionPane.showMessageDialog(null, ex.getCause().getMessage(), "Error",
											JOptionPane.ERROR_MESSAGE);
								} finally {
									dialog.dispose();
								}
							}
						}
					}
				});
				worker.execute();

				JProgressBar progressBar = new JProgressBar();
				progressBar.setIndeterminate(true);
				JPanel panel = new JPanel(new BorderLayout());
				panel.add(progressBar, BorderLayout.CENTER);
				panel.add(new JLabel("Please wait......."), BorderLayout.PAGE_START);
				dialog.add(panel);
				dialog.setUndecorated(true);
				dialog.pack();
				dialog.setLocationRelativeTo(win);
				dialog.setVisible(true);
			}
		} catch (Exception ex) {
			console.addText("Error while loading dataset: " + ex.getMessage());
			JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private class TxtFileFilter extends FileFilter {

		public boolean accept(File file) {
			if (file.isDirectory())
				return true;
			String fname = file.getName().toLowerCase();
			return fname.endsWith("dsi");
		}

		public String getDescription() {
			return "Dataset Index File";
		}
	}
}