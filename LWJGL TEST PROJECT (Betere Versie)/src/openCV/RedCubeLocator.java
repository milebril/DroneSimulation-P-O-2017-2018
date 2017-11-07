package openCV;
import java.util.ArrayList;

import java.util.Arrays;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.lwjgl.util.vector.Vector3f;

public class RedCubeLocator {
	
	private static final Mat Mat = null;
	/**
	 * Creates a RedCubeLocator object which processes the given image data to locate the red cubes
	 * co�rdinates relative to the drone.
	 * @param image
	 * 		  a byte array of the image data (from the pinhole camera) in RGB format
	 * @param nbColumns
	 * 		  the height of the image (in pixels)
	 * @param nbRows
	 * 		  the width of the image (in pixels)
	 * @param horizontalAngleOfView
	 * 		  the horizontal angle of view of the pinhole camera (in radiants)
	 * @param verticalAngleOfView
	 * 		  the vertical angle of view of the pinhole camera (in radiants)
	 * @throws IllegalArgumentException 
	 * 		 | if the given image dimensions do not match the length of the given byte array
	 * 		 | image.length != nbColumns * nbRows * 3
	 * @throws IllegalArgumentException 
	 * 		 | if the given image dimensions cannot correspond with the given angles of view
	 * 		 | (A margin EPSILON is used to account for rounding errors)
	 * 		 | Math.abs( Math.sin(horizontalAngleOfView) / Math.sin(verticalAngleOfView) - nbColumns / nbRows ) > EPSILON
	 */
	public RedCubeLocator(byte[] image, int nbColumns, int nbRows, 
			float horizontalAngleOfView, float verticalAngleOfView) throws IllegalArgumentException {
		if (image.length != nbColumns * nbRows * 3) {
			throw new IllegalArgumentException("Given image dimensions do not match the length of the given byte array");
		}
		if (Math.abs( Math.sin(horizontalAngleOfView) / Math.sin(verticalAngleOfView) - nbColumns / nbRows ) > EPSILON) {
			throw new IllegalArgumentException("Given image dimensions cannot correspond with the given angles of view");
		}
		this.imageByteArray = image;
		this.imageHeight = nbRows;
		this.imageWidth = nbColumns;
		this.horizontalAngleOfView = horizontalAngleOfView;
		this.verticalAngleOfView = verticalAngleOfView;
		
		// process the data
		this.process();
	}
	
	/** Default values for the horizontal angles of view are 120 degrees */
	public RedCubeLocator(byte[] image, int nbColumns, int nbRows) {
		this(image, nbColumns, nbRows, (float) (120 / 180 * Math.PI), (float) (120 / 180 * Math.PI));
	}
	
	/** Default values for the image height and width are 200 pixels */
	public RedCubeLocator(byte[] image) {
		this(image, 200, 200);
	}
	
	
	
	/** Margin for checking the input dimensions vs angles of view */
	public static double EPSILON = 0.01;
	
	
	/** A byte array containing the RGB data of the input image */
	private final byte[] imageByteArray;
	public byte[] getImageByteArray() {
		return this.imageByteArray;
	}
	
	/** Variables containing the height and width of the input image */
	private final int imageHeight;
	private final int imageWidth;
	public int getImageHeight() {
		return this.imageHeight;
	}
	public int getImageWidth() {
		return this.imageWidth;
	}
	
	/** Variables containing the angles of view of the input image (which is taken by a pinhole camera) */
	private final float horizontalAngleOfView;
	private final float verticalAngleOfView;
	public float getHorizontalAngleOfView() {
		return this.horizontalAngleOfView;
	}
	public float getVerticalAngleOfView() {
		return this.verticalAngleOfView;
	}
	
	
	/** Co�rdinates of the center of mass of the cube in the given image (origin in top left corner) */
	private double xCoordInImage;
	private double yCoordInImage;
	
	/** 
	 * Returns the co�rdinates of the center of mass of the cube in the given image 
	 * The origin of the coordinate is in the top left corner with the leftmost and topmost
	 * pixel having co�rdinates (0, 0) 
	*/
	public double[] get2DCenterOfMassCoordinates() {
		double[] coords = new double[2];
		coords[0] = this.xCoordInImage;
		coords[1] = this.yCoordInImage;
		return coords;
	}
	
