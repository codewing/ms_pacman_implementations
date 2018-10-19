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

    public static SimulationResult simulateUntilNextJunction(MCTSParams params, Game gameState, Controller<EnumMap<Constants.GHOST,Constants.MOVE>> ghostController, Constants.MOVE direction) {
        SimulationResult result = simulateToNextJunctionOrLimit(params, gameState, ghostController, direction, Integer.MAX_VALUE);

        return result;
    }

    public static SimulationResult simulateToNextJunctionOrLimit(MCTSParams params, Game gameState, Controller<EnumMap<Constants.GHOST,Constants.MOVE>> ghostController, Constants.MOVE direction, int maxSteps) {
        SimulationResult result = new SimulationResult();

        Constants.MOVE currentPacmanDirection = direction;
        do {
            currentPacmanDirection = getPacmanMoveAlongPath(gameState, currentPacmanDirection);

            gameState.advanceGame(currentPacmanDirection, ghostController.getMove(gameState, System.currentTimeMillis() + params.ghostSimulationTimeMS));

            // update stats
            result.steps++;
            maxSteps--;
            if(gameState.wasPacManEaten()) {
                result.diedDuringSimulation = true;
                break;
            }
        } while(!gameState.isJunction(gameState.getPacmanCurrentNodeIndex()) && maxSteps > 0);

        result.gameState = gameState;

        return result;
    }

    public static ArrayList<Constants.MOVE> getPacmanMovesAtJunctionWithoutReverse(Game gameState) {
        ArrayList<Constants.MOVE> moves = getPacmanMovesWithoutNeutral(gameState);
        moves.remove(gameState.getPacmanLastMoveMade().opposite());

        return moves;
    }

    public static Constants.MOVE getPacmanMoveAlongPath(Game gameState, Constants.MOVE direction) {
        ArrayList<Constants.MOVE> moves = getPacmanMovesWithoutNeutral(gameState);
        if(moves.contains(direction)) return direction;
        moves.remove(gameState.getPacmanLastMoveMade().opposite());
        assert moves.size() == 1; // along a path there is only one possible way remaining

        return moves.get(0);
    }
}
