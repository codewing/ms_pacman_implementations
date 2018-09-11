package pacman.entries.pacman.wiba.bt.composite;

import pacman.entries.pacman.wiba.bt.Status;
import pacman.entries.pacman.wiba.bt.TreeNode;

public class Sequence extends CompositeNode {

    public Sequence(String name) {
        super();
        this.name = name + " Sequence";
    }

    @Override
    public void initialize() {

    }

    @Override
    public Status update() {
        // iterate over every node
        for(TreeNode child : children) {
            Status childStatus = child.tick();

            if(childStatus != Status.SUCCESS) {
                return childStatus;
            }
        }

        return Status.SUCCESS;
    }

    @Override
    public void postUpdate() {

    }
}
