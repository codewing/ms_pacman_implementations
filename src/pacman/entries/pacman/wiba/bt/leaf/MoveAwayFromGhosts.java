package pacman.entries.pacman.wiba.bt.leaf;

import pacman.entries.pacman.wiba.bt.Status;
import pacman.entries.pacman.wiba.bt.utils.IControllerActions;
import pacman.game.Constants;
import pacman.game.Game;

import java.util.ArrayList;
import java.util.Random;

public class MoveAwayFromGhosts extends ActionNode {

    private Random random;

    public MoveAwayFromGhosts(IControllerActions controllerActions) {
        super(controllerActions);
        this.random = new Random();
    }

    @Override
    protected void initialize() {

    }

    @Override
    protected Status update() {
        ArrayList<Constants.MOVE> goodMoves = new ArrayList<>();
        Game currentGameState = controllerActions.getGameState();

        for(Constants.GHOST ghost : Constants.GHOST.values()) {
            if(currentGameState.getGhostEdibleTime(ghost) == 0 && currentGameState.getGhostLairTime(ghost) == 0) {
                if(currentGameState.getShortestPathDistance(currentGameState.getPacmanCurrentNodeIndex(), currentGameState.getGhostCurrentNodeIndex(ghost)) < 100) {
                    goodMoves.add(currentGameState.getNextMoveAwayFromTarget(currentGameState.getPacmanCurrentNodeIndex(),currentGameState.getGhostCurrentNodeIndex(ghost), Constants.DM.PATH));
                }
            }
        }

        if(goodMoves.size() > 0) {
            Constants.MOVE nextMove = goodMoves.get(random.nextInt(goodMoves.size()));
            controllerActions.setNextMove(nextMove);
        } else {
            controllerActions.setNextMove(Constants.MOVE.values()[random.nextInt(Constants.MOVE.values().length)]);
        }


        return Status.SUCCESS;
    }

    @Override
    protected void postUpdate() {

    }
}
