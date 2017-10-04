

import java.util.ArrayList;
import java.util.List;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Line;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.event.EventHandler;
import javafx.geometry.Point3D;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.Node;

public class hallo extends Application {
	 
    final Group root = new Group();
    final Group axisGroup = new Group();
    final Xform world = new Xform();
    final PerspectiveCamera camera = new PerspectiveCamera(true);
    final Xform cameraXform = new Xform();
    final Xform cameraXform2 = new Xform();
    final Xform cameraXform3 = new Xform();
    final double cameraDistance = 450;
 
    boolean timelinePlaying = false;
    double ONE_FRAME = 1.0/24.0;
    double DELTA_MULTIPLIER = 200.0;
    double CONTROL_MULTIPLIER = 0.1;
    double SHIFT_MULTIPLIER = 0.1;
    double ALT_MULTIPLIER = 0.5;
        
    double mousePosX;
    double mousePosY;
    double mouseOldX;
    double mouseOldY;
    double mouseDeltaX;
    double mouseDeltaY;
    
    Box testBox;
    int x = 0;
    int speed = 4;
    int angle = 0;
    
    private void buildScene() {
        System.out.println("buildScene");
        root.getChildren().add(world);
    }
 
    @Override
    public void start(Stage primaryStage) {
        System.out.println("start");
        buildScene();
        buildCamera();
        buildAxes();
 
        Scene scene = new Scene(root, 1024, 768, true);
        scene.setFill(Color.GREY);
        
        // create axis walls
        Group grid = createGrid(400);

        // add objects to scene
        //StackPane root = new StackPane();
        root.getChildren().add(grid);
        
        handleKeyboard(scene, world);
        handleMouse(scene, world);
        
        primaryStage.setTitle("Drone Simulation");
        primaryStage.setScene(scene);
        primaryStage.show();
        scene.setCamera(camera);
        
        AnimationTimer animator = new AnimationTimer(){

			@Override
			public void handle(long arg0) {
				// TODO Auto-generated method stub
				x += speed;
				angle++;
				
				if (x > 120) {
					speed = -1;
				} else if (x < -120) {
					speed = 1;
				}
				
				testBox.setTranslateX(x);
				testBox.setTranslateZ(x);
				testBox.setRotate(angle);
			}
        	
        };
        
        animator.start();
    }
    
