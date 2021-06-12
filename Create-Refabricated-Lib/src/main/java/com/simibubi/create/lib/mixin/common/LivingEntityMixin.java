package com.simibubi.create.lib.mixin.common;

import java.util.ArrayList;
import java.util.Collection;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.simibubi.create.lib.event.LivingEntityEvents;
import com.simibubi.create.lib.extensions.BlockStateExtensions;
import com.simibubi.create.lib.extensions.EntityExtensions;
import com.simibubi.create.lib.item.EntitySwingListenerItem;
import com.simibubi.create.lib.utility.MixinHelper;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
	@Shadow
	protected PlayerEntity attackingPlayer;
	@Unique
	private DamageSource create$currentDamageSource;

	public LivingEntityMixin(EntityType<?> entityType, World world) {
		super(entityType, world);
	}

	@Shadow
	public abstract ItemStack getHeldItem(Hand hand);

	@Inject(method = "spawnDrops(Lnet/minecraft/util/DamageSource;)V", at = @At("HEAD"))
	private void create$spawnDropsHEAD(DamageSource source, CallbackInfo ci) {
		create$currentDamageSource = source;
		((EntityExtensions) this).create$captureDrops(new ArrayList<>());
	}

	@ModifyVariable(method = "spawnDrops(Lnet/minecraft/util/DamageSource;)V",
			at = @At(value = "STORE", ordinal = 0))
	private int create$spawnDropsBODY(int j) {
		int modifiedLevel = LivingEntityEvents.LOOTING_LEVEL.invoker().modifyLootingLevel(create$currentDamageSource);
		if (modifiedLevel != 0) {
			return modifiedLevel;
		} else {
			return EnchantmentHelper.getLootingModifier((LivingEntity) create$currentDamageSource.getTrueSource());
		}
	}

	@ModifyVariable(method = "spawnDrops(Lnet/minecraft/util/DamageSource;)V",
			at = @At(value = "STORE", ordinal = 1))
	private int create$spawnDropsBODY2(int j) {
		return LivingEntityEvents.LOOTING_LEVEL.invoker().modifyLootingLevel(create$currentDamageSource);
	}

	@Inject(method = "spawnDrops(Lnet/minecraft/util/DamageSource;)V", at = @At("TAIL"))
	private void create$spawnDropsTAIL(DamageSource source, CallbackInfo ci) {
		Collection<ItemEntity> drops = ((EntityExtensions) this).create$captureDrops(null);
		if (!LivingEntityEvents.DROPS.invoker().onLivingEntityDrops(source, drops))
			drops.forEach(e -> world.addEntity(e));
	}

	@Environment(EnvType.CLIENT)
	@Inject(method = "swingHand(Lnet/minecraft/util/Hand;Z)V", at = @At("HEAD"), cancellable = true)
	private void create$swingHand(Hand hand, boolean bl, CallbackInfo ci) {
		ItemStack stack = getHeldItem(hand);
		if (!stack.isEmpty() && stack.getItem() instanceof EntitySwingListenerItem && ((EntitySwingListenerItem) stack.getItem())
				.onEntitySwing(stack, (LivingEntity) (Object) this)) ci.cancel();
	}

	@Inject(method = "tick()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;tick()V"))
	private void create$tick(CallbackInfo ci) {
		LivingEntityEvents.TICK.invoker().onLivingEntityTick((LivingEntity) (Object) this);
	}

	@ModifyVariable(method = "takeKnockback(FDD)V", at = @At("STORE"), ordinal = 0)
	private float create$takeKnockback(float f) {
		if (attackingPlayer != null)
			return LivingEntityEvents.KNOCKBACK_STRENGTH.invoker().onLivingEntityTakeKnockback(f, attackingPlayer);

		return f;
	}

	@ModifyVariable(method = "dropXp()V", at = @At("STORE"), ordinal = 0)
	private int create$dropXp(int i) {
		return LivingEntityEvents.EXPERIENCE_DROP.invoker().onLivingEntityExperienceDrop(i, attackingPlayer);
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/server/ServerWorld;spawnParticle(Lnet/minecraft/particles/IParticleData;DDDIDDDD)I", shift = At.Shift.BEFORE),
			locals = LocalCapture.CAPTURE_FAILEXCEPTION,
			method = "updateFallState(DZLnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;)V", cancellable = true)
	protected void create$updateFallState(double d, boolean bl, BlockState blockState, BlockPos blockPos, CallbackInfo ci,
										  float f, double e, int i) {
		if (((BlockStateExtensions) blockState).create$addLandingEffects((ServerWorld) world, blockPos, blockState, MixinHelper.cast(this), i)) {
			super.updateFallState(d, bl, blockState, blockPos);
			ci.cancel();
		}
	}

	// TODO Make this less :concern: when fabric's mixin fork updates
	@ModifyVariable(at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/block/Block;getSlipperiness()F"),
			method = "travel(Lnet/minecraft/util/math/vector/Vector3d;)V",
			index = 7)
	public float create$setSlipperiness(float t) {
		return ((BlockStateExtensions) MixinHelper.<LivingEntity>cast(this).world.getBlockState(getVelocityAffectingPos()))
				.create$getSlipperiness(MixinHelper.<LivingEntity>cast(this).world, getVelocityAffectingPos(), MixinHelper.<LivingEntity>cast(this));
	}
}
