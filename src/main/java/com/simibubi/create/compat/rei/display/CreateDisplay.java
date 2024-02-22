package com.simibubi.create.compat.rei.display;

import static com.simibubi.create.compat.rei.category.CreateRecipeCategory.getResultItem;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

public class CreateDisplay<R extends Recipe<?>> implements Display {
	protected final R recipe;
	private final CategoryIdentifier<CreateDisplay<R>> uid;
	private final List<EntryIngredient> input;
	private final List<EntryIngredient> output;

	public CreateDisplay(R recipe, CategoryIdentifier<CreateDisplay<R>> id, List<EntryIngredient> input, List<EntryIngredient> output) {
		this.recipe = recipe;
		this.uid = id;
		this.input = input;
		this.output = output;
	}

	public CreateDisplay(R recipe, CategoryIdentifier<CreateDisplay<R>> id) {
		this.uid = id;
		this.recipe = recipe;

		this.input = EntryIngredients.ofIngredients(recipe.getIngredients());

		if (recipe instanceof ProcessingRecipe) {
			this.output = ((List<ItemStack>)((ProcessingRecipe) recipe).getRollableResultsAsItemStacks()).stream()
				.map(EntryIngredients::of)
				.collect(Collectors.toList());
		} else {
			this.output = Collections.singletonList(EntryIngredients.of(getResultItem(recipe)));
		}
	}



	public R getRecipe() {
		return recipe;
	}

	@Override
	public List<EntryIngredient> getInputEntries() {
		return input;
	}

	@Override
	public List<EntryIngredient> getOutputEntries() {
		return output;
	}

	@Override
	public CategoryIdentifier<?> getCategoryIdentifier() {
		return uid;
	}
}
