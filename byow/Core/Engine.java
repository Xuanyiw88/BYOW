package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import edu.princeton.cs.introcs.StdDraw;
import static byow.Core.Utils.*;

import java.awt.*;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;
import edu.princeton.cs.algs4.WeightedQuickUnionUF;

public class Engine implements Serializable {
    public static final int MAX_ROOM_SIZE = 7;
    public static final int MIN_ROOM_SIZE = 3;
    public static final int BOUNDARIES = 3;
    public static final int WIDTH = 80;
    public static final int HEIGHT = 60;
    public static final int ROOM_SPACE = (int) (WIDTH * HEIGHT * 0.10);
    public static final File CWD = new File(System.getProperty("user.dir"));


    public static TETile AVATAR_TILE = Tileset.FLOWER;
    public static TETile HALLWAY_TILE = Tileset.FLOOR;
    public static String HALLWAY_TILE_STR = "Floor";
    public static TETile WALL_TILE = Tileset.WALL;
    public static String WALL_TILE_STR = "Wall";
    public static TETile BACKGROUND_TILE = Tileset.NOTHING;
    public static String BACKGROUND_TILE_STR = "Nothing";
    public static TETile BL_CORNER_TILE = Tileset.FLOOR;

    public static Random rand;
    public static Long seed;
    public static TETile[][] world;
    public static Coordinate[][] coordinates;
    public static ArrayList<Room> roomListPerm;
    public static WeightedQuickUnionUF wquRoom;
    public static Avatar avatar;
    public static String avatarName = "Avatar";

