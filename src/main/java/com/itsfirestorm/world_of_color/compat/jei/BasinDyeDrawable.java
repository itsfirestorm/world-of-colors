package com.itsfirestorm.world_of_color.compat.jei;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import com.simibubi.create.AllBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import org.joml.Matrix4f;

public final class BasinDyeDrawable {

    private static final int CYCLE_MS = 3200; // How long it takes for a loop to cycle.

    // Base scale & position values
    private static final float BASE_X = 50f, BASE_Y = 60f;
    private static final float BASE_Z = 100f, BASE_SCALE = 28f;

    private static final float FLUID_LEVEL = 0.78f; // Base fluid level (adjustable)
    private static final float INSET = 2f / 16f; // Fluid level in basins is ~2/16 to 14/16.

    private static final float ITEM_SIZE = 0.75f; // Item size/scale
    private static final float ITEM_Y_TOP = 1.9f, ITEM_Y_BOTTOM = 0.30f; // Max top Y and max bottom Y used for animation

    private static final float DIP_END = 0.30f, HOLD_END = 0.55f, RAISE_END = 0.85f;
    private static final float SWAP_AT = 0.40f; // Must stay between DIP_END and HOLD_END.

    BasinDyeDrawable() {}

    public static void draw(GuiGraphics guiGraphics, ItemStack target, FluidStack fluid, ItemStack result) {
        float t = (net.minecraft.Util.getMillis() % CYCLE_MS) / (float) CYCLE_MS; // t = ticks, 20t = 1s

        // Animation table
        float dip;
        if (t < DIP_END)        dip = ease(t/DIP_END); // Dip into basin
        else if (t < HOLD_END)  dip = 1f; // Wait a bit
        else if (t < RAISE_END) dip = 1f - ease((t - HOLD_END) / (RAISE_END - HOLD_END)); // Pull out of basin
        else                    dip = 0f; // Show end result, then loop.

        ItemStack shown = t < SWAP_AT ? target : result; // Item to show in the animation
        // During 40% of the animation, the target (ingredient) converts into the result, which is pulled out of the basin.

        float itemY = Mth.lerp(dip, ITEM_Y_TOP, ITEM_Y_BOTTOM); // float of the item's Y value.
        // dip = delta, Y_TOP = start, Y_BOTTOM = end

        PoseStack poseStack = guiGraphics.pose(); // Simplified call

        poseStack.pushPose(); // Start base pose
        poseStack.translate(BASE_X, BASE_Y, BASE_Z); // Base position values
        poseStack.scale(BASE_SCALE, -BASE_SCALE, BASE_SCALE); // Base scale values
        // Basin is rotated 45º to the right and tilted over 22.5º vertically.
        poseStack.mulPose(Axis.XP.rotationDegrees(22.5f));
        poseStack.mulPose(Axis.YP.rotationDegrees(45f));

        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        Lighting.setupFor3DItems();

        drawBasin(guiGraphics); // Draws basin using base pose values
        drawFluid(guiGraphics, fluid); // Draws fluid inside the basin using base pose values
        drawItem(guiGraphics, shown, itemY); // Draws item with its own values, but using base positioning from base values

        poseStack.popPose(); // End posing
    }

    /*
     * Renders basin model
     */
    private static void drawBasin(GuiGraphics guiGraphics) {
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                AllBlocks.BASIN.getDefaultState(), guiGraphics.pose(), guiGraphics.bufferSource(),
                LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        guiGraphics.flush();
    }

    /*
     * Renders fluid, handles color and positions fluid inside the basin.
     */
    private static void drawFluid(GuiGraphics guiGraphics, FluidStack stack) {
        var ext = IClientFluidTypeExtensions.of(stack.getFluid()); // Extension variable to get fluid methods
        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(ext.getStillTexture(stack));
        int tint = ext.getTintColor(stack); // Gets tint color from fluid
        // RGB values
        float r = ((tint >> 16) & 0xFF) / 255f, gr = ((tint >> 8) & 0xFF) / 255f, b = (tint & 0xFF) / 255f;

        Matrix4f m = guiGraphics.pose().last().pose(); // Stores positioning from base values
        float lo = INSET, hi = 1f - INSET, y = FLUID_LEVEL; // Defines vertices and º based on fluid level

        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
        BufferBuilder buf = Tesselator.getInstance()
                .begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        buf.addVertex(m, lo, y, lo).setUv(sprite.getU0(), sprite.getV0()).setColor(r, gr, b, 1f); // bottom left
        buf.addVertex(m, lo, y, hi).setUv(sprite.getU0(), sprite.getV1()).setColor(r, gr, b, 1f); // bottom right
        buf.addVertex(m, hi, y, hi).setUv(sprite.getU1(), sprite.getV1()).setColor(r, gr, b, 1f); // top left
        buf.addVertex(m, hi, y, lo).setUv(sprite.getU1(), sprite.getV0()).setColor(r, gr, b, 1f); // top right
        BufferUploader.drawWithShader(buf.buildOrThrow());
        RenderSystem.enableCull();
    }

    /*
     * Renders item, handles lighting of the rendered item depending on if the model is flat or not (2D vs 3D)
     */
    private static void drawItem(GuiGraphics guiGraphics, ItemStack stack, float y) {
        Minecraft mc = Minecraft.getInstance();
        PoseStack poseStack = guiGraphics.pose();

        poseStack.pushPose();
        poseStack.translate(0.5f, y, 0.5f);
        poseStack.mulPose(Axis.YP.rotationDegrees(-45f));
        poseStack.mulPose(Axis.XP.rotationDegrees(-22.5f));
        poseStack.scale(ITEM_SIZE, ITEM_SIZE, ITEM_SIZE);

        BakedModel model = mc.getItemRenderer().getModel(stack, mc.level, null, 0);
        boolean flat = !model.usesBlockLight();
        if (flat) Lighting.setupForFlatItems();
        mc.getItemRenderer().render(stack, ItemDisplayContext.GUI, false, poseStack,
                guiGraphics.bufferSource(), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, model);
        guiGraphics.flush();
        if (flat) Lighting.setupFor3DItems(); // If 2D, use regular lighting
        poseStack.popPose();
    }

    private static float ease(float x) { return x * x * (3 - 2 * x); } // Ease bezier
}
