package deus.brainless.actor;

import deus.brainless.actor.interfaces.Undoable;

import java.util.function.Consumer;

public final class Reversible {

    private Reversible() {
    }

    public static <T> Action<T> of(Consumer<T> run, Consumer<T> undo) {
        return new UndoableAction<>(run, undo);
    }

    public static final class UndoableAction<T> extends Action<T> implements Undoable<T> {

        private final Consumer<T> undoConsumer;

        private UndoableAction(Consumer<T> run, Consumer<T> undo) {
            super(run);
            this.undoConsumer = undo;
        }

        @Override
        public void undo(T state) {
            undoConsumer.accept(state);
        }
    }
}
