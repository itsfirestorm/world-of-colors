package com.itsfirestorm.world_of_color.compat.jei;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.AllBlocks;
import mezz.jei.api.gui.drawable.IDrawable;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

public class BasinDyeDrawable implements IDrawable {

    private static final int W = 80, H = 79;
    private static final int CYCLE_MS = 3200;

    private static final int FLUID_X = 22, FLUID_Y = 4, FLUID_W = 44, FLUID_H = 9;

    private WOCJeiPlugin.BasinInteraction display;

    public void setDisplay(WOCJeiPlugin.BasinInteraction display) {
        this.display = display;
    }

    @Override public int getWidth() { return 0; }
    @Override public int getHeight() { return 0; }

    @Override
    public void draw(GuiGraphics guiGraphics, int xOffset, int yOffset) {
        if (display == null) return;

        float t = (net.minecraft.Util.getMillis() % CYCLE_MS) / (float) CYCLE_MS;

        // Animation table
        float dip;
        if (t < 0.30f)          dip = ease(t/0.30f);
        else if (t < 0.65f)     dip = 1f;
        else                    dip = 1f - ease((t - 0.65f) / 0.35f);

        ItemStack shown = t < 0.48f ? display.target() : display.result();

        PoseStackHelper.run(guiGraphics, xOffset, yOffset, () -> {
            // 1. Render Basin
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(8, 26, 0);
            GuiGameElement.of(AllBlocks.BASIN.getDefaultState())
                    .rotateBlock(22.5, 45, 0)
                    .scale(20)
                    .render(guiGraphics);
            guiGraphics.pose().popPose();

            // 2. Render item
            float itemY = Mth.lerp(dip, -2f, 30f);
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(32, itemY, 50);
            guiGraphics.pose().scale(1.25f, 1.25f, 1f);
            guiGraphics.renderItem(shown, 0, 0);
            guiGraphics.pose().popPose();

            // 3. Render fluid
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0, 0, 100);
            drawFluid(guiGraphics, display.fluid(), FLUID_X, FLUID_Y, FLUID_W, FLUID_H, 1.0f);
            guiGraphics.pose().popPose();
        });
    }

    private static void drawFluid(GuiGraphics guiGraphics, FluidStack fluidStack,
                                  int x, int y, int w, int h, float alpha)
    {
        var ext = IClientFluidTypeExtensions.of(fluidStack.getFluid());
        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(ext.getStillTexture(fluidStack));
        int tint = ext.getTintColor();
        RenderSystem.enableBlend();
        guiGraphics.setColor(((tint >> 16) & 0xFF) / 255f, ((tint >> 8) & 0xFF) / 255f, (tint & 0xFF) / 255f, alpha);
        guiGraphics.blit(x, y, 0, w, h, sprite);
        guiGraphics.setColor(1f, 1f, 1f, 1f);
        RenderSystem.disableBlend();
    }

    private static float ease(float x) { return x * x * (3 - 2 * x); }

    private static final class PoseStackHelper {
        static void run(GuiGraphics guiGraphics, int x, int y, Runnable runnable) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(x, y, 0);
            runnable.run();
            guiGraphics.pose().popPose();
        }
    }
}
