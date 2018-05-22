package engineTester;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.PixelFormat;

import static org.lwjgl.opengl.GL11.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.vecmath.Vector2f;

import org.lwjgl.LWJGLException;

import renderEngine.DisplayManager;

public class MiniMapTest {
	private static final int WORLD_SIZE = 8000;
	private static List<Vector2f> positions;

	public static void main(String[] args) {
		positions = new ArrayList<>();
		
		Random rand = new Random();
		for (int i = 0; i < 20; i++) {
			positions.add(new Vector2f(rand.nextInt(WORLD_SIZE), rand.nextInt(WORLD_SIZE)));
		}

		try {
			Display.setDisplayMode(new DisplayMode(1280, 700));
			Display.setTitle("MiniMap");
			Display.create();
		} catch (LWJGLException e) {
			e.printStackTrace();
			Display.destroy();
			System.exit(1);
		}

		glMatrixMode(GL_PROJECTION);
		glLoadIdentity(); // Resets any previous projection matrices
		glOrtho(0, 1280, 700, 0, 1, -1);
		glMatrixMode(GL_MODELVIEW);
		
		Random r = new Random();
		Vector2f tempPos;
		
		float x, y;
		
		while (!Display.isCloseRequested()) {
			// Randomize position movement
				for (Vector2f position : positions) {
				tempPos = new Vector2f(position.x + (r.nextInt(10) - 5) , position.y + (r.nextInt(10) - 5));
				while (tempPos.x < 0 || tempPos.y < 0 || tempPos.x > WORLD_SIZE || tempPos.y > WORLD_SIZE) {
					tempPos = new Vector2f(position.x + (r.nextInt(10) - 5) , position.y + (r.nextInt(10) - 5));
				}
				position.set(tempPos.x, tempPos.y);
			}
			
			glClear(GL_COLOR_BUFFER_BIT);
			glClearColor(1, 1, 1, 1);
			
			for (Vector2f position : positions) {
				x = position.x * 1280 / WORLD_SIZE;
				y = position.y * 700 / WORLD_SIZE;
				
				glColor3f(1f, 0f, 0f);
				glBegin(GL_QUADS);
				glVertex2f(x, y);
				glVertex2f(x, y + 10);
				glVertex2f(x + 10, y + 10);
				glVertex2f(x + 10, y);
				glEnd();
			}

			Display.update();
			// Refresh the display and poll input.
			Display.sync(60);
		}

		Display.destroy();
		System.exit(0);
	}

}
