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
package name.dashkal.minecraft.miasma.common.event;

import name.dashkal.minecraft.miasma.api.capability.IMiasmaModifier;
import name.dashkal.minecraft.miasma.common.capability.IMiasmaHandler;
import name.dashkal.minecraft.miasma.common.capability.MiasmaHandlerCapability;
import name.dashkal.minecraft.miasma.common.logic.MiasmaLogic;
import name.dashkal.minecraft.miasma.common.network.MiasmaChannel;
import name.dashkal.minecraft.miasma.common.network.MiasmaHandlerSyncPacket;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

/**
 * Event handlers for interacting with the Miasma infection.
 * <p>Responsible for dispatching to the {@link IMiasmaHandler} and
 * {@link IMiasmaModifier} capability instances.</p>
 */
public class MiasmaEventHandlers {
    public static void registerHandlers() {
        MinecraftForge.EVENT_BUS.addListener(MiasmaEventHandlers::onLivingUpdateEvent);
        MinecraftForge.EVENT_BUS.addListener(MiasmaEventHandlers::onPotionRemoveEvent);
        MinecraftForge.EVENT_BUS.addListener(MiasmaEventHandlers::onPlayerLoggedIn);
        MinecraftForge.EVENT_BUS.addListener(MiasmaEventHandlers::onPlayerClone);
    }

    public static void onLivingUpdateEvent(LivingEvent.LivingUpdateEvent event) {
        // Be very careful about CPU in this event! It's fired for every living entity every tick.
        LivingEntity entity = event.getEntityLiving();
        MiasmaHandlerCapability.ifPresent(entity, handler ->
            MiasmaLogic.gameTick(entity, handler)
        );
    }

    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        PlayerEntity player = event.getPlayer();
        MiasmaHandlerCapability.ifPresent(player, miasmaHandler ->
                MiasmaChannel.sendToPlayerClient(event.getEntityLiving(), new MiasmaHandlerSyncPacket(miasmaHandler))
        );
    }

    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) {
            // Travel from the end.  Preserve the capability data.
            LivingEntity oldP = event.getOriginal();
            LivingEntity newP = event.getEntityLiving();

            MiasmaHandlerCapability.ifPresent(oldP, oldHandler ->
                MiasmaHandlerCapability.ifPresent(newP, newHandler ->
                    newHandler.copyFrom(oldHandler)
                )
            );
        }
    }

    public static void onPotionRemoveEvent(PotionEvent.PotionRemoveEvent event) {
        MiasmaLogic.onEffectRemoved(event.getEntityLiving());
    }
}
