package autopilot.algorithms;

import autopilot.algorithmHandler.AlgorithmHandler;
import autopilot.algorithmHandler.Properties;
import autopilot.interfaces.path.MyPath;

public class Takeoff implements Algorithm {
	
	public static boolean condition(AlgorithmHandler handler) {
		return 40 <= handler.getProperties().getVelocity().length();
	}
	
	@Override
	public void cycle(AlgorithmHandler handler) {

		handler.setThrust(handler.getProperties().getMaxThrust());
		if (40 < handler.getProperties().getY()) {
			float[] x = new float[]{  0,    0};
			float[] y = new float[]{ 50,   55};
			float[] z = new float[]{-50, -100};
			MyPath path = new MyPath(x, y, z);
			handler.setAlgorithm(new PathFinder(path));
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
