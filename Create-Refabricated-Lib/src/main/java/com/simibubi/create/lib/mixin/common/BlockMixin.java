package com.simibubi.create.lib.mixin.common;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.simibubi.create.lib.extensions.BlockExtensions;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;

@Mixin(Block.class)
public abstract class BlockMixin extends AbstractBlock implements BlockExtensions {
	private BlockMixin(Properties properties) {
		super(properties);
	}

	@Shadow
	public abstract SoundType getSoundType(BlockState blockState);

	@Override
	public SoundType create$getSoundType(BlockState state, IWorldReader world, BlockPos pos, @Nullable Entity entity) {
		return getSoundType(state);
	}

	@Override
	public int create$getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
		return state.getLightValue();
	}
}
