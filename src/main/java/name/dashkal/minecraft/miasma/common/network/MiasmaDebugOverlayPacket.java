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

import name.dashkal.minecraft.miasma.client.MiasmaClient;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class MiasmaDebugOverlayPacket implements IMiasmaPlayToClientPacket {
    private final Command command;

    public enum Command {
        SHOW,
        HIDE,
        TOGGLE
    }

    public MiasmaDebugOverlayPacket(Command command) {
        this.command = command;
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeEnum(command);
    }

    public static MiasmaDebugOverlayPacket decoder(PacketBuffer buffer) {
        return new MiasmaDebugOverlayPacket(buffer.readEnum(Command.class));
    }

    @Override
    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            switch (command) {
                case SHOW:
                    MiasmaClient.INSTANCE.setEnableDebugOverlay(true);
                    break;
                case HIDE:
                    MiasmaClient.INSTANCE.setEnableDebugOverlay(false);
                    break;
                case TOGGLE:
                    MiasmaClient.INSTANCE.setEnableDebugOverlay(!MiasmaClient.INSTANCE.isEnableDebugOverlay());
                    break;
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
