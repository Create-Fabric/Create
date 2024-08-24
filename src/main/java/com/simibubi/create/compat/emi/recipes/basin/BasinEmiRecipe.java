package com.simibubi.create.compat.emi.recipes.basin;

import java.util.List;

import com.google.common.collect.Lists;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.compat.emi.recipes.CreateEmiRecipe;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.utility.Lang;

import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;

public class BasinEmiRecipe extends CreateEmiRecipe<BasinRecipe> {
	private final List<EmiIngredient> catalysts = Lists.newArrayList();
	private final boolean needsHeating;

	public BasinEmiRecipe(EmiRecipeCategory category, BasinRecipe recipe, boolean needsHeating) {
		super(category, recipe, 177, 108);
		if (!needsHeating) {
			height = 90;
		}
		this.needsHeating = needsHeating;
		HeatCondition requiredHeat = recipe.getRequiredHeat();
		if (!requiredHeat.testBlazeBurner(HeatLevel.NONE)) {
			catalysts.add(EmiStack.of(AllBlocks.BLAZE_BURNER.get()));
		}
		if (!requiredHeat.testBlazeBurner(HeatLevel.KINDLED)) {
			catalysts.add(EmiStack.of(AllItems.BLAZE_CAKE.get()));
		}
	}

	@Override
	public List<EmiIngredient> getCatalysts() {
		return catalysts;
	}

	@Override
	public void addWidgets(WidgetHolder widgets) {
		int inputSize = input.size();
		int outputSize = output.size();
		int vRows = (1 + outputSize) / 2;
		HeatCondition requiredHeat = recipe.getRequiredHeat();

		if (vRows <= 2) {
			addTexture(widgets, AllGuiTextures.JEI_DOWN_ARROW, 136, 32 - 19 * (vRows - 1));
		}

		boolean noHeat = requiredHeat == HeatCondition.NONE;
		AllGuiTextures shadow = noHeat ? AllGuiTextures.JEI_SHADOW : AllGuiTextures.JEI_LIGHT;
		addTexture(widgets, shadow, 81, 58 + (noHeat ? 10 : 30));

		if (needsHeating) {
			AllGuiTextures heatBar = noHeat ? AllGuiTextures.JEI_NO_HEAT_BAR : AllGuiTextures.JEI_HEAT_BAR;
			addTexture(widgets, heatBar, 4, 80);
			widgets.addText(Lang.translateDirect(requiredHeat.getTranslationKey()).getVisualOrderText(), 9, 86, requiredHeat.getColor(), true);
		}

		int xOff = inputSize < 3 ? (3 - inputSize) * 19 / 2 : 0;
		int yOff = 0;

		for (int i = 0; i < inputSize; i++) {
			EmiIngredient stack = input.get(i);
			addSlot(widgets, stack, xOff + 16 + (i % 3) * 19, yOff + 50 - (i / 3) * 19);
		}

		for (int i = 0; i < outputSize; i++) {
			int x = 140 - (outputSize % 2 != 0 && i == outputSize - 1 ? 0 : i % 2 == 0 ? 10 : -9);
			int y = 50 - 20 * (i / 2) + yOff;

			EmiStack stack = output.get(i);
			addSlot(widgets, stack, x, y).recipeContext(this);
		}

		if (!requiredHeat.testBlazeBurner(HeatLevel.NONE)) {
			widgets.addSlot(EmiStack.of(AllBlocks.BLAZE_BURNER.get()), 133, 81).drawBack(false).catalyst(true);
		}
		if (!requiredHeat.testBlazeBurner(HeatLevel.KINDLED)) {
			widgets.addSlot(EmiStack.of(AllItems.BLAZE_CAKE.get()), 152, 81).drawBack(false);
		}
	}
}
