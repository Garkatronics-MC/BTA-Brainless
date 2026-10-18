package deus.brainless.pathfinding;

import net.minecraft.core.entity.Mob;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * A {@link Mob} that walks where it is told. All thinking lives in
 * {@link MobNavigator}; tunables are forwarded to it.
 */
public abstract class MobPathfinder extends Mob {

	private final MobNavigator navigator = new MobNavigator(this);

	public MobPathfinder(@NotNull World world) {
		super(world);
	}

	protected MobNavigator navigator() {
		return navigator;
	}

	@Override
	protected void updateAI() {
		super.updateAI();
		applyIntent(navigator().think());
	}

	protected void applyIntent(MobNavigator.Intent intent) {
		this.yRot = intent.yRot();
		this.moveForward = this.moveSpeed * intent.forward();
		this.isJumping = intent.jumping();
	}

	// ------------------------------------------------------------------ facade

	public void setTarget(@Nullable TilePos target) {
		navigator().setTarget(target);
	}

	public Optional<TilePos> getTargetTilePos() {
		return navigator().getTargetTilePos();
	}

	public void clearTarget() {
		navigator().clearTarget();
	}

	public boolean hasPath() {
		return navigator().hasPath();
	}

	public boolean isMoving() {
		return navigator().isMoving();
	}
}
