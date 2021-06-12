package com.simibubi.create.lib.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.lib.event.FogEvents;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.vector.Vector3f;

@Environment(EnvType.CLIENT)
@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {

	@Shadow
	private static float red;

	@Shadow
	private static float green;

	@Shadow
	private static float blue;

	@Inject(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;clearColor(FFFF)V"),
			method = "render(Lnet/minecraft/client/renderer/ActiveRenderInfo;FLnet/minecraft/client/world/ClientWorld;IF)V")
	private static void render(ActiveRenderInfo activeRenderInfo, float f, ClientWorld clientWorld, int i, float g, CallbackInfo ci) {
		Vector3f color = FogEvents.SET_COLOR.invoker().setColor(activeRenderInfo, new Vector3f(red, green, blue));
		red = color.getX();
		green = color.getY();
		blue = color.getZ();
	}

	@Inject(at = @At("HEAD"),
			method = "applyFog(Lnet/minecraft/client/renderer/ActiveRenderInfo;Lnet/minecraft/client/renderer/FogRenderer$FogType;FZ)V", cancellable = true)
	private static void applyFog(ActiveRenderInfo activeRenderInfo, FogRenderer.FogType fogType, float f, boolean bl, CallbackInfo ci) {
		float density = FogEvents.SET_DENSITY.invoker().setDensity(activeRenderInfo, 0.1f);
		if (density != 0.1f) {
			RenderSystem.fogDensity(density);
			ci.cancel();
		}
	}
}
