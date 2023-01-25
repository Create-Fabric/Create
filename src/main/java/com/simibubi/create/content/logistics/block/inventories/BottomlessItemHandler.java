package com.simibubi.create.content.logistics.block.inventories;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

import javax.annotation.ParametersAreNonnullByDefault;

import io.github.fabricators_of_create.porting_lib.transfer.item.ItemHandlerHelper;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandlerSnapshot;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BottomlessItemHandler extends ItemStackHandler implements SingleSlotStorage<ItemVariant> { // must extend ItemStackHandler for mounted storages

	private Supplier<ItemStack> suppliedItemStack;
	private List<? extends StorageView<ItemVariant>> self;

	public BottomlessItemHandler(Supplier<ItemStack> suppliedItemStack) {
		this.suppliedItemStack = suppliedItemStack;
		self = List.of(this);
	}

	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		return maxAmount;
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		ItemStack stack = suppliedItemStack.get();
		if (stack == null || !resource.matches(stack))
			return 0;
		if (!stack.isEmpty())
			return Math.min(stack.getMaxStackSize(), maxAmount);
		return 0;
	}

	@Override
	public boolean isResourceBlank() {
		return getResource().isBlank();
	}

	@Override
	public ItemVariant getResource() {
		ItemStack stack = suppliedItemStack.get();
		return stack == null || stack.isEmpty() ? ItemVariant.blank() : ItemVariant.of(stack);
	}

	@Override
	public long getAmount() {
		return Long.MAX_VALUE;
	}

	@Override
	public long getCapacity() {
		return Long.MAX_VALUE;
	}

	@Override
	public Iterator<StorageView<ItemVariant>> iterator(TransactionContext transaction) {
		return SingleSlotStorage.super.iterator(transaction); // no, this is not pointless
	}

	@Override
	public Iterator<? extends StorageView<ItemVariant>> nonEmptyViews() {
		return isResourceBlank() ? Collections.emptyIterator() : self.iterator();
	}

	@Override
	public void setStackInSlot(int slot, ItemStack stack) {
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		ItemStack stack = suppliedItemStack.get();
		if (stack == null)
			return ItemStack.EMPTY;
		if (!stack.isEmpty())
			return ItemHandlerHelper.copyStackWithSize(stack, stack.getMaxStackSize());
		return stack;
	}

	@Override
	protected ItemStackHandlerSnapshot createSnapshot() {
		return BottomlessSnapshotData.INSTANCE;
	}

	public static class BottomlessSnapshotData implements ItemStackHandlerSnapshot {
		public static final BottomlessSnapshotData INSTANCE = new BottomlessSnapshotData();
		private BottomlessSnapshotData() {
		}

		@Override
		public void apply(ItemStackHandler handler) {
		}
	}
}
