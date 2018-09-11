package pacman.entries.pacman.wiba.bt.leaf;

import pacman.entries.pacman.wiba.bt.Blackboard;
import pacman.entries.pacman.wiba.bt.Status;
import pacman.entries.pacman.wiba.bt.TreeNode;

import java.util.function.Predicate;
import java.util.function.Supplier;

public class CheckVariableLeaf extends TreeNode {

    private String variable;
    private Predicate<String> condition;
    private Blackboard blackboard;

    public CheckVariableLeaf(Blackboard blackboard, String variable, Predicate<String> condition) {
        this.name = "Check Variable: " + variable;

        this.blackboard = blackboard;
        this.variable = variable;
        this.condition = condition;
    }

    @Override
    protected void initialize() {}

    @Override
    protected Status update() {
        if (condition.test(blackboard.get(variable))) {
            return Status.SUCCESS;
        }
        return Status.FAILURE;
    }

    @Override
    protected void postUpdate() {}

}
