package classifier.actions;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.text.ParseException;

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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.NumberFormatter;
import classifier.core.Common;
import classifier.core.Console;
import classifier.workers.ValidateClassifierWorker;

public class ValidateClassifierAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3891044661472141018L;
	protected Console console;

	public ValidateClassifierAction(Console console) {
		this.console = console;
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		synchronized (Common.loaded) {
			if (Common.modelsLoaded > 2) {
				JOptionPane.showMessageDialog(null,
						"Can only perform validations on a binary classifier at the moment!");
			} else if (Common.modelsLoaded == 2) {
				try {
					JPanel optionsPanel = new JPanel(new GridBagLayout()); // FlowLayout
					GridBagConstraints c = new GridBagConstraints();

					c.fill = GridBagConstraints.HORIZONTAL;
					c.weightx = 0.5;
					c.gridx = 0;
					c.gridy = 0;
					optionsPanel.add(new JLabel("Number of folds for Cross-Validation"), c);

					NumberFormat format = NumberFormat.getInstance();
					NumberFormatter formatter = new NumberFormatter(format) {
						/**
						 * 
						 */
						private static final long serialVersionUID = -927292327813026204L;

						@Override
						public Object stringToValue(String string) throws ParseException {
							if (string == null || string.length() == 0) {
								return null;
							}
							return super.stringToValue(string);
						}
					};
					formatter.setValueClass(Integer.class);
					formatter.setMinimum(0);
					formatter.setMaximum(Integer.MAX_VALUE);
					formatter.setAllowsInvalid(false);
					formatter.setCommitsOnValidEdit(true);
					JFormattedTextField iterations = new JFormattedTextField(formatter) {
						/**
						 * 
						 */
						private static final long serialVersionUID = 4365656020045455764L;

						@Override
						public Dimension getPreferredSize() {
							return new Dimension(50, 25);
						}
					};
					iterations.setText("10");
					iterations.setEditable(true);
					iterations.getDocument().addDocumentListener(new DocumentListener() {
						@Override
						public void removeUpdate(DocumentEvent e) {
							iterations.selectAll();
						}

						@Override
						public void insertUpdate(DocumentEvent e) {
							iterations.selectAll();
						}

						@Override
						public void changedUpdate(DocumentEvent e) {
							// nothing to do..
						}

					});
					c.insets = new Insets(0, 10, 0, 0); // top padding
					c.fill = GridBagConstraints.HORIZONTAL;
					c.weightx = 0.5;
					c.gridx = 1;
					c.gridy = 0;
					optionsPanel.add(iterations, c);

					// Custom button text
					Object[] options = { "Start", "Abort" };
					int choice = JOptionPane.showOptionDialog(null, optionsPanel, "Train settings",
							JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
					if (choice == JOptionPane.YES_OPTION) {
						ValidateClassifierWorker worker = new ValidateClassifierWorker(console, Integer.parseInt(iterations.getText()));
						console.addText("Validate classifier performance, it may takes a while...");
						Window win = SwingUtilities.getWindowAncestor((AbstractButton) evt.getSource());
						JDialog dialog = new JDialog(win, "Dialog", ModalityType.APPLICATION_MODAL);
						worker.addPropertyChangeListener(new PropertyChangeListener() {
							@Override
							public void propertyChange(PropertyChangeEvent evt) {
								if (evt.getPropertyName().equals("state")) {
									if (evt.getNewValue() == SwingWorker.StateValue.DONE) {
										try {
											worker.get();
											console.addText("Validation session ended successfully!");
										} catch (Exception ex) {
											console.addText("There was an error while validating this classifier: "
													+ ex.getCause().getMessage());
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
				} catch (NumberFormatException nex) {
					JOptionPane.showMessageDialog(null, "Please specify a valid number of folds!", "Error",
							JOptionPane.ERROR_MESSAGE);
				}

				catch (Exception ex) {
					console.addText("Error while validating the classifier: " + ex.getMessage());
					JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}
}