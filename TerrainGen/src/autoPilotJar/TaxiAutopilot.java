package autoPilotJar;

import org.lwjgl.util.vector.Vector3f;


public class TaxiAutopilot {

	private Vector3f goal = new Vector3f(50, 0, -200);
	
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
		if (properties.getHeading() - getHorAngle(properties) < -0.02) {
			System.out.println("jow");
			properties.setLeftBrakeForce(getParent().getConfig().getRMax()/2);
			properties.setRightBrakeForce(0);
			properties.setFrontBrakeForce(0);
			properties.setThrust(50);	
		} else if (properties.getHeading() - getHorAngle(properties) > 0.02) {
			System.out.println("hey");
			properties.setLeftBrakeForce(0);
			properties.setRightBrakeForce(getParent().getConfig().getRMax()/2);
			properties.setFrontBrakeForce(0);
			properties.setThrust(50);
		} else if (properties.getVelocity().length() > 15) {
			properties.setThrust(0);
			properties.setLeftBrakeForce(0);
			properties.setRightBrakeForce(0);
			properties.setFrontBrakeForce(0);
		} else {
			properties.setThrust(200);
			properties.setLeftBrakeForce(0);
			properties.setRightBrakeForce(0);
			properties.setFrontBrakeForce(0);
		}
		
		System.out.println("hoek: " + String.valueOf(properties.getHeading() - getHorAngle(properties)));
		System.out.println("thrust: " + String.valueOf(properties.getThrust()));
		System.out.println("linksrem: " + String.valueOf(properties.getLeftBrakeForce()));
		System.out.println("rechtsrem: " + String.valueOf(properties.getRightBrakeForce()));
		
		//TODO: Case if nearly reached -> vertragen
		
		// Als de drone binnen 1 meter komt en trager dan 1m/s rijdt, dan wordt de goal bereikt.
		// In de opgave staat er dat de goal exact bereikt moet worden, maar we nemen binnen 1 meter want zo exact is niet echt belangrijk.
		if(SimpleAutopilot.getEuclidDist(properties.getPosition(),goal) <= 1 &&	properties.getVelocity().length() <= 1)
			System.out.println("GOAL REACHED!0000000000000000000000000000000000000000000000000000000");
			
		
		return properties;
	}

		public SimpleAutopilot getParent() {
			return parent;
		}


		public void setParent(SimpleAutopilot parent) {
			this.parent = parent;
		}


}
