package com.simibubi.create.compat.rei.category;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.simibubi.create.compat.rei.category.animations.AnimatedKinetics;
import com.simibubi.create.compat.rei.display.CreateDisplay;
import com.simibubi.create.content.kinetics.deployer.ItemApplicationRecipe;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import com.simibubi.create.foundation.utility.Lang;

import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.util.ClientEntryStacks;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class ItemApplicationCategory extends CreateRecipeCategory<ItemApplicationRecipe> {

	public ItemApplicationCategory(Info<ItemApplicationRecipe> info) {
		super(info);
	}

	@Override
	public void addWidgets(CreateDisplay<ItemApplicationRecipe> display, List<Widget> ingredients, Point origin) {
		ingredients.add(basicSlot( 27, 38, origin)
				.markInput()
				.entries(EntryIngredients.ofIngredient(display.getRecipe().getProcessedItem())));

		Slot slot = basicSlot(51, 5, origin)
				.markInput()
				.entries(EntryIngredients.ofIngredient(display.getRecipe().getRequiredHeldItem()));
		ClientEntryStacks.setTooltipProcessor(slot.getCurrentEntry(), (entryStack, tooltip) -> {
			if (display.getRecipe().shouldKeepHeldItem())
					tooltip.add(Lang.translateDirect("recipe.deploying.not_consumed")
					.withStyle(ChatFormatting.GOLD));
			return tooltip;
		});
		ingredients.add(slot);

		display.getRecipe().getRollableResults().stream().limit(1).forEach(result -> {
			Slot outputSlot = basicSlot(132, 38, origin)
					.markOutput()
					.entries(EntryIngredients.of(result.getStack()));
			ingredients.add(outputSlot);
			addStochasticTooltip(outputSlot, result);
		});
	}

	@Override
	public void draw(ItemApplicationRecipe recipe, CreateDisplay<ItemApplicationRecipe> display, PoseStack matrixStack,
		double mouseX, double mouseY) {
		AllGuiTextures.JEI_SLOT.render(matrixStack, 50, 4);
		AllGuiTextures.JEI_SLOT.render(matrixStack, 26, 37);
		getRenderedSlot(recipe, 0).render(matrixStack, 131, 37);
		AllGuiTextures.JEI_SHADOW.render(matrixStack, 62, 47);
		AllGuiTextures.JEI_DOWN_ARROW.render(matrixStack, 74, 10);

		EntryIngredient displayedIngredient = display.getInputEntries().get(0);
		if (displayedIngredient.isEmpty())
			return;

		Item item = ((ItemStack)displayedIngredient.get(0).getValue()).getItem();
		if (!(item instanceof BlockItem blockItem))
			return;

		BlockState state = blockItem.getBlock()
			.defaultBlockState();

		matrixStack.pushPose();
		matrixStack.translate(74, 51, 100);
		matrixStack.mulPose(Vector3f.XP.rotationDegrees(-15.5f));
		matrixStack.mulPose(Vector3f.YP.rotationDegrees(22.5f));
		int scale = 20;

		GuiGameElement.of(state)
			.lighting(AnimatedKinetics.DEFAULT_LIGHTING)
			.scale(scale)
			.render(matrixStack);

		matrixStack.popPose();
	}

}
