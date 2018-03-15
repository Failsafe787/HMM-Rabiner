package samples.heater;

public class SampleConfig {

	// Paths
	public static final String baseWinterModelPath = ""; // Base model (the one BW will work on) path (Winter)
	public static final String baseSummerModelPath = ""; // Base model (the one BW will work on) path (Summer)
	public static final String newWinterModelPath = ""; // Path where the new model (after a BW step) is saved (Winter)
	public static final String newSummerModelPath = ""; // Path where the new model (after a BW step) is saved (Summer)
	public static final String winterTrainingPath = ""; // Winter training dataset path
	public static final String summerTrainingPath = ""; // Summer training dataset path
	public static final String winterPruningPath = ""; // Winter pruning dataset path
	public static final String summerPruningPath = ""; // Summer pruning dataset path
	
	// Final ContinuousModel configurations
	public static final int nStatesWinter = 10; // Number of states of the final model (for Winter measurements)
	public static final int nStatesSummer = 10; // Number of states of the final model (for Summer measurements)
	
	// Debug settings
	
}
