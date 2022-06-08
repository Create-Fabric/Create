package com.simibubi.create.compat.emi;

import java.util.List;

import com.simibubi.create.content.contraptions.fluids.actors.FillingRecipe;
import com.simibubi.create.foundation.gui.AllGuiTextures;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;

public class SpoutEmiRecipe extends CreateEmiRecipe<FillingRecipe> {

	public SpoutEmiRecipe(FillingRecipe recipe) {
		super(CreateEmiPlugin.SPOUT_FILLING, recipe, 134, 74, c -> {});
		input = List.of(EmiIngredient.of(recipe.getIngredients().get(0)),
			fluidStack(recipe.getRequiredFluid().getMatchingFluidStacks().get(0)));
		output = List.of(EmiStack.of(recipe.getRollableResults().get(0).getStack()));
	}

	@Override
	public void addWidgets(WidgetHolder widgets) {
		addTexture(widgets, AllGuiTextures.JEI_SHADOW, 40, 57);
		addTexture(widgets, AllGuiTextures.JEI_DOWN_ARROW, 104, 29);

		addSlot(widgets, input.get(0), 4, 31);
		addSlot(widgets, input.get(1), 4, 50);

		addSlot(widgets, output.get(0), 109, 50).recipeContext(this);

		CreateEmiAnimations.addSpout(widgets, widgets.getWidth() / 2 - 13, 22, recipe.getRequiredFluid().getMatchingFluidStacks());
	}
}
