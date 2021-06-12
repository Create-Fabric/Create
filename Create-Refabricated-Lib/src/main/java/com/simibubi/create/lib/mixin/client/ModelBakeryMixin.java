package com.simibubi.create.lib.mixin.client;

import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.lib.event.OnModelRegistryCallback;
import com.simibubi.create.lib.utility.SpecialModelUtil;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

@Environment(EnvType.CLIENT)
@Mixin(ModelBakery.class)
public abstract class ModelBakeryMixin {
	@Final
	@Shadow
	private Map<ResourceLocation, IUnbakedModel> field_217849_F;
	@Final
	@Shadow
	private Map<ResourceLocation, IUnbakedModel> field_217851_H;

	@Shadow
	public abstract IUnbakedModel getUnbakedModel(ResourceLocation p_209597_1_);

	private void create$addModelToCache(ResourceLocation p_217843_1_) {
		IUnbakedModel iunbakedmodel = this.getUnbakedModel(p_217843_1_);
		this.field_217849_F.put(p_217843_1_, iunbakedmodel);
		this.field_217851_H.put(p_217843_1_, iunbakedmodel);
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/IProfiler;startSection(Ljava/lang/String;)V", shift = At.Shift.BEFORE),
			method = "<init>(Lnet/minecraft/resources/IResourceManager;Lnet/minecraft/client/renderer/color/BlockColors;Lnet/minecraft/profiler/IProfiler;I)V")
	public void create$onModelRegistry(IResourceManager iResourceManager, BlockColors blockColors, IProfiler iProfiler, int i, CallbackInfo ci) {
		OnModelRegistryCallback.EVENT.invoker().onModelRegistry();
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/IProfiler;endStartSection(Ljava/lang/String;)V", shift = At.Shift.BEFORE, ordinal = 4),
			method = "<init>(Lnet/minecraft/resources/IResourceManager;Lnet/minecraft/client/renderer/color/BlockColors;Lnet/minecraft/profiler/IProfiler;I)V")
	public void create$registerSpecialModels(IResourceManager iResourceManager, BlockColors blockColors, IProfiler iProfiler, int i, CallbackInfo ci) {
		for (ResourceLocation location : SpecialModelUtil.specialModels) {
			create$addModelToCache(location);
		}
	}
}
