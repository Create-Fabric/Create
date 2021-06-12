package com.simibubi.create.lib.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.lib.event.KeyInputCallback;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyboardListener;

@Environment(EnvType.CLIENT)
@Mixin(KeyboardListener.class)
public abstract class KeyboardListenerMixin {
	// First return opcode is jumped over if condition is met.
	@Inject(slice = @Slice(from = @At(value = "RETURN", ordinal = 0, shift = Shift.AFTER)), at = @At(value = "RETURN"), method = "onKeyEvent(JIIII)V")
	public void create$onHandleKeyInput(long window, int key, int scancode, int action, int mods, CallbackInfo ci) {
		KeyInputCallback.EVENT.invoker().onKeyInput(key, scancode, action, mods);
	}
}
