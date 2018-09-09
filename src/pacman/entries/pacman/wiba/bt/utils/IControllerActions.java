package pacman.entries.pacman.wiba.bt.utils;

import pacman.game.Constants;
import pacman.game.Game;

public interface IControllerActions {
    void setNextMove(Constants.MOVE nextMove);
    Game getGameState();
}
