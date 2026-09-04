package com.itsfirestorm.world_of_color.util;

import com.itsfirestorm.world_of_color.api.PaintColor;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.block.DyedBlockList;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class PaintColorMapperModded {
    private static final Map<Item, Item[]> COLOR_FAMILIES = new HashMap<>();

    public static void registerFamily(Item... items) {
        if (items.length != 16) {
            throw new IllegalArgumentException(
                    "Recolor family must have exactly 16 items (one per DyeColor), got " + items.length);
        }
        for (Item item : items) {
            if (item != null) COLOR_FAMILIES.put(item, items);
        }
    }

    public static void registerSparseFamily(Map<DyeColor, Item> family) {
        Item[] array = new Item[16];
        family.forEach((dye, item) -> array[dye.getId()] = item);
        for (Item item : array) {
            if (item != null) COLOR_FAMILIES.put(item, array);
        }
    }

    public static boolean isRecolorable(ItemStack stack) {
        return COLOR_FAMILIES.containsKey(stack.getItem());
    }

    public static Optional<ItemStack> recolor(ItemStack stack, PaintColor color) {
        Item[] family = COLOR_FAMILIES.get(stack.getItem());
        if (family == null) return Optional.empty();

        DyeColor dyeColor = PaintColorMapper.toDyeColor(color);
        Item result = family[dyeColor.getId()];
        if (result == null) return Optional.empty();

        ItemStack resultStack = new ItemStack(result, 1);
        resultStack.applyComponentsAndValidate(stack.getComponentsPatch());
        return Optional.of(resultStack);
    }

    public static Set<Item> getPaintableItems() {
        return Collections.unmodifiableSet(COLOR_FAMILIES.keySet());
    }

    public static Map<Item, Item[]> getFamilies() {
        return Collections.unmodifiableMap(COLOR_FAMILIES);
    }
}