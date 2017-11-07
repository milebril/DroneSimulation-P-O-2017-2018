package entities;

import org.lwjgl.util.vector.Vector3f;

/**
 * all airfoils have a neutral attack vector oriented along (O,0,-1) expressed in the Drone Frame
 * @author Jakob
 *
 */
public class AirFoil {
	
	public AirFoil(Vector3f centerOfMass, float wingMass, float wingLiftSlope, Vector3f rotAx) {
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
		Vector3f.cross(this.getRotAxis(), this.calculateAttackVector(), result);
		return result;	
	}
	
	
	//TODO Angle Calculation
}
