package com.simibubi.create.foundation.utility;

import java.text.NumberFormat;
import java.util.Locale;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.util.Mth;

public class LangNumberFormat {

	private NumberFormat format = NumberFormat.getNumberInstance(Locale.ROOT);
	public static LangNumberFormat numberFormat = new LangNumberFormat();

	public NumberFormat get() {
		return format;
	}

	public void update() {
		LanguageManager manager = Minecraft.getInstance()
				.getLanguageManager();
		Locale locale = manager.getSelectedJavaLocale();

		// fabric: clear error if this is somehow null.
		if (locale == null) {
			throw new IllegalStateException("Locale is null! selected: " + manager.getSelected());
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
