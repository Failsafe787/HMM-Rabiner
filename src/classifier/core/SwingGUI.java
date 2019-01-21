package classifier.core;

import java.awt.event.ActionEvent;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JTabbedPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JLabel;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.NumberFormatter;

import classifier.actions.*;

import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

public class SwingGUI {

	// GUI Elements
	JFrame frmSimpleHmmSolver;
	JTabbedPane tabbedPane;
	JPanel classifyTab, preprocessTab;
	Console console;

	// Classify TAB
	JButton btnLoadModel, btnRenameModel, btnRemoveModel, btnLoadDataset, btnRemoveDataset, btnTrainModel, btnSaveModel,
			btnValidateClassifier, btnTestSequence;
	JLabel lblLoadedModels, lblAssociatedDataset, lblDatasetPath;
	JPanel modelPropPanel;
	DefaultListModel<String> listModel;
	JList<String> list;
	JScrollPane listScrollPane;

	// Preprocess TAB
	JButton btnSelectSrcDataset, btnRemoveSrcDataset, btnSelectDestDataset, btnRemoveDestDataset, btnRescaleDataset;
	JLabel lblSelectedDataset, lblSourceDirectory, lblOutputDirectory, lblDestDirectory, lblMergeFactor;
	JPanel mergePropPanel;
	JSeparator separator;
	JFormattedTextField txtfldMergeFactor;

	/**
	 * Creates the application
	 * 
	 * @throws IOException
	 */
	public SwingGUI() throws IOException {
		initialize();
	}

	/**
	 * Initialize the contents of the frame
	 * 
	 * @throws IOException if there's an error while loading Resource files
	 */
	private void initialize() throws IOException {

		// Main Frame
		frmSimpleHmmSolver = new JFrame();
		frmSimpleHmmSolver.setIconImage(ImageIO.read(SwingGUI.class.getResourceAsStream("/hmm.png")));
		frmSimpleHmmSolver.setResizable(false);
		frmSimpleHmmSolver.setTitle(Common.APPNAME + " " + Common.VERSION);
		frmSimpleHmmSolver.setBounds(100, 100, 850, 524);
		frmSimpleHmmSolver.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmSimpleHmmSolver.getContentPane().setLayout(new GridLayout(1, 0, 0, 0));

		// Console creation
		console = new Console();
		console.setLocation(frmSimpleHmmSolver.getX() + 860, frmSimpleHmmSolver.getY());

		// Add Tabbed Pane
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		frmSimpleHmmSolver.getContentPane().add(tabbedPane);

		// Classify Tab Creation
		buildClassifyTab();

		// Preprocess Tab Creation
		buildPreprocessTab();

		// Handle window movement adapter
		frmSimpleHmmSolver.addComponentListener(new ComponentAdapter() {
			Point lastLocation;

			@Override
			public void componentMoved(ComponentEvent e) {
				if (lastLocation == null && frmSimpleHmmSolver.isVisible()) {
					lastLocation = frmSimpleHmmSolver.getLocation();
				} else {
					Point newLocation = frmSimpleHmmSolver.getLocation();
					int dx = newLocation.x - lastLocation.x;
					int dy = newLocation.y - lastLocation.y;
					console.setLocation(console.getX() + dx, console.getY() + dy);
					lastLocation = newLocation;
				}
			}
		});
	}

