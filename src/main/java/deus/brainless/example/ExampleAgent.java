package deus.brainless.example;

import deus.brainless.agent.Agent;
import deus.brainless.agent.AgentMob;
import deus.brainless.agent.DriveGoals;
import deus.brainless.agent.Goal;
import deus.brainless.drive.Drive;
import deus.brainless.drive.Drives;
import deus.brainless.task.behaviour.Attack;
import deus.brainless.task.behaviour.Flee;
import deus.brainless.task.behaviour.Approach;
import deus.brainless.task.behaviour.Recover;
import deus.brainless.task.behaviour.Wander;
import deus.brainless.goap.GoapAction;
import deus.brainless.goap.GoapPlanner;
import deus.brainless.goap.arbitration.ArbiterConfig;
import deus.brainless.goap.arbitration.GoapArbiter;
import deus.brainless.goap.arbitration.HighestPriorityProposalSelector;
import deus.brainless.limbic.Psyche;
import deus.brainless.limbic.Temperament;
import deus.brainless.task.Task;
import deus.brainless.task.TaskFactory;
import deus.brainless.task.Tasks;
import deus.brainless.utils.Timer;

import java.util.Optional;

/** Wires the zombie's temperament, psyche, goals, planner and tasks into one {@link Agent}. */
public final class ExampleAgent {

	public static final Drive WANDER = Drive.of("wander");

	private static final Temperament TEMPERAMENT = buildTemperament();
	private static final GoapPlanner PLANNER = buildPlanner();
	private static final DriveGoals GOALS = buildGoals();

	private ExampleAgent() {}

	public static Agent build(AgentMob mob) {
		Psyche psyche = new Psyche(TEMPERAMENT);
		GoapArbiter arbiter = new GoapArbiter(
			new HighestPriorityProposalSelector(),
			new ArbiterConfig(0.0, WANDER)
		);
		TaskFactory<GoapAction> tasks = action -> {
			Task task = switch (action.id()) {
				case "approach" -> new Approach(mob);
				case "strike"   -> new Attack(mob);
				case "retreat"  -> new Flee(mob);
				case "recover"  -> new Recover(mob, 60);
				case "wander"   -> new Wander(mob);
				default         -> Tasks.waitFor(1);
			};
			ExampleLog.LOGGER.info("[example] action={} -> {}", action.id(), task.getClass().getSimpleName());
			return task;
		};
		return new Agent(
			psyche,
			arbiter,
			PLANNER,
			GOALS,
			tasks,
			mob.controller(),
			Timer.fromTicks(3)
		);
	}

	static Temperament buildTemperament() {
		Temperament t = new Temperament();
		t.inputs(in -> in
			.declare("health", 1.0)
			.declare("targetDist", 1.0));

		// Mixes must live in a layer: probe() only computes layers, so a mix
		// declared with t.mix() outside one is never evaluated.
		t.layer("drives", layer -> {
			// proximity = 1 - targetDist
			layer.mix("proximity")
				.bias(1.0)
				.sub("targetDist", 1.0);

			// anger = proximity * health: close and healthy -> attack.
			layer.mix("anger")
				.bias(1.0)
				.mul("proximity", 1.0)
				.mul("health", 1.0)
				.onHigh(Drives.ANGER);

			// weak = 1 - health
			layer.mix("weak")
				.bias(1.0)
				.sub("health", 1.0);

			// fear = proximity * weak: close and hurt -> flee.
			layer.mix("fear")
				.bias(1.0)
				.mul("proximity", 1.0)
				.mul("weak", 1.0)
				.onHigh(Drives.FEAR);
		});

		return t;
	}

	private static GoapPlanner buildPlanner() {
		return new GoapPlanner()
			.addAction(new GoapAction("approach",
				s -> s.get("targetDist") > 0.05f,
				s -> s.set("targetDist", Math.max(0f, s.get("targetDist") - 0.4f)),
				s -> 1.0))
			.addAction(new GoapAction("strike",
				s -> s.get("targetDist") <= 0.05f && s.get("attacked") < 1f,
				s -> s.set("attacked", 1f),
				s -> 2.0))
			.addAction(new GoapAction("retreat",
				s -> s.get("fled") < 1f,
				s -> s.set("fled", 1f),
				s -> 1.0))
			.addAction(new GoapAction("recover",
				s -> s.get("fled") >= 1f && s.get("safe") < 1f,
				s -> s.set("safe", 1f),
				s -> 3.0))
			.addAction(new GoapAction("wander",
				s -> s.get("wandered") < 1f,
				s -> s.set("wandered", 1f),
				s -> 1.0));
	}

	private static DriveGoals buildGoals() {
		return (drive, world) -> {
			Optional<Goal> goal;
			if (drive == Drives.ANGER) goal = Optional.of(Goal.of(s -> s.get("attacked") >= 1f, world));
			else if (drive == Drives.FEAR) goal = Optional.of(Goal.of(s -> s.get("safe") >= 1f, world));
			else if (drive == WANDER) goal = Optional.of(Goal.of(s -> s.get("wandered") >= 1f, world));
			else goal = Optional.empty();

			ExampleLog.LOGGER.info("[example] drive={} goal={}", drive.id(), goal.isPresent());
			return goal;
		};
	}
}
