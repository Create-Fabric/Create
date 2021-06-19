package com.simibubi.create.content.contraptions.components.deployer;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.simibubi.create.content.contraptions.components.structureMovement.mounted.CartAssemblerBlockItem;

import net.minecraft.block.DoublePlantBlock;

import net.minecraft.entity.merchant.villager.AbstractVillagerEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.state.properties.DoubleBlockHalf;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Multimap;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.contraptions.components.deployer.DeployerTileEntity.Mode;
import com.simibubi.create.content.contraptions.components.structureMovement.mounted.CartAssemblerBlockItem;
import com.simibubi.create.content.curiosities.tools.SandPaperItem;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.foundation.utility.BlockHelper;
import com.simibubi.create.foundation.utility.worldWrappers.WrappedWorld;
import com.simibubi.create.lib.extensions.EntityExtensions;
import com.simibubi.create.lib.mixin.accessor.BucketItemAccessor;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.BeehiveBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoublePlantBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceContext.BlockMode;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class DeployerHandler {

	private static final class ItemUseWorld extends WrappedWorld {
		private final Direction face;
		private final BlockPos pos;
		boolean rayMode = false;

		private ItemUseWorld(World world, Direction face, BlockPos pos) {
			super(world, world.getChunkProvider());
			this.face = face;
			this.pos = pos;
		}

		@Override
		public BlockRayTraceResult rayTraceBlocks(RayTraceContext context) {
			rayMode = true;
			BlockRayTraceResult rayTraceBlocks = super.rayTraceBlocks(context);
			rayMode = false;
			return rayTraceBlocks;
		}

		@Override
		public BlockState getBlockState(BlockPos position) {
			if (rayMode && (pos.offset(face.getOpposite(), 3)
				.equals(position)
				|| pos.offset(face.getOpposite(), 1)
					.equals(position)))
				return Blocks.BEDROCK.getDefaultState();
			return world.getBlockState(position);
		}
	}

	static boolean shouldActivate(ItemStack held, World world, BlockPos targetPos, @Nullable Direction facing) {
		if (held.getItem() instanceof BlockItem)
			if (world.getBlockState(targetPos)
				.getBlock() == ((BlockItem) held.getItem()).getBlock())
				return false;

		if (held.getItem() instanceof BucketItem) {
			BucketItem bucketItem = (BucketItem) held.getItem();
			Fluid fluid = ((BucketItemAccessor) bucketItem).getContainedBlock();
			if (fluid != Fluids.EMPTY && world.getFluidState(targetPos)
				.getFluid() == fluid)
				return false;
		}

		if (!held.isEmpty() && facing == Direction.DOWN
			&& TileEntityBehaviour.get(world, targetPos, TransportedItemStackHandlerBehaviour.TYPE) != null)
			return false;

		return true;
	}

	static void activate(DeployerFakePlayer player, Vector3d vec, BlockPos clickedPos, Vector3d extensionVector,
		Mode mode) {
		Multimap<Attribute, AttributeModifier> attributeModifiers = player.getHeldItemMainhand()
			.getAttributeModifiers(EquipmentSlotType.MAINHAND);
		player.getAttributes()
			.addTemporaryModifiers(attributeModifiers);
		activateInner(player, vec, clickedPos, extensionVector, mode);
		player.getAttributes()
			.addTemporaryModifiers(attributeModifiers);
	}

	private static void activateInner(DeployerFakePlayer player, Vector3d vec, BlockPos clickedPos,
		Vector3d extensionVector, Mode mode) {

		Vector3d rayOrigin = vec.add(extensionVector.scale(3 / 2f + 1 / 64f));
		Vector3d rayTarget = vec.add(extensionVector.scale(5 / 2f - 1 / 64f));
		player.setPosition(rayOrigin.x, rayOrigin.y, rayOrigin.z);
		BlockPos pos = new BlockPos(vec);
		ItemStack stack = player.getHeldItemMainhand();
		Item item = stack.getItem();

		// Check for entities
		final ServerWorld world = player.getServerWorld();
		List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(clickedPos));
		Hand hand = Hand.MAIN_HAND;
		if (!entities.isEmpty()) {
			Entity entity = entities.get(world.rand.nextInt(entities.size()));
			List<ItemEntity> capturedDrops = new ArrayList<>();
			boolean success = false;
			((EntityExtensions) entity).create$captureDrops(capturedDrops);

			// Use on entity
			if (mode == Mode.USE) {
				ActionResultType cancelResult = UseEntityCallback.EVENT.invoker().interact(player, world, hand, entity, new EntityRayTraceResult(entity));

				if (cancelResult == ActionResultType.FAIL || cancelResult == ActionResultType.SUCCESS) {
					((EntityExtensions) entity).create$captureDrops(null);
					return;
				}
				if (cancelResult == ActionResultType.PASS) {
					if (entity.processInitialInteract(player, hand)
						.isAccepted()){
						if (entity instanceof AbstractVillagerEntity) {
							AbstractVillagerEntity villager = ((AbstractVillagerEntity) entity);
							if (villager.getCustomer() instanceof DeployerFakePlayer)
								villager.setCustomer(null);
						}
						success = true;
					}
					else if (entity instanceof LivingEntity && stack.useOnEntity(player, (LivingEntity) entity, hand)
						.isAccepted())
						success = true;
				}
				if (!success && stack.isFood() && entity instanceof PlayerEntity) {
					PlayerEntity playerEntity = (PlayerEntity) entity;
					if (playerEntity.canEat(item.getFood()
						.canEatWhenFull())) {
						playerEntity.onFoodEaten(world, stack);
						player.spawnedItemEffects = stack.copy();
						success = true;
					}
				}
			}

			// Punch entity
			if (mode == Mode.PUNCH) {
				player.resetCooldown();
				player.attackTargetEntityWithCurrentItem(entity);
				success = true;
			}

			((EntityExtensions) entity).create$captureDrops(null);
			capturedDrops.forEach(e -> player.inventory.placeItemBackInInventory(world, e.getItem()));
			if (success)
				return;
		}

		// Shoot ray
		RayTraceContext rayTraceContext =
			new RayTraceContext(rayOrigin, rayTarget, BlockMode.OUTLINE, FluidMode.NONE, player);
		BlockRayTraceResult result = world.rayTraceBlocks(rayTraceContext);
		if (result.getPos() != clickedPos)
			result = new BlockRayTraceResult(result.getHitVec(), result.getFace(), clickedPos, result.isInside());
		BlockState clickedState = world.getBlockState(clickedPos);
		Direction face = result.getFace();
		if (face == null)
			face = Direction.getFacingFromVector(extensionVector.x, extensionVector.y, extensionVector.z)
				.getOpposite();

		// Left click
		if (mode == Mode.PUNCH) {
			if (!world.isBlockModifiable(player, clickedPos))
				return;
			if (clickedState.getShape(world, clickedPos)
				.isEmpty()) {
				player.blockBreakingProgress = null;
				return;
			}
			ActionResultType actionResult = UseBlockCallback.EVENT.invoker().interact(player, player.world, player.getActiveHand(), result);
			if (actionResult != ActionResultType.PASS)
				return;
			if (BlockHelper.extinguishFire(world, player, clickedPos, face)) // FIXME: is there an equivalent in world, as there was in 1.15?
				return;
//			if (event.getUseBlock() != DENY)
				clickedState.onBlockClicked(world, clickedPos, player);
			if (stack.isEmpty())
				return;

			float progress = clickedState.getPlayerRelativeBlockHardness(player, world, clickedPos) * 16;
			float before = 0;
			Pair<BlockPos, Float> blockBreakingProgress = player.blockBreakingProgress;
			if (blockBreakingProgress != null)
				before = blockBreakingProgress.getValue();
			progress += before;
			world.playSound(null, clickedPos, clickedState.getSoundType()
				.getHitSound(), SoundCategory.NEUTRAL, .25f, 1);

			if (progress >= 1) {
				tryHarvestBlock(player.interactionManager, clickedPos);
				world.sendBlockBreakProgress(player.getEntityId(), clickedPos, -1);
				player.blockBreakingProgress = null;
				return;
			}
			if (progress <= 0) {
				player.blockBreakingProgress = null;
				return;
			}

			if ((int) (before * 10) != (int) (progress * 10))
				world.sendBlockBreakProgress(player.getEntityId(), clickedPos, (int) (progress * 10));
			player.blockBreakingProgress = Pair.of(clickedPos, progress);
			return;
		}

		// Right click
		ItemUseContext itemusecontext = new ItemUseContext(player, hand, result);
		ActionResultType useBlock = null;
		ActionResultType useItem = null;
		if (!clickedState.getShape(world, clickedPos)
			.isEmpty()) {
			useBlock = UseBlockCallback.EVENT.invoker().interact(player, player.world, hand, result);
			useItem = useBlock;
		}

		// Item has custom active use
//		if (useItem != null) {
//			ActionResultType actionresult = stack.onItemUseFirst(itemusecontext);
//			if (actionresult != ActionResultType.PASS)
//				return;
//		}

		boolean holdingSomething = !player.getHeldItemMainhand()
			.isEmpty();
		boolean flag1 =
			!(player.isSneaking() && holdingSomething) /*|| (stack.doesSneakBypassUse(world, clickedPos, player))*/;

		// Use on block
		if (useBlock != null && flag1
			&& safeOnUse(clickedState, world, clickedPos, player, hand, result).isAccepted())
			return;
		if (stack.isEmpty())
			return;
		if (useItem == null)
			return;
		if (item instanceof BlockItem
			&& !(item instanceof CartAssemblerBlockItem)
			&& !clickedState.isReplaceable(new BlockItemUseContext(itemusecontext)))
			return;

		// Reposition fire placement for convenience
		if (item == Items.FLINT_AND_STEEL) {
			Direction newFace = result.getFace();
			BlockPos newPos = result.getPos();
			if (!AbstractFireBlock.method_30032(world, clickedPos, newFace))
				newFace = Direction.UP;
			if (clickedState.getMaterial() == Material.AIR)
				newPos = newPos.offset(face.getOpposite());
			result = new BlockRayTraceResult(result.getHitVec(), newFace, newPos, result.isInside());
			itemusecontext = new ItemUseContext(player, hand, result);
		}

		// 'Inert' item use behaviour & block placement
		ActionResultType onItemUse = stack.onItemUse(itemusecontext);
		if (onItemUse.isAccepted())
			return;
		if (item == Items.ENDER_PEARL)
			return;

		// buckets create their own ray, We use a fake wall to contain the active area
		World itemUseWorld = world;
		if (item instanceof BucketItem || item instanceof SandPaperItem)
			itemUseWorld = new ItemUseWorld(world, face, pos);

		ActionResult<ItemStack> onItemRightClick = item.onItemRightClick(itemUseWorld, player, hand);
		ItemStack resultStack = onItemRightClick.getResult();
		if (resultStack != stack || resultStack.getCount() != stack.getCount() || resultStack.getUseDuration() > 0
			|| resultStack.getDamage() != stack.getDamage()) {
			player.setHeldItem(hand, onItemRightClick.getResult());
		}

		CompoundNBT tag = stack.getTag();
		if (tag != null && stack.getItem() instanceof SandPaperItem && tag.contains("Polishing")) {
			player.spawnedItemEffects = ItemStack.read(tag.getCompound("Polishing"));
			AllSoundEvents.AUTO_POLISH.playOnServer(world, pos, .25f, 1f);
		}

		if (!player.getActiveItemStack()
			.isEmpty())
			player.setHeldItem(hand, stack.onItemUseFinish(world, player));

		player.resetActiveHand();
	}

	public static boolean tryHarvestBlock(PlayerInteractionManager interactionManager, BlockPos pos) {
		// <> PlayerInteractionManager#tryHarvestBlock

		ServerWorld world = interactionManager.world;
		ServerPlayerEntity player = interactionManager.player;
		BlockState blockstate = world.getBlockState(pos);
		GameType gameType = interactionManager.getGameType();

		if (!PlayerBlockBreakEvents.BEFORE.invoker().beforeBlockBreak(world, player, pos, world.getBlockState(pos), world.getTileEntity(pos)))
			return false;


		TileEntity tileentity = world.getTileEntity(pos);
//		if (player.getHeldItemMainhand()
//			.onBlockStartBreak(pos, player))
//			return false;
		if (player.isBlockBreakingRestricted(world, pos, gameType))
			return false;

		ItemStack prevHeldItem = player.getHeldItemMainhand();
		ItemStack heldItem = prevHeldItem.copy();

		boolean canHarvest = player.isUsingEffectiveTool(blockstate) && player.isAllowEdit();
		prevHeldItem.onBlockDestroyed(world, blockstate, pos, player);
//		if (prevHeldItem.isEmpty() && !heldItem.isEmpty())
//			net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(player, heldItem, Hand.MAIN_HAND);


		BlockPos posUp = pos.up();
		BlockState stateUp = world.getBlockState(posUp);
		if (blockstate.getBlock() instanceof DoublePlantBlock
			&& blockstate.get(DoublePlantBlock.HALF) == DoubleBlockHalf.LOWER
			&& stateUp.getBlock() == blockstate.getBlock()
			&& stateUp.get(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER
		) {
			// hack to prevent DoublePlantBlock from dropping a duplicate item
			world.setBlockState(pos, Blocks.AIR.getDefaultState(), 35);
			world.setBlockState(posUp, Blocks.AIR.getDefaultState(), 35);
		} else {
			/*if (!*/blockstate.getBlock().onBlockHarvested(player.world, pos, blockstate, player/*, world.getFluidState(pos)))
				return true*/);
			world.setBlockState(pos, blockstate);
		}

		blockstate.getBlock()
			.onPlayerDestroy(world, pos, blockstate);
		if (!canHarvest)
			return true;

		Block.getDrops(blockstate, world, pos, tileentity, player, prevHeldItem)
			.forEach(item -> player.inventory.placeItemBackInInventory(world, item));
		blockstate.spawnAdditionalDrops(world, pos, prevHeldItem);
		return true;
	}

	public static ActionResultType safeOnUse(BlockState state, World world, BlockPos pos, PlayerEntity player,
		Hand hand, BlockRayTraceResult ray) {
		if (state.getBlock() instanceof BeehiveBlock)
			return safeOnBeehiveUse(state, world, pos, player, hand);
		return state.onUse(world, player, hand, ray);
	}

	protected static ActionResultType safeOnBeehiveUse(BlockState state, World world, BlockPos pos, PlayerEntity player,
		Hand hand) {
		// <> BeehiveBlock#onUse

		BeehiveBlock block = (BeehiveBlock) state.getBlock();
		ItemStack prevHeldItem = player.getHeldItem(hand);
		int honeyLevel = state.get(BeehiveBlock.HONEY_LEVEL);
		boolean success = false;
		if (honeyLevel < 5)
			return ActionResultType.PASS;

		if (prevHeldItem.getItem() == Items.SHEARS) {
			world.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_BEEHIVE_SHEAR,
				SoundCategory.NEUTRAL, 1.0F, 1.0F);
			// <> BeehiveBlock#dropHoneycomb
			player.inventory.placeItemBackInInventory(world, new ItemStack(Items.HONEYCOMB, 3));
			prevHeldItem.damageItem(1, player, s -> s.sendBreakAnimation(hand));
			success = true;
		}

		if (prevHeldItem.getItem() == Items.GLASS_BOTTLE) {
			prevHeldItem.shrink(1);
			world.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_BOTTLE_FILL,
				SoundCategory.NEUTRAL, 1.0F, 1.0F);
			ItemStack honeyBottle = new ItemStack(Items.HONEY_BOTTLE);
			if (prevHeldItem.isEmpty())
				player.setHeldItem(hand, honeyBottle);
			else
				player.inventory.placeItemBackInInventory(world, honeyBottle);
			success = true;
		}

		if (!success)
			return ActionResultType.PASS;

		block.takeHoney(world, state, pos);
		return ActionResultType.SUCCESS;
	}

}
