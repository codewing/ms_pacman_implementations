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
import pacman.entries.pacman.wiba.mcts.UCT;
import pacman.game.Constants;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/*
 * This is the class you need to modify for your entry. In particular, you need to
 * fill in the getAction() method. Any additional classes you write should either
 * be placed in this package or sub-packages (e.g., game.entries.pacman.mypackage).
 */
public class WibaPacManMCTS extends Controller<MOVE> {
    private MOVE myMove = MOVE.NEUTRAL;

    private UCT uct;

    public MOVE getMove(Game game, long timeDue) {
        uct = new UCT(game, timeDue);
        setNextMove(uct.runUCT());

        return myMove;
    }

    public void setNextMove(MOVE nextMove) {
        this.myMove = nextMove;
    }

}