package pacman.entries.pacman.wiba.bt.leaf;

import pacman.entries.pacman.wiba.bt.Blackboard;
import pacman.entries.pacman.wiba.bt.Status;
import pacman.entries.pacman.wiba.bt.TreeNode;

import java.util.function.Predicate;

public class CheckVariableLeaf extends TreeNode {

    private String variable;
    private Predicate<String> condition;
    private Blackboard blackboard;

    public CheckVariableLeaf(Blackboard blackboard, String variable, Predicate<String> condition) {
        super("Check Variable: " + variable);

        this.blackboard = blackboard;
        this.variable = variable;
        this.condition = condition;
    }

    @Override
    protected void initialize() {}

    @Override
    protected Status update() {
        Status ret;

        if (condition.test(blackboard.get(variable))) {
            ret = Status.SUCCESS;
        } else {
            ret = Status.FAILURE;
        }
        System.out.println("Check variable: " + ret);
        return ret;
    }

    @Override
    protected void postUpdate() {}

}
