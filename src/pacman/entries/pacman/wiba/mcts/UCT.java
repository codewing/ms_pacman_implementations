package pacman.entries.pacman.wiba.mcts;

import pacman.controllers.examples.StarterGhosts;
import pacman.entries.pacman.wiba.utils.Utils;
import pacman.game.Constants;
import pacman.game.Game;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
        Optional<MctsNode> bestNode = currentNode.children.stream().max(Comparator.comparingInt(n -> n.timesvisited));
        if(bestNode.isPresent()) return bestNode.get().parentAction;

        return Constants.MOVE.NEUTRAL;
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
        while(!TerminalState(st)){
            // let pacman decide the direction at every junction
            if(st.isJunction(st.getPacmanCurrentNodeIndex())) {
                pacmanAction = RandomPossibleAction(st);
            }
            EnumMap<pacman.game.Constants.GHOST, pacman.game.Constants.MOVE> ghostMoves = ghostsController.getMove(st, System.currentTimeMillis() + 5);
            st.advanceGame(pacmanAction, ghostMoves);
        }
        return st.wasPacManEaten() ? 0 : 1;
    }

    /**
     * Assign the received reward to every parent of the parent up to the rootNode
     * Increase the visited count of every node included in backpropagation
     * @param reward
     */
    private void Backpropagate(float reward) {
        while(currentNode != null) {
            currentNode.timesvisited++;
            currentNode.reward += reward;
            currentNode = currentNode.parent;
        }
    }

    /**
     * Check if the node is fully expanded
     * @param nt
     * @return
     */
    private boolean FullyExpanded(MctsNode nt) {
        List<Constants.MOVE> possibleMoves = Arrays.asList(nt.gameState.getPossibleMoves(nt.gameState.getPacmanCurrentNodeIndex()));
        possibleMoves.remove(Constants.MOVE.NEUTRAL);

        return nt.children.size() == possibleMoves.size();
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

        float bestUCTValue = -1;
        for(MctsNode child : nt.children) {
            float childValue = UCTvalue(child, c);
            if(childValue > bestUCTValue) {
                bestChild = child;
                bestUCTValue = childValue;
            }
        }

        currentNode = bestChild;
    }

    /**
     * Calculate UCT value for the best child choosing
     * @param n child node of currentNode
     * @param c Exploration coefficient
     * @return
     */
    private float UCTvalue(MctsNode n, float c) {
        return (float) (n.reward / n.timesvisited + c * Math.sqrt( Math.log(n.parent.timesvisited) / n.timesvisited));
    }

    /**
     * Expand the current node by adding new child to the currentNode
     */
    private void Expand() {

        // copy the state for the child node
        Game nextGameState = currentNode.gameState.copy();

        // Get Move for Pacman and AI
        Constants.MOVE pacmanMove = UntriedAction(currentNode);
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
     * Returns the first untried action of the node
     * @param n
     * @return
     */
    private Constants.MOVE UntriedAction(MctsNode n) {
        ArrayList<Constants.MOVE> possibleMoves = new ArrayList<>(Arrays.asList(n.gameState.getPossibleMoves(n.gameState.getPacmanCurrentNodeIndex())));
        possibleMoves.remove(Constants.MOVE.NEUTRAL);

        List<Constants.MOVE> executedMoves = n.children.parallelStream().map(child -> child.parentAction).collect(Collectors.toList());
        for(Constants.MOVE move : executedMoves) {
            possibleMoves.remove(move);
        }

        if(possibleMoves.size() > 0){
            return possibleMoves.get(0);
        }

        throw new RuntimeException("All possible moves tried. Why am I here?");
    }

    /**
     * Check if the algorithm is to be terminated, e.g. reached number of iterations limit
     * @param lastDeltaNS the amount of time the last iteration took
     * @return
     */
    private boolean Terminate(long lastDeltaNS) {
        long lastDeltaMillis = TimeUnit.MILLISECONDS.convert(lastDeltaNS, TimeUnit.NANOSECONDS);
        if ( System.currentTimeMillis() + lastDeltaMillis > timeDue) return true;
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
