/*
 * Miasma Minecraft Mod
 * Copyright © 2021 Dashkal <dashkal@darksky.ca>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package name.dashkal.minecraft.miasma.integration.client.curios;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import name.dashkal.minecraft.miasma.api.MiasmaAPI;
import name.dashkal.minecraft.miasma.integration.common.curios.ICurioRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;

import static name.dashkal.minecraft.miasma.lib.CastUtils.ifCast;

/**
 * Render for Miasma provided masks used as a curio rather than an armor piece.
 */
public class MaskCurioRenderer implements ICurioRenderer {
    private static final ResourceLocation MASK_RESOURCE = new ResourceLocation(MiasmaAPI.getInstance().getMiasmaModId(), "textures/models/armor/cloth_mask_layer_1.png");

    private final BipedModel<LivingEntity> model;

    public MaskCurioRenderer() {
        this.model = new BipedModel<>(0.2f); // Added to the scaling to space out this layer. 0.5f is inner armor. 1.0f is outer armor.
        model.setAllVisible(false);
        model.head.visible = true;
        model.hat.visible = true;
    }

    /** Renders the mask  */
    @SuppressWarnings("unchecked") // Use of BipedModel.copyPropertiesTo
    @Override
    public void render(String identifier,
                       int index,
                       MatrixStack mStack,
                       IRenderTypeBuffer renderTypeBuffer,
                       int packedLightCoords,
                       LivingEntity livingEntity,
                       float limbSwing,
                       float limbSwingAmount,
                       float partialTicks,
                       float ageInTicks,
                       float netHeadYaw,
                       float headPitch) {
        // Only render if the entity uses the biped model
        ifCast(IEntityRenderer.class, Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(livingEntity), er ->
            ifCast(BipedModel.class, er.getModel(), bipedModel ->
                CuriosClientIntegration.findRenderingItemStack(livingEntity, identifier, index).ifPresent(itemStack -> {
                    // Copy the wearer's scale and orientation to our overlay model
                    bipedModel.copyPropertiesTo(model); // Nothing inside actually uses T, making this safe.

                    // Render
                    IVertexBuilder vertexBuilder = ItemRenderer.getArmorFoilBuffer(renderTypeBuffer, RenderType.armorCutoutNoCull(MASK_RESOURCE), false, itemStack.hasFoil());
                    model.renderToBuffer(mStack, vertexBuilder, packedLightCoords, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f);
                })
            )
        );
    }
}
