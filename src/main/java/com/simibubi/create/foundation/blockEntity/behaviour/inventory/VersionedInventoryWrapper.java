package com.simibubi.create.foundation.blockEntity.behaviour.inventory;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

import org.jetbrains.annotations.NotNull;
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
	public long getVersion() {
		return inventory.getVersion();
	}

	//

	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		return inventory.insert(resource, maxAmount, transaction);
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		return inventory.extract(resource, maxAmount, transaction);
	}

	@Override
	@NotNull
	public Iterator<StorageView<ItemVariant>> iterator() {
		return inventory.iterator();
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
	@Nullable
	public StorageView<ItemVariant> exactView(ItemVariant resource) {
		return inventory.exactView(resource);
	}
}
