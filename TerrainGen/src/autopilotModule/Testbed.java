package autopilotModule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import entities.Drone;
import models.Airport;

public class Testbed {

	public Testbed(){
	}
	
	private static Drone[] activeDrones = new Drone[50];
	private static Drone[] inactiveDrones = new Drone[50];
	private static ArrayList<Airport> airports = new ArrayList<Airport>();
	
	public ArrayList<Airport> getAirports(){
		return this.airports;
	}
	
	public int getNextAirportID() {
		return this.airports.size();
	}
	
	public Drone[] getInactiveDrones(){
		return this.activeDrones;
	}
	
	public Drone[] getActiveDrones(){
		return this.inactiveDrones;
	}
	
//	public ArrayList<Drone> getDrones(){
//		ArrayList<Drone> d = new ArrayList<Drone>();
//		d.addAll(activeDrones);
//		d.addAll(inactiveDrones);
//		return d;
//	}
	
//	public int getNextDroneID() {
//		return getDrones().size();
//	}
}
