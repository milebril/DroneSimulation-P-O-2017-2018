package autoPilotJar;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import autopilot.AutopilotConfig;
import autopilot.AutopilotConfigReader;
import openCV.RedCubeLocator;

public class AutoPilot {
	
	private static final float CRUISING_SPEED = 200;

	public AutoPilot(){
	}
	
	private float gravity;
	private float wingX;
	private float tailSize;
	private float engineMass;
	private float wingMass; 
	private float tailMass;
	private float maxThrust;
	private float maxAOA;
	private float wingLiftSlope;
	private float horStabLiftSlope;
	private float verStabLiftSlope;
	private float horizontalAngleOfView;
	private float verticalAngleOfView;
	public float getGravity() {
		return gravity;
	}

	public void setGravity(float gravity) {
		this.gravity = gravity;
	}

	public float getWingX() {
		return wingX;
	}

	public void setWingX(float wingX) {
		this.wingX = wingX;
	}

	public float getTailSize() {
		return tailSize;
	}

	public void setTailSize(float tailSize) {
		this.tailSize = tailSize;
	}

	public float getEngineMass() {
		return engineMass;
	}

	public void setEngineMass(float engineMass) {
		this.engineMass = engineMass;
	}

	public float getWingMass() {
		return wingMass;
	}

	public void setWingMass(float wingMass) {
		this.wingMass = wingMass;
	}

	public float getTailMass() {
		return tailMass;
	}

	public void setTailMass(float tailMass) {
		this.tailMass = tailMass;
	}

	public float getMaxThrust() {
		return maxThrust;
	}

	public void setMaxThrust(float maxThrust) {
		this.maxThrust = maxThrust;
	}

	public float getMaxAOA() {
		return maxAOA;
	}

	public void setMaxAOA(float maxAOA) {
		this.maxAOA = maxAOA;
	}

	public float getWingLiftSlope() {
		return wingLiftSlope;
	}

	public void setWingLiftSlope(float wingLiftSlope) {
		this.wingLiftSlope = wingLiftSlope;
	}

	public float getHorStabLiftSlope() {
		return horStabLiftSlope;
	}

	public void setHorStabLiftSlope(float horStabLiftSlope) {
		this.horStabLiftSlope = horStabLiftSlope;
	}

	public float getVerStabLiftSlope() {
		return verStabLiftSlope;
	}

	public void setVerStabLiftSlope(float verStabLiftSlope) {
		this.verStabLiftSlope = verStabLiftSlope;
	}

	public float getHorizontalAngleOfView() {
		return horizontalAngleOfView;
	}

	public void setHorizontalAngleOfView(float horizontalAngleOfView) {
		this.horizontalAngleOfView = horizontalAngleOfView;
	}

	public float getVerticalAngleOfView() {
		return verticalAngleOfView;
	}

	public void setVerticalAngleOfView(float verticalAngleOfView) {
		this.verticalAngleOfView = verticalAngleOfView;
	}

	public boolean isInitialised() {
		return isInitialised;
	}

	public void setInitialised(boolean isInitialised) {
		this.isInitialised = isInitialised;
	}

	public static float getCruisingSpeed() {
		return CRUISING_SPEED;
	}

	private byte[] image;
    private float x;
    private float oldX;
    private float y;
    private float oldY;
    private float z;
    private float oldZ;
    private float heading;
    private float pitch;
    private float roll;
    private float elapsedTime;
    private RedCubeLocator imageProcessor;
	private boolean isInitialised = false;
		
	public byte[] getImage() {
		return image;
	}

	public void setImage(byte[] image) {
		this.image = image;
	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.setOldX(this.getX());
		this.x = x;
	}

	public float getOldX() {
		return oldX;
	}

	public void setOldX(float oldX) {
		this.oldX = oldX;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.setOldY(this.getY());
		this.y = y;
	}

	public float getOldY() {
		return oldY;
	}

	public void setOldY(float oldY) {
		this.oldY = oldY;
	}

	public float getZ() {
		return z;
	}

	public void setZ(float z) {
		this.setOldZ(this.getZ());
		this.z = z;
	}

	public float getOldZ() {
		return oldZ;
	}

