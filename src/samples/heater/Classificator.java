package samples.heater;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Stream;

import baumwelch.BaumWelch;
import baumwelch.ObsSequence;
import exceptions.IllegalADefinitionException;
import exceptions.IllegalBDefinitionException;
import exceptions.IllegalPiDefinitionException;
import exceptions.IllegalStatesNamesSizeException;

public class Classificator {

	public static void main(String[] args) {

	}

	public static void train(String season) {
		// Season path selection
		String datasetPath;
		String basemodelPath;
		String newmodelPath;
		int nStates = 0;
		switch (season) {
		case "Winter": // Winter
			basemodelPath = SampleConfig.baseWinterModelPath;
			newmodelPath = SampleConfig.newWinterModelPath;
			datasetPath = SampleConfig.winterTrainingPath;
			nStates = SampleConfig.nStatesWinter;
			break;
		case "Summer": // Summer
			basemodelPath = SampleConfig.baseSummerModelPath;
			newmodelPath = SampleConfig.newSummerModelPath;
			datasetPath = SampleConfig.summerTrainingPath;
			nStates = SampleConfig.nStatesSummer;
			break;
		default: // Anything else
			throw new IllegalArgumentException("The specified season is invalid");
		}
		ArrayList<ObsSequence> dataset = loadDataset(datasetPath);
		BaumWelch trainer = null;
		try {
			trainer = new BaumWelch(nStates,basemodelPath,dataset,false);
			trainer.step(newmodelPath,false,false);
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
