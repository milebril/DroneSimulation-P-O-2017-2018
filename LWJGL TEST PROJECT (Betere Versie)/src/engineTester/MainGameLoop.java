package engineTester;

import models.RawModel;
import physicsEngine.DroneCrashException;
import physicsEngine.PhysicsEngine;
import physicsEngine.approximationMethods.EulerPrediction;

import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glOrtho;
import static org.lwjgl.opengl.GL11.glViewport;

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

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.opencv.core.Core;

import autopilot.AutopilotConfigReader;
import renderEngine.DisplayManager;
import renderEngine.Loader;
import renderEngine.Renderer;
import shaders.StaticShader;
import testObjects.Cube;
import testObjects.Cuboid;
import entities.Camera;
import entities.Drone;
import entities.Entity;
import fontMeshCreator.FontType;
import fontMeshCreator.GUIText;
import fontRendering.TextMaster;
import guis.Button;
import guis.GuiRenderer;
import guis.GuiTexture;
import interfaces.Autopilot;
import interfaces.AutopilotConfig;
import interfaces.AutopilotFactory;
import interfaces.AutopilotInputs;
import interfaces.AutopilotOutputs;

public class MainGameLoop {

	private static final float STEP_TIME = 0.001f;

	public static AutopilotConfig autopilotConfig;
	
	private static Drone drone;
	
	private static boolean oLock = false;
	private static boolean lLock = false;
	private static boolean sLock = false;
	
	private static List<Entity> entities;
	private static List<Entity> scaledEntities;
	
	public static Loader loader;
	
	//Renderers
	private static Renderer renderer;
	private static Renderer rendererFreeCam;
	private static Renderer renderTopDown;
	private static Renderer renderSideView;
	private static Renderer rendererText;
	
	//Shaders
	private static StaticShader shader;
	private static StaticShader shaderFreeCam;
	private static StaticShader shaderTopDown;
	private static StaticShader shaderSideView;
	private static StaticShader shaderText;
	
	//Cameras
	private static Camera freeRoamCamera;
	private static boolean freeRoamCameraLocked = true;
	
	private static Camera topDownCamera;
	private static Camera sideViewCamera;
	
	//Buttons
	private static Button openFile;
	private static Button randomCubes;

	//ViewStates
	private static ViewStates viewState = ViewStates.CHASE;
	private static enum ViewStates {
			CHASE, ORTHO
	};
	
	//Autopilot
	private static Autopilot autopilot;
	
