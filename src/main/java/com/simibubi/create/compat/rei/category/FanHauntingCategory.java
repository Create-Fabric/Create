package com.simibubi.create.compat.rei.category;

import java.util.Arrays;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllItems;
import com.simibubi.create.compat.rei.display.FanHauntingDisplay;
import com.simibubi.create.content.contraptions.components.fan.HauntingRecipe;
import com.simibubi.create.content.contraptions.processing.ProcessingOutput;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.element.GuiGameElement;

import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class FanHauntingCategory extends ProcessingViaFanCategory<HauntingRecipe, FanHauntingDisplay> {

	public FanHauntingCategory() {
		super(doubleItemIcon(AllItems.PROPELLER, () -> Items.SOUL_CAMPFIRE));
	}

    @Override
	public void renderAttachedBlock(PoseStack matrixStack) {
		GuiGameElement.of(Blocks.SOUL_FIRE.defaultBlockState())
				.scale(24)
				.atLocal(0, 0, 2)
				.render(matrixStack);
	}
}
