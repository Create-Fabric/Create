package com.simibubi.create.compat.emi;

import com.simibubi.create.content.contraptions.components.press.PressingRecipe;
import com.simibubi.create.foundation.gui.AllGuiTextures;

import dev.emi.emi.api.widget.WidgetHolder;

public class PressingEmiRecipe extends CreateEmiRecipe<PressingRecipe> {
	
	public PressingEmiRecipe(PressingRecipe recipe) {
		super(CreateEmiPlugin.PRESSING, recipe, 134, 90);
	}

	@Override
	public void addWidgets(WidgetHolder widgets) {
		addTexture(widgets, AllGuiTextures.JEI_SHADOW, 40, 59);
		addTexture(widgets, AllGuiTextures.JEI_LONG_ARROW, 31, 69);

		addSlot(widgets, input.get(0), 6, 66);

		for (int i = 0; i < output.size(); i++) {
			addChancedSlot(widgets, output.get(i), 110 + i * 19, 65, i).recipeContext(this);
		}
		
		CreateEmiAnimations.addPress(widgets, 50, 40, false);
	}
}
