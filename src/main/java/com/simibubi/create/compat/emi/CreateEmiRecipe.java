package com.simibubi.create.compat.emi;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import com.google.common.collect.ImmutableList;
import com.simibubi.create.content.contraptions.processing.BasinRecipe;
import com.simibubi.create.content.contraptions.processing.ProcessingOutput;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipe;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.Pair;

import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.TextureWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import io.github.fabricators_of_create.porting_lib.util.FluidStack;

public abstract class CreateEmiRecipe<T extends Recipe<?>> implements EmiRecipe {
	protected final EmiRecipeCategory category;
	protected final T recipe;
	protected ResourceLocation id;
	protected List<EmiIngredient> input;
	protected List<EmiStack> output;
	protected boolean chanced;
	protected int width, height;

	public CreateEmiRecipe(EmiRecipeCategory category, T recipe, int width, int height) {
		this.category = category;
		this.recipe = recipe;
		this.id = recipe.getId();
		this.width = width;
		this.height = height;
		if (recipe instanceof BasinRecipe basin) {
			ImmutableList.Builder<EmiIngredient> input = ImmutableList.builder();
			ImmutableList.Builder<EmiStack> output = ImmutableList.builder();
			for (Pair<Ingredient, MutableInt> pair : ItemHelper.condenseIngredients(recipe.getIngredients())) {
				input.add(EmiIngredient.of(pair.getFirst(), pair.getSecond().getValue()));
			}
			for (FluidIngredient ingredient : basin.getFluidIngredients()) {
				input.add(fluidStack(ingredient.getMatchingFluidStacks().get(0)));
			}
			for (ItemStack stack : basin.getRollableResultsAsItemStacks()) {
				output.add(EmiStack.of(stack));
			}
			for (FluidStack stack : basin.getFluidResults()) {
				output.add(fluidStack(stack));
			}
			this.input = input.build();
			this.output = output.build();
		} else {
			this.input = recipe.getIngredients().stream().map(EmiIngredient::of).toList();
			if (recipe instanceof ProcessingRecipe<?> processing) {
				ImmutableList.Builder<EmiStack> builder = ImmutableList.builder();
				for (ProcessingOutput output : processing.getRollableResults()) {
					if (output.getChance() != 1) {
						chanced = true;
					}
					builder.add(EmiStack.of(output.getStack()));
				}
				this.output = builder.build();
			} else {
				this.output = List.of(EmiStack.of(recipe.getResultItem()));
			}
		}
	}

	public CreateEmiRecipe(EmiRecipeCategory category, T recipe, int width, int height, Consumer<CreateEmiRecipe<T>> setup) {
		this.category = category;
		this.recipe = recipe;
		this.id = recipe.getId();
		this.width = width;
		this.height = height;
		setup.accept(this);
	}

	@Override
	public EmiRecipeCategory getCategory() {
		return category;
	}

	@Override
	public @Nullable ResourceLocation getId() {
		return id;
	}

	@Override
	public List<EmiIngredient> getInputs() {
		return input;
	}

	@Override
	public List<EmiStack> getOutputs() {
		return output;
	}

	@Override
	public int getDisplayWidth() {
		return width;
	}

	@Override
	public int getDisplayHeight() {
		return height;
	}

	@Override
	public boolean supportsRecipeTree() {
		return !chanced && EmiRecipe.super.supportsRecipeTree();
	}

	public static SlotWidget addSlot(WidgetHolder widgets, EmiIngredient stack, int x, int y) {
		return addSlot(widgets, stack, x, y, AllGuiTextures.JEI_SLOT);
	}

	public static SlotWidget addSlot(WidgetHolder widgets, EmiIngredient stack, int x, int y, AllGuiTextures texture) {
		return widgets.addSlot(stack, x, y).backgroundTexture(texture.location, texture.startX, texture.startY);
	}

	public SlotWidget addChancedSlot(WidgetHolder widgets, EmiIngredient stack, int x, int y, float chance) {
		if (chance != 1) {
			return addSlot(widgets, stack, x, y, AllGuiTextures.JEI_CHANCE_SLOT)
				.appendTooltip(Lang.translate("recipe.processing.chance",
					chance < 0.01 ? "<1" : (int) (chance * 100))
					.withStyle(ChatFormatting.GOLD));
		}
		return addSlot(widgets, stack, x, y);
	}

	public SlotWidget addChancedSlot(WidgetHolder widgets, EmiIngredient stack, int x, int y, int index) {
		if (recipe instanceof ProcessingRecipe) {
			ProcessingRecipe<?> processing = (ProcessingRecipe<?>) recipe;
			List<ProcessingOutput> results = processing.getRollableResults();
			if (index < results.size()) {
				return addChancedSlot(widgets, stack, x, y, results.get(index).getChance());
			}
		}
		return addSlot(widgets, stack, x, y);
	}

	public static EmiStack fluidStack(FluidStack stack) {
		return EmiStack.of(stack.getType(), (int) stack.getAmount());
	}

	public static TextureWidget addTexture(WidgetHolder widgets, AllGuiTextures texture, int x, int y) {
		return widgets.addTexture(texture.location, x, y, texture.width, texture.height, texture.startX, texture.startY);
	}
}
