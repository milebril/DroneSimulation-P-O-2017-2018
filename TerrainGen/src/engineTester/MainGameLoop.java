package engineTester;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JFileChooser;

import models.RawCubeModel;
import models.RawModel;
import models.TexturedModel;
import physicsEngine.DroneCrashException;
import physicsEngine.MaxAoAException;
import physicsEngine.PhysicsEngine;
import physicsEngine.approximationMethods.EulerPrediction;
import prevAutopilot.SimpleAutopilot;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.opencv.core.Core;

import autopilot.interfaces.Autopilot;
import autopilot.interfaces.AutopilotConfig;
import autopilot.interfaces.AutopilotFactory;
import autopilot.interfaces.AutopilotInputs;
import autopilot.interfaces.AutopilotOutputs;
import autopilot.interfaces.config.AutopilotConfigReader;
import renderEngine.CubeRenderer;
import renderEngine.DisplayManager;
import renderEngine.EntityRenderer;
import renderEngine.Loader;
import renderEngine.MasterRenderer;
import renderEngine.OBJLoader;
import shaders.StaticShader;
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
import fontMeshCreator.FontType;
import fontMeshCreator.GUIText;
import fontRendering.TextMaster;
import guis.Button;
import guis.GuiRenderer;
import guis.GuiTexture;

public class MainGameLoop {
	
	private static final float STEP_TIME = 0.001f;
	
	//Key press lock
	private static boolean lLock;
	private static boolean sLock;
	private static boolean oLock;

	public static AutopilotConfig autopilotConfig;
	
	private static Drone drone;
	
	//Entities lists
	private static List<Entity> entities;
	private static List<Terrain> terrains;
	private static List<Entity> cubes;
	
	//Loader
	private static Loader loader;
	
	//Autopilot
	private static Autopilot autopilot;

	//Camera Stuff
	private static Camera chaseCam;
	private static boolean chaseCameraLocked = true;
	
	//Shaders
	private static CubeShader cubeShader;
	
	//Renderers
	private static MasterRenderer renderer;
	private static CubeRenderer cubeRenderer;
	
	//Lights
	private static Light light;
	
	//Buttons
	private static Button openFile;
	private static Button randomCubes;

