package com.simibubi.create.compat.emi;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.contraptions.components.fan.SplashingRecipe;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import net.minecraft.world.level.material.Fluids;

public class FanWashingEmiRecipe extends FanEmiRecipe.MultiOutput<SplashingRecipe> {
	
	public FanWashingEmiRecipe(SplashingRecipe recipe) {
		super(CreateEmiPlugin.FAN_WASHING, recipe);
	}

	@Override
	protected void renderAttachedBlock(PoseStack matrices) {
		GuiGameElement.of(Fluids.WATER)
			.scale(SCALE)
			.atLocal(0, 0, 2)
			.lighting(CreateEmiAnimations.DEFAULT_LIGHTING)
			.render(matrices);
	}
}
