package com.simibubi.create.foundation.blockEntity.behaviour;

import com.simibubi.create.foundation.utility.AdventureUtil;

import net.fabricmc.fabric.api.entity.FakePlayer;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTags.AllItemTags;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.SidedFilteringBehaviour;
import com.simibubi.create.foundation.utility.RaycastHelper;

import io.github.fabricators_of_create.porting_lib.util.EnvExecutor;
import net.fabricmc.api.EnvType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public class ValueSettingsInputHandler {

	public static InteractionResult onBlockActivated(Player player, Level world, InteractionHand hand, BlockHitResult hit) {
		BlockPos pos = hit.getBlockPos();

		if (!canInteract(player))
			return InteractionResult.PASS;
		if (AllBlocks.CLIPBOARD.isIn(player.getMainHandItem()))
			return InteractionResult.PASS;
		if (!(world.getBlockEntity(pos)instanceof SmartBlockEntity sbe))
			return InteractionResult.PASS;

		MutableBoolean cancelled = new MutableBoolean(false);
		if (world.isClientSide)
			EnvExecutor.runWhenOn(EnvType.CLIENT,
				() -> () -> CreateClient.VALUE_SETTINGS_HANDLER.cancelIfWarmupAlreadyStarted(pos, cancelled));

		if (cancelled.booleanValue())
			return InteractionResult.FAIL;

		for (BlockEntityBehaviour behaviour : sbe.getAllBehaviours()) {
			if (!(behaviour instanceof ValueSettingsBehaviour valueSettingsBehaviour))
				continue;

			BlockHitResult ray = RaycastHelper.rayTraceRange(world, player, 10);
			if (ray == null)
				return InteractionResult.PASS;
			if (behaviour instanceof SidedFilteringBehaviour) {
				behaviour = ((SidedFilteringBehaviour) behaviour).get(ray.getDirection());
				if (behaviour == null)
					continue;
			}

			if (!valueSettingsBehaviour.isActive())
				continue;
			if (valueSettingsBehaviour.onlyVisibleWithWrench()
				&& !AllItemTags.WRENCH.matches(player.getItemInHand(hand)))
				continue;
			if (valueSettingsBehaviour.getSlotPositioning()instanceof ValueBoxTransform.Sided sidedSlot) {
				if (!sidedSlot.isSideActive(sbe.getBlockState(), ray.getDirection()))
					continue;
				sidedSlot.fromSide(ray.getDirection());
			}

			boolean fakePlayer = player instanceof FakePlayer;
			if (!valueSettingsBehaviour.testHit(ray.getLocation()) && !fakePlayer)
				continue;

			if (!valueSettingsBehaviour.acceptsValueSettings() || fakePlayer) {
				valueSettingsBehaviour.onShortInteract(player, hand, ray.getDirection());
				return InteractionResult.SUCCESS;
			}

			if (world.isClientSide) {
				BehaviourType<?> type = behaviour.getType();
				EnvExecutor.runWhenOn(EnvType.CLIENT, () -> () -> CreateClient.VALUE_SETTINGS_HANDLER
					.startInteractionWith(pos, type, hand, ray.getDirection()));
			}

			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	public static boolean canInteract(Player player) {
		return player != null && !player.isSpectator() && !player.isShiftKeyDown() && !AdventureUtil.isAdventure(player);
	}

}
