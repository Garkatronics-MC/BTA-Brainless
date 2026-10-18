package deus.brainless.example;

import net.minecraft.client.render.EntityRendererDispatcher;
import net.minecraft.client.render.TileEntityRenderDispatcher;
import net.minecraft.client.render.block.color.BlockColorDispatcher;
import net.minecraft.client.render.block.model.BlockModelDispatcher;
import net.minecraft.client.render.item.model.ItemModelDispatcher;
import turniplabs.halplibe.util.ModelEntrypoint;

/** Gives the example zombie a body through halplibe's {@code initModels} entrypoint. */
public class ExampleModels implements ModelEntrypoint {

	private static final String ZOMBIE_MODEL = "geometry.zombie";

	@Override
	public void initEntityModels(EntityRendererDispatcher dispatcher) {
		dispatcher.assignRenderer(ExampleZombie.class, new ExampleMobRenderer<>(ZOMBIE_MODEL, 0.5f));
		ExampleLog.LOGGER.info("[example] renderer assigned to ExampleZombie using model {}",
			ZOMBIE_MODEL);
	}

	@Override
	public void initBlockModels(BlockModelDispatcher dispatcher) {}

	@Override
	public void initItemModels(ItemModelDispatcher dispatcher) {}

	@Override
	public void initTileEntityModels(TileEntityRenderDispatcher dispatcher) {}

	@Override
	public void initBlockColors(BlockColorDispatcher dispatcher) {}
}