	public static void main(String[] args) {
		//Needed to load openCV
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		//***INITIALIZE CONFIG***
		try {
			File config = new File("res/AutopilotConfig.cfg");
			DataInputStream inputStream = new DataInputStream(new FileInputStream(config));
			autopilotConfig = AutopilotConfigReader.read(inputStream);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//***INITIALIZE LOADERS & SCREEN***
		DisplayManager.createDisplay();
		loader = new Loader();
		TextMaster.init(loader);
		entities = new ArrayList<>();
		terrains = new ArrayList<>();
		cubes = new ArrayList<>();
		
		//***INITIALIZE DRONEVIEW***
//		RawModel droneModel = OBJLoader.loadObjModel("tree", loader);
//		TexturedModel staticDroneModel = new TexturedModel(droneModel,new ModelTexture(loader.loadTexture("tree")));
		RawModel droneModel = OBJLoader.loadObjModel("untitled5", loader);
	    TexturedModel staticDroneModel = new TexturedModel(droneModel,new ModelTexture(loader.loadTexture("droneTex")));
	    // drone op de grond zetten -> y-pos = PhysicsEngine.groundLevel - autopilotConfig.getWheelY() + autopilotConfig.getTyreRadius()
	    float droneOnGroundLevel = (float) (PhysicsEngine.groundLevel - autopilotConfig.getWheelY() + autopilotConfig.getTyreRadius());
		drone = new Drone(staticDroneModel, new Matrix4f().translate(new Vector3f(0, 40, 0)), 1f,
				autopilotConfig, new EulerPrediction(STEP_TIME));
		entities.add(drone);
		
		//***INITIALIZE CHASE-CAM***
		chaseCam = new Camera();
		chaseCam.setPosition(drone.getPosition().translate(30, 0, 0));
		
		//***INITIALIZE GUI-TEXT***
		FontType font = new FontType(loader.loadTexture("verdana"), new File("res/verdana.fnt"));
		
		//Speed text
		String speed = String.valueOf(Math.round(drone.getAbsVelocity()));
		GUIText textSpeed = new GUIText("Speed = " + speed + "m/s", 3, font, new Vector2f(0,0), 1, false);
		textSpeed.setColour(0, 0, 0);
		
		//Position text
		String xpos, ypos, zpos;
		GUIText textPosition = new GUIText("" , 3, font, new Vector2f(0,0.15f), 1, false);
		textPosition.setColour(0, 0, 0);
		
		//Wing inclinations text
		String leftWingInc = String.valueOf(Math.round(drone.getLeftWing().getInclination() / Math.PI * 180));
		GUIText textLeftWing = new GUIText("l wing inc = " + leftWingInc + "deg", 3, font, new Vector2f(0,0.3f),1,false);
		textLeftWing.setColour(1, 0, 0);
		
		String rightWingInc = String.valueOf(Math.round(drone.getRightWing().getInclination() / Math.PI * 180 * 100.0)/100.0);
		GUIText textRightWing = new GUIText("r wing inc = " + rightWingInc + "deg", 3, font, new Vector2f(0,0.45f),1,false);
		textRightWing.setColour(1, 0, 0);
		
		String horzStab = String.valueOf(Math.round(drone.getHorStabilizer().getInclination() / Math.PI * 180 * 100.0)/100.0);
		GUIText textHorzStab = new GUIText("h stab inc = " + horzStab + "deg", 3, font, new Vector2f(0,0.60f), 1, false);
		textHorzStab.setColour(1, 0, 0);
		
		String vertStab = String.valueOf(Math.round(drone.getVertStabilizer().getInclination() / Math.PI * 180 * 100.0)/100.0);
		GUIText textVertStab = new GUIText("v stab inc = " + vertStab + "deg", 3, font, new Vector2f(0,0.75f), 1, false);
		textVertStab.setColour(1, 0, 0);
		
		

		//Load Trees
//		RawModel model = OBJLoader.loadObjModel("tree", loader);
//		TexturedModel staticModel = new TexturedModel(model,new ModelTexture(loader.loadTexture("tree")));
//		Random random = new Random();
//		for(int i=0;i<500;i++){
//			entities.add(new Entity(staticModel,
//					new Matrix4f().translate(new Vector3f(random.nextFloat()*800 - 400,0, random.nextFloat() * -600)), 1));
//		}
		
		light = new Light(new Vector3f(20000,20000,2000),new Vector3f(1,1,1));
		
		terrains.add(new Terrain(0,-1,loader,new ModelTexture(loader.loadTexture("greenScreen"))));
		terrains.add(new Terrain(-1,-1,loader,new ModelTexture(loader.loadTexture("greenScreen"))));
		terrains.add(new Terrain(0,-2,loader,new ModelTexture(loader.loadTexture("greenScreen"))));
		terrains.add(new Terrain(-1,-2,loader,new ModelTexture(loader.loadTexture("greenScreen"))));
		terrains.add(new LandingStrip(-0.5f,-1,loader,new ModelTexture(loader.loadTexture("landing"))));
		
		camera = new Camera(200, 200);	
		camera.setPosition(drone.getPosition().translate(0, 0, 0));
		renderer = new MasterRenderer();
		
		//Cube Render
		cubeShader = new CubeShader();
		cubeRenderer = new CubeRenderer(cubeShader, 120, 120);
		
		Cube c = new Cube(1, 1, 0);
		RawCubeModel cube = loader.loadToVAO(c.positions, c.colors);
		cubes.add(new Entity(cube, new Matrix4f().translate(new Vector3f(0, 20, -480)), 1));
		cubes.add(new Entity(cube, new Matrix4f().translate(new Vector3f(0, 20, -560)), 1));
		cubes.add(new Entity(cube, new Matrix4f().translate(new Vector3f(0, 20, -640)), 1));
		cubes.add(new Entity(cube, new Matrix4f().translate(new Vector3f(0, 20, -720)), 1));
		cubes.add(new Entity(cube, new Matrix4f().translate(new Vector3f(0, 20, -800)), 1));
		cubes.add(new Entity(cube, new Matrix4f().translate(new Vector3f(0, 20, -880)), 1));
		
		/* INITIALIZE AUTOPILOT */
		autopilot = AutopilotFactory.createAutopilot();
		autopilot.simulationStarted(autopilotConfig, drone.getAutoPilotInputs());
		
		//***INITIALIZE BUTTONS GUI***
		List<GuiTexture> guis = new ArrayList<>();
		GuiRenderer guiRenderer = new GuiRenderer(loader);
		
		createOpenFileButton();
		openFile.show(guis);
		
		createRandomCubeButton();
		randomCubes.show(guis);
		
		while(!Display.isCloseRequested()){
				
			//***BIG SCREEN***
			renderer.prepare();
			GL11.glViewport(0, 200, Display.getWidth(), Display.getHeight() - 200);
			GL11.glScissor(0, 200, Display.getWidth(), Display.getHeight() - 200);
			GL11.glEnable(GL11.GL_SCISSOR_TEST);
			//chaseCam.setPosition(chaseCam.getPosition().translate(0, -0.01f, -1));
			renderEntities(chaseCam, "3D");
			chaseCam.setPosition(drone.getPosition().translate(0, 2, 10));
			
			//CAMERA VIEW
			GL11.glViewport(0, 0, 200, 200);
			GL11.glScissor(0,0,200,200);
			GL11.glEnable(GL11.GL_SCISSOR_TEST);
			//camera.setPosition(camera.getPosition().translate(0, -0.01f, -1));
			renderEntities(camera, "Drone");
			camera.setPosition(drone.getPosition().translate(0, 0, -5));
			//drone.rotate(0.01f, new Vector3f(1, 0, 0));
			
			//GUI
			GL11.glViewport(200, 0, 600, 200);
			GL11.glScissor(200, 0, 600, 200);
			GL11.glEnable(GL11.GL_SCISSOR_TEST);
			renderer.prepareBlack();
			
			
			speed = String.valueOf(Math.round(drone.getLinearVelocity().x * 10.0) / 10.0)
					+ ", " + String.valueOf(Math.round(drone.getLinearVelocity().y * 10.0) / 10.0)
					+ ", " + String.valueOf(Math.round(drone.getLinearVelocity().z * 10.0) / 10.0);
			textSpeed .setString("vel = " + speed + "m/s");
			TextMaster.loadText(textSpeed);
			
			xpos = String.valueOf(Math.round(drone.getPosition().x));
			ypos = String.valueOf(Math.round(drone.getPosition().y));
			zpos = String.valueOf(Math.round(drone.getPosition().z));
			textPosition.setString("pos = ("+xpos+" , "+ypos+" , "+zpos +")");
			TextMaster.loadText(textPosition);
			
			leftWingInc = String.valueOf(Math.round(drone.getLeftWing().getInclination() / Math.PI * 180 * 100)/100.0);
			textLeftWing.setString("l wing inc = " + leftWingInc + "deg");
			TextMaster.loadText(textLeftWing);
			
			rightWingInc = String.valueOf(Math.round(drone.getRightWing().getInclination() / Math.PI * 180 * 100)/100.0);
			textRightWing.setString("r wing inc = " + rightWingInc + "deg");
			TextMaster.loadText(textRightWing);
			
			horzStab = String.valueOf(Math.round(drone.getHorStabilizer().getInclination() / Math.PI * 180 * 100)/100.0);
			textHorzStab.setString("h stab inc = " + horzStab + "deg");
			TextMaster.loadText(textHorzStab);
			
			vertStab = String.valueOf(Math.round(drone.getVertStabilizer().getInclination() / Math.PI * 180 * 100)/100.0);
			textVertStab.setString("v stab inc = " + vertStab + "deg");
			TextMaster.loadText(textVertStab);
			
			TextMaster.render();
			TextMaster.removeText(textSpeed);
			TextMaster.removeText(textPosition);
			TextMaster.removeText(textLeftWing);
			TextMaster.removeText(textRightWing);
			TextMaster.removeText(textHorzStab);
			TextMaster.removeText(textVertStab);
			
			//***BUTTON GUI***
			GL11.glViewport(0, 0, Display.getWidth(), Display.getHeight());
			GL11.glScissor(0, 0, Display.getWidth(), Display.getHeight());
			GL11.glEnable(GL11.GL_SCISSOR_TEST);
			
			guiRenderer.render(guis);
			openFile.checkHover();
			randomCubes.checkHover();
			
			
			//***UPDATES***
			float dt = DisplayManager.getFrameTimeSeconds();
			if(!entities.isEmpty() && dt > 0.00001) {
				
				// fysica toepassen
				try {
					PhysicsEngine.applyPhysics(drone, dt);
				} catch (DroneCrashException e) {
					System.err.println(e);
					System.exit(-1);
				} catch (MaxAoAException e) {
					System.err.println(e);
					System.exit(-1);
				}
				
				// Autopilot
				AutopilotInputs inputs = drone.getAutoPilotInputs();
				AutopilotOutputs outputs = autopilot.timePassed(inputs);
				drone.setAutopilotOutputs(outputs);
			}
			
			// check for keyboard input
			keyInputs();
			while (paused) // if M is pressed, the simulation is paused untill M is pressed again
			{
				try {Thread.sleep(20);} catch (InterruptedException e) {}
				keyInputs();
			}
			
			removeCubes();
			DisplayManager.updateDisplay();
		}

		renderer.cleanUp();
		loader.cleanUp();
		TextMaster.cleanUp();
		cubeShader.cleanUp();
		DisplayManager.closeDisplay();
	}
	
	private static void renderEntities(Camera camera, String type) {
		for (Terrain t : terrains) {
			renderer.processTerrain(t);
		}
		
		for(Entity entity:entities){
			renderer.processEntity(entity);
		}
		renderer.render(light, camera);
		
		cubeShader.start();
		cubeShader.loadViewMatrix(camera);
		for (Entity entity : cubes) {
			cubeRenderer.render(entity, cubeShader);
		}
		cubeShader.stop();
	}
	
	private static void removeCubes() {
		List<Entity> toRemove = new ArrayList<>();
		for (Entity e : cubes) {
			if (getEuclidDist(drone.getPosition(), e.getPosition()) <= 3) {
				toRemove.add(e);
			}
		}
		
		cubes.removeAll(toRemove);
	}
	
	private static float getEuclidDist(Vector3f vec1, Vector3f vec2){
		Vector3f temp = new Vector3f(0,0,0);
		Vector3f.sub(vec2, vec1, temp);
		return temp.length();
	}
	
	private static boolean paused = false;
	
	private static Camera camera;
	
	public static void keyInputs() {
		
		// update Keyboard events
		Display.processMessages();
		
		while (Keyboard.next()) 
		{
			if (Keyboard.getEventKeyState()) // only react to pressed event, not to released event
			{
				switch (Keyboard.getEventKey()) 
				{
					case Keyboard.KEY_L:
						/* Lock/Unlock on Third Person Camera */
						if (!lLock) {
							chaseCameraLocked = !chaseCameraLocked;
						}
						lLock = true;
						break;
						
					case Keyboard.KEY_O:
						oLock = true;
						break;
						
					case Keyboard.KEY_S:
						if (!sLock) {
							DisplayManager.start();
						}
						sLock = true;
						break;
						
					case Keyboard.KEY_R:
						reset();
						break;
						
					case Keyboard.KEY_P:
						camera.takeSnapshot();
						break;
						
					case Keyboard.KEY_M:
						paused = !paused;
						break;
				}
			}
		}
		
		if (chaseCameraLocked) {
			Vector3f.add(drone.getPosition(), new Vector3f(0, 0, 30), chaseCam.getPosition());
		} else {
			chaseCam.roam();
		}
		lLock = false;
		oLock = false;
		sLock = false;
	}
	
	private static void reset() {
		//Reset Cubes & Display
		generateRandomCubes();
		//DisplayManager.reset();
		
		//Reset Cameras
		chaseCameraLocked = true;
//		viewState = ViewStates.CHASE;
		
		entities.remove(drone);
		RawModel droneModel = OBJLoader.loadObjModel("tree", loader);
		TexturedModel staticDroneModel = new TexturedModel(droneModel,new ModelTexture(loader.loadTexture("tree")));
		drone = new Drone(staticDroneModel, new Matrix4f().translate(new Vector3f(0, 20, 0)), 1, autopilotConfig, new EulerPrediction(STEP_TIME));
		//drone.getPose().rotate((float) -(Math.PI/2), new Vector3f(1,0,0));
		entities.add(drone);
		
		//Reset AP
		autopilot = AutopilotFactory.createAutopilot();
		autopilot.simulationStarted(autopilotConfig, drone.getAutoPilotInputs());
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
		      float y = ((float) r.nextInt(20));
		      float z = i*-40;
		      Vector3f position = new Vector3f(x,y,z);
		      	
		      while(Math.sqrt(Math.pow(x - prevX, 2) + Math.pow(y - prevY, 2)) > 10) {
		    	  x = r.nextFloat()*20-10;
			      //x = 0;
			      y = ((float) r.nextInt(1000) / 500 - 1)*10;
		      }
		      
		      prevX = x;
		      prevY = y;
		      
		      cubes.add(new Entity(model, new Matrix4f().translate(position), 1));
		 }
		 
	}
	