	/** 
	 * Returns the co�rdinates of the center of mass of the cube in the given image 
	 * The origin of the coordinate is in the top left corner with the leftmost and topmost
	 * pixel having co�rdinates (0, 0) 
	*/
	public Vector3f getCoordinatesOfCube() {
		// byteArra --> Mat object
		Mat rgbMat = byteArrayToRGBMat(this.getImageWidth(), this.getImageHeight(), this.getImageByteArray());
				
		// Filter RGB Mat for 6 different red Hue's
		Mat[] matArray = redRGBMatFilter(rgbMat);
		Mat totalFilter = combineMatArray(matArray);
				
		// Get center of mass of the 6 filters
		int[] centerOfMass = getType0CenterOfMass(combineMatArray(matArray));
		this.xCoordInImage = (centerOfMass[0] + 1) / (0.5 * getImageWidth()) - 1; 
		this.yCoordInImage = -1 * ((centerOfMass[1] + 1) / (0.5 * getImageHeight()) - 1);
		// Get 2D perspective size
		int perspectiveSize = Core.countNonZero(totalFilter);
		//System.out.println("perspective Size: " + String.valueOf(perspectiveSize));
		// 2D straight size
		double ratio = getAreaRatio(0,0,0);
		double size = perspectiveSize / ratio;
//		System.out.println("perspectiveSize: " + String.valueOf(perspectiveSize));
//		System.out.println("ratio: " + String.valueOf(ratio));
//		System.out.println("size: " + String.valueOf(size));
		
		// 2D pixels for 1 meter
		float pixelsPerMeter = (float) Math.sqrt(size);
		//System.out.println("pixels per meter: " + String.valueOf(pixelsPerMeter));
		//System.out.println("center " + centerOfMass[0]);

		double W = 200 / pixelsPerMeter;
		double hoek = (120.0 / 180) * Math.PI;
//		System.out.println("schermbreedte in meter: " + String.valueOf(W));
		
		double afstand = (W / 2) * (Math.cos(hoek/2) / Math.sin(hoek/2))+0.5;
		return new Vector3f(centerOfMass[0]/pixelsPerMeter,centerOfMass[1]/pixelsPerMeter,(float) -afstand);
		
	}
	
	
	
	
	/** Processes the data of this object. */
	private void process() throws IllegalArgumentException {
		
		// byteArra --> Mat object
		//Mat rgbMat = byteArrayToRGBMat(this.getImageWidth(), this.getImageHeight(), this.getImageByteArray());
		
		// Filter RGB Mat for 6 different red Hue's
		//Mat[] matArray = redRGBMatFilter(rgbMat);
		//Mat totalFilter = combineMatArray(matArray);
		
		// Get center of mass of the 6 filters
		//int[] centerOfMass = getType0CenterOfMass(combineMatArray(matArray));
		//this.xCoordInImage = (centerOfMass[0] + 1) / (0.5 * getImageWidth()) - 1; 
		//this.yCoordInImage = -1 * ((centerOfMass[1] + 1) / (0.5 * getImageHeight()) - 1);
		

		System.out.println(getCoordinatesOfCube());
		//W = 10*2 / (Math.cos(hoek/2) / Math.sin(hoek/2));
//		System.out.println(W);
//		System.out.println(200 / pixelsPerMeter);
		
		// Get the biggest filter
		//Mat biggestFilter = getBiggestFilter(matArray);
		
		// Apply the filter on the rgb Mat object
		//Mat filteredrgbMat = applyFilterMat(rgbMat, biggestFilter);
		
		// RGB Mat to blurred grayscale Mat
		//Mat blurredGrayMat = rgbToBlurredGrayScaleMat(filteredrgbMat);
		
		//this.cornerC�ordinates = getCornerList(blurredGrayMat);
		// Get corner contours of the blurred grayscale Mat
	}
	
	
	/**
	 * Returns a Mat object of given width and height containing the RGB values of the given byteArray.
	 * System.loadLibrary(Core.NATIVE_LIBRARY_NAME) must have been excecuted once before calling this method!
	 * @throws IllegalArgumentException if the given byteArray length is not equal to width*height*3.
	 * 									(because each pixel needs exactly 3 values: red, green, blue)
	 */
	private static Mat byteArrayToRGBMat(int width, int height, byte[] byteArray) 
			throws IllegalArgumentException {
		if (width*height*3 != byteArray.length) {
			throw new IllegalArgumentException("given byteArray has " + String.valueOf(byteArray.length) 
			+ " values but " + String.valueOf(width*height*3) + " (=width*height*3) are required!");
		}
		
		// data reads the given array as BGR
		System.out.println(String.valueOf(width) + " " +  String.valueOf(height) + " " + String.valueOf(byteArray.length) );
		Mat data = new Mat(height, width, CvType.CV_8UC3);
		data.put(0, 0, byteArray);
		
		// the input was RGB instead of BGR so transform...
		Imgproc.cvtColor(data, data, Imgproc.COLOR_BGR2RGB);
		
		return data;
	}
	
