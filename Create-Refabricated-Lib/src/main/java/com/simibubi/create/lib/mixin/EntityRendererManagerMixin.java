package com.simibubi.create.lib.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.lib.event.OnPlayerRendererInitCallback;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.GameSettings;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.IReloadableResourceManager;

@Environment(EnvType.CLIENT)
@Mixin(EntityRendererManager.class)
public abstract class EntityRendererManagerMixin {
	@Inject(at = @At("RETURN"), method = "<init>")
	public void create$EntityRendererManager(TextureManager textureManager, ItemRenderer itemRenderer, IReloadableResourceManager iReloadableResourceManager, FontRenderer fontRenderer, GameSettings gameSettings, CallbackInfo ci) {
		OnPlayerRendererInitCallback.EVENT.invoker().register();
	}
}
