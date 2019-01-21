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
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import classifier.core.Common;
import classifier.core.Console;
import classifier.workers.PreprocessDatasetWorker;

public class PreprocessDatasetAction extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 146601744221816914L;
	protected Console console;
	protected JFormattedTextField txtfldMergeFactor;

	public PreprocessDatasetAction(Console console, JFormattedTextField txtfldMergeFactor) {
		this.console = console;
		this.txtfldMergeFactor = txtfldMergeFactor;
	}

	@Override
	public void actionPerformed(ActionEvent evt) {

		try {
			File sourceDir, outDir;
			synchronized(Common.preprocessInIndex) {
				sourceDir = Common.preprocessInIndex;
			}
			synchronized(Common.preprocessOutFolder) {
				outDir = Common.preprocessOutFolder;
			}
			int n = Integer.parseInt(txtfldMergeFactor.getText());
			if (n <= 0) {
				throw new NumberFormatException();
			}
			PreprocessDatasetWorker worker = new PreprocessDatasetWorker(sourceDir, outDir, n);
			console.addText("Processing the dataset, wait please... ");
			Window win = SwingUtilities.getWindowAncestor((AbstractButton) evt.getSource());
			JDialog dialog = new JDialog(win, "Processing the dataset...", ModalityType.APPLICATION_MODAL);
			worker.addPropertyChangeListener(new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					if (evt.getPropertyName().equals("state")) {
						if (evt.getNewValue() == SwingWorker.StateValue.DONE) {
							try {
								worker.get();
								console.addText("Dataset processed successfully");
							} catch (Exception ex) {
								console.addText("Error while processing the dataset: " + ex.getCause().getMessage());
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
		} catch (NumberFormatException nex) {
			JOptionPane.showMessageDialog(null, "Please specify a valid merge factor!", "Error",
					JOptionPane.ERROR_MESSAGE);
		} catch (Exception ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
}