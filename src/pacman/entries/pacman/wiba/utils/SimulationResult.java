package pacman.entries.pacman.wiba.utils;

import pacman.game.Game;

public class SimulationResult {
    public int steps = 0;
    public Game gameState;

    public boolean diedDuringSimulation = false;
    public boolean levelComplete = false;
    public boolean powerPillEatenButActive = false;
}
