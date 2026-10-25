package deus.brainless.fsm;

public record FSMRule<C>(FSMCondition<C> condition, FSMState target) {}
