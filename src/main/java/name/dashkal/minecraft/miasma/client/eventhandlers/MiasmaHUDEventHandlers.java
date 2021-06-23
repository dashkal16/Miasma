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

import name.dashkal.minecraft.miasma.client.MiasmaClient;
import name.dashkal.minecraft.miasma.client.gui.MiasmaDebugHUD;
import name.dashkal.minecraft.miasma.common.capability.MiasmaHandlerCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;

/**
 * Event handler responsible for ensuring HUDs display when required.
 */
public class MiasmaHUDEventHandlers {
    private static final Minecraft minecraft = Minecraft.getInstance();
    private static final MiasmaDebugHUD MIASMA_DEBUG_HUD = new MiasmaDebugHUD();

    public static void registerListeners() {
        MinecraftForge.EVENT_BUS.addListener(MiasmaHUDEventHandlers::renderDebugHUD);
    }

    public static void renderDebugHUD(RenderGameOverlayEvent.Post event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.ALL && minecraft.player != null && MiasmaClient.INSTANCE.isEnableDebugOverlay()) {
            PlayerEntity player = minecraft.player;
            MiasmaHandlerCapability.ifPresent(player, h -> MIASMA_DEBUG_HUD.render(event.getMatrixStack(), player, h));
        }
    }
}
