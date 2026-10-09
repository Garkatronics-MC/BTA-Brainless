package deus.brainless.limbic;

@FunctionalInterface
public interface Effector<C> {
    void run(C ctx, double value);
}
