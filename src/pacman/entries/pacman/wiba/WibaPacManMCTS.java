package pacman.entries.pacman.wiba;

import pacman.controllers.Controller;
import pacman.entries.pacman.wiba.mcts.SimpleMCTS;
import pacman.entries.pacman.wiba.utils.Utils;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/*
 * This is the class you need to modify for your entry. In particular, you need to
 * fill in the getAction() method. Any additional classes you write should either
 * be placed in this package or sub-packages (e.g., game.entries.pacman.mypackage).
 */
public class WibaPacManMCTS extends Controller<MOVE> {
    private MOVE myMove = MOVE.NEUTRAL;

    private final boolean printLog = false;

    public MOVE getMove(Game game, long timeDue) {

        SimpleMCTS mcts = new SimpleMCTS(game, timeDue,printLog);
        MOVE nextMove = mcts.runMCTS();
        if(nextMove != null) {
            myMove = nextMove;
        }

        if (printLog) {
            System.out.println("Time done: " + Utils.getFormattedTime(System.currentTimeMillis()));
            System.out.println("Delta: " + (System.currentTimeMillis()-timeDue) + "ms");
        }

        return myMove;
    }

}