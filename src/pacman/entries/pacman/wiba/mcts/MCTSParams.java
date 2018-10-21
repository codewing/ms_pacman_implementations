package pacman.entries.pacman.wiba.mcts;

public class MCTSParams {
    public final int MAX_PATH_LENGTH;
    public final double explorationCoefficient;
    public final int MIN_VISIT_COUNT;

    public final int ghostSimulationTimeMS;

    public MCTSParams() {
        MAX_PATH_LENGTH = 140;
        explorationCoefficient = 1.0/Math.sqrt(2);
        MIN_VISIT_COUNT = 20;
        ghostSimulationTimeMS = 8;
    }

    public MCTSParams(int MAX_PATH_LENGTH, double explorationCoefficient, int MIN_VISIT_COUNT, int ghostSimulationTimeMS) {
        this.MAX_PATH_LENGTH = MAX_PATH_LENGTH;
        this.explorationCoefficient = explorationCoefficient;
        this.MIN_VISIT_COUNT = MIN_VISIT_COUNT;
        this.ghostSimulationTimeMS = ghostSimulationTimeMS;
    }
}
