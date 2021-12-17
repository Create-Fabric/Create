package com.simibubi.create.lib.utility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import com.simibubi.create.lib.mixin.accessor.BiomeGenerationSettings$BuilderAccessor;

import net.minecraft.world.level.biome.BiomeGenerationSettings;
import com.simibubi.create.lib.mixin.accessor.BiomeGenerationSettingsAccessor;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;

import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
public class BiomeUtil {
	public static BiomeGenerationSettings.Builder settingsToBuilder(BiomeGenerationSettings settings) {
		BiomeGenerationSettings.Builder builder = new BiomeGenerationSettings.Builder();
		BiomeGenerationSettings$BuilderAccessor builderAccessor = MixinHelper.cast(builder);
		BiomeGenerationSettingsAccessor settingsAccessor = MixinHelper.cast(settings);

		builderAccessor.setCarvers(settingsAccessor.getCarvers());

		List<List<Supplier<PlacedFeature>>> list = new ArrayList<>();
		settings.features().forEach(feature -> list.add(new ArrayList<>(feature)));

		builderAccessor.setFeatures(list);
		return builder;
	}
}
