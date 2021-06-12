package com.simibubi.create.lib.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import com.simibubi.create.lib.extensions.BlockParticleDataExtensions;
import com.simibubi.create.lib.utility.MixinHelper;

import net.minecraft.particles.BlockParticleData;
import net.minecraft.util.math.BlockPos;

@Mixin(BlockParticleData.class)
public abstract class BlockParticleDataMixin implements BlockParticleDataExtensions {
	private BlockPos pos;

	@Override
	@Unique
	public BlockParticleData create$setPos(BlockPos pos) {
		this.pos = pos;
		return MixinHelper.cast(this);
	}

	@Override
	@Unique
	public BlockPos create$getPos() {
		return pos;
	}
}
