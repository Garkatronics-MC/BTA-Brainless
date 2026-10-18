package deus.brainless.drive;

/** A drive and its urgency. Arbitration compares proposals, never raw node values. */
public record Proposal(Drive drive, double priority) {}