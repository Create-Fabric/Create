package com.simibubi.create.compat.jei.category;

import java.util.Collection;
import java.util.function.Consumer;

import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.Create;
import com.simibubi.create.compat.jei.category.animations.AnimatedSpout;
import com.simibubi.create.content.fluids.potion.PotionFluidHandler;
import com.simibubi.create.content.fluids.transfer.FillingRecipe;
import com.simibubi.create.content.fluids.transfer.GenericItemFilling;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.utility.RegisteredObjects;

import com.simibubi.create.foundation.item.ItemHelper;

import io.github.fabricators_of_create.porting_lib.transfer.MutableContainerItemContext;
import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.fabric.constants.FabricTypes;
import mezz.jei.api.fabric.ingredients.fluids.IJeiFluidIngredient;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientManager;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.crafting.Ingredient;

@ParametersAreNonnullByDefault
public class SpoutCategory extends CreateRecipeCategory<FillingRecipe> {

	private final AnimatedSpout spout = new AnimatedSpout();

	public SpoutCategory(Info<FillingRecipe> info) {
		super(info);
	}

	public static void consumeRecipes(Consumer<FillingRecipe> consumer, IIngredientManager ingredientManager) {
		Collection<IJeiFluidIngredient> fluidStacks = ingredientManager.getAllIngredients(FabricTypes.FLUID_STACK);
		for (ItemStack stack : ingredientManager.getAllIngredients(VanillaTypes.ITEM_STACK)) {
			if (stack.getItem() instanceof PotionItem) {
				FluidStack fluidFromPotionItem = PotionFluidHandler.getFluidFromPotionItem(stack);
				Ingredient bottle = Ingredient.of(Items.GLASS_BOTTLE);
				consumer.accept(new ProcessingRecipeBuilder<>(FillingRecipe::new, Create.asResource("potions"))
					.withItemIngredients(bottle)
					.withFluidIngredients(FluidIngredient.fromFluidStack(fluidFromPotionItem))
					.withSingleItemOutput(stack)
					.build());
				continue;
			}

			ContainerItemContext testCtx = ContainerItemContext.withConstant(stack);
			Storage<FluidVariant> testStorage = testCtx.find(FluidStorage.ITEM);
			if (testStorage == null)
				continue;

			for (IJeiFluidIngredient ingredient : fluidStacks) {
				FluidStack fluidStack = fromJei(ingredient);
				ItemStack copy = stack.copy();
				MutableContainerItemContext ctx = new MutableContainerItemContext(copy);
				Storage<FluidVariant> storage = ctx.find(FluidStorage.ITEM);
				if (!GenericItemFilling.isFluidHandlerValid(copy, storage))
					continue;
				FluidStack fluidCopy = fluidStack.copy();
				fluidCopy.setAmount(FluidConstants.BUCKET);
				TransferUtil.insertFluid(storage, fluidCopy);
				ItemVariant container = ctx.getItemVariant();
				if (copy.is(container.getItem()))
					continue;
				if (container.isBlank())
					continue;

				Ingredient bucket = Ingredient.of(stack);
				ResourceLocation itemName = RegisteredObjects.getKeyOrThrow(stack.getItem()
						);
				ResourceLocation fluidName = RegisteredObjects.getKeyOrThrow(fluidCopy.getFluid()
						);
				consumer.accept(new ProcessingRecipeBuilder<>(FillingRecipe::new,
						Create.asResource("fill_" + itemName.getNamespace() + "_" + itemName.getPath()
								+ "_with_" + fluidName.getNamespace() + "_" + fluidName.getPath()))
						.withItemIngredients(bucket)
						.withFluidIngredients(FluidIngredient.fromFluidStack(fluidCopy))
						.withSingleItemOutput(container.toStack(ItemHelper.truncateLong(ctx.getAmount())))
						.build());
			}
		}
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, FillingRecipe recipe, IFocusGroup focuses) {
		builder
				.addSlot(RecipeIngredientRole.INPUT, 27, 51)
				.setBackground(getRenderedSlot(), -1, -1)
				.addIngredients(recipe.getIngredients().get(0));
		builder
				.addSlot(RecipeIngredientRole.INPUT, 27, 32)
				.setBackground(getRenderedSlot(), -1, -1)
				.addIngredients(FabricTypes.FLUID_STACK, toJei(withImprovedVisibility(recipe.getRequiredFluid().getMatchingFluidStacks())))
				.addTooltipCallback(addFluidTooltip(recipe.getRequiredFluid().getRequiredAmount()));
		builder
				.addSlot(RecipeIngredientRole.OUTPUT, 132, 51)
				.setBackground(getRenderedSlot(), -1, -1)
				.addItemStack(getResultItem(recipe));
	}

	@Override
	public void draw(FillingRecipe recipe, IRecipeSlotsView iRecipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
		AllGuiTextures.JEI_SHADOW.render(graphics, 62, 57);
		AllGuiTextures.JEI_DOWN_ARROW.render(graphics, 126, 29);
		spout.withFluids(recipe.getRequiredFluid()
			.getMatchingFluidStacks())
			.draw(graphics, getBackground().getWidth() / 2 - 13, 22);
	}

}
