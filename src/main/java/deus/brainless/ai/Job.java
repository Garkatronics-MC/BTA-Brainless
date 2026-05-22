package deus.brainless.ai;

import deus.brainless.ai.interfaces.JobTask;

public record Job<CTX>(String name, double priority, JobTask<CTX> task, JobTask<CTX> onInterrupt) {

    public Job(String name, double priority, JobTask<CTX> task) {
        this(name, priority, task, null);
    }

    public void run(CTX context) {
        task.run(context);
    }

    public void interrupt(CTX context) {
        if (onInterrupt != null) onInterrupt.run(context);
    }

    @Override
    public String toString() {
        return "Job[" + name + ", priority=" + String.format("%.2f", priority) + "]";
    }
}
