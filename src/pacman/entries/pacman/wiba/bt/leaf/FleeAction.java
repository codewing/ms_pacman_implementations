package pacman.entries.pacman.wiba.bt.leaf;

import pacman.entries.pacman.wiba.bt.Status;
import pacman.entries.pacman.wiba.bt.utils.IControllerActions;
import pacman.entries.pacman.wiba.bt.utils.Pair;
import pacman.game.Constants;
import pacman.game.Game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class FleeAction extends ActionNode {

    private Random random;

    public FleeAction(IControllerActions controllerActions) {
        super(controllerActions);
        this.random = new Random();
    }

    @Override
    protected void initialize() {

    }

    @Override
    protected Status update() {
        ArrayList<Pair<Constants.MOVE, Integer>> enemyDistances = new ArrayList<>();

        Game currentGameState = controllerActions.getGameState();
        int pacmanPosition = currentGameState.getPacmanCurrentNodeIndex();

        for(Constants.GHOST ghost : Constants.GHOST.values()) {
            if(currentGameState.getGhostEdibleTime(ghost) == 0 && currentGameState.getGhostLairTime(ghost) == 0) {

                Constants.MOVE direction = currentGameState.getNextMoveTowardsTarget(pacmanPosition, currentGameState.getGhostCurrentNodeIndex(ghost), Constants.DM.PATH);
                int distance = currentGameState.getShortestPathDistance(pacmanPosition, currentGameState.getGhostCurrentNodeIndex(ghost));

                enemyDistances.add(new Pair<>(direction, distance));
            }
        }

        ArrayList<Constants.MOVE> goodMoves = new ArrayList<>(Arrays.asList(Constants.MOVE.values()));
        for(Pair<Constants.MOVE, Integer> enemyDirections : enemyDistances) {
            goodMoves.remove(enemyDirections.getFirst());
        }


        Constants.MOVE nextMove;
        if(goodMoves.size() > 0) {
            nextMove = goodMoves.get(random.nextInt(goodMoves.size()));
        } else {
            nextMove = enemyDistances.stream().max((p1, p2) -> p2.getSecond() - p1.getSecond()).map(Pair::getFirst).get();
        }

        controllerActions.setNextMove(nextMove);


        return Status.SUCCESS;
    }

    @Override
    protected void postUpdate() {

    }
}
