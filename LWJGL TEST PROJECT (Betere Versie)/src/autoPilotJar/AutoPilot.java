package autoPilotJar;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import openCV.RedCubeLocator;

public class AutoPilot {
	
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

	public AutoPilot() {
		System.out.println("hoi");
		// TODO Auto-generated constructor stub
	}
	
	public void sendToDrone() throws IOException{
		DataOutputStream s = new DataOutputStream(new FileOutputStream("res/APOutputs.cfg"));
		
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
		
	}
	
	private float calculateThrust(){		
	}
	
	private float getHorizDeviation(){
		return (float) this.getImageProcessor().get2DCenterOfMassCoordinates()[0];
	}
	
	private float getVertDeviation(){
		return (float) this.getImageProcessor().get2DCenterOfMassCoordinates()[1];
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
