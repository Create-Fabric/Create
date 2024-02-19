package com.simibubi.create.content.contraptions;

import java.util.List;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.simibubi.create.foundation.utility.Lang;

import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

public class MountedStorageInteraction {

	public static final List<MenuType<?>> menus = ImmutableList.of(MenuType.GENERIC_9x1, MenuType.GENERIC_9x2,
		MenuType.GENERIC_9x3, MenuType.GENERIC_9x4, MenuType.GENERIC_9x5, MenuType.GENERIC_9x6);

	public static MenuProvider createMenuProvider(Component displayName, ItemStackHandler primary, @Nullable ItemStackHandler secondary,
		int slotCount, Supplier<Boolean> stillValid) {
		int rows = Mth.clamp(slotCount / 9, 1, 6);
		MenuType<?> menuType = menus.get(rows - 1);
		Component menuName = Lang.translateDirect("contraptions.moving_container", displayName);

		return new MenuProvider() {

			@Override
			public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
				return new ChestMenu(menuType, pContainerId, pPlayerInventory, new StorageInteractionContainer(primary, secondary, stillValid),
					rows);
			}

			@Override
			public Component getDisplayName() {
				return menuName;
			}

		};
	}

	public static class StorageInteractionContainer implements Container {

		private Supplier<Boolean> stillValid;
		private final ItemStackHandler primary;
		@Nullable
		private final ItemStackHandler secondary; // for double chests


		public StorageInteractionContainer(ItemStackHandler primary, @Nullable ItemStackHandler secondary, Supplier<Boolean> stillValid) {
			this.primary = primary;
			this.secondary = secondary;
			this.stillValid = stillValid;
		}

		private ItemStackHandler handlerForSlot(int slot) {
			return secondary == null || slot < primary.getSlotCount() ? primary : secondary;
		}

		private int actualSlot(int slot) {
			return handlerForSlot(slot) == primary ? slot : slot - primary.getSlotCount();
		}

		private boolean oob(int slot) {
			if (slot < 0)
				return true;
			ItemStackHandler handler = handlerForSlot(slot);
			slot = actualSlot(slot);
			return slot >= handler.getSlotCount();
		}

		@Override
		public int getContainerSize() {
			return primary.getSlotCount() + (secondary == null ? 0 : secondary.getSlotCount());
		}

		@Override
		public boolean isEmpty() {
			boolean primaryEmpty = Iterators.size(primary.nonEmptyIterator()) == 0;
			boolean secondaryEmpty = secondary == null || Iterators.size(secondary.nonEmptyIterator()) == 0;
			return primaryEmpty && secondaryEmpty;
		}

		@Override
		public ItemStack getItem(int slot) {
			if (oob(slot))
				return ItemStack.EMPTY;
			ItemStackHandler handler = handlerForSlot(slot);
			slot = actualSlot(slot);
			return handler.getStackInSlot(slot);
		}

		@Override
		public ItemStack removeItem(int slot, int count) {
			if (oob(slot))
				return ItemStack.EMPTY;
			ItemStackHandler handler = handlerForSlot(slot);
			slot = actualSlot(slot);

			ItemStack current = handler.getStackInSlot(slot);
			if (current.isEmpty())
				return ItemStack.EMPTY;
			current = current.copy();
			ItemStack extracted = current.split(count);
			handler.setStackInSlot(slot, current);
			return extracted;
		}

		@Override
		public ItemStack removeItemNoUpdate(int slot) {
			return removeItem(slot, Integer.MAX_VALUE);
		}

		@Override
		public void setItem(int slot, ItemStack stack) {
			if (!oob(slot)) {
				ItemStackHandler handler = handlerForSlot(slot);
				slot = actualSlot(slot);
				handler.setStackInSlot(slot, stack.copy());
			}
		}

		@Override
		public void setChanged() {
		}

		@Override
		public boolean stillValid(Player player) {
			return stillValid.get();
		}

		@Override
		public boolean canPlaceItem(int slot, ItemStack stack) {
			ItemStackHandler handler = handlerForSlot(slot);
			slot = actualSlot(slot);
			return handler.isItemValid(slot, ItemVariant.of(stack), 1);
		}

		@Override
		public void clearContent() {
			primary.setSize(primary.getSlotCount());
			if (secondary != null)
				secondary.setSize(secondary.getSlotCount());
		}
	}

}
