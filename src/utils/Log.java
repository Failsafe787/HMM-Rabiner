package utils;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Log {

	private static Logger logger = null;
	
	private static void initialize() { // Singleton class for logger
		logger = Logger.getLogger("BaumWelch");
		FileHandler fh;  
	    try {  
	        fh = new FileHandler("C:\\Users\\Luca\\test.log"); // Save to file
	        logger.addHandler(fh);
	        SimpleFormatter formatter = new SimpleFormatter();  
	        fh.setFormatter(formatter);  
	    } catch (SecurityException e) {  
	        e.printStackTrace();  
	    } catch (IOException e) {  
	        e.printStackTrace();  
	    } 
	}

	public static Logger getLogger() {
		if(logger==null) {
			initialize();
		}
		return logger;
	}

}
