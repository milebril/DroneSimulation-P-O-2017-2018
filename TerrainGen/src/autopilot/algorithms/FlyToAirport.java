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
	private enum Doel {LINKS, RECHTS, RECHTDOOR};
	
	public FlyToAirport(Airport airport, int gate, AutopilotAlain ap) {
		this.target = airport;
		this.targetGate = gate;
		
		positionTarget = target.getGate(gate).getPosition();
		
		// opstijgen
		ap.addAlgorithm(new TakeOff(35f));
		ap.addAlgorithm(new FlyToHeight(ap.getCruiseHeight()));
		
		
		// RICHTING BEPALEN
		Vector3f relativeTarget = new Vector3f();
		relativeTarget.x = positionTarget.x - ap.getProperties().getX();
		relativeTarget.y = positionTarget.y - ap.getProperties().getY();
		relativeTarget.z = positionTarget.z - ap.getProperties().getZ();
		relativeTarget = ap.getProperties().transformToDroneFrame(relativeTarget);
		Doel doelrichting = null;
		if (Math.abs(relativeTarget.x + 850) < 20) doelrichting = Doel.LINKS;
		else if (Math.abs(relativeTarget.x - 850) < 20) doelrichting = Doel.RECHTS;
		else doelrichting = Doel.RECHTDOOR;
		
		System.out.println("doelrichting: " + doelrichting);
		
		// rechtdoor vliegen
		ap.addAlgorithm(new YayoRechtdoor(-255 - ap.getProperties().getCruiseheight() / 2, positionTarget));
		
		// optioneel: links of rechts draaien
		if (doelrichting == Doel.LINKS) {
			ap.addAlgorithm(new TurnStijn((float) Math.PI/2));
		} else if (doelrichting == Doel.RECHTS) {
			ap.addAlgorithm(new TurnStijn((float) -Math.PI/2));
		}
		
		
		// rechtdoor vliegen naar de luchthaven
		ap.addAlgorithm(new YayoRechtdoor(-350, positionTarget));
		
		// landen
		//ap.addAlgorithm(new FlyToHeight(6.0f));
		ap.addAlgorithm(new Land());
		
		ap.addAlgorithm(new Taxi(positionTarget));
		
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
