package com.itsfirestorm.world_of_color.api;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class BottleFillRegistry {
    private BottleFillRegistry() {}

    private record Entry(Predicate<FluidStack> matcher, Function<FluidStack, ItemStack> converter) {}
    private static final List<Entry> ENTRIES = new ArrayList<>();

    public static void register(Predicate<FluidStack> matcher, Function<FluidStack, ItemStack> converter) {
        ENTRIES.add(new Entry(matcher, converter));
    }

    public static boolean matches(FluidStack stack) {
        return ENTRIES.stream().anyMatch(e -> e.matcher().test(stack));
    }

    public static Optional<ItemStack> convert(FluidStack stack) {
        return ENTRIES.stream()
                .filter(e -> e.matcher().test(stack))
                .findFirst()
                .map(e -> e.converter().apply(stack));
    }


}
