package com.itsfirestorm.world_of_color.util;

import com.itsfirestorm.world_of_color.api.BottleFillRegistry;
import net.minecraft.world.item.ItemStack;
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
        return BottleFillRegistry.matches(stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        // This is called when the bottle extracts fluid FROM a container
        if (resource.isEmpty() || !isFluidValid(0, resource)) {
            return 0;
        }

        int fillAmount = Math.min(CAPACITY, resource.getAmount());
        if (fillAmount < CAPACITY) return 0;

        if (action.execute()) { // Only fill if you can fill completely
            BottleFillRegistry.convert(resource).ifPresent(item -> container = item.copy());
        }

        return fillAmount;
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