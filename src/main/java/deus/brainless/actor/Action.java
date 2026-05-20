package deus.brainless.actor;

import deus.brainless.actor.interfaces.INamedRule;
import deus.brainless.actor.interfaces.IRule;
import deus.brainless.utils.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Action<T> {

    protected final List<IRule> rules;
    protected final Consumer<T> runnable;

    protected final List<Consumer<T>> okConsumers;
    protected final List<Consumer<List<RuleData>>> errConsumers;

    public Action(Consumer<T> runnable) {
        this.runnable = runnable;
        this.rules = new ArrayList<>();
        this.okConsumers = new ArrayList<>();
        this.errConsumers = new ArrayList<>();
    }

    public static <C> Action<C> create(Consumer<C> runnable) {
        return new Action<>(runnable);
    }

    public static <C> Action<C> from(Consumer<C> consumer) {
        return new Action<>(consumer);
    }

    public Result<Void, List<RuleData>> run(T value) {
        List<RuleData> failed = new ArrayList<>();

        for (int i = 0; i < rules.size(); i++) {
            IRule rule = rules.get(i);
            boolean result = rule.run();

            String name = (rule instanceof INamedRule named)
                    ? named.name()
                    : "Unnamed";

            if (result) {
                failed.add(new RuleData(i, name));
            }
        }

        if (failed.isEmpty()) {
            runnable.accept(value);
            okConsumers.forEach(c -> c.accept(value));
            return Result.ok((Void) null);
        } else {
            errConsumers.forEach(c -> c.accept(failed));
            return Result.err(failed);
        }
    }

    // ---- Rules ----

    public Action<T> addRule(IRule rule) {
        rules.add(rule);
        return this;
    }

    public Action<T> quitRule(IRule rule) {
        rules.remove(rule);
        return this;
    }

    // ---- OK consumers ----

    public Action<T> onOk(Consumer<T> consumer) {
        okConsumers.add(consumer);
        return this;
    }

    public Action<T> quitOk(Consumer<T> consumer) {
        okConsumers.remove(consumer);
        return this;
    }

    // ---- ERR consumers ----

    public Action<T> onErr(Consumer<List<RuleData>> consumer) {
        errConsumers.add(consumer);
        return this;
    }

    public Action<T> quitErr(Consumer<List<RuleData>> consumer) {
        errConsumers.remove(consumer);
        return this;
    }
}
