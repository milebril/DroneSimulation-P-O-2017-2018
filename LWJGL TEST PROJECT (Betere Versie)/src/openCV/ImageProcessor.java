package openCV;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
		int[] i = getFilterCenterOfMass(getRedFilter(rgbMat));
		if (i == null) {
			i = new int[] { 100 ,100 };
		}
		return i;
	}
	
	public float getPixelsPerMeter() {
		Mat rgbMat = byteArrayToRGBMat(this.getImageWidth(), this.getImageHeight(), this.getImage());
		
		Mat totalFilter = getRedFilter(rgbMat);
		
		// Get 2D perspective size
		int perspectiveSize = Core.countNonZero(totalFilter);

		// 2D straight size
		double ratio = getAreaRatio(0,0,0);
		double size = perspectiveSize / ratio;

		float pixelsPerMeter = (float) Math.sqrt(size);
		
		return pixelsPerMeter;
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
	
	static double d = 2;
	static double f = 1;
	
	private double getAreaRatio(double heading, double pitch, double roll) {
		int factor = 1000000;
		double originalArea = 4.0/9;
		double rotatedArea;

		List<double[]> points = setRotatedPoints(d, heading, pitch, roll);

		// project all points
		List<java.awt.Point> projectedPoints = new ArrayList<>();
		int i;
		double x; double newX;
		double y; double newY;
		double z;
		for (i = 0; i < points.size(); i++) {
			x = points.get(i)[0]; y = points.get(i)[1]; z = points.get(i)[2];
			newX = (f / z) * x;
			newY = (f / z) * y;
			projectedPoints.add(new java.awt.Point((int) Math.round(newX * factor), (int) Math.round(newY * factor)));
			points.set(i, new double[]{newX, newY});
		}
		
		// get convex hull
		List<java.awt.Point> convexHullPoints = GrahamScan.getConvexHull(projectedPoints);
		ArrayList<double[]> convexHullCoordinates = new ArrayList<double[]>();
		for (i = 0; i < convexHullPoints.size(); i++) {
			convexHullCoordinates.add(new double[]{0,0});
		}
		
		for (i = 0; i < convexHullPoints.size(); i++) {
			for (double[] originalPoint : points) {
				if (Math.round(originalPoint[0] * factor) == convexHullPoints.get(i).x
						&& Math.round(originalPoint[1] * factor) == convexHullPoints.get(i).y) {
					convexHullCoordinates.set(i, originalPoint);
					// geprojeteerde hull coordinaat:
					//System.out.println(String.valueOf(originalPoint[0]) + "," +String.valueOf(originalPoint[1]));
				}
			}
		}
		
		
		// get 2D area
		double totaal = 0;
		for (i = 0; i < convexHullCoordinates.size() - 1; i++) {
			totaal += convexHullCoordinates.get(i)[0] * convexHullCoordinates.get(i+1)[1];
			totaal -= convexHullCoordinates.get(i)[1] * convexHullCoordinates.get(i+1)[0];
		}
		rotatedArea = Math.abs(totaal/2);
		
		//System.out.println(rotatedArea);
		return rotatedArea/originalArea;
	}
	
	/**
	 * Returns points
	 * @return geroteerde punten via transformatiematrices (heading, pitch en roll)
	 */
	
	private List<double[]> setRotatedPoints(double d, double heading, double pitch, double roll) {
		
		
		int i;
		double x; double newX;
		double y; double newY;
		double z; double newZ;
		List<double[]> points = setPositionCube();
		
		for (i = 0; i < points.size(); i++) {
			x = points.get(i)[0]; y = points.get(i)[1]; z = points.get(i)[2];
			newX = x * (Math.cos(heading)*Math.cos(roll) - Math.sin(heading)*Math.sin(pitch)*Math.sin(roll))  + y * (-Math.cos(pitch)*Math.sin(roll)) + z * (Math.cos(roll)*Math.sin(heading) + Math.cos(heading)*Math.sin(pitch)*Math.sin(roll));
			newY = x* (Math.cos(heading)*Math.sin(roll) + Math.cos(roll)*Math.sin(heading)*Math.sin(pitch)) + y * (Math.cos(pitch)*Math.cos(roll)) + z * (Math.sin(heading)*Math.sin(roll) - Math.cos(heading)*Math.cos(roll)*Math.sin(pitch));
			newZ = x * (-Math.cos(pitch)*Math.sin(heading)) + y * (Math.sin(pitch))+ z * (Math.cos(heading) * Math.cos(pitch));
			points.set(i, new double[]{newX, newY, newZ + d});
		}
		return points;
		
		
	}
	
	private List<double[]> setPositionCube() {

		
		int x = 0;
		int y = 0;

		double[] A = new double[]{x-0.5, y+0.5, -0.5};
		double[] B = new double[]{x-0.5, y+0.5,  0.5};
		double[] C = new double[]{x+0.5, y+0.5,  0.5};
		double[] D = new double[]{x+0.5, y+0.5, -0.5};
		double[] E = new double[]{x-0.5, y-0.5, -0.5};
		double[] F = new double[]{x-0.5, y-0.5,  0.5};
		double[] G = new double[]{x+0.5, y-0.5,  0.5};
		double[] H = new double[]{x+0.5, y-0.5, -0.5};
		
		List<double[]> points = Arrays.asList(A, B, C, D, E, F, G, H);
		return points;
		
	}
	
}
