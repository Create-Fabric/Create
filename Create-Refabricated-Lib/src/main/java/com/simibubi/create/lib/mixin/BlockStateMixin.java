package com.simibubi.create.lib.mixin;

import org.spongepowered.asm.mixin.Mixin;

import com.simibubi.create.lib.extensions.BlockStateExtensions;

import net.minecraft.block.BlockState;

@Mixin(BlockState.class)
public abstract class BlockStateMixin implements BlockStateExtensions {
}