	/**
	 * Returns a Mat[] array of the 6 different red cube hue's filtered from the given RGB Mat object.
	 * Order: pos x, neg x, pos y, neg y, pos z, neg z
	 */
	private static Mat[] redRGBMatFilter(Mat rgbMat) {
		
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
		int i;
		for (i = 0; i < 6; i++) {
			// filter the hsv Mat
			Core.inRange(hsvMat, new Scalar(0,   255, vValues.get(i) - 3), new Scalar(10,  255, vValues.get(i) + 3), tempMat1);
			Core.inRange(hsvMat, new Scalar(160, 255, vValues.get(i) - 3), new Scalar(179, 255, vValues.get(i) + 3), tempMat2);
			// combine the 2 filtered Mat objects into 1
			Core.addWeighted(tempMat1, 1, tempMat2, 1, 0, tempMat1);
			// save the Mat object in the array
			matArray[i] = tempMat1.clone();
		}
		
		return matArray;
	}
	
	/** 
	 * Combines all the Mats in the given Mat array into 1 Mat object.
	 * @throws IllegalArgumentException if not all the Mat objects are of the same size.
	 * @throws IllegalArgumentException if not all the Mat objects are of the same type.
	 */
	private static Mat combineMatArray(Mat[] matArray) {
		// get the width, height and type of the Mat objects from the first one in the array
		int width = matArray[0].width();
		int height = matArray[0].height();
		int type = matArray[0].type();
		int channels = matArray[0].channels();
		
		// assert that all Mat objects are of the same size and type
		int i;
		for (i = 1; i < matArray.length; i++) {
			if (matArray[i].width() != width || matArray[i].height() != height)
				throw new IllegalArgumentException("not all Mat objects are of the same size!");
			if (matArray[i].type() != type)
				throw new IllegalArgumentException("not all Mat objects are of the same type!");
		}
		
		// get an empty Scalar of correct dimension
		Scalar sc;
		switch (channels) {
		case 1:
			sc = new Scalar(0);
			break;
		case 2:
			sc = new Scalar(0, 0);
			break;
		case 3:
			sc = new Scalar(0, 0, 0);
			break;
		case 4:
			sc = new Scalar(0, 0, 0, 0);
			break;
		default:
			sc = new Scalar(0);
		}
		
		// combine all the Mat objects into one
		Mat totalMat = new Mat(height, width, type, sc);
		for (i = 0; i < matArray.length; i++)
			Core.addWeighted(matArray[i], 1, totalMat, 1, 0, totalMat);
		
		// return a copy of the total Mat
		return totalMat.clone();
	}
	
	/**
	 * Returns the type 0 Mat object with the highest non zero value.
	 * All Mat objects that are not type 0 are ignored.
	 */
	private static Mat getBiggestFilter(Mat[] matArray) {
		// the bigges filter
		Mat biggestFilter = null;
		// loop variables
		int maxSize = 0; int nonZeroCount;
		// loop
		for (Mat mat : matArray) {
			// get the non zero count of the current mat
			nonZeroCount = Core.countNonZero(mat);
			// if the current mat has a higher non zero count, save it
			if (maxSize <= nonZeroCount) {
				maxSize = nonZeroCount;
				biggestFilter = mat;
			}
		}
		return biggestFilter.clone();
	}
	
