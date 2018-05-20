package autopilot;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class Pep {
	
	public static double[] getCoordinatesOfCube(byte[] image) {
		// calculate positions of cubes
		
		// create a list with the HSV values
		List<float[]> colorHSVList = byteArrayToHSVList(image);
		
		// create a matrix
		float [][][] HSVList = createMatrixOfHSVList(colorHSVList);
		
		// create a list with all the different HSV colors.
		List<double[]> differentColorsHSVList = getAllDifferentHSVColors(colorHSVList);
		int[] mostPixels = new int[differentColorsHSVList.size()];
		for (int i=0; i < differentColorsHSVList.size(); i++) {
			int Amount = getAmountOfPixels(HSVList, differentColorsHSVList.get(i) ); 
			mostPixels[i] = Amount;
		}
		
		int largest = 0;
		for ( int i = 1; i < mostPixels.length; i++ ){
		      if ( mostPixels[i] > mostPixels[largest] ) {
		    	  largest = i;
		      }
		 }
		
		// Get center of mass (of the 2D red cube)
		 
	    double[] centerOfMass = getType0CenterOfMass(HSVList, differentColorsHSVList.get(largest));
	 
			
		return new double[]{centerOfMass[0],centerOfMass[1]};
	}
	
	/**
	 * Returns a list with the HSV values of the given byteArray.
	 */
	
	private static List<float[]> byteArrayToHSVList(byte[] byteArray) {
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
	
	private static float[][][] createMatrixOfHSVList(List<float[]> colorHSVList) {
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
	private static List<double[]> getAllDifferentHSVColors(List<float[]> colorHSVList){
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
					if (! ((colorHSVList.get(i)[0]  == 63 || colorHSVList.get(i)[0] == 64)  && (colorHSVList.get(i)[1] == 99|| colorHSVList.get(i)[1] == 98) || (colorHSVList.get(i)[1] < 30))) {
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
	private static int getAmountOfPixels(float[ ][ ][ ] HSVList, double[] color) {
		
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
	
	/**
	 * Returns the center of mass of the given cube
	 */
	private static double[] getType0CenterOfMass(float[ ][ ][ ] HSVList, double[] color) {
		
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
		System.out.println(coordinates[0]);
		coordinates[0] = (coordinates[0] - 100.0) / 100.0;
		coordinates[1] = (int) Math.round(yCoord / (float) amount);
		System.out.println(coordinates[1]);
		coordinates[1] = (100.0 - coordinates[1]) / 100.0;
		
		return coordinates;
	}
}
