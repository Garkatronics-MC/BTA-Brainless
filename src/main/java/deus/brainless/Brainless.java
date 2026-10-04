package deus.brainless;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import turniplabs.halplibe.HalpLibe;
import turniplabs.halplibe.event.defs.CommonEvents;
import turniplabs.halplibe.util.dependency.Key;

public class Brainless implements ModInitializer{
	public static final String MOD_ID = HalpLibe.registerMod("brainless", true);
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static ModConfig CONFIG = new ModConfig();

	@Override
	public void onInitialize() {
		LOGGER.info("Brainless initialized.");

		CommonEvents.BEFORE_GAME_START.listen(Key.of(MOD_ID), this::beforeGameStart);
		CommonEvents.AFTER_GAME_START.listen(Key.of(MOD_ID), this::afterGameStart);

	}

	public void beforeGameStart() {

	}

	public void afterGameStart() {
	}
}
