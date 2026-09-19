package com.itsfirestorm.world_of_color.compat.jei;

import com.simibubi.create.content.fluids.transfer.FillingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;
import net.minecraft.resources.ResourceLocation;

public class FillingRecipeBuilder extends ProcessingRecipeBuilder<ProcessingRecipeParams, FillingRecipe, FillingRecipeBuilder> {

    public FillingRecipeBuilder(ResourceLocation id) {
        super(FillingRecipe::new, id);
    }

    // Anonymous subclass to reach protected constructor
    // !! If create ever gets moves the constructor, this whole class becomes obsolete
    @Override
    protected ProcessingRecipeParams createParams() {
        return new ProcessingRecipeParams() {};
    }

    @Override
    public FillingRecipeBuilder self() {
        return this;
    }
}