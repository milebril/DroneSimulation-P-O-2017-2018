package openCV;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.awt.Point;

public class ImaginaryCube {
	
	public ImaginaryCube(float heading, float pitch, float roll) {
		this.heading = heading;
		this.pitch = pitch;
		this.roll = roll;
		
		// rotate the cube to be in alignment with the world frame
		alignCube();
		
		// place the cube in front of the camera
		translate(0, 0, -3);
	}
	
	
	// Focal length of the simulated pinhole camera
	
	private static double f = 100;
	
	
	// Properties
	
	private final float heading;
	
	public float getHeading() {
		return heading;
	}
	
	private final float pitch;
	
	public float getPitch() {
		return pitch;
	}

	private final float roll;
	
	public float getRoll() {
		return roll;
	}
	
	
	private List<double[]> corners = Arrays.asList(new double[]{-0.5,  0.5, -0.5}, new double[]{-0.5,  0.5,  0.5}, 
			new double[]{ 0.5,  0.5,  0.5}, new double[]{ 0.5,  0.5, -0.5}, new double[]{-0.5, -0.5, -0.5}, 
			new double[]{-0.5, -0.5,  0.5}, new double[]{ 0.5, -0.5,  0.5}, new double[]{ 0.5, -0.5, -0.5});
	
	private List<double[]> getCorners() {
		return this.corners;
	}
	
	
	// Setters
	
	/**
	 * Rotate the cube
	 */
	private void rotate(float heading, float pitch, float roll) {
		double x; double y; double z;
		for (double[] point : getCorners()) {
			x = point[0]; y = point[1]; z = point[2];
			point[0] = x * (Math.cos(heading)*Math.cos(roll) - Math.sin(heading)*Math.sin(pitch)*Math.sin(roll))  + y * (-Math.cos(pitch)*Math.sin(roll)) + z * (Math.cos(roll)*Math.sin(heading) + Math.cos(heading)*Math.sin(pitch)*Math.sin(roll));
			point[1] = x * (Math.cos(heading)*Math.sin(roll) + Math.cos(roll)*Math.sin(heading)*Math.sin(pitch)) + y * (Math.cos(pitch)*Math.cos(roll)) + z * (Math.sin(heading)*Math.sin(roll) - Math.cos(heading)*Math.cos(roll)*Math.sin(pitch));
			point[2] = x * (-Math.cos(pitch)*Math.sin(heading)) + y * (Math.sin(pitch))+ z * (Math.cos(heading) * Math.cos(pitch));
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
	public void translate(float deltaX, float deltaY, float deltaZ) {
		for (double[] point : getCorners()) {
			point[0] += deltaX;
			point[1] += deltaY;
			point[2] += deltaZ;
		}
	}
	
	
	// Calculations
	
	/**
	 * Returns a list of the corners, projected using a pinhole camera
	 */
	private List<double[]> getProjectedCorners() {
		List<double[]> projectedCorners = new ArrayList<>();
		
		// project all points
		for (double[] point : getCorners()) {
			projectedCorners.add(new double[]{ImaginaryCube.f * point[0] / point[2], ImaginaryCube.f * point[1] / point[2]});
		}
		
		return projectedCorners;
	}
	
	/**
	 * Returns the convext hull of the given 2D coordinate list. The first and last coordinate are the same.
	 */
	private List<double[]> getConvextHull() {
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
		double width = 2 * ImaginaryCube.f * Math.tan(horAngleOfView);
		double height = 2 * ImaginaryCube.f * Math.tan(verAngleOfView);
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
		
		return new double[]{cx, cy};
	}
	
	
	
	public void saveAsImage(String name) {
		// line(Mat img, Point pt1, Point pt2, Scalar color, int thickness) 
		Mat image = new Mat(200, 200, CvType.CV_16SC3, new Scalar(255, 255, 255));
		List<double[]> convexHull = getConvextHull();
		
		org.opencv.core.Point p0;
		org.opencv.core.Point p1;
		for (int i = 0; i < convexHull.size() - 1; i++) {
			p0 = new org.opencv.core.Point((int) Math.round(convexHull.get(i)[0]+100), (int) Math.round(convexHull.get(i)[1]+100));
			p1 = new org.opencv.core.Point((int) Math.round(convexHull.get(i+1)[0]+100), (int) Math.round(convexHull.get(i+1)[1]+100));
			Imgproc.line(image, p0, p1, new Scalar(0, 0, 0));
		}
		
		Imgcodecs.imwrite("res/" + name + ".png", image);
		
		
	}
}
