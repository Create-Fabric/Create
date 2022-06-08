package com.simibubi.create.compat.emi;

import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.compat.rei.ConversionRecipe;
import com.simibubi.create.foundation.gui.AllGuiTextures;

import org.apache.commons.compress.utils.Lists;

import dev.emi.emi.api.widget.WidgetHolder;

public class MysteriousConversionEmiRecipe extends CreateEmiRecipe<ConversionRecipe> {
	public static final List<ConversionRecipe> RECIPES = Lists.newArrayList();

	static {
		RECIPES.add(ConversionRecipe.create(AllItems.EMPTY_BLAZE_BURNER.asStack(), AllBlocks.BLAZE_BURNER.asStack()));
		RECIPES.add(ConversionRecipe.create(AllBlocks.PECULIAR_BELL.asStack(), AllBlocks.HAUNTED_BELL.asStack()));
		RECIPES.add(ConversionRecipe.create(AllItems.CHROMATIC_COMPOUND.asStack(), AllItems.SHADOW_STEEL.asStack()));
		RECIPES.add(ConversionRecipe.create(AllItems.CHROMATIC_COMPOUND.asStack(), AllItems.REFINED_RADIANCE.asStack()));
	}
	
	public MysteriousConversionEmiRecipe(ConversionRecipe recipe) {
		super(CreateEmiPlugin.MYSTERY_CONVERSION, recipe, 134, 50);
	}

	@Override
	public void addWidgets(WidgetHolder widgets) {
		addTexture(widgets, AllGuiTextures.JEI_LONG_ARROW, 32, 20);
		addTexture(widgets, AllGuiTextures.JEI_QUESTION_MARK, 57, 5);

		addSlot(widgets, input.get(0), 5, 17);
		
		addSlot(widgets, output.get(0), 110, 17).recipeContext(this);
	}
}
