package com.simibubi.create.lib.utility;

import java.util.function.Consumer;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;

public class NetworkUtil {

	public static void openGUI(ServerPlayerEntity player, INamedContainerProvider containerProvider, Consumer<PacketBuffer> extraDataWriter) {
		player.openContainer(new ExtendedScreenHandlerFactory() {
			@Override
			public ITextComponent getDisplayName() {
				return containerProvider.getDisplayName();
			}

			@Override
			public Container createMenu(int arg0, PlayerInventory arg1, PlayerEntity arg2) {
				return containerProvider.createMenu(arg0, arg1, arg2);
			}

			@Override
			public void writeScreenOpeningData(ServerPlayerEntity player, PacketBuffer buf) {
				extraDataWriter.accept(buf);
			}
		});
	}
}
