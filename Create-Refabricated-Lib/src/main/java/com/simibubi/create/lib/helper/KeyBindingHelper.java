package com.simibubi.create.lib.helper;

import com.simibubi.create.lib.mixin.accessor.KeyBindingAccessor;
import com.simibubi.create.lib.utility.MixinHelper;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;

@Environment(EnvType.CLIENT)
public final class KeyBindingHelper {
	public static InputMappings.Input getKeyCode(KeyBinding keyBinding) {
		return get(keyBinding).create$keyCode();
	}

	private static KeyBindingAccessor get(KeyBinding keyBinding) {
		return MixinHelper.cast(keyBinding);
	}

	private KeyBindingHelper() { }
}
