package classifier.actions;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import classifier.core.SwingGUI;
import classifier.workers.ConsoleSaveLogWorker;
import utils.SysUtils;

public class ConsoleSaveLogAction extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7965289641712577160L;
	protected JTextArea text;
	protected JList<String> list;
	protected int index;

	public ConsoleSaveLogAction(JTextArea text) {
		this.text = text;
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		try {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setDialogTitle("Save Log");
			fileChooser.setApproveButtonText("Save");
			fileChooser.setSelectedFile(new File("simple_hmm_" + SysUtils.currentTime() + ".log"));

			int n = fileChooser.showOpenDialog(null);
			if (n == JFileChooser.APPROVE_OPTION) {
				final File datasetDir = fileChooser.getSelectedFile();
				ConsoleSaveLogWorker worker = new ConsoleSaveLogWorker(datasetDir, text.getText());
				Window win = SwingUtilities.getWindowAncestor((AbstractButton) evt.getSource());
				JDialog dialog = new JDialog(win, "Dialog", ModalityType.APPLICATION_MODAL);
				dialog.setIconImage(ImageIO.read(SwingGUI.class.getResourceAsStream("/hmm.png")));

				worker.addPropertyChangeListener(new PropertyChangeListener() {

					@Override
					public void propertyChange(PropertyChangeEvent evt) {
						if (evt.getPropertyName().equals("state")) {
							if (evt.getNewValue() == SwingWorker.StateValue.DONE) {
								try {
									worker.get();
								} catch (Exception ex) {
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
}
