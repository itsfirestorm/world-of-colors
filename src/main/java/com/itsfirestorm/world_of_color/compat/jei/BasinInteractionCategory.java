package com.itsfirestorm.world_of_color.compat.jei;

import com.itsfirestorm.world_of_color.recipes.PaintDyesBlocks;
import com.simibubi.create.AllBlocks;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.network.chat.Component;

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
        builder.addSlot(RecipeIngredientRole.INPUT, 4 ,8)
                .setStandardSlotBackground()
                .addItemStack(recipe.target());

        builder.addSlot(RecipeIngredientRole.INPUT, 4, 42)
                .setStandardSlotBackground()
                .addFluidStack(recipe.fluid().getFluid(), recipe.fluid().getAmount());

        builder.addSlot(RecipeIngredientRole.OUTPUT, 118, 26)
                .setStandardSlotBackground()
                .addItemStack(recipe.result());
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, WOCJeiPlugin.BasinInteraction recipe, IFocusGroup focuses) {
        animation.setDisplay(recipe);
        builder.addDrawable(animation, 30, 0);
    }
}