package com.simibubi.create.content.logistics.trains.management.schedule.condition;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.processing.EmptyingByBasin;
import com.simibubi.create.content.logistics.item.filter.FilterItem;
import com.simibubi.create.content.logistics.trains.entity.Carriage;
import com.simibubi.create.content.logistics.trains.entity.Train;
import com.simibubi.create.foundation.fluid.CombinedTankWrapper;
import com.simibubi.create.foundation.gui.ModularGuiLineBuilder;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;

import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.util.FluidStack;
import io.github.fabricators_of_create.porting_lib.util.NBTSerializer;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public class FluidThresholdCondition extends CargoThresholdCondition {
	public ItemStack compareStack = ItemStack.EMPTY;
	public FluidStack fluidStack = null;

	@Override
	protected Component getUnit() {
		return Components.literal("b");
	}

	@Override
	protected ItemStack getIcon() {
		return compareStack;
	}

	@Override
	protected boolean test(Level level, Train train, CompoundTag context) {
		Ops operator = getOperator();
		long target = getThreshold();

		int foundFluid = 0;
		for (Carriage carriage : train.carriages) {
			CombinedTankWrapper fluids = carriage.storage.getFluids();
			try (Transaction t = TransferUtil.getTransaction()) {
				for (StorageView<FluidVariant> view : TransferUtil.getNonEmpty(fluids, t)) {
					FluidStack fluidInTank = new FluidStack(view);
					if (!FilterItem.test(level, fluidInTank, compareStack))
						continue;
					foundFluid += fluidInTank.getAmount();
				}
			}
		}

		requestStatusToUpdate(foundFluid / FluidConstants.BUCKET, context);
		return operator.test(foundFluid, target * FluidConstants.BUCKET);
	}

	@Override
	protected void writeAdditional(CompoundTag tag) {
		super.writeAdditional(tag);
		tag.put("Bucket", NBTSerializer.serializeNBTCompound(compareStack));
	}

	@Override
	protected void readAdditional(CompoundTag tag) {
		super.readAdditional(tag);
		compareStack = ItemStack.of(tag.getCompound("Bucket"));
	}

	@Override
	public boolean tickCompletion(Level level, Train train, CompoundTag context) {
		return super.tickCompletion(level, train, context);
	}

	@Environment(EnvType.CLIENT)
	private FluidStack loadFluid() {
		if (fluidStack != null)
			return fluidStack;
		fluidStack = FluidStack.EMPTY;
		if (!EmptyingByBasin.canItemBeEmptied(Minecraft.getInstance().level, compareStack))
			return fluidStack;
		FluidStack fluidInFilter = EmptyingByBasin.emptyItem(Minecraft.getInstance().level, compareStack, true)
			.getFirst();
		if (fluidInFilter == null)
			return fluidStack;
		return fluidStack = fluidInFilter;
	}

	@Override
	public List<Component> getTitleAs(String type) {
		return ImmutableList.of(
			Lang.translateDirect("schedule.condition.threshold.train_holds",
				Lang.translateDirect("schedule.condition.threshold." + Lang.asId(getOperator().name()))),
			Lang.translateDirect("schedule.condition.threshold.x_units_of_item", getThreshold(),
				Lang.translateDirect("schedule.condition.threshold.buckets"),
				compareStack.isEmpty() ? Lang.translateDirect("schedule.condition.threshold.anything")
					: compareStack.getItem() instanceof FilterItem
						? Lang.translateDirect("schedule.condition.threshold.matching_content")
						: loadFluid().getDisplayName())
				.withStyle(ChatFormatting.DARK_AQUA));
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		compareStack = stack;
	}

	@Override
	public ItemStack getItem(int slot) {
		return compareStack;
	}

	@Override
	public ResourceLocation getId() {
		return Create.asResource("fluid_threshold");
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void initConfigurationWidgets(ModularGuiLineBuilder builder) {
		super.initConfigurationWidgets(builder);
		builder.addSelectionScrollInput(71, 50, (i, l) -> {
			i.forOptions(ImmutableList.of(Lang.translateDirect("schedule.condition.threshold.buckets")))
				.titled(null);
		}, "Measure");
	}

	@Override
	public MutableComponent getWaitingStatus(Level level, Train train, CompoundTag tag) {
		long lastDisplaySnapshot = getLastDisplaySnapshot(tag);
		if (lastDisplaySnapshot == -1)
			return Components.empty();
		int offset = getOperator() == Ops.LESS ? -1 : getOperator() == Ops.GREATER ? 1 : 0;
		return Lang.translateDirect("schedule.condition.threshold.status", lastDisplaySnapshot,
			Math.max(0, getThreshold() + offset), Lang.translateDirect("schedule.condition.threshold.buckets"));
	}

}
