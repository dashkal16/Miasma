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
import name.dashkal.minecraft.miasma.common.capability.IMiasmaHandler;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class MiasmaHandlerSyncPacket implements IMiasmaPlayToClientPacket {
    private final CompoundNBT nbt;

    public MiasmaHandlerSyncPacket(IMiasmaHandler miasmaHandler) {
        this.nbt = miasmaHandler.serializeNBT();
    }

    private MiasmaHandlerSyncPacket(CompoundNBT nbt) {
        this.nbt = nbt;
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeNbt(nbt);
    }

    public static MiasmaHandlerSyncPacket decoder(PacketBuffer buffer) {
        return new MiasmaHandlerSyncPacket(buffer.readNbt());
    }

    @Override
    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
            MiasmaClient.INSTANCE.syncMiasmaData(nbt)
        );
        ctx.get().setPacketHandled(true);
    }
}
