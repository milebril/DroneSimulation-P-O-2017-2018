package autopilot.algorithmHandler;

import autopilot.algorithms.Algorithm;
import autopilot.algorithms.PathFinder;
import autopilot.algorithms.SpeedUp;
import autopilot.algorithms.Takeoff;
import autopilot.interfaces.Autopilot;
import autopilot.interfaces.AutopilotConfig;
import autopilot.interfaces.AutopilotInputs;
import autopilot.interfaces.AutopilotOutputs;
import autopilot.interfaces.path.MyPath;

public class AutopilotAlain implements Autopilot, AlgorithmHandler {
	
	// Constructor
	
	public AutopilotAlain(Algorithm startingAlgorithm) {
		setAlgorithm(startingAlgorithm);
	}
	
	public AutopilotAlain() {
		// default algoritme
		float[] x = new float[]{ -20,    0};
		float[] y = new float[]{  50,   55};
		float[] z = new float[]{-400, -800};
		MyPath path = new MyPath(x, y, z);
		setAlgorithm(new PathFinder(path));
	}
	
	// AlgorithmHandler interface
	
	private Algorithm algorithm;
	public void setAlgorithm(Algorithm algorithm) {
		this.algorithm = algorithm;
	}
	public Algorithm getAlgorithm() {
		return this.algorithm;
	}
	public String getAlgorithmName() {
		return getAlgorithm().getName();
	}
	
	private float thrust = 0;
	public float getThrust() {
		return thrust;
	}
	public void setThrust(float thrust) {
		if (thrust > getProperties().getMaxThrust()) {
			this.thrust = getProperties().getMaxThrust();
		} else {
			this.thrust = thrust;
		}
	}

	private float leftWingInclination = 0;
	public float getLeftWingInclination() {
		return leftWingInclination;
	}
	public void setLeftWingInclination(float leftWingInclination) {
		this.leftWingInclination = leftWingInclination;
	}

	private float rightWingInclination = 0;
	public float getRightWingInclination() {
		return rightWingInclination;
	}
	public void setRightWingInclination(float rightWingInclination) {
		this.rightWingInclination = rightWingInclination;
	}
	
	private float horStabInclination = 0;
	public float getHorStabInclination() {
		return horStabInclination;
	}
	public void setHorStabInclination(float horStabInclination) {
		this.horStabInclination = horStabInclination;
	}

	private float verStabInclination = 0;
	public float getVerStabInclination() {
		return verStabInclination;
	}
	public void setVerStabInclination(float verStabInclination) {
		this.verStabInclination = verStabInclination;
	}

	private float frontBrakeForce = 0;
	public float getFrontBrakeForce() {
		return frontBrakeForce;
	}
	public void setFrontBrakeForce(float frontBrakeForce) {
		this.frontBrakeForce = frontBrakeForce;
	}

	private float leftBrakeForce = 0;
	public float getLeftBrakeForce() {
		return leftBrakeForce;
	}
	public void setLeftBrakeForce(float leftBrakeForce) {
		this.leftBrakeForce = leftBrakeForce;
	}

	private float rightBrakeForce = 0;
	public float getRightBrakeForce() {
		return rightBrakeForce;
	}
	public void setRightBrakeForce(float rightBrakeForce) {
		this.rightBrakeForce = rightBrakeForce;
	}

	// Autopilot interface
	
	@Override
	public AutopilotOutputs simulationStarted(AutopilotConfig config, AutopilotInputs inputs) {
		
		// create Properties object
		this.properties = new Properties(config, inputs);
		
		// run 1 cycle of the current algorithm
		getAlgorithm().cycle(this);
		
		return this;
	}

	@Override
	public AutopilotOutputs timePassed(AutopilotInputs inputs) {
		// update Properties
		getProperties().update(inputs);
		
		// run 1 cycle of the current algorithm
		getAlgorithm().cycle(this);
		
		return this;
	}

	@Override
	public void simulationEnded() {
	}
	
	// Properties
	
	private Properties properties;
	public Properties getProperties() {
		return this.properties;
	}
		
	
	
	
}
