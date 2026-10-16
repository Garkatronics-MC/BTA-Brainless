package deus.brainless.agent;

import deus.brainless.drive.Drive;
import deus.brainless.drive.Proposal;
import deus.brainless.goap.GoapAction;
import deus.brainless.goap.GoapPlanner;
import deus.brainless.goap.GoapState;
import deus.brainless.goap.arbitration.GoapArbiter;
import deus.brainless.limbic.Psyche;
import deus.brainless.task.TaskController;
import deus.brainless.utils.Timer;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Glue. Holds a psyche, an arbiter and a planner, and moves one tick forward:
 *
 * <pre>
 *   senses -> Urges -> drives -> Proposal -> arbitration -> the one drive wanted
 *                                                              |
 *                                          already being served -+-> nothing to do
 *                                                              |
 *                                                             goal
 *                                                              |
 *                                                             plan
 * </pre>
 *
 * <p>This is the top of the stack: it depends on everything and nothing depends
 * on it. {@link #update} stops at the plan on purpose — turning actions into tasks
 * belongs to whoever owns the game loop, so that {@code Agent} never has to know
 * what a "task" is.
 *
 * <p>The arbiter is asked only what is wanted, never whether to abandon it. How
 * committed an agent is to what it is doing is answered by {@code
 * Task.interruptible()}, over in the task package — not from here.
 */
public final class Agent {

	private final Psyche psyche;
	private final GoapArbiter arbiter;
	private final GoapPlanner planner;
	private final DriveGoals goals;
	private final TaskController tc;
	private final Timer updateTimer;

	private final List<Proposal> proposed = new ArrayList<>();
	private Drive currentDrive;
	private Optional<Plan> currentPlan = Optional.empty();

	public Agent(Psyche psyche, GoapArbiter arbiter, GoapPlanner planner, DriveGoals goals, TaskController tc, Timer timer) {
		this.psyche = psyche;
		this.arbiter = arbiter;
		this.planner = planner;
		this.goals = goals;
		this.tc = tc;
		this.updateTimer = timer;
	}

	/** Feeds what the world currently looks like. Called every tick, before {@link #update}. */
	public Agent sense(String node, double value) {
		psyche.set(node, value);
		return this;
	}

	/**
	 * Moves one tick forward.
	 *
	 * @param world what the planner's world looks like now — facts, not feelings
	 * @return the plan to carry out, or empty if there is nothing new to do
	 */
	public void update(GoapState world) {
		updateTimer.tick();
		if (!updateTimer.hasElapsed()) return;

		psyche.probe(proposed::add);
		proposed.forEach(arbiter::propose);

		Optional<Drive> wanted = arbiter.resolve();
		if (wanted.isEmpty()) return;

		// The intersection: already being served. Re-planning the same drive every
		// tick would restart the agent forever.
		if (wanted.get().equals(currentDrive)) return;

		Optional<Goal> goal = goals.goalFor(wanted.get(), world);
		if (goal.isEmpty()) return;

		Optional<List<GoapAction>> actions = planner.compute(goal.get().satisfied(), goal.get().heuristic(), goal.get().start());

		if (actions.isEmpty()) return ;

		currentDrive = wanted.get();
		currentPlan = actions.map(list -> new Plan(wanted.get(), List.copyOf(list)));

		// tc.setCurrent()

	}

	/**
	 * Forgets what was being pursued, so the next tick plans afresh. Whoever runs
	 * the tasks calls this when the activity is over or was abandoned — the agent
	 * cannot know on its own, since it never watches the task finish.
	 */
	public void finishCurrent() {
		currentDrive = null;
		currentPlan = Optional.empty();
	}

	public Psyche psyche() { return psyche; }

	/** The drive currently being pursued, or null. */
	public Optional<Drive> currentDrive() { return Optional.ofNullable(currentDrive); }

	public Optional<Plan> currentPlan() { return currentPlan; }

	/** Everything asked for on the last tick, for debugging. */
	public List<Proposal> proposed() { return List.copyOf(proposed); }

	/** A drive and the actions that would serve it. */
	public record Plan(Drive drive, List<GoapAction> actions) {}
}