	private static void createOpenFileButton() {
		JFileChooser fc = new JFileChooser();
		openFile = new Button(loader, "openfile", new Vector2f(0.9f, 0.9f), new Vector2f(0.05f, 0.05f)) {
			@Override
			public void whileHover() { }

			@Override
			public void stopHover() {
				this.setScale(new Vector2f(0.05f, 0.05f));
			}

			@Override
			public void startHover() {
				this.playHoverAnimation(0.01f);
			}
			
			@Override
			public void onClick() {
				this.playerClickAnimation(0.02f);

				int returnVal = fc.showOpenDialog(null);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					//Read file and load cubes
					loadCubes(file);
				} else {
					System.out.println("Open command cancelled by user.");
				}
			}
		};
	}
	
	private static void createRandomCubeButton() {
		randomCubes = new Button(loader, "random", new Vector2f(0.9f, 0.75f), new Vector2f(0.05f, 0.05f)) {
			@Override
			public void whileHover() {
			}
			
			@Override
			public void stopHover() {
			}
			
			@Override
			public void startHover() {
			}
			
			@Override
			public void onClick() {
				generateRandomCubes();
			}
		};
	}
	

	public static void loadCubes(File file) {
		
		//reset entities first
		cubes = new ArrayList<>();
		
		int count = 0;
		
		try(BufferedReader br = new BufferedReader(new FileReader(file))) {
		    for(String line; (line = br.readLine()) != null; ) {
		        String[] s = line.split(" ");
		        float x = Float.parseFloat(s[0]);
		        float y = Float.parseFloat(s[1]);
		        float z = Float.parseFloat(s[2]);
		        
		        Cube c = null;
		        
			    switch (count) {
					case 0:
						c = new Cube(1, 0, 0);
						break;
					case 1:
						c = new Cube(0, 1, 0);
						break;
					case 2:
						c = new Cube(1, 0, 1);
						break;
					case 3:
						c = new Cube(1, 1, 0);
						break;
					case 4:
						c = new Cube(0, 1, 1);
						break;
					default:
						break;
				}
		        count++;
		        
				RawCubeModel model = loader.loadToVAO(c.positions, c.colors);
		        
				cubes.add(new Entity(model, new Matrix4f().translate(new Vector3f(x, y, z)), 1));
		    }
		    // line is not visible here.
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
