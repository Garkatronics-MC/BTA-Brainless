package deus.brainless.example;

import deus.brainless.agent.AgentMob;
import net.minecraft.client.render.entity.MobRendererBiped;
import org.useless.dragonfly.models.entity.StaticEntityModel;

/** Draws an {@link AgentMob} borrowing vanilla mob geometry. */
public class ExampleMobRenderer<T extends AgentMob> extends MobRendererBiped<T> {

	private static final String MAIN = "main";

	private final String model;

	public ExampleMobRenderer(String modelId, float shadowRadius) {
		super(shadowRadius);
		this.model = modelId;
		setModel(MAIN, modelId, 1.0D);
	}

	public String modelId() {
		return model;
	}

	@Override
	protected StaticEntityModel getActiveModel(T entity) {
		return getModel(MAIN);
	}
}
