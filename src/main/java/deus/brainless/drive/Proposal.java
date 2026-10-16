package deus.brainless.drive;

/**
 * A drive paired with how badly it wants to be served, as read off the limbic
 * state. The unit of competition: arbitration chooses between proposals, never
 * between raw node values.
 *
 * <p>Carries no action. Deciding between actions that would serve the same
 * drive is the planner's job, not this record's.
 */
public record Proposal(Drive drive, double priority) {}