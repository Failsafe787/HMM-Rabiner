package classifier.core;

import java.awt.EventQueue;

import javax.swing.UIManager;

public class Start {

	public static void main(String[] argv) {
		if (argv.length == 0) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					try {
						UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
						SwingGUI window = new SwingGUI();
						window.frmSimpleHmmSolver.setVisible(true);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		} else {
			help();
		}
	}

	private static void help() {
		System.out.println(Common.APPNAME + " " + " v" + Common.VERSION + " build " + Common.BUILD);
		System.out.println();
		System.out.println("Usage: java -jar <simple-hmm-solver-jar-file>");
		System.out.println();
	}
}
