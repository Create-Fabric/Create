package com.simibubi.create.foundation.mixin;

import io.github.fabricators_of_create.porting_lib.extensions.ITeleporter;

import net.minecraft.world.level.portal.PortalForcer;

import org.spongepowered.asm.mixin.Mixin;

/**
 * Forge implements {@link ITeleporter} Through patches so... Let's do the same here!
 */
@Mixin(PortalForcer.class)
public class PortalForcerMixin implements ITeleporter {}
