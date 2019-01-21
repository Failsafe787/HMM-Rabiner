package classifier.core;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Font;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JTextArea;

import classifier.actions.ConsoleSaveLogAction;

import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;

public class Console extends JFrame implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1133135685645716989L;
	JTextArea consoleTextArea;
	JScrollPane consoleScroll;
	JMenuBar menuBar;
	JMenu mnFile;
	JMenuItem mntmSaveLog, mntmClearLog;

	public Console() throws IOException {

		setTitle("Console");
		setSize(500, 530);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setIconImage(ImageIO.read(SwingGUI.class.getResourceAsStream("/hmm.png")));
		getContentPane().setLayout(new GridLayout(1, 0, 0, 0));

		consoleTextArea = new JTextArea();
		consoleTextArea.setFont(new Font("Monospaced", Font.PLAIN, 15));
		consoleTextArea.setEnabled(true);
		consoleTextArea.setEditable(false);
		consoleTextArea.setText(Common.APPNAME + " " + Common.VERSION + " build " + Common.BUILD + "\n");
		consoleTextArea.setMargin(new Insets(10, 10, 10, 10));
		consoleScroll = new JScrollPane(consoleTextArea);
		getContentPane().add(consoleScroll);

		menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		mnFile = new JMenu("File");
		menuBar.add(mnFile);

		mntmSaveLog = new JMenuItem("Save Log");
		mntmClearLog = new JMenuItem("Clear Log");
		mnFile.add(mntmSaveLog);
		mnFile.add(mntmClearLog);

		mntmSaveLog.addActionListener(new ConsoleSaveLogAction(consoleTextArea));
		mntmClearLog.addActionListener(new ClearLog());

		setVisible(true);
	}

	public void addText(String text) {
		consoleTextArea.append("\n" + text);
	}

	private class ClearLog implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			consoleTextArea.setText(Common.APPNAME + " " + Common.VERSION + " build " + Common.BUILD + "\n");
		}
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub

	}
}
