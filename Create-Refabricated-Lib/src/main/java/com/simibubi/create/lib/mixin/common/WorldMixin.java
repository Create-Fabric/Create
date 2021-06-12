package com.simibubi.create.lib.mixin.common;

import java.util.Iterator;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.simibubi.create.lib.block.WeakPowerCheckingBlock;
import com.simibubi.create.lib.extensions.BlockStateExtensions;
import com.simibubi.create.lib.utility.MixinHelper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(World.class)
public abstract class WorldMixin {
	@Shadow
	public abstract BlockState getBlockState(BlockPos blockPos);

	@Inject(at = @At("RETURN"), method = "getRedstonePower(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/Direction;)I", cancellable = true)
	public void create$getRedstonePower(BlockPos blockPos, Direction direction, CallbackInfoReturnable<Integer> cir) {
		BlockState create$blockstate = MixinHelper.<World>cast(this).getBlockState(blockPos);
		int create$i = create$blockstate.getWeakPower(MixinHelper.<World>cast(this), blockPos, direction);

		if (create$blockstate.getBlock() instanceof WeakPowerCheckingBlock) {
			cir.setReturnValue(
					((WeakPowerCheckingBlock) create$blockstate.getBlock()).shouldCheckWeakPower(create$blockstate, MixinHelper.<World>cast(this), blockPos, direction)
							? Math.max(create$i, MixinHelper.<World>cast(this).getStrongPower(blockPos))
							: create$i);
		}
	}

	@Inject(at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/world/World;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;"),
			locals = LocalCapture.CAPTURE_FAILEXCEPTION,
			method = "updateComparatorOutputLevel(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;)V")
	public void create$updateComparatorOutputLevel(BlockPos blockPos, Block block, CallbackInfo ci,
												   Iterator<?> var3, Direction direction, BlockPos blockPos2) {
		((BlockStateExtensions) getBlockState(blockPos2)).create$onNeighborChange(MixinHelper.cast(this), blockPos2, blockPos);
	}
}
