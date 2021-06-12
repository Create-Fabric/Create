package com.simibubi.create.lib.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.simibubi.create.lib.extensions.PacketBufferExtensions;
import com.simibubi.create.lib.lba.fluid.FluidStack;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;

@Mixin(PacketBuffer.class)
public abstract class PacketBufferMixin implements PacketBufferExtensions {
	@Shadow
	public abstract ByteBuf writeBoolean(boolean bl);

	@Shadow
	public abstract boolean readBoolean();

	@Override
	public void writeFluidStack(FluidStack stack) {
		//			stack.writeToPacket(MixinHelper.cast(this));
		writeBoolean(!stack.isEmpty());
	}

	@Override
	public FluidStack readFluidStack() {
		return /*readBoolean() ? */FluidStack.EMPTY /*: FluidStack.readFromPacket(MixinHelper.cast(this))*/;
	}
}
