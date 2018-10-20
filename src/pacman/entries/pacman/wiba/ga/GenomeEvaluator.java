package pacman.entries.pacman.wiba.ga;

import pacman.controllers.Controller;
import pacman.game.Constants;
import pacman.game.Game;

import java.lang.reflect.Constructor;
import java.util.EnumMap;
import java.util.Random;
import java.util.concurrent.Callable;

import static pacman.game.Constants.DELAY;

public class GenomeEvaluator implements Callable<Void> {

    private Genome genome;
    private Controller<Constants.MOVE> pacManController;
    private Class<? extends Controller<EnumMap<pacman.game.Constants.GHOST, pacman.game.Constants.MOVE>>> ghostControllerClass;
    private int trials;

    public GenomeEvaluator(Genome genome, Controller<Constants.MOVE> pacManController, Class<? extends Controller<EnumMap<Constants.GHOST, Constants.MOVE>>> ghostControllerClass, int trials) {
        this.genome = genome;
        this.pacManController = pacManController;
        this.ghostControllerClass = ghostControllerClass;
        this.trials = trials;
    }

    @Override
    public Void call() {
        float fitness = evaluateGenome();
        genome.setFitness(fitness);

        return null;
    }

    private float evaluateGenome()
    {
        Controller<EnumMap<Constants.GHOST,Constants.MOVE>> ghostController = instantiateGhostController();

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

    private Controller<EnumMap<Constants.GHOST,Constants.MOVE>> instantiateGhostController() {
        Controller<EnumMap<Constants.GHOST,Constants.MOVE>> controller = null;
        try {
            Constructor<? extends Controller<EnumMap<Constants.GHOST,Constants.MOVE>>> ctor = ghostControllerClass.getConstructor();
            controller = ctor.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return controller;
    }
}
