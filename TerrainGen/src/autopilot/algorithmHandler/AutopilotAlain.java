package autopilot.algorithmHandler;

import java.util.LinkedList;
import java.util.NoSuchElementException;

import org.lwjgl.util.vector.Vector3f;

import autopilot.algorithms.*;
import autopilot.interfaces.Autopilot;
import autopilot.interfaces.AutopilotConfig;
import autopilot.interfaces.AutopilotInputs;
import autopilot.interfaces.AutopilotOutputs;
import autopilot.interfaces.path.MyPath;

public class AutopilotAlain implements Autopilot, AlgorithmHandler {

	// Constructor

	public static float CRUISESPEED = 40;

	public boolean crashed = false;

	private long time;
	private float startZ;
	public long timeToLand;
	public float lengthToLand;
	private float groundTouchStart;
	public float groundLength;

	public AutopilotAlain(Algorithm startingAlgorithm) {
		setAlgorithm(startingAlgorithm);
	}

	public AutopilotAlain() {
		// add algorithms in order
		// addAlgorithm(new Aanloop(34f, 15f));
		// addAlgorithm(new FlyToHeight(20f));
		//// addAlgorithm(new FlyToHeight(10f));
		// addAlgorithm(new Land());

		// start 1st algorithm
		// nextAlgorithm();
	}

	private LinkedList<Algorithm> algorithmList = new LinkedList<Algorithm>();

	public void addAlgorithm(Algorithm a) {
		algorithmList.add(a);
	}

	public void nextAlgorithm() {
		try {
			setAlgorithm(algorithmList.pop());
		} catch (NoSuchElementException e) {
			setAlgorithm(new VliegRechtdoor());
		}

		if (algorithm instanceof Land) {
			startZ = getProperties().getPosition().z;
			time = System.currentTimeMillis();
		}

		if (getAlgorithm() == null) {
			setAlgorithm(new VliegRechtdoor());
		}
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
		if (algorithm != null) {
			return getAlgorithm().getName();
		} else {
			return "wait";
		}
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
	public float lenghtOnGround;

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
		properties.setCruiseHeight(cruiseHeight);

		// run 1 cycle of the current algorithm
		if (algorithm != null)
			getAlgorithm().cycle(this);

		return this;
	}

	@Override
	public AutopilotOutputs timePassed(AutopilotInputs inputs) {
		// check if new algorithm entered queue
		if (algorithm instanceof VliegRechtdoor || algorithm == null) {
			nextAlgorithm();
		}

		// update Properties
		getProperties().update(inputs);
		// run 1 cycle of the current algorithm
		if (algorithm != null)
			getAlgorithm().cycle(this);

		if (getProperties().getPosition().getY() < 2.5f && groundTouchStart == 0) {
			groundTouchStart = getProperties().getPosition().getZ();
		}

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

	public void startTimeHasPassed(AutopilotInputs inputs) {
		getProperties().update(inputs);
	}

	public AutopilotOutputs completeTimeHasPassed() {
		getAlgorithm().cycle(this);
		return this;
	}

	public boolean isFinished() {
		if (algorithm instanceof VliegRechtdoor) {
			timeToLand = System.currentTimeMillis() - time;
			lengthToLand = Math.abs(getProperties().getPosition().z - startZ);
			groundLength = Math.abs(getProperties().getPosition().z - groundTouchStart);
			return true;
		} else {
			return false;
		}
	}

	private float cruiseHeight;
	
	public void setCruiseHeight(int i) {
		cruiseHeight = i;
	}

	public float getCruiseHeight() {
		return cruiseHeight;
	}
}
