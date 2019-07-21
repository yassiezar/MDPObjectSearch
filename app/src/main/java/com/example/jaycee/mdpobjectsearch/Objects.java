package com.example.jaycee.mdpobjectsearch;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Objects
{
    private static final Random rand = new Random();
    private static final List<Observation> OBSERVATIONS = Collections.unmodifiableList(Arrays.asList(Observation.values()));
    private static final int SIZE = OBSERVATIONS.size();

    public enum Observation
    {
        UNDEFINED(-1, "", "UNDEFINED"),
        O_NOTHING (0, "", "Nothing"),
        T_COMPUTER_MONITOR (7, "monitor.txt", "Monitor"),
        T_COMPUTER_KEYBOARD (14, "keyboard.txt", "Keyboard"),
        T_COMPUTER_MOUSE (19, "mouse.txt", "Mouse"),
        T_DESK (24, "desk.txt", "Desk"),
//        T_LAPTOP (5, "laptop.txt", "Laptop"),
//        T_MUG (6, "mug.txt", "Mug"),
        T_WINDOW (27, "window.txt", "Window"),
//        T_LAMP (8, "lamp.txt", "Lamp"),
//        T_BACKPACK (9, "backpack.txt", "Backpack"),
        T_CHAIR (10, "chair.txt", "Chair"),
//        T_COUCH (11, "couch.txt", "Couch"),
        T_PLANT (28, "plant.txt", "Plant"),
//        T_TELEPHONE (13, "telephone.txt", "Telephone"),
        T_WHITEBOARD (21, "whiteboard.txt", "Whiteboard"),
        T_DOOR (25, "door.txt", "Door");

        private final int obsCode;
        private final String fileName;
        private final String friendlyName;

        Observation(int obsCode, String fileName, String friendlyName)
        {
            this.obsCode = obsCode;
            this.fileName = fileName;
            this.friendlyName = friendlyName;
        }

        public int getCode() { return this.obsCode; }
        public String getFileName() { return this.fileName; }
        public String getFriendlyName() { return this.friendlyName; }
    }

    public static int getRandomObservation(int code)
    {
        int index;
        do
        {
            index = rand.nextInt(SIZE);
        } while(index == code);
        return OBSERVATIONS.get(index).getCode();
    }

    public static Objects.Observation getObservation(int code)
    {
        // Updated for markers
        switch(code)
        {
/*            case 0: return Observation.O_NOTHING;
            case 1: return Observation.T_COMPUTER_MONITOR;
            case 2: return Observation.T_COMPUTER_KEYBOARD;
            case 3: return Observation.T_COMPUTER_MOUSE;
            case 4: return Observation.T_DESK;
            case 5: return Observation.T_LAPTOP;
            case 6: return Observation.T_MUG;
            case 7: return Observation.T_WINDOW;
            case 8: return Observation.T_LAMP;
            case 9: return Observation.T_BACKPACK;
            case 10: return Observation.T_CHAIR;
            case 11: return Observation.T_COUCH;
            case 12: return Observation.T_PLANT;
            case 13: return Observation.T_TELEPHONE;
            case 14: return Observation.T_WHITEBOARD;
            case 15: return Observation.T_DOOR;
            default: return Observation.UNDEFINED;*/

            case 0: return Observation.O_NOTHING;
            case 7: return Observation.T_COMPUTER_MONITOR;
            case 14: return Observation.T_COMPUTER_KEYBOARD;
            case 19: return Observation.T_COMPUTER_MOUSE;
            case 21: return Observation.T_WHITEBOARD;
            case 24: return Observation.T_DESK;
            case 25: return Observation.T_DOOR;
            case 27: return Observation.T_WINDOW;
            case 28: return Observation.T_PLANT;
/*            case 0: return Observation.O_NOTHING;
            case 7: return Observation.T_COMPUTER_MONITOR;
            case 14: return Observation.T_COMPUTER_KEYBOARD;
            case 19: return Observation.T_COMPUTER_MOUSE;
            case 20: return Observation.T_DOOR;
            case 21: return Observation.T_WHITEBOARD;
            case 24: return Observation.T_DESK;
            case 27: return Observation.T_WINDOW;
            case 28: return Observation.T_PLANT;*/
            default: return Observation.O_NOTHING;
        }
    }
}
