package testObjects;

import toolbox.generateColors;

public class Cube {
	
	public float[] colors;
	
	public Cube(float r, float g, float b) {
		colors = generateColors.generateColors(r, g, b);
	}
	
	public float[] positions = new float[] {
			//Linker Zijvlak
			-2.5f,-2.5f,-2.5f, // triangle 1 : begin
		    -2.5f,-2.5f, 2.5f,
		    -2.5f, 2.5f, 2.5f, // triangle 1 : end
		    -2.5f,-2.5f,-2.5f,
		    -2.5f, 2.5f, 2.5f,
		    -2.5f, 2.5f,-2.5f,
		    //AchterVlak
		    2.5f, 2.5f,-2.5f, // triangle 2 : begin
		    -2.5f,-2.5f,-2.5f,
		    -2.5f, 2.5f,-2.5f, // triangle 2 : end
		    2.5f, 2.5f,-2.5f,
		    2.5f,-2.5f,-2.5f,
		    -2.5f,-2.5f,-2.5f,
		    //Rechter Zijvlak
		    2.5f, 2.5f, 2.5f,
		    2.5f,-2.5f,-2.5f,
		    2.5f, 2.5f,-2.5f,
		    2.5f,-2.5f,-2.5f,
		    2.5f, 2.5f, 2.5f,
		    2.5f,-2.5f, 2.5f,
		    //Voorvlak
		    -2.5f, 2.5f, 2.5f,
		    -2.5f,-2.5f, 2.5f,
		    2.5f,-2.5f, 2.5f,
		    2.5f, 2.5f, 2.5f,
		    -2.5f, 2.5f, 2.5f,
		    2.5f,-2.5f, 2.5f,
		    //Onderkant
		    2.5f,-2.5f, 2.5f,
		    -2.5f,-2.5f,-2.5f,
		    2.5f,-2.5f,-2.5f,
		    2.5f,-2.5f, 2.5f,
		    -2.5f,-2.5f, 2.5f,
		    -2.5f,-2.5f,-2.5f,
		    //Bovenkant
		    2.5f, 2.5f, 2.5f,
		    2.5f, 2.5f,-2.5f,
		    -2.5f, 2.5f,-2.5f,
		    2.5f, 2.5f, 2.5f,
		    -2.5f, 2.5f,-2.5f,
		    -2.5f, 2.5f, 2.5f,
		};
	
}
