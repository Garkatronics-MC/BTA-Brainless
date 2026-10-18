package deus.brainless.agent;

import deus.brainless.drive.Drive;
import deus.brainless.drive.Proposal;
import deus.brainless.goap.GoapAction;
import deus.brainless.goap.GoapPlanner;
import deus.brainless.goap.GoapState;
import deus.brainless.goap.arbitration.GoapArbiter;
import deus.brainless.limbic.Psyche;
import deus.brainless.task.Task;
import deus.brainless.task.TaskController;
import deus.brainless.task.TaskFactory;
import deus.brainless.task.TaskRun;
import deus.brainless.task.flow.Sequence;
import deus.brainless.utils.Timer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Glue between psyche, arbiter, planner and task controller. One tick is:
 *
 * <pre>
 * senses -&gt; urges -&gt; drives -&gt; proposals -&gt; arbitration -&gt; wanted drive
 *                                                        |
 *                                        already served -+&gt; nothing to do
 *                                                        |
 *                                    goal -&gt; plan -&gt; tasks on the controller
 * </pre>
 *
 * <p>Top of the stack: depends on everything, nothing depends on it.
 *
 * <p>Planning and doing stay separate. A {@link GoapAction} says what would be
 * true after it, in the planner's copy of the world; the {@link Task} for that
 * same action is what touches the real one. Both are written by the caller and
 * glued here through {@link TaskFactory}, never by making an action also a task.
 *
 * <p>The arbiter is only asked what is wanted, never whether to abandon it. How
 * committed an agent is is answered by {@code Task.interruptible()}, not here.
 * This class never marks itself busy with a plan the controller refused.
 */
public final class Agent {

	private final Psyche psyche;
	private final GoapArbiter arbiter;
	private final GoapPlanner planner;
	private final DriveGoals goals;
	private final TaskFactory<GoapAction> tasks;
	private final TaskController tc;
	private final Timer updateTimer;

	private final List<Proposal> proposed = new ArrayList<>();
	private Drive currentDrive;
	private Optional<Plan> currentPlan = Optional.empty();

	/**
	 * @param tasks turns a planned action into something that runs. Asked for a
	 *              fresh, never-null {@link Task} each time a plan starts, so a
	 *              plan that is retried never inherits the last one's state.
	 * @param timer  how often {@link #update} is allowed to think; the world
	 *              still moves every tick
	 */
	public Agent(Psyche psyche,
	             GoapArbiter arbiter,
	             GoapPlanner planner,
	             DriveGoals goals,
	             TaskFactory<GoapAction> tasks,
	             TaskController tc,
	             Timer timer) {
		this.psyche = psyche;
		this.arbiter = arbiter;
		this.planner = planner;
		this.goals = goals;
		this.tasks = tasks;
		this.tc = tc;
		this.updateTimer = timer;

		tc.onFinished(run -> {
			if (run.origin() == currentDrive) finishCurrent();
		});
	}

	/** Feeds what the world currently looks like. Called every tick, before {@link #update}. */
	public Agent sense(String node, double value) {
		psyche.set(node, value);
		return this;
	}

	/**
	 * Moves one tick forward; if a new drive won, puts its plan on the
	 * controller. The controller itself is ticked by whoever owns the game loop.
	 *
	 * @param world the planner's world as it is now — facts, not feelings
	 * @return the plan just started, or empty if there is nothing new to do
	 */
	public Optional<Plan> update(GoapState world) {
		updateTimer.tick();
		if (!updateTimer.hasElapsed()) return Optional.empty();

		proposed.clear();
		psyche.probe(proposed::add);
		proposed.forEach(arbiter::propose);

		Optional<Drive> wanted = arbiter.resolve();
		if (wanted.isEmpty()) return Optional.empty();

		if (wanted.get().equals(currentDrive)) return Optional.empty();

		Optional<Goal> goal = goals.goalFor(wanted.get(), world);
		if (goal.isEmpty()) return Optional.empty();

		Optional<List<GoapAction>> actions = planner.compute(goal.get().satisfied(), goal.get().heuristic(), goal.get().start());

		if (actions.isEmpty()) return Optional.empty();

		Plan plan = new Plan(wanted.get(), List.copyOf(actions.get()));

		if (!tc.setCurrent(new TaskRun<>(sequenceFor(plan), plan.drive()))) return Optional.empty();

		currentDrive = plan.drive();
		currentPlan = Optional.of(plan);
		return currentPlan;
	}

	/**
	 * Forgets what was being pursued, so the next tick plans afresh, and stops
	 * whatever it was doing. Automatic when the activity ends or fails; call it
	 * by hand to abandon a plan that turned out to be a mistake.
	 */
	public void finishCurrent() {
		currentDrive = null;
		currentPlan = Optional.empty();
		tc.clear();
	}

	/** The plan as a sequence of steps, each asking the factory afresh. */
	private Task sequenceFor(Plan plan) {
		List<Supplier<Task>> steps = new ArrayList<>(plan.actions().size());
		for (GoapAction action : plan.actions()) {
			steps.add(() -> tasks.create(action));
		}
		return new Sequence(steps);
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
