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
package name.dashkal.minecraft.miasma.client;

import name.dashkal.minecraft.miasma.common.capability.MiasmaHandlerCapability;
import name.dashkal.minecraft.miasma.common.logic.MiasmaLogic;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;

import java.util.Objects;

/**
 * Class responsible for providing a safe entry point from common code (like packet handlers).
 */
public class MiasmaClient {
    public static final MiasmaClient INSTANCE = new MiasmaClient();
    private boolean enableDebugOverlay;

    /** Returns {@code true} if the miasma debug overlay is enabled. */
    public boolean isEnableDebugOverlay() {
        return enableDebugOverlay;
    }

    /** Sets the status of the miasma debug overlay. */
    public void setEnableDebugOverlay(boolean enableDebugOverlay) {
        this.enableDebugOverlay = enableDebugOverlay;
    }

    /** Synchronizes the local player's Miasma handler NBT from the server. */
    public void syncMiasmaData(CompoundNBT nbt) {
        LivingEntity player = Minecraft.getInstance().player;
        Objects.requireNonNull(player);
        MiasmaHandlerCapability.ifPresent(player, (miasmaHandler -> {
            miasmaHandler.deserializeNBT(nbt);
            MiasmaLogic.updateEffect(player, miasmaHandler);
        }));
    }
}
