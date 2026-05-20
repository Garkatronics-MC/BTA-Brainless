package deus.brainless.actor;

import java.util.List;

public class ActionChain<T> {

    List<Action<T>> actions;

    private ActionChain(List<Action<T>> actions) {
        this.actions = actions;
    }

    public <C> ActionChain<C> of(Action<C>... actions) {
        return new ActionChain<>(List.of(actions));
    }


}