	public static void main(String[] args) throws IOException {
		
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
		scaledEntities = new ArrayList<>();
		
		//***INITIALIZE DRONEVIEW***
		shader = new StaticShader();
		renderer = new Renderer(shader, autopilotConfig.getHorizontalAngleOfView(), autopilotConfig.getVerticalAngleOfView());
		
		Cuboid droneCube = new Cuboid(0, 0, 0);
		drone = new Drone(loader.loadToVAO(droneCube.positions, droneCube.colors, null),
				new Matrix4f().translate(new Vector3f(0, 0, 0)), 1, autopilotConfig, new EulerPrediction(STEP_TIME));
		
		//***INITIALIZE FREEROAM***
		shaderFreeCam = new StaticShader();
		rendererFreeCam = new Renderer(shaderFreeCam, 50, 50);
		freeRoamCamera = new Camera();
		freeRoamCamera.setPosition(new Vector3f(0, 100, 0));
		
		//***INITIALIZE TOPDOWN***
		shaderTopDown = new StaticShader();
		renderTopDown = new Renderer(shaderTopDown, 50, 40);
		topDownCamera = new Camera();
		topDownCamera.setPosition(new Vector3f(0, 300, -100));
		topDownCamera.setRotation((float) -(Math.PI / 2), 0, 0);
		
		//***INITIALIZE SIDEVIEW***
		shaderSideView = new StaticShader();
		renderSideView = new Renderer(shaderSideView, 40, 20);
		sideViewCamera = new Camera();
		sideViewCamera.setPosition(new Vector3f(300,0,-100));
		sideViewCamera.setRotation(0, (float) -(Math.PI / 2), 0);
		
		//***INITIALIZE GUI-TEXT***
		shaderText = new StaticShader();
		rendererText = new Renderer(shaderText, 50, 50);
		String speed = String.valueOf(Math.round(drone.getAbsVelocity()));
		FontType font = new FontType(loader.loadTexture("verdana"), new File("res/verdana.fnt"));
		GUIText textSpeed = new GUIText("Speed = " + speed + "m/s", 5, font, new Vector2f(0.01f,0), 1, true);
		textSpeed.setColour(1, 1, 1);
		
		String xpos, ypos, zpos;
		GUIText textPosition = new GUIText("" , 5, font, new Vector2f(0.01f,0.2f), 1, true);
		textPosition.setColour(1, 1, 1);
		
		Cube c = new Cube(1, 0, 0);
		RawModel redCubeModel = loader.loadToVAO(c.positions, c.colors, null);
		
//		entities.add(new Entity(redCubeModel, new Matrix4f().translate(new Vector3f(0,0,-80)), 1));
//		entities.add(new Entity(redCubeModel, new Matrix4f().translate(new Vector3f(3,5,-120)), 1));
//		entities.add(new Entity(redCubeModel, new Matrix4f().translate(new Vector3f(3,-5,-160)), 1));
//		entities.add(new Entity(redCubeModel, new Matrix4f().translate(new Vector3f(6,0,-200)), 1));
		
		//***WORKING DEMO***
//		entities.add(new Entity(redCubeModel, new Matrix4f().translate(new Vector3f(0,-10, -40)), 1));
//		entities.add(new Entity(redCubeModel, new Matrix4f().translate(new Vector3f(0,0,-80)), 1));
//		entities.add(new Entity(redCubeModel, new Matrix4f().translate(new Vector3f(0,-5,-120)), 1));
//		entities.add(new Entity(redCubeModel, new Matrix4f().translate(new Vector3f(0,8,-160)), 1));
//		entities.add(new Entity(redCubeModel, new Matrix4f().translate(new Vector3f(0,0,-200)), 1));
		
		//***INITIALIZE AP***
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
			//***DRONE CAMERA VIEW***
			renderCameraView();
			
			//****MAIN PANEL****
			renderMainScreen();
			
			//***GUI VIEW***
			GL11.glViewport(0, 200, 200, Display.getHeight() - 200);
			GL11.glScissor(0, 200, 200, Display.getHeight() - 200);
			GL11.glEnable(GL11.GL_SCISSOR_TEST);
			rendererText.prepareDroneCamera();

			speed = String.valueOf(Math.round(drone.getAbsVelocity()));
			textSpeed .setString("Speed = " + speed + "m/s");
			TextMaster.loadText(textSpeed);

			xpos = String.valueOf(Math.round(drone.getPosition().x));
			ypos = String.valueOf(Math.round(drone.getPosition().y));
			zpos = String.valueOf(Math.round(drone.getPosition().z));
			textPosition.setString("Position = ("+xpos+" , "+ypos+" , "+zpos +")");
			TextMaster.loadText(textPosition);
			
			TextMaster.render();
			TextMaster.removeText(textSpeed);
			TextMaster.removeText(textPosition);
			
			//***BUTTON GUI***
			GL11.glViewport(0, 0, Display.getWidth(), Display.getHeight());
			GL11.glScissor(0, 0, Display.getWidth(), Display.getHeight());
			GL11.glEnable(GL11.GL_SCISSOR_TEST);
			
			guiRenderer.render(guis);
			openFile.checkHover();
			randomCubes.checkHover();
			
			//***UPDATES***
			float dt = DisplayManager.getFrameTimeSeconds();
			if(!entities.isEmpty()) {
				
				//applyphysics rekent de krachten uit en gaat dan de kinematische waarden van de drone
				// aanpassen op basis daarvan 
				try {
					PhysicsEngine.applyPhysics(drone, dt);
				} catch (DroneCrashException e) {
					System.out.println(e);
				} // TODO: stop simulation (drone crashed)
				
				
				//Autopilot stuff
				AutopilotInputs inputs = drone.getAutoPilotInputs();
				AutopilotOutputs outputs = autopilot.timePassed(inputs);
				drone.setAutopilotOutouts(outputs);
				
				
			}
			
			keyInputs();
			removeCubes();
			
			shader.stop();
			shaderFreeCam.stop();
			shaderTopDown.stop();
			shaderSideView.stop();
			DisplayManager.updateDisplay();
		}

