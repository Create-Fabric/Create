package com.simibubi.create.lib.mixin;

import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.lib.helper.KeyBindingHelper;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.GameSettings;
import net.minecraft.client.settings.KeyBinding;

@Environment(EnvType.CLIENT)
@Mixin(GameSettings.class)
public class GameSettingsMixin {
	@Mutable
	@Shadow
	@Final
	public KeyBinding[] keyBindings;

	@Inject(at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lorg/apache/commons/lang3/ArrayUtils;addAll([Ljava/lang/Object;[Ljava/lang/Object;)[Ljava/lang/Object;"), method = "<init>")
	public void loadHook(CallbackInfo info) {
		for (KeyBinding binding : KeyBindingHelper.keysToAdd) {
			keyBindings = ArrayUtils.add(keyBindings, binding);
		}
	}
}
