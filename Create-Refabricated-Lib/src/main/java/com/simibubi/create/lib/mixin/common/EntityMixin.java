package com.simibubi.create.lib.mixin.common;

import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.simibubi.create.lib.event.EntityEyeHeightCallback;
import com.simibubi.create.lib.event.StartRidingCallback;
import com.simibubi.create.lib.extensions.BlockStateExtensions;
import com.simibubi.create.lib.extensions.EntityExtensions;
import com.simibubi.create.lib.helper.EntityHelper;
import com.simibubi.create.lib.utility.ListenerProvider;
import com.simibubi.create.lib.utility.MixinHelper;
import com.simibubi.create.lib.utility.NBTSerializable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(Entity.class)
public abstract class EntityMixin implements EntityExtensions, NBTSerializable {
	@Unique
	private static final Logger CREATE$LOGGER = LogManager.getLogger();
	@Shadow
	public World world;
	@Shadow
	private BlockPos blockPos;
	@Shadow
	private float eyeHeight;
	@Unique
	private CompoundNBT create$extraCustomData;
	@Unique
	private Collection<ItemEntity> create$captureDrops = null;

	@Shadow
	public abstract void read(CompoundNBT compoundNBT);

	@Inject(at = @At("TAIL"), method = "<init>(Lnet/minecraft/entity/EntityType;Lnet/minecraft/world/World;)V")
	public void create$entityInit(EntityType<?> entityType, World world, CallbackInfo ci) {
		int newEyeHeight = EntityEyeHeightCallback.EVENT.invoker().onEntitySize((Entity) (Object) this);
		if (newEyeHeight != -1)
			eyeHeight = newEyeHeight;
	}

	// CAPTURE DROPS

	@Inject(locals = LocalCapture.CAPTURE_FAILHARD,
			at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/item/ItemEntity;setDefaultPickupDelay()V", shift = At.Shift.AFTER),
			method = "entityDropItem(Lnet/minecraft/item/ItemStack;F)Lnet/minecraft/entity/item/ItemEntity;", cancellable = true)
	public void create$entityDropItem(ItemStack stack, float f, CallbackInfoReturnable<ItemEntity> cir, ItemEntity itemEntity) {
		if (create$captureDrops != null) create$captureDrops.add(itemEntity);
		else cir.cancel();
	}

	@Unique
	@Override
	public Collection<ItemEntity> create$captureDrops() {
		return create$captureDrops;
	}

	@Unique
	@Override
	public Collection<ItemEntity> create$captureDrops(Collection<ItemEntity> value) {
		Collection<ItemEntity> ret = create$captureDrops;
		create$captureDrops = value;
		return ret;
	}

	// EXTRA CUSTOM DATA

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;writeAdditional(Lnet/minecraft/nbt/CompoundNBT;)V"),
			method = "writeWithoutTypeId(Lnet/minecraft/nbt/CompoundNBT;)Lnet/minecraft/nbt/CompoundNBT;")
	public void create$beforeWriteCustomData(CompoundNBT tag, CallbackInfoReturnable<CompoundNBT> cir) {
		if (create$extraCustomData != null && !create$extraCustomData.isEmpty()) {
			CREATE$LOGGER.debug("Create: writing custom data to entity [{}]", MixinHelper.<Entity>cast(this).toString());
			tag.put(EntityHelper.EXTRA_DATA_KEY, create$extraCustomData);
		}
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;readAdditional(Lnet/minecraft/nbt/CompoundNBT;)V"), method = "read(Lnet/minecraft/nbt/CompoundNBT;)V")
	public void create$beforeReadCustomData(CompoundNBT tag, CallbackInfo ci) {
		if (tag.contains(EntityHelper.EXTRA_DATA_KEY)) {
			create$extraCustomData = tag.getCompound(EntityHelper.EXTRA_DATA_KEY);
		}
	}

	// RUNNING EFFECTS

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;", shift = At.Shift.AFTER),
			locals = LocalCapture.CAPTURE_FAILEXCEPTION,
			method = "spawnSprintingParticles()V", cancellable = true)
	public void create$spawnSprintingParticles(CallbackInfo ci, int i, int j, int k, BlockPos blockPos) {
		if (((BlockStateExtensions) world.getBlockState(blockPos)).create$addRunningEffects(world, blockPos, MixinHelper.cast(this))) {
			ci.cancel();
		}
	}

	//

	@Inject(method = "remove()V", at = @At("HEAD"))
	public void create$remove(CallbackInfo ci) {
		if (this instanceof ListenerProvider) {
			((ListenerProvider) this).invalidate();
		}
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;canBeRidden(Lnet/minecraft/entity/Entity;)Z", shift = At.Shift.BEFORE),
			method = "startRiding(Lnet/minecraft/entity/Entity;Z)Z", cancellable = true)
	public void create$startRiding(Entity entity, boolean bl, CallbackInfoReturnable<Boolean> cir) {
		if (StartRidingCallback.EVENT.invoker().onStartRiding(MixinHelper.cast(this), entity) == ActionResultType.FAIL) {
			cir.setReturnValue(false);
		}
	}

	@Unique
	@Override
	public CompoundNBT create$getExtraCustomData() {
		if (create$extraCustomData == null) {
			create$extraCustomData = new CompoundNBT();
		}
		return create$extraCustomData;
	}

	@Unique
	@Override
	public CompoundNBT create$serializeNBT() {
		CompoundNBT nbt = new CompoundNBT();
		String id = EntityHelper.getEntityString(MixinHelper.cast(this));

		if (id != null) {
			nbt.putString("id", id);
		}

		return nbt;
	}

	@Unique
	@Override
	public void create$deserializeNBT(CompoundNBT nbt) {
		read(nbt);
	}
}
