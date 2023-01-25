package com.simibubi.create.foundation.item;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.utility.LongAttached;

import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemHandlerHelper;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class CountedItemStackList {

	Map<Item, Set<ItemStackEntry>> items = new HashMap<>();

	public CountedItemStackList(Storage<ItemVariant> inventory, FilteringBehaviour filteringBehaviour) {
		try (Transaction t = TransferUtil.getTransaction()){
			for (StorageView<ItemVariant> view : TransferUtil.getNonEmpty(inventory, t)) {
				if (filteringBehaviour.test(view.getResource().toStack()))
					add(view.getResource().toStack(ItemHelper.truncateLong(view.getAmount())));
			}
		}
	}

	public Stream<LongAttached<MutableComponent>> getTopNames(int limit) {
		return items.values()
			.stream()
			.flatMap(Collection::stream)
			.sorted(LongAttached.comparator())
			.limit(limit)
			.map(entry -> LongAttached.with(entry.count(), entry.stack()
				.getHoverName()
				.copy()));
	}

	public void add(ItemStack stack) {
		add(stack, stack.getCount());
	}

	public void add(ItemStack stack, int amount) {
		if (stack.isEmpty())
			return;

		Set<ItemStackEntry> stackSet = getOrCreateItemSet(stack);
		for (ItemStackEntry entry : stackSet) {
			if (!entry.matches(stack))
				continue;
			entry.grow(amount);
			return;
		}
		stackSet.add(new ItemStackEntry(stack, amount));
	}

	private Set<ItemStackEntry> getOrCreateItemSet(ItemStack stack) {
		if (!items.containsKey(stack.getItem()))
			items.put(stack.getItem(), new HashSet<>());
		return getItemSet(stack);
	}

	private Set<ItemStackEntry> getItemSet(ItemStack stack) {
		return items.get(stack.getItem());
	}

	public static class ItemStackEntry extends LongAttached<ItemStack> {

		public ItemStackEntry(ItemStack stack) {
			this(stack, stack.getCount());
		}

		public ItemStackEntry(ItemStack stack, long amount) {
			super(amount, stack);
		}

		public boolean matches(ItemStack other) {
			return ItemHandlerHelper.canItemStacksStack(other, stack());
		}

		public ItemStack stack() {
			return getSecond();
		}

		public void grow(long amount) {
			setFirst(getFirst() + amount);
		}

		public long count() {
			return getFirst();
		}

	}

}
