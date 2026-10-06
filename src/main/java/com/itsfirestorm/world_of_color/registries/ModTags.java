package com.itsfirestorm.world_of_color.registries;

import com.itsfirestorm.world_of_color.api.WorldOfColorsAPI;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ModTags {
    public static final TagKey<Item> UNPAINTABLE =
            ItemTags.create(ResourceLocation.fromNamespaceAndPath(WorldOfColorsAPI.MODID, "unpaintable"));
}
