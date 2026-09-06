package com.itsfirestorm.world_of_color.util;

import com.itsfirestorm.world_of_color.api.PaintColor;
import com.itsfirestorm.world_of_color.api.WorldOfColorsAPI;
import com.itsfirestorm.world_of_color.fluids.PaintFluidType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;

public class EmptyBottleFluidHandler implements IFluidHandlerItem {
    private ItemStack container;
    private static final int CAPACITY = 250; // 250mb = 1/4 bucket

    public EmptyBottleFluidHandler(ItemStack container) {
        this.container = container;
    }

    @Override
    public @NotNull ItemStack getContainer() {
        return container;
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        return FluidStack.EMPTY; // Empty bottle has no fluid
    }

    @Override
    public int getTankCapacity(int tank) {
        return CAPACITY;
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        // Only accept paint fluids
        return stack.getFluidType() instanceof PaintFluidType;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        // This is called when the bottle extracts fluid FROM a container
        if (resource.isEmpty()) {
            return 0;
        }
        int fillAmount = Math.min(CAPACITY, resource.getAmount());

        if (action.execute() && fillAmount == CAPACITY) { // Only fill if you can fill completely
            if ((resource.getFluidType() instanceof PaintFluidType paintFluidType)) {
                // Get the paint color from the fluid type
                ItemStack paintItem = getPaintItem(paintFluidType);

                // Transform the container
                container = paintItem.copy();
            }
        }

        return fillAmount;
    }

    private static @NotNull ItemStack getPaintItem(PaintFluidType paintFluidType) {
        PaintColor color = paintFluidType.getPaintColor();

        // Transform the empty bottle into the corresponding paint item
        return WorldOfColorsAPI.registry().getPaintItem(color)
                .map(ItemStack::new)
                .orElse(ItemStack.EMPTY);
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        return FluidStack.EMPTY; // Cannot drain from empty bottle
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        return FluidStack.EMPTY; // Cannot drain from empty bottle
    }
}