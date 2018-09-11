package pacman.entries.pacman.wiba.bt.leaf;

import pacman.entries.pacman.wiba.bt.Status;
import pacman.entries.pacman.wiba.bt.utils.IControllerActions;
import pacman.entries.pacman.wiba.bt.utils.Pair;
import pacman.game.Constants;
import pacman.game.Game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class CollectClosestPillAction extends ActionNode {

    private Random random;

    public CollectClosestPillAction(IControllerActions controllerActions) {
        super(controllerActions);
        this.name = "Collect closest pill";

        this.random = new Random();
    }

    @Override
    protected void initialize() {

    }

    @Override
    protected Status update() {
        Game currentGameState = controllerActions.getGameState();

        int pacmanPosition = currentGameState.getPacmanCurrentNodeIndex();
        int closestPillPosition = currentGameState.getClosestNodeIndexFromNodeIndex(pacmanPosition, currentGameState.getActivePillsIndices(), Constants.DM.PATH);

        Constants.MOVE directionToPill = currentGameState.getNextMoveTowardsTarget(pacmanPosition, closestPillPosition, Constants.DM.PATH);

        controllerActions.setNextMove(directionToPill);

        return Status.SUCCESS;
    }

    @Override
    protected void postUpdate() {

    }
}
