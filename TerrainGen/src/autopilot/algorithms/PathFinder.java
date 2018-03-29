package autopilot.algorithms;

import autopilot.algorithmHandler.AlgorithmHandler;

public class PathFinder implements Algorithm {
	
	public static boolean condition(AlgorithmHandler handler) {
		System.out.println(handler.getProperties().getVelocity().getY());
		return 30 < handler.getProperties().getVelocity().getY();
	}
	
	@Override
	public void cycle(AlgorithmHandler handler) {

		handler.setLeftWingInclination(0);
		handler.setRightWingInclination(0);
		
		// keep around 50m/s
		if (handler.getProperties().getVelocity().length() < 50) {
			handler.setThrust(handler.getProperties().getMaxThrust());
		} else if (handler.getProperties().getVelocity().length() < 55){
			handler.setThrust((float) (handler.getProperties().getMaxThrust()* 0.5));
		} else {
			handler.setThrust(0);
		}
		
		// bring pitch to 0
		if (handler.getProperties().getPitch() < 0 * Math.PI / 180) {
			handler.setHorStabInclination((float) (-8 * Math.PI / 180));
		} else {
			handler.setHorStabInclination((float) (0 * Math.PI / 180));
		}
		
		// stay around height 50
		if (handler.getProperties().getY() < 50) {
			handler.setLeftWingInclination((float) (-5 * Math.PI / 180));
			handler.setRightWingInclination((float) (-5 * Math.PI / 180));
		} else {
			handler.setLeftWingInclination((float) (0 * Math.PI / 180));
			handler.setRightWingInclination((float) (0 * Math.PI / 180));
		}
		
	}

	@Override
	public String getName() {
		return "PathFinder";
	}

}
