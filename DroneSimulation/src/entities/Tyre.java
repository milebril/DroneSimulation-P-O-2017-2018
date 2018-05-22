package entities;

import org.lwjgl.util.vector.Vector3f;

import physicsEngine.PhysicsEngine;

public class Tyre {
	
	// CONSTRUCTOR
	
	public Tyre(Drone drone, Vector3f position, double radius, double maxBrakingForce, double maxFrictionCoeff, double tyreSlope, double dampSlope) {
		this.drone = drone;
		this.position = position;
		this.radius = radius;
		this.maxBrakingForce = maxBrakingForce;
		this.maxFrictionCoeff = maxFrictionCoeff;
		this.tyreSlope = tyreSlope;
		this.dampSlope = dampSlope;
	}
	
	// DRONE
	
	/**
	 * The drone to which this Tyre is attached.
	 */
	private final Drone drone;
	
	/**
	 * Returns the drone to which this Tyre is attached.
	 */
	public Drone getDrone() {
		return this.drone;
	}
	
	// POSITION
	
	/**
	 * The position of the Tyre in drone frame.
	 */
	private final Vector3f position;
	
	/**
	 * Returns the position of the Tyre in drone frame.
	 */
	public Vector3f getPosition() {
		return this.position;
	}
	
	// RADIUS
	
	/**
	 * The radius of this Tyre.
	 */
	private final double radius;
	
	/**
	 * Returns the radius of this Tyre.
	 */
	public double getRadius() {
		return this.radius;
	}
	
	// BRAKING
	
	/**
	 * The braking force applied by this Tyre.
	 */
	private double brakingForce = 0;
	
	/**
	 * Returns the braking force applied by this Tyre.
	 * The braking force is positive and smaller than the maxBrakingForce.
	 */
	public double getBrakingForce() {
		return this.brakingForce;
	}
	
	/**
	 * Sets the braking force of this Tyre.
	 * The braking force is kept between 0 and the maxBrakingForce.
	 */
	public void setBrakingForce(double brakingForce) {
		if (brakingForce <= 0){
			this.brakingForce = 0;
		} else if (brakingForce <= getMaxBrakingForce()) {
			this.brakingForce = brakingForce;
		} else {
			this.brakingForce = getMaxBrakingForce();
		}
	}
	
	// MAX BRAKING FORCE
	
	/**
	 * The maximum braking force this Tyre can apply.
	 */
	private final double maxBrakingForce;
	
	/**
	 * Returns the maximum braking force this Tyre can apply.
	 */
	public double getMaxBrakingForce() {
		return this.maxBrakingForce;
	}
	
	// MAX FRICTION COEFFICIENT
	
	/**
	 * The maximal friction coefficient.
	 */
	private final double maxFrictionCoeff;
	
	/**
	 * Returns the maximal friction coefficient.
	 */
	public double getMaxFrictionCoeff() {
		return this.maxFrictionCoeff;
	}
	
	// SLOPES
	
	/**
	 * The tyre slope of this Tyre.
	 */
	private final double tyreSlope;
	
	/**
	 * Returns the tyre slope of this Tyre.
	 */
	public double getTyreSlope() {
		return this.tyreSlope;
	}
	
	/**
	 * The damp slope of this Tyre.
	 */
	private final double dampSlope;
	
	/**
	 * Returns the damp slope of this Tyre.
	 */
	public double getDampSlope() {
		return this.dampSlope;
	}
	
	// COMPRESSION
	
	/**
	 * The save compression
	 */
	private double savedCompression;
	
	/**
	 * Get the saved compression.
	 */
	public double getSavedCompression() {
		return this.savedCompression;
	}
	
	/**
	 * Save a new compression.
	 */
	public void saveCompression(double compression) {
		this.savedCompression = compression;
	}
	
	/*
	 * !! "Moddeleer elk wiel als een schijf..." !!
	 * De indrukking van het wiel
	 * -> bepaald door de afstand tussen het centrum van het wiel en de grond
	 * 			~ Tyre y-coord - PhysicsEngine.groundLevel
	 * -> bepaald door de orientatie van het wiel tov de grond
	 * 			~ hoek tussen 2 vlakken: xz-vlak & vlak waarin het wiel ligt
	 */
	
