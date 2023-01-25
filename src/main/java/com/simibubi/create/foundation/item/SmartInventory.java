package com.simibubi.create.foundation.item;

import com.simibubi.create.foundation.tileEntity.SyncedTileEntity;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandlerContainer;

import io.github.fabricators_of_create.porting_lib.util.INBTSerializable;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.nbt.CompoundTag;

public class SmartInventory extends ItemStackHandlerContainer implements INBTSerializable<CompoundTag> {

	protected boolean extractionAllowed;
	protected boolean insertionAllowed;
	protected boolean stackNonStackables;
	protected int stackSize;

	public SmartInventory(int slots, SyncedTileEntity te) {
		this(slots, te, 64, false);
	}

	public SmartInventory(int slots, SyncedTileEntity te, int stackSize, boolean stackNonStackables) {
		super(slots);
		this.stackNonStackables = stackNonStackables;
		insertionAllowed = true;
		extractionAllowed = true;
		this.stackSize = stackSize;
		this.te = te;
	}

	public SmartInventory withMaxStackSize(int maxStackSize) {
		stackSize = maxStackSize;
		return this;
	}

	public SmartInventory whenContentsChanged(Runnable updateCallback) {
		this.updateCallback = updateCallback;
		return this;
	}

	public SmartInventory allowInsertion() {
		insertionAllowed = true;
		return this;
	}

	public SmartInventory allowExtraction() {
		extractionAllowed = true;
		return this;
	}

	public SmartInventory forbidInsertion() {
		insertionAllowed = false;
		return this;
	}

	public SmartInventory forbidExtraction() {
		extractionAllowed = false;
		return this;
	}

	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		if (!insertionAllowed)
			return 0;
		return super.insert(resource, maxAmount, transaction);
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		if (!extractionAllowed)
			return 0;
		if (stackNonStackables) {
			try (Transaction t = transaction.openNested()) {
				long extracted = super.extract(resource, maxAmount, t);
				t.abort();
				if (extracted != 0 && resource.getItem().getMaxStackSize() < extracted)
					maxAmount = resource.getItem().getMaxStackSize();
			}
		}
		return super.extract(resource, maxAmount, transaction);
	}

	// fabric: merge SyncedStackHandler, it exists only to be wrapped, and removing it allows avoiding extending RecipeWrapper

	private SyncedTileEntity te;
	private Runnable updateCallback;

	@Override
	protected void onFinalCommit() {
		if (updateCallback != null)
			updateCallback.run();
		te.notifyUpdate();
	}

	@Override
	public int getSlotLimit(int slot) {
		return Math.min(stackNonStackables ? 64 : super.getSlotLimit(slot), stackSize);
	}

	@Override
	public int getMaxStackSize() {
		return Math.min(64, stackSize);
	}
}
