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
package name.dashkal.minecraft.miasma.common.network;

import name.dashkal.minecraft.miasma.MiasmaMod;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

/**
 * Class responsible for owning the Miasma network channel
 */
public class MiasmaChannel {
    // We only allow exactly matching versions
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MiasmaMod.MODID, "main"),
            MiasmaMod::getModVersion,
            v -> v.equals(MiasmaMod.getModVersion()),
            v -> v.equals(MiasmaMod.getModVersion())
    );
    private static final Logger LOGGER = LogManager.getLogger(MiasmaMod.MODID);

    @SuppressWarnings("UnusedAssignment")
    public static void registerPackets() {
        LOGGER.info("registerPackets(): " + MiasmaMod.getModVersion());

        int packetId = 0;
        CHANNEL.registerMessage(
                packetId++,
                MiasmaHandlerSyncPacket.class,
                MiasmaHandlerSyncPacket::encode,
                MiasmaHandlerSyncPacket::decoder,
                MiasmaHandlerSyncPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
        CHANNEL.registerMessage(
                packetId++,
                MiasmaDebugOverlayPacket.class,
                MiasmaDebugOverlayPacket::encode,
                MiasmaDebugOverlayPacket::decoder,
                MiasmaDebugOverlayPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
    }

    /**
     * Sends a packet from the server to the client.
     * @param entity the entity to send the packet to
     * @param packet the packet to send
     */
    public static void sendToPlayerClient(LivingEntity entity, IMiasmaPlayToClientPacket packet) {
        if (!entity.getCommandSenderWorld().isClientSide && entity instanceof ServerPlayerEntity) {
            CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) entity), packet);
        }
    }
}
