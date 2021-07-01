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
package name.dashkal.minecraft.miasma.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import name.dashkal.minecraft.miasma.MiasmaMod;
import name.dashkal.minecraft.miasma.lib.client.render.Color;
import name.dashkal.minecraft.miasma.lib.client.render.Colors;
import name.dashkal.minecraft.miasma.lib.client.render.MiasmaSprite;
import name.dashkal.minecraft.miasma.common.capability.IMiasmaHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.opengl.GL11;

import java.util.Comparator;
import java.util.List;

/**
 * GUI Overlay used to display debugging information about an active miasma infection.
 */
public class MiasmaDebugHUD extends AbstractGui {
    private static final Minecraft minecraft = Minecraft.getInstance();
    private static final Color FADED_GREY = Colors.DARK_GREY.withAlpha(0.5f);

    public void render(MatrixStack mStack, PlayerEntity player, IMiasmaHandler miasmaHandler) {
        FontRenderer font = minecraft.font;
        //int maxW = minecraft.getWindow().getGuiScaledWidth();
        int maxH = minecraft.getWindow().getGuiScaledHeight();

        List<IFormattableTextComponent> debugReport = miasmaHandler.getDebugReport();
        int minWidth = debugReport.stream().map(font::width).max(Comparator.comparingInt(a -> a)).orElse(0);

        int color = Colors.WHITE.getColorRGB();
        int lines = debugReport.size();
        int x = 5;
        int spacing = 10;
        int y = (maxH - (spacing * lines)) / 2;
        int width = miasmaHandler.getInfection().isPresent() ? Math.max(104, minWidth) : 97;
        int bgColor = FADED_GREY.getColorARGB();

        GL11.glPushMatrix();
        RenderSystem.enableBlend();

        try {
            RenderSystem.defaultBlendFunc();
            fill(mStack, x - 2, y - 2, x + width + 4, y + (spacing * lines) + 2, bgColor);

            for (ITextComponent t : debugReport) {
                font.drawShadow(mStack, t, x, y, color);
                y += spacing;
            }
        } finally {
            RenderSystem.disableBlend();
            GL11.glPopMatrix();
        }
    }
}