    public static void initializeWorld(TETile[][] world) {
        coordinates = new Coordinate[WIDTH][HEIGHT];
        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                Coordinate cord = new Coordinate(i, j);
                coordinates[i][j] = cord;
                cord.setTile(BACKGROUND_TILE_STR);
                world[i][j] = BACKGROUND_TILE;
            }
        }
    }

    public static void addRoom() {
        int totalRoomTiles = 0;
        roomListPerm = new ArrayList<>();
        while (totalRoomTiles < ROOM_SPACE) {
            //generate a random room size for the room that we are going to make
            int hei = randIntBound(MIN_ROOM_SIZE, MAX_ROOM_SIZE);
            int wid = randIntBound(MIN_ROOM_SIZE, MAX_ROOM_SIZE);
            Room room = new Room(wid, hei);
            //gets bottom-left corner of the room and return the x, y coords of it in a Pair
            Pair<Integer, Integer> bl = findBottomCoor(wid, hei);
            //accessing the bottom left corner because it was returned as a pair
            int x = bl.getFirst();
            int y = bl.getSecond();
            //updates the room object to have the object cords as the bottom left corner
            room.setBL(coordinates[x][y]);
            //adds room to a list
            roomListPerm.add(room);
            //sets the region that room takes up, its boundary, and assigns a room to each coordinate
            setRoom(bl.getFirst(), bl.getSecond(), wid, hei, room);
            setBoundaries(bl.getFirst(), bl.getSecond(), wid, hei);
            //this helper method actually constructs the room
            world = room.makeRoom(world);
            //adds to the totalRoomTiles count to check if there is sufficient rooms
            totalRoomTiles += hei * wid;
        }
    }


    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    public void interactWithKeyboard() {
        drawInitialFrame();
        world = new TETile[WIDTH][HEIGHT];
        char lastKey = 0;
        TERenderer t = new TERenderer();
        boolean firstKeyPressed = false;
        boolean lightOff = false;
        while (true) {
            if (firstKeyPressed) {
                mouseHUD();
            }
            if (StdDraw.hasNextKeyTyped()) {
                char c = StdDraw.nextKeyTyped();
                if (!firstKeyPressed) {
                    if (c == 'N' || c == 'n') {
                        Long s = inputSeed();
                        if (s != null) {
                            StdDraw.clear(Color.black);
                            seed = s;
                            rand = new Random(seed);
                            worldGeneration();
                            placeAvatar();
                            TERenderer ter = new TERenderer();
                            ter.initialize(WIDTH, HEIGHT);
                            ter.renderFrame(world);
                        }
                        firstKeyPressed = true;
                    } else if (c == 'L' || c == 'l') {
                        File f = join(CWD, "seed.txt");
                        File g = join(CWD, "avatar.txt");
                        File h = join(CWD, "lights.txt");
                        if (!f.exists()) {
                            drawFrame("No save state found");
                            StdDraw.pause(1000);
                            drawFrame("Goodbye!");
                            StdDraw.pause(1000);
                            System.exit(0);
                        } else {
                            StdDraw.clear(Color.black);
                            seed = readObject(f, Long.class);
                            lightOff = readObject(h, Boolean.class);
                            rand = new Random(seed);
                            System.out.println(seed);
                            avatar = readObject(g, Avatar.class);
                            avatarName = avatar.getName();
                            worldGeneration();
                            placeAvatar(avatar.getXpos(), avatar.getYpos());
                            TERenderer ter = new TERenderer();
                            ter.initialize(WIDTH, HEIGHT);
                            if (!lightOff) {
                                t.renderFrame(world);
                            } else {
                                t.renderFrameLight(world, avatar.getXpos(), avatar.getYpos());
                            }
                        }
                        firstKeyPressed = true;
                    } else if (c == 'Q' || c == 'q') {
                        System.exit(0);
                        firstKeyPressed = true;
                    } else if (c == 'C' || c == 'c') {
                        drawFrameCS("");
                        String s = "";
                        boolean doneTyping = false;
                        int numCharsInputted = 0;
                        while (numCharsInputted < 15 && !doneTyping) {
                            if (StdDraw.hasNextKeyTyped()) {
                                char h = StdDraw.nextKeyTyped();
                                if (h == '1') {
                                    doneTyping = true;
                                } else {
                                    s += h;
                                    drawFrameCS(s);
                                    numCharsInputted++;
                                }
                            }
                        }
                        avatarName = s;
                        drawInitialFrame();
                    }
                } else {
                    if ((c == 'q' || c == 'Q') && lastKey == ':') {
                        File f = join(CWD, "seed.txt");
                        File g = join(CWD, "avatar.txt");
                        File h = join(CWD, "lights.txt");
                        writeObject(f, seed);
                        writeObject(g, avatar);
                        writeObject(h, lightOff);
                        System.exit(0);
                    } else if (c == 'W' || c == 'w') {
                        avatar.moveUp();
                        if (!lightOff) {
                            t.renderFrame(world);
                        } else {
                            t.renderFrameLight(world, avatar.getXpos(), avatar.getYpos());
                        }
                    } else if (c == 'A' || c == 'a') {
                        avatar.moveLeft();
                        if (!lightOff) {
                            t.renderFrame(world);
                        } else {
                            t.renderFrameLight(world, avatar.getXpos(), avatar.getYpos());
                        }
                    } else if (c == 'S' || c == 's') {
                        avatar.moveDown();
                        if (!lightOff) {
                            t.renderFrame(world);
                        } else {
                            t.renderFrameLight(world, avatar.getXpos(), avatar.getYpos());
                        }
                    } else if (c == 'D' || c == 'd') {
                        avatar.moveRight();
                        if (!lightOff) {
                            t.renderFrame(world);
                        } else {
                            t.renderFrameLight(world, avatar.getXpos(), avatar.getYpos());
                        }
                    } else if (c == '0') {
                        lightOff = !lightOff;
                        if (!lightOff) {
                            t.renderFrame(world);
                        } else {
                            t.renderFrameLight(world, avatar.getXpos(), avatar.getYpos());
                        }
                    }
                    lastKey = c;
                }
            }
        }
    }


    public static Long inputSeed() {
        drawFrame("Please enter a seed:");
        String s = "";
        boolean errorNumberOnly = false;
        int numCharsInputted = 0;
        while (numCharsInputted < 19) {
            if (StdDraw.hasNextKeyTyped()) {
                char c = StdDraw.nextKeyTyped();
                if (c == 's' || c == 'S') {
                    return Long.parseLong(s);
                } else if (!Character.isDigit(c)) {
                    drawFrame("Must enter a number");
                    StdDraw.pause(500);
                    errorNumberOnly = true;
                    drawFrame("Seed: " + s);
                } else {
                    s += c;
                    drawFrame("Seed: " + s);
                    numCharsInputted++;
                }
            }
        }
        if (!errorNumberOnly) {
            drawFrame("Seed inputted was too long");
        } else {
            return Long.parseLong(s);
        }
        return null;
    }


    public static void drawFrame(String s) {
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(StdDraw.WHITE);
        //middle string
        Font font = new Font("Monaco", Font.BOLD, 40);
        StdDraw.setFont(font);
        StdDraw.text(WIDTH / 2, HEIGHT / 2, s);
        StdDraw.show();
    }

    public static void drawFrameCS(String s) {
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(StdDraw.WHITE);
        //middle string
        Font font = new Font("Monaco", Font.BOLD, 40);
        StdDraw.setFont(font);
        StdDraw.text(WIDTH / 2, HEIGHT / 2, s);
        Font font2 = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(font2);
        StdDraw.text(WIDTH / 2, HEIGHT / 4 * 3, "Press 1 When Done");
        StdDraw.text(WIDTH / 2, HEIGHT * 0.7, "(15 Characters Max)");
        StdDraw.show();
    }

    
    /**
     * Method used for autograding and testing your code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     *
     * Recall that strings ending in ":q" should cause the game to quite save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     *
     * In other words, both of these calls:
     *   - interactWithInputString("n123sss:q")
     *   - interactWithInputString("lww")
     *
     * should yield the exact same world state as:
     *   - interactWithInputString("n123sssww")
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] interactWithInputString(String input) {
        int ind = 0;
        char lastKey = 0;
        TERenderer t = new TERenderer();
        boolean firstKeyPressed = false;
        boolean lightOff = false;
        while (input.length() != ind) {
            Long s = null;
            char c = input.charAt(ind);
            if (!firstKeyPressed) {
                if (c == 'N' || c == 'n') {
                    ind++;
                    String sr = "";
                    int numCharsInputted = 0;
                    boolean seedEnd = false;
                    while (numCharsInputted < 20 && !seedEnd) {
                        char ch = input.charAt(ind);
                        if (ch == 's' || ch == 'S') {
                            s = Long.parseLong(sr);
                            seedEnd = true;
                        } else if (!Character.isDigit(ch)) {
                        } else {
                            sr += ch;
                            numCharsInputted++;
                            ind++;
                        }
                    }
                    if (s != null) {
                        seed = s;
                        rand = new Random(seed);
                        worldGeneration();
                        placeAvatar();
                    }
                    firstKeyPressed = true;
                } else if (c == 'L' || c == 'l') {
                    File f = join(CWD, "seed.txt");
                    File g = join(CWD, "avatar.txt");
                    File h = join(CWD, "lights.txt");
                    if (!f.exists()) {
                    } else {
                        seed = readObject(f, Long.class);
                        lightOff = readObject(h, Boolean.class);
                        rand = new Random(seed);
                        System.out.println(seed);
                        avatar = readObject(g, Avatar.class);
                        avatarName = avatar.getName();
                        worldGeneration();
                        placeAvatar(avatar.getXpos(), avatar.getYpos());
                    }
                    firstKeyPressed = true;
                } else if (c == 'Q' || c == 'q') {
                    firstKeyPressed = true;
                }
            } else {
                if ((c == 'q' || c == 'Q') && lastKey == ':') {
                    File f = join(CWD, "seed.txt");
                    File g = join(CWD, "avatar.txt");
                    File h = join(CWD, "lights.txt");
                    writeObject(f, seed);
                    writeObject(g, avatar);
                    writeObject(h, lightOff);
                    
                } else if (c == 'W' || c == 'w') {
                    avatar.moveUp();
                } else if (c == 'A' || c == 'a') {
                    avatar.moveLeft();
                } else if (c == 'S' || c == 's') {
                    avatar.moveDown();
                } else if (c == 'D' || c == 'd') {
                    avatar.moveRight();
                } else if (c == '0') {
                    lightOff = !lightOff;
                }
                lastKey = c;
            }
            ind++;
        }
        return world;
    }

    public static void worldGeneration() {
        world = new TETile[WIDTH][HEIGHT];
        initializeWorld(world);
        addRoom();
        addHallways();
        wrapHallway();
    }

    public static void placeAvatar(int x, int y) {
        avatar = new Avatar(x, y);
        avatar.setName(avatarName);
        coordinates[x][y].setTile(avatar.getName());
        world[x][y] = AVATAR_TILE;
    }

    public static void placeAvatar() {
        int i = randIntBound(0, roomListPerm.size() - 1);
        Room r = roomListPerm.get(i);
        int x = r.getCord().getX();
        int y = r.getCord().getY();
        avatar = new Avatar(x, y);
        avatar.setName(avatarName);
        coordinates[x][y].setTile(avatarName);
        world[x][y] = AVATAR_TILE;
    }

    /** Method to add all the hallways that are needed in the world */
    public static void addHallways() {
        wquRoom = new WeightedQuickUnionUF(Room.getNumRooms());
        ArrayList<Room> roomList = new ArrayList<>(roomListPerm);
        ArrayList<Room> copy = new ArrayList<>(roomList);
        while (!roomList.isEmpty()) {
            int ind = randIntBound(0, roomList.size() - 1);
            Room curr = roomList.remove(ind);
            Room neigh = findNeighborRoom(curr, roomList);
            if (neigh != null) {
                roomList.remove(neigh);
                Pair<String, String> chose = chooseSide(curr, neigh);
                wquRoom.union(curr.getRoomNumber(), neigh.getRoomNumber());
                constructHallways(curr, neigh, chose.getFirst(), chose.getSecond());
            } else {
                copy.remove(curr);
                Room room = findNeighborRoom(curr, copy);
                Pair<String, String> chose = chooseSide(curr, room);
                wquRoom.union(curr.getRoomNumber(), room.getRoomNumber());
                constructHallways(curr, room, chose.getFirst(), chose.getSecond());
            }
        }
        while (findClosestUnconnected(roomListPerm.get(0)) != null) {
            ArrayList<Room> temp = new ArrayList<>(roomListPerm);
            int n = randIntBound(0, temp.size() - 1);
            Room r = temp.get(n);
            Room r2 = findClosestUnconnected(r);
            Room connect = getCloser(r2 , r);
            Pair<String, String> chose = chooseSide(r2, connect);
            wquRoom.union(r2.getRoomNumber(), connect.getRoomNumber());
            constructHallways(r2, connect, chose.getFirst(), chose.getSecond());
        }
    }

    public static Room getCloser(Room goal, Room connected) {
        Room min = connected;
        for (Room i : roomListPerm) {
            if (distanceRoom(min, goal) > distanceRoom(i, goal)
                    && wquRoom.connected(connected.getRoomNumber(), i.getRoomNumber())) {
                min = i;
            }
        }
        return min;
    }

    public static Room findClosestUnconnected(Room r) {
        ArrayList<Room> temp = new ArrayList<>(roomListPerm);
        temp.remove(r);
        while (!temp.isEmpty()) {
            Room closest = findNeighborRoom(r, temp);
            if (wquRoom.connected(r.getRoomNumber(), closest.getRoomNumber())) {
                temp.remove(closest);
            } else {
                return closest;
            }
        }
        return null;
    }

    /** Method to create the Hallways between two rooms. Takes in the rooms that will be
     * connected and the sides of where the room should have the entrance on. */
    public static void constructHallways(Room r1, Room r2, String s1, String s2) {
        Hallway h = new Hallway(r1, r2);
        Pair<Integer, Integer> e1 = makeEntrances(r1, s1, h);
        Pair<Integer, Integer> e2 = makeEntrances(r2, s2, h);
        int currX = e1.getFirst();
        int currY = e1.getSecond();
        String currDir = "Y";
        String lastHor = null;
        String lastVert = null;
        while (currX != e2.getFirst() || currY != e2.getSecond()) {
            outerloop:
            if (currDir.equals("Y")) {
                if (currY > e2.getSecond()) {
                    //going down
                    lastVert = "D";
                    while (currY > e2.getSecond()) {
                        if (!coordinates[currX][currY - 1].getOccupiedRoom()) {
                            setHallway(currX, currY, h);
                            currY--;
                        } else {
                            setHallway(currX, currY, h);
                            currDir = "X";
                            break outerloop;
                        }
                    }
                } else if (currY < e2.getSecond()) {
                    //going up
                    lastVert = "U";
                    while (currY < e2.getSecond()) {
                        if (!coordinates[currX][currY + 1].getOccupiedRoom()) {
                            setHallway(currX, currY, h);
                            currY++;
                        } else {
                            setHallway(currX, currY, h);
                            currDir = "X";
                            break outerloop;
                        }
                    }
                } else {
                    //go thru
                    if ("R".equals(lastHor) && coordinates[currX + 1][currY].getOccupiedRoom()) {
                        setHallway(currX, currY, h);
                        currX++;
                    } else if ("L".equals(lastHor) && coordinates[currX - 1][currY].getOccupiedRoom()) {
                        setHallway(currX, currY, h);
                        currX--;
                    } else {
                        setHallway(currX, currY, h);
                        currDir = "X";
                        break outerloop;
                    }
                }
            } else {
                if (currX > e2.getFirst()) {
                    //go left
                    lastHor = "L";
                    while (currX > e2.getFirst()) {
                        if (!coordinates[currX - 1][currY].getOccupiedRoom()) {
                            setHallway(currX, currY, h);
                            currX--;
                        } else {
                            setHallway(currX, currY, h);
                            currDir = "Y";
                            break outerloop;
                        }
                    }
                } else if (currX < e2.getFirst()) {
                    while (currX < e2.getFirst()) {
                        //go right
                        lastHor = "R";
                        if (!coordinates[currX + 1][currY].getOccupiedRoom()) {
                            setHallway(currX, currY, h);
                            currX++;
                        } else {
                            setHallway(currX, currY, h);
                            currDir = "Y";
                            break outerloop;
                        }
                    }
                    //switch/go thru
                } else {
                    //go thru
                    if ("U".equals(lastVert) && coordinates[currX][currY + 1].getOccupiedRoom()) {
                        setHallway(currX, currY, h);
                        currY++;
                    } else if ("D".equals(lastVert) && coordinates[currX][currY - 1].getOccupiedRoom()) {
                        setHallway(currX, currY, h);
                        currY--;
                    } else {
                        setHallway(currX, currY, h);
                        currDir = "Y";
                        break outerloop;
                    }
                }
            }
        }
    }

    public static void wrapHallway() {
        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                if (coordinates[i][j].getOccupiedHall()) {
                    oneHallway(i, j);
                }
            }
        }
    }

    public static void oneHallway(int x, int y) {
        for (int i = x - 1; i <= x + 1; i++) {
            for (int j = y - 1; j <= y + 1; j++) {
                if (!coordinates[i][j].getOccupiedRoom() && !coordinates[i][j].getOccupiedHall()) {
                    world[i][j] = WALL_TILE;
                    coordinates[i][j].setTile(WALL_TILE_STR);
                }
            }
        }
    }

    /** Makes the entrances of the rooms and returns a Pair that has the coordinates of the entrance */
    public static Pair<Integer, Integer> makeEntrances(Room r, String s, Hallway h) {
        int entrance1X;
        int entrance1Y;
        switch (s) {
            case "T" -> {
                entrance1X = randIntBound(0, r.getWidth() - 1) + r.getCord().getX();
                entrance1Y = r.getCord().getY() + r.getHeight();
                setHallway(entrance1X, entrance1Y, h);
                setHallway(entrance1X, entrance1Y + 1, h);
                entrance1Y = entrance1Y + 1;
            }
            case "B" -> {
                entrance1X = randIntBound(0, r.getWidth() - 1) + r.getCord().getX();
                entrance1Y = r.getCord().getY() - 1;
                setHallway(entrance1X, entrance1Y, h);
                setHallway(entrance1X, entrance1Y - 1, h);
                entrance1Y = entrance1Y - 1;
            }
            case "L" -> {
                entrance1X = r.getCord().getX() - 1;
                entrance1Y = randIntBound(0, r.getHeight() - 1) + r.getCord().getY();
                setHallway(entrance1X, entrance1Y, h);
                setHallway(entrance1X - 1, entrance1Y, h);
                entrance1X = entrance1X - 1;
            }
            default -> {
                entrance1X = r.getCord().getX() + r.getWidth();
                entrance1Y = randIntBound(0, r.getHeight() - 1) + r.getCord().getY();
                setHallway(entrance1X, entrance1Y, h);
                setHallway(entrance1X + 1, entrance1Y, h);
                entrance1X = entrance1X + 1;
            }
        }
        return new Pair<>(entrance1X, entrance1Y);
    }

    /** Given r1, it gives the closest room to r1 in terms of distance from the centers */
    public static Room findNeighborRoom(Room r1, ArrayList<Room> a) {
        if (!a.isEmpty()) {
            Room closest = a.get(0);
            for (Room room : a) {
                if (distanceRoom(r1, room) < distanceRoom(r1, closest)) {
                    closest = room;
                }
            }
            return closest;
        }
        return null;
    }

    /** Calculates the distance between the midpoints of a room */
    public static double distanceRoom(Room r1, Room r2) {
        Pair<Integer, Integer> r1Cord = r1.getMidpoint();
        Pair<Integer, Integer> r2Cord = r2.getMidpoint();
        return Point2D.distance(r1Cord.getFirst(), r1Cord.getSecond(), r2Cord.getFirst(), r2Cord.getSecond());
    }

    /** Provided l, u, randIntBound returns a random integer between l and u, inclusive */
    public static int randIntBound(int l, int u) {
        int add = rand.nextInt(u - l + 1);
        return l + add;
    }

    /** Changes the visual block for the Hallway into the Hallway tile and
     assigns the coordinate the hallway that is passed in. */
    public static void setHallway(int x, int y, Hallway h) {
        world[x][y] = HALLWAY_TILE;
        coordinates[x][y].setTile(HALLWAY_TILE_STR);
        coordinates[x][y].assignHallway(h);
        coordinates[x][y].changeOccupiedHall();
    }


    /** closestSides returns a pair of pairs representing the 4 possible closest sides */
    public static Pair<Pair<String, String>, Pair<String, String>> closestSides(Room r1, Room r2) {
        Pair<Integer, Integer> r1Cord = r1.getMidpoint();
        Pair<Integer, Integer> r2Cord = r2.getMidpoint();
        int xDiff = r1Cord.getFirst() - r2Cord.getFirst();
        int yDiff = r1Cord.getSecond() - r2Cord.getSecond();
        if (xDiff <= 0 && yDiff <= 0) {
            Pair<String, String> pair1 = new Pair("T", "R");
            Pair<String, String> pair2 = new Pair("B", "L");
            Pair<Pair<String, String>, Pair<String, String>> finalP = new Pair(pair1, pair2);
            return finalP;
        } else if (xDiff <= 0 && yDiff >= 0) {
            Pair<String, String> pair1 = new Pair("B", "R");
            Pair<String, String> pair2 = new Pair("T", "L");
            Pair<Pair<String, String>, Pair<String, String>> finalP = new Pair(pair1, pair2);
            return finalP;
        } else if (xDiff >= 0 && yDiff <= 0) {
            Pair<String, String> pair1 = new Pair("T", "L");
            Pair<String, String> pair2 = new Pair("B", "R");
            Pair<Pair<String, String>, Pair<String, String>> finalP = new Pair(pair1, pair2);
            return finalP;
        } else {
            Pair<String, String> pair1 = new Pair("B", "L");
            Pair<String, String> pair2 = new Pair("T", "R");
            Pair<Pair<String, String>, Pair<String, String>> finalP = new Pair(pair1, pair2);
            return finalP;
        }
    }

    /** setRoom sets the boundaries of the rooms and the rooms to know what places to not
     put future rooms.
     */
    public static void setRoom(int x, int y, int wid, int hei, Room r) {
        for (int i = x - 1; i < x + wid + 1; i++) {
            for (int j = y - 1; j < y + hei + 1; j++) {
                coordinates[i][j].changeOccupiedRoom();
                coordinates[i][j].assignRoom(r);
            }
        }
    }

    public static void setBoundaries(int x, int y, int wid, int hei) {
        for (int i = x - BOUNDARIES; i < x + wid + BOUNDARIES; i++) {
            for (int j = y - BOUNDARIES; j < y + hei + BOUNDARIES; j++) {
                coordinates[i][j].changeOccupiedBoundary();
            }
        }
    }

    /** Randomly picks coordinates that can act as places to start a new room */
    public static Pair<Integer, Integer> findBottomCoor(int wid, int hei) {
        while (true) {
            int randX = randIntBound(BOUNDARIES, WIDTH - MAX_ROOM_SIZE - BOUNDARIES);
            int randY = randIntBound(BOUNDARIES, HEIGHT - MAX_ROOM_SIZE - BOUNDARIES);
            boolean broken = false;
            outerloop:
            for (int i = randX; i < randX + wid + 1; i++){
                for (int j = randY; j < randY + hei + 1; j++) {
                    if (coordinates[i][j].getOccupiedRoom() || coordinates[i][j].getOccupiedBoundary()) {
                        broken = true;
                        break outerloop;
                    }
                }
            }
            if (!broken) {
                return new Pair<>(randX, randY);
            }
        }
    }

    /** Checks if the rooms share a common side in the x-direction and returns the sides
     they share if they do and null otherwise */
    public static Pair<Integer, Integer> ifShareSidesX(Room r1, Room r2) {
        int x1 = r1.getCord().getX();
        int x2 = r2.getCord().getX();
        if (x1 > x2 + r2.getWidth() || x1 + r2.getWidth() < x2) {
            return null;
        }
        else {
            return new Pair<>(Math.max(x1, x2), Math.min(x1 + r2.getWidth(), x2 + r2.getWidth()));
        }
    }

    /** Checks if the rooms share a common side in the y-direction and returns the sides
     they share if they do and null otherwise */
    public static Pair<Integer, Integer> ifShareSidesY(Room r1, Room r2) {
        int y1 = r1.getCord().getY();
        int y2 = r2.getCord().getY();
        if (y1 > y2 + r2.getHeight() || y1 + r2.getHeight() < y2) {
            return null;
        }
        else {
            return new Pair<>(Math.max(y1, y2), Math.min(y1 + r2.getHeight(), y2 + r2.getHeight()));
        }
    }

    /** Uses the closest sides to return random sides of the room to make as entrances */
    public static Pair<String, String> randomSide(Room r1, Room r2) {
        Pair<Pair<String, String>, Pair<String, String>> sides = closestSides(r1, r2);
        int randInt1 = randIntBound(0, 1);
        int randInt2 = randIntBound(0, 1);
        String chosenSide1;
        String chosenSide2;
        if (randInt1 == 0) {
            chosenSide1 = sides.getFirst().getFirst();
        } else {
            chosenSide1 = sides.getFirst().getSecond();
        }
        if (randInt2 == 0) {
            chosenSide2 = sides.getSecond().getFirst();
        } else {
            chosenSide2 = sides.getSecond().getSecond();
        }
        return new Pair<>(chosenSide1, chosenSide2);
    }

    /** Checks if the rooms shares a side and chooses those if they do, otherwise they choose random sides */
    public static Pair<String, String> chooseSide(Room r1, Room r2) {
        String chosenSide1;
        String chosenSide2;
        if (ifShareSidesX(r1, r2) != null) {
            int y1 = r1.getCord().getY();
            int y2 = r2.getCord().getY();
            if (y1 - y2 > 0) {
                chosenSide1 = "B";
                chosenSide2 = "T";
            } else {
                chosenSide1 = "T";
                chosenSide2 = "B";
            }
        } else if (ifShareSidesY(r1, r2) != null) {
            int x1 = r1.getCord().getX();
            int x2 = r2.getCord().getX();
            if (x1 - x2 > 0) {
                chosenSide1 = "L";
                chosenSide2 = "R";
            } else {
                chosenSide1 = "R";
                chosenSide2 = "L";
            }
        } else {
            Pair<String, String> ran = randomSide(r1, r2);
            chosenSide1 = ran.getFirst();
            chosenSide2 = ran.getSecond();
        }
        return new Pair<>(chosenSide1, chosenSide2);
    }


    public static void drawInitialFrame() {
        StdDraw.setCanvasSize(WIDTH * 16, HEIGHT * 16);
        StdDraw.setXscale(0, WIDTH);
        StdDraw.setYscale(0, HEIGHT);
        StdDraw.clear(StdDraw.BLACK);
        StdDraw.setPenColor(StdDraw.WHITE);
        Font font = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(font);
        StdDraw.text(WIDTH / 2, HEIGHT * 0.5, "New Game (N)");
        StdDraw.text(WIDTH / 2, HEIGHT * 0.46, "Load Game (L)");
        StdDraw.text(WIDTH / 2, HEIGHT * 0.42, "Change Avatar Name (C)");
        StdDraw.text(WIDTH / 2, HEIGHT * 0.38, "Quit (Q)");
        Font font2 = new Font("Monaco", Font.BOLD, 45);
        StdDraw.setFont(font2);
        StdDraw.text(WIDTH / 2, HEIGHT * 0.8, "CS61B: THE GAME");
    }

    public static void mouseHUD() {
        double mouseX = StdDraw.mouseX();
        double mouseY = StdDraw.mouseY();
        Font font = new Font("Monaco", Font.BOLD, 15);
        String s = coordinates[(int) mouseX][(int) mouseY].getTile();
        StdDraw.setPenColor(StdDraw.BLACK);
        StdDraw.filledRectangle(0, HEIGHT, 12, 3);
        StdDraw.setPenColor(StdDraw.WHITE);
        //middle string
        StdDraw.setFont(font);
        StdDraw.textLeft(2, HEIGHT - 2, s);
        StdDraw.show();
    }


    public static void main(String[] args) {
        Engine engine = new Engine();
        engine.interactWithKeyboard();
    }
}
