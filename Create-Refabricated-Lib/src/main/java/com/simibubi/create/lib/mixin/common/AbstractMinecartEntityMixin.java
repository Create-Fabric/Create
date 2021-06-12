package com.simibubi.create.lib.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.lib.block.MinecartPassHandlerBlock;
import com.simibubi.create.lib.extensions.AbstractMinecartEntityExtensions;
import com.simibubi.create.lib.utility.MinecartController;
import com.simibubi.create.lib.utility.MixinHelper;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

@Mixin(AbstractMinecartEntity.class)
public abstract class AbstractMinecartEntityMixin extends Entity implements AbstractMinecartEntityExtensions {
	public boolean create$canUseRail = true;
	public MinecartController create$controller = null;

	private AbstractMinecartEntityMixin(EntityType<?> entityType, World world) {
		super(entityType, world);
	}

	@Shadow
	protected abstract double getMaximumSpeed();

	@Shadow
	public abstract AbstractMinecartEntity.Type getMinecartType();

	@Inject(at = @At("TAIL"), method = "<init>(Lnet/minecraft/entity/EntityType;Lnet/minecraft/world/World;)V")
	public void create$abstractMinecartEntity(EntityType<?> entityType, World world, CallbackInfo ci) {
		create$controller = MinecartController.InitController.initController.create(MixinHelper.cast(this));
	}

	@Inject(at = @At("TAIL"), method = "<init>(Lnet/minecraft/entity/EntityType;Lnet/minecraft/world/World;DDD)V")
	public void create$abstractMinecartEntity(EntityType<?> entityType, World world, double d, double e, double f, CallbackInfo ci) {
		create$controller = MinecartController.InitController.initController.create(MixinHelper.cast(this));
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;floor(D)I", ordinal = 4),
			method = "moveAlongTrack(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)V")
	protected void create$moveAlongTrack(BlockPos blockPos, BlockState blockState, CallbackInfo ci) {
		if (blockState.getBlock() instanceof MinecartPassHandlerBlock) {
			((MinecartPassHandlerBlock) blockState.getBlock()).onMinecartPass(blockState, MixinHelper.<Entity>cast(this).world, blockPos, MixinHelper.cast(this));
		}
	}

	@Override
	public void create$moveMinecartOnRail(BlockPos pos) {
		double d24 = isBeingRidden() ? 0.75D : 1.0D;
		double d25 = getMaximumSpeed(); // getMaximumSpeed instead of getMaxSpeedWithRail *should* be fine after intense pain looking at Forge patches
		Vector3d vec3d1 = getMotion();
		move(MoverType.SELF, new Vector3d(MathHelper.clamp(d24 * vec3d1.x, -d25, d25), 0.0D, MathHelper.clamp(d24 * vec3d1.z, -d25, d25)));
	}

	@Override
	public ItemStack create$getCartItem() {
		switch (getMinecartType()) {
			case FURNACE:
				return new ItemStack(Items.FURNACE_MINECART);
			case CHEST:
				return new ItemStack(Items.CHEST_MINECART);
			case TNT:
				return new ItemStack(Items.TNT_MINECART);
			case HOPPER:
				return new ItemStack(Items.HOPPER_MINECART);
			case COMMAND_BLOCK:
				return new ItemStack(Items.COMMAND_BLOCK_MINECART);
			default:
				return new ItemStack(Items.MINECART);
		}
	}

	@Override
	public boolean create$canUseRail() {
		return create$canUseRail;
	}

	@Override
	public BlockPos create$getCurrentRailPos() {
		BlockPos pos = new BlockPos(MathHelper.floor(getX()), MathHelper.floor(getY()), MathHelper.floor(getZ()));
		if (world.getBlockState(pos.down()).isIn(BlockTags.RAILS)) {
			pos = pos.down();
		}

		return pos;
	}

	@Override
	public MinecartController create$getController() {
		return create$controller;
	}
}
