package com.itsfirestorm.world_of_color.compat.jei;

import com.itsfirestorm.world_of_color.recipes.PaintDyesBlocks;
import com.simibubi.create.AllBlocks;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class BasinInteractionCategory extends AbstractRecipeCategory<WOCJeiPlugin.BasinInteraction> {
    private BasinDyeDrawable animation = new BasinDyeDrawable();

    public BasinInteractionCategory(IGuiHelper guiHelper) {
        super(
                WOCJeiPlugin.BASIN,
                Component.translatable("jei.world_of_color.basin_interaction"),
                guiHelper.createDrawableIngredient(
                        VanillaTypes.ITEM_STACK,
                        AllBlocks.BASIN.asItem().getDefaultInstance()
                ),
                140,
                70
        );
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, WOCJeiPlugin.BasinInteraction recipe, IFocusGroup focuses) {
        var target = builder.addSlot(RecipeIngredientRole.INPUT, 4 ,8)
                .setSlotName("target")
                .setStandardSlotBackground()
                .addItemStacks(recipe.targets());

        builder.addSlot(RecipeIngredientRole.INPUT, 4, 42)
                .setStandardSlotBackground()
                .addFluidStack(recipe.fluid().getFluid(), recipe.fluid().getAmount());

        var result = builder.addSlot(RecipeIngredientRole.OUTPUT, 118, 26)
                .setSlotName("result")
                .setStandardSlotBackground()
                .addItemStacks(recipe.results());

        builder.createFocusLink(target, result);
    }

    @Override
    public void draw(WOCJeiPlugin.BasinInteraction recipe, IRecipeSlotsView slots, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        ItemStack target = slots.findSlotByName("target")
                .flatMap(IRecipeSlotView::getDisplayedItemStack)
                .orElse(recipe.targets().get(0));

        ItemStack result = slots.findSlotByName("result")
                .flatMap(IRecipeSlotView::getDisplayedItemStack)
                .orElse(recipe.results().get(0));

        BasinDyeDrawable.draw(guiGraphics, 30, 0, target, recipe.fluid(), result);
    }
}