	private void buildClassifyTab() {

		// Tab
		classifyTab = new JPanel();
		classifyTab.setLayout(null);

		// "Loaded models" label
		lblLoadedModels = new JLabel("Loaded models");
		lblLoadedModels.setBounds(10, 11, 120, 14);

		// Open model button
		btnLoadModel = new JButton("Open");
		btnLoadModel.setBounds(131, 7, 103, 23);

		// Rename model button
		btnRenameModel = new JButton("Rename");
		btnRenameModel.setBounds(244, 7, 103, 23);
		btnRenameModel.setEnabled(false);

		// Remove model button
		btnRemoveModel = new JButton("Remove");
		btnRemoveModel.setBounds(357, 7, 103, 23);
		btnRemoveModel.setEnabled(false);

		// Models list + scroll
		listModel = new DefaultListModel<>();
		list = new JList<>(listModel);
		list.setBounds(10, 42, 459, 420);
		listScrollPane = new JScrollPane(list);
		listScrollPane.setBounds(10, 41, 450, 410);

		// Model properties panel
		modelPropPanel = new JPanel();
		modelPropPanel.setBorder(new LineBorder(new Color(192, 192, 192), 1, true));
		modelPropPanel.setBounds(470, 7, 351, 196);
		modelPropPanel.setLayout(null);

		// Associated dataset label
		lblAssociatedDataset = new JLabel("Associated dataset");
		lblAssociatedDataset.setEnabled(false);
		lblAssociatedDataset.setBounds(10, 11, 115, 26);

		// Load Dataset button
		btnLoadDataset = new JButton("Load");
		btnLoadDataset.setBounds(166, 13, 76, 23);
		btnLoadDataset.setEnabled(false);

		// Remove Dataset button
		btnRemoveDataset = new JButton("Remove");
		btnRemoveDataset.setBounds(252, 13, 89, 23);
		btnRemoveDataset.setEnabled(false);

		// Dataset path label
		lblDatasetPath = new JLabel("");
		lblDatasetPath.setBounds(10, 67, 313, 26);
		lblDatasetPath.setEnabled(false);

		// Train Model button
		btnTrainModel = new JButton("Train Model");
		btnTrainModel.setBounds(10, 127, 165, 58);
		btnTrainModel.setEnabled(false);

		// Save Model button
		btnSaveModel = new JButton("Save Model");
		btnSaveModel.setBounds(185, 127, 156, 58);
		btnSaveModel.setEnabled(false);

		// Validate models button
		btnValidateClassifier = new JButton("Validate Classifier (Experimental, only binary)");
		btnValidateClassifier.setBounds(493, 280, 313, 58);
		btnValidateClassifier.setEnabled(false);

		// Test sequence button
		btnTestSequence = new JButton("Test sequence");
		btnTestSequence.setBounds(493, 349, 313, 58);
		btnTestSequence.setEnabled(false);

		// Add items to Model properties panel
		modelPropPanel.add(lblAssociatedDataset);
		modelPropPanel.add(btnLoadDataset);
		modelPropPanel.add(btnRemoveDataset);
		modelPropPanel.add(lblDatasetPath);
		modelPropPanel.add(btnTrainModel);
		modelPropPanel.add(btnSaveModel);

		// Add Actions to Buttons
		btnLoadModel.addActionListener(new LoadModelAction(listModel, btnTestSequence, btnValidateClassifier, console));
		btnRenameModel.addActionListener(new RenameModel());
		btnRemoveModel.addActionListener(new RemoveModel());
		list.addListSelectionListener(new SelectItem());
		btnLoadDataset.addActionListener(
				new LoadDatasetAction(list, console, lblDatasetPath, btnRemoveDataset, btnTrainModel, btnValidateClassifier));
		btnRemoveDataset.addActionListener(new RemoveDataset());
		btnTrainModel.addActionListener(new TrainModelAction(list, console));
		btnSaveModel.addActionListener(new SaveModelAction(list, console));
		btnValidateClassifier.addActionListener(new ValidateClassifierAction(console));
		btnTestSequence.addActionListener(new TestSequenceAction(console));

		// Add Components to the tab
		classifyTab.add(lblLoadedModels);
		classifyTab.add(btnLoadModel);
		classifyTab.add(btnRenameModel);
		classifyTab.add(btnRemoveModel);
		classifyTab.add(listScrollPane);
		classifyTab.add(modelPropPanel);
		classifyTab.add(btnValidateClassifier);
		classifyTab.add(btnTestSequence);

		// Add Tab to the main window
		tabbedPane.addTab("Classify", null, classifyTab, null);
	}

