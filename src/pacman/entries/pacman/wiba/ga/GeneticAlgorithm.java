package pacman.entries.pacman.wiba.ga;

import pacman.controllers.Controller;
import pacman.controllers.examples.StarterGhosts;
import pacman.entries.pacman.wiba.WibaPacmanGA;
import pacman.entries.pacman.wiba.mcts.MCTSParams;
import pacman.entries.pacman.wiba.utils.Utils;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GeneticAlgorithm {

    private Random random;
    private ExecutorService gameExecutor;

    static int NUMBER_OF_GENERATIONS = 10;
    static int RUNS_PER_GENOME_FOR_AVG = 5;
    static int POPULATION_SIZE = 10;
    private ArrayList<Genome> population = new ArrayList<>();
    private Class<? extends Controller<EnumMap<pacman.game.Constants.GHOST, pacman.game.Constants.MOVE>>> ghostControllerClass = StarterGhosts.class;

    private Genome champion;

    public GeneticAlgorithm(int numberOfThreads) {
        this(numberOfThreads, "");
    }

    public GeneticAlgorithm(int numberOfThreads, String fileToLoadFrom) {
        random = new Random();
        gameExecutor = Executors.newFixedThreadPool(numberOfThreads);

        if(fileToLoadFrom.isEmpty()) {
            for (int i = 0; i < POPULATION_SIZE; i++) {
                Genome individual = new Genome();
                individual.randomizeChromosome();

                population.add(individual);
            }
        } else {
            population.addAll(GAStorage.loadGenomeCSV(fileToLoadFrom));
        }

    }

    public boolean shouldMutate(Genome individual) {
        if(random.nextInt(100) == 0) {
            return true;
        }
        return false;
    }

    public void mutatePopulation() {
        for (Genome individual : population) {
            if(shouldMutate(individual)) {
                individual.mutate();
            }
        }
    }

    public ArrayList<Genome> reproduce(Genome parent_one, Genome parent_two) {
        ArrayList<Genome> newOffspring = new ArrayList<>(2);

        int c0_maxPathLength, c1_maxPathLength;
        double c0_explorationCoefficient, c1_explorationCoefficient;
        int c0_minVisitCount, c1_minVisitCount;
        int c0_ghostSimulationCountMS, c1_ghostSimulationCountMS;

        // Uniform Crossover -> definitely not clean code :D
        if(random.nextInt(2) == 0) {
            c0_maxPathLength = parent_one.getChromosome().MAX_PATH_LENGTH;
            c1_maxPathLength = parent_two.getChromosome().MAX_PATH_LENGTH;
        } else {
            c1_maxPathLength = parent_one.getChromosome().MAX_PATH_LENGTH;
            c0_maxPathLength = parent_two.getChromosome().MAX_PATH_LENGTH;
        }

        if(random.nextInt(2) == 0) {
            c0_explorationCoefficient = parent_one.getChromosome().explorationCoefficient;
            c1_explorationCoefficient = parent_two.getChromosome().explorationCoefficient;
        } else {
            c1_explorationCoefficient = parent_one.getChromosome().explorationCoefficient;
            c0_explorationCoefficient = parent_two.getChromosome().explorationCoefficient;
        }

        if(random.nextInt(2) == 0) {
            c0_minVisitCount = parent_one.getChromosome().MIN_VISIT_COUNT;
            c1_minVisitCount = parent_two.getChromosome().MIN_VISIT_COUNT;
        } else {
            c1_minVisitCount = parent_one.getChromosome().MIN_VISIT_COUNT;
            c0_minVisitCount = parent_two.getChromosome().MIN_VISIT_COUNT;
        }

        if(random.nextInt(2) == 0) {
            c0_ghostSimulationCountMS = parent_one.getChromosome().ghostSimulationTimeMS;
            c1_ghostSimulationCountMS = parent_two.getChromosome().ghostSimulationTimeMS;
        } else {
            c1_ghostSimulationCountMS = parent_one.getChromosome().ghostSimulationTimeMS;
            c0_ghostSimulationCountMS = parent_two.getChromosome().ghostSimulationTimeMS;
        }

        MCTSParams offspring0Chromosome = new MCTSParams(c0_maxPathLength, c0_explorationCoefficient, c0_minVisitCount, c0_ghostSimulationCountMS);
        MCTSParams offspring1Chromosome = new MCTSParams(c1_maxPathLength, c1_explorationCoefficient, c1_minVisitCount, c1_ghostSimulationCountMS);

        newOffspring.add(new Genome(offspring0Chromosome));
        newOffspring.add(new Genome(offspring1Chromosome));

        return newOffspring;
    }

    public void reproducePopulation() {
        ArrayList<Genome> offsprings = selection();
        replacement(offsprings);
    }

    public ArrayList<Genome> selection() {
        ArrayList<Genome> offspringList = new ArrayList<>();

        Collections.sort(population);

        //implement selection and fill the offsprings
        for(int i = 0; i < population.size()/2; i += 2) {
            offspringList.addAll(reproduce(population.get(i), population.get(i+1)));
        }

        return offspringList;
    }

    public void replacement(ArrayList<Genome> offspringList) {
        Collections.sort(population);

        while(population.size() + offspringList.size() > POPULATION_SIZE) {
            population.remove(population.size()-1);
        }

        population.addAll(offspringList);
    }

    public void startEvolution() {
        int current_generation = 0;

        GAStorage.saveGenomeCSV("generation_-1", population);

        while (evaluatePopulation(current_generation)) {
            System.out.println("////////////////////////");
            System.out.println("//// Generation: " + current_generation + " ////");
            System.out.println("////////////////////////");

            GAStorage.saveGenomeCSV("generation_"+current_generation, population);

            generateStatistics(true);
            reproducePopulation();
            mutatePopulation();
            current_generation++;
        }

        System.out.println("\nENDING GENERATION: " + current_generation + "\n");
        System.out.println("Statistics:");
        generateStatistics(true);
    }

    public boolean evaluatePopulation(int currentIteration) {

        ArrayList<Callable<Void>> evaluationTasks = new ArrayList<>();

        for (Genome individual : population) {
            // add all evaluation tasks
            evaluationTasks.add(new GenomeEvaluator(individual, new WibaPacmanGA(individual.getChromosome()), ghostControllerClass, RUNS_PER_GENOME_FOR_AVG));
        }

        try {
            // execute and wait for them to finish
            gameExecutor.invokeAll(evaluationTasks);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return currentIteration < NUMBER_OF_GENERATIONS;
    }



    public void generateStatistics(boolean print) {
        float avgFitness = 0.f;
        float minFitness = Float.POSITIVE_INFINITY;
        float maxFitness = Float.NEGATIVE_INFINITY;

        String bestIndividual = "";
        String worstIndividual = "";
        for (int i = 0; i < population.size(); i++) {
            float currFitness = population.get(i).getFitness();
            avgFitness += currFitness;
            if (currFitness < minFitness) {
                minFitness = currFitness;
                worstIndividual = population.get(i).toString();
            }
            if (currFitness > maxFitness) {
                maxFitness = currFitness;
                champion = population.get(i);
                bestIndividual = population.get(i).toString();
            }
        }

        avgFitness /= population.size();

        if (print) {
            System.out.println("Avg. fitness: " + avgFitness + "; Worst fitness: " + minFitness + "; Best fitness: " + maxFitness);
            System.out.println("Best individual: " + bestIndividual + "; Worst individual: " + worstIndividual);
        }
    }

    public Genome getChampion() {
        return champion;
    }
}
