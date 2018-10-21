package pacman.entries.pacman.wiba.ga;

import pacman.entries.pacman.wiba.mcts.MCTSParams;

import java.util.Random;

public class Genome implements Comparable<Genome> {

    // sensible value range
    private static int MAX_MAX_PATH_LENGTH = 980;
    private static double MAX_EXPLORATION_COEFFICIENT = 1.9d;
    private static int MAX_MIN_VISIT_COUNT = 499;
    private static int MAX_GHOST_SIMULATION_COUNT_MS = 9;

    public static int CHROMOSOME_COUNT = 4;

    private MCTSParams chromosome;
    private float fitness;
    private Random random;

    public Genome() {
        fitness = 0.0f;
        random = new Random();
    }

    public Genome(MCTSParams chromosome) {
        this();
        this.chromosome = chromosome;
    }

    public void randomizeChromosome() {
        int maxPathLength = 20 + random.nextInt(MAX_MAX_PATH_LENGTH);
        double explorationCoefficient = 0.1d + random.nextDouble() * MAX_EXPLORATION_COEFFICIENT;
        int minVisitCount = 1 + random.nextInt(MAX_MIN_VISIT_COUNT);
        int ghostSimulationCountMS = 1 + random.nextInt(MAX_GHOST_SIMULATION_COUNT_MS);

        chromosome = new MCTSParams(maxPathLength, explorationCoefficient, minVisitCount, ghostSimulationCountMS);
    }

    public void mutate() {
        int attributeToMutate = random.nextInt(CHROMOSOME_COUNT);

        int maxPathLength = 0;
        double explorationCoefficient = 0;
        int minVisitCount = 0;
        int ghostSimulationCountMS = 0;

        switch (attributeToMutate) {
            case 0:{
                maxPathLength = random.nextInt();
            }break;
            case 1:{
                explorationCoefficient = random.nextDouble() * Double.MAX_VALUE;
            }break;
            case 2:{
                minVisitCount = random.nextInt();
            }break;
            case 3:{
                ghostSimulationCountMS = random.nextInt();
            }break;
        }
        chromosome = modifyMCTSParams(chromosome, maxPathLength, explorationCoefficient, minVisitCount, ghostSimulationCountMS);
    }

    public float getFitness() {
        return fitness;
    }

    public void setFitness(float fitness) {
        this.fitness = fitness;
    }

    @Override
    public int compareTo(Genome other) {
        return (int) ((other.getFitness() - this.getFitness()));
    }

    public static MCTSParams modifyMCTSParams(MCTSParams params, int diffPathLength, double diffExplCoeff, int diffMinVisCount, int diffGhostSimTime) {
        return new MCTSParams(
                (params.MAX_PATH_LENGTH + diffPathLength) % MAX_MAX_PATH_LENGTH,
                (params.explorationCoefficient + diffExplCoeff) % MAX_EXPLORATION_COEFFICIENT,
                (params.MIN_VISIT_COUNT + diffMinVisCount) % MAX_MIN_VISIT_COUNT,
                (params.ghostSimulationTimeMS + diffGhostSimTime) % MAX_GHOST_SIMULATION_COUNT_MS
        );
    }

    public MCTSParams getChromosome() {
        return chromosome;
    }

    @Override
    public String toString() {
        return  "Fitness:" + fitness + " / " +
                "MPL:" + chromosome.MAX_PATH_LENGTH + "/" +
                "EC:" + chromosome.explorationCoefficient + "/" +
                "MVC:" + chromosome.MIN_VISIT_COUNT + "/" +
                 "GST:" + chromosome.ghostSimulationTimeMS;
    }
}
