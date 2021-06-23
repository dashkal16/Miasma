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
package name.dashkal.minecraft.miasma.common.effect;

import com.mojang.blaze3d.matrix.MatrixStack;
import name.dashkal.minecraft.miasma.MiasmaMod;
import name.dashkal.minecraft.miasma.api.InfectionStage;
import name.dashkal.minecraft.miasma.client.effect.MiasmaEffectRender;
import name.dashkal.minecraft.miasma.common.capability.MiasmaHandlerCapability;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation class for the Miasma Potion Effect.
 * <p>
 *     Note: This effect has no logic.  It exists to provide visual feedback when entities are infected.
 * </p>
 */
public class MiasmaEffect extends Effect {
    /** Handle to the singleton instance of this effect. */
    public static final Effect INSTANCE = new MiasmaEffect();

    private MiasmaEffect() {
        // Color is dark orange
        super(EffectType.HARMFUL, 0x8c3300);
        setRegistryName(new ResourceLocation(MiasmaMod.MODID, "miasma"));
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // Check if we need to clean up once in awhile.
        return duration % (20 * 5) == 1;
    }

    @Override
    public void applyEffectTick(@Nonnull LivingEntity entity, int amplifier) {
        // Clean off the effect if it's somehow present without an infection.
        if (!MiasmaHandlerCapability.withCapability(entity, h -> h.getInfection().isPresent()).orElse(false)) {
            entity.removeEffect(this);
        }
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        // Miasma may not be cured by a milk bucket.
        return new ArrayList<>();
    }

    /**
     * Creates a new effect instance from this effect.
     *
     * @param stage the stage of the miasma.
     */
    public static EffectInstance createEffectInstance(InfectionStage stage) {
        return new EffectInstance(INSTANCE, Integer.MAX_VALUE, stage.ordinal());
    }

    // Rendering

    @Override
    public boolean shouldRenderInvText(EffectInstance effect) {
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void renderInventoryEffect(EffectInstance effect, DisplayEffectsScreen<?> gui, MatrixStack mStack, int x, int y, float z) {
        MiasmaEffectRender.renderInventoryEffect(gui, mStack, x, y);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void renderHUDEffect(EffectInstance effect, AbstractGui gui, MatrixStack mStack, int x, int y, float z, float alpha) {
        MiasmaEffectRender.renderHUDEffect(gui, mStack, x, y, alpha);
    }
}
