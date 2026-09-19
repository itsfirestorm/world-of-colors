package com.itsfirestorm.world_of_color.api;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class BottleFillRegistry {
    private BottleFillRegistry() {}
    public static final int DEFAULT_AMOUNT = 250;

    public record Entry(Predicate<FluidStack> matcher, Function<FluidStack, ItemStack> converter,
                         int amount, Supplier<List<FluidStack>> displayFluids) {}
    public static final List<Entry> ENTRIES = new ArrayList<>();

    public static List<Entry> entries() {
        return List.copyOf(ENTRIES);
    }

    /**
     *
     * @param matcher - FluidStack to match
     * @param converter - Stack to Item conversion
     * @param displayFluids - List of fluids to represent conversion, this is for JEI only.
     */
    public static void register(Predicate<FluidStack> matcher, Function<FluidStack, ItemStack> converter,
                                Supplier<List<FluidStack>> displayFluids) {
        ENTRIES.add(new Entry(matcher, converter, DEFAULT_AMOUNT, displayFluids));
    }

    /**
     *
     * @param matcher - FluidStack to match
     * @param converter - Stack to Item conversion
     * @param amount - User-defined amount
     * @param displayFluids - List of fluids to represent conversion, this is for JEI only.
     * <p>
     * This mostly just serves as a separate way of registration where the user can define a set amount if wanted to,
     * although this is the "Bottle Fill Registry" I think we should adapt to other mods probably wanting more mB to
     * match their conversion.
     */
    public static void register(Predicate<FluidStack> matcher, Function<FluidStack, ItemStack> converter,
                                int amount, Supplier<List<FluidStack>> displayFluids) {
        ENTRIES.add(new Entry(matcher, converter, amount, displayFluids));
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

    public static OptionalInt getAmount(FluidStack stack) {
        return ENTRIES.stream()
                .filter(e -> e.matcher().test(stack))
                .findFirst()
                .map(e -> OptionalInt.of(e.amount()))
                .orElse(OptionalInt.empty());
    }

    public static int maxAmount() {
        return ENTRIES.stream().mapToInt(Entry::amount).max().orElse(DEFAULT_AMOUNT);
    }
}
