package deus.brainless.ai.jobs;

import deus.brainless.ai.interfaces.Job;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

public class InlineJob<CTX> implements Job<CTX> {
    private final String name;
    private final Consumer<CTX> tick;
    private final Predicate<CTX> isDone;
    private final ToDoubleFunction<CTX> progress;
    private final Consumer<CTX> onFinish;

    public InlineJob(String name, Consumer<CTX> tick, Predicate<CTX> isDone,
                     ToDoubleFunction<CTX> progress, Consumer<CTX> onFinish) {
        this.name = name;
        this.tick = tick;
        this.isDone = isDone;
        this.progress = progress;
        this.onFinish = onFinish;
    }

    @Override public String name() { return name; }
    @Override public void tick(CTX ctx) { tick.accept(ctx); }
    @Override public boolean isDone(CTX ctx) { return isDone.test(ctx); }
    @Override public double progress(CTX ctx) { return progress.applyAsDouble(ctx); }
	@Override public void onFinish(CTX ctx) { onFinish.accept(ctx); }

	@Override
	public String parentJobName() {
		return "noparenth";
	}

	@Override
	public boolean hasParent() {
		return false;
	}

	@Override
	public void setParent(String parent) {

	}


}
