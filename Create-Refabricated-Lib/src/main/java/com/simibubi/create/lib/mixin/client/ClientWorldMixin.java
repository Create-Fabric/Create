package com.simibubi.create.lib.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.lib.event.ClientWorldEvents;
import com.simibubi.create.lib.utility.MixinHelper;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;

@Environment(EnvType.CLIENT)
@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin {
	@Shadow
	@Final
	private Minecraft mc;

	@Inject(at = @At("TAIL"), method = "<init>(Lnet/minecraft/client/network/play/ClientPlayNetHandler;Lnet/minecraft/client/world/ClientWorld$ClientWorldInfo;Lnet/minecraft/util/RegistryKey;Lnet/minecraft/world/DimensionType;ILjava/util/function/Supplier;Lnet/minecraft/client/renderer/WorldRenderer;ZJ)V")
	public void create$onTailInit(CallbackInfo ci) {
		ClientWorldEvents.LOAD.invoker().onWorldLoad(mc, MixinHelper.cast(this));
	}
}
