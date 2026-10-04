package deus.brainless.agent;

@FunctionalInterface
public interface Reaction<C> {
    void run(C ctx, double value);
}
