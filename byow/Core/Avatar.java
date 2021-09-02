package byow.Core;

import byow.TileEngine.Tileset;

import java.io.Serializable;

public class Avatar implements Serializable {
    private int xpos;
    private int ypos;
    private String name;


    public Avatar(int x, int y) {
        xpos = x;
        ypos = y;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getXpos() {
        return xpos;
    }

    public int getYpos() {
        return ypos;
    }

    public void moveLeft() {
        if (Engine.world[xpos - 1][ypos] != Tileset.WALL) {
            Engine.coordinates[xpos][ypos].setTile("Floor");
            Engine.world[xpos][ypos] = Tileset.FLOOR;
            xpos--;
            Engine.coordinates[xpos][ypos].setTile(name);
            Engine.world[xpos][ypos] = Tileset.FLOWER;
        }
    }

    public void moveRight() {
        if (Engine.world[xpos + 1][ypos] != Tileset.WALL) {
            Engine.coordinates[xpos][ypos].setTile("Floor");
            Engine.world[xpos][ypos] = Tileset.FLOOR;
            xpos++;
            Engine.coordinates[xpos][ypos].setTile(name);
            Engine.world[xpos][ypos] = Tileset.FLOWER;
        }
    }

    public void moveUp() {
        if (Engine.world[xpos][ypos + 1] != Tileset.WALL) {
            Engine.coordinates[xpos][ypos].setTile("Floor");
            Engine.world[xpos][ypos] = Tileset.FLOOR;
            ypos++;
            Engine.coordinates[xpos][ypos].setTile(name);
            Engine.world[xpos][ypos] = Tileset.FLOWER;
        }
    }

    public void moveDown() {
        if (Engine.world[xpos][ypos - 1] != Tileset.WALL) {
            Engine.coordinates[xpos][ypos].setTile("Floor");
            Engine.world[xpos][ypos] = Tileset.FLOOR;
            ypos--;
            Engine.coordinates[xpos][ypos].setTile(name);
            Engine.world[xpos][ypos] = Tileset.FLOWER;
        }
    }



}
