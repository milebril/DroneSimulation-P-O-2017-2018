package autopilot.algorithms;

import autopilot.Algorithm;
import autopilot.AlgorithmHandler;
import autopilot.Properties;

public class Takeoff implements Algorithm {

	public static float takeoffVelocity = 40;
	
	@Override
	public void cycle(AlgorithmHandler handler) {

		handler.setThrust(handler.getProperties().getMaxThrust());
		// pull up nose if pitch is less than 30%
		if (handler.getProperties().getY() < 4) {
			handler.setLeftWingInclination((float) (15 * Math.PI / 180));
			handler.setRightWingInclination((float) (15 * Math.PI / 180));
			handler.setHorStabInclination((float) (-1 * Math.PI / 180));
		} else if (handler.getProperties().getPitch() < 25 * Math.PI / 180) {
			handler.setLeftWingInclination(0);
			handler.setRightWingInclination(0);
			handler.setHorStabInclination((float) (-8 * Math.PI / 180));
		} else {
			handler.setHorStabInclination((float) 0);
		}
		
	}
	
	@Override
	public String getName() {
		return "Takeoff";
	}
}