	/**
	 * Returns how much this Tyre is compressed in function of its current location
	 * and the ground level.
	 * @Deprecated
	 */
	@Deprecated
	public double getCompressionOld() {
		
		// the position of this Tyre in world frame
		Vector3f position = new Vector3f(0, 0, 0);
		Vector3f.add(getDrone().transformToWorldFrame(getPosition()), getDrone().getPosition(), position);
		
		double deltaY = position.y - PhysicsEngine.groundLevel;
		
		// if the center of the tyre is too far from the ground, the tyre can't be compressed
		if (deltaY >= getRadius()) return 0;
		
		
		// calculate the angle between the tyre and the ground
		Vector3f xzNormal = new Vector3f(0, 1, 0); // normal of the ground level
		Vector3f tyreNormal = getDrone().transformToWorldFrame(new Vector3f(1, 0, 0)); // normal of the tyre
		
		// angle between two planes is equal to the angle between their normals
		float angle = Vector3f.angle(xzNormal, tyreNormal);
		
		// if the tyre is perpendicular to the xz-plane, the tyre can't be compressed
		if (Math.abs(angle) % Math.PI == 0) return 0;
		
		
		double distance = getRadius() / Math.sin(angle);
		
		if (distance >= radius) return 0;
		else return radius - distance;
	}
	
	/**
	 * Returns how much this Tyre is compressed in function of its current location
	 * and the ground level.
	 */
	public double getCompression() {
		
		// the position of this Tyre in world frame
		Vector3f position = new Vector3f(0, 0, 0);
		Vector3f.add(getDrone().transformToWorldFrame(getPosition()), getDrone().getPosition(), position);
		
		double deltaY = position.y - PhysicsEngine.groundLevel;
		
		// if the center of the tyre is too far from the ground, the tyre can't be compressed
		if (deltaY >= getRadius()) return 0;
		
		// else
		else return (getRadius() - deltaY);
	}
	
	// GROUNDED
	
	/**
	 * Returns whether this Tyre is touching the ground.
	 */
	public boolean isGrounded() {
		return 0 < getCompression();
	}
	
	/**
	 * Returns where the drone touches the ground (in drone frame)
	 * returns null if the tyre is not grounded.
	 */
	public Vector3f getGroundedPosition() {
		if (!isGrounded()) return null;
		else {
			Vector3f worldPosition = getDrone().transformToWorldFrame(getPosition());
			worldPosition.y = 0;
			return getDrone().transformToDroneFrame(worldPosition);
		}
	}
	
	
	/**
	 * Returns at which position (in drone frame) this Tyre touches the ground and the
	 * forward and sideward orientation: {grounded position, rolling orientation, sliding orientation}
	 * Returns null if the Tyre is not grounded
	 * @deprecated function was incorrect
	 */
	@Deprecated
	public Vector3f[] getGroundedProperties() {
		if (!isGrounded()) return null;
		
		// normaal v.d. Tyre (in world frame)
		Vector3f Tyrenormal = getDrone().transformToWorldFrame(new Vector3f(1, 0, 0));
		
		// normaal v.d. ground (in world frame)
		Vector3f groundNormal = new Vector3f(0, 1, 0);
		
		// kruisproduct v.d. normalen geeft de orientatie v.d. intersection lijn (aka voorwaartse orientatie)
		Vector3f intersectionOrientation = new Vector3f();
		Vector3f.cross(groundNormal, Tyrenormal, intersectionOrientation);
		intersectionOrientation.normalise();
		
		// Zoek een willekeurig punt op de intersection (geweten is dat y = 0 adhv het grondvlak)
		Vector3f intersectionPoint = new Vector3f(1, 0, intersectionOrientation.z/intersectionOrientation.x);
		
		// Positie v.h. Tyre centrum t.o.v. het intersection point
		Vector3f center = new Vector3f();
		Vector3f.sub(getDrone().transformToWorldFrame(getPosition()), intersectionPoint, center);
		
		// projectie van v.h. relatieve tyre centrum op de intersection lijn en berekening van het raakpunt
		intersectionOrientation.scale(Vector3f.dot(center, intersectionOrientation));
		Vector3f projection = new Vector3f();
		Vector3f.add(intersectionPoint, intersectionOrientation, projection);
		projection = getDrone().transformToDroneFrame(projection);
		
		// zijwaartse orientatie = loodrechte op de intersectie lijn en de normaal v.h. grondvlak
		intersectionOrientation.normalise();
		Vector3f sideWays = new Vector3f();
		Vector3f.cross(intersectionOrientation, new Vector3f(0, 1, 0), sideWays);
		
		// transform the results to drone frame
		projection = getDrone().transformToDroneFrame(projection);
		intersectionOrientation = getDrone().transformToDroneFrame(intersectionOrientation);
		sideWays = getDrone().transformToDroneFrame(sideWays);
		
		return new Vector3f[] {projection, intersectionOrientation, sideWays};
	}
	
	
	
	
	
	
	
	
}
