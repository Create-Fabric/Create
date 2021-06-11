package com.simibubi.create.lib.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.renderer.RenderSkybox;

@Environment(EnvType.CLIENT)
@Mixin(MainMenuScreen.class)
public interface MainMenuScreenAccessor {
	@Accessor("panorama")
	RenderSkybox getPanorama();
}
