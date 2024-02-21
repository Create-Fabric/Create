package com.simibubi.create.foundation.utility;

import java.text.NumberFormat;
import java.util.Locale;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.util.Mth;

public class LangNumberFormat {

	private NumberFormat format = NumberFormat.getNumberInstance(Locale.ROOT);
	public static LangNumberFormat numberFormat = new LangNumberFormat();

	public NumberFormat get() {
		return format;
	}

	public void update() {
		LanguageInfo lang = Minecraft.getInstance()
				.getLanguageManager()
				.getSelected();
		Locale locale = lang.getJavaLocale();

		// fabric: clear error if this is somehow null.
		if (locale == null) {
			throw new IllegalStateException("LanguageInfo's javaLocale is null! info: " + lang);
		}

		format = NumberFormat.getInstance(locale);
		format.setMaximumFractionDigits(2);
		format.setMinimumFractionDigits(0);
		format.setGroupingUsed(true);
	}

	public static String format(double d) {
		if (Mth.equal(d, 0))
			d = 0;
		return numberFormat.get()
			.format(d)
			.replace("\u00A0", " ");
	}

}
