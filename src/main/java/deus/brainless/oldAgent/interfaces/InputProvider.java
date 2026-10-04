package deus.brainless.oldAgent.interfaces;

import deus.brainless.oldAgent.AI;

@Deprecated(since = "1.5.0", forRemoval = true)
public interface InputProvider {
    void fill(AI.InputSetter input);
}
