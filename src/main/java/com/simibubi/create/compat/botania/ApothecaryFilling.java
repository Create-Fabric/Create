package com.simibubi.create.compat.botania;

import com.simibubi.create.api.behaviour.BlockSpoutingBehaviour;
import com.simibubi.create.compat.Mods;
import com.simibubi.create.content.fluids.spout.SpoutBlockEntity;
import com.simibubi.create.foundation.utility.RegisteredObjects;
import com.simibubi.create.infrastructure.config.AllConfigs;

import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import vazkii.botania.api.block.PetalApothecary;

public class ApothecaryFilling extends BlockSpoutingBehaviour {

	static Boolean BOTANIA_PRESENT = null;

	private final ResourceLocation APOTHECARY = new ResourceLocation("botania", "altar");

	@Override
	public long fillBlock(Level level, BlockPos pos, SpoutBlockEntity spout, FluidStack availableFluid,
						  boolean simulate) {
		if (!enabled())
			return 0;

		BlockEntity te = level.getBlockEntity(pos);
		if (te == null)
			return 0;

		ResourceLocation registryName = RegisteredObjects.getKeyOrThrow(te.getType());
		if (!registryName.equals(APOTHECARY))
			return 0;

		// this shouldn't fail but... better safe than sorry.
		if (!(te instanceof PetalApothecary apothecary))
			return 0;
		// don't insert if it's not empty
		if (apothecary.getFluid() != PetalApothecary.State.EMPTY)
			return 0;

		PetalApothecary.State fluidState;

		Fluid fluid = availableFluid.getType().getFluid();
		if (fluid == Fluids.WATER) {
			fluidState = PetalApothecary.State.WATER;
		} else if (fluid == Fluids.LAVA) {
			fluidState = PetalApothecary.State.LAVA;
		} else {
			return 0;
		}

		// don't insert if we have less than a bucket's worth of fluid
		if (availableFluid.getAmount() < FluidConstants.BUCKET) {
			return 0;
		}

		long inserted = FluidConstants.BUCKET;
		if (!simulate) {
			availableFluid.shrink(inserted);
			apothecary.setFluid(fluidState);
		}

		return inserted;
	}

	private boolean enabled() {
		if (BOTANIA_PRESENT == null)
			BOTANIA_PRESENT = Mods.BOTANIA.isLoaded();
		if (!BOTANIA_PRESENT)
			return false;
		return AllConfigs.server().recipes.allowFillingBySpout.get();
	}

}
