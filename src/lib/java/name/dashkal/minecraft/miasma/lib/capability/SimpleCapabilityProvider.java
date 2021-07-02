/*
 * Miasma Minecraft Mod
 * Copyright © 2021 Dashkal
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the “Software”), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package name.dashkal.minecraft.miasma.lib.capability;

import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * A simple capability provider that does not handle NBT or differentiate by side.
 */
public class SimpleCapabilityProvider<T> implements ICapabilityProvider {
    private final Supplier<Capability<T>> capabilitySupplier;
    private final LazyOptional<T> handler;

    /**
     * Create a new simple capability provider.
     *
     * @param capabilitySupplier a supplier that will provide the capability definition.  If this supplier yields null
     *                           at the time a capability is requested, this capability provider will provide
     *                           {@link LazyOptional#empty()}.
     * @param supplier a supplier that will be called once to obtain the capability instance
     * @param <U> the specific type of the capability instance
     */
    public <U extends T> SimpleCapabilityProvider(@Nonnull Supplier<Capability<T>> capabilitySupplier, @Nonnull NonNullSupplier<U> supplier) {
        this.capabilitySupplier = capabilitySupplier;
        // Casting U back to T.  LazyOptional uses U in covariant position, so the upcast is safe.
        this.handler = LazyOptional.of(supplier).cast();
    }

    /**
     * Retrieves the handler for the capability held by this provider.
     *
     * @param cap The capability to check
     * @param side ignored
     * @return The requested an optional holding the requested capability.
     */
    @Nonnull
    @Override
    public <U> LazyOptional<U> getCapability(@Nonnull Capability<U> cap, @Nullable Direction side) {
        Capability<T> capability = capabilitySupplier.get();
        if (capability == null) {
            return LazyOptional.empty();
        } else {
            return capability.orEmpty(cap, handler);
        }
    }

    /**
     * Invalidates this capability provider.
     */
    public void invalidate() {
        handler.invalidate();
    }
}
