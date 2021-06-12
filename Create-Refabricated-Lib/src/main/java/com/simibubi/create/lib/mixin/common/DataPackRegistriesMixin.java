package com.simibubi.create.lib.mixin.common;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.lib.event.DataPackReloadCallback;
import com.simibubi.create.lib.utility.MixinHelper;

import net.minecraft.command.Commands;
import net.minecraft.resources.DataPackRegistries;
import net.minecraft.resources.IFutureReloadListener;
import net.minecraft.resources.IReloadableResourceManager;

@Mixin(DataPackRegistries.class)
public abstract class DataPackRegistriesMixin {
	@Shadow
	@Final
	private IReloadableResourceManager resourceManager;

	@Inject(at = @At("TAIL"),
			method = "<init>(Lnet/minecraft/command/Commands$EnvironmentType;I)V")
	public void create$DataPackRegistries(Commands.EnvironmentType environmentType, int i, CallbackInfo ci) {
		for (IFutureReloadListener listener : DataPackReloadCallback.EVENT.invoker().onDataPackReload(MixinHelper.cast(this))) {
			resourceManager.addReloadListener(listener);
		}
	}
}
