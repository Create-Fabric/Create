package com.simibubi.create.infrastructure.data;

import com.simibubi.create.AllTags.AllRecipeSerializerTags;

import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class CreateRecipeSerializerTagsProvider extends TagsProvider<RecipeSerializer<?>> {
	public CreateRecipeSerializerTagsProvider(DataGenerator generator) {
		super(generator, Registry.RECIPE_SERIALIZER);
	}

	@Override
	protected void addTags() {
//		tag(AllRecipeSerializerTags.AUTOMATION_IGNORE.tag)
//			.addOptional(Mods.OCCULTISM.rl("spirit_trade"))
//			.addOptional(Mods.OCCULTISM.rl("ritual"));

		// VALIDATE

		for (AllRecipeSerializerTags tag : AllRecipeSerializerTags.values()) {
			if (tag.alwaysDatagen) {
				getOrCreateRawBuilder(tag.tag);
			}
		}
	}

	@Override
	public String getName() {
		return "Create's Recipe Serializer Tags";
	}
}
