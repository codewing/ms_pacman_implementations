package pacman.entries.pacman.wiba.bt;

public abstract class TreeNode {
    protected String name;
    protected Status currentStatus = Status.INVALID;

    public TreeNode(String name) {
        this.name = name;
    }

    public Status tick() {

        if(currentStatus == Status.INVALID) {
            initialize();
        }

        currentStatus = update();

        if(currentStatus != Status.RUNNING) {
            postUpdate();
        }

        return currentStatus;
    }

    protected abstract void initialize();
    protected abstract Status update();
    protected abstract void postUpdate();

    public Status getState() {
        return currentStatus;
    }
}
