package deus.brainless;

import turniplabs.halplibe.util.TomlConfigHandler;
import turniplabs.halplibe.util.toml.Toml;

import java.util.Locale;

import static deus.brainless.Brainless.MOD_ID;

public class ModConfig {


	public static final TomlConfigHandler TOML_CONFIG;

	static {
		Toml toml = new Toml(MOD_ID.toUpperCase(Locale.ROOT));

		toml.addCategory("Generic")
			.addEntry("url", "https://api.typesafe.ai/v1/systemone")
			.addEntry("api_key", "");

		toml.addCategory("Typesafe")
			.addEntry("url", "https://api.typesafe.ai/v1/systemone")
			.addEntry("api_key", "");



		TOML_CONFIG = new TomlConfigHandler(MOD_ID, toml);

	}


	public TomlConfigHandler getConfig() {
		return TOML_CONFIG;
	}

}
