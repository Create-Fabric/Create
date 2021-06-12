package com.simibubi.create.lib.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.simibubi.create.lib.event.FluidPlaceBlockCallback;

import net.minecraft.block.BlockState;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(FlowingFluidBlock.class)
public abstract class FlowingFluidBlockMixin {
	@Redirect(method = "reactWithNeighbors(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Z",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Z"))
	private boolean create$onReactWithNeighbors(World world, BlockPos pos, BlockState state) {
		BlockState newState = FluidPlaceBlockCallback.EVENT.invoker().onFluidPlaceBlock(world, pos, state);

		return world.setBlockState(pos, newState != null ? newState : state);
	}
}
