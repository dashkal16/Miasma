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
package name.dashkal.minecraft.miasma.client.eventhandlers;

import name.dashkal.minecraft.miasma.api.events.MiasmaEvent;
import name.dashkal.minecraft.miasma.common.effect.MiasmaEffect;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;

import java.util.Optional;

/**
 * Event handlers for responding to miasma events on the client side.
 */
public class ClientMiasmaEventHandlers {
    private static final int VISIBLE_DURATION_LIMIT = 10 * 64 * 20; // 10 minutes

    public static void registerListeners() {
        MinecraftForge.EVENT_BUS.addListener(ClientMiasmaEventHandlers::onMiasmaAppliedEvent);
        MinecraftForge.EVENT_BUS.addListener(ClientMiasmaEventHandlers::onMiasmaInfectionTickEvent);
        MinecraftForge.EVENT_BUS.addListener(ClientMiasmaEventHandlers::onMiasmaStageChangeEvent);
        MinecraftForge.EVENT_BUS.addListener(ClientMiasmaEventHandlers::onMiasmaStageRestartedEvent);
    }

    public static void onMiasmaAppliedEvent(MiasmaEvent.InfectionPostApplyEvent event) {
        updateEffectCounter();
    }

    public static void onMiasmaInfectionTickEvent(MiasmaEvent.InfectionPostPulseEvent event) {
        updateEffectCounter();
    }

    public static void onMiasmaStageChangeEvent(MiasmaEvent.InfectionStageChangeEvent event) {
        updateEffectCounter();
    }

    public static void onMiasmaStageRestartedEvent(MiasmaEvent.InfectionStageRestartedEvent event) {
        updateEffectCounter();
    }

    private static void updateEffectCounter() {
        Optional.ofNullable(Minecraft.getInstance().player)
                .flatMap(p -> Optional.ofNullable(p.getEffect(MiasmaEffect.INSTANCE)))
                .ifPresent(e -> e.setNoCounter(e.getDuration() > VISIBLE_DURATION_LIMIT));
    }
}
