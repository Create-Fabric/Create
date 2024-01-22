package com.simibubi.create.compat.emi.recipes;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.compat.emi.CreateSlotWidget;
import com.simibubi.create.compat.emi.EmiSequencedAssemblySubCategory;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import com.simibubi.create.content.processing.sequenced.SequencedRecipe;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.utility.Pair;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.TextureWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import io.github.fabricators_of_create.porting_lib.util.FluidStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

public abstract class CreateEmiRecipe<T extends Recipe<?>> implements EmiRecipe {
	protected final EmiRecipeCategory category;
	protected final T recipe;
	protected ResourceLocation id;
	protected List<EmiIngredient> input;
	protected List<EmiStack> output;
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
		} else if (recipe instanceof SequencedAssemblyRecipe sequenced) {
			this.output = List.of(getResultEmi(recipe).setChance(sequenced.getOutputChance()));
			int loops = sequenced.getLoops();
			ImmutableList.Builder<EmiIngredient> input = ImmutableList.builder();
			input.add(EmiIngredient.of(sequenced.getIngredient()));
			for (SequencedRecipe<?> sequence : sequenced.getSequence()) {
				EmiSequencedAssemblySubCategory subCategory = SequencedAssemblyEmiRecipe.getSubCategory(sequence);
				EmiIngredient ingredient = subCategory.getAppliedIngredient(sequence);
				if (ingredient == null)
					continue;
				input.add(ingredient);
				if (ingredient instanceof EmiStack stack && stack.getRemainder() == stack)
					continue;
				ingredient.setAmount(ingredient.getAmount() * loops);
			}
			this.input = input.build();
		} else {
			this.input = recipe.getIngredients().stream().map(EmiIngredient::of).toList();
			if (recipe instanceof ProcessingRecipe<?> processing) {
				ImmutableList.Builder<EmiStack> builder = ImmutableList.builder();
				for (ProcessingOutput output : processing.getRollableResults()) {
					builder.add(EmiStack.of(output.getStack()).setChance(output.getChance()));
				}
				this.output = builder.build();
			} else {
				this.output = List.of(getResultEmi(recipe));
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

	public static SlotWidget addSlot(WidgetHolder widgets, EmiIngredient stack, int x, int y) {
		AllGuiTextures texture = stack.getChance() == 1 ? AllGuiTextures.JEI_SLOT : AllGuiTextures.JEI_CHANCE_SLOT;
		return addSlot(widgets, stack, x, y, texture);
	}

	public static SlotWidget addSlot(WidgetHolder widgets, EmiIngredient stack, int x, int y, AllGuiTextures texture) {
		return widgets.add(new CreateSlotWidget(stack, x, y)).backgroundTexture(texture.location, texture.startX, texture.startY);
	}

	public static EmiStack fluidStack(FluidStack stack) {
		return EmiStack.of(stack.getFluid(), stack.getAmount());
	}

	public static TextureWidget addTexture(WidgetHolder widgets, AllGuiTextures texture, int x, int y) {
		return widgets.addTexture(texture.location, x, y, texture.width, texture.height, texture.startX, texture.startY);
	}

	public static <T, U> U firstOrElse(List<T> list, U empty, Function<T, U> function) {
		return list.isEmpty() ? empty : function.apply(list.get(0));
	}

	public static EmiIngredient firstIngredientOrEmpty(List<Ingredient> ingredients) {
		return firstOrElse(ingredients, EmiStack.EMPTY, EmiIngredient::of);
	}

	public static EmiStack firstFluidOrEmpty(List<FluidStack> fluids) {
		return firstOrElse(fluids, EmiStack.EMPTY, CreateEmiRecipe::fluidStack);
	}

	public static EmiStack firstResultOrEmpty(List<ProcessingOutput> outputs) {
		return firstOrElse(outputs, EmiStack.EMPTY, output -> EmiStack.of(output.getStack()));
	}

	public static ItemStack getResultItem(Recipe<?> recipe) {
		ClientLevel level = Minecraft.getInstance().level;
		if (level == null)
			return ItemStack.EMPTY;
		return recipe.getResultItem(level.registryAccess());
	}

	public static EmiStack getResultEmi(Recipe<?> recipe) {
		return EmiStack.of(getResultItem(recipe));
	}
}
