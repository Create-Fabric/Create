package com.simibubi.create.lib.mixin.client;

import java.util.Locale;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.lib.extensions.LanguageExtensions;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.Language;

@Environment(EnvType.CLIENT)
@Mixin(Language.class)
public abstract class LanguageMixin implements LanguageExtensions {
	@Shadow
	@Final
	private String languageCode;

	@Unique
	private Locale create$javaLocale;

	@Inject(at = @At("TAIL"), method = "<init>")
	private void create$addLocale(String string, String string2, String string3, boolean bl, CallbackInfo ci) {
		String[] splitLangCode = languageCode.split("_", 2);
		if (splitLangCode.length == 1) { // Vanilla has some languages without underscores
			this.create$javaLocale = new Locale(languageCode);
		} else {
			this.create$javaLocale = new Locale(splitLangCode[0], splitLangCode[1]);
		}
	}

	@Override
	public Locale create$getJavaLocale() {
		return create$javaLocale;
	}
}
