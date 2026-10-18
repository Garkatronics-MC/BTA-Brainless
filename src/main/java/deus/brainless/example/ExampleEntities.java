package deus.brainless.example;

import net.minecraft.core.entity.EntityDispatcher;
import net.minecraft.core.util.collection.NamespaceID;

/** Registers the example entities. Call once from the mod's afterGameStart. */
public final class ExampleEntities {

	private ExampleEntities() {}

	public static void register() {
		EntityDispatcher.getInstance().addMapping(
			ExampleZombie.class,
			NamespaceID.fromPool("brainless", "example_zombie"),
			ExampleZombie::new
		);
	}
}
