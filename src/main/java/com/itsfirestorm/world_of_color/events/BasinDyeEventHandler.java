package com.itsfirestorm.world_of_color.events;

import com.itsfirestorm.world_of_color.api.PaintColor;
import com.itsfirestorm.world_of_color.api.PaintHelper;
import com.itsfirestorm.world_of_color.api.WorldOfColorsAPI;
import com.itsfirestorm.world_of_color.items.Paint;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.Objects;
import java.util.Optional;

public class BasinDyeEventHandler {

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        Player player = event.getEntity();
        ItemStack heldStack = player.getItemInHand(InteractionHand.MAIN_HAND);

        // Create already has a built-in function to fill basins using paints that are related to fluids.
        if (heldStack.isEmpty() || heldStack.getItem() instanceof Paint) return;

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof BasinBlockEntity basin)) return;

        // Click check to allow the player to click the filter slot on basins.
        FilteringBehaviour filteringBehaviour = basin.getBehaviour(FilteringBehaviour.TYPE);
        if (filteringBehaviour.getSlotPositioning() instanceof ValueBoxTransform.Sided sided)
            sided.fromSide(event.getFace());
        if (filteringBehaviour.testHit(event.getHitVec().getLocation())) return;

        var fluidHandler = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, null);
        if (fluidHandler == null) return;

        for (int i = 0; i < fluidHandler.getTanks(); i++) {
            FluidStack fluidInTank = fluidHandler.getFluidInTank(i);
            if (fluidInTank.isEmpty()) continue;

            Optional<PaintColor> maybeColor = WorldOfColorsAPI.registry().getPaintColorForFluidType(fluidInTank.getFluidType());
            if (maybeColor.isEmpty()) continue;
            PaintColor paintColor = maybeColor.get();

            // Try armor dyeing first, this doesn't work like crafting, it only replaces the color of the armor
            // by the color of the paint that is in the basin
            if (PaintHelper.isDyeableArmor(heldStack)) {
                if (WorldOfColorsAPI.registry().isExcluded(heldStack)) return;
                if(!level.isClientSide) {
                    FluidStack drained = fluidHandler.drain(
                            new FluidStack(fluidInTank.getFluid(), 50),
                            IFluidHandler.FluidAction.EXECUTE
                    );
                    if (!drained.isEmpty()) {
                        int color = paintColor.getColor();
                        heldStack.set(DataComponents.DYED_COLOR, new DyedItemColor(color, true));
                        level.playSound(null, pos, SoundEvents.PLAYER_SPLASH, SoundSource.AMBIENT, 0.4F, 1.0F);
                    }
                }
                event.setCanceled(true);
                return;
            }

            if(WorldOfColorsAPI.registry().isPaintable(level, heldStack)) {
                var recolored = WorldOfColorsAPI.registry().recolor(level, heldStack, paintColor);

                if (recolored.isEmpty()) {
                    event.setCanceled(true);
                    event.setCancellationResult(InteractionResult.SUCCESS);
                    return;
                }

                if (event.getEntity().isShiftKeyDown()) {
                    // If the player is shifting we're just going to allow any item/block to do their own action
                    // and not cancel it like usual.
                    return;
                }

                if (recolored.get().getItem() == heldStack.getItem()) {
                    event.setCanceled(true);
                    event.setCancellationResult(InteractionResult.SUCCESS);
                    return;
                }

                if(!level.isClientSide()) {
                    FluidStack drained = fluidHandler.drain(
                            new FluidStack(fluidInTank.getFluid(), 50),
                            IFluidHandler.FluidAction.EXECUTE
                    );

                    if (!drained.isEmpty()) {
                        heldStack.shrink(1);

                        ItemStack result = recolored.get();
                        result.setCount(1);
                        player.getInventory().placeItemBackInInventory(result);

                        level.playSound(null, pos, SoundEvents.PLAYER_SPLASH, SoundSource.AMBIENT, 0.4F, 1.0F);
                    }
                }

                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
                return;
            }
        }
    }
}
