package autopilot.algorithmHandler;

import java.util.LinkedList;
import java.util.NoSuchElementException;

import org.lwjgl.util.vector.Vector3f;

import autopilot.algorithms.*;
import autopilot.interfaces.path.MyPath;
import interfaces.Autopilot;
import interfaces.AutopilotConfig;
import interfaces.AutopilotInputs;
import interfaces.AutopilotOutputs;

public class AutopilotAlain implements Autopilot, AlgorithmHandler {
	
	public static float CRUISESPEED = 40f;
	
	/*
	 * INFORMATIE IVM VERSCHILLENDE CRUISE SNELHEDEN
	 * 	Cruise speed	|	Vereiste rear wing incl om -+ hoogte te behouden
	 * 	45				|	0.117 rad
	 * 	42				|	0.134 rad
	 * 	40				|	0.149 rad
	 * 	38				|	0.165 rad
	 * 	35				|	0.197 rad
	 */
	
	// Constructor
	
	public AutopilotAlain(Algorithm startingAlgorithm) {
		setAlgorithm(startingAlgorithm);
	}
	
	public AutopilotAlain() {
		
		// add algorithms in order
		addAlgorithm(new TakeOff(34f));
//		addAlgorithm(new FlyToPoint(new FlyToPoint(new VliegRechtdoor(), new Vector3f(5, 40, -500)), new Vector3f(5, 20, -300)));
		addAlgorithm(new FlyToPointTorben(new FlyToPointTorben(new VliegRechtdoor(), new Vector3f(5,20,-1000)),new Vector3f(5,20,-500)));
//		addAlgorithm(new Turn(1.0f));
//		addAlgorithm(new Stabilize());
		
		/*
		addAlgorithm(new FlyToHeight2(15f, -370f));
		addAlgorithm(new FlyToHeight2(10f, -450f));
		addAlgorithm(new FlyToHeight2(15f, -500f));
		addAlgorithm(new FlyToHeight2(12f, -550f));
		addAlgorithm(new FlyToHeight2(9f, -600f));
		addAlgorithm(new FlyToHeight2(15f, -650f));
		*/
		
		// start 1st algorithm
		nextAlgorithm();
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
		
		if (getAlgorithm() == null)
			setAlgorithm(new VliegRechtdoor());
	}
	
	// AlgorithmHandler interface
	
	private Algorithm algorithm;
	public void setAlgorithm(Algorithm algorithm) {
		System.out.println("autopilot: " + algorithm.getName());
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
