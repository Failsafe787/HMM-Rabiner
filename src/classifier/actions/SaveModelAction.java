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
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import classifier.core.Common;
import classifier.core.Console;
import classifier.workers.SaveModelWorker;

public class SaveModelAction extends AbstractAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5611024109783196159L;
	protected Console console;
	protected JList<String> list;
	protected int index;

	public SaveModelAction(JList<String> list, Console console) {
		this.list = list;
		this.console = console;
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		try {
			JFileChooser dirChooser = new JFileChooser();
			dirChooser.setDialogTitle("Save Model To...");
			dirChooser.setApproveButtonText("Save");
			dirChooser.setSelectedFile(new File(list.getSelectedValue()+".hmm"));
			int n = dirChooser.showOpenDialog(null);
			if (n == JFileChooser.APPROVE_OPTION) {
				final File modelFilePath = dirChooser.getSelectedFile();
				index = list.getSelectedIndex();
				SaveModelWorker worker = new SaveModelWorker(modelFilePath, index);
				console.addText("Saving " + list.getSelectedValue() + " model...");
				Window win = SwingUtilities.getWindowAncestor((AbstractButton) evt.getSource());
				JDialog dialog = new JDialog(win, "Dialog", ModalityType.APPLICATION_MODAL);

				worker.addPropertyChangeListener(new PropertyChangeListener() {

					@Override
					public void propertyChange(PropertyChangeEvent evt) {
						if (evt.getPropertyName().equals("state")) {
							if (evt.getNewValue() == SwingWorker.StateValue.DONE) {
								try {
									worker.get();
									console.addText("Model saved to " + modelFilePath.getAbsolutePath());
								} catch (Exception ex) {
									synchronized(Common.loaded) {
										console.addText("Error while saving " + Common.loaded.get(index).hmm.getModelName() + " model: " + ex.getCause().getMessage());
									}
									JOptionPane.showMessageDialog(null, ex.getCause().getMessage(), "Error",
											JOptionPane.ERROR_MESSAGE);
									ex.printStackTrace();
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
			synchronized(Common.loaded) {
				console.addText("Error while saving" + Common.loaded.get(index).hmm.getModelName() + " model: " + ex.getMessage());
			}
			JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
}