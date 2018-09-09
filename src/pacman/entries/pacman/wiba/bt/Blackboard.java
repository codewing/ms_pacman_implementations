package pacman.entries.pacman.wiba.bt;

import java.util.HashMap;

public class Blackboard {
    private HashMap<String, String> database;

    public Blackboard() {
        database = new HashMap<>();
    }

    public void set(String variable, String value) {
        database.put(variable, value);
    }

    public String get(String variable) {
        return database.get(variable);
    }
}
