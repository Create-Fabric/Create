package com.simibubi.create.content.contraptions.sync;

import java.util.function.Supplier;

import com.simibubi.create.foundation.mixin.fabric.ServerGamePacketListenerImplAccessor;
import com.simibubi.create.AllPackets;
import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class ClientMotionPacket extends SimplePacketBase {

	private Vec3 motion;
	private boolean onGround;
	private float limbSwing;

	public ClientMotionPacket(Vec3 motion, boolean onGround, float limbSwing) {
		this.motion = motion;
		this.onGround = onGround;
		this.limbSwing = limbSwing;
	}

	public ClientMotionPacket(FriendlyByteBuf buffer) {
		motion = new Vec3(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
		onGround = buffer.readBoolean();
		limbSwing = buffer.readFloat();
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeFloat((float) motion.x);
		buffer.writeFloat((float) motion.y);
		buffer.writeFloat((float) motion.z);
		buffer.writeBoolean(onGround);
		buffer.writeFloat(limbSwing);
	}

	@Override
	public boolean handle(Context context) {
		context.enqueueWork(() -> {
			ServerPlayer sender = context.getSender();
			if (sender == null)
				return;
			sender.setDeltaMovement(motion);
			sender.setOnGround(onGround);
			if (onGround) {
				sender.causeFallDamage(sender.fallDistance, 1, sender.damageSources().fall());
				sender.fallDistance = 0;
				ServerGamePacketListenerImplAccessor access = (ServerGamePacketListenerImplAccessor) sender.connection;
					access.create$setAboveGroundTickCount(0);
				access.create$setAboveGroundVehicleTickCount(0);
			}
			AllPackets.getChannel().sendToClientsTracking(new LimbSwingUpdatePacket(sender.getId(), sender.position(), limbSwing), sender);
		});
		return true;
	}

}
