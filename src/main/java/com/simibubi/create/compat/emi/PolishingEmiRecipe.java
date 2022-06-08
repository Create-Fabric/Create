package com.simibubi.create.compat.emi;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.curiosities.tools.SandPaperPolishingRecipe;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.element.GuiGameElement;

import dev.emi.emi.api.widget.WidgetHolder;
import io.github.fabricators_of_create.porting_lib.util.NBTSerializer;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class PolishingEmiRecipe extends CreateEmiRecipe<SandPaperPolishingRecipe> {

	public PolishingEmiRecipe(SandPaperPolishingRecipe recipe) {
		super(CreateEmiPlugin.SANDPAPER_POLISHING, recipe, 134, 55);
	}

	@Override
	public void addWidgets(WidgetHolder widgets) {
		addTexture(widgets, AllGuiTextures.JEI_SHADOW, 39, 25);
		addTexture(widgets, AllGuiTextures.JEI_LONG_ARROW, 30, 37);

		addSlot(widgets, input.get(0), 5, 33);

		addChancedSlot(widgets, output.get(0), 110, 33, 0).recipeContext(this);

		NonNullList<Ingredient> ingredients = recipe.getIngredients();
		if (!ingredients.isEmpty() && !ingredients.get(0).isEmpty()) {
			ItemStack[] matchingStacks = ingredients.get(0).getItems();
			ItemStack stack = AllItems.SAND_PAPER.asStack();
			CompoundTag tag = stack.getOrCreateTag();
			tag.put("Polishing", NBTSerializer.serializeNBT(matchingStacks[0]));
			tag.putBoolean("JEI", true);
			widgets.addDrawable(49, 4, 0, 0, (matrices, mouseX, mouseY, delta) -> {
				matrices.translate(0, 0, 100);
				matrices.scale(2, 2, 1);
				GuiGameElement.of(stack)
						.<GuiGameElement.GuiRenderBuilder>at(0, 0, 1)
						.scale(1)
						.render(matrices);
			});
		}
	}
}
