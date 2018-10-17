# Ms Pacman AI Implementations

This project implements controllers using a very simple Behavior Tree, Monte Carlo Tree Search and a Genetic Algorithm evolving the hyperparameters of the MCTS implementation.

## Implementation and Evaluation Notes

### Behavior Trees

#### First Algorithm - Behavior Tree

The tree:

- Sequence("Root")
  - SetVariable("closest enemy distance")
  - Selector("Gather-Escape")
    - Sequence("Gather")
      - CheckVariableLeaf("closest enemy distance")
      - CollectClosestPillAction()
    - Sequence("Escape")
      - FleeAction()

- The tree could be simpler but this was mean to be used as a base to extend upon
- Gather and Escape sequences have to work together to not flee to the left to escape and then when out of range go instantly right because the closest pill is still in this direction
- Thus this approach using "Collect closest pill" was not particularly good
- This simple tree was okay against random ghosts but did not perform well against starter ghosts

#### Second Algorithm - Monte Carlo Tree Search

- This implementation of an MCTS tree used the following approach:
    - Instead of single steps the nodes of the algorithm were the intersections of the tree
    - An edge connecting two intersections is represented as an action (e.g. MOVE.RIGHT)
    - The reward at each node is ```1 - (pills_remaining / total_number_of_pills)```
    - To make ensure a bigger difference between rewards of the nodes return moves were not considered and the maximum number of steps was limited by a constant.
- Using intersections as nodes greatly reduced the state space and made debugging easier because following the path was easy.

Problems:
- One problem which needs to be addressed is that due to the fact that the distance was limited pacman had trouble finding pills that not in the simulation distance in the end.
- Another problem is that small groups between intersections were left out because the reward was higher to ignore them but go a little bit further for the bigger group.