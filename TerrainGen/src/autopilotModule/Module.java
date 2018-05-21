package autopilotModule;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lwjgl.util.vector.Vector3f;

import autopilot.algorithmHandler.AutopilotAlain;
import autopilot.algorithms.FlyToAirport;
import autopilot.algorithms.TurnOnGround;
import autopilot.algorithms.VliegRechtdoor;
import autopilot.algorithms.Wait;
import autopilot.interfaces.AutopilotConfig;
import autopilot.interfaces.AutopilotInputs;
import autopilot.interfaces.AutopilotModule;
import autopilot.interfaces.AutopilotOutputs;
import autopilotModule.Testbed;
import engineTester.MainGameLoop;
import entities.Drone;
import models.Airport;
import models.RawModel;
import models.TexturedModel;
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

	private int droneCount = 0;

	private AutopilotOutputs[] apOutputs = new AutopilotOutputs[2000];

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
		Airport airport = new Airport((int) centerX, (int) centerZ, getTestbed().getNextAirportID(), s);
		this.getTestbed().getAirports().add(getTestbed().getNextAirportID(), airport);
	}

	@Override
	public void defineDrone(int airport, int gate, int pointingToRunway, AutopilotConfig config) {
		// airport and gate define the drone's initial location, pointingToRunway its
		// initial orientation. The first drone that is defined is drone 0, etc.

		Airport luchthaven = getTestbed().getAirports().get(airport);
		loader = new Loader();
		RawModel droneModel = OBJLoader.loadObjModel("untitled5", loader);
		TexturedModel staticDroneModel = new TexturedModel(droneModel,
				new ModelTexture(loader.loadTexture("untitled")));
		Random r = new Random();
		int droneId = droneCount++;
		// int x = r.nextInt(2000);
		// int z = r.nextInt(2000);
		int x = 0;
		int z = 0;
		Drone drone = new Drone(staticDroneModel,
				luchthaven.getDronePosition(gate, config).translate(new Vector3f(x, 0, z)), 1f, config,
				new EulerPrediction(STEP_TIME), droneId, "Drone: " + droneId);
		drone.setHomeBase(luchthaven);
		drone.setCurrentAirport(luchthaven);
		drone.setStartGate(gate);

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

		((AutopilotAlain) drone.getAutopilot()).setCruiseHeight(drone.getId() * 5 + 10);
		drone.getAutopilot().simulationStarted(config, drone.getAutoPilotInputs());

		// TODO: Drones mogen pas actief worden als er een pakketje op valt
		this.getTestbed().getInactiveDrones()[droneId] = drone;
	}

	@Override
	public void startTimeHasPassed(int drone, AutopilotInputs inputs) {
		// Allows the autopilots for all drones to run in parallel if desired. Called
		// with drone = 0 through N - 1, in that order, if N drones have been defined.
		apOutputs[drone] = this.getTestbed().getActiveDrones()[drone].getAutopilot().timePassed(inputs);
	}

	@Override
	public AutopilotOutputs completeTimeHasPassed(int drone) {
		// Called with drone = 0 through N - 1, in that order, if N drones have been
		// defined.
		return apOutputs[drone];
	}

	@Override
	public void deliverPackage(int fromAirport, int fromGate, int toAirport, int toGate) {
		// Informs the autopilot module of a new package delivery request generated by
		// the testbed.

		Integer distance = Integer.MAX_VALUE;
		Drone dBest = this.testbed.getInactiveDrones()[0];

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

	public void update() {
		for (int i = 0; i < testbed.getInactiveDrones().length; i++) {
			if (testbed.getInactiveDrones()[i] != null) {
				Drone d = testbed.getInactiveDrones()[i];
				if (d.getHomebase() == d.getCurrentAirport()) { // Drone is at homebase
					int gate = getEuclidDist(d.getPosition(), d.getHomebase().getGate(0).getPosition()) <= 2 ? 0 : 1;
					if (d.getHomebase().containsPackage(gate)) {
						entities.Package temp = d.getCurrentAirport().getPackage(gate);
						if (temp.getDestAirport() != d.getHomebase()) {
							System.out.println("Take Packet");
							d.getHomebase().showPackage(gate);
							flyToAirport(d, temp.getDestAirport(), temp.getDestGate());

							testbed.getInactiveDrones()[i] = null;
							testbed.getActiveDrones()[i] = d;

							MainGameLoop.setActiveDrone(d);

						} else {
							((AutopilotAlain) d.getAutopilot()).setAlgorithm(new Wait());
						}
					}
				}
			}
		}

		for (int i = 0; i < testbed.getActiveDrones().length; i++) {
			if (testbed.getActiveDrones()[i] != null) {
				Drone d = testbed.getActiveDrones()[i];

				// Airport reached
				if (((AutopilotAlain) d.getAutopilot()).getAlgorithm() instanceof VliegRechtdoor
						&& d.getCurrentAirport() != d.getHomebase()) {
					d.getCurrentAirport().showPackage(
							getEuclidDist(d.getPosition(), d.getCurrentAirport().getGate(0).getPosition()) <= 2 ? 0
									: 1);
					flyToHomebase(d);
				} else if (d.getHomebase() == d.getCurrentAirport()
						&& getEuclidDist(d.getPosition(), d.getHomebase().getGate(0).getPosition()) <= 2) {
					testbed.getActiveDrones()[i] = null;
					testbed.getInactiveDrones()[i] = d;
				}

				if (((AutopilotAlain) d.getAutopilot()).getAlgorithm() instanceof TurnOnGround) {
					if (Math.abs(((TurnOnGround) ((AutopilotAlain) d.getAutopilot()).getAlgorithm()).angleDif) <= 0.005) {
						d.setHeading(((TurnOnGround) ((AutopilotAlain) d.getAutopilot()).getAlgorithm()).angle);
						((TurnOnGround) ((AutopilotAlain) d.getAutopilot()).getAlgorithm()).fullBrakeNext();
					}
				}
			}
		}

	}

	public void spawnPacket(Airport startAirport, int startGate, Airport destAirport, int destGate) {
		startAirport.setPackage(startGate, destAirport, destGate, destAirport, destGate);
	}

	public void flyToAirport(Drone drone, Airport airport, int gate) {
		new FlyToAirport(airport, gate, (AutopilotAlain) drone.getAutopilot());
		drone.setCurrentAirport(airport);
	}

	public void flyToHomebase(Drone drone) {
		Vector3f current = drone.getCurrentAirport().getPosition();
		Vector3f homebasePosition = drone.getHomebase().getPosition();
		
		if (drone.getCurrentAirport().getAirportID() == 0) {
			((AutopilotAlain) drone.getAutopilot()).addAlgorithm(new TurnOnGround((float) (Math.PI/2)));
		} else if (drone.getCurrentAirport().getAirportID() == 1) {
			((AutopilotAlain) drone.getAutopilot()).addAlgorithm(new TurnOnGround((float) (-3.1415)));
		} else if (drone.getCurrentAirport().getAirportID() == 2) {
			((AutopilotAlain) drone.getAutopilot()).addAlgorithm(new TurnOnGround((float) (-1.569)));
		} else if (drone.getCurrentAirport().getAirportID() == 3) {
			((AutopilotAlain) drone.getAutopilot()).addAlgorithm(new TurnOnGround((float) (0.0)));
		}
		
		flyToAirport(drone, drone.getHomebase(), drone.getStartGate());
	}

	private float getEuclidDist(Vector3f vec1, Vector3f vec2) {
		Vector3f temp = new Vector3f(0, 0, 0);
		Vector3f.sub(vec2, vec1, temp);
		return temp.length();
	}

}
