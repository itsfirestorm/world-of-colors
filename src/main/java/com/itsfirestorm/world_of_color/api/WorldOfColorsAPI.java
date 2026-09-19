package com.itsfirestorm.world_of_color.api;

import com.itsfirestorm.world_of_color.fluids.PaintFluidType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;

public final class WorldOfColorsAPI {
    public static final String MODID = "world_of_color";
    public static final String VERSION = "1.3.0";
    private static final Logger LOGGER = LogManager.getLogger("WorldOfColorsAPI");

    private static PaintRegistry registry;

    private WorldOfColorsAPI() {}

    public static PaintRegistry registry() {
        if (registry == null) {
            throw new IllegalStateException(
                    "WorldOfColorsAPI accessed before initialization — " +
                            "call this after mod loading (e.g. FMLCommonSetupEvent or later), not during class loading."
            );
        }
        return registry;
    }

    public static void internalInit(PaintRegistry impl) {
        if (registry != null) {
            throw new IllegalStateException("WorldOfColorsAPI already initialized.");
        }
        LOGGER.info("WorldOfColorsAPI successfully initialized.");
        registry = impl;

        BottleFillRegistry.register(
                stack -> stack.getFluidType() instanceof PaintFluidType,
                stack -> WorldOfColorsAPI.registry()
                        .getPaintItem(((PaintFluidType) stack.getFluidType()).getPaintColor())
                        .map(ItemStack::new)
                        .orElse(ItemStack.EMPTY),
                BottleFillRegistry.DEFAULT_AMOUNT,
                () -> PaintColor.values().length == 0
                        ? List.of()
                        : java.util.Arrays.stream(PaintColor.values())
                                .map(color -> WorldOfColorsAPI.registry().getPaintFluid(color))
                                .flatMap(Optional::stream)
                                .map(fluidSupplier -> new FluidStack(fluidSupplier.get(), BottleFillRegistry.DEFAULT_AMOUNT))
                                .toList()
        );
    }
}
