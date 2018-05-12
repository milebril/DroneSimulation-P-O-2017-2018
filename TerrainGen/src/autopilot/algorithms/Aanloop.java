package autopilot.algorithms;

import autopilot.algorithmHandler.AlgorithmHandler;

public class Aanloop implements Algorithm {
	
	private final float snelheid;
	private float angle;
	
	/**
	 * Versnelt tot de gegeven de snelheid is behaalt en start dan het volgende algoritme
	 */
	public Aanloop(float snelheid, float angle) {
		this.snelheid = snelheid;
		this.angle = angle;
	}
	
	@Override
	public void cycle(AlgorithmHandler handler) {
		handler.setFrontBrakeForce(0);
		handler.setLeftBrakeForce(0);
		handler.setRightBrakeForce(0);
		if (handler.getProperties().getVelocity().length() < this.snelheid) { // versnellen
			handler.setThrust(handler.getProperties().getMaxThrust());
		} 
		else {
			handler.setLeftWingInclination((float) Math.toRadians(angle)); 
			handler.setRightWingInclination((float) Math.toRadians(angle)); 
		} 
		if (handler.getProperties().getY() > 5) {
			handler.nextAlgorithm();
		}
	}

	@Override
	public String getName() {
		return "Aanloop";
	}
	
}
