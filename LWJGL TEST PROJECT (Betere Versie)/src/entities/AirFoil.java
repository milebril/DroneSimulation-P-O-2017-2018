package entities;

import org.lwjgl.util.vector.Vector3f;

/**
 * all airfoils have a neutral attack vector oriented along (O,0,-1) expressed in the Drone Frame
 * @author Jakob
 *
 */
public class AirFoil {
	
	public AirFoil(Drone drone, Vector3f centerOfMass, float wingMass, float wingLiftSlope, Vector3f rotAx) {
		this.drone = drone;
		this.centerOfMassD = centerOfMass;
		this.airFoilMass = wingMass;
		this.airFoilLiftSlope = wingLiftSlope;
		this.rotAx = rotAx;		
	}
	
	
	// The AirFoil mass
	private final float airFoilMass;
	
	public float getMass(){
		return this.airFoilMass;
	}
	
	
	// The center of mass of the AirFoil (in drone coordinates)
	private Vector3f centerOfMassD;
	
	public Vector3f getCenterOfMass() {
		return new Vector3f(this.centerOfMassD.x, this.centerOfMassD.y, this.centerOfMassD.z);
	}

	public void setCenterOfMass(Vector3f vector) {
		this.centerOfMassD.set(vector.x, vector.y, vector.z);
	}
	
	
	// The rotation axis of the AirFoil (in drone coordinates)
	private Vector3f rotAx;
	
	public Vector3f getRotAxis(){
		return new Vector3f(this.rotAx.x, this.rotAx.y, this.rotAx.z);
	}
	
	
	// The lift slope of the AirFoil
	private float airFoilLiftSlope;
	
	public float getLiftSlope(){
		return this.airFoilLiftSlope;
	}

	public void setLiftSlope(float slope){
		this.airFoilLiftSlope = slope;
	}
	
	
	// The inclination of the AirFoil (in radians)
	private float inclination = 0;

	public float getInclination(){
		return this.inclination;
	}

	public void setInclination(float radians){
		this.inclination = radians;
	}
	
	
	
