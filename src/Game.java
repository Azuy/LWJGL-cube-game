import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ResourceLoader;


public class Game {
	
	private static final float MOUSE_SPEED_SCALE = 0.1f;
	private static final float MOVEMENT_SPEED = 5.0f;
	private static final float FALSE_GRAVITY_SPEED = 8.0f;
	private static final boolean FULLSCREEN = false;
	private static final boolean VSYNC = false;
	
	// Game components
	private Camera camera;
	private CubeTerrain terrain;
	private Skybox skybox;
	
	// Toggles
	private boolean flyMode = true;
	private boolean doCollisionChecking = false;
	private boolean renderSkybox = true;
	
	// Moving cube (trollface)
	private Cube movingCube;
	private Vector3f movingCubeVel = new Vector3f(0.0f, 0.0f, 20.0f);
	
	public void start() {
		// Create the display
		try {
			Display.setDisplayMode(Display.getDesktopDisplayMode());
			Display.setFullscreen(FULLSCREEN);
			Display.setVSyncEnabled(VSYNC);
			Display.create();
		} catch(LWJGLException e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		//hej
		
		int width = Display.getDesktopDisplayMode().getWidth();
		int height = Display.getDesktopDisplayMode().getHeight();
		
		// Initialize OpenGL
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		
		GLU.gluPerspective(45.0f, (float)width / (float)height, 0.1f, 200.0f);
		
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		
		// Set OpenGL options
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL11.GL_LIGHTING);
		
		GL11.glShadeModel(GL11.GL_SMOOTH); 
		
		GL11.glBlendFunc (GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_BLEND);
		
		// Create the ambient light
		float ambientLightArray[] = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
		FloatBuffer ambientLight = BufferUtils.createFloatBuffer(4);
		ambientLight.put(ambientLightArray);
		ambientLight.position(0);
		
		GL11.glLight(GL11.GL_LIGHT0, GL11.GL_AMBIENT, ambientLight);
		GL11.glEnable(GL11.GL_LIGHT0);
		
		// Hide the mouse
		Mouse.setGrabbed(true);
				
		Texture skyboxTexture = null;
		Texture trollfaceTexture = null;
		
		// Load textures
		try {
			skyboxTexture = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream("res/skybox.png"));
			trollfaceTexture = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream("res/troll.png"));
			
			// Create mipmaps
			CubeTerrain.createMipmaps(skyboxTexture);
			CubeTerrain.createMipmaps(trollfaceTexture);
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	
		// Create the terrain
		terrain = new CubeTerrain(new Vector3(50, 25, 50), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(-25.0f, -40.0f, -25.0f));
		
		final int TERRAIN_MAX_HEIGHT = 20;
		final int TERRAIN_MIN_HEIGHT = 3;
		final int TERRAIN_SMOOTH_LEVEL = 4;
		
		final int TERRAIN_GEN_SEED = 0;
		final float TERRAIN_GEN_NOISE_SIZE = 2.0f;
		final float TERRAIN_GEN_PERSISTENCE = 0.25f;
		final int TERRAIN_GEN_OCTAVES = 1;
		
		terrain.generateTerrain(TERRAIN_MAX_HEIGHT, TERRAIN_MIN_HEIGHT, TERRAIN_SMOOTH_LEVEL,
								TERRAIN_GEN_SEED, TERRAIN_GEN_NOISE_SIZE, TERRAIN_GEN_PERSISTENCE, TERRAIN_GEN_OCTAVES);
		
		// Create the camera
		camera = new Camera(new Vector3f(0.0f, 2.0f, 20.0f), new Vector3f(0.0f, 0.0f, 0.0f), terrain);
		
		// Create the skybox
		skybox = new Skybox(new Vector3f(-50.0f, -50.0f, -50.0f), new Vector3f(50.0f, 50.0f, 50.0f), null, skyboxTexture);
		
		// Create the moving cube
		movingCube = new Cube(new Vector3f(-5.0f, 30.0f, -5.0f), new Vector3f(5.0f, 40.0f, 5.0f), null, trollfaceTexture);
		
		// Main loop
		long lastFrame = System.currentTimeMillis();
		
