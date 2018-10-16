package pacman.entries.pacman.wiba.mcts;

public abstract class MCTSParams {
    public static final int MAX_PATH_LENGTH = 120;
    public static final double explorationCoefficient = 1.0/Math.sqrt(2);
    public static final int MIN_VISIT_COUNT = 5;

    public static final int ghostSimulationTimeMS = 5;
}
