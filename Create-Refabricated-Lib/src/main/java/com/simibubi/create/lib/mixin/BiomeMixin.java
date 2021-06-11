package com.simibubi.create.lib.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.lib.event.BiomeLoadingCallback;
import com.simibubi.create.lib.utility.BiomeUtil;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeAmbience;
import net.minecraft.world.biome.BiomeGenerationSettings;
import net.minecraft.world.biome.MobSpawnInfo;

@Mixin(Biome.class)
public abstract class BiomeMixin {
	@Mutable
	@Shadow
	@Final
	private BiomeGenerationSettings generationSettings;

	@Inject(at = @At("TAIL"), method = "<init>(Lnet/minecraft/world/biome/Biome$Climate;Lnet/minecraft/world/biome/Biome$Category;FFLnet/minecraft/world/biome/BiomeAmbience;Lnet/minecraft/world/biome/BiomeGenerationSettings;Lnet/minecraft/world/biome/MobSpawnInfo;)V")
	public void create$biomeInit(Biome.Climate climate, Biome.Category category, float f, float g, BiomeAmbience biomeAmbience, BiomeGenerationSettings biomeGenerationSettings, MobSpawnInfo mobSpawnInfo, CallbackInfo ci) {
		ResourceLocation key = WorldGenRegistries.BIOME.getKey((Biome) (Object) this); // dunno
		this.generationSettings = BiomeLoadingCallback.EVENT.invoker().onBiomeLoad(key, category, BiomeUtil.settingsToBuilder(biomeGenerationSettings)).build();
	}
}
