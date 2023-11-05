package com.simibubi.create.infrastructure.data;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

import com.simibubi.create.AllDamageTypes;
import com.simibubi.create.Create;
import com.simibubi.create.infrastructure.worldgen.AllBiomeModifiers;
import com.simibubi.create.infrastructure.worldgen.AllConfiguredFeatures;
import com.simibubi.create.infrastructure.worldgen.AllPlacedFeatures;

import io.github.fabricators_of_create.porting_lib.data.DatapackBuiltinEntriesProvider;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.RegistrySetBuilder.RegistryBootstrap;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;

public class GeneratedEntriesProvider extends DatapackBuiltinEntriesProvider {
	@SuppressWarnings({ "rawtypes", "unchecked" }) // fabric: biome modifiers not a registry
	private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
			.add(Registries.DAMAGE_TYPE, AllDamageTypes::bootstrap)
			.add(Registries.CONFIGURED_FEATURE, (RegistryBootstrap) AllConfiguredFeatures::bootstrap)
			.add(Registries.PLACED_FEATURE, AllPlacedFeatures::bootstrap);

	public GeneratedEntriesProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries, BUILDER, Set.of(Create.ID));
	}

	@Override
	public String getName() {
		return "Create's Generated Registry Entries";
	}
}
