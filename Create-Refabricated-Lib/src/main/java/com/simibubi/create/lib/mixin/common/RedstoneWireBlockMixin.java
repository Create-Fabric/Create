package com.simibubi.create.lib.mixin.common;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.lib.block.CanConnectRedstoneBlock;

import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

@Mixin(RedstoneWireBlock.class)
public abstract class RedstoneWireBlockMixin {
	// I have concerns for this Shadow but it should be fine? :tiny_potato:
	@Shadow
	protected static boolean canConnectTo(BlockState blockState, @Nullable Direction side) {
		return false;
	}

	@Inject(at = @At(value = "RETURN", ordinal = 3),
			method = "canConnectTo(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/Direction;)Z", cancellable = true)
	private static void create$canConnectTo(BlockState state, Direction side, CallbackInfoReturnable<Boolean> cir) {
		if (state.getBlock() instanceof CanConnectRedstoneBlock) {
			// Passing null for world and pos here just for extra upstream compat, not properly implementing it because
			// 1. world and pos are never used in Create
			// 2. extra work :help_me:
			cir.setReturnValue(((CanConnectRedstoneBlock) state.getBlock()).canConnectRedstone(state, null, null, side));
		}
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/block/RedstoneWireBlock;canConnectUpwardsTo(Lnet/minecraft/block/BlockState;)Z"),
			method = "method_27841(Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/Direction;Z)Lnet/minecraft/state/properties/RedstoneSide;")
	private boolean create$canConnectUpwardsTo(BlockState state, IBlockReader world, BlockPos pos, Direction side, boolean bl) {
		return canConnectTo(state, side);
	}
}
