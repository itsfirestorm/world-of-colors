package com.itsfirestorm.world_of_color.api;

import com.itsfirestorm.world_of_color.fluids.PaintFluidType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidType;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public interface PaintRegistry {

    Optional<Item> getPaintItem(PaintColor color);

    Optional<Supplier<? extends Fluid>> getPaintFluid(PaintColor color);

    Optional<PaintFluidType> getPaintFluidType(PaintColor paintColor);

    Optional<PaintColor> getPaintColorForFluidType(FluidType fluidType);

    Map<PaintColor, Item> allPaintItems();

    void registerRecolorFamily(Item... itemsByDyeColor);

    void registerRecolorFamily(Map<net.minecraft.world.item.DyeColor, Item> sparseFamily);

    boolean isPaintable(Level level, ItemStack stack);

    Optional<ItemStack> recolor(Level level, ItemStack stack, PaintColor color);
}
