package pacman.entries.pacman.wiba.mcts;

import pacman.controllers.examples.StarterGhosts;
import pacman.entries.pacman.wiba.utils.SimulationResult;
import pacman.entries.pacman.wiba.utils.Utils;
import pacman.game.Constants;
import pacman.game.Game;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class SimpleMCTS {

    private final MCTSNode rootNode;

    private final Random random = new Random();
    private final StarterGhosts ghostsController = new StarterGhosts();

    private final int numberOfActivePillsStart;

    private final long timeDue;

    public SimpleMCTS(Game gameState, long timeDue) {
        this.rootNode = new MCTSNode(gameState.copy(), 0);
        this.numberOfActivePillsStart = gameState.getNumberOfActivePills();
        this.timeDue = timeDue;
    }

    public Constants.MOVE runMCTS() {
        long deltaTimeNS = 0;
        long lastNS = System.nanoTime();

        while (!Terminate(deltaTimeNS)) {

            MCTSNode selectedNode = TreePolicy(rootNode);
            System.err.println("Selected Node path: " + selectedNode.path());

            float reward = SimulateGame(selectedNode);
            Backpropagate(selectedNode, reward);

            // Timing stuff
            long currentNS = System.nanoTime();
            deltaTimeNS = currentNS - lastNS;
            lastNS = currentNS;
        }

        Optional<MCTSNode> bestNode = rootNode.children.stream().max(Comparator.comparingDouble(n -> n.getReward()));
        if(bestNode.isPresent()) {
            return bestNode.get().parentAction;
        }

        return null;
    }

    /**
     * Check if the algorithm is to be terminated
     * @param lastDeltaNS the amount of time the last iteration took
     * @return
     */
    private boolean Terminate(long lastDeltaNS) {
        long lastDeltaMillis = TimeUnit.MILLISECONDS.convert(lastDeltaNS, TimeUnit.NANOSECONDS);
        long returnTimeMS = 2; // approx. time required to return to the getMove method
        if ( System.currentTimeMillis() + lastDeltaMillis + returnTimeMS > timeDue) return true;
        return false;
    }

    MCTSNode TreePolicy(MCTSNode currentNode) {
        System.err.println("called with: " + currentNode.path());
        if(currentNode.isGameOver()) {
            System.err.println("gameover");
            return currentNode.parent != null ? currentNode.parent : currentNode;
        }

        if(!currentNode.isFullyExpanded()) {
            System.err.println("expanded");
            return expandNode(currentNode);
        }

        if(currentNode.children.isEmpty()) {
            System.err.println("empty");
            return currentNode; // simulation depth reached (fully expanded + no children)
        }

        // randomize selection if visit count of one child < min visit count
        boolean allChildsVisitsAboveMinVisitCount =
                currentNode.children.parallelStream()
                    .map(c -> c.timesVisited)
                    .min(Integer::compareTo).get() > MCTSParams.MIN_VISIT_COUNT;

        if(allChildsVisitsAboveMinVisitCount) {
            System.err.println("childbest");
            return TreePolicy(currentNode.getBestChild());
        } else {
            System.err.println("childrnd");
            return TreePolicy(currentNode.children.get(random.nextInt(currentNode.children.size())));
        }
    }

    MCTSNode expandNode(MCTSNode parentNode) {
        ArrayList<Constants.MOVE> pacmanMoves = parentNode.getPacmanMovesNotExpanded();
        assert !pacmanMoves.isEmpty();
        Constants.MOVE pacmanMove = pacmanMoves.get(random.nextInt(pacmanMoves.size()));

        SimulationResult result = Utils.simulateUntilNextJunction(parentNode.gameState.copy(), ghostsController, pacmanMove);

        MCTSNode child = new MCTSNode(result.gameState, parentNode.pathLengthInSteps + result.steps);

        child.parentAction = pacmanMove;
        child.parent = parentNode;

        parentNode.children.add(child);

        if(result.diedDuringSimulation) {
            // make sure this node doesn't get simulated but simulate the parent
            child.reward = -1;
            child.timesVisited = 1;

            return parentNode;
        }

        return child;
    }

    private float SimulateGame(MCTSNode selectedNode) {

        Game simulationGameState = selectedNode.gameState.copy();
        int remainingSteps = MCTSParams.MAX_PATH_LENGTH - selectedNode.pathLengthInSteps;
        SimulationResult lastSimulationResult;

        while(remainingSteps > 0) {
            ArrayList<Constants.MOVE> availableMoves = Utils.getPacmanMovesAtJunctionWithoutReverse(simulationGameState);
            Constants.MOVE pacmanMove = availableMoves.get(random.nextInt(availableMoves.size()));

            lastSimulationResult = Utils.simulateToNextJunctionOrLimit(simulationGameState, ghostsController, pacmanMove, remainingSteps);

            remainingSteps -= lastSimulationResult.steps;
            if(lastSimulationResult.diedDuringSimulation) {
                return 0;
            }
        }

        return 1.0f - ( simulationGameState.getActivePillsIndices().length/((float)numberOfActivePillsStart));
    }

    private void Backpropagate(MCTSNode selectedNode, float reward) {
        selectedNode.reward = Math.max(reward, (float)selectedNode.getReward());

        while (selectedNode != null) {
            selectedNode.timesVisited++;
            selectedNode = selectedNode.parent;
        }
    }
}
