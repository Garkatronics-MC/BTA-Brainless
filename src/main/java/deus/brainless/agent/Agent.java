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
 * One tick: senses -> drives -> plan -> tasks on the controller.
 * Planning and doing stay separate — a {@link GoapAction} says what would be
 * true after it; the {@link Task} touches the real world.
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
	 * @param tasks provides a fresh {@link Task} per action when a plan starts
	 * @param timer gates how often {@link #update} may re-think
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


	public Agent sense(String node, double value) {
		psyche.set(node, value);
		return this;
	}

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

	public void finishCurrent() {
		currentDrive = null;
		currentPlan = Optional.empty();
		tc.clear();
	}


	private Task sequenceFor(Plan plan) {
		List<Supplier<Task>> steps = new ArrayList<>(plan.actions().size());
		for (GoapAction action : plan.actions()) {
			steps.add(() -> tasks.create(action));
		}
		return new Sequence(steps);
	}

	public Psyche psyche() { return psyche; }


	public Optional<Drive> currentDrive() { return Optional.ofNullable(currentDrive); }

	public Optional<Plan> currentPlan() { return currentPlan; }

	public List<Proposal> proposed() { return List.copyOf(proposed); }

	public record Plan(Drive drive, List<GoapAction> actions) {}
}
