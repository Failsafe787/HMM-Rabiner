package utils;

public class GaussianCurve {

	private double mu;
	private double sigma;

	public GaussianCurve() { // Standard gaussian curve
		mu = 0.0;
		sigma = 1.0;
	}

	public GaussianCurve(double mu, double sigma) { // Gaussian curve with provided values
		if (mu < 0.0) {
			throw new IllegalArgumentException("Mu cannot be less than 0.0"); // Check if mu is less than 0
		}
		this.mu = mu;
		if (Double.compare(sigma, 0.0) < 0 || Double.compare(sigma, 0.0) == 0) {
			throw new IllegalArgumentException("Sigma cannot be less or equal to 0.0"); // Check if sigma is less or
																						// equal to 0
		}
		this.sigma = sigma;
	}

	public double fi(double x) { // Calculate the probability density using an observation x
		double fraction = 1.0 / (sigma * Math.sqrt(2.0 * Math.PI));
		double exponent = -((x - mu) * (x - mu)) / (2 * sigma * sigma);
		return fraction * Math.pow(Math.E, exponent);
	}

	public double getMu() { // get current mu
		return mu;
	}

	public double getSigma() { // get current sigma
		return sigma;
	}

	public void setMu(double mu) { // set current mu to the one provided
		if (Double.compare(mu, 0.0) < 0) {
			throw new IllegalArgumentException("Mu cannot be less than 0.0"); // Check if mu is less than 0
		}
		this.mu = mu;
	}

	public void setSigma(double sigma) { // set current sigma to the one provided
		if (Double.compare(sigma, 0.0) < 0 || Double.compare(sigma, 0.0) == 0) {
			throw new IllegalArgumentException("Sigma cannot be less or equal to 0.0"); // Check if sigma is less or
																						// equal to 0
		}
		this.sigma = sigma;
	}

	@Override
	public String toString() { // Returns a printable representation of this curve in the format: G(mu,sigma)
		return "G(" + mu + "," + sigma + ")";
	}
}
