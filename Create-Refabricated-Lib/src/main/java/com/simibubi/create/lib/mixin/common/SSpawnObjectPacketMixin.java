package com.simibubi.create.lib.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.lib.entity.ExtraSpawnDataEntity;
import com.simibubi.create.lib.extensions.SSpawnObjectPacketExtensions;

import io.netty.buffer.Unpooled;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SSpawnObjectPacket;
import net.minecraft.util.math.BlockPos;

@Mixin(SSpawnObjectPacket.class)
public abstract class SSpawnObjectPacketMixin implements SSpawnObjectPacketExtensions {
	@Unique
	private PacketBuffer create$extraDataBuf;

	@Inject(at = @At("TAIL"), method = "<init>(Lnet/minecraft/entity/Entity;I)V")
	public void create$onEntityCtor(Entity entity, int entityData, CallbackInfo ci) {
		create$setExtraData(entity);
	}

	@Inject(at = @At("TAIL"), method = "<init>(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/EntityType;ILnet/minecraft/util/math/BlockPos;)V")
	public void create$onEntityCtor(Entity entity, EntityType<?> entityType, int data, BlockPos pos, CallbackInfo ci) {
		create$setExtraData(entity);
	}

	@Unique
	private void create$setExtraData(Entity entity) {
		if (entity instanceof ExtraSpawnDataEntity) {
			create$extraDataBuf = new PacketBuffer(Unpooled.buffer());
			((ExtraSpawnDataEntity) entity).writeSpawnData(create$extraDataBuf);
		}
	}

	@Inject(at = @At("TAIL"), method = "writePacketData(Lnet/minecraft/network/PacketBuffer;)V")
	public void create$onTailWrite(PacketBuffer buf, CallbackInfo ci) {
		if (create$extraDataBuf != null) {
			buf.writeBytes(create$extraDataBuf);
		}
	}

	@Inject(at = @At("TAIL"), method = "readPacketData(Lnet/minecraft/network/PacketBuffer;)V")
	public void create$onTailRead(PacketBuffer buf, CallbackInfo ci) {
		int readable = buf.readableBytes();
		if (readable != 0) {
			this.create$extraDataBuf = new PacketBuffer(buf.readBytes(readable));
		}
	}

	@Inject(at = @At("TAIL"), method = "processPacket(Lnet/minecraft/client/network/play/IClientPlayNetHandler;)V")
	public void create$onTailApply(IClientPlayNetHandler listener, CallbackInfo ci) {
		if (create$extraDataBuf != null) {
			create$extraDataBuf.release();
		}
	}

	@Unique
	@Override
	public PacketBuffer create$getExtraDataBuf() {
		return create$extraDataBuf;
	}
}
