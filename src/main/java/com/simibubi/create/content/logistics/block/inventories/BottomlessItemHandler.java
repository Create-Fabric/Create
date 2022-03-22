package com.simibubi.create.content.logistics.block.inventories;

import java.util.function.Supplier;

import javax.annotation.ParametersAreNonnullByDefault;

import io.github.fabricators_of_create.porting_lib.transfer.item.ItemHandlerHelper;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BottomlessItemHandler extends ItemStackHandler {

	private Supplier<ItemStack> suppliedItemStack;

	public BottomlessItemHandler(Supplier<ItemStack> suppliedItemStack) {
		this.suppliedItemStack = suppliedItemStack;
	}

//	@Override
//	public int getSlots() {
//		return 2;
//	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		ItemStack stack = suppliedItemStack.get();
//		if (slot == 1)
//			return ItemStack.EMPTY;
		if (stack == null)
			return ItemStack.EMPTY;
		if (!stack.isEmpty())
			return ItemHandlerHelper.copyStackWithSize(stack, stack.getMaxStackSize());
		return stack;
	}

	@Override
	public void setStackInSlot(int slot, ItemStack stack) {}

	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		return maxAmount;
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		ItemStack stack = suppliedItemStack.get();
//		if (slot == 1)
//			return ItemStack.EMPTY;
		if (stack == null)
			return 0;
		if (!stack.isEmpty())
			return Math.min(stack.getMaxStackSize(), maxAmount);
		return 0;
	}

	@Override
	public boolean isItemValid(int slot, ItemVariant stack) {
		return true;
	}
}
