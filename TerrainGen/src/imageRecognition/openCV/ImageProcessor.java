package imageRecognition.openCV;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.util.vector.Vector3f;

import autopilot.algorithmHandler.Properties;
import prevAutopilot.SimpleAutopilot;

public class ImageProcessor {
	
	/* ImageProcessor constructor */
	
	public ImageProcessor(Properties properties) {
		this.properties = properties;
		// rotate the cube to be in alignment with the world frame
		alignCube();

		// places the cube 5 meter in front of the camera 
		translate(0, 0, -5);
	}
	
	private final Properties properties;
	
	//Getters for the AutoPilot config 
	
	public float getHorizontalAngleOfView() {
		return properties.getHorizontalAngleOfView();
	}
	
	public float getVerticalAngleOfView() {
		return properties.getVerticalAngleOfView();
	}
	
	public int getImageWidth() {
		return properties.getNbColumns();
	}
	
	public int getImageHeight() {
		return properties.getNbRows();
	}
	
	
	//Getters for the AutoPilot input 
	
	public byte[] getImage() {
		return properties.getImage();
	}
	
	public float getHeading() {
		return properties.getHeading();
	}
	
	public float getPitch() {
		return properties.getPitch();
	}
	
	public float getRoll() {
		return properties.getRoll();
	}
	
	
	
	
	/* Private static methods used for calculating the image properties */
	
	
	
	
	
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
	 * Returns a list with the HSV values of the given byteArray.
	 */
	
	public static List<float[]> byteArrayToHSVList(byte[] byteArray) {
		List<float[]> colorHSVList = new ArrayList<float[]>();
		for (int i=0; i < byteArray.length; i+=3) {
			float[] hsv = new float[3];
			Color.RGBtoHSB(byteArray[i],byteArray[i+1],byteArray[i+2],hsv);
			hsv[0] = Math.round(hsv[0]*100.0);
			hsv[1] = Math.round(hsv[1]*100.0);
			hsv[2] = Math.round(hsv[2]*100.0);
			colorHSVList.add(hsv);
		}
		return colorHSVList;
	}
	
	
	
	
	/**
	 * Returns a matrix with the HSV values of every pixel.
	 */
	
	public static float[][][] createMatrixOfHSVList(List<float[]> colorHSVList) {
		float [][][] HSVList = new float [200][200][3];
		int l = 0;
		for (int y=0; y < HSVList.length; y++) {
			for (int x=0; x < HSVList.length; x++) {
				
				HSVList[x][y][0] = colorHSVList.get(l)[0];
				HSVList[x][y][1] = colorHSVList.get(l)[1];
				HSVList[x][y][2] = colorHSVList.get(l)[2];
				l += 1;
			}
		}
		return HSVList;
	}
	
	
	
	/**
	 * Returns all the different HS colors, which represent a cube. Only a cube color if V-value is lower than 50.
	 */
	
