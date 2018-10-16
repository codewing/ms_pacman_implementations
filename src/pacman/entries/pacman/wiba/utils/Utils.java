package pacman.entries.pacman.wiba.utils;

import pacman.controllers.Controller;
import pacman.entries.pacman.wiba.mcts.MCTSParams;
import pacman.game.Constants;
import pacman.game.Game;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumMap;

public abstract class Utils {

    public static String getFormattedTime(long time) {
        Date date = new Date(time);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");

        return sdf.format(date);
    }

    public static ArrayList<Constants.MOVE> getPacmanMovesWithoutNeutral(Game gameState) {
        Constants.MOVE[] possibleMoves = gameState.getPossibleMoves(gameState.getPacmanCurrentNodeIndex());

        ArrayList<Constants.MOVE> pacmanMoves = new ArrayList<>(Arrays.asList(possibleMoves));
        pacmanMoves.remove(Constants.MOVE.NEUTRAL);

        return pacmanMoves;
    }

    public static SimulationResult simulateUntilNextJunction(Game gameState, Controller<EnumMap<Constants.GHOST,Constants.MOVE>> ghostController, Constants.MOVE direction) {
        SimulationResult result = simulateToNextJunctionOrLimit(gameState, ghostController, direction, Integer.MAX_VALUE);

        return result;
    }

    public static SimulationResult simulateToNextJunctionOrLimit(Game gameState, Controller<EnumMap<Constants.GHOST,Constants.MOVE>> ghostController, Constants.MOVE direction, int maxSteps) {
        SimulationResult result = new SimulationResult();

        do {
            Constants.MOVE pacmanMove = getPacmanMoveAlongPath(gameState, direction);
            direction = pacmanMove;

            gameState.advanceGame(pacmanMove, ghostController.getMove(gameState, System.currentTimeMillis() + MCTSParams.ghostSimulationTimeMS));

            // update stats
            result.steps++;
            maxSteps--;
            if(gameState.wasPacManEaten()) {
                result.diedDuringSimulation = true;
                break;
            }

        } while(gameState.isJunction(gameState.getPacmanCurrentNodeIndex()) && maxSteps > 0);

        result.gameState = gameState;

        return result;
    }

    public static Constants.MOVE getPacmanMoveAtJunctionWithoutReverse(Game gameState, Constants.MOVE direction) {
        ArrayList<Constants.MOVE> moves = getPacmanMovesWithoutNeutral(gameState);

        moves.remove(direction.opposite());

        return moves.get(0);
    }

    public static Constants.MOVE getPacmanMoveAlongPath(Game gameState, Constants.MOVE direction) {
        ArrayList<Constants.MOVE> moves = getPacmanMovesWithoutNeutral(gameState);

        if(moves.contains(direction)) return direction;

        moves.remove(direction.opposite());

        assert moves.size() == 1; // along a path there is only one possible way remaining

        return moves.get(0);
    }
}
