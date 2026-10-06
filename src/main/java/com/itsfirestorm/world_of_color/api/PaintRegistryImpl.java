package com.itsfirestorm.world_of_color.api;

import com.itsfirestorm.world_of_color.fluids.PaintFluidType;
import com.itsfirestorm.world_of_color.items.Paint;
import com.itsfirestorm.world_of_color.recipes.PaintDyesBlocks;
import com.itsfirestorm.world_of_color.registries.ModFluids;
import com.itsfirestorm.world_of_color.registries.ModItems;
import com.itsfirestorm.world_of_color.registries.ModTags;
import com.itsfirestorm.world_of_color.util.PaintColorMapper;
import com.itsfirestorm.world_of_color.util.PaintColorMapperModded;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Supplier;

public class PaintRegistryImpl implements PaintRegistry {

    private final Map<PaintColor, DeferredItem<Item>> items = new EnumMap<>(PaintColor.class);
    private final Map<PaintColor, DeferredHolder<Fluid, ?>> fluids = new EnumMap<>(PaintColor.class);
    private final Map<PaintColor, DeferredHolder<FluidType, PaintFluidType>> fluidTypes = new EnumMap<>(PaintColor.class);
    private final Set<Item> excluded = new HashSet<>();

    public PaintRegistryImpl() {
        for (PaintColor color : PaintColor.values()) {
            items.put(color, ModItems.getPaint(color));
            fluids.put(color, ModFluids.getFluid(color));
            fluidTypes.put(color, ModFluids.getType(color));
        }
    }

    @Override
    public Optional<Item> getPaintItem(PaintColor paintColor) {
        DeferredItem<Item> holder = items.get(paintColor);
        return holder != null && holder.isBound() ? Optional.of(holder.get()) : Optional.empty();
    }

    @Override
    public Optional<Supplier<? extends Fluid>> getPaintFluid(PaintColor color) {
        DeferredHolder<Fluid, ?> holder = fluids.get(color);
        return holder != null && holder.isBound()
                ? Optional.of((Supplier<? extends Fluid>) holder)
                : Optional.empty();
    }

    @Override
    public Optional<PaintFluidType> getPaintFluidType(PaintColor color) {
        DeferredHolder<FluidType, PaintFluidType> holder = fluidTypes.get(color);
        return holder != null && holder.isBound() ? Optional.of(holder.get()) : Optional.empty();
    }

    @Override
    public Optional<PaintColor> getPaintColorForFluidType(FluidType fluidType) {
        if (fluidType instanceof PaintFluidType paintFluidType) {
            return Optional.of(paintFluidType.getPaintColor());
        }
        return Optional.empty();
    }

    @Override
    public Map<PaintColor, Item> allPaintItems() {
        Map<PaintColor, Item> result = new EnumMap<>(PaintColor.class);
        items.forEach((color, holder) -> {
            if (holder.isBound()) result.put(color, holder.get());
        });
        return Collections.unmodifiableMap(result);
    }

    @Override
    public void registerRecolorFamily(Item... itemsByDyeColor) {
        PaintColorMapperModded.registerFamily(itemsByDyeColor);
    }

    @Override
    public void registerRecolorFamily(Map<net.minecraft.world.item.DyeColor, Item> sparseFamily) {
        PaintColorMapperModded.registerSparseFamily(sparseFamily);
    }

    @Override
    public boolean isPaintable(Level level, ItemStack stack) {
        if (isExcluded(stack)) return false;
        if (PaintColorMapper.isRecolorable(stack) || PaintColorMapperModded.isRecolorable(stack)) {
            return true;
        }
        return findMatchingRecipe(level, stack, null).isPresent();
    }

    @Override
    public Optional<ItemStack> recolor(Level level, ItemStack stack, PaintColor color) {
        if (isExcluded(stack)) return Optional.empty();
        Optional<ItemStack> mapped = PaintColorMapperModded.recolor(stack, color)
                .or(() -> PaintColorMapper.recolor(stack, color));
        if (mapped.isPresent()) {
            return mapped.get().getItem() == stack.getItem() ? Optional.empty() : mapped;
        }
        return findMatchingRecipe(level, stack, color).map(recipe -> {
                ItemStack result = recipe.getResult().copyWithCount(1);
                result.applyComponentsAndValidate(stack.getComponentsPatch());
                return result;
        }).filter(result -> result.getItem() != stack.getItem());
    }

    private Optional<PaintDyesBlocks> findMatchingRecipe(Level level, ItemStack stack, PaintColor color) {
        return level.getRecipeManager()
                .getAllRecipesFor(RecipeType.CRAFTING)
                .stream()
                .map(RecipeHolder::value)
                .filter(r -> r instanceof PaintDyesBlocks)
                .map(r -> (PaintDyesBlocks) r)
                .filter(r -> r.canApply(stack, color))
                .findFirst();
    }

    @Override
    public void excludeRecoloring(Item... items) {
        excluded.addAll(Arrays.asList(items));
    }

    @Override
    public boolean isExcluded(ItemStack stack) {
        return stack.getItem() instanceof Paint
                || excluded.contains(stack.getItem())
                || stack.is(ModTags.UNPAINTABLE);
    }
}
