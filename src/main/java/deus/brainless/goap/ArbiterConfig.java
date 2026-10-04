package deus.brainless.goap;

public record ArbiterConfig(double hysteresis, double minPriority, Intent fallback) {}
