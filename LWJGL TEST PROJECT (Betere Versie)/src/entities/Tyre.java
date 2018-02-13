package entities;

public class Tyre {
	
	public Tyre(double maxBrakingForce) {
		this.maxBrakingForce = maxBrakingForce;
	}
	
	// BRAKING
	
	/**
	 * The braking force applied by this Tyre.
	 */
	private double brakingForce = 0;
	
	/**
	 * Returns the braking force applied by this Tyre.
	 * The braking force is positive and smaller than the maxBrakingForce.
	 */
	public double getBrakingForce() {
		return this.brakingForce;
	}
	
	/**
	 * Sets the braking force of this Tyre.
	 * The braking force is kept between 0 and the maxBrakingForce.
	 */
	public void setBrakingForce(double brakingForce) {
		if (brakingForce <= 0){
			this.brakingForce = 0;
		} else if (brakingForce <= getMaxBrakingForce()) {
			this.brakingForce = brakingForce;
		} else {
			this.brakingForce = getMaxBrakingForce();
		}
	}
	
	// MAX BRAKING FORCE
	
	/**
	 * The maximum braking force this Tyre can apply.
	 */
	private final double maxBrakingForce;
	
	/**
	 * Returns the maximum braking force this Tyre can apply.
	 */
	public double getMaxBrakingForce() {
		return this.maxBrakingForce;
	}
	
	// GROUNDED
	
	/**
	 * Returns whether this Tyre is touching the ground.
	 */
	public boolean isGrounded() {
		// TODO
		return false;
	}

}
