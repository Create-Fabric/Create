package com.simibubi.create.content.contraptions.components.crusher;

import com.simibubi.create.content.contraptions.processing.ProcessingRecipe;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipeBuilder.ProcessingRecipeParams;
import com.simibubi.create.foundation.utility.recipe.IRecipeTypeInfo;

import net.minecraft.world.Container;

public abstract class AbstractCrushingRecipe extends ProcessingRecipe<Container> {

	public AbstractCrushingRecipe(IRecipeTypeInfo recipeType, ProcessingRecipeParams params) {
		super(recipeType, params);
	}

	@Override
	protected int getMaxInputCount() {
		return 1;
	}
}
