/*
 * Released under MIT License (Expat)
 * @author Luca Banzato
 * @version 0.1.8
 */

package utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Log {

	private static Logger logger = null;
	private final static String path = "C:\\Users\\Luca\\Desktop\\BW Logs\\";

	private static void initialize() { // Singleton class for logger
		logger = Logger.getLogger("BaumWelch");
		FileHandler fh;
		int nTries = 0;
		boolean error = false;
		while (nTries < 2 && !error) {
			try {
				fh = new FileHandler(path + "BaumWelch.log"); // Save to file
				logger.addHandler(fh);
				logger.setUseParentHandlers(false);
				BriefFormatter formatter = new BriefFormatter();
				fh.setFormatter(formatter);
			} catch (SecurityException e) {
				error = true;
				System.out
						.println("WARNING: cannot save log file in the specified location due to permission problems: "
								+ e.getMessage());
			} catch (NoSuchFileException e) {
				try {
					Files.createDirectories(Paths.get(path));
				} catch (IOException e1) {
					error = true;
					System.out.println("WARNING: cannot save log file in the specified location due to IO problems: "
							+ e1.getMessage());
				}
			} catch (IOException e) {
				error = true;
				System.out.println("WARNING: cannot save log file in the specified location due to IO problems: "
						+ e.getMessage());
			}
			nTries++;
		}
	}

	public static Logger getLogger() {
		if (logger == null) {
			initialize();
		}
		return logger;
	}

	// Thanks to Jarrod Roberson @ Stack Overflow for the tip about BriefFormatter
	public static class BriefFormatter extends Formatter {
		public BriefFormatter() {
			super();
		}

		@Override
		public String format(final LogRecord record) {
			return record.getMessage() + "\n";
		}
	}

}
