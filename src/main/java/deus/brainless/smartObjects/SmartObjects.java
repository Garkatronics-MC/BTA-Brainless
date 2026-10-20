package deus.brainless.smartObjects;

import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePos;

import java.util.ArrayList;
import java.util.List;

/**
 * Finds the {@link Interactable} things near a point — the smart objects an
 * agent can act on. Two sources are searched: block entities (furnaces, chests,
 * workbenches…) and regular entities (mobs, dropped items…).
 */
public final class SmartObjects {
	private SmartObjects() {}

	/** Every smart object within {@code radius} of the centre tile. */
	public static List<Interactable> find(World world, TilePos center, double radius) {
		return find(world, center.x, center.y, center.z, radius);
	}

	/** Every smart object within {@code radius} of the centre point. */
	public static List<Interactable> find(World world, double x, double y, double z, double radius) {
		List<Interactable> found = new ArrayList<>(findInBlocks(world, x, y, z, radius));
		found.addAll(findAmongEntities(world, x, y, z, radius));
		return found;
	}

	/** Smart objects that live in blocks, within {@code radius} of the centre tile. */
	public static List<Interactable> findInBlocks(World world, TilePos center, double radius) {
		return findInBlocks(world, center.x, center.y, center.z, radius);
	}

	/** Smart objects that live in blocks, within {@code radius} of the centre point. */
	public static List<Interactable> findInBlocks(World world, double x, double y, double z, double radius) {
		List<Interactable> found = new ArrayList<>();
		for (TileEntity tileEntity : world.getLoadedTileEntityList()) {
			if (tileEntity instanceof Interactable interactable && tileEntity.getDistanceFrom(x, y, z) <= radius)
				found.add(interactable);
		}
		return found;
	}

	/** Smart objects that live as entities, within {@code radius} of the centre tile. */
	public static List<Interactable> findAmongEntities(World world, TilePos center, double radius) {
		return findAmongEntities(world, center.x, center.y, center.z, radius);
	}

	/** Smart objects that live as entities, within {@code radius} of the centre point. */
	public static List<Interactable> findAmongEntities(World world, double x, double y, double z, double radius) {
		List<Interactable> found = new ArrayList<>();
		for (Entity entity : world.getEntitiesWithinRadius(Entity.class, x, y, z, radius)) {
			if (entity instanceof Interactable interactable) found.add(interactable);
		}
		return found;
	}
}
