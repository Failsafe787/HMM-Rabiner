package utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SysUtils {

	// Thanks to mkyong for the OS deteection tip!
	// https://www.mkyong.com/java/how-to-detect-os-in-java-systemgetpropertyosname/
	private static boolean isWindows = System.getProperty("os.name").toLowerCase().indexOf("win") >= 0;

	/**
	 * Returns a string which represents an absolute path of a file
	 *
	 * @param directory an absolute path of a directory
	 * @param fileName  a file name inside this directory
	 * @return an absolute path of a file
	 */
	public static String osFilePath(String directory, String fileName) {
		if (isWindows) { // Windows Environment
			return directory + "\\" + fileName;
		} else {
			return directory + "/" + fileName;
		}
	}

	/**
	 * Returns a string with each word separated by a single tab (\t) starting from
	 * a string with each word separated by spaces (\t) or multiple tabs (\t+)
	 *
	 * @param string a string with words separated by one or more \s or \t
	 * @return the formatted string
	 */
	public static String formatString(String string) {
		return string.replaceAll("\\s", "\t").trim().replaceAll("\t+", "\t");
	}
	
	public static String currentTime() {
		Date currentTime = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
		return dateFormat.format(currentTime);
	}
}
