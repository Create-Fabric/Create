package com.simibubi.create.foundation.config;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.simibubi.create.lib.config.Config;
import com.simibubi.create.lib.config.ConfigGroup;
import com.simibubi.create.lib.config.ConfigValue;


public abstract class ConfigBase {

	public abstract Config getConfig();

	public static List<ConfigGroup> currentGroupSet = new ArrayList<>();
	public static ConfigGroup currentGroup;
	public static ConfigGroup getCurrentGroup() {
		return currentGroup;
	}

	public static void initGroups(Config config) {
		for (ConfigGroup group : currentGroupSet) {
			group.setConfig(config);
			group.registerValues();
		}
		currentGroupSet.clear();
	}

//	public ForgeConfigSpec specification;

	protected int depth;
//	protected List<CValue<?, ?>> allValues;
//	protected List<ConfigBase> children;

	protected void registerAll() {
//		for (CValue<?, ?> cValue : allValues)
//			cValue.register(builder);
	}

	public void onLoad() {
//		if (children != null)
//			children.forEach(ConfigBase::onLoad);
	}

	public void onReload() {
//		if (children != null)
//			children.forEach(ConfigBase::onReload);
	}

	public abstract String getName();

//	@FunctionalInterface
//	protected static interface IValueProvider<V, T extends ConfigValue<V>>
//		extends Function<ForgeConfigSpec.Builder, T> {
//	}

	protected ConfigBool b(boolean current, String name, String... comment) {
		ConfigBool result = new ConfigBool(name, current);
		result.addComments(comment);
		result.setConstraint(ConfigValue.TYPE);
		getCurrentGroup().addConfigValue(result);
		Config.valuesAndStrings.put(result.value.toString(), result);
		return result;
	}

	protected ConfigFloat f(float current, float min, float max, String name, String... comment) {
		ConfigFloat result = new ConfigFloat(name, current);
		result.addComments(comment);
		result.max = max;
		result.min = min;
		result.setConstraint(ConfigValue.MIN_MAX);
		getCurrentGroup().addConfigValue(result);
		Config.valuesAndStrings.put(result.value.toString(), result);
		return result;
	}

	protected ConfigFloat f(float current, float min, String name, String... comment) {
		return f(current, min, Float.MAX_VALUE, name, comment);
	}

	protected ConfigInt i(int current, int min, int max, String name, String... comment) {
		ConfigInt result = new ConfigInt(name, current);
		result.addComments(comment);
		result.max = max;
		result.min = min;
		result.setConstraint(ConfigValue.MIN_MAX);
		getCurrentGroup().addConfigValue(result);
		Config.valuesAndStrings.put(result.value.toString(), result);
		return result;
	}

	protected ConfigInt i(int current, int min, String name, String... comment) {
		return i(current, min, Integer.MAX_VALUE, name, comment);
	}

	protected <T extends Enum<T>> ConfigEnum<T> e(T defaultValue, String name, String... comment) {
		ConfigEnum<T> result = new ConfigEnum<T>(name, defaultValue);
		result.addComments(comment);
		result.setConstraint(ConfigValue.TYPE);
		getCurrentGroup().addConfigValue(result);
		Config.valuesAndStrings.put(result.value.toString(), result);
		return result;
	}

	protected ConfigGroup group(int depth, String name, String... comment) {
		ConfigGroup group = new ConfigGroup(name, depth, comment);
		currentGroup = group;
		currentGroupSet.add(group);
		return group;
	}

	protected static <T extends ConfigBase> T nested(int depth, Supplier<T> constructor, String... comment) {
		T config = constructor.get();
//		new ConfigGroup(config.getName(), depth, comment);
//		new CValue<Boolean, ForgeConfigSpec.BooleanValue>(config.getName(), builder -> {
//			config.depth = depth;
//			config.registerAll(builder);
//			if (config.depth > depth)
//				builder.pop(config.depth - depth);
//			return null;
//		});
//		if (children == null)
//			children = new ArrayList<>();
//		children.add(config);
		return config;
	}

//	public class CValue<V, T extends ConfigValue<V>> {
//		protected ConfigValue<V> value;
//		protected String name;
//		private IValueProvider<V, T> provider;
//
//		public CValue(String name, IValueProvider<V, T> provider, String... comment) {
//			this.name = name;
//			this.provider = builder -> {
//				addComments(builder, comment);
//				return provider.apply(builder);
//			};
//			if (allValues == null)
//				allValues = new ArrayList<>();
//			allValues.add(this);
//		}
//
//		public void addComments(Builder builder, String... comment) {
//			if (comment.length > 0) {
//				String[] comments = new String[comment.length + 1];
//				comments[0] = "";
//				System.arraycopy(comment, 0, comments, 1, comment.length);
//				builder.comment(comments);
//			} else
//				builder.comment("");
//		}
//
//		public void register(ForgeConfigSpec.Builder builder) {
//			value = provider.apply(builder);
//		}
//
//		public V get() {
//			return value.get();
//		}
//
//		public void set(V value) {
//			this.value.set(value);
//		}
//
//		public String getName() {
//			return name;
//		}
//	}
//
//	/**
//	 * Marker for config subgroups
//	 */
//	public static class ConfigGroupWrapper extends ConfigGroup {
//		private int groupDepth;
//		private String[] comment;
//
//		public ConfigGroup(String name, int depth, String... comment) {
//			super(name, builder -> null, comment);
//			groupDepth = depth;
//			this.comment = comment;
//		}
//
//		@Override
//		public void register(Builder builder) {
//			if (depth > groupDepth)
//				builder.pop(depth - groupDepth);
//			depth = groupDepth;
//			addComments(builder, comment);
//			builder.push(getName());
//			depth++;
//		}
//
//	}

	public static class ConfigBool extends ConfigValue<Boolean> {
		public ConfigBool(String key, boolean value) {
			super(key, value);
		}
	}

	public static class ConfigEnum<T> extends ConfigValue<T> {
		public ConfigEnum(String key, T value) {
			super(key, value);
		}
	}

	public static class ConfigFloat extends ConfigValue<Float> {
		public ConfigFloat(String key, float value) {
			super(key, value);
		}
	}

	public static class ConfigInt extends ConfigValue<Integer> {
		public ConfigInt(String key, Integer value) {
			super(key, value);
		}
	}

//	public class ConfigBool extends CValue<Boolean, BooleanValue> {
//
//		public ConfigBool(String name, boolean def, String... comment) {
//			super(name, builder -> builder.define(name, def), comment);
//		}
//	}
//
//	public class ConfigEnum<T extends Enum<T>> extends CValue<T, EnumValue<T>> {
//
//		public ConfigEnum(String name, T defaultValue, String[] comment) {
//			super(name, builder -> builder.defineEnum(name, defaultValue), comment);
//		}
//
//	}
//
//	public class ConfigFloat extends CValue<Double, DoubleValue> {
//
//		public ConfigFloat(String name, float current, float min, float max, String... comment) {
//			super(name, builder -> builder.defineInRange(name, current, min, max), comment);
//		}
//
//		public float getF() {
//			return get().floatValue();
//		}
//	}
//
//	public class ConfigInt extends CValue<Integer, IntValue> {
//
//		public ConfigInt(String name, int current, int min, int max, String... comment) {
//			super(name, builder -> builder.defineInRange(name, current, min, max), comment);
//		}
//	}

}
