package deus.brainless.pathfinding;

import de.bsommerfeld.pathetic.api.pathing.processing.ValidationProcessor;
import de.bsommerfeld.pathetic.api.pathing.processing.context.EvaluationContext;
import de.bsommerfeld.pathetic.api.wrapper.PathPosition;
import deus.brainless.Brainless;
import net.minecraft.core.block.tag.BlockTags;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePos;

/**
 * Decides whether a mob could stand at a point on the path: headroom plus a
 * floor under the feet block, not the head block, so a single step up stays
 * walkable.
 */
public class DefaultWalkValidator implements ValidationProcessor {

	private final World world;

	public DefaultWalkValidator(World world) {
		this.world = world;
	}

	@Override
	public boolean isValid(EvaluationContext context) {
		PathPosition pos = context.getCurrentPathPosition();

		// The position is where the feet go, so the floor is one below and the
		// head is one above. Both are read from the floored point: a path
		// position can arrive mid-block.
		TilePos feet = blockAt(pos);
		TilePos floor = below(feet);
		TilePos head = above(feet);

		boolean solidFloor = isSolid(world.getBlockType(floor));
		boolean bodyBlocked = isSolid(world.getBlockType(feet)) || isSolid(world.getBlockType(head));

		boolean lava =
			world.getBlockType(feet).hasTag(BlockTags.IS_LAVA) ||
				world.getBlockType(floor).hasTag(BlockTags.IS_LAVA);
		boolean water =
			world.getBlockType(feet).hasTag(BlockTags.IS_WATER) ||
				world.getBlockType(floor).hasTag(BlockTags.IS_WATER);

		boolean valid = solidFloor && !bodyBlocked && !lava && !water;

		Brainless.LOGGER.trace(
			"[VALIDATOR] feet={} floorSolid={} blocked={} water={} lava={} valid={}",
			feet, solidFloor, bodyBlocked, water, lava, valid
		);

		return valid;
	}

	private static TilePos blockAt(PathPosition pos) {
		return new TilePos().set(pos.getX(), pos.getY(), pos.getZ());
	}

	private static TilePos below(TilePos pos) {
		return new TilePos().set(pos.x, pos.y - 1, pos.z);
	}

	private static TilePos above(TilePos pos) {
		return new TilePos().set(pos.x, pos.y + 1, pos.z);
	}

	private static boolean isSolid(net.minecraft.core.block.Block<?> block) {
		return block.isCollidable();
	}
}
