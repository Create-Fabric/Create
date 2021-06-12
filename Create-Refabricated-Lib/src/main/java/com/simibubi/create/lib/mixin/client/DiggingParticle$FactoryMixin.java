package com.simibubi.create.lib.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import com.simibubi.create.lib.extensions.BlockParticleDataExtensions;
import com.simibubi.create.lib.extensions.DiggingParticle$FactoryExtensions;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Blocks;
import net.minecraft.client.particle.DiggingParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BlockParticleData;

@Environment(EnvType.CLIENT)
@Mixin(DiggingParticle.Factory.class)
public abstract class DiggingParticle$FactoryMixin implements DiggingParticle$FactoryExtensions {
	@Override
	@Unique
	public Particle create$makeParticleAtPos(BlockParticleData blockParticleData, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
		return !blockParticleData.getBlockState().isAir() && !blockParticleData.getBlockState().isIn(Blocks.MOVING_PISTON)
				? ((DiggingParticle$FactoryExtensions) (new DiggingParticle(clientWorld, d, e, f, g, h, i, blockParticleData.getBlockState())).init()).create$updateSprite(((BlockParticleDataExtensions) blockParticleData).create$getPos())
				: null;
	}
}
