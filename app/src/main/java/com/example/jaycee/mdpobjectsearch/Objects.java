package com.example.jaycee.mdpobjectsearch;

public class Objects
{
    public enum Observation
    {
        UNDEFINED(-1, "", "UNDEFINED"),
        O_NOTHING (0, "", "Nothing"),
        T_COMPUTER_MONITOR (1, "monitor.txt", "Monitor"),
        T_COMPUTER_KEYBOARD (2, "keyboard.txt", "Keyboard"),
        T_COMPUTER_MOUSE (3, "mouse.txt", "Mouse"),
        T_DESK (4, "desk.txt", "Desk"),
        T_LAPTOP (5, "laptop.txt", "Laptop"),
        T_MUG (6, "mug.txt", "Mug"),
        T_WINDOW (7, "window.txt", "Window"),
        T_LAMP (8, "lamp.txt", "Lamp"),
        T_BACKPACK (9, "backpack.txt", "Backpack"),
        T_CHAIR (10, "chair.txt", "Chair"),
        T_COUCH (11, "couch.txt", "Couch"),
        T_PLANT (12, "plant.txt", "Plant"),
        T_TELEPHONE (13, "telephone.txt", "Telephone"),
        T_WHITEBOARD (14, "whiteboard.txt", "Whiteboard"),
        T_DOOR (15, "door.txt", "Door");

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

    public static Objects.Observation getObservation(int code)
    {
        switch(code)
        {
            case 0: return Observation.O_NOTHING;
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
            default: return Observation.UNDEFINED;
        }
    }
}
