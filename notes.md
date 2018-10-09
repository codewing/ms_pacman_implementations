# Evaludation Notes

## Behavior Trees

### First Approach

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
- Thus this approach using "Collect closest pill" wasn't particularly good
- Idea: Add a new node that evaluates each available direction independently from the task