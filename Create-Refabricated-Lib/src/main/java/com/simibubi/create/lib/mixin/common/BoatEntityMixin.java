package com.simibubi.create.lib.mixin.common;

import net.minecraft.util.math.shapes.VoxelShape;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.simibubi.create.lib.extensions.BlockStateExtensions;
import com.simibubi.create.lib.utility.MixinHelper;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(BoatEntity.class)
public abstract class BoatEntityMixin {
	// you can't capture locals in a @ModifyVariable, so we have this
	@Unique
	BlockState create$state;
	@Unique
	World create$world;
	@Unique
	BlockPos.Mutable create$pos;
	@Unique
	Entity create$entity;

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;getSlipperiness()F"),
			locals = LocalCapture.CAPTURE_FAILEXCEPTION,
			method = "getBoatGlide()F")
	public void create$getBoatGlide(CallbackInfoReturnable<Float> cir,
									AxisAlignedBB axisAlignedBB, AxisAlignedBB axisAlignedBB2, int i, int j, int k,
									int l, int m, int n, VoxelShape shape, float f, int o, BlockPos.Mutable mutable,
									int p, int q, int r, int s, BlockState blockState) {
		create$state = blockState;
		create$world = MixinHelper.<BoatEntity>cast(this).world;
		create$pos = mutable;
		create$entity = MixinHelper.<BoatEntity>cast(this);
	}

	@ModifyVariable(at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/block/Block;getSlipperiness()F"),
			method = "getBoatGlide()F")
	public float create$setSlipperiness(float f) {
		return ((BlockStateExtensions) create$state).create$getSlipperiness(create$world, create$pos, create$entity);
	}
}
