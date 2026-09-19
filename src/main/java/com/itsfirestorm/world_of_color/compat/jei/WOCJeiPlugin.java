package com.itsfirestorm.world_of_color.compat.jei;

import com.itsfirestorm.world_of_color.api.BottleFillRegistry;
import com.itsfirestorm.world_of_color.api.WorldOfColorsAPI;

import com.simibubi.create.content.fluids.transfer.FillingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.api.recipe.RecipeType;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@JeiPlugin
public class WOCJeiPlugin implements IModPlugin {

    private static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(WorldOfColorsAPI.MODID, "jei_plugin");

    public static final RecipeType<RecipeHolder<FillingRecipe>> SPOUT_FILLING =
            RecipeType.createRecipeHolderType(ResourceLocation.fromNamespaceAndPath("create", "spout_filling"));

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<RecipeHolder<FillingRecipe>> recipes = new ArrayList<>();
        var entries = BottleFillRegistry.entries();
        var logger = org.apache.logging.log4j.LogManager.getLogger("WOCJeiPlugin");
        logger.info("BottleFillRegistry has {} entries at JEI registration time.", entries.size());

        for (BottleFillRegistry.Entry entry : entries) {
            for (FluidStack displayFluid : entry.displayFluids().get()) {
                try {
                    FluidStack sample = displayFluid.copy();
                    sample.setAmount(entry.amount());

                    ItemStack output = entry.converter().apply(sample);
                    if (output.isEmpty()) {
                        logger.warn("Entry for fluid {} produced an empty output, skipping.",
                                BuiltInRegistries.FLUID.getKey(sample.getFluid()));
                        continue;
                    }

                    ResourceLocation id = ResourceLocation.fromNamespaceAndPath(
                            WorldOfColorsAPI.MODID,
                            "jei/filling/" + BuiltInRegistries.FLUID.getKey(sample.getFluid()).getPath()
                    );

                    FillingRecipe recipe = new FillingRecipeBuilder(id)
                            .withItemIngredients(Ingredient.of(Items.GLASS_BOTTLE))
                            .withFluidIngredients(SizedFluidIngredient.of(sample))
                            .withItemOutputs(new ProcessingOutput(output, 1f))
                            .build();

                    recipes.add(new RecipeHolder<>(id, recipe));
                } catch (Exception e) {
                    logger.error("Failed to build JEI filling display for fluid {}",
                    BuiltInRegistries.FLUID.getKey(displayFluid.getFluid()), e);
                }
            }
        }

        registration.addRecipes(SPOUT_FILLING, recipes);
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        var recipeManager = jeiRuntime.getRecipeManager();
        var logger = org.apache.logging.log4j.LogManager.getLogger("WOCJeiPlugin");

        List<RecipeHolder<FillingRecipe>> spoutRecipes =
                recipeManager.createRecipeLookup(SPOUT_FILLING).get().toList();

        logger.info("Found {} recipes in SPOUT_FILLING category", spoutRecipes.size());

        Set<ResourceLocation> claimedFluids = BottleFillRegistry.entries().stream()
                .flatMap(e -> e.displayFluids().get().stream())
                .map(fs -> BuiltInRegistries.FLUID.getKey(fs.getFluid()))
                .collect(Collectors.toSet());

        logger.info("Claimed fluids: {} ", claimedFluids);

        List<RecipeHolder<FillingRecipe>> toHide = spoutRecipes.stream()
                .filter(holder -> !holder.id().getPath().startsWith("jei/filling/"))
                .filter(holder -> holder.value().getIngredients().stream()
                        .anyMatch(ing -> ing.test(new ItemStack(Items.GLASS_BOTTLE))))
                .filter(holder -> Arrays.stream(holder.value().getRequiredFluid().getFluids())
                        .anyMatch(fs -> claimedFluids.contains(BuiltInRegistries.FLUID.getKey(fs.getFluid()))))
                .toList();

        recipeManager.hideRecipes(SPOUT_FILLING, toHide);
        logger.info("Hiding {} recipes: {}", toHide.size(),
                toHide.stream().map(RecipeHolder::id).toList());
    }
}
