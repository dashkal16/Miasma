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
package name.dashkal.minecraft.miasma.lib.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/**
 * Utility class containing utilities to help render things onto the screen
 */
public class RenderUtils {
    /**
     * Draws a sprite from a texture atlas onto the screen.
     *
     * @param sprite the texture atlas sprite to render
     * @param mStack the matrix stack to render into
     * @param x the x coordinate on the screen to render to
     * @param y the y coordinate on the screen to render to
     * @param z the z coordinate or "blit offset" to render to
     * @param width width to render the sprite
     * @param height height to render the sprite
     *
     * @see MiasmaSprite
     */
    public static void drawAtlasSprite(TextureAtlasSprite sprite, MatrixStack mStack, int x, int y, int z, int width, int height) {
        Minecraft.getInstance().getTextureManager().bind(sprite.atlas().location());
        AbstractGui.blit(mStack, x, y, z, 18, 18, sprite);
    }

    /**
     * Draws an entire texture onto the screen.
     *
     * @param texture the resource location of the texture to draw.
     * @param mStack the matrix stack to render into
     * @param x the x coordinate on the screen to render to
     * @param y the y coordinate on the screen to render to
     * @param z the z coordinate or "blit offset" to render to
     * @param width width of the sprite
     * @param height height of the sprite
     *
     * @see MiasmaSprite
     */
    public static void drawTexture(ResourceLocation texture, MatrixStack mStack, int x, int y, int z, int width, int height) {
        drawTexture(texture, mStack, x, y, z, 0, 0, width, height, width, height);
    }

    /**
     * Draws a sprite from a texture onto the screen.
     *
     * @param texture the resource location of the texture to draw.
     * @param mStack the matrix stack to render into
     * @param x the x coordinate on the screen to render to
     * @param y the y coordinate on the screen to render to
     * @param z the z coordinate or "blit offset" to render to
     * @param u x coordinate within the texture of the sprite
     * @param v y coordinate within the texture of the sprite
     * @param width width of the sprite
     * @param height height of the sprite
     * @param fileWidth width of the entire texture file
     * @param fileHeight height of the entire texture file
     *
     * @see MiasmaSprite
     */
    public static void drawTexture(ResourceLocation texture, MatrixStack mStack, int x, int y, int z, int u, int v, int width, int height, int fileWidth, int fileHeight) {
        Minecraft.getInstance().getTextureManager().bind(texture);
        blit(mStack, x, y, z, u, v, width, height, fileWidth, fileHeight);
    }

    /**
     * Blits from the currently bound texture onto the screen.
     * <p>This is {@link AbstractGui#blit(MatrixStack, int, int, int, float, float, int, int, int, int)}, but with named arguments.</p>
     *
     * @param mStack the matrix stack to render into
     * @param x the x coordinate on the screen to render to
     * @param y the y coordinate on the screen to render to
     * @param z the z coordinate or "blit offset" to render to
     * @param u x coordinate within the bound texture to blit from
     * @param v y coordinate within the bound texture to blit from
     * @param width width of the area from the bound texture to blit
     * @param height height of the area from the bound texture to blit
     * @param textureWidth width of the bound texture
     * @param textureHeight height of the bound texture
     */
    private static void blit(MatrixStack mStack, int x, int y, int z, int u, int v, int width, int height, int textureWidth, int textureHeight) {
        AbstractGui.blit(mStack, x, y, z, u, v, width, height, textureWidth, textureHeight);
    }

    /**
     * Sets the current rendering color.
     * @param color the color to set.
     * @see GL11#glColor3f
     */
    public static void setGLColor3(Color color) {
        GL11.glColor3f(color.red, color.green, color.blue);
    }

    /**
     * Sets the current rendering color including alpha.
     * @param color the color to set.
     * @see GL11#glColor4f
     */
    public static void setGLColor4(Color color) {
        GL11.glColor4f(color.red, color.green, color.blue, color.alpha);
    }

    /**
     * Resets the current rendering color to white with full alpha.
     * @see GL11#glColor4f
     */
    public static void resetGLColor() {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
