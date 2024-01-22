package com.simibubi.create.foundation.ponder;

import java.util.HashMap;
import java.util.Map;

import com.mojang.serialization.Codec;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;

import io.github.fabricators_of_create.porting_lib.util.FluidStack;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

/**
 * Processing for Ponder schematics to allow using the same ones on Forge and Fabric.
 */
public class FabricPonderProcessing {
	public static final Codec<Processor> PROCESSOR_CODEC = ResourceLocation.CODEC
			.fieldOf("structureId")
			.xmap(Processor::new, processor -> processor.structureId)
			.codec();

	public static final StructureProcessorType<Processor> PROCESSOR_TYPE = Registry.register(
			BuiltInRegistries.STRUCTURE_PROCESSOR,
			Create.asResource("fabric_ponder_processor"),
			() -> PROCESSOR_CODEC
	);

	/**
	 * A predicate that makes all processes apply to all schematics.
	 */
	public static final ProcessingPredicate ALWAYS = (id, process) -> true;

	private static final Map<String, ProcessingPredicate> predicates = new HashMap<>();

	/**
	 * Register a {@link ProcessingPredicate} for a mod.
	 * Only one predicate may be registered for each mod.
	 * The predicate determines which {@link Process}es will be applied to which schematics.
	 */
	public static ProcessingPredicate register(String modId, ProcessingPredicate predicate) {
		ProcessingPredicate existing = predicates.get(modId);
		if (existing != null) {
			throw new IllegalStateException(
					"Tried to register ProcessingPredicate [%s] for mod '%s', while one already exists: [%s]"
							.formatted(predicate, modId, existing)
			);
		}
	    predicates.put(modId, predicate);
		return predicate;
	}

	public static StructurePlaceSettings makePlaceSettings(ResourceLocation structureId) {
		return new StructurePlaceSettings().addProcessor(new Processor(structureId));
	}

	@Internal
	public static void init() {
		register(Create.ID, ALWAYS);
	}

	public enum Process {
		FLUID_TANK_AMOUNTS
	}

	@FunctionalInterface
	public interface ProcessingPredicate {
		boolean shouldApplyProcess(ResourceLocation schematicId, Process process);
	}

	public static class Processor extends StructureProcessor {
		public final ResourceLocation structureId;

		public Processor(ResourceLocation structureId) {
			this.structureId = structureId;
		}

		@Nullable
		@Override
		public StructureTemplate.StructureBlockInfo processBlock(
				@NotNull LevelReader level, @NotNull BlockPos pos, @NotNull BlockPos pivot,
				@NotNull StructureBlockInfo blockInfo, @NotNull StructureBlockInfo relativeBlockInfo,
				@NotNull StructurePlaceSettings settings) {
			ProcessingPredicate predicate = predicates.get(structureId.getNamespace());
			if (predicate == null) // do nothing
				return relativeBlockInfo;

			CompoundTag nbt = relativeBlockInfo.nbt();
			if (nbt != null
					&& AllBlocks.FLUID_TANK.has(relativeBlockInfo.state())
					&& nbt.contains("TankContent", Tag.TAG_COMPOUND)
					&& predicate.shouldApplyProcess(structureId, Process.FLUID_TANK_AMOUNTS)) {

				FluidStack content = FluidStack.loadFluidStackFromNBT(nbt.getCompound("TankContent"));
				long amount = content.getAmount();
				float buckets = amount / 1000f;
				long fixedAmount = (long) (buckets * FluidConstants.BUCKET);
				content.setAmount(fixedAmount);

				CompoundTag newNbt = nbt.copy();
				newNbt.put("TankContent", content.writeToNBT(new CompoundTag()));
				return new StructureBlockInfo(relativeBlockInfo.pos(), relativeBlockInfo.state(), newNbt);
			}

			// no processes were applied
			return relativeBlockInfo;
		}

		@Override
		@NotNull
		protected StructureProcessorType<?> getType() {
			return PROCESSOR_TYPE;
		}
	}
}
