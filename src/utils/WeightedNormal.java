package utils;

import org.apache.commons.math3.distribution.NormalDistribution;

public class WeightedNormal extends NormalDistribution {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4557537698624936077L;
	private double weight;
	
	public WeightedNormal() {
		this(1.0, 0.0, 1.0);
	}
	
	public WeightedNormal(double weight) {
		this(weight, 0.0, 1.0);
	}

	public WeightedNormal(double mean, double deviation) {
		this(1.0, mean, deviation);
	}

	public WeightedNormal(double weight, double mean, double deviation) {
		super(mean, deviation);
		this.weight = weight;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	@Override
	public String toString() {
		return "G("+ getMean() + "," + getStandardDeviation() +") - W = " + weight;
	}
}
