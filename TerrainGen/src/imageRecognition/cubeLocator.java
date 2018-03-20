package imageRecognition;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;








public class cubeLocator {
	
	public static void main(String[] args) {
		process("image", (float)0.0, (float) 0.0, (float) 0.0);
    }
	
	
	private static void process(String imageName, float heading, float pitch, float roll) {
		
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
		List<float[]> colorHSVList = new ArrayList<float[]>();
		for (int i=0; i < byteArray.length; i+=3) {
			float[] hsv = new float[3];
			Color.RGBtoHSB(byteArray[i],byteArray[i+1],byteArray[i+2],hsv);
			hsv[0] = Math.round(hsv[0]*100.0);
			hsv[1] = Math.round(hsv[1]*100.0);
			hsv[2] = Math.round(hsv[2]*100.0);
			colorHSVList.add(hsv);
		}
		
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
		
		List<double[]> differentColorsHSVList = getAllDifferentHSVColors(colorHSVList);
		for (int k=0; k < differentColorsHSVList.size(); k++) {
			System.out.println(differentColorsHSVList.get(k)[0]+" "+differentColorsHSVList.get(k)[1]);
		}
		
		for (int k=0; k < differentColorsHSVList.size(); k++) {
			double[] Center = getType0CenterOfMass(HSVList, differentColorsHSVList.get(k));
			System.out.println(Center[0]+" "+Center[1]);
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
	 * Returns the center of mass of the given type 0 Mat object
	 * @throws IllegalArgumentException if the type of the mat is not 0
	 * @throws IllegalArgumentException if the total sum of x or y coordinates overflows.
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
}
			

	
		
	

