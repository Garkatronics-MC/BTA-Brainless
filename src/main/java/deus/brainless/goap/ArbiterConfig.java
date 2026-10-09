package deus.brainless.goap;

import deus.brainless.agent.Intent;

public record ArbiterConfig(double hysteresis, double minPriority, Intent fallback) {}
