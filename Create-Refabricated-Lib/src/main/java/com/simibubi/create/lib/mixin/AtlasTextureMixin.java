package com.simibubi.create.lib.mixin;

import java.util.Set;
import java.util.stream.Stream;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.simibubi.create.lib.event.OnTextureStitchCallback;
import com.simibubi.create.lib.utility.MixinHelper;
import com.simibubi.create.lib.utility.TextureStitchUtil;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

@Environment(EnvType.CLIENT)
@Mixin(AtlasTexture.class)
public abstract class AtlasTextureMixin {
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/IProfiler;endStartSection(Ljava/lang/String;)V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILEXCEPTION,
			method = "stitch(Lnet/minecraft/resources/IResourceManager;Ljava/util/stream/Stream;Lnet/minecraft/profiler/IProfiler;I)Lnet/minecraft/client/renderer/texture/AtlasTexture$SheetData;")
	public void create$stitch(IResourceManager iResourceManager, Stream<ResourceLocation> stream, IProfiler iProfiler, int i, CallbackInfoReturnable<AtlasTexture.SheetData> cir, Set<ResourceLocation> set) {
		OnTextureStitchCallback.EVENT.invoker().onModelRegistry(new TextureStitchUtil(MixinHelper.cast(this), set));
	}
}
