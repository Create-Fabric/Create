package com.simibubi.create.foundation.data;

import java.util.function.Function;
import java.util.stream.Stream;

import com.simibubi.create.AllTags;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.data.recipe.Mods;
import com.simibubi.create.foundation.mixin.fabric.TagAppenderAccessor;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.providers.RegistrateTagsProvider;
import com.tterrag.registrate.util.nullness.NonNullFunction;

import io.github.fabricators_of_create.porting_lib.tags.Tags;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;

import net.minecraft.core.HolderLookup;

import org.jetbrains.annotations.NotNull;

import net.minecraft.core.Holder;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.data.tags.TagsProvider.TagAppender;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class TagGen {
	public static <T extends Block, P> NonNullFunction<BlockBuilder<T, P>, BlockBuilder<T, P>> axeOrPickaxe() {
		return b -> b.tag(BlockTags.MINEABLE_WITH_AXE)
			.tag(BlockTags.MINEABLE_WITH_PICKAXE);
	}

	public static <T extends Block, P> NonNullFunction<BlockBuilder<T, P>, BlockBuilder<T, P>> axeOnly() {
		return b -> b.tag(BlockTags.MINEABLE_WITH_AXE);
	}

	public static <T extends Block, P> NonNullFunction<BlockBuilder<T, P>, BlockBuilder<T, P>> pickaxeOnly() {
		return b -> b.tag(BlockTags.MINEABLE_WITH_PICKAXE);
	}

	public static <T extends Block, P> NonNullFunction<BlockBuilder<T, P>, ItemBuilder<BlockItem, BlockBuilder<T, P>>> tagBlockAndItem(
		String... path) {
		return b -> {
			for (String p : path)
				b.tag(AllTags.forgeBlockTag(p));
			ItemBuilder<BlockItem, BlockBuilder<T, P>> item = b.item();
			for (String p : path)
				item.tag(AllTags.forgeItemTag(p));
			return item;
		};
	}

	public static <T extends TagAppender<?>> T addOptional(T appender, Mods mod, String id) {
		appender.addOptional(mod.asResource(id));
		return appender;
	}

	public static <T extends TagAppender<?>> T addOptional(T appender, Mods mod, String... ids) {
		for (String id : ids) {
			appender.addOptional(mod.asResource(id));
		}
		return appender;
	}

	public static class CreateTagsProvider<T> {

		private RegistrateTagsProvider<T> provider;
		private Function<T, ResourceKey<T>> keyExtractor;

		public CreateTagsProvider(RegistrateTagsProvider<T> provider, Function<T, Holder.Reference<T>> refExtractor) {
			this.provider = provider;
			this.keyExtractor = refExtractor.andThen(Holder.Reference::key);
		}

		public CreateTagAppender<T> tag(TagKey<T> tag) {
			return new CreateTagAppender<>(provider.addTag(tag), keyExtractor);
		}
		
		// fabric: this is just used to force datagen of tags
		public void getOrCreateRawBuilder(TagKey<T> tag) {
			this.tag(tag);
		}
	}

	public static class CreateTagAppender<T> extends TagsProvider.TagAppender<T> {

		private Function<T, ResourceKey<T>> keyExtractor;
		// fabric: take the fabric builder, use it to call forceAddTag instead of addTag
		private final FabricTagProvider<T>.FabricTagBuilder fabricBuilder;

		public CreateTagAppender(FabricTagProvider<T>.FabricTagBuilder fabricBuilder, Function<T, ResourceKey<T>> pKeyExtractor) {
			super(getBuilder(fabricBuilder));
			this.keyExtractor = pKeyExtractor;
			this.fabricBuilder = fabricBuilder;
		}

		private static TagBuilder getBuilder(TagAppender<?> appender) {
			return ((TagAppenderAccessor) appender).getBuilder();
		}

		public CreateTagAppender<T> add(T entry) {
			this.add(this.keyExtractor.apply(entry));
			return this;
		}

		@SafeVarargs
		public final CreateTagAppender<T> add(T... entries) {
			Stream.<T>of(entries)
				.map(this.keyExtractor)
				.forEach(this::add);
			return this;
		}

		@Override
		@NotNull
		public TagAppender<T> addTag(@NotNull TagKey<T> tag) {
			this.fabricBuilder.forceAddTag(tag);
			return this;
		}
	}
}
