package pacman.entries.pacman.wiba.mcts;

import pacman.controllers.examples.StarterGhosts;
import pacman.entries.pacman.wiba.utils.Utils;
import pacman.game.Constants;
import pacman.game.Game;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class UCT {
    private Random random = new Random();
    private StarterGhosts ghostsController = new StarterGhosts();

    private MCTSNode rootNode; // place where we start
    private MCTSNode currentNode; // currently processing this node

    private final float explorationCoefficient = (float) (1.0/Math.sqrt(2));

    int maxPathLength = 40;

    /*
     * Computational limit
     */
    private final long timeDue;

    /**
     * Constructor
     * get the current game
     */
    public UCT(Game gameState, long timeDue){
        this.rootNode = new MCTSNode(gameState.copy(), 0);
        this.timeDue = timeDue;

        System.out.println("Started MCTS with UCT at " + Utils.getFormattedTime(System.currentTimeMillis())
                + " and it is allowed to run until " + Utils.getFormattedTime(timeDue));
    }

    /**
     * run the UCT search and find the optimal action for the root node state
     * @return best move for the current simulation
     */
    public Constants.MOVE runUCT() {
        /*
         * Apply UCT search inside computational budget limit (default=100 iterations)
         */
        long deltaTimeNS = 0;
        long lastNS = System.nanoTime();

        while(!Terminate(deltaTimeNS)){
            TreePolicy();
            float reward = DefaultPolicy();
            Backpropagate(reward);

            long currentNS = System.nanoTime();
            deltaTimeNS = currentNS - lastNS;
            lastNS = currentNS;
        }

        /*
         * Get the action that directs to the best node
         */
        currentNode = rootNode;
        //rootNode is the one we are working with
        //and we apply the exploitation of it to find the child with the highest average reward
        Optional<MCTSNode> bestNode = currentNode.children.stream().max(Comparator.comparingDouble(n -> n.timesVisited));
        if(bestNode.isPresent()) {
            return bestNode.get().parentAction;
        }

        return null;
    }

    /**
     * Expand the nonterminal nodes with one available child.
     * Chose a node to expand with BestChild(explorationCoefficient) method
     */
    private void TreePolicy() {
        currentNode = rootNode;

        while(!TerminalState(currentNode.gameState)) {
            if(!currentNode.isFullyExpanded()) {
                Expand();
                return;
            } else {
                if(currentNode.children.isEmpty()) { return; } // simulation depth reached (fully expaneded + no children)

                // randomize selection if visit count of one child < min visit count
                if(currentNode.children.parallelStream()
                        .map(c -> c.timesVisited)
                        .min(Integer::compareTo).get() > MCTSParams.MIN_VISIT_COUNT) {
                    currentNode = currentNode.children.get(random.nextInt(currentNode.children.size()));
                } else {
                    currentNode = currentNode.getBestChild();
                }
            }
        }
    }

    /**
     * Simulation of the game. Choose random actions up until the game is over (goal reached or dead)
     * @return reward (1 for win, 0 for loss)
     */
    private float DefaultPolicy() {
        Game st = currentNode.gameState.copy();
        Constants.MOVE pacmanAction = Constants.MOVE.NEUTRAL;

        int maxTotalSteps = maxPathLength - currentNode.pathLengthInSteps;
        int actualSteps = 0;
        while(actualSteps < maxTotalSteps && !TerminalState(st)){
            // let pacman decide the direction at every junction
            int pacmanIndex = st.getPacmanCurrentNodeIndex();
            if(st.isJunction(pacmanIndex)) {
                pacmanAction = RandomPossibleAction(st);
            } else {
                // if the pacman would run into a wall because its a winding path
                ArrayList<Constants.MOVE> possibleMoves = new ArrayList<>(Arrays.asList(st.getPossibleMoves(pacmanIndex)));
                if(!possibleMoves.contains(pacmanAction)) {
                    possibleMoves.remove(pacmanAction.opposite());
                    pacmanAction = possibleMoves.get(0);
                }
            }
            simulateOneStep(st, pacmanAction);

            if(st.wasPacManEaten()) break;

            actualSteps++;
        }
        float pillsPercentage = 1 - st.getActivePillsIndices().length / (float)(st.getPillIndices().length);
        float reward = st.wasPacManEaten() ? 0 : pillsPercentage;

        return reward;
    }

    /**
     * Takes a game state and simulates moves following the path until a junction is reached
     * @param gameState
     */
    int simulateUntilNextJunction(Game gameState) {
        Constants.MOVE pacmanAction = Constants.MOVE.NEUTRAL;
        int steps = 0;

        while(!isPacmanAtJunction(gameState) && !gameState.wasPacManEaten()) {
            int pacmanIndex = gameState.getPacmanCurrentNodeIndex();

            ArrayList<Constants.MOVE> possibleMoves = getAvailableMoves(gameState, pacmanIndex);
            if(!possibleMoves.contains(pacmanAction)) {
                possibleMoves.remove(pacmanAction.opposite());
                pacmanAction = possibleMoves.get(0);
            }

            simulateOneStep(gameState, pacmanAction);
            steps++;
        }

        return gameState.wasPacManEaten() ? -1 : steps;
    }

    void simulateOneStep(Game gameState, Constants.MOVE pacmanAction) {
        EnumMap<pacman.game.Constants.GHOST, pacman.game.Constants.MOVE> ghostMoves = ghostsController.getMove(gameState, System.currentTimeMillis() + 5);
        gameState.advanceGame(pacmanAction, ghostMoves);
    }

    boolean isPacmanAtJunction(Game gameState) {
        return gameState.isJunction(gameState.getPacmanCurrentNodeIndex());
    }

    /**
     * Returns the possible moves without the neutral move
     * @param gameState
     * @param index
     * @return
     */
    ArrayList<Constants.MOVE> getAvailableMoves(Game gameState, int index) {
        ArrayList<Constants.MOVE> possibleMoves = new ArrayList<>(Arrays.asList(gameState.getPossibleMoves(index)));
        possibleMoves.remove(Constants.MOVE.NEUTRAL);

        return possibleMoves;
    }

    /**
     * Assign the received reward to every parent of the parent up to the rootNode
     * Increase the visited count of every node included in backpropagation
     * @param reward
     */
    private void Backpropagate(float reward) {
        currentNode.reward = reward;
        currentNode.timesVisited++;
        currentNode = currentNode.parent;

        while(currentNode != null) {
            currentNode.timesVisited++;
            currentNode.reward = currentNode.children.stream().max(Comparator.comparingDouble(c -> (double)c.reward)).get().reward;
            currentNode = currentNode.parent;
        }
    }

    private int getDistanceToNextJunction(MCTSNode node, Constants.MOVE direction) {
        Game tempState = node.gameState.copy();

        simulateOneStep(tempState, direction);

        return simulateUntilNextJunction(tempState) + 1;
    }

    /**
     * Check if the state is the end of the game
     * @param gameState
     * @return
     */
    private boolean TerminalState(Game gameState) {
        return gameState.wasPacManEaten() || gameState.getActivePillsIndices().length == 0;
    }

    /**
     * Expand the current node by adding new child to the currentNode
     */
    private void Expand() {
        Game nextGameState = currentNode.gameState.copy();

        ArrayList<Constants.MOVE> unperformedActions = currentNode.getPacmanMovesNotExpanded();
        Constants.MOVE pacmanMove = unperformedActions.get(random.nextInt(unperformedActions.size()));

        EnumMap ghostMoves = GhostAIActions(currentNode.gameState);
        // Advance the game and create a new node for the new state
        nextGameState.advanceGame(pacmanMove, ghostMoves);

        int pathLengthInSteps = simulateUntilNextJunction(nextGameState);

        MCTSNode child = new MCTSNode(nextGameState, pathLengthInSteps);

        child.parent = currentNode;
        child.parentAction = pacmanMove;

        currentNode.children.add(child);
        currentNode = child;
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

    /**
     * Used in game simulation to pick random action for the agent
     * @param gameState state
     * @return action
     */
    private Constants.MOVE RandomPossibleAction(Game gameState) {
        List<Constants.MOVE> possibleMoves = Arrays.asList(gameState.getPossibleMoves(gameState.getPacmanCurrentNodeIndex()));
        possibleMoves.remove(Constants.MOVE.NEUTRAL);
        return possibleMoves.get(random.nextInt(possibleMoves.size()));
    }

    /**
     * Used in game simulation to pick actions for each ghost in the game
     * @param gameState
     * @return returns an {@link EnumMap} containing the move for each ghost
     */
    private EnumMap<Constants.GHOST, Constants.MOVE> GhostAIActions(Game gameState) {
        return ghostsController.getMove(gameState, System.currentTimeMillis() + 5);
    }

}