		while(!Display.isCloseRequested()) {
			// Calculate delta time
			long t = System.currentTimeMillis();
			float deltaTime = (t - lastFrame) * 0.001f;
			
			// Clear the screen
			GL11.glClearColor(0.25f, 0.8f, 1.0f, 1.0f);
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			GL11.glLoadIdentity();

			// Apply the camera matrix
			camera.applyMatrix();
			
			// Render some rectangles
			GL11.glColor3f(0.5f, 0.5f, 1.0f);
			
			GL11.glBegin(GL11.GL_QUADS);
				GL11.glVertex3f(2.0f, 4.0f, -5.0f);
				GL11.glVertex3f(-2.0f, 4.0f, -5.0f);
				GL11.glVertex3f(-2.0f, 0.0f, -5.0f);
				GL11.glVertex3f(2.0f, 0.0f, -5.0f);
			GL11.glEnd();

			GL11.glColor3f(0.9f, 0.5f, 1.0f);
			GL11.glBegin(GL11.GL_QUADS);
				GL11.glVertex3f(5.0f, 0.0f, -5.0f);
				GL11.glVertex3f(-5.0f, 0.0f, -5.0f);
				GL11.glVertex3f(-5.0f, 0.0f, 5.0f);
				GL11.glVertex3f(5.0f, 0.0f, 5.0f);
			GL11.glEnd();
			
			// Render the terrain
			terrain.render();
			
			// Render the skybox
			if(renderSkybox)
				skybox.render();
			
			// Render the moving cube
			movingCube.render();
			
			// Set title to debug info
			Display.setTitle("x: " + camera.coordinates.x + " y: " + camera.coordinates.y + " z: " + camera.coordinates.z +
					" xRot: " + camera.rotation.x + " yRot: " + camera.rotation.y + " zRot: " + camera.rotation.z);
			
			// Updates the display, also polls the mouse and keyboard
			Display.update();
			
			// Handle mouse movement
			camera.addRotation(new Vector3f(Mouse.getDY() * MOUSE_SPEED_SCALE, -Mouse.getDX() * MOUSE_SPEED_SCALE, 0.0f));
			
			// Handle keypresses
			if(Keyboard.isKeyDown(Keyboard.KEY_ESCAPE))
				break;
			if(Keyboard.isKeyDown(Keyboard.KEY_W))
				camera.move(MOVEMENT_SPEED * deltaTime, Camera.FORWARD, 0, doCollisionChecking, flyMode);
			if(Keyboard.isKeyDown(Keyboard.KEY_S))
				camera.move(MOVEMENT_SPEED * deltaTime, Camera.BACKWARD, 0, doCollisionChecking, flyMode);
			if(Keyboard.isKeyDown(Keyboard.KEY_A))
				camera.move(MOVEMENT_SPEED * deltaTime, Camera.LEFT, 0, doCollisionChecking, flyMode);
			if(Keyboard.isKeyDown(Keyboard.KEY_D))
				camera.move(MOVEMENT_SPEED * deltaTime, Camera.RIGHT, 0, doCollisionChecking, flyMode);
			if(Keyboard.isKeyDown(Keyboard.KEY_SPACE))
				camera.move(0, Camera.RIGHT, -FALSE_GRAVITY_SPEED * 2 * deltaTime, doCollisionChecking, flyMode);
			if(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
				camera.move(0, Camera.RIGHT, FALSE_GRAVITY_SPEED * 2 * deltaTime, doCollisionChecking, flyMode);
			
			if(Keyboard.isKeyDown(Keyboard.KEY_UP)) {
				movingCube.pos1.y += deltaTime * 20.0f;
				movingCube.pos2.y += deltaTime * 20.0f;
			}
			else if(Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
				movingCube.pos1.y -= deltaTime * 20.0f;
				movingCube.pos2.y -= deltaTime * 20.0f;
			}
			
			// Check for pressed keys
			while (Keyboard.next()) {
				if (Keyboard.getEventKey() == Keyboard.KEY_F) {
				    if (Keyboard.getEventKeyState()) { 
				    	flyMode = !flyMode;
				    	System.out.println("Fly mode toggled");
				    }   
				} else if (Keyboard.getEventKey() == Keyboard.KEY_C) {
				    if (Keyboard.getEventKeyState()) { 
				    	doCollisionChecking = !doCollisionChecking;
				    	System.out.println("Collision checking toggled");
				    }   
				} else if (Keyboard.getEventKey() == Keyboard.KEY_B) {
				    if (Keyboard.getEventKeyState()) { 
				    	renderSkybox = !renderSkybox;
				    	System.out.println("Skybox toggled");
				    }   
				}
			}
			
			// Apply gravity
			if(!flyMode)
				camera.move(0, Camera.FORWARD, deltaTime * FALSE_GRAVITY_SPEED, doCollisionChecking, flyMode);
			
			// Update the coordinates and velocity of the moving cube
			movingCube.pos1.x += movingCubeVel.x * deltaTime;
			movingCube.pos1.y += movingCubeVel.y * deltaTime;
			movingCube.pos1.z += movingCubeVel.z * deltaTime;
			
			movingCube.pos2.x = movingCube.pos1.x + 10.0f;
			movingCube.pos2.y = movingCube.pos1.y + 10.0f;
			movingCube.pos2.z = movingCube.pos1.z + 10.0f;
			
			if(movingCube.pos1.z <= -40.0f) {
				movingCubeVel = new Vector3f(0.0f, 0.0f, 20.0f);
			} else if(movingCube.pos2.z >= 40.0f) {
				movingCubeVel = new Vector3f(0.0f, 0.0f, -20.0f);
			}
			
			lastFrame = t;
		}
		
		// Cleanup
		Display.destroy();
	}

	public static void main(String[] args) {
		Game cubeGame = new Game();
		cubeGame.start();
	}

}
