package com.simibubi.create.lib.mixin.common;

import org.spongepowered.asm.mixin.Mixin;

import com.simibubi.create.lib.extensions.StructureProcessorExtensions;

import net.minecraft.world.gen.feature.template.StructureProcessor;

@Mixin(StructureProcessor.class)
public abstract class StructureProcessorMixin implements StructureProcessorExtensions {

}
