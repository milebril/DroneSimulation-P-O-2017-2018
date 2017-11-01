package openCV;

import java.util.ArrayList;
import java.util.Arrays;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import autoPilotJar.Autopilot;

public class ImageProcessor {
	
	/* ImageProcessor constructor */
	
	public ImageProcessor(Autopilot autopilot) {
		this.autopilot = autopilot;
	}
	
	private final Autopilot autopilot;
	
	 //Getters for the AutoPilot config 
	
	public float getHorizontalAngleOfView() {
		return autopilot.getConfig().getHorizontalAngleOfView();
	}
	
	public float getVerticalAngleOfView() {
		return autopilot.getConfig().getVerticalAngleOfView();
	}
	
	public int getImageWidth() {
		return autopilot.getConfig().getNbColumns();
	}
	
	public int getImageHeight() {
		return autopilot.getConfig().getNbRows();
	}
	
	
	 //Getters for the AutoPilot input 
	
	public byte[] getImage() {
		return autopilot.getInput().getImage();
	}
	
	public float getHeading() {
		return autopilot.getInput().getHeading();
	}
	
	public float getPitch() {
		return autopilot.getInput().getPitch();
	}
	
	public float getRoll() {
		return autopilot.getInput().getRoll();
	}
	
	
	/* Getters for the image properties (cube detection) */
	
	/**
	 * Returns the coordinates of the center of mass of the cube area in the 2D image.
	 * x coord = [0, getImageWidth() - 1]
	 * y coord = [0, getImageHeight() - 1]
	 */
	public int[] get2DCenterOfMassCoordinates() {
		Mat rgbMat = byteArrayToRGBMat(getImageWidth(), getImageHeight(), getImage());
		return getFilterCenterOfMass(getRedFilter(rgbMat));
	}
	
	/**
	 * Returns the normalized coordinates of the center of mass of the cube area in the 2D image.
	 * x coord = [-1, 1]
	 * y coord = [-1, 1]
	 */
	public double[] getNormalized2DCenterOfMassCoordinates() {
		int[] coordinates = get2DCenterOfMassCoordinates();
		double[] normalizedCoordinates = new double[]{2 * (coordinates[0] + 1.0) / getImageWidth() - 1,
													2 * (coordinates[1] + 1.0) / getImageHeight() - 1};
		return normalizedCoordinates;
	}
	
	/**
	 * Returns the area the red cube occipies on the 2D image.
	 * area = [0, getImageWidth() * getImageHeight()]
	 */
	public int get2DRedCubeArea() {
		Mat rgbMat = byteArrayToRGBMat(getImageWidth(), getImageHeight(), getImage());
		return Core.countNonZero(getRedFilter(rgbMat));
	}
	
	/**
	 * Returns the horizontal angle between the forward vector of the drone and the
	 * vector that is aimed from the drone to the cube.
	 * angle = [-HorizontalAngleOfView, HorizontalAngleOfView] (from left to right)
	 */
	public double getHorizontalAngleToCube() {
		double[] normalizedCoordinates = getNormalized2DCenterOfMassCoordinates();
		
		double theta = getHorizontalAngleOfView() / 2;
		double w = getImageWidth() / 2;
		double f = Math.abs( w * (Math.cos(theta) / Math.sin(theta)) );
		
		return Math.atan2(normalizedCoordinates[0] * w, f);
	}
	
	/**
	 * Returns the vertical angle between the forward vector of the drone and the
	 * vector that is aimed from the drone to the cube.
	 * angle = [-VerticalAngleOfView, VerticalAngleOfView] (from bottom to top)
	 */
	public double getVerticalAngleToCube() {
		double[] normalizedCoordinates = getNormalized2DCenterOfMassCoordinates();
		
		double theta = getVerticalAngleOfView() / 2;
		double w = getImageHeight() / 2;
		double f = Math.abs( w * (Math.cos(theta) / Math.sin(theta)) );
		
		return Math.atan2(normalizedCoordinates[1] * w, f);
	}
	
