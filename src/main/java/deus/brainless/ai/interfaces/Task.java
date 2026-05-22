package deus.brainless.ai.interfaces;

@FunctionalInterface
public interface Task<CTX> {
    void run(CTX context);
}
