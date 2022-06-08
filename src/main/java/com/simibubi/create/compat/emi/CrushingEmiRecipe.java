package com.simibubi.create.compat.emi;

import com.simibubi.create.content.contraptions.components.crusher.AbstractCrushingRecipe;
import com.simibubi.create.foundation.gui.AllGuiTextures;

import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.resources.ResourceLocation;

public class CrushingEmiRecipe extends CreateEmiRecipe<AbstractCrushingRecipe> {

	public CrushingEmiRecipe(AbstractCrushingRecipe recipe) {
		super(CreateEmiPlugin.CRUSHING, recipe, 134, 110);
		ResourceLocation rid = recipe.getId();
		this.id = new ResourceLocation("emi", "create/crushing/" + rid.getNamespace() + "/" + rid.getPath());
	}

	@Override
	public void addWidgets(WidgetHolder widgets) {
		addTexture(widgets, AllGuiTextures.JEI_DOWN_ARROW, 51, 11);

		addSlot(widgets, input.get(0), 29, 6);
		
		int xOff = -output.size() * 19 / 2;
		for (int i = 0; i < output.size(); i++) {
			addChancedSlot(widgets, output.get(i), 67 + xOff + 19 * i, 82, i).recipeContext(this);
		}
		
		CreateEmiAnimations.addCrushingWheels(widgets, 41, 63);
	}
}
