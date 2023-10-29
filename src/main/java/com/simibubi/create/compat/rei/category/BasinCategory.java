package com.simibubi.create.compat.rei.category;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.mutable.MutableInt;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.compat.rei.display.CreateDisplay;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.Pair;

import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class BasinCategory extends CreateRecipeCategory<BasinRecipe> {

	private boolean needsHeating;

	public BasinCategory(Info<BasinRecipe> info, boolean needsHeating) {
		super(info);
		this.needsHeating = needsHeating;
	}

	@Override
	public void addWidgets(CreateDisplay<BasinRecipe> display, List<Widget> widgets, Point origin) {
		BasinRecipe recipe = display.getRecipe();
		NonNullList<FluidIngredient> fluidIngredients = recipe.getFluidIngredients();
		List<Pair<Ingredient, MutableInt>> ingredients = ItemHelper.condenseIngredients(recipe.getIngredients());
		List<ItemStack> itemOutputs = recipe.getRollableResultsAsItemStacks();
		NonNullList<FluidStack> fluidOutputs = recipe.getFluidResults();

		int size = ingredients.size() + fluidIngredients.size();
		int xOffset = size < 3 ? (3 - size) * 19 / 2 : 0;
		int yOffset = 0;

		int i;
		for (i = 0; i < ingredients.size(); i++) {
			List<ItemStack> stacks = new ArrayList<>();
			Pair<Ingredient, MutableInt> pair = ingredients.get(i);
			Ingredient ingredient = pair.getFirst();
			MutableInt amount = pair.getSecond();

			for (ItemStack itemStack : ingredient.getItems()) {
				ItemStack stack = itemStack.copy();
				stack.setCount(amount.getValue());
				stacks.add(stack);
			}

			widgets.add(basicSlot(origin.x + 17 + xOffset + (i % 3) * 19, origin.y + 51 - (i / 3) * 19 + yOffset).markInput().entries(EntryIngredients.ofItemStacks(stacks)));
		}

		int j;
		for (j = 0; j < fluidIngredients.size(); j++) {
			int i2 = i + j;
			List<FluidStack> stacks = fluidIngredients.get(j)
					.getMatchingFluidStacks();
			Slot fluidSlot = basicSlot(origin.x + 17 + xOffset + (i2 % 3) * 19, origin.y + 51 - (i2 / 3) * 19 + yOffset)
					.markInput()
					.entries(EntryIngredients.of(CreateRecipeCategory.convertToREIFluid(stacks.get(0))));
			CreateRecipeCategory.setFluidRenderRatio(fluidSlot);
			widgets.add(fluidSlot);
		}

		int outSize = fluidOutputs.size() + recipe.getRollableResults()
				.size();
		int outputIndex = 0;

		if (!itemOutputs.isEmpty())
			addStochasticTooltip(widgets, recipe.getRollableResults(), i);

		for (; outputIndex < outSize; outputIndex++) {
			int xPosition = 141 - (outSize % 2 != 0 && outputIndex == outSize - 1 ? 0 : outputIndex % 2 == 0 ? 10 : -9);
			int yPosition = -19 * (outputIndex / 2) + 50 + yOffset;

			if (itemOutputs.size() > outputIndex) {
				widgets.add(basicSlot(origin.x + xPosition + 1, origin.y + yPosition + yOffset + 1)
						.markOutput()
						.entries(EntryIngredients.of(itemOutputs.get(outputIndex))));
				i++;
			} else {
				Slot fluidSlot = basicSlot(origin.x + xPosition + 1, origin.y + yPosition + 1 + yOffset)
						.markOutput()
						.entries(EntryIngredients.of(CreateRecipeCategory.convertToREIFluid(fluidOutputs.get(outputIndex - itemOutputs.size()))));
				CreateRecipeCategory.setFluidRenderRatio(fluidSlot);
				widgets.add(fluidSlot);
				j++;
			}

		}

		addFluidTooltip(widgets, fluidIngredients, fluidOutputs);

		HeatCondition requiredHeat = recipe.getRequiredHeat();
		if (!requiredHeat.testBlazeBurner(HeatLevel.NONE)) {
			widgets.add(basicSlot(origin.x + 134, origin.y + 81).markInput().entries(EntryIngredients.of(AllBlocks.BLAZE_BURNER.asStack())));
			i++;
		}
		if (!requiredHeat.testBlazeBurner(HeatLevel.KINDLED)) {
			widgets.add(basicSlot(origin.x + 153, origin.y + 81).markOutput().entries(EntryIngredients.of(AllItems.BLAZE_CAKE.asStack())));
			i++;
		}
	}

	@Override
	public void draw(BasinRecipe recipe, GuiGraphics graphics, double mouseX, double mouseY) {
		List<Pair<Ingredient, MutableInt>> actualIngredients = ItemHelper.condenseIngredients(recipe.getIngredients());

		int size = actualIngredients.size() + recipe.getFluidIngredients()
			.size();
		int outSize = recipe.getFluidResults().size() + recipe.getRollableResults().size();

		int xOffset = size < 3 ? (3 - size) * 19 / 2 : 0;
		HeatCondition requiredHeat = recipe.getRequiredHeat();
		int yOffset = 0;

		for (int i = 0; i < size; i++)
			AllGuiTextures.JEI_SLOT.render(graphics, 16 + xOffset + (i % 3) * 19, 50 - (i / 3) * 19 + yOffset);

		boolean noHeat = requiredHeat == HeatCondition.NONE;

		int vRows = (1 + outSize) / 2;
		for (int i = 0; i < outSize; i++)
			AllGuiTextures.JEI_SLOT.render(graphics,
				141 - (outSize % 2 != 0 && i == outSize - 1 ? 0 : i % 2 == 0 ? 10 : -9), -19 * (i / 2) + 50 + yOffset);
		if (vRows <= 2)
			AllGuiTextures.JEI_DOWN_ARROW.render(graphics, 136, -19 * (vRows - 1) + 32 + yOffset);

		AllGuiTextures shadow = noHeat ? AllGuiTextures.JEI_SHADOW : AllGuiTextures.JEI_LIGHT;
		shadow.render(graphics, 81, 58 + (noHeat ? 10 : 30));

		if (!needsHeating)
			return;

		AllGuiTextures heatBar = noHeat ? AllGuiTextures.JEI_NO_HEAT_BAR : AllGuiTextures.JEI_HEAT_BAR;
		heatBar.render(graphics, 4, 80);
		graphics.drawString(Minecraft.getInstance().font, Lang.translateDirect(requiredHeat.getTranslationKey()), 9,
			86, requiredHeat.getColor());
	}

}
