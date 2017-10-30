package testObjects;

import toolbox.generateColors;

public class Cube {
	
	public float[] colors;
	
	public Cube(float r, float g, float b) {
		colors = generateColors.generateColors(r, g, b);
	}
	
	public float[] positions = new float[] {
			//Linker Zijvlak
			-0.5f,-0.5f,-0.5f, // triangle 1 : begin
		    -0.5f,-0.5f, 0.5f,
		    -0.5f, 0.5f, 0.5f, // triangle 1 : end
		    -0.5f,-0.5f,-0.5f,
		    -0.5f, 0.5f, 0.5f,
		    -0.5f, 0.5f,-0.5f,
		    //AchterVlak
		    0.5f, 0.5f,-0.5f, // triangle 2 : begin
		    -0.5f,-0.5f,-0.5f,
		    -0.5f, 0.5f,-0.5f, // triangle 2 : end
		    0.5f, 0.5f,-0.5f,
		    0.5f,-0.5f,-0.5f,
		    -0.5f,-0.5f,-0.5f,
		    //Rechter Zijvlak
		    0.5f, 0.5f, 0.5f,
		    0.5f,-0.5f,-0.5f,
		    0.5f, 0.5f,-0.5f,
		    0.5f,-0.5f,-0.5f,
		    0.5f, 0.5f, 0.5f,
		    0.5f,-0.5f, 0.5f,
		    //Voorvlak
		    -0.5f, 0.5f, 0.5f,
		    -0.5f,-0.5f, 0.5f,
		    0.5f,-0.5f, 0.5f,
		    0.5f, 0.5f, 0.5f,
		    -0.5f, 0.5f, 0.5f,
		    0.5f,-0.5f, 0.5f,
		    //Onderkant
		    0.5f,-0.5f, 0.5f,
		    -0.5f,-0.5f,-0.5f,
		    0.5f,-0.5f,-0.5f,
		    0.5f,-0.5f, 0.5f,
		    -0.5f,-0.5f, 0.5f,
		    -0.5f,-0.5f,-0.5f,
		    //Bovenkant
		    0.5f, 0.5f, 0.5f,
		    0.5f, 0.5f,-0.5f,
		    -0.5f, 0.5f,-0.5f,
		    0.5f, 0.5f, 0.5f,
		    -0.5f, 0.5f,-0.5f,
		    -0.5f, 0.5f, 0.5f,
		};
	
}
