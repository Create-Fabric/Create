package com.simibubi.create.lib.mixin.client;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.lib.event.ModelsBakedCallback;
import com.simibubi.create.lib.extensions.ModelManagerExtensions;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.model.ModelManager;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

@Environment(EnvType.CLIENT)
@Mixin(ModelManager.class)
public abstract class ModelManagerMixin implements ModelManagerExtensions {
	@Shadow
	private Map<ResourceLocation, IBakedModel> modelRegistry;

	@Inject(at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/profiler/IProfiler;endStartSection(Ljava/lang/String;)V", args = "ldc=cache", shift = At.Shift.BEFORE), method = "apply(Lnet/minecraft/client/renderer/model/ModelBakery;Lnet/minecraft/resources/IResourceManager;Lnet/minecraft/profiler/IProfiler;)V")
	public void create$onModelBake(ModelBakery modelLoader, IResourceManager resourceManager, IProfiler profiler, CallbackInfo ci) {
		ModelsBakedCallback.EVENT.invoker().onModelsBaked((ModelManager) (Object) this, modelRegistry, modelLoader);
	}

	@Override
	public IBakedModel create$getModel(ResourceLocation id) {
		return modelRegistry.get(id);
	}
}