	public void setOldZ(float oldZ) {
		this.oldZ = oldZ;
	}

	public float getHeading() {
		return heading;
	}

	public void setHeading(float heading) {
		this.heading = heading;
	}

	public float getPitch() {
		return pitch;
	}

	public void setPitch(float pitch) {
		this.pitch = pitch;
	}

	public float getRoll() {
		return roll;
	}

	public void setRoll(float roll) {
		this.roll = roll;
	}

	public float getElapsedTime() {
		return elapsedTime;
	}

	public void setElapsedTime(float elapsedTime) {
		this.elapsedTime = elapsedTime;
	}

	public RedCubeLocator getImageProcessor() {
		return imageProcessor;
	}

	public void setImageProcessor(RedCubeLocator imageProcessor) {
		this.imageProcessor = imageProcessor;
	}

	//eerste ronde is er nog niet geïnitialiseerd en zijn er geen oude waarden
	public void sendToDrone() throws IOException{
		
		DataOutputStream s = new DataOutputStream(new FileOutputStream("res/APOutputs.cfg"));
		
		if (!isInitialised){
			
			initialize();

			AutopilotOutputs value = new AutopilotOutputs(){

				@Override
				public float getThrust() {return 0;}
				@Override
				public float getLeftWingInclination() {return calculateLeftWingIncl();}
				@Override
				public float getRightWingInclination() {return calculatRightWingIncl();}
				@Override
				public float getHorStabInclination() {return calculateHorStabIncl();}
				@Override
				public float getVerStabInclination() {return calculateVertStabIncl();}
			};
		}
		
		else{
			AutopilotOutputs value = new AutopilotOutputs(){
	
				@Override
				public float getThrust() {return calculateThrust();}
				@Override
				public float getLeftWingInclination() {return calculateLeftWingIncl();}
				@Override
				public float getRightWingInclination() {return calculatRightWingIncl();}
				@Override
				public float getHorStabInclination() {return calculateHorStabIncl();}
				@Override
				public float getVerStabInclination() {return calculateVertStabIncl();}
			};
		}
		
		AutopilotOutputsWriter.write(s, value);
		s.close();
	}
	
	protected float calculateVertStabIncl() {
		
	}

	protected float calculateHorStabIncl() {
		// TODO Auto-generated method stub
		
	}

	protected float calculatRightWingIncl() {
				
	}

	protected float calculateLeftWingIncl() {
		return this.calculatRightWingIncl();
	}

	private float getSpeed(){
		return (float) Math.sqrt(Math.pow((getX()-getOldX()), 2) + Math.pow((getY()-getOldY()), 2) + Math.pow((getZ()-getOldZ()), 2)) / this.getElapsedTime();
	}
	
	private float calculateThrust(){
		if (this.getSpeed() < CRUISING_SPEED){
			return this.getMaxThrust();
		}
		return 0f;
	}
	
	private float getHorizDeviation(){
		return (float) this.getImageProcessor().get2DCenterOfMassCoordinates()[0];
	}
	
	private float getVertDeviation(){
		return (float) this.getImageProcessor().get2DCenterOfMassCoordinates()[1];
	}
	
	private void initialize() throws IOException {
	
		DataInputStream inputStream = new DataInputStream(new FileInputStream("res/AutopilotConfig.cfg"));
		
		AutopilotConfig autopilotConfig = AutopilotConfigReader.read(inputStream);
		//nu alle benodigde waarden eruit halen
		
		this.setMaxThrust(autopilotConfig.getMaxThrust());
		
		this.setOldX(getX());
		this.setOldY(getY());
		this.setOldZ(getZ());	

		}

	public void getFromDrone() throws IOException {
		DataInputStream i = new DataInputStream(new FileInputStream("res/APOinputs"));
		
		AutopilotInputs inputs = AutopilotInputsReader.read(i);
		
		setElapsedTime(inputs.getElapsedTime());
		this.setHeading(inputs.getHeading());
		this.setImage(inputs.getImage());
		this.setPitch(inputs.getPitch());
		this.setRoll(inputs.getRoll());
		this.setX(inputs.getX());
		this.setY(inputs.getY());
		this.setZ(inputs.getZ());
	}
	
	
}
