package autopilot.algorithms;

import autopilot.algorithmHandler.AlgorithmHandler;

public class Aanloop implements Algorithm {
	
	private final float snelheid;
	
	/**
	 * Versnelt tot de gegeven de snelheid is behaalt en start dan het volgende algoritme
	 */
	public Aanloop(float snelheid) {
		this.snelheid = snelheid;
	}
	
	@Override
	public void cycle(AlgorithmHandler handler) {
		if (handler.getProperties().getVelocity().length() < this.snelheid) { // versnellen
			handler.setThrust(handler.getProperties().getMaxThrust());
		} else if (handler.getProperties().getY() < 7) {
			handler.setLeftWingInclination((float) Math.toRadians(10)); 
			handler.setRightWingInclination((float) Math.toRadians(10)); 
		} else {
			handler.nextAlgorithm();
		}
	}

	@Override
	public String getName() {
		return "Aanloop";
	}
	
}
