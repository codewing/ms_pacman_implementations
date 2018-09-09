package pacman.entries.pacman.wiba.bt.leaf;

import pacman.entries.pacman.wiba.bt.TreeNode;
import pacman.entries.pacman.wiba.bt.utils.IControllerActions;

public abstract class ActionNode extends TreeNode {

    protected IControllerActions controllerActions;

    protected ActionNode(IControllerActions controllerActions) {
        this.controllerActions = controllerActions;
    }
}
