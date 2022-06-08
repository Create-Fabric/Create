package com.simibubi.create.compat.emi;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.level.block.Blocks;

public class FanSmokingEmiRecipe extends FanEmiRecipe<SmokingRecipe> {

	public FanSmokingEmiRecipe(SmokingRecipe recipe) {
		super(CreateEmiPlugin.FAN_SMOKING, recipe);
		ResourceLocation rid = recipe.getId();
		this.id = new ResourceLocation("emi", "create/fan_smoking/" + rid.getNamespace() + "/" + rid.getPath());
	}

	@Override
	protected void renderAttachedBlock(PoseStack matrices) {
		GuiGameElement.of(Blocks.FIRE.defaultBlockState())
			.scale(SCALE)
			.atLocal(0, 0, 2)
			.lighting(CreateEmiAnimations.DEFAULT_LIGHTING)
			.render(matrices);
	}
}
