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
package name.dashkal.minecraft.miasma.lib.capability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.Message;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * Composite capability provider that allows multiple capabilities to be attached to the same thing.
 * <p>
 *     Supports both sided capabilities and serialization/deserialization.
 * </p>
 */
public class CompositeCapabilityProvider implements ICapabilitySerializable<CompoundNBT> {
    private static final Logger LOGGER = LogManager.getLogger();

    private final Map<String, ICapabilityProvider> providers;

    /**
     * Create a new composite capability provider.
     * @param providers the providers to compose. The keys are used as the NBT tag keys when serializing to NBT.
     */
    public CompositeCapabilityProvider(Map<String, ICapabilityProvider> providers) {
        this.providers = new HashMap<>(providers);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        // Return the first one that matches
        for (ICapabilityProvider provider : providers.values()) {
            LazyOptional<T> handler = provider.getCapability(cap, side);
            if (handler.isPresent()) {
                return handler;
            }
        }
        // Nothing matched, return empty.
        return LazyOptional.empty();
    }

    @Override
    public CompoundNBT serializeNBT() {
        // Let each provider serialize
        CompoundNBT tag = new CompoundNBT();
        for (Map.Entry<String, ICapabilityProvider> entry : providers.entrySet()) {
            ICapabilityProvider provider = entry.getValue();
            if (provider instanceof INBTSerializable) {
                tag.put(entry.getKey(), ((INBTSerializable<?>) provider).serializeNBT());
            }
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundNBT tag) {
        // Let each provider deserialize its entry
        for (Map.Entry<String, ICapabilityProvider> entry : providers.entrySet()) {
            ICapabilityProvider provider = entry.getValue();
            if (tag.contains(entry.getKey()) && provider instanceof INBTSerializable) {
                try {
                    // We are passing back exactly what it gave us, so under normal circumstances, the type will match
                    //noinspection unchecked
                    ((INBTSerializable<INBT>) provider).deserializeNBT(tag.get(entry.getKey()));
                } catch (ClassCastException ex) {
                    // Nope, the type didn't match. We have data corruption.

                    // log4j 2.11.2 doesn't support both exceptions and format arguments
                    Message msg = LOGGER.getMessageFactory().newMessage(
                            "Potential data corruption. Got an unexpected ClassCastException when deserializing for {}",
                            provider.getClass().getName()
                    );
                    LOGGER.error(msg, ex);

                    // This is seriously bad. Propagate the exception.
                    throw ex;
                }
            }
        }
    }
}
