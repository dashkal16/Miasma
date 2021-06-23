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
package name.dashkal.minecraft.miasma.client.effect;

import com.mojang.blaze3d.matrix.MatrixStack;
import name.dashkal.minecraft.miasma.MiasmaMod;
import name.dashkal.minecraft.miasma.api.IInfection;
import name.dashkal.minecraft.miasma.api.InfectionMode;
import name.dashkal.minecraft.miasma.api.InfectionStage;
import name.dashkal.minecraft.miasma.api.MiasmaAPI;
import name.dashkal.minecraft.miasma.lib.client.render.Color;
import name.dashkal.minecraft.miasma.lib.client.render.Colors;
import name.dashkal.minecraft.miasma.lib.client.render.RenderUtils;
import name.dashkal.minecraft.miasma.common.effect.MiasmaEffect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import org.lwjgl.opengl.GL11;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Special Effect renderer for the Miasma effect.
 * <p>
 *     This class provides the specialized icon tinting and dual timers used when showing the player an active miasma
 *     infection.
 * </p>
 */
public class MiasmaEffectRender {
    private static final Minecraft minecraft = Minecraft.getInstance();
    private static final MiasmaAPI api = MiasmaAPI.getInstance();

    private static final Color DIM_AQUA   = Colors.AQUA  .withSaturation(0.50f).withValue(0.25f);
    private static final Color DIM_RED    = Colors.RED   .withSaturation(0.50f).withValue(0.25f);
    private static final Color DIM_GOLD   = Colors.GOLD  .withSaturation(0.50f).withValue(0.25f);
    private static final Color DIM_YELLOW = Colors.YELLOW.withSaturation(0.50f).withValue(0.25f);

    private static final ResourceLocation miasmaSprite = new ResourceLocation(MiasmaMod.MODID, "mob_effect/miasma_real");

    /**
     * Draws the Miasma potion effect onto the player's inventory when it's active.
     *
     * @param gui a handle to the gui currently rendering
     * @param mStack The MatrixStack to render into
     * @param x the x coordinate to start rendering from
     * @param y the y coordinate to start rendering from
     */
    public static void renderInventoryEffect(DisplayEffectsScreen<?> gui, MatrixStack mStack, int x, int y) {
        // Only render if the player is actually infected with the Miasma
        withInfection(infection -> {
            // Render the Potion Icon
            renderPotionSprite(mStack, x + 6, y + 7, gui.getBlitOffset(), getSpriteTint(infection));

            // Render the effect name and rank
            String effectName = I18n.get(MiasmaEffect.INSTANCE.getDescriptionId());
            String rank = getRankForStage(infection.getStage());
            gui.font.drawShadow(mStack, effectName + ' ' + rank, (float) (x + 28), (float) (y + 6), Colors.WHITE.getColorRGB());

            // Render the cleansing duration
            String cleansingDuration = formatDuration(infection.getGameTicksToCleanseStage(), 4);
            Color cleansingDurationColor = calculateCleansingCountdownColor(infection);
            gui.font.drawShadow(mStack, cleansingDuration, (float) (x + 28), (float) (y + 16), cleansingDurationColor.getColorRGB());

            // Render the infection duration
            String infectionDuration = formatDuration(infection.getGameTicksToIntensifyStage(), 5);
            Color infectionDurationColor = calculateIntensifyingCountdownColor(infection);
            gui.font.drawShadow(mStack, infectionDuration, (float) (x + 88), (float) (y + 16), infectionDurationColor.getColorRGB());
        });
    }

    /**
     * Draws the Miasma potion effect onto the in-game HUD.
     *
     * @param gui a handle to the gui currently rendering
     * @param mStack The MatrixStack
     * @param x      the x coordinate
     * @param y      the y coordinate
     * @param alpha the alpha value, blinks when the potion is about to run out
     */
    public static void renderHUDEffect(AbstractGui gui, MatrixStack mStack, int x, int y, float alpha) {
        // Only render if the player is actually infected with the Miasma
        withInfection(infection ->
            renderPotionSprite(mStack, x + 3, y + 3, gui.getBlitOffset(), getSpriteTint(infection).withAlpha(alpha))
        );
    }

    /** Renders the actual effect sprite. */
    private static void renderPotionSprite(MatrixStack mStack, int x, int y, int blitOffset, Color color) {
        RenderUtils.setGLColor4(color);
        TextureAtlasSprite tx = minecraft.getMobEffectTextures().textureAtlas.getSprite(miasmaSprite);
        RenderUtils.drawAtlasSprite(tx, mStack, x, y, blitOffset, 18, 18);
        RenderUtils.resetGLColor();
    }

    /** Returns the effect rank identifier (I, II, etc), given an infection stage. */
    private static String getRankForStage(InfectionStage stage) {
        switch(stage) {
            case KILLING: return I18n.get("enchantment.level." + 3);
            case HARMING: return I18n.get("enchantment.level." + 2);
            case WARNING:
            default: return "";
        }
    }

    /** Chooses a tinting color for the effect icon, given an active infection. */
    private static Color getSpriteTint(IInfection infection) {
        switch (infection.getMode()) {
            case CLEANSING:
                return Colors.AQUA;
            case PAUSED:
            case INTENSIFYING:
                switch(infection.getStage()) {
                    case KILLING:
                        return Colors.RED;
                    case HARMING:
                        return Colors.GOLD;
                    case WARNING:
                        return Colors.YELLOW;
                }
        }
        return Colors.GREY;
    }

    /** Chooses a color for the cleansing counter of an active infection. */
    private static Color calculateCleansingCountdownColor(IInfection infection) {
        if (infection.getMode() == InfectionMode.CLEANSING) {
            return Colors.AQUA;
        } else {
            return DIM_AQUA;
        }
    }

    /** Chooses a color for the intensifying counter of an active infection. */
    private static Color calculateIntensifyingCountdownColor(IInfection infection) {
        if (infection.getMode() == InfectionMode.INTENSIFYING) {
            switch (infection.getStage()) {
                case KILLING: return Colors.RED;
                case HARMING: return Colors.GOLD;
                case WARNING: return Colors.YELLOW;
            }
        } else {
            switch (infection.getStage()) {
                case KILLING: return DIM_RED;
                case HARMING: return DIM_GOLD;
                case WARNING: return DIM_YELLOW;
            }
        }
        return Colors.GREY;
    }

    /** Formats an infection countdown timer. */
    private static String formatDuration(Optional<Integer> duration, int padSize) {
        return duration.map(d ->
                org.apache.commons.lang3.StringUtils.leftPad(StringUtils.formatTickDuration(d), padSize, ' ')
        ).orElse("**:**");
//        return duration.map(d -> Integer.toString(d)).orElse("*"); // Raw ticks.
    }

    /** Helper function for obtaining the active infection of a player. */
    private static void withInfection(Consumer<IInfection> consumer) {
        Optional.ofNullable(minecraft.player)
                .flatMap(api::getInfection)
                .ifPresent(consumer);
    }
}
