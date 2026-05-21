package deus.brainless.pathfinding;

import de.bsommerfeld.pathetic.api.pathing.processing.ValidationProcessor;
import de.bsommerfeld.pathetic.api.pathing.processing.context.EvaluationContext;
import de.bsommerfeld.pathetic.api.wrapper.PathPosition;
import net.minecraft.core.block.tag.BlockTags;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePos;

public class DefaultWalkValidator implements ValidationProcessor {

	private final World world;

	public DefaultWalkValidator(World world) {
		this.world = world;
	}



	@Override
	public boolean isValid(EvaluationContext context) {

		PathPosition pos = context.getCurrentPathPosition();

		TilePos current = new TilePos()
			.set(pos.getX(), pos.getY(), pos.getZ());

		TilePos below = new TilePos()
			.set(pos.getX(), pos.getY() - 1, pos.getZ());

		var currentBlock = world.getBlockType(current);
		var belowBlock = world.getBlockType(below);

		boolean currentSolid = currentBlock.isCollidable();
		boolean floorSolid = belowBlock.isCollidable();

		boolean lava =
			currentBlock.hasTag(BlockTags.IS_LAVA) ||
				belowBlock.hasTag(BlockTags.IS_LAVA);

		boolean water =
			currentBlock.hasTag(BlockTags.IS_WATER) ||
				belowBlock.hasTag(BlockTags.IS_WATER);

		boolean valid =
			!currentSolid &&
				floorSolid &&
				!lava &&
				!water;

		System.out.println(
			"[VALIDATOR] pos=" + current +
				" solid=" + currentSolid +
				" floor=" + floorSolid +
				" water=" + water +
				" lava=" + lava +
				" valid=" + valid
		);

		return valid;
	}
}
