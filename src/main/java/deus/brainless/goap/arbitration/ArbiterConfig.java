package deus.brainless.goap.arbitration;

import deus.brainless.drive.Drive;

/** Arbitration tuning: the priority floor and the drive wanted when nothing else is. */
public record ArbiterConfig(double minPriority, Drive fallback) {}