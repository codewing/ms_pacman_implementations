package pacman.entries.pacman.wiba.bt.composite;

import pacman.entries.pacman.wiba.bt.TreeNode;

import java.util.ArrayList;

public abstract class CompositeNode extends TreeNode {
    protected ArrayList<TreeNode> children;

    protected CompositeNode() {
        children = new ArrayList<>();
    }

    public void addChild(TreeNode child) {
        children.add(child);
    }
}
