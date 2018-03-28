package autopilot.algorithms;


import autopilot.Algorithm;
import autopilot.AlgorithmHandler;
import autopilot.Properties;

public class Takeoff implements Algorithm {

	@Override
	public void cycle(AlgorithmHandler handler) {
		Properties properties = handler.getProperties();
		System.out.println("- - - takeoff algorithm cycle - - -");
		
		if (properties.getVelocity().length() > 10) {
			handler.setFrontBrakeForce(properties.getRMax());
			handler.setLeftBrakeForce(properties.getRMax());
			handler.setRightBrakeForce(properties.getRMax());
		} else {
			handler.setFrontBrakeForce(0);
			handler.setLeftBrakeForce(0);
			handler.setRightBrakeForce(0);
		}
		
		
		// IF (situatie waarin bepaalt algoritme moet activeren)
		//		handler.setAlgorithm(new nextAlgorithm())
	}

}
