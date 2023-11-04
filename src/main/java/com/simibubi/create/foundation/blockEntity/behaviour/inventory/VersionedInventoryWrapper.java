package com.simibubi.create.foundation.blockEntity.behaviour.inventory;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import io.github.fabricators_of_create.porting_lib.transfer.item.ItemHandlerHelper;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

public class VersionedInventoryWrapper implements Storage<ItemVariant> {

	public static final AtomicInteger idGenerator = new AtomicInteger();

	private Storage<ItemVariant> inventory;
	//private int version;
	private int id;

	public VersionedInventoryWrapper(Storage<ItemVariant> inventory) {
		this.id = idGenerator.getAndIncrement();
		this.inventory = inventory;
	}

	public int getId() {
		return id;
	}

	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		return inventory.insert(resource, maxAmount, transaction);
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		return inventory.extract(resource, maxAmount, transaction);
	}

	@Override
	public boolean supportsInsertion() {
		return inventory.supportsInsertion();
	}

	@Override
	public long simulateInsert(ItemVariant resource, long maxAmount, @Nullable TransactionContext transaction) {
		return inventory.simulateInsert(resource, maxAmount, transaction);
	}

	@Override
	public boolean supportsExtraction() {
		return inventory.supportsExtraction();
	}

	@Override
	public long simulateExtract(ItemVariant resource, long maxAmount, @Nullable TransactionContext transaction) {
		return inventory.simulateExtract(resource, maxAmount, transaction);
	}

	@Override
	public Iterator<StorageView<ItemVariant>> iterator() {
		return inventory.iterator();
	}

	@Override
	public @Nullable StorageView<ItemVariant> exactView(ItemVariant resource) {
		return inventory.exactView(resource);
	}

	@Override
	public long getVersion() {
		return inventory.getVersion();
	}
}
