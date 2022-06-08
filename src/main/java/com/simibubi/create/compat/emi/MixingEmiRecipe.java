package com.simibubi.create.compat.emi;

import com.simibubi.create.content.contraptions.processing.BasinRecipe;
import com.simibubi.create.content.contraptions.processing.HeatCondition;

import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.widget.WidgetHolder;

public class MixingEmiRecipe extends BasinEmiRecipe {

	public MixingEmiRecipe(EmiRecipeCategory category, BasinRecipe recipe) {
		super(category, recipe, false);
	}
	
	@Override
	public void addWidgets(WidgetHolder widgets) {
		super.addWidgets(widgets);
		
		HeatCondition requiredHeat = recipe.getRequiredHeat();
		if (requiredHeat != HeatCondition.NONE) {
			CreateEmiAnimations.addBlazeBurner(widgets, widgets.getWidth() / 2 + 3, 55, requiredHeat.visualizeAsBlazeBurner());
		}
		CreateEmiAnimations.addMixer(widgets, widgets.getWidth() / 2 + 3, 40);
	}
}
