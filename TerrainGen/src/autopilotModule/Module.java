package autopilotModule;

import java.util.ArrayList;
import java.util.Random;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import autopilot.algorithmHandler.AutopilotAlain;
import autopilot.interfaces.Autopilot;
import autopilot.interfaces.AutopilotConfig;
import autopilot.interfaces.AutopilotFactory;
import autopilot.interfaces.AutopilotInputs;
import autopilot.interfaces.AutopilotModule;
import autopilot.interfaces.AutopilotOutputs;
import autopilotModule.Testbed;
import entities.Drone;
import models.Airport;
import models.RawModel;
import models.TexturedModel;
import physicsEngine.PhysicsEngine;
import physicsEngine.approximationMethods.EulerPrediction;
import renderEngine.Loader;
import renderEngine.OBJLoader;
import textures.ModelTexture;

public class Module implements AutopilotModule {

	private static final float STEP_TIME = 0.001f;
	private static Loader loader;

	private float length;
	private float width;

	private Testbed testbed;

	private ArrayList<AutopilotOutputs> apOutputs = new ArrayList<>();

	public Module(Testbed testbed) {
		setTestbed(testbed);

	};

	@Override
	public void defineAirportParams(float length, float width) {
		this.length = length;
		this.width = width;
	}

	@Override
	public void defineAirport(float centerX, float centerZ, float centerToRunway0X, float centerToRunway0Z) {
		// (centerToRunway0X, centerToRunway0Z) constitutes a unit vector pointing from
		// the center of the airport towards runway 0
		String s = "";
		if (centerToRunway0X == 1) {
			s = "block";
		}
		Airport airport = new Airport((int) centerX, (int) centerZ, this.getTestbed().getAirports().size(), s);
		this.getTestbed().getAirports().add(this.getTestbed().getAirports().size(), airport);
	}

	// TODO: moeten we drones bijhouden hier en dezelfde lijst int testbed? of wa
	// moete we hier eigenlijk juist doen???
	@Override
	public void defineDrone(int airport, int gate, int pointingToRunway, AutopilotConfig config) {
		// airport and gate define the drone's initial location, pointingToRunway its
		// initial orientation. The first drone that is defined is drone 0, etc.

		Airport luchthaven = this.getTestbed().getAirports().get(airport);
		loader = new Loader();
		RawModel droneModel = OBJLoader.loadObjModel("untitled5", loader);
		TexturedModel staticDroneModel = new TexturedModel(droneModel,
				new ModelTexture(loader.loadTexture("untitled")));
		Random r = new Random();
//		int x = r.nextInt(2000);
//		int z = r.nextInt(2000);
		int x = 0;
		int z = 0;
		Drone drone = new Drone(staticDroneModel,
				luchthaven.getDronePosition(gate, config).translate(new Vector3f(x, 0, z)), 1f, config,
				new EulerPrediction(STEP_TIME));
		int droneId = getTestbed()
				.getDrones(testbed.getInactiveDrones(), testbed.getActiveDrones()).size();
		drone.setName("Drone: " + droneId);
		drone.setId(droneId);
		System.out.println("Drone: " + drone.getId());

		if (pointingToRunway == 1) {
			if (luchthaven.isRotated()) {
				drone.rotate((float) -Math.PI / 2, new Vector3f(0, 1, 0));
			}
		} else {
			if (luchthaven.isRotated()) {
				drone.rotate((float) Math.PI / 2, new Vector3f(0, 1, 0));
			} else {
				drone.rotate((float) Math.PI, new Vector3f(0, 1, 0));
			}
		}

		drone.getAutopilot().simulationStarted(config, drone.getAutoPilotInputs());

		this.getTestbed().getActiveDrones().add(droneId, drone);

	}

	@Override
	public void startTimeHasPassed(int drone, AutopilotInputs inputs) {
		// Allows the autopilots for all drones to run in parallel if desired. Called
		// with drone = 0 through N - 1, in that order, if N drones have been defined.
		this.apOutputs.add(drone, this.getTestbed().getActiveDrones().get(drone).getAutopilot().timePassed(inputs));
	}

	@Override
	public AutopilotOutputs completeTimeHasPassed(int drone) {
		// Called with drone = 0 through N - 1, in that order, if N drones have been
		// defined.
		return apOutputs.get(drone);
	}

	@Override
	public void deliverPackage(int fromAirport, int fromGate, int toAirport, int toGate) {
		// Informs the autopilot module of a new package delivery request generated by
		// the testbed.

		Integer distance = Integer.MAX_VALUE;
		Drone dBest = this.testbed.getInactiveDrones().get(0);

		Vector3f airport = this.getTestbed().getAirports().get(fromAirport).getPackagePosition(fromGate);
		for (Drone d : this.testbed.getInactiveDrones()) {
			Vector3f dpos = d.getPosition();
			Vector3f newDistance = new Vector3f(airport.getX() - dpos.getX(), airport.getY() - dpos.getY(),
					airport.getZ() - dpos.getZ());
			if (newDistance.length() < distance) {
				distance = (int) newDistance.length();
				dBest = d;
			}
		}

		this.getTestbed().getInactiveDrones().remove(dBest);
		this.getTestbed().getActiveDrones().add(dBest);

	}

	@Override
	public void simulationEnded() {

	}

	public Testbed getTestbed() {
		return testbed;
	}

	public void setTestbed(Testbed testbed) {
		this.testbed = testbed;
	}

}
