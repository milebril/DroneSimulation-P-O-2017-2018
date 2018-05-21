package autopilot.algorithms;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector3f;

import autopilot.algorithmHandler.AlgorithmHandler;
import autopilot.algorithmHandler.AutopilotAlain;
import models.Airport;

public class FlyToAirport implements Algorithm {

	private Airport target;
	private int targetGate;
	
	private Vector3f positionTarget;
	
	public FlyToAirport(Airport airport, int gate, AutopilotAlain ap) {
		this.target = airport;
		this.targetGate = gate;
		
		positionTarget = target.getGate(gate).getPosition();
		
		//Algorithms to follow
		ap.addAlgorithm(new TakeOff(35f));
		ap.addAlgorithm(new FlyToHeight(ap.getCruiseHeight()));
		
		Vector3f groundTouchPosition;
		if (positionTarget.getZ() >= -200) {
			groundTouchPosition = new Vector3f(positionTarget.x, positionTarget.y, positionTarget.z - 150);
		} else {
			groundTouchPosition = new Vector3f(positionTarget.x, positionTarget.y, positionTarget.z + 150);
		}
		ap.addAlgorithm(new FlyStraightToLand(groundTouchPosition));
		
		ap.addAlgorithm(new Land());
		
		if (positionTarget.getZ() >= -200) {
			groundTouchPosition = new Vector3f(positionTarget.x, positionTarget.y, positionTarget.z);
		} else {
			groundTouchPosition = new Vector3f(positionTarget.x, positionTarget.y, positionTarget.z);
		}
		
		ap.addAlgorithm(new Taxi(groundTouchPosition));
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
