package autopilotModule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import entities.Drone;
import models.Airport;

public class Testbed {

	public Testbed(ArrayList<Drone> activeDrones, ArrayList<Drone> inactiveDrones, ArrayList<Airport> airports){
		this.activeDrones = activeDrones;
		this.inactiveDrones = inactiveDrones;
		this.airports = airports;
	}
	
	private static ArrayList<Drone> activeDrones = new ArrayList<Drone>();
	private static ArrayList<Drone> inactiveDrones = new ArrayList<Drone>();
	private static ArrayList<Airport> airports = new ArrayList<Airport>();
	
	public ArrayList<Airport> getAirports(){
		return this.airports;
	}
	
	public ArrayList<Drone> getInactiveDrones(){
		return this.activeDrones;
	}
	
	public ArrayList<Drone> getActiveDrones(){
		return this.inactiveDrones;
	}
	
	public static List<Drone> getDrones(ArrayList<Drone> activeDrones, ArrayList<Drone> inactiveDrones){
		ArrayList<Drone> d = new ArrayList<Drone>();
		d.addAll(activeDrones);
		d.addAll(inactiveDrones);
		return d;
	}
}
