package com.simibubi.create.compat;

import java.util.Optional;
import java.util.function.Supplier;

import com.simibubi.create.foundation.utility.Lang;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

/**
 * For compatibility with and without another mod present, we have to define load conditions of the specific code
 */
public enum Mods {
	AETHER,
	COMPUTERCRAFT,
	CONNECTIVITY,
	CURIOS,
	DYNAMICTREES,
	OCCULTISM,
	PACKETFIXER,
	STORAGEDRAWERS,
	TCONSTRUCT,
	XLPACKETS,

	// fabric mods
	SANDWICHABLE,
	TRINKETS,
	MODMENU,
	BOTANIA,
	SODIUM,
	INDIUM;

	private final String id;
	private final boolean loaded;

	Mods() {
		id = Lang.asId(name());
		loaded = FabricLoader.getInstance().isModLoaded(id);
	}

	/**
	 * @return the mod id
	 */
	public String id() {
		return id;
	}

	public ResourceLocation rl(String path) {
		return new ResourceLocation(id, path);
	}

	public Block getBlock(String id) {
		return BuiltInRegistries.BLOCK.get(rl(id));
	}

	/**
	 * @return a boolean of whether the mod is loaded or not based on mod id
	 */
	public boolean isLoaded() {
		return loaded;
    }

	/**
	 * Simple hook to run code if a mod is installed
	 * @param toRun will be run only if the mod is loaded
	 * @return Optional.empty() if the mod is not loaded, otherwise an Optional of the return value of the given supplier
	 */
	public <T> Optional<T> runIfInstalled(Supplier<Supplier<T>> toRun) {
		if (isLoaded())
			return Optional.of(toRun.get().get());
		return Optional.empty();
	}

	/**
	 * Simple hook to execute code if a mod is installed
	 * @param toExecute will be executed only if the mod is loaded
	 */
	public void executeIfInstalled(Supplier<Runnable> toExecute) {
		if (isLoaded()) {
			toExecute.get().run();
		}
	}
}
