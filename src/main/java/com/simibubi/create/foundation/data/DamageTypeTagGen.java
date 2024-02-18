package com.simibubi.create.foundation.data;

import java.util.concurrent.CompletableFuture;

import com.simibubi.create.AllDamageTypes;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageType;

// fabric: Must use addOptional otherwise validation will fail
public class DamageTypeTagGen extends FabricTagProvider<DamageType> {
	public DamageTypeTagGen(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
		super(output, Registries.DAMAGE_TYPE, lookupProvider);
	}

	@Override
	protected void addTags(HolderLookup.Provider provider) {
		getOrCreateTagBuilder(DamageTypeTags.BYPASSES_ARMOR)
				.addOptional(AllDamageTypes.CRUSH)
				.addOptional(AllDamageTypes.FAN_FIRE)
				.addOptional(AllDamageTypes.FAN_LAVA)
				.addOptional(AllDamageTypes.DRILL)
				.addOptional(AllDamageTypes.SAW);
		getOrCreateTagBuilder(DamageTypeTags.IS_FIRE)
				.addOptional(AllDamageTypes.FAN_FIRE)
				.addOptional(AllDamageTypes.FAN_LAVA);
		getOrCreateTagBuilder(DamageTypeTags.IS_EXPLOSION)
				.addOptional(AllDamageTypes.CUCKOO_SURPRISE);
	}

	@Override
	public String getName() {
		return "Create's Damage Type Tags";
	}
}
