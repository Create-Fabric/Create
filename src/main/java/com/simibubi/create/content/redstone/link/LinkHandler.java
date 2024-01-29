package com.simibubi.create.content.redstone.link;

import java.util.Arrays;

import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.AdventureUtil;
import com.simibubi.create.foundation.utility.RaycastHelper;

import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class LinkHandler {

	public static InteractionResult onBlockActivated(Player player, Level world, InteractionHand hand, BlockHitResult blockRayTraceResult) {
		BlockPos pos = blockRayTraceResult.getBlockPos();
		if (player.isShiftKeyDown() || player.isSpectator())
			return InteractionResult.PASS;

		LinkBehaviour behaviour = BlockEntityBehaviour.get(world, pos, LinkBehaviour.TYPE);
		if (behaviour == null)
			return InteractionResult.PASS;
		if (AdventureUtil.isAdventure(player))
			return InteractionResult.PASS;

		ItemStack heldItem = player.getItemInHand(hand);
		BlockHitResult ray = RaycastHelper.rayTraceRange(world, player, 10);
		if (ray == null)
			return InteractionResult.PASS;
		if (AllItems.LINKED_CONTROLLER.isIn(heldItem))
			return InteractionResult.PASS;
		if (AllItems.WRENCH.isIn(heldItem))
			return InteractionResult.PASS;

		boolean fakePlayer = player instanceof FakePlayer;
		boolean fakePlayerChoice = false;

		if (fakePlayer) {
			BlockState blockState = world.getBlockState(pos);
			Vec3 localHit = ray.getLocation()
				.subtract(Vec3.atLowerCornerOf(pos))
				.add(Vec3.atLowerCornerOf(ray.getDirection()
					.getNormal())
					.scale(.25f));
			fakePlayerChoice = localHit.distanceToSqr(behaviour.firstSlot.getLocalOffset(blockState)) > localHit
				.distanceToSqr(behaviour.secondSlot.getLocalOffset(blockState));
		}

		for (boolean first : Arrays.asList(false, true)) {
			if (behaviour.testHit(first, ray.getLocation()) || fakePlayer && fakePlayerChoice == first) {
				if (!world.isClientSide)
					behaviour.setFrequency(first, heldItem);
				world.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, .25f, .1f);
				return InteractionResult.SUCCESS;
			}
		}
		return InteractionResult.PASS;
	}

}
