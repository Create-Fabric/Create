package com.simibubi.create.content.contraptions;

import java.lang.ref.WeakReference;
import java.util.Collection;

import javax.annotation.Nullable;

import com.github.exopandora.shouldersurfing.api.client.ShoulderSurfing;
import com.github.exopandora.shouldersurfing.api.model.PickContext;
import com.simibubi.create.compat.Mods;

import net.minecraft.world.phys.HitResult;

import org.apache.commons.lang3.mutable.MutableObject;

import com.jamieswhiteshirt.reachentityattributes.ReachEntityAttributes;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllPackets;
import com.simibubi.create.content.contraptions.sync.ContraptionInteractionPacket;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.content.trains.entity.TrainRelocator;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.RaycastHelper;
import com.simibubi.create.foundation.utility.RaycastHelper.PredicateTraceResult;
import com.simibubi.create.foundation.utility.VecHelper;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ContraptionHandlerClient {

	@Environment(EnvType.CLIENT)
	public static void preventRemotePlayersWalkingAnimations(Player player) {
//		if (event.phase == Phase.START)
//			return;
		if (!(player instanceof RemotePlayer))
			return;
		RemotePlayer remotePlayer = (RemotePlayer) player;
		CompoundTag data = remotePlayer.getCustomData();
		if (!data.contains("LastOverrideLimbSwingUpdate"))
			return;

		int lastOverride = data.getInt("LastOverrideLimbSwingUpdate");
		data.putInt("LastOverrideLimbSwingUpdate", lastOverride + 1);
		if (lastOverride > 5) {
			data.remove("LastOverrideLimbSwingUpdate");
			data.remove("OverrideLimbSwing");
			return;
		}

		float limbSwing = data.getFloat("OverrideLimbSwing");
		remotePlayer.xo = remotePlayer.getX() - (limbSwing / 4);
		remotePlayer.zo = remotePlayer.getZ();
	}

	@Environment(EnvType.CLIENT)
	public static InteractionResult rightClickingOnContraptionsGetsHandledLocally(Minecraft mc, HitResult result, InteractionHand hand) {
		if (Minecraft.getInstance().screen != null) // this is the only input event that doesn't check this?
			return InteractionResult.PASS;

		LocalPlayer player = mc.player;

		if (player == null)
			return InteractionResult.PASS;
		if (player.isSpectator())
			return InteractionResult.PASS;
		if (mc.level == null)
			return InteractionResult.PASS;
		if (mc.gameMode == null)
			return InteractionResult.PASS;
//		if (!event.isUseItem())
//			return InteractionResult.PASS;

		Couple<Vec3> rayInputs = getRayInputs(player);
		Vec3 origin = rayInputs.getFirst();
		Vec3 target = rayInputs.getSecond();
		AABB aabb = new AABB(origin, target).inflate(16);

		Collection<WeakReference<AbstractContraptionEntity>> contraptions =
			ContraptionHandler.loadedContraptions.get(mc.level)
				.values();

		for (WeakReference<AbstractContraptionEntity> ref : contraptions) {
			AbstractContraptionEntity contraptionEntity = ref.get();
			if (contraptionEntity == null)
				continue;
			if (!contraptionEntity.getBoundingBox()
				.intersects(aabb))
				continue;

			BlockHitResult rayTraceResult = rayTraceContraption(origin, target, contraptionEntity);
			if (rayTraceResult == null)
				continue;

			Direction face = rayTraceResult.getDirection();
			BlockPos pos = rayTraceResult.getBlockPos();

			if (contraptionEntity.handlePlayerInteraction(player, pos, face, hand)) {
				AllPackets.getChannel().sendToServer(new ContraptionInteractionPacket(contraptionEntity, hand, pos, face));
			} else if (handleSpecialInteractions(contraptionEntity, player, pos, face, hand)) {
			} else
				continue;

			return InteractionResult.FAIL;
		}
		return InteractionResult.PASS;
	}

	private static boolean handleSpecialInteractions(AbstractContraptionEntity contraptionEntity, Player player,
		BlockPos localPos, Direction side, InteractionHand interactionHand) {
		if (AllItems.WRENCH.isIn(player.getItemInHand(interactionHand))
			&& contraptionEntity instanceof CarriageContraptionEntity car)
			return TrainRelocator.carriageWrenched(car.toGlobalVector(VecHelper.getCenterOf(localPos), 1), car);
		return false;
	}

	@Environment(EnvType.CLIENT)
	public static Couple<Vec3> getRayInputs(LocalPlayer player) {
		Minecraft mc = Minecraft.getInstance();
		double reach = ReachEntityAttributes.getReachDistance(player, mc.gameMode.getPickRange());
		if (Mods.SHOULDERSURFING.isLoaded() && ShoulderSurfing.getInstance().isShoulderSurfing()) {
			var blockTrace = new PickContext.Builder(mc.gameRenderer.getMainCamera())
				.build()
				.blockTrace(reach, mc.getDeltaFrameTime());
			return Couple.create(blockTrace.left(), blockTrace.right());
		}
		Vec3 origin = RaycastHelper.getTraceOrigin(player);
		if (mc.hitResult != null && mc.hitResult.getLocation() != null)
			reach = Math.min(mc.hitResult.getLocation()
				.distanceTo(origin), reach);
		Vec3 target = RaycastHelper.getTraceTarget(player, reach, origin);
		return Couple.create(origin, target);
	}

	@Nullable
	public static BlockHitResult rayTraceContraption(Vec3 origin, Vec3 target,
		AbstractContraptionEntity contraptionEntity) {
		Vec3 localOrigin = contraptionEntity.toLocalVector(origin, 1);
		Vec3 localTarget = contraptionEntity.toLocalVector(target, 1);
		Contraption contraption = contraptionEntity.getContraption();

		MutableObject<BlockHitResult> mutableResult = new MutableObject<>();
		PredicateTraceResult predicateResult = RaycastHelper.rayTraceUntil(localOrigin, localTarget, p -> {
			for (Direction d : Iterate.directions) {
				if (d == Direction.UP)
					continue;
				BlockPos pos = d == Direction.DOWN ? p : p.relative(d);
				StructureBlockInfo blockInfo = contraption.getBlocks()
					.get(pos);
				if (blockInfo == null)
					continue;
				BlockState state = blockInfo.state();
				VoxelShape raytraceShape = state.getShape(contraption.getContraptionWorld(), BlockPos.ZERO.below());
				if (raytraceShape.isEmpty())
					continue;
				if (contraption.isHiddenInPortal(pos))
					continue;
				BlockHitResult rayTrace = raytraceShape.clip(localOrigin, localTarget, pos);
				if (rayTrace != null) {
					mutableResult.setValue(rayTrace);
					return true;
				}
			}
			return false;
		});

		if (predicateResult == null || predicateResult.missed())
			return null;

		BlockHitResult rayTraceResult = mutableResult.getValue();
		return rayTraceResult;
	}

}
