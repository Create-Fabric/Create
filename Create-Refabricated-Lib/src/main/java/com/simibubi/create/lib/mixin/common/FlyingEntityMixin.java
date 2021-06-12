package com.simibubi.create.lib.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;

import com.simibubi.create.lib.extensions.BlockStateExtensions;
import com.simibubi.create.lib.utility.MixinHelper;

import net.minecraft.entity.FlyingEntity;
import net.minecraft.util.math.BlockPos;

@Mixin(FlyingEntity.class)
public abstract class FlyingEntityMixin {
	@ModifyVariable(slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;onGround:Z")),
			at = @At(value = "STORE"),
			method = "travel(Lnet/minecraft/util/math/vector/Vector3d;)V")
	public float create$setSlipperiness1(float f) {
		BlockPos create$ground = new BlockPos(
				MixinHelper.<FlyingEntity>cast(this).getX(),
				MixinHelper.<FlyingEntity>cast(this).getY() - 1.0D,
				MixinHelper.<FlyingEntity>cast(this).getZ());

		return ((BlockStateExtensions) MixinHelper.<FlyingEntity>cast(this).world.getBlockState(create$ground))
				.create$getSlipperiness(MixinHelper.<FlyingEntity>cast(this).world, create$ground, MixinHelper.<FlyingEntity>cast(this)) * 0.91F;
	}

	@ModifyVariable(slice = @Slice(from = @At(value = "FIELD", ordinal = 1, target = "Lnet/minecraft/entity/Entity;onGround:Z")),
			at = @At(value = "STORE"),
			method = "travel(Lnet/minecraft/util/math/vector/Vector3d;)V")
	public float create$setSlipperiness2(float f) {
		BlockPos create$ground = new BlockPos(
				MixinHelper.<FlyingEntity>cast(this).getX(),
				MixinHelper.<FlyingEntity>cast(this).getY() - 1.0D,
				MixinHelper.<FlyingEntity>cast(this).getZ());

		return ((BlockStateExtensions) MixinHelper.<FlyingEntity>cast(this).world.getBlockState(create$ground))
				.create$getSlipperiness(MixinHelper.<FlyingEntity>cast(this).world, create$ground, MixinHelper.<FlyingEntity>cast(this)) * 0.91F;
	}
}
