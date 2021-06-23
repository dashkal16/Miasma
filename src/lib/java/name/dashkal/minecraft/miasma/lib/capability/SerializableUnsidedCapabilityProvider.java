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

import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Capability provider that supports serialization, but ignores the side argument when capabilities are requested.
 */
public class SerializableUnsidedCapabilityProvider implements ICapabilitySerializable<INBT> {
    // Existential type pattern. This allows us to hide irrelevant type information.
    private final Worker<?, ?> worker;

    /**
     * Create a new serializable unsided capability provider.
     *
     * @param capabilitySupplier a supplier that will provide the capability definition.  If this supplier yields null
     *                           at the time a capability is requested, this capability provider will provide
     *                           {@link LazyOptional#empty()}.
     * @param supplier a supplier invoked once to retrieve the capability instance, then the instance will be cached
     *                 for later requests.
     * @param store a function invoked to store the state of the capability instance to nbt
     * @param load a consumer invoked to load the state of the capability from nbt
     */
    public <T, U extends T> SerializableUnsidedCapabilityProvider(Supplier<Capability<T>> capabilitySupplier,
                                                                  NonNullSupplier<U> supplier,
                                                                  Function<U, INBT> store,
                                                                  BiConsumer<U, INBT> load) {
        this.worker = new Worker<>(capabilitySupplier, supplier, store, load);
    }

    /**
     * Create a new serializable unsided capability provider that uses the capability's default
     * {@link Capability.IStorage} to handle serialization and deserialization. {@code null} will be provided where
     * methods require a side.
     *
     * @param capabilitySupplier a supplier that will provide the capability definition.  If this supplier yields null
     *                           at the time a capability is requested, this capability provider will provide
     *                           {@link LazyOptional#empty()}.
     * @param supplier a supplier invoked once to retrieve the capability instance (once retrieved, it will be cached
     *                 and returned for subsequent requests).
     */
    public <T> SerializableUnsidedCapabilityProvider(Supplier<Capability<T>> capabilitySupplier, NonNullSupplier<T> supplier) {
        this(capabilitySupplier, supplier, t -> capabilitySupplier.get().writeNBT(t, null), (t, nbt) -> capabilitySupplier.get().readNBT(t, null, nbt));
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return worker.getCapability(cap, side);
    }

    public void invalidate() {
        worker.invalidate();
    }

    @Nullable
    @Override
    public INBT serializeNBT() {
        return worker.serializeNBT();
    }

    @Override
    public void deserializeNBT(@Nullable INBT nbt) {
        worker.deserializeNBT(nbt);
    }

    private static class Worker<T, U extends T> {
        private final Supplier<Capability<T>> capabilitySupplier;
        private final LazyOptional<U> handler;
        private final Function<U, INBT> store;
        private final BiConsumer<U, INBT> load;

        public Worker(Supplier<Capability<T>> capabilitySupplier, NonNullSupplier<U> supplier, Function<U, INBT> store, BiConsumer<U, INBT> load) {
            this.capabilitySupplier = capabilitySupplier;
            this.handler = LazyOptional.of(supplier);
            this.store = store;
            this.load = load;
        }

        public <V> LazyOptional<V> getCapability(@Nonnull Capability<V> cap, @Nullable Direction side) {
            Capability<T> capability = capabilitySupplier.get();
            if (capability == null) {
                return LazyOptional.empty();
            } else {
                // Casting U back to T.  LazyOptional uses U in covariant position, so the upcast is safe.
                return capability.orEmpty(cap, handler.cast());
            }
        }

        public void invalidate() {
            handler.invalidate();
        }

        @Nullable
        public INBT serializeNBT() {
            return handler.resolve().map(store).orElse(null);
        }

        public void deserializeNBT(@Nullable INBT nbt) {
            handler.ifPresent(t -> load.accept(t, nbt));
        }
    }
}
