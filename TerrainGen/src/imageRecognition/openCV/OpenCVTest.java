package imageRecognition.openCV;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

public class OpenCVTest {
	
	public static void main(String[] args) {
		
		// load library
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		
		process("image", (float)0.0, (float) 0.0, (float) 0.0);
		
		
		
		
		
		// voorheen gebruikte toepassingen die misschien nog van pas kunnen komen in de toekomst:
		
		/* ERODE & DILATE IMAGE
		System.out.println("eroding and dilating filtered Mat...");
		Imgproc.erode(outputMat, outputMat, new Mat());
		Imgproc.dilate(outputMat, outputMat, new Mat());
		// save
		//Imgcodecs.imwrite(imageMap + "output1.png", outputMat);
		System.out.println(" -> saved under output1.png");
		*/
		
		
		/* DETECTING CORNERS ON GRAYSCALE MAT
		System.out.println("detecting corners...");
		Mat cornerMat = new Mat();
		Imgproc.cornerHarris(grayMat, cornerMat, 8, 3, 0.04); // detect corners
		Imgproc.threshold(cornerMat, cornerMat, 0.000001, 255, Imgproc.THRESH_BINARY); // apply threshold
		// save
		//Imgcodecs.imwrite(imageMap + "output3.png", cornerMat);
		System.out.println(" -> saved under output3.png");
		*/
		
		/* GETTING CONTOURS OF CORNERS
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		cornerMat.convertTo(cornerMat, CvType.CV_8UC1);
		Imgproc.findContours(cornerMat, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		*/
    }
	
	
	private static void process(String imageName, float heading, float pitch, float roll) {
		long startTime = System.nanoTime();
		// file locations
		String imageMap = "res/";
		String fileLocation = imageMap + imageName + ".png";
		
		
		// attempt to load image, return if failed
		BufferedImage image = null;
		try {
			image = ImageIO.read(new File(fileLocation));
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		
		// bufferedImage --> byteArray
		byte[] byteArray = bufferedImageToByteArray(image);
		for (int i =0; i<byteArray.length;i += 3) {
			if (byteArray[i] != -1) {
				System.out.println(byteArray[i]+" "+byteArray[i+1]+" "+byteArray[i+2]);
			}
		}
		
		
		
		// byteArra --> Mat object
		Mat rgbMat = byteArrayToRGBMat(image.getWidth(), image.getHeight(), byteArray);
		

		List<double[]> colorHSVList = getAllDifferentHSVColors(rgbMat);
		
		List<double[]> colorRGBList = getAllDifferentRGBColors(rgbMat);
		
		System.out.println("rgb");
		
		for (int i = 0; i < colorRGBList.size(); i++){
			System.out.println(colorRGBList.get(i)[0]+" "+colorRGBList.get(i)[1]+" "+colorRGBList.get(i)[2]);
		}
		
		System.out.println("hsv");
		
		for (int i = 0; i < colorHSVList.size(); i++){
			System.out.println(colorHSVList.get(i)[0]+" "+colorHSVList.get(i)[1]+" "+colorHSVList.get(i)[2]);
		}
		
		for (int i = 0; i < colorHSVList.size(); i++){
			
			double[] color = colorHSVList.get(i);
			// Filter RGB Mat for 6 different red Hue's
			Mat[] matArray = redRGBMatFilter(rgbMat, color[0]);

			// Combine the 6 filtered Mats
			Mat filterMat = combineMatArray(matArray);
			
		

			// Get center of mass (of the 2D red cube)
			double[] centerOfMass = getType0CenterOfMass(filterMat);


			// Get red area in image
			int redArea = Core.countNonZero(filterMat);


			// Get red area percentage
			double 
			percentage = redArea / ((float) image.getHeight()*image.getWidth());



			// create imaginary cube
			ImaginaryCube imaginaryCube = new ImaginaryCube(0.0, 0.0, 0.0);

			double[] imCenterOfMass; double deltaX=10; double deltaY=10;
			double imPercentage; double ratio = 10;
			int iterations = 0;

			Imgcodecs.imwrite("res/" + "Test" + ".png", filterMat);
			while (deltaX > 0.005 || deltaY > 0.005 || ratio > 1.025 || ratio < 0.975) {
				iterations++;
				if (iterations > 400) {
					//break;
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
				imaginaryCube.saveAsImage("result " + String.valueOf(iterations * 2 - 1), rgbMat);
				
			}
			
			long duration = (System.nanoTime() - startTime) / 1000000;

			System.out.println("~ ~ " + imageName + " ~ ~");
			System.out.println("Algorithm ended in " + String.valueOf(iterations) + " iterations (" + String.valueOf(duration) + "ms)");
			System.out.println("estimated location: (" + String.valueOf(imaginaryCube.getPosition()[0]) + ", " + String.valueOf(imaginaryCube.getPosition()[1]) + ", " + String.valueOf(imaginaryCube.getPosition()[2]) + ")");
			System.out.println("estimated distance: " + String.valueOf(imaginaryCube.getDistance()));
		}
		
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
	 * Returns a Mat object of given width and height containing the RGB values of the given byteArray.
	 * System.loadLibrary(Core.NATIVE_LIBRARY_NAME) must have been excecuted once before calling this method!
	 * @throws IllegalArgumentException if the given byteArray length is not equal to width*height*3.
	 * 									(because each pixel needs exactly 3 values: red, green, blue)
	 */
	public static Mat byteArrayToRGBMat(int width, int height, byte[] byteArray) 
			throws IllegalArgumentException {
		if (width*height*3 != byteArray.length) {
			throw new IllegalArgumentException("given byteArray has " + String.valueOf(byteArray.length) 
			+ " values but " + String.valueOf(width*height*3) + " (=width*height*3) are required!");
		}
		
		// data reads the given array as BGR
		Mat data = new Mat(height, width, CvType.CV_8UC3);
		
		data.put(0, 0, byteArray);
		
		
		// the input was RGB instead of BGR so transform...
		Imgproc.cvtColor(data, data, Imgproc.COLOR_BGR2RGB);
		
		return data;
	}
	
	/**
	 * Returns a Mat[] array of the 6 different red cube hue's filtered from the given RGB Mat object.
	 * Order: pos x, neg x, pos y, neg y, pos z, neg z
	 * @param colorVList 
	 */
	public static Mat[] redRGBMatFilter(Mat rgbMat, double color) {
		
			// turn the rgb Mat into a hsv Mat
			// (vreemd genoeg heeft BGR 2 HSV hier het gewenste effect en RGB 2 HSV niet)
			Mat hsvMat = new Mat();
			
			Imgproc.cvtColor(rgbMat, hsvMat, Imgproc.COLOR_BGR2HSV);
			// filter the 6 different red hue's
			// Red Hue range: [0,10] & [160,179] Saturation is 255 and Value range depends on the surface
			// Values: pos x: 216, neg x: 76, pos y: 255, neg y: 38, pos z: 178, neg z: 114
			Mat[] matArray = new Mat[3];
			Mat tempMat1 = new Mat(hsvMat.height(), hsvMat.width(), 0, new Scalar(0));
		

		
			for (int i = 0; i < 3; i++) {
				
				// filter the hsv Mat
				Core.inRange(hsvMat, new Scalar(color,   0, 0), new Scalar(color,   255, 255), tempMat1);
			
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
		
		return coordinates;
	}
	
	
	/**
	 * Returns a Mat object on which the given filter is applied.
	 * @throws IllegalArgumentException if the given Mat objects are not of the same dimensions.
	 */
	public static Mat applyFilterMat(Mat scr, Mat filter) {
		if (scr.height() != filter.height() || scr.width() != filter.width())
			throw new IllegalArgumentException("Mat src and Mat filter must be of same dimensions!");
		Mat filteredMat = new Mat(scr.height(), scr.width(), CvType.CV_8UC3, new Scalar(0,0,0));
		Core.bitwise_and(scr, scr, filteredMat, filter);
		return filteredMat;
	}
	
	public static Mat rgbToBlurredGrayScaleMat(Mat scr) {
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
	public static List<int[]> getCornerList(Mat src) {
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
		Point[] contour;
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



	public static Mat addPointsToMat(Mat src, List<int[]> points) {
		Mat returnMat = src.clone();
		for (int[] point : points) {
			Imgproc.circle(returnMat, new org.opencv.core.Point(point[0], point[1]), 4, new Scalar(0,0,0));
		}
		return returnMat;
	}
	
	
	public static List<double[]> getAllDifferentHSVColors(Mat rgbMat){
		List<double[]> colorHSVList = new ArrayList<double[]>();
		Mat hsvMat = new Mat();
		Imgproc.cvtColor(rgbMat, hsvMat, Imgproc.COLOR_RGB2HSV);
		for (int x = 0; x < 200; x+=2) {
			for (int y = 0; y < 200; y+=2) {
				if ( !(rgbMat.get(y, x)[0] == 255.0 && rgbMat.get(y, x)[1] == 255.0 && rgbMat.get(y, x)[2] == 255.0 )) {
					int teller = 0;
					for (int j = 0; j < colorHSVList.size(); j++) {
						if ( (colorHSVList.get(j)[0]%178 <= hsvMat.get(y, x)[0]%178 && colorHSVList.get(j)[0]%178 >= hsvMat.get(y, x)[0]%178 )){
							teller+=1;
							}
					}
					if ( teller == 0 ) {
						colorHSVList.add(new double[]{hsvMat.get(y, x)[0],hsvMat.get(y, x)[1],hsvMat.get(y, x)[2]});
					}

					
				}
				
			}
		}
		

    return colorHSVList;
   
	}

	public static List<double[]> getHSValuesofCubes(List<double[]> colorHSVList){
		List<double[]> colorHSList = new ArrayList<double[]>();
		
		colorHSList.add(new double[]{colorHSVList.get(0)[0],colorHSVList.get(0)[1]});
		
		for (int i = 1; i < colorHSVList.size(); i++) {
			int teller = 0;
			int size = colorHSList.size();
			for (int j = 0; j < size; j++) {
				if ((colorHSVList.get(i)[0] >= colorHSList.get(j)[0]-3 && colorHSVList.get(i)[0] <= colorHSList.get(j)[0]+3 ) && (colorHSVList.get(i)[1] >= colorHSList.get(j)[1]-3 && colorHSVList.get(i)[1] <= colorHSList.get(j)[1]+3 )) {
					teller+=1;
					}
			}
			if ( teller == 0 ) {
				
				colorHSList.add(new double[]{colorHSVList.get(i)[0],colorHSVList.get(i)[1]} );	
			}	
			
		}

		return colorHSList;	
		}

			
	public static List<double[]> getValuesOfV(List<double[]> colorHSVList){
		List<double[]> colorVList = new ArrayList<double[]>();
		
		colorVList.add(new double[]{colorHSVList.get(0)[2]});
		
		for (int i = 1; i < colorHSVList.size(); i++) {
			int teller = 0;
			int size = colorVList.size();
			for (int j = 0; j < size; j++) {
				if ((colorHSVList.get(i)[2] == colorVList.get(j)[0])) {
					teller+=1;
					}
			}
			if ( teller == 0 ) {
				
				colorVList.add(new double[]{colorHSVList.get(i)[2]} );	
			}	
			
		}

		return colorVList;	
		}
	
	public static List<double[]> getAllDifferentRGBColors(Mat rgbMat){
		List<double[]> colorRGBList = new ArrayList<double[]>();
		Imgproc.cvtColor(rgbMat, rgbMat, Imgproc.COLOR_RGB2BGR);
		for (int x = 0; x < 200; x+=2) {
			for (int y = 0; y < 200; y+=2) {
				if ( !(rgbMat.get(y, x)[0] == 255.0 && rgbMat.get(y, x)[1] == 255.0 && rgbMat.get(y, x)[2] == 255.0 )) {
					int teller = 0;
					for (int j = 0; j < colorRGBList.size(); j++) {
						if ( (colorRGBList.get(j)[0] <= rgbMat.get(y, x)[0]+3 && colorRGBList.get(j)[0] >= rgbMat.get(y, x)[0]-3 ) && (colorRGBList.get(j)[1] <= rgbMat.get(y, x)[1]+3 &&colorRGBList.get(j)[1] >= rgbMat.get(y, x)[1]-3 ) && (colorRGBList.get(j)[2] <= rgbMat.get(y, x)[2]+3 &&colorRGBList.get(j)[2] >= rgbMat.get(y, x)[2]-3 )) {
							teller+=1;
							}
					}
					if ( teller == 0 ) {
						colorRGBList.add(new double[]{rgbMat.get(y, x)[0], rgbMat.get(y, x)[1],rgbMat.get(y, x)[2]});
					}

					
				}
				
			}
		}
		

    return colorRGBList;
   
	}
	
	}
		
	

