package deus.brainless.agent;

@FunctionalInterface
public interface Action<C> {
    void run(C ctx, double value);
}
