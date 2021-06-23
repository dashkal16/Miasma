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
import net.minecraft.util.ResourceLocation;

/**
 * Sprite implementation to work around not understanding how the Minecraft Texture Atlas stuff works.
 */
public class MiasmaSprite {
    private final ResourceLocation resourceLocation;
    private final int u;
    private final int v;
    private final int width;
    private final int height;
    private final int fileWidth;
    private final int fileHeight;

    /*
     * In TextureStitchEvent.Pre you can ask which texture atlas is being built with event.getMap().getLocation().
     * If it's the map you want to add to, go ahead and call event.addSprite(spriteLocation).
     * To get the sprite back later for blitting, call Minecraft.getInstance().getTextureAtlas(atlasLocation).apply(spriteLocation).
     * That will return a TextureAtlasSprite which is correct for AbstractGui.blit(mStack, x, y, z, spriteDrawWidth, spriteDrawHeight, sprite)
     *
     * Also look into PotionSpriteUploader and SpriteUploader.  Minecraft ties them in as "reload listeners".
     */

    /**
     * Create a new miasma sprite.
     *
     * @param location resource location of the texture file
     * @param u x coordinate within the texture of the sprite
     * @param v y coordinate within the texture of the sprite
     * @param width width of the sprite
     * @param height height of the sprite
     * @param fileWidth width of the entire texture file
     * @param fileHeight height of the entire texture file
     */
    public MiasmaSprite(ResourceLocation location, int u, int v, int width, int height, int fileWidth, int fileHeight) {
        this.resourceLocation = location;
        this.u = u;
        this.v = v;
        this.width = width;
        this.height = height;
        this.fileWidth = fileWidth;
        this.fileHeight = fileHeight;
    }

    /**
     * Create a new miasma sprite from a single sprite texture file.
     *
     * @param location resource location of the texture file
     * @param width width of the sprite
     * @param height height of the sprite
     */
    public MiasmaSprite(ResourceLocation location, int width, int height) {
        this(location, 0, 0, width, height, width, height);
    }

    /**
     * Blits this sprite onto the screen.
     * <p>This method will first bind the texture. Use this when only once from a given backing texture file.</p>
     *
     * @param mStack the matrix stack to render into
     * @param x the x coordinate on the screen to render to
     * @param y the y coordinate on the screen to render to
     * @param z the z coordinate or "blit offset" to render to
     */
    public void draw(MatrixStack mStack, int x, int y, int z) {
        bind();
        blit(mStack, x, y, z);
    }

    /** Binds the rendering engine to this sprite's texture. */
    public void bind() {
        Minecraft.getInstance().getTextureManager().bind(resourceLocation);
    }

    /**
     * Blits this sprite onto the screen.
     *
     * @param mStack the matrix stack to render into
     * @param x the x coordinate on the screen to render to
     * @param y the y coordinate on the screen to render to
     * @param z the z coordinate or "blit offset" to render to
     */
    public void blit(MatrixStack mStack, int x, int y, int z) {
        AbstractGui.blit(mStack, x, y, z, u, v, width, height, fileWidth, fileHeight);
    }

    /** Returns the resource location of the texture file this sprite belongs to. */
    public ResourceLocation getResourceLocation() {
        return resourceLocation;
    }

    /** Returns the x coordinate of this sprite within its texture file. */
    public int getU() {
        return u;
    }

    /** Returns the y coordinate of this sprite within its texture file. */
    public int getV() {
        return v;
    }

    /** Returns the width of this sprite. */
    public int getWidth() {
        return width;
    }

    /** Returns the height of this sprite. */
    public int getHeight() {
        return height;
    }

    /** Returns the width of the texture file this sprite belongs to. */
    public int getFileWidth() {
        return fileWidth;
    }

    /** Returns the height of the texture file this sprite belongs to. */
    public int getFileHeight() {
        return fileHeight;
    }
}