    private void buildCamera() {
        root.getChildren().add(cameraXform);
        cameraXform.getChildren().add(cameraXform2);
        cameraXform2.getChildren().add(cameraXform3);
        cameraXform3.getChildren().add(camera);
        cameraXform3.setRotateZ(180.0);
 
        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);
        camera.setTranslateZ(-cameraDistance);
        cameraXform.ry.setAngle(320.0);
        cameraXform.rx.setAngle(40);
    }
    
    private void buildAxes() {
        System.out.println("buildAxes()");
        final PhongMaterial redMaterial = new PhongMaterial();
        redMaterial.setDiffuseColor(Color.DARKRED);
        redMaterial.setSpecularColor(Color.RED);
 
        final PhongMaterial greenMaterial = new PhongMaterial();
        greenMaterial.setDiffuseColor(Color.DARKGREEN);
        greenMaterial.setSpecularColor(Color.GREEN);
 
        final PhongMaterial blueMaterial = new PhongMaterial();
        blueMaterial.setDiffuseColor(Color.DARKBLUE);
        blueMaterial.setSpecularColor(Color.BLUE);
 
        final Box xAxis = new Box(240.0, 0.5, 0.5); //Rood
        final Box yAxis = new Box(0.5, 240.0, 0.5); //Groen
        final Box zAxis = new Box(0.5, 0.5, 240.0); //Blauw
        
        testBox = new Box(10, 10, 10);
        testBox.setRotationAxis(new Point3D(1, 0, 0));
        testBox.setTranslateY(100);
        
        xAxis.setMaterial(redMaterial);
        yAxis.setMaterial(greenMaterial);
        zAxis.setMaterial(blueMaterial);
 
        
        
        axisGroup.getChildren().addAll(xAxis, yAxis, zAxis);
        world.getChildren().addAll(axisGroup);
        world.getChildren().add(testBox);
    }
    
 //
 // The handleCameraViews file contains the handleMouse() and handleKeyboard() 
 // methods that are used in the MoleculeSampleApp application to handle the 
 // different 3D camera views.  These methods are used in the Getting Started with 
 // JavaFX 3D Graphics tutorial. 
 //

    private void handleMouse(Scene scene, final Node root) {
         scene.setOnMousePressed(new EventHandler<MouseEvent>() {
             @Override 
             public void handle(MouseEvent me) {
                 mousePosX = me.getSceneX();
                 mousePosY = me.getSceneY();
                 mouseOldX = me.getSceneX();
                 mouseOldY = me.getSceneY();
             }
         });
         scene.setOnMouseDragged(new EventHandler<MouseEvent>() {
             @Override 
             public void handle(MouseEvent me) {
                 mouseOldX = mousePosX;
                 mouseOldY = mousePosY;
                 mousePosX = me.getSceneX();
                 mousePosY = me.getSceneY();
                 mouseDeltaX = (mousePosX - mouseOldX); 
                 mouseDeltaY = (mousePosY - mouseOldY); 
                 
                 double modifier = 1.0;
                 double modifierFactor = 0.1;
                 
                 if (me.isControlDown()) {
                     modifier = 0.1;
                 } 
                 if (me.isShiftDown()) {
                     modifier = 10.0;
                 }     
                 if (me.isPrimaryButtonDown()) {                     
                     cameraXform.t.setX(cameraXform.t.getX() + mouseDeltaX*modifierFactor*modifier*0.3);  // -
                     cameraXform.t.setY(cameraXform.t.getY() + mouseDeltaY*modifierFactor*modifier*0.3);  // -
                 }
                 else if (me.isSecondaryButtonDown()) {
                     double z = camera.getTranslateZ();
                     double newZ = z + mouseDeltaX;
                     camera.setTranslateZ(newZ);
                 }
                 else if (me.isMiddleButtonDown()) {
                     cameraXform.ry.setAngle(cameraXform.ry.getAngle() - mouseDeltaX*modifierFactor*modifier*2.0);  // +
                     cameraXform.rx.setAngle(cameraXform.rx.getAngle() + mouseDeltaY*modifierFactor*modifier*2.0);  // -
                 }
             }
         });
       scene.setOnScroll(new EventHandler<ScrollEvent>() {
		@Override
		public void handle(ScrollEvent event) {
			double z = camera.getTranslateZ();
            double newZ = z + event.getDeltaY();
            camera.setTranslateZ(newZ);
		}
	});  
         
     }


     private void handleKeyboard(Scene scene, final Node root) {
         scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
             @Override
             public void handle(KeyEvent event) {
                 switch (event.getCode()) {
                 case X:
                     if (axisGroup.isVisible()) {
                         System.out.println("setVisible(false)");
                         axisGroup.setVisible(false);
                     }
                     else {
                         System.out.println("setVisible(true)");
                         axisGroup.setVisible(true);
                     }
                     break;
                 case R:
            		 System.out.println("Resetting World");
            		 resetCamera();
            		 break;
				default:
					System.out.println("Error: unknown event");
					break;
                 }
             }
         });
     }

     
	private void resetCamera() {
	 cameraXform.t.setX(0);
	 cameraXform.t.setY(0);
	 camera.setTranslateZ(-cameraDistance);
	 cameraXform.ry.setAngle(320.0);
     cameraXform.rx.setAngle(40);
	}
	
	/**
     * Axis wall
     */
    public static class Axis extends Pane {

        Rectangle wall;

        public Axis(double size) {

            // wall
            // first the wall, then the lines => overlapping of lines over walls
            // works
            wall = new Rectangle(size, size);
            getChildren().add(wall);

            // grid
            double zTranslate = 0;
            double lineWidth = 0.5;
            Color gridColor = Color.RED;

            for (int y = 0; y <= size; y += size / 40) {

                Line line = new Line(0, 0, size, 0);
                line.setStroke(gridColor);
                line.setFill(gridColor);
                line.setTranslateY(y);
                line.setTranslateZ(zTranslate);
                line.setStrokeWidth(lineWidth);

                getChildren().addAll(line);

            }

            for (int x = 0; x <= size; x += size / 40) {

                Line line = new Line(0, 0, 0, size);
                line.setStroke(gridColor);
                line.setFill(gridColor);
                line.setTranslateX(x);
                line.setTranslateZ(zTranslate);
                line.setStrokeWidth(lineWidth);

                getChildren().addAll(line);

            }

        }

        public void setFill(Paint paint) {
            wall.setFill(paint);
        }

    }

    /**
     * Create axis walls
     *
     * @param size
     * @return
     */
    private Group createGrid(int size) {

        Group cube = new Group();

        // size of the cube
        Color color = Color.LIGHTGRAY;

        List<Axis> cubeFaces = new ArrayList<>();
        Axis r;
        
        // back face
        r = new Axis(size);
        r.setFill(color.deriveColor(0.0, 1.0, (1 - 0.5 * 1), 1.0));
        r.setTranslateX(-0.5 * size);
        r.setTranslateZ(0.5 * size);
        cubeFaces.add(r);
        
        // right face
        r = new Axis(size);
        r.setFill(color.deriveColor(0.0, 1.0, (1 - 0.2 * 1), 1.0));
        r.setTranslateX(-size);
        r.setRotationAxis(Rotate.Y_AXIS);
        r.setRotate(90);
        cubeFaces.add(r);
        
        // top face
        r = new Axis(size);
        r.setFill(color.deriveColor(0.0, 1.0, (1 - 0.1 * 1), 1.0));
        r.setTranslateX(-0.5 * size);
        r.setTranslateY(-0.5 * size);
        r.setRotationAxis(Rotate.X_AXIS);
        r.setRotate(90);
        cubeFaces.add( r);
        
        cube.getChildren().addAll(cubeFaces);
        
        return cube;
    }

    
    
    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.setProperty("prism.dirtyopts", "false");
        launch(args);
    }
}