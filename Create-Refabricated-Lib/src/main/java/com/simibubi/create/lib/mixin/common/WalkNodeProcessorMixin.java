package com.simibubi.create.lib.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.lib.block.CustomPathNodeTypeBlock;

import net.minecraft.block.Block;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

@Mixin(WalkNodeProcessor.class)
public abstract class WalkNodeProcessorMixin {
	@Inject(at = @At("HEAD"), method = "getCommonNodeType(Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/pathfinding/PathNodeType;", cancellable = true)
	private static void create$getCommonNodeType(IBlockReader iBlockReader, BlockPos blockPos, CallbackInfoReturnable<PathNodeType> cir) {
		Block block = iBlockReader.getBlockState(blockPos).getBlock();
		if (block instanceof CustomPathNodeTypeBlock) {
			cir.setReturnValue(((CustomPathNodeTypeBlock) block).getAiPathNodeType(iBlockReader.getBlockState(blockPos), iBlockReader, blockPos, null));
		}
	}
}
