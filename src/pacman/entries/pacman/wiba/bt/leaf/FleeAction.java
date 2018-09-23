package pacman.entries.pacman.wiba.bt.leaf;

import pacman.entries.pacman.wiba.bt.Blackboard;
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

    public FleeAction(Blackboard blackboard, IControllerActions controllerActions) {
        super("Flee Action", blackboard, controllerActions);

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

                int distance = currentGameState.getShortestPathDistance(pacmanPosition, currentGameState.getGhostCurrentNodeIndex(ghost));

                if(distance < 30) {
                    Constants.MOVE direction = currentGameState.getNextMoveTowardsTarget(pacmanPosition, currentGameState.getGhostCurrentNodeIndex(ghost), Constants.DM.PATH);
                    enemyDistances.add(new Pair<>(direction, distance));
                }

            }
        }

        ArrayList<Constants.MOVE> goodMoves = new ArrayList<>(Arrays.asList(currentGameState.getPossibleMoves(pacmanPosition)));
        goodMoves.remove(Constants.MOVE.NEUTRAL);
        for(Pair<Constants.MOVE, Integer> enemyDirections : enemyDistances) {
            goodMoves.remove(enemyDirections.getFirst());
        }

        Constants.MOVE nextMove;
        if(goodMoves.size() > 0) {
            nextMove = goodMoves.get(random.nextInt(goodMoves.size()));
        } else {
            nextMove = enemyDistances.stream().max((p1, p2) -> p2.getSecond() - p1.getSecond()).map(Pair::getFirst).get().opposite();
        }

        controllerActions.setNextMove(nextMove);


        return Status.SUCCESS;
    }

    @Override
    protected void postUpdate() {

    }
}
