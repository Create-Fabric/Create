package com.simibubi.create.foundation.config;

import com.simibubi.create.lib.config.Config;
import com.simibubi.create.lib.config.ConfigGroup;
import com.simibubi.create.lib.config.Configs;

public class CCommon extends ConfigBase {

	public static CWorldGen worldGen;

	public static void register() {
		worldGen = nested(0, CWorldGen::new, Comments.worldGen);
		worldGen.registerAll();
		initGroups(worldGen.getConfig());
		worldGen.getConfig().init();
	}

	public ConfigGroup common = group(0, "common", "Configs used for both Client and Server");

	public Config config = new Config(getName());
	@Override
	public Config getConfig() {
		return config;
	}

	@Override
	public String getName() {
		return "common";
	}

	private static class Comments {
		static String worldGen = "Modify Create's impact on your terrain";
	}

}
