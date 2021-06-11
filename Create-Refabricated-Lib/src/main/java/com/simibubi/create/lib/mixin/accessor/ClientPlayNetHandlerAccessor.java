package com.simibubi.create.lib.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.play.ClientPlayNetHandler;

@Environment(EnvType.CLIENT)
@Mixin(ClientPlayNetHandler.class)
public interface ClientPlayNetHandlerAccessor {
	@Accessor("viewDistance")
	int create$viewDistance();
}
