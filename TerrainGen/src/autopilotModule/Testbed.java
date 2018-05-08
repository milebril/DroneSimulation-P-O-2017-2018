package autopilotModule;

import java.util.HashMap;

import entities.Drone;
import models.Airport;

public class Testbed {

	public Testbed(HashMap<Integer,Drone> activeDrones, HashMap<Integer,Drone> inactiveDrones, HashMap<Integer,Airport> airports){
		this.activeDrones = activeDrones;
		this.inactiveDrones = inactiveDrones;
		this.airports = airports;
	}
	
	private static HashMap<Integer,Drone> activeDrones = new HashMap<Integer, Drone>();
	private static HashMap<Integer,Drone> inactiveDrones = new HashMap<Integer, Drone>();
	private static HashMap<Integer,Airport> airports = new HashMap<Integer, Airport>();
	
	public HashMap<Integer,Airport> getAirports(){
		return this.airports;
	}
	
	public HashMap<Integer,Drone> getInactiveDrones(){
		return this.activeDrones;
	}
	
	public HashMap<Integer,Drone> getActiveDrones(){
		return this.inactiveDrones;
	}
}
