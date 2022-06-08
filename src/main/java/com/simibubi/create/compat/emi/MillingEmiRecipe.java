package com.simibubi.create.compat.emi;

import com.simibubi.create.content.contraptions.components.millstone.MillingRecipe;
import com.simibubi.create.foundation.gui.AllGuiTextures;

import dev.emi.emi.api.widget.WidgetHolder;

public class MillingEmiRecipe extends CreateEmiRecipe<MillingRecipe> {

	public MillingEmiRecipe(MillingRecipe recipe) {
		super(CreateEmiPlugin.MILLING, recipe, 177, 61);
	}

	@Override
	public void addWidgets(WidgetHolder widgets) {
		addTexture(widgets, AllGuiTextures.JEI_DOWN_ARROW, 43, 4);
		addTexture(widgets, AllGuiTextures.JEI_ARROW, 85, 32);
		addTexture(widgets, AllGuiTextures.JEI_SHADOW, 32, 40);

		addSlot(widgets, input.get(0), 14, 8);

		for (int i = 0; i < output.size(); i++) {
			int xOff = (i % 2) * 19;
			int yOff = (i / 2) * -19;
			addChancedSlot(widgets, output.get(i), 133 + xOff, 27 + yOff, i).recipeContext(this);
		}
		
		CreateEmiAnimations.addMillstone(widgets, 46, 45);
	}
}
