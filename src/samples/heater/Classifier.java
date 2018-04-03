package samples.heater;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import baumwelch.BWContainer;
import baumwelch.BaumWelch;
import baumwelch.ContinuousModel;
import baumwelch.Formula;
import baumwelch.ObsSequence;

public class Classifier {

	private static ArrayList<ObsSequence> ldataset;
	private static ArrayList<ObsSequence> pdataset;
	private static ContinuousModel winterModel;
	private static ContinuousModel summerModel;

	public static void main(String[] args) {
		boolean exit = false;
		Scanner inScanner = new Scanner(System.in);
		while(!exit) {
			System.out.println("\nChoose an activity: ");
			System.out.println("");
			System.out.println("=======> [CLASSIFICATOR] <=======");
			System.out.println("1) Train models");
			System.out.println("2) Load models");
			System.out.println("3) Test sequences");
			System.out.println("");
			System.out.println("=======> [PRE-PROCESSING] <=======");
			System.out.println("4) Rescale data");
			System.out.println("5) Extract data");
			System.out.println("");
			System.out.println("6) Exit");
			System.out.println("");
			String input = inScanner.nextLine();
			int value = 0;
			try {
				value = Integer.parseInt(input);
			}
			catch (NumberFormatException e){
				System.out.println("Insert a digit!");
			}
			switch(value) {
			case 1:
				opt1();
				break;
			case 2:
				opt2();
				break;
			case 3:
				opt3();
				break;
			case 4:
				opt4();
				break;
			case 5:
				opt4();
				break;
			case 6:
				System.out.println("Bye!");
				exit = true;
				break;
			default:
				System.out.println("Invalid choice!");
				break;
			}
		}
		inScanner.close();
		System.exit(0);
	}
	
	public static void opt1() {
		try {
			System.out.println("Training winter models...");
			winterModel = train("Winter");
			System.out.println("Training summer models...");
			summerModel = train("Summer");
		} catch (Exception e) {
			System.out.println("There was an error while training models: " + e.getMessage());
		} 
		
	}
	
	public static void opt2() {
		System.out.println("Unimplemented!");
	}
	
	public static void opt3() {
		if(winterModel==null || summerModel==null) {
			System.out.println("Please train or load the models before starting the classification task");
		}
		else {
			boolean validSequence = false;
			Scanner inScanner = new Scanner(System.in);
			ObsSequence sequence = null;
			while(!validSequence) {
				System.out.println("Please, provide a path to the test sequence: ");
				String input = inScanner.nextLine();
				try {
					sequence = new ObsSequence(input,2);
					validSequence = true;
					classify(sequence);
				} catch (Exception e) {
					System.out.println("There was an error while reading the test sequence: " + e.getMessage());
				}
			}
		}
	}
	
	public static void opt4() {
		System.out.println("Unimplemented!");
	}
	
	public static void opt5() {
		System.out.println("Unimplemented!");
	}
	
	public static ArrayList<ObsSequence> loadDataset(String path) throws FileNotFoundException, IOException, Exception { // Two seasons are available, Summer and Winter
		// Creation of ArrayList<ObsSequence>
		ArrayList<ObsSequence> sequences = new ArrayList<ObsSequence>();
		File[] files = new File(path).listFiles();
		if (files == null) {
			throw new IllegalArgumentException("The specified folder is empty!");
		}
		for (File file : files) {
			if (file.isFile()) {
				sequences.add(new ObsSequence(file.getAbsolutePath(), 2));
			}
		}
		return sequences;
	}

	public static ContinuousModel train(String season) throws FileNotFoundException, IOException, Exception {

		Configuration modelConfig = new Configuration(season);
		ldataset = loadDataset(modelConfig.getLdatasetPath());
		pdataset = loadDataset(modelConfig.getPdatasetPath());
		BaumWelch trainer = null;
		BWContainer container = null;
		ContinuousModel currentModel = null;

		try {
			trainer = new BaumWelch(modelConfig.getnStates(), modelConfig.getBasemodelPath(), ldataset, false);
			boolean convergent = false;
			double currentLikelihood = 0.0;
			while (!convergent) {
				ContinuousModel newModel = trainer.step(modelConfig.getNewmodelPath(), false, false);
				double newLikelihood = 1.0;
				for (ObsSequence sequence : pdataset) {
					container = new BWContainer(newModel.getNumberOfStates(), sequence.size());
					Formula.alpha(newModel, container, sequence, false, false);
					newLikelihood *= container.getAlphaValue();
				}
				if (trainer.getCurrentRound() == 1) {
					currentLikelihood = newLikelihood;
				} else if (newLikelihood < currentLikelihood) {
					convergent = true;
				} else {
					currentModel = newModel;
					currentLikelihood = newLikelihood;
				}
			}

		} catch (Exception e) {
			System.out.println("An error has occoured: " + e.getMessage());
		}

		return currentModel;
	}
	
	public static void classify(ObsSequence sequence) {
		BWContainer winterContainer = new BWContainer(winterModel.getNumberOfStates(), sequence.size());
		BWContainer summerContainer = new BWContainer(summerModel.getNumberOfStates(), sequence.size());
		Formula.alpha(winterModel, winterContainer, sequence, true, false);
		Formula.alpha(summerModel, summerContainer, sequence, true, false);
		System.out.println("Likelihood with winter model: " + winterContainer.getAlphaValue());
		System.out.println("Likelihood with summer model: " + summerContainer.getAlphaValue());
	}

}
