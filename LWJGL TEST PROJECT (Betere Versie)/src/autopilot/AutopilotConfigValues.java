package autopilot;

public class AutopilotConfigValues implements AutopilotConfig{

	private float gravity = 9.81f;
	private float wingX = 15.0f;
	private float tailSize = 10.0f;
	private float engineMass = 50.0f;
	private float wingMass = 70.0f;
	private float tailMass = 100.0f;
	private float maxThrust = 1000.0f;
	private float maxAOA = (float) (Math.PI/4);
	private float wingLiftSlope = 100.0f;
	private float horStabLiftSlope = 10.0f;
	private float verStabLiftSlope = 10.0f;
	private float horizontalAngleOfView = 120.0f;
	private float verticalAngleOfView = 120.0f;
	private int nbColumns = 200;
	private int nbRows = 200;
	
	@Override
	public float getGravity() {
		return this.gravity;
	}

	@Override
	public float getWingX() {
		return this.wingX;
	}

	@Override
	public float getTailSize() {
		return this.tailSize;
	}

	@Override
	public float getEngineMass() {
		return this.engineMass;
	}

	@Override
	public float getWingMass() {
		return this.wingMass;
	}

	@Override
	public float getTailMass() {
		return this.tailMass;
	}

	@Override
	public float getMaxThrust() {
		return this.maxThrust;
	}

	@Override
	public float getMaxAOA() {
		return this.maxAOA;
	}

	@Override
	public float getWingLiftSlope() {
		return this.wingLiftSlope;
	}

	@Override
	public float getHorStabLiftSlope() {
		return this.horStabLiftSlope;
	}

	@Override
	public float getVerStabLiftSlope() {
		return this.verStabLiftSlope;
	}

	@Override
	public float getHorizontalAngleOfView() {
		return this.horizontalAngleOfView;
	}

	@Override
	public float getVerticalAngleOfView() {
		return this.verticalAngleOfView;
	}

	@Override
	public int getNbColumns() {
		return this.nbColumns;
	}

	@Override
	public int getNbRows() {
		return this.nbRows;
	}

}
