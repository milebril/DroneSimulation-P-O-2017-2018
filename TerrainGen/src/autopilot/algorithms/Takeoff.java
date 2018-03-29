package autopilot.algorithms;

import autopilot.Algorithm;
import autopilot.AlgorithmHandler;
import autopilot.Properties;

public class Takeoff implements Algorithm {
	
	public static boolean condition(AlgorithmHandler handler) {
		return 40 <= handler.getProperties().getVelocity().length();
	}
	
	@Override
	public void cycle(AlgorithmHandler handler) {

		handler.setThrust(handler.getProperties().getMaxThrust());
		System.out.println(PathFinder.condition(handler));
		if (PathFinder.condition(handler)) {
			handler.setAlgorithm(new PathFinder());
		} else if (handler.getProperties().getY() < 4) {
			handler.setLeftWingInclination((float) (15 * Math.PI / 180));
			handler.setRightWingInclination((float) (15 * Math.PI / 180));
			handler.setHorStabInclination((float) (-8 * Math.PI / 180));
		} else if (handler.getProperties().getY() < 20 && handler.getProperties().getPitch() < 20 * Math.PI / 180) {
			handler.setLeftWingInclination(0);
			handler.setRightWingInclination(0);
			handler.setHorStabInclination((float) (-8 * Math.PI / 180));
		}
		
	}
	
	@Override
	public String getName() {
		return "Takeoff";
	}
}
