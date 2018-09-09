package pacman.entries.pacman.wiba.bt.leaf;

import pacman.entries.pacman.wiba.bt.Blackboard;
import pacman.entries.pacman.wiba.bt.Status;
import pacman.entries.pacman.wiba.bt.TreeNode;

import java.util.function.Supplier;

public class SetVariableLeaf extends TreeNode {

    private String variable;
    private Supplier<String> value;
    private Blackboard blackboard;

    public SetVariableLeaf(Blackboard blackboard, String variable, Supplier<String> value) {
        this.blackboard = blackboard;
        this.variable = variable;
        this.value = value;
    }

    @Override
    protected void initialize() {}

    @Override
    protected Status update() {
        blackboard.set(variable, value.get());
        return Status.SUCCESS;
    }

    @Override
    protected void postUpdate() {}

}
