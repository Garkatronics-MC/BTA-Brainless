package deus.brainless.example;

import deus.brainless.agent.Agent;
import deus.brainless.agent.AgentMob;
import deus.brainless.goap.GoapState;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.util.helper.DamageType;
import net.minecraft.core.world.World;

/** One concrete mob driven by Brainless: {@link AgentMob} plus its three answers. */
public class ExampleZombie extends AgentMob {

	private static final int STRIKE_DAMAGE = 3;

	public ExampleZombie(World world) {
		super(world);
	}

	@Override
	protected Agent createAgent() {
		return ExampleAgent.build(this);
	}

	@Override
	protected Entity acquireTarget() {
		Entity closest = null;
		double closestDistance = Double.MAX_VALUE;

		for (Player player : world.players) {
			if (!player.isAlive()) continue;
			double distance = distanceTo(player);
			if (distance < closestDistance) {
				closestDistance = distance;
				closest = player;
			}
		}
		return closest;
	}

	@Override
	protected void strike(Entity target, float distance) {
		target.hurt(this, STRIKE_DAMAGE, DamageType.COMBAT);
	}

	@Override
	protected GoapState worldState() {
		GoapState state = super.worldState();
		state.set("attacked", 0f);
		state.set("fled", 0f);
		state.set("safe", 0f);
		state.set("wandered", 0f);
		return state;
	}
}
