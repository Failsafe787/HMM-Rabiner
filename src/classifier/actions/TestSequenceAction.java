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
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileFilter;

import classifier.core.Console;
import classifier.workers.TestSequenceWorker;

public class TestSequenceAction extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7530414184992359033L;
	protected Console console;

	public TestSequenceAction(Console console) {
		this.console = console;

	}

	@Override
	public void actionPerformed(ActionEvent evt) {

		try {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setDialogTitle("Load Sequences");
			fileChooser.setApproveButtonText("Load");
			fileChooser.setMultiSelectionEnabled(true);
			fileChooser.setFileFilter(new TxtFileFilter());
			int n = fileChooser.showOpenDialog(null);
			if (n == JFileChooser.APPROVE_OPTION) {
				boolean useViterbi = false;
				int result = JOptionPane.showConfirmDialog(null, "Would you like to save the states sequence using Viterbi?",
						"Viterbi", JOptionPane.YES_NO_OPTION);
				if(result==JOptionPane.YES_OPTION) {
					useViterbi = true;
				}
				TestSequenceWorker worker = new TestSequenceWorker(fileChooser.getSelectedFiles(), console, useViterbi);
				console.addText("Loading sequences to be tested... ");
				Window win = SwingUtilities.getWindowAncestor((AbstractButton) evt.getSource());
				final JDialog dialog = new JDialog(win, "Loading sequences...", ModalityType.APPLICATION_MODAL);
				worker.addPropertyChangeListener(new PropertyChangeListener() {

					@Override
					public void propertyChange(PropertyChangeEvent evt) {
						if (evt.getPropertyName().equals("state")) {
							if (evt.getNewValue() == SwingWorker.StateValue.DONE) {
								try {
									worker.get();
									System.gc();
								} catch (Exception ex) {
									console.addText("Error while loading the sequence: " + ex.getCause().getMessage());
									ex.printStackTrace();
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
			JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private class TxtFileFilter extends FileFilter {

		public boolean accept(File file) {
			if (file.isDirectory())
				return true;
			String fname = file.getName().toLowerCase();
			return fname.endsWith("hst");
		}

		public String getDescription() {
			return "HST Observations File";
		}
	}
}