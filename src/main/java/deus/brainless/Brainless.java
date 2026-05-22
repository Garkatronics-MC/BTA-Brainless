package deus.brainless;

import deus.brainless.ai.AI;
import deus.brainless.ai.jobs.InlineJob;
import deus.brainless.ai.jobs.JobDefinition;
import deus.brainless.ai.jobs.schedulers.PersistentJobScheduler;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.entity.animal.MobSheep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import turniplabs.halplibe.HalpLibe;
import turniplabs.halplibe.util.GameStartEntrypoint;
import turniplabs.halplibe.util.RecipeEntrypoint;

public class Brainless implements ModInitializer, GameStartEntrypoint, RecipeEntrypoint {
	public static final String MOD_ID = HalpLibe.registerMod("brainless", true);
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Brainless initialized.");


	}

	@Override
	public void beforeGameStart() {

	}

	@Override
	public void afterGameStart() {

	}

	@Override
	public void onRecipesReady() {

	}

	@Override
	public void initNamespaces() {

	}
}
