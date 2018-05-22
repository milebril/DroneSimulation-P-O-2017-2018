package engineTester;

import org.lwjgl.util.vector.Vector3f;

import entities.Camera;
import entities.Drone;

public enum ViewEnumExtra {

	TOP_DOWN {
		@Override
		public Vector3f getCameraPosition(Drone drone, Camera camera) {
			camera.setYaw((float) (-Math.PI/2));
			camera.setPitch(0);
			return drone.getPosition().translate(0, 70, 0);
		}
		
	}, 
	LEFT_SIDE {
		@Override
		public Vector3f getCameraPosition(Drone drone, Camera camera) {
			camera.setYaw(0);
			camera.setPitch((float) (Math.PI/2));
			return drone.getPosition().translate(-50, 0, 0);
		}
	},
	RIGHT_SIDE {
		@Override
		public Vector3f getCameraPosition(Drone drone, Camera camera) {
			camera.setYaw(0);
			camera.setPitch((float) (-Math.PI/2));
			return drone.getPosition().translate(50, 0, 0);
		}
	};
	
	public abstract Vector3f getCameraPosition(Drone drone, Camera camera);
}
