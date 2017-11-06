package entities;

import org.lwjgl.util.vector.Vector3f;

/**
 * all airfoils have a neutral attack vector oriented along (O,0,-1) expressed in the Drone Frame
 * @author Jakob
 *
 */
public class AirFoil {
	
	private Vector3f centerOfMass;
	private Vector3f rotAx;
	private float airFoilMass;
	private float airFoilLiftSlope;
	private float inclination = 0;

	
	public AirFoil(Vector3f centerOfMass, float wingMass, float wingLiftSlope, Vector3f rotAx) {
		this.centerOfMass = centerOfMass;
		this.airFoilMass = wingMass;
		this.airFoilLiftSlope = wingLiftSlope;
		this.rotAx = rotAx;		
	}
	
	/**
	 * 
	 * @return copy of vector 
	 */
	public Vector3f getCenterOfMass() {
		Vector3f result = new Vector3f();
		result.x = this.centerOfMass.x;
		result.y = this.centerOfMass.y;
		result.z = this.centerOfMass.z;
		return result;
	}

	public void setCenterOfMass(Vector3f centerOfMass) {
		this.centerOfMass = centerOfMass;
	}

	public float getMass(){
		return this.airFoilMass;
	}
	/**
	 * 
	 * @return copy of the rotation axis
	 */
	public Vector3f getRotAxis(){
		Vector3f result = new Vector3f();
		result.x = this.rotAx.x;
		result.y = this.rotAx.y;
		result.z = this.rotAx.z;
		return result;
	}
	
	public void setInclination(float angleInRad){
		this.inclination = angleInRad;
	}
	
	public float getInclination(){
		return this.inclination;
	}
	
	public void setLiftSlope(float slope){
		this.airFoilLiftSlope = slope;
	}
	
	public float getLiftSlope(){
		return this.airFoilLiftSlope;
	}
	
	/**
	 * only two different rotation axes are possible: x-axis or y-axis. all neutral attack vectors
	 * are (0,0,-1), so the result after the rotation can be shortcut without matrices.
	 * @return
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
	 * normal is defined as cross product of axis vector and attack vector
	 * @return
	 */
	public Vector3f calculateNormal(){
		Vector3f result = new Vector3f();
		Vector3f.cross(this.getRotAxis(), this.calculateAttackVector(), result);
		return result;	
	}

	//TODO Angle Calculation
}
