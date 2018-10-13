package pacman.entries.pacman.wiba.mcts;

import pacman.controllers.examples.StarterGhosts;
import pacman.entries.pacman.wiba.utils.Utils;
import pacman.game.Constants;
import pacman.game.Game;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class UCT {
    /*
     * Maze used to control the game
     */
    private Game game;
    private Random random = new Random();
    private StarterGhosts ghostsController = new StarterGhosts();

    private MctsNode rootNode; // place where we start
    private MctsNode currentNode; // currently processing this node

    private final float explorationCoefficient = (float) (1.0/Math.sqrt(2));

    /*
     * Computational limit
     */
    private final long timeDue;

    private final long initTime;

    /**
     * Constructor
     * get the current game
     */
    public UCT(Game game, long timeDue){
        this.game = game;
        this.timeDue = timeDue;
        this.initTime = System.currentTimeMillis();

        System.out.println("Started MCTS with UCT at " + Utils.getFormattedTime(initTime) + " and it is allowed to run until " + Utils.getFormattedTime(timeDue));
    }

    /**
     * run the UCT search and find the optimal action for the root node state
     * @return best move for the current simulation
     */
    public Constants.MOVE runUCT() {

        /*
         * Create root node with the present state
         */
        rootNode = new MctsNode(game.copy());

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
        Optional<MctsNode> bestNode = currentNode.children.stream().max(Comparator.comparingInt(n -> n.timesVisited));
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
            if(!FullyExpanded(currentNode)) {
                Expand();
                return;
            } else {
                BestChild(explorationCoefficient);
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

        int maxSteps = 40;
        int actualSteps = 0;
        int numberOfActivePillsStart = st.getActivePillsIndices().length;
        while(actualSteps < maxSteps && !TerminalState(st)){
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
            EnumMap<pacman.game.Constants.GHOST, pacman.game.Constants.MOVE> ghostMoves = ghostsController.getMove(st, System.currentTimeMillis() + 5);
            st.advanceGame(pacmanAction, ghostMoves);

            actualSteps++;
        }
        float diffPills = numberOfActivePillsStart - st.getActivePillsIndices().length;
        float reward = st.wasPacManEaten() ? 0 : diffPills/(actualSteps+1.0f);

        return reward;
    }

    /**
     * Assign the received reward to every parent of the parent up to the rootNode
     * Increase the visited count of every node included in backpropagation
     * @param reward
     */
    private void Backpropagate(float reward) {
        while(currentNode != null) {
            currentNode.timesVisited++;
            currentNode.reward += reward;
            currentNode = currentNode.parent;
        }
    }

    /**
     * Check if the node is fully expanded
     * @param node
     * @return
     */
    private boolean FullyExpanded(MctsNode node) {
        ArrayList<Constants.MOVE> possibleMoves = new ArrayList<>(Arrays.asList(node.gameState.getPossibleMoves(node.gameState.getPacmanCurrentNodeIndex())));
        possibleMoves.remove(Constants.MOVE.NEUTRAL);

        // remove the return move
        if(node.parent != null) {
            possibleMoves.remove(node.parentAction.opposite());
        }

        return node.children.size() == possibleMoves.size();
    }

    /**
     * Check if the state is the end of the game
     * @param gameState
     * @return
     */
    private boolean TerminalState(Game gameState) {
        return gameState.wasPacManEaten() || gameState.getPillIndices().length == 0;
    }

    /**
     * Choose the best child according to the UCT value
     * Assign it as a currentNode
     * @param c Exploration coefficient
     */
    private void BestChild(float c) {
        MctsNode nt = currentNode;
        MctsNode bestChild = null;

        float bestUCTValue = Float.MIN_VALUE;
        for(MctsNode child : nt.children) {
            float childValue = UCTvalue(child, c);
            if(childValue > bestUCTValue) {
                bestChild = child;
                bestUCTValue = childValue;
            }
        }

        assert bestChild != null;

        currentNode = bestChild;
    }

    /**
     * Calculate UCT value for the best child choosing
     * @param n child node of currentNode
     * @param c Exploration coefficient
     * @return
     */
    private float UCTvalue(MctsNode n, float c) {
        return (float) (n.reward / n.timesVisited + c * Math.sqrt( Math.log(n.parent.timesVisited) / n.timesVisited));
    }

    /**
     * Expand the current node by adding new child to the currentNode
     */
    private void Expand() {

        Game nextGameState = currentNode.gameState.copy();

        Constants.MOVE pacmanMove = UnperformedAction(currentNode);
        EnumMap ghostMoves = GhostAIActions(currentNode.gameState);

        // Advance the game and create a new node for the new state
        nextGameState.advanceGame(pacmanMove, ghostMoves);
        MctsNode child = new MctsNode(nextGameState);

        child.parent = currentNode;
        child.parentAction = pacmanMove;

        currentNode.children.add(child);
        currentNode = child;
    }

    /**
     * Returns all suitable moves for this node
     * @param n
     * @return
     */
    private Constants.MOVE UnperformedAction(MctsNode n) {
        ArrayList<Constants.MOVE> suitableMoves = new ArrayList<>(Arrays.asList(n.gameState.getPossibleMoves(n.gameState.getPacmanCurrentNodeIndex())));
        suitableMoves.remove(Constants.MOVE.NEUTRAL);

        suitableMoves.removeIf(suitableMove -> n.children.contains(suitableMove));

        // remove the return move
        if(n.parent != null) {
            suitableMoves.remove(n.parentAction.opposite());
        }

        assert suitableMoves.size() > 0;

        return suitableMoves.get(random.nextInt(suitableMoves.size()));
    }

    /**
     * Check if the algorithm is to be terminated, e.g. reached number of iterations limit
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
