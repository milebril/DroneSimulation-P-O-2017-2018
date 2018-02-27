package engineTester;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import models.RawCubeModel;
import models.RawModel;
import models.TexturedModel;
import physicsEngine.approximationMethods.EulerPrediction;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import renderEngine.CubeRenderer;
import renderEngine.DisplayManager;
import renderEngine.Loader;
import renderEngine.MasterRenderer;
import renderEngine.OBJLoader;
import shaders.cubes.CubeShader;
import terrains.LandingStrip;
import terrains.Terrain;
import testObjects.Cube;
import testObjects.Cuboid;
import textures.ModelTexture;
import entities.Camera;
import entities.Drone;
import entities.Entity;
import entities.Light;
import interfaces.AutopilotFactory;

public class MainGameLoop {

	private static boolean lLock;
	private static boolean sLock;
	private static boolean oLock;
	
	//Entities lists
	private static List<Entity> entities = new ArrayList<>();
	private static List<Terrain> terrains = new ArrayList<>();
	private static List<Entity> cubes = new ArrayList<>();
	
	//Loader
	private static Loader loader;

	public static void main(String[] args) {
		DisplayManager.createDisplay();
		
		//Initializing Objects
		loader = new Loader();
		
		RawModel model = OBJLoader.loadObjModel("tree", loader);
		
		TexturedModel staticModel = new TexturedModel(model,new ModelTexture(loader.loadTexture("tree")));
		
		Random random = new Random();
		for(int i=0;i<500;i++){
			entities.add(new Entity(staticModel,
					new Matrix4f().translate(new Vector3f(random.nextFloat()*800 - 400,0, random.nextFloat() * -600)), 1));
		}
		
		Light light = new Light(new Vector3f(20000,20000,2000),new Vector3f(1,1,1));
		
		terrains.add(new Terrain(0,-1,loader,new ModelTexture(loader.loadTexture("greenScreen"))));
		terrains.add(new Terrain(-1,-1,loader,new ModelTexture(loader.loadTexture("greenScreen"))));
		terrains.add(new Terrain(0,-2,loader,new ModelTexture(loader.loadTexture("greenScreen"))));
		terrains.add(new Terrain(-1,-2,loader,new ModelTexture(loader.loadTexture("greenScreen"))));
		terrains.add(new LandingStrip(-0.5f,-1,loader,new ModelTexture(loader.loadTexture("landing"))));
		
		Camera camera = new Camera(200, 200);	
		MasterRenderer renderer = new MasterRenderer();
		
		//Cube Render
		CubeShader cubeShader = new CubeShader();
		CubeRenderer cubeRenderer = new CubeRenderer(cubeShader, 120, 120);
		
		Cube c = new Cube(1, 0, 0);
		RawCubeModel cube = loader.loadToVAO(c.positions, c.colors);
		Entity e = new Entity(cube, new Matrix4f().translate(new Vector3f(0, 4, -10)), 1);
		
		while(!Display.isCloseRequested()){
			//CAMERA VIEW
			renderer.prepare();
			GL11.glViewport(0, 0, 200, 200);
			GL11.glScissor(0,0,200,200);
			GL11.glEnable(GL11.GL_SCISSOR_TEST);
			//camera.setPosition(camera.getPosition().translate(0, -0.01f, -1));
			cubeShader.start();
			cubeShader.loadViewMatrix(camera);
			
			for (Terrain t : terrains) {
				renderer.processTerrain(t);
			}
			
			for(Entity entity:entities){
				//TODO
//				renderer.processEntity(entity);
			}
			renderer.render(light, camera);
			
			cubeShader.start();
			cubeShader.loadViewMatrix(camera);
			for (Entity entity : cubes) {
				cubeRenderer.render(entity, cubeShader);
			}
			cubeShader.stop();
			
			//GUI TODO
			GL11.glViewport(200, 0, Display.getWidth() - 200, 200);
			GL11.glScissor(200, 0, Display.getWidth() - 200, 200);
			GL11.glEnable(GL11.GL_SCISSOR_TEST);
			renderer.prepareBlack();
			
			//BIG SCREEN
			renderer.prepare();
			GL11.glViewport(0, 200, Display.getWidth(), Display.getHeight() - 200);
			GL11.glScissor(0, 200, Display.getWidth(), Display.getHeight() - 200);
			GL11.glEnable(GL11.GL_SCISSOR_TEST);

			for (Terrain t : terrains) {
				renderer.processTerrain(t);
			}
			
			for(Entity entity:entities){
				//TODO
//				renderer.processEntity(entity);
			}
			renderer.render(light, camera);
			
			cubeShader.start();
			cubeShader.loadViewMatrix(camera);
			for (Entity entity : cubes) {
				cubeRenderer.render(entity, cubeShader);
			}
			cubeShader.stop();
			
			keyInputs();
			DisplayManager.updateDisplay();
		}

		renderer.cleanUp();
		loader.cleanUp();
		cubeShader.cleanUp();
		DisplayManager.closeDisplay();
	}
	
