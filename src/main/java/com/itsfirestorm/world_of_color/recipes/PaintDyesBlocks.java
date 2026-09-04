package com.itsfirestorm.world_of_color.recipes;

import com.itsfirestorm.world_of_color.api.PaintColor;
import com.itsfirestorm.world_of_color.items.Paint;
import com.itsfirestorm.world_of_color.registries.ModRecipeSerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class PaintDyesBlocks extends ShapelessRecipe {

    private final ItemStack result;

    public PaintDyesBlocks(String group, Optional<CraftingBookCategory> category, ItemStack result, NonNullList<Ingredient> ingredients) {
        super(group, category.orElse(CraftingBookCategory.MISC), result, ingredients);
        this.result = result;
    }

    @Override
    public boolean matches(@NotNull CraftingInput input, @NotNull Level level) {
        return super.matches(input, level);
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull CraftingInput input, HolderLookup.@NotNull Provider provider) {
        ItemStack targetStack = ItemStack.EMPTY;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            if (!(stack.getItem() instanceof Paint)) {
                targetStack = stack;
            }
        }

        if (targetStack.isEmpty()) return ItemStack.EMPTY;

        ItemStack resultStack = result.copyWithCount(1);
        resultStack.applyComponentsAndValidate(targetStack.getComponentsPatch());
        return resultStack;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public @NotNull NonNullList<ItemStack> getRemainingItems(@NotNull CraftingInput input) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(input.size(), ItemStack.EMPTY);
        for (int i = 0; i < input.size(); i++) {
            if (input.getItem(i).getItem() instanceof Paint) {
                remaining.set(i, new ItemStack(Items.GLASS_BOTTLE));
            }
        }
        return remaining;
    }


    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.PAINT_BLOCK_DYE.get();
    }

    public ItemStack getResult() {
        return result;
    }

    public boolean canApply(ItemStack target, @Nullable PaintColor color) {
        boolean matchesTarget = false;
        boolean matchesPaint = (color == null);

        for (Ingredient ingredient : getIngredients()) {
            if (!matchesTarget && ingredient.test(target)) {
                matchesTarget = true;
            }
            if (!matchesPaint) {
                for (ItemStack option : ingredient.getItems()) {
                    if (option.getItem() instanceof Paint p && p.getColor() == color) {
                        matchesPaint = true;
                        break;
                    }
                }
            }
        }
        return matchesTarget && matchesPaint;
    }
}
