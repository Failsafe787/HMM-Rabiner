package samples.heater;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Stream;

import baumwelch.BWContainer;
import baumwelch.BaumWelch;
import baumwelch.ContinuousModel;
import baumwelch.Formula;
import baumwelch.ObsSequence;
import exceptions.IllegalADefinitionException;
import exceptions.IllegalBDefinitionException;
import exceptions.IllegalPiDefinitionException;
import exceptions.IllegalStatesNamesSizeException;

public class Classifier {

	public static void main(String[] args) {

	}

	public static void train(String season) {
		// Season path selection
		String ldatasetPath;
		String pdatasetPath;
		String basemodelPath;
		String newmodelPath;
		int nStates = 0;
		switch (season) {
		case "Winter": // Winter
			basemodelPath = SampleConfig.baseWinterModelPath;
			newmodelPath = SampleConfig.newWinterModelPath;
			ldatasetPath = SampleConfig.winterTrainingPath;
			pdatasetPath = SampleConfig.winterPruningPath;
			nStates = SampleConfig.nStatesWinter;
			break;
		case "Summer": // Summer
			basemodelPath = SampleConfig.baseSummerModelPath;
			newmodelPath = SampleConfig.newSummerModelPath;
			ldatasetPath = SampleConfig.summerTrainingPath;
			pdatasetPath = SampleConfig.summerPruningPath;
			nStates = SampleConfig.nStatesSummer;
			break;
		default: // Anything else
			throw new IllegalArgumentException("The specified season is invalid");
		}
		ArrayList<ObsSequence> ldataset = loadDataset(ldatasetPath);
		ArrayList<ObsSequence> pdataset = loadDataset(pdatasetPath);
		BaumWelch trainer = null;
		BWContainer container = null;
		ContinuousModel finalModel = null;
		try {
			trainer = new BaumWelch(nStates,basemodelPath,ldataset,false);
			finalModel = trainer.step(newmodelPath,false,false);
			// container = new BWContainer(finalModel.getNumberOfStates(),pdataset.get(1).size());
			// Formula.alpha(finalModel, container, , false, false);
			
		} catch (IllegalPiDefinitionException e) {
			System.out.println("An error has occoured: " + e.getMessage());
		} catch (IllegalADefinitionException e) {
			System.out.println("An error has occoured: " + e.getMessage());
		} catch (IllegalBDefinitionException e) {
			System.out.println("An error has occoured: " + e.getMessage());
		} catch (IllegalStatesNamesSizeException e) {
			System.out.println("An error has occoured: " + e.getMessage());
		}
	}
	
	

	public static ArrayList<ObsSequence> loadDataset(String path) { // Two seasons are available, Summer and Winter
		// Creation of ArrayList<ObsSequence>
		ArrayList<ObsSequence> sequences = new ArrayList<ObsSequence>();
		File[] files = new File(path).listFiles();
		if (files == null) {
			throw new IllegalArgumentException("The specified folder is empty");
		}
		for (File file : files) {
			if (file.isFile()) {
				sequences.add(new ObsSequence(file.getAbsolutePath(), 2));
			}
		}
		return sequences;
	}

}
