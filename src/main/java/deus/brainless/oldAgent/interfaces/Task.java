package deus.brainless.oldAgent.interfaces;

@FunctionalInterface
public interface Task<CTX> {
    void run(CTX context);
}
