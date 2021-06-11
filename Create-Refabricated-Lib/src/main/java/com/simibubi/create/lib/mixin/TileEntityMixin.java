package com.simibubi.create.lib.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.lib.extensions.TileEntityExtensions;
import com.simibubi.create.lib.helper.TileEntityHelper;
import com.simibubi.create.lib.utility.MixinHelper;
import com.simibubi.create.lib.utility.NBTSerializable;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;

@Mixin(TileEntity.class)
public abstract class TileEntityMixin implements TileEntityExtensions, NBTSerializable {
	@Unique
	private CompoundNBT create$extraCustomData;

	@Override
	public CompoundNBT create$getExtraCustomData() {
		if (create$extraCustomData == null) {
			create$extraCustomData = new CompoundNBT();
		}
		return create$extraCustomData;
	}

	@Inject(method = "fromTag(Lnet/minecraft/block/BlockState;Lnet/minecraft/nbt/CompoundNBT;)V",
			at = @At("TAIL"))
	public void fromTag(BlockState blockState, CompoundNBT compoundNBT, CallbackInfo ci) {
		if (compoundNBT.contains(TileEntityHelper.EXTRA_DATA_KEY))
			this.create$extraCustomData = compoundNBT.getCompound(TileEntityHelper.EXTRA_DATA_KEY);
	}

	@Inject(method = "writeInternal(Lnet/minecraft/nbt/CompoundNBT;)Lnet/minecraft/nbt/CompoundNBT;",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundNBT;putString(Ljava/lang/String;Ljava/lang/String;)V"))
	private void writeInternal(CompoundNBT compoundNBT, CallbackInfoReturnable<CompoundNBT> cir) {
		if (this.create$extraCustomData != null) {
			compoundNBT.put(TileEntityHelper.EXTRA_DATA_KEY, this.create$extraCustomData);
		}
	}

	@Override
	public CompoundNBT create$serializeNBT() {
		CompoundNBT nbt = new CompoundNBT();
		MixinHelper.<TileEntity>cast(this).write(nbt);
		return nbt;
	}

	@Override
	public void create$deserializeNBT(CompoundNBT nbt) {
		create$deserializeNBT(null, nbt);
	}

	public void create$deserializeNBT(BlockState state, CompoundNBT nbt) {
		MixinHelper.<TileEntity>cast(this).fromTag(state, nbt);
	}
}
