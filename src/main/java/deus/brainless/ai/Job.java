package deus.brainless.ai;

public record Job(String name, double priority, Runnable task, Runnable onInterrupt) {

    public Job(String name, double priority, Runnable task) {
        this(name, priority, task, null);
    }

    public void run() {
        task.run();
    }

    public void interrupt() {
        if (onInterrupt != null) onInterrupt.run();
    }

    @Override
    public String toString() {
        return "Job[" + name + ", priority=" + String.format("%.2f", priority) + "]";
    }
}
