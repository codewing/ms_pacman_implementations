package pacman.entries.pacman.wiba;

import pacman.controllers.Controller;
import pacman.entries.pacman.wiba.bt.Blackboard;
import pacman.entries.pacman.wiba.bt.TreeNode;
import pacman.entries.pacman.wiba.bt.composite.Selector;
import pacman.entries.pacman.wiba.bt.composite.Sequence;
import pacman.entries.pacman.wiba.bt.leaf.CheckVariableLeaf;
import pacman.entries.pacman.wiba.bt.leaf.CollectClosestPillAction;
import pacman.entries.pacman.wiba.bt.leaf.FleeAction;
import pacman.entries.pacman.wiba.bt.leaf.SetVariableLeaf;
import pacman.entries.pacman.wiba.bt.utils.IControllerActions;
import pacman.game.Constants;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/*
 * This is the class you need to modify for your entry. In particular, you need to
 * fill in the getAction() method. Any additional classes you write should either
 * be placed in this package or sub-packages (e.g., game.entries.pacman.mypackage).
 */
public class PacManBT extends Controller<MOVE> implements IControllerActions {
    private MOVE myMove = MOVE.NEUTRAL;

    private TreeNode root;
    private Blackboard blackboard;

    private Game currentGameState;

    public PacManBT() {
        blackboard = new Blackboard();

        // Build the Behavior Tree
        Sequence rootSequence = new Sequence("Root");
        root = rootSequence;

        // 1) initialize common variables
        SetVariableLeaf setClosestEnemyDistance = new SetVariableLeaf(blackboard, "enemy.distance", () -> ""+getDangerDistance());
        Selector gatherEscapeSelector = new Selector("Gather-Escape");

        rootSequence.addChild(setClosestEnemyDistance);
        rootSequence.addChild(gatherEscapeSelector);

        // 2 a) build gather sequence
        Sequence gatherSequence = new Sequence("Gather");
        CheckVariableLeaf canGatherCheck = new CheckVariableLeaf(blackboard, "enemy.distance", (dist) -> Integer.parseInt(dist) > 20);
        CollectClosestPillAction collectClosestPillAction = new CollectClosestPillAction(this);

        gatherSequence.addChild(canGatherCheck);
        gatherSequence.addChild(collectClosestPillAction);

        // 2 b) construct escape sequence
        Sequence escapeSequence = new Sequence("Escape");
        FleeAction fleeAction = new FleeAction(this);

        escapeSequence.addChild(fleeAction);


        gatherEscapeSelector.addChild(gatherSequence);
        gatherEscapeSelector.addChild(escapeSequence);

    }

    public MOVE getMove(Game game, long timeDue) {
        currentGameState = game;

        //System.out.println("Searching the tree...");
        root.tick();
        //System.out.println("Move found! " + myMove);

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

    @Override
    public void setNextMove(MOVE nextMove) {
        this.myMove = nextMove;
    }

    @Override
    public Game getGameState() {
        return currentGameState;
    }
}