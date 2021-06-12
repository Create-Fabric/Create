package com.simibubi.create.lib.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.lib.extensions.BlockStateExtensions;
import com.simibubi.create.lib.extensions.ParticleManagerExtensions;
import com.simibubi.create.lib.utility.MixinHelper;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.math.BlockPos;

@Environment(EnvType.CLIENT)
@Mixin(ParticleManager.class)
public abstract class ParticleManagerMixin implements ParticleManagerExtensions {
	@Shadow
	protected ClientWorld world;

	@Shadow
	protected abstract <T extends IParticleData> void registerFactory(ParticleType<T> particleType, ParticleManager.IParticleMetaFactory<T> spriteAwareFactory);

	@Shadow
	protected abstract <T extends IParticleData> void registerFactory(ParticleType<T> type, IParticleFactory<T> factory);

	@Override
	public <T extends IParticleData> void create$registerFactory0(ParticleType<T> particleType, ParticleManager.IParticleMetaFactory<T> spriteAwareFactory) {
		registerFactory(particleType, spriteAwareFactory);
	}

	@Override
	public <T extends IParticleData> void create$registerFactory1(ParticleType<T> type, IParticleFactory<T> factory) {
		registerFactory(type, factory);
	}

	@Inject(at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lnet/minecraft/block/BlockState;getShape(Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/math/shapes/VoxelShape;"),
			method = "addBlockDestroyEffects(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)V", cancellable = true)
	public void create$addBlockDestroyEffects(BlockPos blockPos, BlockState blockState, CallbackInfo ci) {
		if (((BlockStateExtensions) blockState).create$addDestroyEffects(world, blockPos, MixinHelper.cast(this))) {
			ci.cancel();
		}
	}
}