	public static void keyInputs() {
		if (Keyboard.isKeyDown(Keyboard.KEY_Y)) {
//			Vector3f.add(drone.getPosition(), new Vector3f(0, 150, -50), freeRoamCamera.getPosition());
//			freeRoamCamera.setRotation((float) -(Math.PI / 2), 0, 0);
		} else if (Keyboard.isKeyDown(Keyboard.KEY_X)) {
//			Vector3f.add(drone.getPosition(), new Vector3f(100, 0, 0), freeRoamCamera.getPosition());
//			freeRoamCamera.setRotation(0, (float) -(Math.PI / 2), 0);
		} else if(Keyboard.isKeyDown(Keyboard.KEY_L)) {
			/* Lock/Unlock on Third Person Camera */
			if (!lLock) {
//				freeRoamCameraLocked = !freeRoamCameraLocked;
			}
			lLock = true;
		} else if (Keyboard.isKeyDown(Keyboard.KEY_O)) {
			
			oLock = true;
		} else if(Keyboard.isKeyDown(Keyboard.KEY_S)) {
			if (!sLock) {
				DisplayManager.start();
			}
			
			sLock = true;
		} else if(Keyboard.isKeyDown(Keyboard.KEY_R)) {
			reset();
		}else {
//			if (freeRoamCameraLocked) {
//				Vector3f.add(drone.getPosition(), new Vector3f(0, 0, 30), freeRoamCamera.getPosition());
//				//freeRoamCamera.setRotation((float) -(Math.PI/6), 0, 0);
//			} else {
//				freeRoamCamera.roam();
//			}
			
			lLock = false;
			oLock = false;
			sLock = false;
		}
	}
	
	private static void reset() {
		//Reset Cubes & Display
		generateRandomCubes();
		DisplayManager.reset();
		
		//Reset Cameras
//		freeRoamCameraLocked = true;
//		viewState = ViewStates.CHASE;
		
		//Reset Drone
//		Cuboid droneCube = new Cuboid(0, 0, 0);
//		drone = new Drone(loader.loadToVAO(droneCube.positions, droneCube.colors, null),
//				new Matrix4f().translate(new Vector3f(0, 0, 0)), 1, autopilotConfig, new EulerPrediction(STEP_TIME));
		
		//Reset AP
//		autopilot = AutopilotFactory.createAutopilot();
//		autopilot.simulationStarted(autopilotConfig, drone.getAutoPilotInputs());
	}
	
	public static void generateRandomCubes() {
		Random r = new Random();
		cubes = new ArrayList<>();
		
		float prevX = 0.0f;
		float prevY = 0.0f;
		
		 for (int i = 1; i <= 5; i++) {
		      Cube c = null;
		      
		    switch (i) {
				case 1:
					c = new Cube(1, 0, 0);
					break;
				case 2:
					c = new Cube(0, 1, 0);
					break;
				case 3:
					c = new Cube(1, 0, 0);
					break;
				case 4:
					c = new Cube(1, 1, 0);
					break;
				case 5:
					c = new Cube(0, 1, 1);
					break;
				default:
					break;
			}
		      
		      RawCubeModel model = loader.loadToVAO(c.positions, c.colors);
		      float x = r.nextFloat()*20-10;
		      x = 0;
		      float y = ((float) r.nextInt(1000) / 500 - 1)*10;
		      float z = i*-40;
		      Vector3f position = new Vector3f(x,y,z);
		      	
		      while(Math.sqrt(Math.pow(x - prevX, 2) + Math.pow(y - prevY, 2)) > 10) {
		    	  x = r.nextFloat()*20-10;
			      //x = 0;
			      y = ((float) r.nextInt(1000) / 500 - 1)*10;
		      }
		      
		      prevX = x;
		      prevY = y;
		      
		      //Debug Print
		      System.out.println(position);
		      
		      cubes.add(new Entity(model, new Matrix4f().translate(position), 1));
		 }
		 
		 System.out.println("#####");
	}
}
