package entities;

import org.lwjgl.util.vector.Vector3f;

import physicsEngine.MaxAoAException;

/**
 * all airfoils have a neutral attack vector oriented along (O,0,-1) expressed in the Drone Frame
 * @author Jakob
 *
 */
public class AirFoil {
	
	// CONSTRUCTOR
	
	/**
	 * The wing of a drone.
	 * @param drone: The drone to which this AirFoil is attached
	 * @param rotAx: The rotation axis of this AirFoil (in drone frame)
	 * @param centerOfMass: The center of mass of this AirFoil (in drone frame)
	 * @param mass: The mass of this AirFoil
	 * @param wingLiftSlope: The lift slope of this AirFoil
	 */
	public AirFoil(Drone drone, Vector3f rotAx, Vector3f centerOfMass, float mass, float wingLiftSlope) {
		this.drone = drone;
		this.rotAx = rotAx;		
		this.centerOfMass = centerOfMass;
		this.airFoilMass = mass;
		this.airFoilLiftSlope = wingLiftSlope;
	}
	
	// DRONE
	
	/**
	 * The drone to which this AirFoil is attached
	 */
	private final Drone drone;

	/**
	 * Returns the drone to which this AirFoil is attached
	 */
	public Drone getDrone() {
		return this.drone;
	}
	
	// MASS
	
	/**
	 * The mass of the AirFoil
	 */
	private final float airFoilMass;
	
	/**
	 * Returns the mass of the AirFoil
	 */
	public float getMass(){
		return this.airFoilMass;
	}
	
	// CENTER OF MASS
	
	/**
	 * The AirFoils center of mass (in drone coordinates)
	 */
	private final Vector3f centerOfMass;
	
	/**
	 * Returns the AirFoils center of mass (in drone coordinates)
	 */
	public Vector3f getCenterOfMass() {
		return new Vector3f(this.centerOfMass.x, this.centerOfMass.y, this.centerOfMass.z);
	}
	
	// ROTATION AXIS
	
	/**
	 * The rotation axis of the AirFoil (in drone coordinates)
	 */
	private final Vector3f rotAx;
	
	/**
	 * Returns the rotation axis of the AirFoil (in drone coordinates)
	 */
	public Vector3f getRotAxis(){
		return new Vector3f(this.rotAx.x, this.rotAx.y, this.rotAx.z);
	}
	
	// LIFTSLOPE
	
	/**
	 * The lift slope of the AirFoil.
	 */
	private final float airFoilLiftSlope;
	
	/**
	 * Returns the AirFoils lift slope.
	 */
	public float getLiftSlope(){
		return this.airFoilLiftSlope;
	}
	
	// INCLINATION
	
	/**
	 * The inclination of the AirFoil in radians.
	 */
	private float inclination = 0;
	
	/**
	 * Returns the inclination of the AirFoil in radians.
	 */
	public float getInclination(){
		return this.inclination;
	}
	
	/**
	 * Sets the inclination of the AirFoil in radians.
	 * @param inclination (in radians)
	 */
	public void setInclination(float inclination){
		this.inclination = inclination;
	}
	
	// LIFT FORCE
	
	/**
	 * Returns the AttackVector of the AirFoil. The only two possible rotation axes are the x-axis 
	 * or the y-axis. All neutral attack vectors are (0,0,-1), so the result after the rotation 
	 * can be shortcut without matrices.
	 */
	public Vector3f calculateAttackVector(){
		Vector3f result =  new Vector3f();
		if( this.getRotAxis().x == 1){
			result.x = 0;
			result.y = (float) Math.sin(this.getInclination());
			result.z = (float) - Math.cos(this.getInclination());
		}
		else{
			result.x = (float) - Math.sin(this.getInclination());
			result.y = 0;
			result.z = (float) - Math.cos(this.getInclination());			
		}
		
		return result;		
	}
	
