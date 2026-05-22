package deus.brainless.ai.interfaces;

@FunctionalInterface
public interface JobTask<CTX> {
    void run(CTX context);
}