	/** 
	 * Returns an array of the horizontal and vertical angles to the cube.
	 * @returns new double[]{getHorizontalAngleToCube(), getVerticalAngleToCube()}
	 */
	public double[] getAnglesToCube() {
		double[] angles = new double[]{getHorizontalAngleToCube(), getVerticalAngleToCube()};
		return angles;
	}
	
	
	/* Private static methods used for calculating the image properties */
	
	/**
	 * Returns a Mat object of given width and height containing the RGB values of the given byteArray.
	 */
	private static Mat byteArrayToRGBMat(int width, int height, byte[] byteArray) {
		// reads the given array as BGR (while the array is in RGB formar)
		Mat data = new Mat(height, width, CvType.CV_8UC3);
		data.put(0, 0, byteArray);
		
		// transform from BGR to RGB (to correct the input being read as BGR)
		Imgproc.cvtColor(data, data, Imgproc.COLOR_BGR2RGB);
		
		return data;
	}
	
	/**
	 * Returns a Mat object which is a filter for the 6 different red hue's.
	 */
	private static Mat getRedFilter(Mat rgbMat) {
		
		// turn the rgb Mat into a hsv Mat
		// (vreemd genoeg heeft BGR 2 HSV hier het gewenste effect en RGB 2 HSV niet)
		Mat hsvMat = new Mat();
		Imgproc.cvtColor(rgbMat, hsvMat, Imgproc.COLOR_BGR2HSV);
		
		// filter the 6 different red hue's 
		// Red Hue range: [0,10] & [160,179] Saturation is 255 and Value range depends on the surface
		// Values: pos x: 216, neg x: 76, pos y: 255, neg y: 38, pos z: 178, neg z: 114
		Mat[] matArray = new Mat[6];
		ArrayList<Integer> vValues = new ArrayList<>(Arrays.asList(216, 76, 255, 38, 178, 114));
		Mat tempMat1 = new Mat(hsvMat.height(), hsvMat.width(), 0, new Scalar(0));
		Mat tempMat2 = new Mat(hsvMat.height(), hsvMat.width(), 0, new Scalar(0));
		for (int i = 0; i < 6; i++) {
			// filter the hsv Mat
			Core.inRange(hsvMat, new Scalar(0,   255, vValues.get(i) - 3), new Scalar(10,  255, vValues.get(i) + 3), tempMat1);
			Core.inRange(hsvMat, new Scalar(160, 255, vValues.get(i) - 3), new Scalar(179, 255, vValues.get(i) + 3), tempMat2);
			// combine the 2 filtered Mat objects into 1
			Core.addWeighted(tempMat1, 1, tempMat2, 1, 0, tempMat1);
			// save the Mat object in the array
			matArray[i] = tempMat1.clone();
		}
		
		// combine the 6 filters
		Mat totalMat = new Mat(rgbMat.height(), rgbMat.width(), 0, new Scalar(0));
		for (int i = 0; i < matArray.length; i++)
			Core.addWeighted(matArray[i], 1, totalMat, 1, 0, totalMat);
		
		return totalMat;
	}
	
	/**
	 * Returns the 2D coordinates of the center of mass of the given filter.
	 * Returns null if the filter has no nonzero values.
	 */
	private static int[] getFilterCenterOfMass(Mat filter) {
		// variables to store the total x and y coordinates
		long xCoord = 0; long yCoord= 0; int amount = 0;
		
		// iterate over every x,y coordinate and add the coordinates if their value equals 255
		for (int x = 0; x < filter.width(); x++) {
			for (int y = 0; y < filter.height(); y++) {
				if (filter.get(y, x)[0] == 255) {
					xCoord += x; yCoord += y; amount++;
				}
			}
		}
		
		// if the amount is 0, the filter contains no nonzero values
		if (amount == 0) return null;
		
		// calculate the coordinates
		int[] coordinates = new int[]{(int) Math.round(xCoord / (float) amount), 
										(int) Math.round(yCoord / (float) amount)};
		
		return coordinates;
		
	}
	
}
