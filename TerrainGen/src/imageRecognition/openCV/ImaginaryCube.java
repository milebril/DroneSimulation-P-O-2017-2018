package imageRecognition.openCV;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



import java.awt.Point;

public class ImaginaryCube {
	
	public ImaginaryCube(double heading, double pitch, double roll) {
		this.heading = heading;
		this.pitch = pitch;
		this.roll = roll;
		
		// rotate the cube to be in alignment with the world frame
		alignCube();
		
		// place the cube in front of the camera
		translate(0, 0, -20);
	}
	
	
	// Focal length of the simulated pinhole camera
	
	private static double f = 100 / Math.tan(Math.PI / 3);
	
	
	// Properties
	
	private final double heading;
	
	public double getHeading() {
		return heading;
	}
	
	private final double pitch;
	
	public double getPitch() {
		return pitch;
	}

	private final double roll;
	
	public double getRoll() {
		return roll;
	}
	
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
			projectedCorners.add(new double[]{-ImaginaryCube.f * point[0] / point[2], -ImaginaryCube.f * point[1] / point[2]});
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
		
		return new double[]{cx / 100, cy / 100};
	}
	
	
	
	
}

