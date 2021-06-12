package com.simibubi.create.lib.mixin.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.simibubi.create.lib.block.CustomDataPacketHandlingTileEntity;
import com.simibubi.create.lib.entity.ClientSpawnHandlerEntity;
import com.simibubi.create.lib.entity.ExtraSpawnDataEntity;
import com.simibubi.create.lib.extensions.SSpawnObjectPacketExtensions;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SSpawnObjectPacket;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

@Environment(EnvType.CLIENT)
@Mixin(ClientPlayNetHandler.class)
public abstract class ClientPlayNetHandlerMixin {
	@Unique
	private static final Logger CREATE$LOGGER = LogManager.getLogger();
	@Final
	@Shadow
	private NetworkManager netManager;
	@Shadow
	private ClientWorld world;
	@Unique
	private boolean create$tileEntityHandled;

	@ModifyVariable(at = @At(value = "JUMP", opcode = Opcodes.IFNULL, ordinal = 3, shift = Shift.BEFORE),
			method = "handleSpawnObject(Lnet/minecraft/network/play/server/SSpawnObjectPacket;)V")
	public Entity create$replaceNullEntity(Entity entity, SSpawnObjectPacket packet) {
		if (entity == null) {
			EntityType<?> type = packet.getType();
			if (type != null) {
				entity = type.create(world);
				if (entity instanceof ClientSpawnHandlerEntity) {
					((ClientSpawnHandlerEntity) entity).onClientSpawn(packet);
				}
			}
		}
		return entity;
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;addEntity(ILnet/minecraft/entity/Entity;)V", shift = Shift.AFTER),
			method = "handleSpawnObject(Lnet/minecraft/network/play/server/SSpawnObjectPacket;)V",
			locals = LocalCapture.CAPTURE_FAILHARD)
	public void create$afterAddEntity(SSpawnObjectPacket packet, CallbackInfo ci, double x, double y, double z, Entity entity) {
		if (entity instanceof ExtraSpawnDataEntity) {
			PacketBuffer extraData = ((SSpawnObjectPacketExtensions) packet).create$getExtraDataBuf();
			if (extraData != null) {
				((ExtraSpawnDataEntity) entity).readSpawnData(extraData);
			}
		}
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/tileentity/TileEntity;fromTag(Lnet/minecraft/block/BlockState;Lnet/minecraft/nbt/CompoundNBT;)V"),
		method = "handleUpdateTileEntity(Lnet/minecraft/network/play/server/SUpdateTileEntityPacket;)V"
	)
	private void create$teIsHandled1(SUpdateTileEntityPacket sUpdateTileEntityPacket, CallbackInfo ci) {
		create$tileEntityHandled = true;
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/CommandBlockScreen;updateGui()V"),
		method = "handleUpdateTileEntity(Lnet/minecraft/network/play/server/SUpdateTileEntityPacket;)V"
	)
	private void create$teIsHandled2(SUpdateTileEntityPacket sUpdateTileEntityPacket, CallbackInfo ci) {
		create$tileEntityHandled = true;
	}

	@Inject(at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILEXCEPTION,
			method = "handleUpdateTileEntity(Lnet/minecraft/network/play/server/SUpdateTileEntityPacket;)V",
			cancellable = true)
	public void create$handleCustomTileEntity(SUpdateTileEntityPacket sUpdateTileEntityPacket, CallbackInfo ci, BlockPos pos, TileEntity tileEntity) {
		if (!create$tileEntityHandled) {
			if (tileEntity == null) {
				CREATE$LOGGER.error("Received invalid update packet for null TileEntity");
				ci.cancel();
			} else if (tileEntity instanceof CustomDataPacketHandlingTileEntity) {
				((CustomDataPacketHandlingTileEntity) tileEntity).onDataPacket(netManager, sUpdateTileEntityPacket);
			}
		} else {
			create$tileEntityHandled = false;
		}
	}
}
