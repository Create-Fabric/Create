package com.simibubi.create.foundation.worldgen;

import java.util.Optional;

import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.config.ConfigBase;
import com.simibubi.create.lib.config.Config;
import com.simibubi.create.lib.config.ConfigGroup;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import net.minecraft.block.Block;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.OreFeatureConfig.FillerBlockType;

public class ConfigDrivenFeatureEntry extends ConfigBase {

	public final String id;
	public final NonNullSupplier<? extends Block> block;

	protected ConfigInt clusterSize;
	protected ConfigInt minHeight;
	protected ConfigInt maxHeight;
	protected ConfigFloat frequency;

	Optional<ConfiguredFeature<?, ?>> feature = Optional.empty();

	public ConfigDrivenFeatureEntry(String id, NonNullSupplier<? extends Block> block, int clusterSize,
									float frequency) {
		this.id = id;
		this.block = block;
		this.clusterSize = i(clusterSize, 0, id + "_clusterSize");
		this.minHeight = i(0, 0, id + "_minHeight");
		this.maxHeight = i(256, 0, id + "_maxHeight");
		this.frequency = f(frequency, 0, 512, id + "_frequency", "Amount of clusters generated per Chunk.",
				"  >1 to spawn multiple.", "  <1 to make it a chance.", "  0 to disable.");
	}

	public ConfigDrivenFeatureEntry between(int minHeight, int maxHeight) {
//		allValues.remove(this.minHeight);
//		allValues.remove(this.maxHeight);
//		this.minHeight = i(minHeight, 0, id + "_minHeight");
//		this.maxHeight = i(maxHeight, 0, id + "_maxHeight");
		this.minHeight.set(minHeight);
		this.maxHeight.set(maxHeight);
		return this;
	}

	public ConfiguredFeature<?, ?> getFeature() {
		if (!feature.isPresent())
			feature = Optional.of(createFeature());
		return feature.get();
	}

	private ConfiguredFeature<?, ?> createFeature() {
		ConfigDrivenOreFeatureConfig config =
				new ConfigDrivenOreFeatureConfig(FillerBlockType.BASE_STONE_OVERWORLD, block.get()
						.getDefaultState(), id);

		return ConfigDrivenOreFeature.INSTANCE.configure(config)
				.decorate(ConfigDrivenDecorator.INSTANCE.configure(config));
	}

	public void addToConfig() {
		ConfigGroup group = new ConfigGroup(id, 0);
		group.setConfig(getConfig());
		getConfig().groups.add(group);
		group.configs.add(clusterSize);
		group.configs.add(minHeight);
		group.configs.add(maxHeight);
		group.configs.add(frequency);
		group.registerValues();
	}

	@Override
	public Config getConfig() {
		return AllConfigs.COMMON.worldGen.getConfig();
	}

	@Override
	public String getName() {
		return id;
	}

}
