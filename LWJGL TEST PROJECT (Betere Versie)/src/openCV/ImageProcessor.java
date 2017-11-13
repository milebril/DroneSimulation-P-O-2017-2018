package openCV;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.lwjgl.util.vector.Vector3f;
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
		// rotate the cube to be in alignment with the world frame
		alignCube();

		// place the cube in front of the camera
		translate(0, 0, -5);
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
	 * Combines all the Mats in the given Mat array into 1 Mat object.
	 * @throws IllegalArgumentException if not all the Mat objects are of the same size.
	 * @throws IllegalArgumentException if not all the Mat objects are of the same type.
	 */
	public static Mat combineMatArray(Mat[] matArray) {
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
	 * Returns a byte[] array of the RGB values of the given BufferedImage.
	 */
	public static byte[] bufferedImageToByteArray(BufferedImage image) {
		
		// get width and height of the image
		int width = image.getWidth();
		int height = image.getHeight();
		
		// every int in this array consists of a red, green, blue and alpha component
		int[] pixelArray = image.getRGB(0, 0, width, height, null, 0, width);
		
		// create byte array to store the RGB values separately
		byte[] bytes = new byte[pixelArray.length*3];
		
		// for every pixel, place the RGB values in the byte array separately
		for (int i=0; i < pixelArray.length; i++) {
			bytes[i*3] = (byte) ((pixelArray[i] >> 16) & 0xFF); // red
			bytes[i*3 + 1] = (byte) ((pixelArray[i] >> 8) & 0xFF); // green
			bytes[i*3 + 2] = (byte) ((pixelArray[i] >> 0) & 0xFF); // blue
		}
		
		return bytes;
	}
	
	/**
	 * Returns a Mat[] array of the 6 different red cube hue's filtered from the given RGB Mat object.
	 * Order: pos x, neg x, pos y, neg y, pos z, neg z
	 */
	public static Mat[] redRGBMatFilter(Mat rgbMat) {
		
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
	// Focal length of the simulated pinhole camera
	
		private static double f = 100 / Math.tan(Math.PI / 3);
		

		
		private double x = 0;
		private double y = 0;
		private double z = 0;
		public double[] getPosition() {
			return new double[]{x, y, z};
		}
		public double getDistance() {
			return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
		}
		
		private List<double[]> corners = Arrays.asList(new double[]{-0.5,  0.5, -0.5}, new double[]{-0.5,  0.5,  0.5}, 
				new double[]{ 0.5,  0.5,  0.5}, new double[]{ 0.5,  0.5, -0.5}, new double[]{-0.5, -0.5, -0.5}, 
				new double[]{-0.5, -0.5,  0.5}, new double[]{ 0.5, -0.5,  0.5}, new double[]{ 0.5, -0.5, -0.5});
		
		//private List<double[]> corners = Arrays.asList(new double[]{1, 0, 0}, new double[]{0, 1,  0}, 
		//		new double[]{-1, 0, 0}, new double[]{-1.5, 0, 0}, new double[]{-2.59, 2.59, 0}, new double[]{2.59, -2.59, 0});
			
		public List<double[]> getCorners() {
			return this.corners;
		}
		
		
		// Setters
		
		/**
		 * Rotate the cube
		 */
		public void rotate(double heading, double pitch, double roll) {
			double x; double y; double z;
			heading = -heading;
			pitch = -pitch;
			roll = -roll;
			for (double[] point : getCorners()) {
				x = point[0]; y = point[1]; z = point[2];
				
				/* Without roll
				point[0] = x*Math.cos(heading) + z*Math.sin(heading);
				point[1] = y*Math.cos(pitch) - (z*Math.cos(heading) - x*Math.sin(heading))*Math.sin(pitch);
				point[2] = y*Math.sin(pitch) + (z*Math.cos(heading) - x*Math.sin(heading))*Math.cos(pitch);
				*/
				
				double a = x * (Math.cos(heading)*Math.cos(roll) - Math.sin(heading)*Math.sin(pitch)*Math.sin(roll))  + y * (-Math.cos(pitch)*Math.sin(roll)) + z * (Math.cos(roll)*Math.sin(heading) + Math.cos(heading)*Math.sin(pitch)*Math.sin(roll));
				double b = x * (Math.cos(heading)*Math.sin(roll) + Math.cos(roll)*Math.sin(heading)*Math.sin(pitch)) + y * (Math.cos(pitch)*Math.cos(roll)) + z * (Math.sin(heading)*Math.sin(roll) - Math.cos(heading)*Math.cos(roll)*Math.sin(pitch));
				double c = x * (-Math.cos(pitch)*Math.sin(heading)) + y * (Math.sin(pitch))+ z * (Math.cos(heading) * Math.cos(pitch));
				
				// heading (y-axis)
				x = x*Math.cos(heading) + z*Math.sin(heading);
				z = -x*Math.sin(heading) + z*Math.cos(heading);
				
				// pitch (x-axis)
				y = y*Math.cos(pitch) - z*Math.sin(pitch);
				z = y*Math.sin(pitch) + z*Math.cos(pitch);
				
				// roll (z-axis)
				x = x*Math.cos(roll) - y*Math.sin(roll);
				y = x*Math.sin(roll) + y*Math.cos(roll);
				// -> -roll ! ! !
				
				if (a != x || b != y || c != z) {
					//System.out.println(roll);
					//System.out.println(String.valueOf(x) + " - " + String.valueOf(y) + " - " + String.valueOf(z));
					//System.out.println(String.valueOf(a) + " - " + String.valueOf(b) + " - " + String.valueOf(c));
				}
				
				point[0] = a;
				point[1] = b;
				point[2] = c;
				
			}
			
		}

		/**
		 * Aligns the cube with the world frame
		 */
		private void alignCube() {
			this.rotate(getHeading(), getPitch(), getRoll());
		}
		
		
		
		/**
		 * Translates the imaginary cube, coordinates are in drone frame
		 */
		public void translate(double deltaX, double deltaY, double deltaZ) {
			for (double[] point : getCorners()) {
				point[0] += deltaX;
				point[1] += deltaY;
				point[2] += deltaZ;
			}
			x += deltaX;
			y += deltaY;
			z += deltaZ;
		}
		
		
		// Calculations
		
		/**
		 * Returns a list of the corners, projected using a pinhole camera
		 */
		private List<double[]> getProjectedCorners() {
			List<double[]> projectedCorners = new ArrayList<>();
			
			// project all points
			for (double[] point : getCorners()) {
				projectedCorners.add(new double[]{-ImageProcessor.f * point[0] / point[2], -ImageProcessor.f * point[1] / point[2]});
			}
			
			return projectedCorners;
		}
		
		/**
		 * Returns the convext hull of the given 2D coordinate list. The first and last coordinate are the same.
		 */
		public List<double[]> getConvextHull() {
			List<double[]> corners = getProjectedCorners();
			
			// map all double[] coordinates onto Point objects
			Map<Point, double[]> map = new HashMap<Point, double[]>();
			Point point;
			for (double[] corner : corners) {
				point = new Point((int) Math.round(corner[0] * 1000000), (int) Math.round(corner[1] * 1000000));
				map.put(point, corner);
			}
			
			// use the Point objects to get the convex hull
			List<Point> convexHullPoints = GrahamScan.getConvexHull(new ArrayList<Point>(map.keySet()));
			
			// get the double[] coordinates from the convex hull Point list
			List<double[]> convexHullCoordinates = new ArrayList<>();
			for (Point p : convexHullPoints) {
				convexHullCoordinates.add(map.get(p));
			}
			
			return convexHullCoordinates;
		}
		
		/**
		 * Returns the signed area of the given convex hull.
		 */
		private double getProjectedSingedArea(List<double[]> convexHull) {
			double cubeArea = 0;
			for (int i = 0; i < convexHull.size() - 1; i++) {
				cubeArea += convexHull.get(i)[0] * convexHull.get(i+1)[1];
				cubeArea -= convexHull.get(i)[1] * convexHull.get(i+1)[0];
			}
			return cubeArea / 2;
		}
		
		/**
		 * Returns the signed area of the projection of the cube.
		 */
		private double getProjectedSingedArea() {
			return getProjectedSingedArea(getConvextHull());
		}
		
		
		/**
		 * Returns how much percent of the image, the projected cubes area is.
		 */
		public double getProjectedAreaPercentage(float horAngleOfView, float verAngleOfView) {
			
			// calculate cube area
			double cubeArea = Math.abs(getProjectedSingedArea());
			
			// calculate total image area
			double width = 2 * ImageProcessor.f * Math.tan(horAngleOfView);
			double height = 2 * ImageProcessor.f * Math.tan(verAngleOfView);
			double totalArea = width * height;
			
			return cubeArea / totalArea;
		}
		
		/**
		 * Returns the center of mass of the projection of the cube. (0, 0) is the center of the image,
		 * (-1, y) the left border, (1, y) the right border, (x, -1) the bottom border, (x, 1) the top border.
		 */
		public double[] getProjectedAreaCenterOfMass(float horAngleOfView, float verAngleOfView) {
			List<double[]> convexHull = getConvextHull();
			double singedArea = getProjectedSingedArea(convexHull);
			
			double cx = 0;
			double cy = 0;
			for (int i = 0; i < convexHull.size() - 1; i++) {
				cx += (convexHull.get(i)[0] + convexHull.get(i+1)[0]) * (convexHull.get(i)[0]*convexHull.get(i+1)[1] - convexHull.get(i+1)[0]*convexHull.get(i)[1]);
				cy += (convexHull.get(i)[1] + convexHull.get(i+1)[1]) * (convexHull.get(i)[0]*convexHull.get(i+1)[1] - convexHull.get(i+1)[0]*convexHull.get(i)[1]);
			}
			cx /= (singedArea * 6);
			cy /= (singedArea * 6);
			
			return new double[]{cx / 100, cy / 100};
		}
		
		
		
//		public void saveAsImage(String imageName, Mat surface) {
//			Mat image = surface.clone();
//			
//			List<double[]> convexHull = getConvextHull();
//			org.opencv.core.Point p0;
//			org.opencv.core.Point p1;
//			for (int i = 0; i < convexHull.size() - 1; i++) {
//				p0 = new org.opencv.core.Point((int) (convexHull.get(i)[0]+100), (int) (-convexHull.get(i)[1]+100));
//				p1 = new org.opencv.core.Point((int) (convexHull.get(i+1)[0]+100), (int) (-convexHull.get(i+1)[1]+100));
//				Imgproc.line(image, p0, p1, new Scalar(255, 255, 255));
//			}
//			
//			List<double[]> punten = getProjectedCorners();
//			for (int i = 0; i < punten.size(); i++) {
//				//Imgproc.circle(image, new org.opencv.core.Point((int) (punten.get(i)[0]+100), (int) (-punten.get(i)[1]+100)), 1, new Scalar(120, 120, 120));
//				//Imgproc.circle(image, new org.opencv.core.Point((int) (punten.get(i)[0]+100), (int) (-punten.get(i)[1]+100)), 0, new Scalar(0, 0, 0));
//			}
//			Imgcodecs.imwrite("res/" + imageName + ".png", image);
//		}
		

		
		
		public Vector3f getCoordinatesOfCube() {
			
			// byteArray --> Mat object
			Mat rgbMat = byteArrayToRGBMat(getImageWidth(), getImageHeight(), getImage());
			
			
			// Filter RGB Mat for 6 different red Hue's
			Mat[] matArray = redRGBMatFilter(rgbMat);
			
			
			// Combine the 6 filtered Mats
			Mat filterMat = combineMatArray(matArray);
			
			
			// Get center of mass (of the 2D red cube)
			double[] centerOfMass = getType0CenterOfMass(filterMat);
			
			if (centerOfMass == null) {
				System.out.println("hier");
				return new Vector3f(0,0,0);
			}
			
			
			// Get red area in image
			int redArea = Core.countNonZero(filterMat);
			
			
			// Get red area percentage
			double percentage = redArea / ((float) getImageHeight()*getImageWidth());
			
			
			
			// create imaginary cube
			ImaginaryCube imaginaryCube = new ImaginaryCube(getHeading(), getPitch(), getRoll());
			
			double[] imCenterOfMass; double deltaX=10; double deltaY=10;
			double imPercentage; double ratio = 10;
			
			
			while (deltaX > 0.005 || deltaY > 0.005 || ratio > 1.025 || ratio < 0.975) {
				
				// get difference between the centers of mass
				imCenterOfMass = imaginaryCube.getProjectedAreaCenterOfMass((float) (120.0 / 180 * Math.PI), (float) (120.0 / 180 * Math.PI));
				deltaX =  (centerOfMass[0] - imCenterOfMass[0]);
				deltaY =  (centerOfMass[1] - imCenterOfMass[1]);
				
				imaginaryCube.translate(deltaX * 3, deltaY * 3, 0);
				
				// get the ration between the projected areas
				imPercentage = imaginaryCube.getProjectedAreaPercentage((float) (120.0 / 180 * Math.PI), (float) (120.0 / 180 * Math.PI));
				ratio = imPercentage / percentage;
				
				imaginaryCube.translate(0, 0, (1 - ratio)*0.1);
				
				//imaginaryCube.saveAsImage("result " + String.valueOf(iterations * 2 - 1), rgbMat);
			}
			

			return new Vector3f((float) (imaginaryCube.getPosition()[0]), 
					(float) (imaginaryCube.getPosition()[1]) , 
					(float) (imaginaryCube.getPosition()[2]));
		}
		
		/**
		 * Returns the center of mass of the given type 0 Mat object
		 * @throws IllegalArgumentException if the type of the mat is not 0
		 * @throws IllegalArgumentException if the total sum of x or y coordinates overflows.
		 */
		public static double[] getType0CenterOfMass(Mat mat) {
			// check wether the Mat type is 0
			if (mat.type() != 0) throw new IllegalArgumentException("given mat must be of type 0!");
			// variables to store the total x and y coordinates
			long xCoord = 0; long yCoord= 0; int amount = 0;
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
			double[] coordinates = new double[2];
			coordinates[0] = (int) Math.round(xCoord / (float) amount);
			coordinates[0] = (coordinates[0] - 100.0) / 100.0;
			coordinates[1] = (int) Math.round(yCoord / (float) amount);
			coordinates[1] = (100.0 - coordinates[1]) / 100.0;
			
			if (coordinates[0] == -1 && coordinates[1] == 1) {
				return null;
			}
			
			return coordinates;
		}
		


//		private Vector3f Vector3f(double d, double e, double g) {
//			return Vector3f(d,e,g);
//		}
	}
	

