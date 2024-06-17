package com.simibubi.create.compat.shouldersurfing;

import com.github.exopandora.shouldersurfing.api.callback.ITargetCameraOffsetCallback;
import com.github.exopandora.shouldersurfing.api.client.IShoulderSurfing;
import com.github.exopandora.shouldersurfing.api.plugin.IShoulderSurfingPlugin;
import com.github.exopandora.shouldersurfing.api.plugin.IShoulderSurfingRegistrar;
import com.simibubi.create.content.trains.CameraDistanceModifier;

import net.minecraft.world.phys.Vec3;

public class ShoulderSurfingPlugin implements IShoulderSurfingPlugin {
	@Override
	public void register(IShoulderSurfingRegistrar registrar) {
		registrar.registerTargetCameraOffsetCallback(new ITargetCameraOffsetCallback() {
			@Override
			public Vec3 post(IShoulderSurfing instance, Vec3 targetOffset, Vec3 defaultOffset) {
				return targetOffset.multiply(1, 1, CameraDistanceModifier.getMultiplier());
			}
		});
	}
}