	/**
	 * Returns the center of mass of the given type 0 Mat object
	 * @throws IllegalArgumentException if the type of the mat is not 0
	 * @throws IllegalArgumentException if the total sum of x or y coordinates overflows.
	 */
	private static int[] getType0CenterOfMass(Mat mat) {
		// check wether the Mat type is 0
		if (mat.type() != 0) throw new IllegalArgumentException("given mat must be of type 0!");
		// variables to store the total x and y coordinates
		long xCoord = 0; long yCoord= 0; int amount = 0; int s = 100;
		// iterate over every x,y coordinate and add the coordinates if their value equals 255
		int x; int y;
		for (x = 0; x < mat.width(); x++) {
			for (y = 0; y < mat.height(); y++) {
				if (mat.get(y, x)[0] == 255) {
					if (Long.MAX_VALUE - xCoord < x || Long.MAX_VALUE - yCoord < y)
						throw new IllegalArgumentException("total sum of x or y coordinates overflows!");
					xCoord += x; yCoord += y; amount++;
				}
			}
		}
		// calculate the coordinates
		int[] coordinates = new int[2];
		coordinates[0] = (int) Math.round(xCoord / (float) amount);
		coordinates[0] = coordinates[0] - s;
		coordinates[1] = (int) Math.round(yCoord / (float) amount);
		coordinates[1] = s - coordinates[1] ;
		System.out.println(coordinates[0] + " " +  coordinates[1]);
		return coordinates;
	}
	
	/**
	 * Returns a Mat object on which the given filter is applied.
	 * @throws IllegalArgumentException if the given Mat objects are not of the same dimensions.
	 */
	private static Mat applyFilterMat(Mat scr, Mat filter) {
		if (scr.height() != filter.height() || scr.width() != filter.width())
			throw new IllegalArgumentException("Mat src and Mat filter must be of same dimensions!");
		Mat filteredMat = new Mat(scr.height(), scr.width(), CvType.CV_8UC3, new Scalar(0,0,0));
		Core.bitwise_and(scr, scr, filteredMat, filter);
		return filteredMat;
	}
	
	/**
	 * Returns a blurred grayscale version of the given rgb Mat object.
	 */
	private static Mat rgbToBlurredGrayScaleMat(Mat scr) {
		// scr to grayscale Mat
		Mat grayMat = new Mat(scr.height(), scr.width(), 0, new Scalar(0));
		Imgproc.cvtColor(scr, grayMat, Imgproc.COLOR_RGB2GRAY);
		
		// Blur filtered grayscale Mat
		Mat blurredGrayMat = new Mat(scr.height(), scr.width(), 0, new Scalar(0));
		Imgproc.GaussianBlur(grayMat, blurredGrayMat, new Size(3, 3), 0);
		
		return blurredGrayMat;
	}
	
	/**
	 * Returns a list of the corners. The src should be a blurred grayscale Mat
	 */
	private static List<int[]> getCornerList(Mat src) {
		// get corner Mat
		Mat cornerMat = new Mat();
		Imgproc.cornerHarris(src, cornerMat, 5, 3, 0.04);
		Imgproc.threshold(cornerMat, cornerMat, 0.0000000001, 255, Imgproc.THRESH_BINARY);
		
		// Get contours of the corners
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		cornerMat.convertTo(cornerMat, CvType.CV_8UC1);
		Imgproc.findContours(cornerMat, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		
		
		// Get the contour centers
		List<int[]> contourCenters = new ArrayList<int[]>();
		int i; int j;
		int x; int y;
		org.opencv.core.Point[] contour;
		for (i = 0; i < contours.size(); i++) {
			x = 0; y = 0;
			contour = contours.get(i).toArray();
			for (j = 0; j < contour.length; j++) {
				x += contour[j].x;
				y += contour[j].y;
			}
			x = Math.round(x / (float) contour.length);
			y = Math.round(y / (float) contour.length);
			contourCenters.add(new int[]{x, y});
		}
		return contourCenters;
	}

	/**
	 * Returns a Mat object that contains the src Mat with 
	 * a circle added around every point in the given list.
	 */
	private static Mat addPointsToMat(Mat src, List<int[]> points) {
		// create a clone of the src Mat
		Mat returnMat = src.clone();
		// add all the points to the cloned Mat
		for (int[] point : points) {
			Imgproc.circle(returnMat, new org.opencv.core.Point(point[0], point[1]), 4, new Scalar(0,0,0));
		}
		return returnMat;
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
	
	
	
	
	static double d = 2;
	static double f = 1;
	

	
	
	
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
	/**
	 * Returns rotatedArea/originalArea
	 * @return gedraaide opp / rechte opp
	 */
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
	
	
	
	
	
}