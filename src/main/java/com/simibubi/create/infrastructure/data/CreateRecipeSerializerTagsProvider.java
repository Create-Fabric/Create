package com.simibubi.create.infrastructure.data;

import com.simibubi.create.AllTags.AllRecipeSerializerTags;

import com.simibubi.create.Create;

import io.github.fabricators_of_create.porting_lib.data.ExistingFileHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.concurrent.CompletableFuture;

public class CreateRecipeSerializerTagsProvider extends TagsProvider<RecipeSerializer<?>> {
	public CreateRecipeSerializerTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
		super(output, Registries.RECIPE_SERIALIZER, lookupProvider);
	}

	@Override
	//fixme might need to remove args to this
	protected void addTags(Provider pProvider) {
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
