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
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileFilter;

import classifier.core.Common;
import classifier.core.Console;
import classifier.workers.LoadModelWorker;

public class LoadModelAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1811602272879182606L;
	/**
	* 
	*/
	protected Console console;
	protected DefaultListModel<String> listModel;
	protected JButton btnTestSequence, btnValidateClassifier;

	public LoadModelAction(DefaultListModel<String> listModel, JButton btnTestSequence, JButton btnValidateClassifier, Console console) {
		this.listModel = listModel;
		this.btnTestSequence = btnTestSequence;
		this.btnValidateClassifier = btnValidateClassifier;
		this.console = console;

	}

	@Override
	public void actionPerformed(ActionEvent evt) {

		try {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setDialogTitle("Load Model");
			fileChooser.setApproveButtonText("Load");
			fileChooser.setFileFilter(new TxtFileFilter());
			int n = fileChooser.showOpenDialog(null);
			if (n == JFileChooser.APPROVE_OPTION) {
				LoadModelWorker worker = new LoadModelWorker(fileChooser.getSelectedFile());
				console.addText("Loading model... ");
				Window win = SwingUtilities.getWindowAncestor((AbstractButton) evt.getSource());
				JDialog dialog = new JDialog(win, "Loading model...", ModalityType.APPLICATION_MODAL);
				worker.addPropertyChangeListener(new PropertyChangeListener() {

					@Override
					public void propertyChange(PropertyChangeEvent evt) {
						if (evt.getPropertyName().equals("state")) {
							if (evt.getNewValue() == SwingWorker.StateValue.DONE) {
								try {
									worker.get();
									console.addText("Model loaded: " + fileChooser.getSelectedFile().getAbsolutePath());
									synchronized(Common.loaded) {
										console.addText(Common.loaded.get(Common.loaded.size()-1).hmm.toString());
										if(Common.modelsLoaded > 1 && Common.modelsLoaded==Common.datasetsLoaded) {
											btnValidateClassifier.setEnabled(true);
										}
										else {
											btnValidateClassifier.setEnabled(false);
										}
									}
									listModel.addElement(
											fileChooser.getSelectedFile().getName().replaceFirst("[.][^.]+$", ""));
									btnTestSequence.setEnabled(true);
								} catch (Exception ex) {
									console.addText("Error while loading model: " + ex.getCause().getMessage());
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
			ex.printStackTrace();
			JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private class TxtFileFilter extends FileFilter {

		public boolean accept(File file) {
			if (file.isDirectory())
				return true;
			String fname = file.getName().toLowerCase();
			return fname.endsWith("hmm");
		}

		public String getDescription() {
			return "HMM Model File";
		}
	}
}