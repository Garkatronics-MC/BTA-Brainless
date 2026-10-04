package deus.brainless.oldAgent.interfaces;

@Deprecated(since = "1.5.0", forRemoval = true)
@FunctionalInterface
public interface Task<CTX> {
    void run(CTX context);
}