	/**
	 * Returns the normal of the AirFoil. The normal is defined as the cross product of
	 * the axis vector and the attack vector of the AirFoil.
	 */
	public Vector3f calculateNormal(){
		Vector3f result = new Vector3f();
		Vector3f.cross(this.getRotAxis(), this.calculateAttackVector(),  result);
		return result;	
	}
	
	/**
	 * Returns the lift force of the AirFoil assuming there is no wind.
	 * @throws MaxAoAException if the max angle of attack is exceeded while the lift force of
	 * 						   the airfoil is greater than 50N
	 */
	public Vector3f calculateAirFoilLiftForce() throws MaxAoAException {
		return this.calculateAirfoilLiftForce(new Vector3f(0,0,0));
	}
	
	/**
	 * Returns the lift force of the AirFoil.
	 * @throws MaxAoAException if the max angle of attack is exceeded while the lift force of
	 * 						   the airfoil is greater than 50N
	 */
	public Vector3f calculateAirfoilLiftForce(Vector3f windW) throws MaxAoAException {
		// calculate the airspeed the airfoil experiences
		Vector3f airSpeedW = new Vector3f(0, 0, 0);
		
		// velocity of the airfoil caused by the drones rotation (omega x r = v)
		Vector3f rotationalVelocityW = new Vector3f(0,0,0);
		Vector3f.cross(this.getDrone().getAngularVelocity(), this.getDrone().transformToWorldFrame(this.getCenterOfMass()), rotationalVelocityW);
		
		// velocity of the airfoil caused by the drones linear velocity
		Vector3f linearVelocityW = this.getDrone().getLinearVelocity();
		
		// airspeed = airfoil velocity - wind
		Vector3f.add(airSpeedW, rotationalVelocityW, airSpeedW);
		Vector3f.add(airSpeedW, linearVelocityW, airSpeedW);
		Vector3f.sub(airSpeedW, windW, airSpeedW);

		// transform the airSpeed vector to the drone frame
		Vector3f airSpeedD = this.getDrone().transformToDroneFrame(airSpeedW);
		
		// project airSpeedD on the surface, perpendicular to the rotationAxis of the AirFoil
		Vector3f rotationAxisD = this.getRotAxis();
		
		// projected airspeed vector (S)
		Vector3f projectedAirspeedVectorD = new Vector3f(0, 0, 0);
		Vector3f.sub(airSpeedD, (Vector3f) rotationAxisD.scale(
				Vector3f.dot(airSpeedD, rotationAxisD)), projectedAirspeedVectorD
				);
		
		// attack vector of the airfoil (A)
		Vector3f attackVectorD = this.calculateAttackVector();
		
		// normal of the airfoil (N)
		Vector3f normalD = new Vector3f();
		Vector3f.cross(this.getRotAxis(), attackVectorD,  normalD);
		
		// calculate the angle of attack, defined as -atan2(S . N, S . A), where S
		// is the projected airspeed vector, N is the normal, and A is the attack vector
		float a = Vector3f.dot(projectedAirspeedVectorD, normalD);
		float b = Vector3f.dot(projectedAirspeedVectorD, attackVectorD);
		
		float aoa = (float) - Math.atan2(a, b);
		
		// calculate the lift force N . liftSlope . AOA . s^2, where N is the
		// normal, AOA is the angle of attack, and s is the projected airspeed
		float airspeedSquared = projectedAirspeedVectorD.lengthSquared();
		Vector3f liftForceD = (Vector3f) normalD.scale(
				this.getLiftSlope() * (float)(aoa % Math.PI) * airspeedSquared
				);
		
		// if max AoA is exceeded and the liftForce is greater than 50N, throw exception
		if (aoa > Math.toRadians(drone.getMaxAOA()) && liftForceD.length() > 50) {
			//throw new MaxAoAException("Error Max AoA exceeded!"); TODO
		}
		
		return liftForceD;
	}
}
