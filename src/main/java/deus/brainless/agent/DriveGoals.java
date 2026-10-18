package deus.brainless.agent;

import deus.brainless.drive.Drive;
import deus.brainless.goap.GoapState;

import java.util.Optional;

/**
 * Maps a drive to a {@link Goal} the planner can pursue, or empty if the drive
 * cannot be pursued right now.
 */
@FunctionalInterface
public interface DriveGoals {

	Optional<Goal> goalFor(Drive drive, GoapState world);
}