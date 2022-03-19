package com.simibubi.create.content.contraptions.components.actors;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;

import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.foundation.item.ItemHandlerWrapper;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class PortableItemInterfaceTileEntity extends PortableStorageInterfaceTileEntity {

	protected InterfaceItemHandler capability = new InterfaceItemHandler(Storage.empty());

	public PortableItemInterfaceTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public void startTransferringTo(Contraption contraption, float distance) {
//		LazyOptional<IItemHandlerModifiable> oldCap = capability;
//		InterfaceItemHandler handler = ((InterfaceItemHandler) capability.orElse(null));
		capability.setWrapped(contraption.inventory);
//		oldCap.invalidate();
		super.startTransferringTo(contraption, distance);
	}

	@Override
	protected void stopTransferring() {
//		LazyOptional<IItemHandlerModifiable> oldCap = capability;
//		InterfaceItemHandler handler = ((InterfaceItemHandler) capability.orElse(null));
		capability.setWrapped(Storage.empty());
//		oldCap.invalidate();
		super.stopTransferring();
	}

	private InterfaceItemHandler createEmptyHandler() {
		return capability;
	}

	@Override
	protected void invalidateCapability() {
		capability.setWrapped(Storage.empty());
	}

	class InterfaceItemHandler extends ItemHandlerWrapper {

		public InterfaceItemHandler(Storage<ItemVariant> wrapped) {
			super(wrapped);
		}

		@Override
		public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
			if (!canTransfer())
				return 0;
			long extracted = super.extract(resource, maxAmount, transaction);
			if (extracted != 0) {
				transaction.addOuterCloseCallback(result -> {
					if (result.wasCommitted())
						onContentTransferred();
				});
			}
			return extracted;
		}

		@Override
		public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
			if (!canTransfer())
				return 0;
			long inserted = super.insert(resource, maxAmount, transaction);
			if (inserted != 0) {
				transaction.addOuterCloseCallback(result -> {
					if (result.wasCommitted())
						onContentTransferred();
				});
			}
			return inserted;
		}

		private void setWrapped(Storage<ItemVariant> wrapped) {
			this.wrapped = wrapped;
		}
	}
}
