package testObjects;

import toolbox.generateColors;

public class Cuboid {

public float[] colors;
	
	public Cuboid(float r, float g, float b) {
		colors = generateColors.generateColors(r, g, b);
	}
	
	public float[] positions = new float[] {
			//Linker Zijvlak
			-1.0f,-1.0f,-1.0f, // triangle 1 : begin
		    -1.0f,-1.0f, 2.0f,
		    -1.0f, 1.0f, 2.0f, // triangle 1 : end
		    -1.0f,-1.0f,-1.0f,
		    -1.0f, 1.0f, 2.0f,
		    -1.0f, 1.0f,-1.0f,
		    //AchterVlak
		    1.0f, 1.0f,-1.0f, // triangle 2 : begin
		    -1.0f,-1.0f,-1.0f,
		    -1.0f, 1.0f,-1.0f, // triangle 2 : end
		    1.0f, 1.0f,-1.0f,
		    1.0f,-1.0f,-1.0f,
		    -1.0f,-1.0f,-1.0f,
		    //Rechter Zijvlak
		    1.0f, 1.0f, 2.0f,
		    1.0f,-1.0f,-1.0f,
		    1.0f, 1.0f,-1.0f,
		    1.0f,-1.0f,-1.0f,
		    1.0f, 1.0f, 2.0f,
		    1.0f,-1.0f, 2.0f,
		    //Voorvlak
		    -1.0f, 1.0f, 2.0f,
		    -1.0f,-1.0f, 2.0f,
		    1.0f,-1.0f, 2.0f,
		    1.0f, 1.0f, 2.0f,
		    -1.0f, 1.0f, 2.0f,
		    1.0f,-1.0f, 2.0f,
		    //Onderkant
		    1.0f,-1.0f, 2.0f,
		    -1.0f,-1.0f,-1.0f,
		    1.0f,-1.0f,-1.0f,
		    1.0f,-1.0f, 2.0f,
		    -1.0f,-1.0f, 2.0f,
		    -1.0f,-1.0f,-1.0f,
		    //Bovenkant1
		    1.0f, 1.0f, 2.0f,
		    1.0f, 1.0f,-1.0f,
		    -1.0f, 1.0f,-1.0f,
		    1.0f, 1.0f, 2.0f,
		    -1.0f, 1.0f,-1.0f,
		    -1.0f, 1.0f, 2.0f,
		};
	
}
