package com.simibubi.create.lib.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.simibubi.create.lib.event.FluidPlaceBlockCallback;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.LavaFluid;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

@Mixin(LavaFluid.class)
public abstract class LavaFluidMixin {
	@Redirect(method = "randomTick(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/fluid/FluidState;Ljava/util/Random;)V",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Z"))
	private boolean create$randomTick(World world, BlockPos pos, BlockState state) {
		BlockState newState = FluidPlaceBlockCallback.EVENT.invoker().onFluidPlaceBlock(world, pos, state);

		return world.setBlockState(pos, newState != null ? newState : state);
	}

	@Redirect(method = "flowInto(Lnet/minecraft/world/IWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/Direction;Lnet/minecraft/fluid/FluidState;)V",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/IWorld;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"))
	private boolean create$flowInto(IWorld world, BlockPos pos, BlockState state, int flags) {
		BlockState newState = FluidPlaceBlockCallback.EVENT.invoker().onFluidPlaceBlock(world, pos, state);

		return world.setBlockState(pos, newState != null ? newState : state, flags);
	}
}
