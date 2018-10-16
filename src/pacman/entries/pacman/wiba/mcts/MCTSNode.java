package pacman.entries.pacman.wiba.mcts;

import pacman.entries.pacman.wiba.utils.Utils;
import pacman.game.Constants;
import pacman.game.Game;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MCTSNode {

    final Game gameState;
    final int pathLengthInSteps;

    MCTSNode parent = null;
    Constants.MOVE parentAction = null;
    List<MCTSNode> children = new ArrayList<>();

    float reward = 0;
    int timesVisited = 0;


    MCTSNode(Game gameState, int pathLengthInSteps){
        this.gameState = gameState;
        this.pathLengthInSteps = pathLengthInSteps;
    }

    double getReward() {
        double childrenMax = children.stream().map(node -> node.reward).max(Float::compareTo).orElse(0f);

        return Math.max(childrenMax, reward);
    }

    double getUCTValue() {
        double exploitation = reward / timesVisited;
        double exploration = MCTSParams.explorationCoefficient * Math.sqrt( Math.log(parent.timesVisited) / timesVisited);

        return exploitation + exploration;
    }

    public boolean isFullyExpanded() {
        ArrayList<Constants.MOVE> moves = getPacmanMovesNotExpanded();

        return moves.isEmpty();
    }

    public ArrayList<Constants.MOVE> getPacmanMovesWithoutReverse() {
        ArrayList<Constants.MOVE> moves = Utils.getPacmanMovesWithoutNeutral(gameState);

        if(parent != null) {
            moves.remove(parentAction);
        }

        return moves;
    }

    public ArrayList<Constants.MOVE> getPacmanMovesNotExpanded() {
        // check if enough simulation steps would be made
        if(pathLengthInSteps > 0.8f * MCTSParams.MAX_PATH_LENGTH) {
            return new ArrayList<>();
        } else {
            ArrayList<Constants.MOVE> moves = getPacmanMovesWithoutReverse();

            // remove explored child moves
            moves.removeAll(children.parallelStream().map(child -> child.parentAction).collect(Collectors.toList()));

            return moves;
        }
    }

    public MCTSNode getBestChild() {
        MCTSNode bestChild = null;
        double bestUCTValue = Float.MIN_VALUE;

        for(MCTSNode child : children) {
            double childValue = child.getUCTValue();
            if(childValue > bestUCTValue) {
                bestChild = child;
                bestUCTValue = childValue;
            }
        }

        return bestChild;
    }

    public boolean isGameOver() {
        return gameState.wasPacManEaten() || gameState.getActivePillsIndices().length == 0;
    }

    public boolean isPacmanAtJunction() {
        return gameState.isJunction(gameState.getPacmanCurrentNodeIndex());
    }
}
