package autopilot.algorithmHandler;

import autopilot.algorithms.Algorithm;
import autopilot.interfaces.AutopilotOutputs;

public interface AlgorithmHandler extends AutopilotOutputs {
	
	public void nextAlgorithm();
	
	public void setAlgorithm(Algorithm algorithm);
	
	public String getAlgorithmName();
	
	public void setThrust(float thrust);
	
	public void setLeftWingInclination(float leftWingInclination);
	
	public void setRightWingInclination(float rightWingInclination);
	
	public void setHorStabInclination(float horStabInclination);
	
	public void setVerStabInclination(float verStabInclination);
	
	public void setFrontBrakeForce(float frontBrakeForce);
	
	public void setLeftBrakeForce(float leftBrakeForce);
	
	public void setRightBrakeForce(float rightBrakeForce);
	
	public Properties getProperties();
}
