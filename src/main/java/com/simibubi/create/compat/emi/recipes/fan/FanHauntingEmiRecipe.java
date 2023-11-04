package com.simibubi.create.compat.emi.recipes.fan;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.compat.emi.CreateEmiAnimations;
import com.simibubi.create.compat.emi.CreateEmiPlugin;
import com.simibubi.create.content.kinetics.fan.processing.HauntingRecipe;
import com.simibubi.create.foundation.gui.element.GuiGameElement;

import net.minecraft.world.level.block.Blocks;

public class FanHauntingEmiRecipe extends FanEmiRecipe.MultiOutput<HauntingRecipe> {

	public FanHauntingEmiRecipe(HauntingRecipe recipe) {
		super(CreateEmiPlugin.FAN_HAUNTING, recipe);
	}

	@Override
	protected void renderAttachedBlock(PoseStack matrices) {
		GuiGameElement.of(Blocks.SOUL_FIRE.defaultBlockState())
			.scale(SCALE)
			.atLocal(0, 0, 2)
			.lighting(CreateEmiAnimations.DEFAULT_LIGHTING)
			.render(matrices);
	}
}
