package com.simibubi.create.lib.extensions;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.settings.KeyBinding;

@Environment(EnvType.CLIENT)
public interface GameSettingsExtensions {
	void addKey(KeyBinding key);
}
