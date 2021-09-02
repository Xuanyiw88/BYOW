package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

public class Room {
    private static int numRooms = 0;
    private int roomNumber;
    private int height;
    private int width;
    private Coordinate cord;
    private Pair<Integer, Integer> midpoint;

    public Room(int w, int h) {
        height = h;
        width = w;
        roomNumber = numRooms;
        numRooms += 1;
    }

    public static int getNumRooms() {
        return numRooms;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public Coordinate getCord() {
        return cord;
    }

    public byow.Core.Pair<Integer, Integer> getMidpoint() {
        return midpoint;
    }

    public void setBL(Coordinate cordi) {
        cord = cordi;
        midpoint = new Pair<>(cordi.getX() + width / 2, cordi.getY() + height / 2);
    }

    public TETile[][] makeRoom(TETile[][] world) {
        int x = cord.getX();
        int y = cord.getY();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                world[x + i][y + j] = Tileset.FLOOR;
                Engine.coordinates[x + i][y + j].setTile(Engine.HALLWAY_TILE_STR);
            }
        }
        //makes the walls for the rooms
        int xWall = x - 1;
        int yWall = y - 1;
        for (int i = 0; i < width + 2; i++) {
            world[xWall + i][yWall] = Tileset.WALL;
            Engine.coordinates[xWall + i][yWall].setTile(Engine.WALL_TILE_STR);
            world[xWall + i][yWall + height + 1] = Tileset.WALL;
            Engine.coordinates[xWall + i][yWall + height + 1].setTile(Engine.WALL_TILE_STR);
        }
        for (int j = 0; j < height + 2; j++) {
            world[xWall][yWall + j] = Tileset.WALL;
            Engine.coordinates[xWall][yWall + j].setTile(Engine.WALL_TILE_STR);
            world[xWall + width + 1][yWall + j] = Tileset.WALL;
            Engine.coordinates[xWall + width + 1][yWall + j].setTile(Engine.WALL_TILE_STR);
        }
        return world;
    }
}