	/**
	 * Returns the AttackVector of the AirFoil. The only two possible rotation axes are the x-axis 
	 * or the y-axis. All neutral attack vectors are (0,0,-1), so the result after the rotation 
	 * can be shortcut without matrices.
	 */
	public Vector3f calculateAttackVector(){
//		System.out.println("Airfoil.calculateATtackvector: inclination: " + this.getInclination());
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
		
		System.out.println("Airfoil.ccalculateATtackvector attacvecor: " + result);
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
	
	public Vector3f calculateAirFoilLiftForce(){
		return this.calculateAirfoilLiftForce(new Vector3f(0,0,0));
	}
	
	public Vector3f calculateAirfoilLiftForce(Vector3f windW) {
		// calculate the airspeed the airfoil experiences
		Vector3f airSpeedW = new Vector3f(0, 0, 0);
//		System.out.println("Airfoil calculateAirfliff 115 airspeedWinit: " + airSpeedW);
		
		// velocity of the airfoil caused by the drones rotation (omega x r = v)
		Vector3f rotationalVelocityW = new Vector3f(0,0,0);
		System.out.println("Airfoil calculateAirfliff 119 rotationalVelocityinit: " + rotationalVelocityW);
		System.out.println("Airfoil calculateAirfliff 119 droneangularvelocity: " + this.getDrone().getAngularVelocity());
		System.out.println("Airfoil calculateAirfliff 120 centreofmass (hefboomW): " + this.getDrone().transformToWorldFrame(this.getCenterOfMass()));
		System.out.println("Airfoil calculateAirfliff 120 centreofmass (hefboomD): " + this.getCenterOfMass());
		Vector3f.cross(this.getDrone().getAngularVelocity(), this.getDrone().transformToWorldFrame(this.getCenterOfMass()), rotationalVelocityW);
		
//		System.out.println("Airfoil calculateAirfliff angularVelocity: " + this.getDrone().getAngularVelocity());
//		System.out.println("Airfoil calculateAirfliff centreofMass: " + this.getCenterOfMass());
		
		// velocity of the airfoil caused by the drones linear velocity
		Vector3f linearVelocityW = this.getDrone().getLinearVelocity();
		// airspeed = wind - airfoil velocity
		Vector3f.add(airSpeedW, rotationalVelocityW, airSpeedW);
		Vector3f.add(airSpeedW, linearVelocityW, airSpeedW);
		Vector3f.sub(airSpeedW, windW, airSpeedW);

		
		// transform the airSpeed vector to the drone frame
		Vector3f airSpeedD = this.getDrone().transformToDroneFrame(airSpeedW);

//		System.out.println("Airfoil calculateAirfliff 134 rotationalVel: " + rotationalVelocityW);
//		System.out.println("Airfoil calculateAirfliff 135 linearvelW: " + linearVelocityW);
//		System.out.println("Airfoil calculateAirfliff 136 windW: " + windW);
//		System.out.println("Airfoil calculateAirfliff 137 airspeedW: " + airSpeedW);
//		System.out.println("Airfoil calculateAirfliff 137 airspeedD: " + airSpeedD);
		
		// project airSpeedD on the surface, perpendicular to the rotationAxis of the AirFoil
		Vector3f rotationAxisD = this.getRotAxis();
//		System.out.println("Airfoil calculateAirfliff 140 rotationAxisD: " + rotationAxisD);
		Vector3f projectedAirspeedVectorD = new Vector3f(0, 0, 0);
		Vector3f.sub(airSpeedD, (Vector3f) rotationAxisD.scale(Vector3f.dot(airSpeedD, rotationAxisD)), projectedAirspeedVectorD);
		
//		System.out.println("Airfoil calculateAirfliff projectedairspeedD: " + projectedAirspeedVectorD);

		
		// calculate the angle of attack, defined as -atan2(S . N, S . A), where S
		// is the projected airspeed vector, N is the normal, and A is the attack vector
		
		Vector3f normalD = this.calculateNormal(); // N
		Vector3f attackVectorD = this.calculateAttackVector(); // A
//		float aoa = this.calculateAOA(projectedAirspeedVectorD, normalD, attackVectorD);
		float aoa = (float) - Math.atan2(Vector3f.dot(projectedAirspeedVectorD, normalD), 
											Vector3f.dot(projectedAirspeedVectorD, attackVectorD));	

//		System.out.println("Airfoil calculateAirfliff AOA: " + aoa);

		// calculate the lift force N . liftSlope . AOA . s^2, where N is the
		// normal, AOA is the angle of attack, and s is the projected airspeed
		float airspeedSquared = projectedAirspeedVectorD.lengthSquared();
//		System.out.println("Airfoil calculateAirfliff 160 Liftslope: " + this.getLiftSlope());
//		System.out.println("Airfoil calculateAirfliff 161 aoa : " + aoa);
//		System.out.println("Airfoil calculateAirfliff 162 airspeedSquared: " + airspeedSquared);
//		System.out.println("Airfoil calculateAirfliff 163 normalD: " + normalD);
		Vector3f liftForceD = (Vector3f) normalD.scale(this.getLiftSlope() * aoa * airspeedSquared);
//		System.out.println("Airfoil calculateAirfliff 165 liftforce: "  + liftForceD);
		return liftForceD;
	}
	

	private float calculateAOA(Vector3f projectedAirspeedVectorD, Vector3f normalD, Vector3f attackVectorD) {
		float currentAoa = (float) - Math.atan2(Vector3f.dot(projectedAirspeedVectorD, normalD), 
				Vector3f.dot(projectedAirspeedVectorD, attackVectorD));	
		
		if(currentAoa > this.getDrone().getMaxAOA()){
			while (currentAoa > this.getDrone().getMaxAOA()){
				currentAoa -= Math.PI;
			}
		}
		
		if(currentAoa < (- this.getDrone().getMaxAOA())){
			while (currentAoa < (- this.getDrone().getMaxAOA())){
				currentAoa += Math.PI;
			}
		}
		return currentAoa;
	}


	private final Drone drone;

	public Drone getDrone() {
		return this.drone;
	}
	
	
	//TODO Angle Calculation
}
