package classifier.core;

import java.io.File;
import java.util.ArrayList;

import struct.Struct_HMMDataset;

public class Common {

	public static final String APPNAME = "Continuous HMM Solver";
	public static final String VERSION = "1.0";
	public static final String BUILD = "20190113-180921";
	public static ArrayList<Struct_HMMDataset> loaded = new ArrayList<Struct_HMMDataset>();
	public static int modelsLoaded;
	public static int datasetsLoaded;
	public static File preprocessInIndex = new File("");
	public static File preprocessOutFolder = new File("");

	public static void renameModel(String newName, int index) {
		if (newName != null) {
			if (newName.equals("")) {
				throw new IllegalArgumentException("Invalid name! (Empty name)");
			} else if (newName.equals("") || newName.matches("(\\t|\\s)*")) {
				throw new IllegalArgumentException("Invalid name! (Cannot use spaces and/or tabs as name)");
			}
			loaded.get(index).hmm.setModelName(newName);
		} else {
			throw new IllegalArgumentException("Invalid name! (name is null)");
		}
	}

	public static String printResults(int index) {
		return loaded.get(index).hmm.toString();
	}
}
