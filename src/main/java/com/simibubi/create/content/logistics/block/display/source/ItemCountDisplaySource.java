package com.simibubi.create.content.logistics.block.display.source;

import com.simibubi.create.content.logistics.block.display.DisplayLinkContext;
import com.simibubi.create.content.logistics.block.display.target.DisplayTargetStats;
import com.simibubi.create.content.logistics.block.redstone.ContentObserverTileEntity;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.inventory.InvManipulationBehaviour;
import com.simibubi.create.foundation.utility.Components;

import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ItemCountDisplaySource extends NumericSingleLineDisplaySource {

	@Override
	protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
		BlockEntity sourceTE = context.getSourceTE();
		if (!(sourceTE instanceof ContentObserverTileEntity cote))
			return ZERO.copy();

		InvManipulationBehaviour invManipulationBehaviour = cote.getBehaviour(InvManipulationBehaviour.TYPE);
		FilteringBehaviour filteringBehaviour = cote.getBehaviour(FilteringBehaviour.TYPE);
		Storage<ItemVariant> handler = invManipulationBehaviour.getInventory();

		if (handler == null)
			return ZERO.copy();

		int collected = 0;
		try (Transaction t = TransferUtil.getTransaction()) {
			for (StorageView<ItemVariant> view : TransferUtil.getNonEmpty(handler, t)) {
				if (!filteringBehaviour.test(view.getResource().toStack()))
					continue;
				// extract to avoid counting multiple times
				collected += view.extract(view.getResource(), view.getAmount(), t);
			}
		}

		return Components.literal(String.valueOf(collected));
	}

	@Override
	protected String getTranslationKey() {
		return "count_items";
	}

	@Override
	protected boolean allowsLabeling(DisplayLinkContext context) {
		return true;
	}

}
