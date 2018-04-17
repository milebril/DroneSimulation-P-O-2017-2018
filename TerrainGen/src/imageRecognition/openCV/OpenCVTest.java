package imageRecognition.openCV;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;



import javax.imageio.ImageIO;



public class OpenCVTest {
	
	public static void main(String[] args) {
		
		
		
		
		process("imageNew", (float)0.0, (float) 0.0, (float) 0.0);
		
		
		
		
		
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
		
		
		// calculate positions of cubes
		
		// bufferedImage to byteArray
		byte[] byteArray = bufferedImageToByteArray(image);
		
		// create a list with the HSV values
		List<float[]> colorHSVList = byteArrayToHSVList(byteArray);
		
		// create a matrix
		float [][][] HSVList = createMatrixOfHSVList(colorHSVList);
		
		// create a list with all the different HSV colors.
		List<double[]> differentColorsHSVList = getAllDifferentHSVColors(colorHSVList);
		for (int k=0; k < differentColorsHSVList.size(); k++) {
			System.out.println(differentColorsHSVList.get(k)[0]+" "+differentColorsHSVList.get(k)[1]);
		}
		
		
		for (int i = 0; i < differentColorsHSVList.size(); i++){
			
			double[] color = differentColorsHSVList.get(i);
		
			
		

			// Get center of mass (of the 2D red cube)
			double[] centerOfMass = getType0CenterOfMass(HSVList, color);


			// Get red area in image
			int redArea = getAmountOfPixels(HSVList, color); 


			// Get red area percentage
			double percentage = redArea / ((float) image.getHeight()*image.getWidth());



			// create imaginary cube
			ImaginaryCube imaginaryCube = new ImaginaryCube(0.0, 0.0, 0.0);

			double[] imCenterOfMass; double deltaX=10; double deltaY=10;
			double imPercentage; double ratio = 10;
			int iterations = 0;

			
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
				
				
			}
			
			long duration = (System.nanoTime() - startTime) / 1000000;

			System.out.println("~ ~ " + imageName + " ~ ~");
			System.out.println("Algorithm ended in " + String.valueOf(iterations) + " iterations (" + String.valueOf(duration) + "ms)");
			System.out.println("estimated location: (" + String.valueOf(imaginaryCube.getPosition()[0]) + ", " + String.valueOf(imaginaryCube.getPosition()[1]+2.32) + ", " + String.valueOf(imaginaryCube.getPosition()[2]-5.0) + ")");

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
				if ( (teller == 0) && colorHSVList.get(i)[2] < 50 ) {
					differentColorsHSVList.add(new double[]{colorHSVList.get(i)[0],colorHSVList.get(i)[1]});
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
	
	}
		
	

