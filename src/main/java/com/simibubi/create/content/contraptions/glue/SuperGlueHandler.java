package com.simibubi.create.content.contraptions.glue;

import java.util.HashSet;
import java.util.Set;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllPackets;
import com.simibubi.create.content.contraptions.BlockMovementChecks;
import com.simibubi.create.foundation.placement.IPlacementHelper;
import com.simibubi.create.foundation.utility.AdventureUtil;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.fabric.ReachUtil;
import com.simibubi.create.foundation.utility.worldWrappers.RayTraceWorld;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;

public class SuperGlueHandler {

	public static void glueListensForBlockPlacement(BlockPlaceContext context, BlockPos pos, BlockState state) {
		LevelAccessor world = context.getLevel();
		Player entity = context.getPlayer();

		if (entity == null || AdventureUtil.isAdventure(entity))
			return;
		if (world.isClientSide())
			return;

		Set<SuperGlueEntity> cached = new HashSet<>();
		for (Direction direction : Iterate.directions) {
			BlockPos relative = pos.relative(direction);
			if (SuperGlueEntity.isGlued(world, pos, direction, cached)
				&& BlockMovementChecks.isMovementNecessary(world.getBlockState(relative), entity.level(), relative))
				AllPackets.getChannel().sendToClientsTrackingAndSelf(new GlueEffectPacket(pos, direction, true), entity);
		}

		glueInOffHandAppliesOnBlockPlace(context.getLevel().getBlockState(context.getClickedPos().relative(context.getClickedFace().getOpposite())), pos, entity);
	}

	public static void glueInOffHandAppliesOnBlockPlace(BlockState placedAgainst, BlockPos pos, Player placer) {
		ItemStack itemstack = placer.getOffhandItem();
		if (!AllItems.SUPER_GLUE.isIn(itemstack))
			return;
		if (AllItems.WRENCH.isIn(placer.getMainHandItem()))
			return;
		if (placedAgainst == IPlacementHelper.ID)
			return;

		double distance = ReachUtil.reach(placer);
		Vec3 start = placer.getEyePosition(1);
		Vec3 look = placer.getViewVector(1);
		Vec3 end = start.add(look.x * distance, look.y * distance, look.z * distance);
		Level world = placer.level();

		RayTraceWorld rayTraceWorld =
			new RayTraceWorld(world, (p, state) -> p.equals(pos) ? Blocks.AIR.defaultBlockState() : state);
		BlockHitResult ray =
			rayTraceWorld.clip(new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, placer));

		Direction face = ray.getDirection();
		if (face == null || ray.getType() == Type.MISS)
			return;

		BlockPos gluePos = ray.getBlockPos();
		if (!gluePos.relative(face)
			.equals(pos)) {
			return;
		}

		if (SuperGlueEntity.isGlued(world, gluePos, face, null))
			return;

		SuperGlueEntity entity = new SuperGlueEntity(world, SuperGlueEntity.span(gluePos, gluePos.relative(face)));
		CompoundTag compoundnbt = itemstack.getTag();
		if (compoundnbt != null)
			EntityType.updateCustomEntityTag(world, placer, entity, compoundnbt);

		if (SuperGlueEntity.isValidFace(world, gluePos, face)) {
			if (!world.isClientSide) {
				world.addFreshEntity(entity);
				AllPackets.getChannel().sendToClientsTracking(new GlueEffectPacket(gluePos, face, true), entity);
			}
			itemstack.hurtAndBreak(1, placer, SuperGlueItem::onBroken);
		}
	}

}
