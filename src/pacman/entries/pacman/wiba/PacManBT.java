package pacman.entries.pacman.wiba;

import pacman.controllers.Controller;
import pacman.entries.pacman.wiba.bt.Blackboard;
import pacman.entries.pacman.wiba.bt.TreeNode;
import pacman.entries.pacman.wiba.bt.composite.Sequence;
import pacman.entries.pacman.wiba.bt.leaf.SetVariableLeaf;
import pacman.game.Constants;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/*
 * This is the class you need to modify for your entry. In particular, you need to
 * fill in the getAction() method. Any additional classes you write should either
 * be placed in this package or sub-packages (e.g., game.entries.pacman.mypackage).
 */
public class PacManBT extends Controller<MOVE> {
    private MOVE myMove = MOVE.NEUTRAL;

    private TreeNode root;
    private Blackboard blackboard;

    private Game currentGameState;

    public PacManBT() {
        blackboard = new Blackboard();
        // Build the Behavior Tree
        Sequence sequence = new Sequence();
        root = sequence;

        SetVariableLeaf setClosestEnemyDistance = new SetVariableLeaf(blackboard, "enemy.distance", () -> ""+getDangerDistance());


        sequence.addChild(setClosestEnemyDistance);

    }

    public MOVE getMove(Game game, long timeDue) {
        currentGameState = game;

        System.out.println("Searching the tree...");
        root.tick();
        System.out.println("Move found! " + myMove);

        return myMove;
    }



    public int getDangerDistance() {

        int minDistance = Integer.MAX_VALUE;
        int current = currentGameState.getPacmanCurrentNodeIndex();

        for(Constants.GHOST ghost : Constants.GHOST.values()) {
            if(currentGameState.getGhostEdibleTime(ghost) == 0 && currentGameState.getGhostLairTime(ghost) == 0) {
                minDistance = Math.min(minDistance, currentGameState.getShortestPathDistance(current, currentGameState.getGhostCurrentNodeIndex(ghost)));
            }
        }

        return minDistance;
    }
}