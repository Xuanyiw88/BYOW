package byow.Core;

import java.io.Serializable;
import java.util.HashSet;

public class Coordinate implements Serializable {

    private final int x;
    private final int y;
    private boolean occupiedBoundary = false;
    private boolean occupiedRoom = false;
    private boolean occupiedHall = false;
    private Hallway hallway;
    private Room room;
    private HashSet<Room> connectedRooms;
    private String tile;

    public Coordinate(int xk, int yk) {
        x = xk;
        y = yk;
    }


    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean getOccupiedBoundary() {
        return occupiedBoundary;
    }

    public boolean getOccupiedRoom() {
        return occupiedRoom;
    }

    public boolean getOccupiedHall() {
        return occupiedHall;
    }

    public Room getRoom() {
        return room;
    }

    public Hallway getHallway() {
        return hallway;
    }

    public void changeOccupiedHall() {
        occupiedHall = true;
    }

    public void changeOccupiedRoom() {
        occupiedRoom = true;
    }

    public void changeOccupiedBoundary() {
        occupiedBoundary = true;
    }

    public void assignRoom(Room r) {
        room = r;
    }

    public void assignHallway(Hallway h) {
        hallway = h;
    }

    public void setTile(String t) {
        tile = t;
    }

    public String getTile() {
        return tile;
    }
}