	public static List<double[]> getAllDifferentHSVColors(List<float[]> colorHSVList){
		List<double[]> differentColorsHSVList = new ArrayList<double[]>();
		for (int i = 0; i < colorHSVList.size(); i++) {
			if ( !(colorHSVList.get(i)[0] == 0.0 && colorHSVList.get(i)[1] == 0.0 && colorHSVList.get(i)[2] == 0.0 )) {
				int teller = 0;
				for (int j = 0; j < differentColorsHSVList.size(); j++) {
					if ( (differentColorsHSVList.get(j)[0] == colorHSVList.get(i)[0]) && (differentColorsHSVList.get(j)[1] == colorHSVList.get(i)[1])){
						teller+=1;
					}
				}
				if ( (teller == 0) && (colorHSVList.get(i)[2] == 45 || colorHSVList.get(i)[2] == 20 || colorHSVList.get(i)[2] == 40 || colorHSVList.get(i)[2] == 25 || colorHSVList.get(i)[2] == 35|| colorHSVList.get(i)[2] == 30)) {
					if (! ((colorHSVList.get(i)[0]  == 63 || colorHSVList.get(i)[0] == 64)  && (colorHSVList.get(i)[1] == 99|| colorHSVList.get(i)[1] == 98))) {
					differentColorsHSVList.add(new double[]{colorHSVList.get(i)[0],colorHSVList.get(i)[1],colorHSVList.get(i)[2]});
					}
				}
					
			}
		}	
		return differentColorsHSVList;
	}
						

 
	/**
	 * Returns the center of mass of the given cube
	 */
	static double[] getType0CenterOfMass(float[ ][ ][ ] HSVList, double[] color) {
		
		// variables to store the total x and y coordinates
		long xCoord = 0; long yCoord= 0; int amount = 0;
		// iterate over every x,y coordinate and add the coordinates if their value equals 255
		int x; int y;
		for (x = 0; x < HSVList.length; x++) {
			for (y = 0; y < HSVList.length; y++) {
				if (HSVList[x][y][0] == color[0] && HSVList[x][y][1] == color[1] ) {
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
		
		return coordinates;
	}
	
	/**
	 * Returns the center of mass of the given cube
	 */
	static int getAmountOfPixels(float[ ][ ][ ] HSVList, double[] color) {
		
		// variables to store the total x and y coordinates
		int amount = 0;
		// iterate over every x,y coordinate and add the coordinates if their value equals 255
		int x; int y;
		for (x = 0; x < HSVList.length; x++) {
			for (y = 0; y < HSVList.length; y++) {
				if (HSVList[x][y][0] == color[0] && HSVList[x][y][1] == color[1] ) {
					amount++;
				}
			}
		}
		return amount;
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
		
		private List<double[]> corners = Arrays.asList(new double[]{-2.5,  2.5, -2.5}, new double[]{-2.5,  2.5,  2.5}, 
				new double[]{ 2.5,  2.5,  2.5}, new double[]{ 2.5,  2.5, -2.5}, new double[]{-2.5, -2.5, -2.5}, 
				new double[]{-2.5, -2.5,  2.5}, new double[]{ 2.5, -2.5,  2.5}, new double[]{ 2.5, -2.5, -2.5});
		
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
		

		
		
		public Vector3f getCoordinatesOfCube(Vector3f guessedPosition) {
			// calculate positions of cubes

			// create a list with the HSV values
			List<float[]> colorHSVList = byteArrayToHSVList(getImage());

			// create a matrix
			float [][][] HSVList = createMatrixOfHSVList(colorHSVList);

			// create a list with all the different HSV colors.
			List<double[]> differentColorsHSVList = getAllDifferentHSVColors(colorHSVList);
			int[] mostPixels = new int[differentColorsHSVList.size()];
			for (int i=0; i < differentColorsHSVList.size(); i++) {
				int Amount = getAmountOfPixels(HSVList, differentColorsHSVList.get(i) ); 
				mostPixels[i] = Amount;
				System.out.println(Amount);
			}

			int largest = 0;
			for ( int i = 1; i < mostPixels.length; i++ ){
				if ( mostPixels[i] > mostPixels[largest] ) {
					largest = i;
				}
			}


			double[] color = differentColorsHSVList.get(largest);

			// Get center of mass (of the 2D red cube)
			double[] centerOfMass = getType0CenterOfMass(HSVList, color);


			// Get red area in image
			int redArea = getAmountOfPixels(HSVList, color); 


			// Get red area percentage
			double percentage = redArea / ((float) 200*200);



			// create imaginary cube
			ImaginaryCube imaginaryCube = new ImaginaryCube(getHeading(), getPitch(), getRoll(), guessedPosition, properties.getPosition());

			double[] imCenterOfMass; double deltaX=10; double deltaY=10;
			double imPercentage; double ratio = 10;
			int iterations = 0;


			while (deltaX > 0.005 || deltaY > 0.005 || ratio > 1.025 || ratio < 0.975) {
				iterations++;
				if (iterations > 600) {
					break;
				}
				// get difference between the centers of mass
				imCenterOfMass = imaginaryCube.getProjectedAreaCenterOfMass((float) (120.0 / 180 * Math.PI), (float) (120.0 / 180 * Math.PI));
				deltaX =  (centerOfMass[0] - imCenterOfMass[0]);
				deltaY =  (centerOfMass[1] - imCenterOfMass[1]);

				imaginaryCube.translate(deltaX * 3, deltaY * 3, 0);

				// get the ration between the projected areas
				imPercentage = imaginaryCube.getProjectedAreaPercentage((float) (120.0 / 180 * Math.PI), (float) (120.0 / 180 * Math.PI));
				ratio = imPercentage / percentage;
				imaginaryCube.translate(0, 0, (1 - ratio)*0.1);


			}


			return new Vector3f( (float) imaginaryCube.getPosition()[0], (float)imaginaryCube.getPosition()[1],(float)imaginaryCube.getPosition()[2]);





		}

}

