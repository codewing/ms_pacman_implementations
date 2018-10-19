package pacman.entries.pacman.wiba.ga;

import pacman.controllers.Controller;
import pacman.controllers.examples.StarterGhosts;
import pacman.entries.pacman.wiba.mcts.MCTSParams;
import pacman.game.Constants;
import pacman.game.Game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Random;

import static pacman.game.Constants.DELAY;

public class GeneticAlgorithm {

    private Random random;

    static int EVALUATION_RUNS = 5;
    static int POPULATION_SIZE = 10;
    private ArrayList<Genome> population = new ArrayList<>();
    private Controller<EnumMap<Constants.GHOST, Constants.MOVE>> ghosts = new StarterGhosts();

    private Genome champion;

    public GeneticAlgorithm() {
        random = new Random();

        for (int i = 0; i < POPULATION_SIZE; i++) {
            Genome individual = new Genome();
            individual.randomizeChromosome();

            population.add(individual);
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

        while (evaluatePopulation(current_generation)) {
            System.out.println("////////////////////////");
            System.out.println("//// Generation: " + current_generation + " ////");
            System.out.println("////////////////////////");

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
        boolean keepIterating = true;
        for (Genome individual : population) {
            // evaluate fitness of an individual
            float fitness = evaluateGenome(new WibaPacmanGA(individual.getChromosome()), ghosts, EVALUATION_RUNS);
            individual.setFitness(fitness);
        }

        return currentIteration < 10;
    }

    private float evaluateGenome(Controller<Constants.MOVE> pacManController, Controller<EnumMap<Constants.GHOST,Constants.MOVE>> ghostController, int trials)
    {
        float totalScore = 0;

        Random rnd=new Random(0);
        Game game;

        for(int i = 0; i<trials; i++)
        {
            game = new Game(rnd.nextLong());

            while(!game.gameOver()) {
                game.advanceGame(pacManController.getMove(game.copy(),System.currentTimeMillis()+DELAY),
                        ghostController.getMove(game.copy(),System.currentTimeMillis()+DELAY));
            }

            totalScore += game.getScore();
        }

        return totalScore/trials;
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
