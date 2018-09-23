package pacman.entries.pacman.wiba.bt.leaf;

import pacman.entries.pacman.wiba.bt.Blackboard;
import pacman.entries.pacman.wiba.bt.TreeNode;
import pacman.entries.pacman.wiba.bt.utils.IControllerActions;

public abstract class ActionNode extends TreeNode {

    protected IControllerActions controllerActions;
    protected Blackboard blackboard;

    protected ActionNode(String name, Blackboard blackboard, IControllerActions controllerActions) {
        super(name);
        this.blackboard = blackboard;
        this.controllerActions = controllerActions;
    }
}
