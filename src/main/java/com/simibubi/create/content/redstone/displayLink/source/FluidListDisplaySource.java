package com.simibubi.create.content.redstone.displayLink.source;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.lang3.mutable.MutableInt;

import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.smartObserver.SmartObserverBlockEntity;
import com.simibubi.create.content.trains.display.FlapDisplayBlockEntity;
import com.simibubi.create.content.trains.display.FlapDisplayLayout;
import com.simibubi.create.content.trains.display.FlapDisplaySection;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.TankManipulationBehaviour;
import com.simibubi.create.foundation.gui.ModularGuiLineBuilder;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.FluidFormatter;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.LongAttached;

import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import io.github.fabricators_of_create.porting_lib.util.FluidUnit;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;

public class FluidListDisplaySource extends ValueListDisplaySource {

	@Override
	protected Stream<LongAttached<MutableComponent>> provideEntries(DisplayLinkContext context, int maxRows) {
		BlockEntity sourceBE = context.getSourceBlockEntity();
		if (!(sourceBE instanceof SmartObserverBlockEntity cobe))
			return Stream.empty();

		TankManipulationBehaviour tankManipulationBehaviour = cobe.getBehaviour(TankManipulationBehaviour.OBSERVE);
		FilteringBehaviour filteringBehaviour = cobe.getBehaviour(FilteringBehaviour.TYPE);
		Storage<FluidVariant> handler = tankManipulationBehaviour.getInventory();

		if (handler == null)
			return Stream.empty();


		Map<Fluid, Long> fluids = new HashMap<>();
		Map<Fluid, FluidStack> fluidNames = new HashMap<>();

		try (Transaction t = TransferUtil.getTransaction()) {
			for (StorageView<FluidVariant> view : handler.nonEmptyViews()) {
				FluidStack stack = new FluidStack(view);
				if (!filteringBehaviour.test(stack))
					continue;

				fluids.merge(stack.getFluid(), stack.getAmount(), Long::sum);
				fluidNames.putIfAbsent(stack.getFluid(), stack);
			}
		}

		return fluids.entrySet()
				.stream()
				.sorted(Comparator.<Map.Entry<Fluid, Long>>comparingDouble(value -> value.getValue()).reversed())
				.limit(maxRows)
				.map(entry -> LongAttached.with(
						entry.getValue(),
						FluidVariantAttributes.getName(fluidNames.get(entry.getKey()).getType()).copy()
				));
	}

	@Override
	protected List<MutableComponent> createComponentsFromEntry(DisplayLinkContext context, LongAttached<MutableComponent> entry) {
		long amount = entry.getFirst();
		MutableComponent name = entry.getSecond().append(WHITESPACE);

		Couple<MutableComponent> formatted = FluidFormatter.asComponents(amount, shortenNumbers(context), getUnit(context));

		return List.of(formatted.getFirst(), formatted.getSecond(), name);
	}

	@Override
	public void loadFlapDisplayLayout(DisplayLinkContext context, FlapDisplayBlockEntity flapDisplay, FlapDisplayLayout layout) {
		Integer max = ((MutableInt) context.flapDisplayContext).getValue();
		boolean shorten = shortenNumbers(context);
		FluidUnit fluidUnit = getUnit(context);
		int length = FluidFormatter.asString(max, shorten, fluidUnit).length();
		String layoutKey = "FluidList_" + length;

		if (layout.isLayout(layoutKey))
			return;

		int unitLength = fluidUnit == FluidUnit.DROPLETS ? 8 : 2;

		int maxCharCount = flapDisplay.getMaxCharCount(1);
		int numberLength = Math.min(maxCharCount, Math.max(3, length - unitLength));
		int nameLength = Math.max(maxCharCount - numberLength - unitLength, 0);

		FlapDisplaySection value = new FlapDisplaySection(FlapDisplaySection.MONOSPACE * numberLength, "number", false, false);
		FlapDisplaySection unit = new FlapDisplaySection(FlapDisplaySection.MONOSPACE * unitLength, "fluid_units", true, true);
		FlapDisplaySection name = new FlapDisplaySection(FlapDisplaySection.MONOSPACE * nameLength, "alphabet", false, false);

		layout.configure(layoutKey, List.of(value, unit, name));
	}

	@Override
	protected String getTranslationKey() {
		return "list_fluids";
	}

	@Override
	protected boolean valueFirst() {
		return false;
	}

	protected FluidUnit getUnit(DisplayLinkContext context) {
		int format = context.sourceConfig().getInt("Format");
		return format == 2 ? FluidUnit.DROPLETS : FluidUnit.MILLIBUCKETS;
	}

	@Override
	protected void addFullNumberConfig(ModularGuiLineBuilder builder) {
		builder.addSelectionScrollInput(0, 75,
				(si, l) -> si.forOptions(Lang.translatedOptions("display_source.value_list", "shortened", "full_number", "full_number_droplets"))
					.titled(Lang.translateDirect("display_source.value_list.display")),
				"Format");
	}
}
