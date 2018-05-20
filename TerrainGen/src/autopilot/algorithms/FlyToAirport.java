package autopilot.algorithms;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector3f;

import autopilot.algorithmHandler.AlgorithmHandler;
import autopilot.algorithmHandler.AutopilotAlain;

public class FlyToAirport implements Algorithm {

	private Vector3f target;
	
	public FlyToAirport(Vector3f airportPosition, AutopilotAlain ap) {
		this.target = airportPosition;
		
		//Algorithms to follow
		ap.addAlgorithm(new TakeOff(35f));
		ap.addAlgorithm(new FlyToHeight(20f));
		
		Vector3f groundTouchPosition;
		if (airportPosition.getZ() >= -200) {
			groundTouchPosition = new Vector3f(target.x, target.y, target.z - 150);
		} else {
			groundTouchPosition = new Vector3f(target.x, target.y, target.z + 150);
		}
		ap.addAlgorithm(new FlyStraightToLand(groundTouchPosition));
		
		ap.addAlgorithm(new Land());
	}
	
	@Override
	public void cycle(AlgorithmHandler handler) {
		// TODO Auto-generated method stub
	}

	@Override
	public String getName() {
		return "flyToAirport";
	}

}
