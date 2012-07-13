package obstacle;

import game.Cube;
import game.CubeTerrain;
import game.TextureStore;
import game.Vector4f;

public class TreeObstacle extends Obstacle {

	public TreeObstacle(CubeTerrain terrain, TextureStore textureStore) {
		super(terrain, textureStore);
	}
	
	public void createTree() {
		// Specify size
		xLength = 5;
		zLength = 5;
		yLength = 10;
		
		// Create array
		obstacleArray = new Cube[xLength][yLength][zLength];
		
		for(int x = 0; x < xLength; x++) {
			for(int y = 0; y < yLength; y++) {
				for(int z = 0; z < zLength; z++) {
					obstacleArray[x][y][z] = null;
				}
			}
		}
		
		// Create tree crown
		for(int x = 0; x < xLength; x++) {
			for(int y = yLength/2; y < yLength; y++) {
				for(int z = 0; z < zLength; z++) {
					obstacleArray[x][y][z] = new Cube(null, null, new Vector4f(0.0f, 1.0f, 0.0f, 1.0f), null);
				}
			}
		}
		
		// Create stem
		for(int y = 0; y < yLength - 1; y++ ) {
			obstacleArray[xLength/2][y][zLength/2] = new Cube(null, null, new Vector4f(0.5f, 0.25f, 0.0f, 1.0f), null);
		}
		
	}
	
}