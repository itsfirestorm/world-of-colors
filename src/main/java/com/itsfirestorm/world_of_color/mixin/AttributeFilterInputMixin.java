package com.itsfirestorm.world_of_color.mixin;

import com.itsfirestorm.world_of_color.api.BottleFillRegistry;
import com.itsfirestorm.world_of_color.api.PaintColor;
import com.itsfirestorm.world_of_color.api.WorldOfColorsAPI;
import com.itsfirestorm.world_of_color.registries.ModItems;
import com.simibubi.create.content.logistics.filter.FilterItemStack;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(FilterItemStack.AttributeFilterItemStack.class)
public abstract class AttributeFilterInputMixin {

    @Inject(
            method = "test(Lnet/minecraft/world/level/Level;Lnet/neoforged/neoforge/fluids/FluidStack;Z)Z",
            at = @At("HEAD"),
            cancellable = true,
            require = 1
    )
    private void world_of_color$testMappedFluid(Level world, FluidStack stack, boolean matchNBT,
                                                CallbackInfoReturnable<Boolean> cir) {
        ItemStack item = world_of_color$fluidFilterHelper(stack);
        if (item.isEmpty()) return;
        cir.setReturnValue(((FilterItemStack) (Object) this).test(world, item, matchNBT));
    }

    @Unique
    private static ItemStack world_of_color$fluidFilterHelper(FluidStack stack) {
        if (stack.isEmpty()) return ItemStack.EMPTY;

        Optional<PaintColor> color = WorldOfColorsAPI.registry()
                .getPaintColorForFluidType(stack.getFluidType());
        return color.map(paintColor -> ModItems.getPaint(paintColor).toStack()).orElseGet(() ->
                BottleFillRegistry.convert(stack).orElse(ItemStack.EMPTY));
    }
}
