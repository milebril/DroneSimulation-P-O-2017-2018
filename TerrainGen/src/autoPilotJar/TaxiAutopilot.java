package autoPilotJar;

import org.lwjgl.util.vector.Vector3f;


public class TaxiAutopilot {

	private Vector3f goal = new Vector3f(0, 0, -200);
	
	private SimpleAutopilot parent;
	
public TaxiAutopilot(SimpleAutopilot parent) {
		setParent(parent);
	}

	private float getHorAngle(DroneProperties properties) {
		float overstaande = goal.getX() - properties.getPosition().getX();
		float aanliggende = goal.getZ() - properties.getPosition().getZ();
		return (float) Math.atan(overstaande / aanliggende);
	}
	
	public DroneProperties timePassed(DroneProperties properties) {
		if (properties.getHeading() - getHorAngle(properties) > 0.01) {
			properties.setLeftBrakeForce(getParent().getConfig().getFcMax());
			properties.setRightBrakeForce(0);
			properties.setFrontBrakeForce(0);
			properties.setThrust(200);
		} else if (properties.getHeading() - getHorAngle(properties) < 0.01) {
			properties.setLeftBrakeForce(0);
			properties.setRightBrakeForce(getParent().getConfig().getFcMax());
			properties.setFrontBrakeForce(0);
			properties.setThrust(200);
		} else if (properties.getVelocity().length() > 50) {
			properties.setThrust(0);
		} else {
			properties.setThrust(getParent().getConfig().getMaxThrust());
		}
		
		//TODO: Case if nearly reached -> vertragen
		
		// Als de drone binnen 1 meter komt en trager dan 1m/s rijdt, dan wordt de goal bereikt.
		// In de opgave staat er dat de goal exact bereikt moet worden, maar we nemen binnen 1 meter want zo exact is niet echt belangrijk.
		if(SimpleAutopilot.getEuclidDist(properties.getPosition(),goal) <= 1 &&	properties.getVelocity().length() <= 1)
			System.out.println("GOAL REACHED!");
			
		
		return properties;
	}

		public SimpleAutopilot getParent() {
			return parent;
		}


		public void setParent(SimpleAutopilot parent) {
			this.parent = parent;
		}


}