		guiRenderer.cleanUp();
		TextMaster.cleanUp();
		shader.cleanUp();
		shaderFreeCam.cleanUp();
		shaderTopDown.cleanUp();
		shaderText.cleanUp();
		shaderSideView.cleanUp();
		loader.cleanUp();
		DisplayManager.closeDisplay();

	}
	
	private static void removeCubes() {
		List<Entity> toRemove = new ArrayList<>();
		List<Entity> toRemoveScaled = new ArrayList<>();		
		for (Entity e : entities) {
			if (getEuclidDist(drone.getPosition(), e.getPosition()) <= 4) {
				toRemove.add(e);
			}
		}
		
		entities.removeAll(toRemove);
		
		for (Entity e : scaledEntities) {
			if (getEuclidDist(drone.getPosition(), e.getPosition()) <= 4) {
				toRemoveScaled.add(e);
			}
		}
		
		scaledEntities.removeAll(toRemoveScaled);
	}
	
	private static float getEuclidDist(Vector3f vec1, Vector3f vec2){
		Vector3f temp = new Vector3f(0,0,0);
		Vector3f.sub(vec2, vec1, temp);
		return temp.length();
	}
	
	public static void renderView(Renderer renderer, StaticShader shader) {
		for (Entity entity : entities) {
			renderer.render(entity,shader);
		} 
		
		renderer.render(drone, shader);
	}
	
	public static void renderViewScaled(Renderer renderer, StaticShader shader) {
		for (Entity entity : scaledEntities) {
			renderer.render(entity,shader);
		} 
		
		renderer.render(drone, shader);
	}
	
	private static void renderCameraView() {
		drone.getCamera().setPosition(drone.getPosition());	
		GL11.glViewport(0, 0, 200, 200);
		GL11.glScissor(0,0,200,200);
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		renderer.prepare();
		shader.start();
		shader.loadViewMatrix(drone.getCamera());
		renderView(renderer, shader);
		
		if (Keyboard.isKeyDown(Keyboard.KEY_P)) {
			drone.getCamera().takeSnapshot();
		}
	}
	
	private static void renderMainScreen() {
		if (viewState == ViewStates.CHASE) {
			//3rd Person View (FreeCam)
			GL11.glViewport(200+1, 0, Display.getWidth() - 201, Display.getHeight());
			GL11.glScissor(200+1, 0, Display.getWidth()- 201, Display.getHeight());
			GL11.glEnable(GL11.GL_SCISSOR_TEST);
			renderSideView.prepareText();
			shaderFreeCam.start();
			shaderFreeCam.loadViewMatrix(freeRoamCamera);
			renderView(rendererFreeCam, shaderFreeCam);
		} else {
			//MAKE BLACK LINE
			GL11.glViewport(200+1, 0, Display.getWidth() - 201, Display.getHeight());
			GL11.glScissor(200+1, 0, Display.getWidth()- 201, Display.getHeight());
			GL11.glEnable(GL11.GL_SCISSOR_TEST);
			renderTopDown.prepareText();
			renderTopDown.prepare();
			
			//TopDown View
			GL11.glViewport(200 + 1, Display.getHeight()/2 + 1 ,Display.getWidth() - 201, Display.getHeight()/2);
			GL11.glScissor(200 + 1, Display.getHeight()/2  + 1, Display.getWidth() - 201, Display.getHeight()/2);
			GL11.glEnable(GL11.GL_SCISSOR_TEST);
			renderTopDown.prepare();
			shaderTopDown.start();
			shaderTopDown.loadViewMatrix(topDownCamera);
			
			GL11.glMatrixMode(GL11.GL_PROJECTION);
			GL11.glLoadIdentity();
			//GL11.glOrtho(200+1, Display.getWidth(), Display.getHeight(), Display.getHeight()/2 + 1, 1, -1);
			GL11.glOrtho(0, Display.getWidth(), 0, Display.getHeight() + 1, 1, -1);
			GL11.glMatrixMode(GL11.GL_MODELVIEW);

			renderViewScaled(renderTopDown, shaderTopDown);
			
			//SideView
            GL11.glScissor(200 + 1, 0 ,Display.getWidth() - 201, Display.getHeight()/2);
			GL11.glEnable(GL11.GL_SCISSOR_TEST);
			
			glMatrixMode(GL_PROJECTION);
            glLoadIdentity();
            glOrtho(0, Display.getWidth(), 0, Display.getHeight() + 1, 1, -1);
            //GL11.glTranslatef(sideViewCamera.getPosition().x - Display.getWidth()/2, sideViewCamera.getPosition().x - Display.getHeight()/2, 0.0f);
            GL11.glPushMatrix();
            GL11.glTranslatef(sideViewCamera.getPosition().x, -sideViewCamera.getPosition().y, 0f);
//            glMatrixMode(GL_MODELVIEW);
//            glLoadIdentity();
            glViewport(200 + 1, 0,Display.getWidth() - 201, Display.getHeight()/2);
			
			renderSideView.prepareText();
			shaderSideView.start();
			shaderSideView.loadViewMatrix(sideViewCamera);
			
			renderViewScaled(renderSideView, shaderSideView);
			
			GL11.glPopMatrix();
		}
	}
	
	public static void keyInputs() {
		if (Keyboard.isKeyDown(Keyboard.KEY_Y)) {
			Vector3f.add(drone.getPosition(), new Vector3f(0, 150, -50), freeRoamCamera.getPosition());
			freeRoamCamera.setRotation((float) -(Math.PI / 2), 0, 0);
		} else if (Keyboard.isKeyDown(Keyboard.KEY_X)) {
			Vector3f.add(drone.getPosition(), new Vector3f(100, 0, 0), freeRoamCamera.getPosition());
			freeRoamCamera.setRotation(0, (float) -(Math.PI / 2), 0);
		} else if(Keyboard.isKeyDown(Keyboard.KEY_L)) {
			/* Lock/Unlock on Third Person Camera */
			if (!lLock) {
				freeRoamCameraLocked = !freeRoamCameraLocked;
			}
			lLock = true;
		} else if (Keyboard.isKeyDown(Keyboard.KEY_O)) {
			
			if (!oLock)
				if (viewState == ViewStates.ORTHO) {
					viewState = ViewStates.CHASE;
				} else {
					viewState = ViewStates.ORTHO;
				}
			
			oLock = true;
		} else if(Keyboard.isKeyDown(Keyboard.KEY_S)) {
			if (!sLock) {
				DisplayManager.start();
			}
			
			sLock = true;
		} else if(Keyboard.isKeyDown(Keyboard.KEY_R)) {
			reset();
		}else {
			if (freeRoamCameraLocked) {
				Vector3f.add(drone.getPosition(), new Vector3f(0, 0, 30), freeRoamCamera.getPosition());
				//freeRoamCamera.setRotation((float) -(Math.PI/6), 0, 0);
			} else {
				freeRoamCamera.roam();
			}
			
			lLock = false;
			oLock = false;
			sLock = false;
		}
	}
	
	public static void loadCubes(File file) {
		Random r = new Random();
		
		//reset entities first
		entities = new ArrayList<>();
		scaledEntities = new ArrayList<>();
		
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
		        
				RawModel model = loader.loadToVAO(c.positions, c.colors, null);
		        
		        entities.add(new Entity(model, new Matrix4f().translate(new Vector3f(x, y, z)), 1));
		        scaledEntities.add(new Entity(model, new Matrix4f().translate(new Vector3f(x, y, z)), 2));
		    }
		    // line is not visible here.
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void generateRandomCubes() {
		Random r = new Random();
		entities = new ArrayList<>();
		scaledEntities = new ArrayList<>();
		
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
		      
		      RawModel model = loader.loadToVAO(c.positions, c.colors, null);
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
		      
		      entities.add(new Entity(model, new Matrix4f().translate(position), 1));
		      scaledEntities.add(new Entity(model, new Matrix4f().translate(position), 2));
		 }
	}
	
	private static void reset() {
		//Reset Cubes & Display
		generateRandomCubes();
		DisplayManager.reset();
		
		//Reset Cameras
		freeRoamCameraLocked = true;
		viewState = ViewStates.CHASE;
		
		//Reset Drone
		Cuboid droneCube = new Cuboid(0, 0, 0);
		drone = new Drone(loader.loadToVAO(droneCube.positions, droneCube.colors, null),
				new Matrix4f().translate(new Vector3f(0, 0, 0)), 1, autopilotConfig, new EulerPrediction(STEP_TIME));
		
		//Reset AP
		autopilot = AutopilotFactory.createAutopilot();
		autopilot.simulationStarted(autopilotConfig, drone.getAutoPilotInputs());
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
}