	private void buildPreprocessTab() {

		// Tab
		preprocessTab = new JPanel();
		preprocessTab.setLayout(null);

		// Preprocess dataset properties panel
		mergePropPanel = new JPanel();
		mergePropPanel.setLayout(null);
		mergePropPanel.setBorder(new LineBorder(new Color(192, 192, 192), 1, true));
		mergePropPanel.setBounds(10, 11, 819, 214);

		// Selected dataset label
		lblSelectedDataset = new JLabel("Selected dataset");
		lblSelectedDataset.setBounds(10, 11, 115, 26);
		lblSelectedDataset.setEnabled(true);

		// Current source directory label
		lblSourceDirectory = new JLabel("No source directory");
		lblSourceDirectory.setBounds(10, 59, 313, 26);
		lblSourceDirectory.setEnabled(true);

		// Select source dataset button
		btnSelectSrcDataset = new JButton("Select");
		btnSelectSrcDataset.setBounds(225, 13, 76, 23);
		btnSelectSrcDataset.setEnabled(true);

		// Remove source dataset button
		btnRemoveSrcDataset = new JButton("Remove");
		btnRemoveSrcDataset.setBounds(311, 13, 89, 23);
		btnRemoveSrcDataset.setEnabled(false);

		// Output directory dataset label
		lblOutputDirectory = new JLabel("Output directory");
		lblOutputDirectory.setBounds(10, 104, 115, 26);
		lblOutputDirectory.setEnabled(true);

		// Select output directory dataset button
		btnSelectDestDataset = new JButton("Select");
		btnSelectDestDataset.setBounds(225, 106, 76, 23);
		btnSelectDestDataset.setEnabled(true);

		// Remove output path dataset button
		btnRemoveDestDataset = new JButton("Remove");
		btnRemoveDestDataset.setBounds(311, 106, 89, 23);
		btnRemoveDestDataset.setEnabled(false);

		// Current output dataset label
		lblDestDirectory = new JLabel("No output directory");
		lblDestDirectory.setBounds(10, 150, 313, 26);
		lblDestDirectory.setEnabled(true);

		// Separator
		separator = new JSeparator();
		separator.setOrientation(SwingConstants.VERTICAL);
		separator.setForeground(Color.LIGHT_GRAY);
		separator.setBounds(410, 0, 2, 214);

		// Merge factor label
		lblMergeFactor = new JLabel("Merge Factor");
		lblMergeFactor.setBounds(431, 11, 115, 26);
		lblMergeFactor.setEnabled(true);

		// Merge factor input field
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
		txtfldMergeFactor = new JFormattedTextField(formatter);
		txtfldMergeFactor.setBounds(653, 14, 156, 20);
		txtfldMergeFactor.setText("10");
		txtfldMergeFactor.setEnabled(true);
		txtfldMergeFactor.setEditable(true);
		txtfldMergeFactor.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				txtfldMergeFactor.selectAll();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				txtfldMergeFactor.selectAll();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				// nothing to do..
			}
		});

		// Rescale dataset button
		btnRescaleDataset = new JButton("Rescale Dataset");
		btnRescaleDataset.setBounds(543, 88, 156, 58);
		btnRescaleDataset.setEnabled(false);

		// Add items to Preprocess dataset properties panel
		mergePropPanel.add(lblSourceDirectory);
		mergePropPanel.add(btnSelectSrcDataset);
		mergePropPanel.add(btnRemoveSrcDataset);
		mergePropPanel.add(lblSelectedDataset);
		mergePropPanel.add(lblOutputDirectory);
		mergePropPanel.add(btnSelectDestDataset);
		mergePropPanel.add(btnRemoveDestDataset);
		mergePropPanel.add(lblDestDirectory);
		mergePropPanel.add(separator);
		mergePropPanel.add(lblMergeFactor);
		mergePropPanel.add(txtfldMergeFactor);
		mergePropPanel.add(btnRescaleDataset);

		// Add actions to the buttons
		btnSelectSrcDataset.addActionListener(new SelectSourceDirectory());
		btnSelectDestDataset.addActionListener(new SelectOutputDirectory());
		btnRemoveSrcDataset.addActionListener(new RemoveSourceDirectory());
		btnRemoveDestDataset.addActionListener(new RemoveDestDirectory());
		btnRescaleDataset.addActionListener(new PreprocessDatasetAction(console, txtfldMergeFactor));
		// Add Components to the tab
		preprocessTab.add(mergePropPanel);

		// Add Tab to the main window
		tabbedPane.addTab("Preprocess", null, preprocessTab, null);

	}

	// Classify Tab - Action Listeners
	private class RemoveModel implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			int index = list.getSelectedIndex();
			listModel.removeElementAt(index);
			synchronized (Common.loaded) {
				if(Common.loaded.get(index).dataset!=null) {
					Common.datasetsLoaded--;
				}
				Common.modelsLoaded--;
				Common.loaded.remove(index);
				if(Common.modelsLoaded > 1 && Common.modelsLoaded==Common.datasetsLoaded) {
					btnValidateClassifier.setEnabled(true);
				}
				else {
					btnValidateClassifier.setEnabled(false);
				}
			}
			btnRemoveModel.setEnabled(false);
			btnRenameModel.setEnabled(false);
			btnTrainModel.setEnabled(false);
			lblAssociatedDataset.setEnabled(false);
			lblDatasetPath.setEnabled(false);
			btnLoadDataset.setEnabled(false);
			btnRemoveDataset.setEnabled(false);
			btnSaveModel.setEnabled(false);
			lblDatasetPath.setText("");
			if (listModel.size() == 0) {
				btnTestSequence.setEnabled(false);
			}
		}
	}

	private class RenameModel implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			try {
				int index = list.getSelectedIndex();
				String newName = JOptionPane.showInputDialog(null, "Please enter a new name for this model");
				if (newName != null) {
					Common.renameModel(newName, index);
					listModel.set(index, newName);
				}
			} catch (Exception ex) {
				displayError(ex.getMessage());
			}
		}
	}

	private class RemoveDataset implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			synchronized (Common.loaded) {
				Common.loaded.get(list.getSelectedIndex()).dataset = null;
				Common.loaded.get(list.getSelectedIndex()).datasetDirectory = null;
				Common.datasetsLoaded--;
			}
			btnRemoveDataset.setEnabled(false);
			btnValidateClassifier.setEnabled(false);
			btnTrainModel.setEnabled(false);
			lblDatasetPath.setText("No dataset loaded");
		}
	}

	private class SelectItem implements ListSelectionListener {

		@Override
		public void valueChanged(ListSelectionEvent arg0) {
			if (!arg0.getValueIsAdjusting()) {

			} else {
				int index = list.getSelectedIndex();
				btnRemoveModel.setEnabled(true);
				btnRenameModel.setEnabled(true);
				btnSaveModel.setEnabled(true);
				lblAssociatedDataset.setEnabled(true);
				lblDatasetPath.setEnabled(true);
				btnLoadDataset.setEnabled(true);
				synchronized (Common.loaded) {
					if (Common.loaded.get(index).datasetDirectory != null) {
						lblDatasetPath.setText(Common.loaded.get(index).datasetDirectory.getAbsolutePath());
						btnRemoveDataset.setEnabled(true);
						btnTrainModel.setEnabled(true);
					} else {
						lblDatasetPath.setText("No dataset loaded");
						btnRemoveDataset.setEnabled(false);
						btnTrainModel.setEnabled(false);
					}
				}
			}
		}
	}

	// Preprocess Tab - Action Listeners

	private class SelectSourceDirectory implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setDialogTitle("Select Source Dataset Index");
			fileChooser.setApproveButtonText("Select");
			fileChooser.setFileFilter(new TxtFileFilter());
			int n = fileChooser.showOpenDialog(null);
			if (n == JFileChooser.APPROVE_OPTION) {
				final File datasetDir = fileChooser.getSelectedFile();
				synchronized (Common.preprocessInIndex) {
					Common.preprocessInIndex = datasetDir;
				}
				lblSourceDirectory.setText(datasetDir.getAbsolutePath());
				btnRemoveSrcDataset.setEnabled(true);
				if (!lblDestDirectory.getText().equals("No output directory")) {
					btnRescaleDataset.setEnabled(true);
				}
			}
		}
	}

	private class RemoveSourceDirectory implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			synchronized (Common.preprocessInIndex) {
				Common.preprocessInIndex = new File("");
			}
			if (btnRescaleDataset.isEnabled()) {
				btnRescaleDataset.setEnabled(false);
			}
			lblSourceDirectory.setText("No source directory");
			btnRemoveSrcDataset.setEnabled(false);
		}
	}

	private class RemoveDestDirectory implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			synchronized (Common.preprocessOutFolder) {
				Common.preprocessOutFolder = new File("");
			}
			if (btnRescaleDataset.isEnabled()) {
				btnRescaleDataset.setEnabled(false);
			}
			lblDestDirectory.setText("No output directory");
			btnRemoveDestDataset.setEnabled(false);
		}
	}

	private class SelectOutputDirectory implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser dirChooser = new JFileChooser();
			dirChooser.setDialogTitle("Select Output Dataset Directory");
			dirChooser.setApproveButtonText("Select");
			dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int n = dirChooser.showOpenDialog(null);
			if (n == JFileChooser.APPROVE_OPTION) {
				final File datasetDir = dirChooser.getSelectedFile();
				synchronized (Common.preprocessOutFolder) {
					Common.preprocessOutFolder = datasetDir;
				}
				lblDestDirectory.setText(datasetDir.getAbsolutePath());
				btnRemoveDestDataset.setEnabled(true);
				if (!lblSourceDirectory.getText().equals("No source directory")) {
					btnRescaleDataset.setEnabled(true);
				}
			}
		}
	}

	// Display error dialogs
	private static void displayError(String message) {
		JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
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
