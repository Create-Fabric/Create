package com.simibubi.create.foundation;

import java.util.Collection;
import java.util.Set;

import com.simibubi.create.Create;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.kinetics.belt.BeltHelper;
import com.simibubi.create.foundation.sound.SoundScapes;
import com.simibubi.create.foundation.utility.LangNumberFormat;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceReloadListenerKeys;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

public class ClientResourceReloadListener implements ResourceManagerReloadListener, IdentifiableResourceReloadListener {
	public static final ResourceLocation ID = Create.asResource("client_reload_listener");
	// fabric: make sure number format is updated after languages load
	public static final Set<ResourceLocation> DEPENDENCIES = Set.of(ResourceReloadListenerKeys.LANGUAGES);

	@Override
	public void onResourceManagerReload(ResourceManager resourceManager) {
		CreateClient.invalidateRenderers();
		SoundScapes.invalidateAll();
		LangNumberFormat.numberFormat.update();
		BeltHelper.uprightCache.clear();
	}

	@Override
	public ResourceLocation getFabricId() {
		return ID;
	}

	@Override
	public Collection<ResourceLocation> getFabricDependencies() {
		return DEPENDENCIES;
	}
}
