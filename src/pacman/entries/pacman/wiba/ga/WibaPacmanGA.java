package pacman.entries.pacman.wiba.ga;

import pacman.controllers.Controller;
import pacman.entries.pacman.wiba.mcts.MCTSParams;
import pacman.entries.pacman.wiba.mcts.SimpleMCTS;
import pacman.game.Constants;
import pacman.game.Game;

public class WibaPacmanGA extends Controller<Constants.MOVE> {
    private Constants.MOVE myMove = Constants.MOVE.NEUTRAL;

    private MCTSParams params;

    public WibaPacmanGA(MCTSParams params) {
        this.params = params;
    }

    public Constants.MOVE getMove(Game game, long timeDue) {

        SimpleMCTS mcts = new SimpleMCTS(params, game, timeDue, false);
        Constants.MOVE nextMove = mcts.runMCTS();
        if(nextMove != null) {
            myMove = nextMove;
        }

        return myMove;
    }
}
