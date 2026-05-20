package deus.brainless.actor.interfaces;

public interface Undoable<T> {
    void undo(T state);
}
