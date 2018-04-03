package samples.heater;

public class Configuration {

	private String ldatasetPath;
	private String pdatasetPath;
	private String basemodelPath;
	private String newmodelPath;
	private int nStates = 0;

	public Configuration(String season) {
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
			throw new IllegalArgumentException("The specified season is invalid: " + season);
		}
	}

	public String getLdatasetPath() {
		return ldatasetPath;
	}

	public String getPdatasetPath() {
		return pdatasetPath;
	}

	public String getBasemodelPath() {
		return basemodelPath;
	}

	public String getNewmodelPath() {
		return newmodelPath;
	}

	public int getnStates() {
		return nStates;
	}

}
