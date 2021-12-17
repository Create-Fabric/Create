package com.simibubi.create.lib.mixin.accessor;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;

@Mixin(BiomeGenerationSettings.Builder.class)
public interface BiomeGenerationSettings$BuilderAccessor {

	@Accessor("carvers")
	Map<GenerationStep.Carving, List<Supplier<ConfiguredWorldCarver<?>>>> getCarvers();

	@Accessor("carvers")
	@Mutable
	void setCarvers(Map<GenerationStep.Carving, List<Supplier<ConfiguredWorldCarver<?>>>> carvers);

	@Accessor("features")
	List<List<Supplier<PlacedFeature>>> getFeatures();

	@Accessor("features")
	@Mutable
	void setFeatures(List<List<Supplier<PlacedFeature>>> features);

}
