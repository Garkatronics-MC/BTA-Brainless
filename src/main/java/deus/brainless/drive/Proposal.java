package deus.brainless.drive;

/**
 * A drive and the urgency it is asking with, read off the limbic state.
 * Arbitration compares proposals, never raw node values. Carries no action.
 */
public record Proposal(Drive drive, double priority) {}