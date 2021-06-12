package com.simibubi.create.lib.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.simibubi.create.lib.extensions.BlockStateExtensions;
import com.simibubi.create.lib.utility.MixinHelper;

import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.util.math.BlockPos;

@Mixin(ExperienceOrbEntity.class)
public abstract class ExperienceOrbEntityMixin {
	@ModifyVariable(at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/block/Block;getSlipperiness()F"),
			method = "tick()V")
	public float create$setSlipperiness(float g) {
		BlockPos create$pos = new BlockPos(
				MixinHelper.<ExperienceOrbEntity>cast(this).getX(),
				MixinHelper.<ExperienceOrbEntity>cast(this).getY(),
				MixinHelper.<ExperienceOrbEntity>cast(this).getZ()
		);

		return ((BlockStateExtensions) MixinHelper.<ExperienceOrbEntity>cast(this).world.getBlockState(create$pos))
				.create$getSlipperiness(MixinHelper.<ExperienceOrbEntity>cast(this).world, create$pos, MixinHelper.<ExperienceOrbEntity>cast(this)) * 0.98F;
	}
}
