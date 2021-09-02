package byow.Core;

import java.util.ArrayList;

public class Hallway {

    private ArrayList<Room> connectedRooms;

    public Hallway(Room r1, Room r2) {
        connectedRooms = new ArrayList<>();
        connectedRooms.add(r1);
        connectedRooms.add(r2);
    }

    public ArrayList<Room> getConnectedRooms() {
        return connectedRooms;
    }

    public void addConnectedRooms(Room r1, Room r2) {
        connectedRooms.add(r1);
        connectedRooms.add(r2);
    }

}
