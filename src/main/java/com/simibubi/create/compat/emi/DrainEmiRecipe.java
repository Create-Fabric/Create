package com.simibubi.create.compat.emi;

import java.util.List;

import com.simibubi.create.content.contraptions.processing.EmptyingRecipe;
import com.simibubi.create.foundation.gui.AllGuiTextures;

import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;

public class DrainEmiRecipe extends CreateEmiRecipe<EmptyingRecipe> {

	public DrainEmiRecipe(EmptyingRecipe recipe) {
		super(CreateEmiPlugin.DRAINING, recipe, 134, 52, c -> {});
		input = List.of(EmiStack.of(recipe.getIngredients().get(0).getItems()[0]));
		output = List.of(fluidStack(recipe.getFluidResults().get(0)), EmiStack.of(recipe.getRollableResults().get(0).getStack()));
	}

	@Override
	public void addWidgets(WidgetHolder widgets) {
		addTexture(widgets, AllGuiTextures.JEI_SHADOW, 40, 37);
		addTexture(widgets, AllGuiTextures.JEI_DOWN_ARROW, 51, 4);

		addSlot(widgets, input.get(0), 4, 7);

		addSlot(widgets, output.get(0), 109, 7).recipeContext(this);
		addSlot(widgets, output.get(1), 109, 26).recipeContext(this);

		CreateEmiAnimations.addDrain(widgets, widgets.getWidth() / 2 - 13, 40, recipe.getFluidResults().get(0));
	}
}
