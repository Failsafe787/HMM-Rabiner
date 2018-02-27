/*
 * Released under MIT License (Expat)
 * @author Luca Banzato (Java rewritten code of a C++ rescaler given by our professor)
 * @version 0.1.8
 */

package samples.heater;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Rescale {

	public static int run(String inpath, String outpath, int n) {
		ArrayList<EngSlot> buffer = new ArrayList<EngSlot>();
		BufferedReader br = null;
		BufferedWriter bw = null;
		try {
			br = new BufferedReader(new FileReader(inpath));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		String line;
		try {
			while ((line = br.readLine()) != null) {
				buffer.add(new EngSlot(line));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			bw = new BufferedWriter(new FileWriter(outpath));
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (int i = 0; i < buffer.size(); i = i + n) {
			try {
				bw.write(accumulate(buffer, i, n).print() + "\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public static EngSlot accumulate(ArrayList<EngSlot> data, int start, int n) {
		EngSlot res = new EngSlot();
		for (int i = start; i < start + n && i < data.size(); i++) {
			res.power += data.get(i).power;
		}
		res.sec += data.get(start).sec;
		res.power /= (data.size() - start < n ? data.size() - start : n);
		return res;
	}

	static class EngSlot {

		public int sec;
		public double power;

		public EngSlot() {
			sec = 0;
			power = 0.0;
		}

		public EngSlot(String in) {
			read(in);
		}

		public boolean read(String in) {
			String[] splitted = in.split(" ");
			sec = Integer.parseInt(splitted[0]);
			power = Double.parseDouble(splitted[1]);
			return true;
		}

		public String print() {
			return power + "";
		}
	}
}
