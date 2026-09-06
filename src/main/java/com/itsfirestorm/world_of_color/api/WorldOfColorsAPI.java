package com.itsfirestorm.world_of_color.api;

import com.itsfirestorm.world_of_color.fluids.PaintFluidType;
import net.minecraft.world.item.ItemStack;

public final class WorldOfColorsAPI {
    public static final String MODID = "world_of_color";
    public static final String VERSION = "1.0.0";

    private static PaintRegistry registry;

    private WorldOfColorsAPI() {}

    public static PaintRegistry registry() {
        if (registry == null) {
            throw new IllegalStateException(
                    "world_of_color API accessed before initialization — " +
                            "call this after mod loading (e.g. FMLCommonSetupEvent or later), not during class loading."
            );
        }
        return registry;
    }

    public static void internalInit(PaintRegistry impl) {
        if (registry != null) {
            throw new IllegalStateException("world_of_color API already initialized.");
        }
        registry = impl;

        BottleFillRegistry.register(
                stack -> stack.getFluidType() instanceof PaintFluidType,
                stack -> WorldOfColorsAPI.registry()
                        .getPaintItem(((PaintFluidType) stack.getFluidType()).getPaintColor())
                        .map(ItemStack::new)
                        .orElse(ItemStack.EMPTY)
        );
    }
}
