package pacman.entries.pacman.wiba.mcts;

import pacman.game.Constants;
import pacman.game.Game;

import java.util.ArrayList;
import java.util.List;

public class MctsNode{

    Game gameState;
    List<MctsNode> children = new ArrayList<>();
    MctsNode parent = null;
    Constants.MOVE parentAction = null;
    float reward = 0;
    int timesVisited = 0;


    MctsNode(Game gameState){
        this.gameState = gameState;
    }
}
