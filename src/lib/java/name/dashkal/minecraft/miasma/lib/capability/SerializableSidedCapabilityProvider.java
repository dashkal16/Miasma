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

import name.dashkal.minecraft.miasma.lib.TriConsumer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * Capability provider that supports both serialization and returning different capability instances from different sides.
 */
public class SerializableSidedCapabilityProvider implements ICapabilitySerializable<INBT> {
    // Existential type pattern. This allows us to hide irrelevant type information.
    private final Worker<?, ?> worker;

    /**
     * Create a new serializable sided capability provider.
     *
     * @param capabilitySupplier a supplier that will provide the capability definition.  If this supplier yields null
     *                           at the time a capability is requested, this capability provider will provide
     *                           {@link LazyOptional#empty()}.
     * @param supplierMap a map associating {@link Direction}s to suppliers.  For each side, the supplier will be
     *                    invoked once to retrieve the capability instance, then the instance will be cached for later
     *                    requests.
     * @param store a function invoked to store the state of the capability instance to nbt
     * @param load a consumer invoked to load the state of the capability from nbt
     */
    public <T, U extends T> SerializableSidedCapabilityProvider(Supplier<Capability<T>> capabilitySupplier,
                                                                Map<Direction, NonNullSupplier<U>> supplierMap,
                                                                BiFunction<U, Direction, INBT> store,
                                                                TriConsumer<U, Direction, INBT> load) {
        this.worker = new Worker<>(capabilitySupplier, supplierMap, store, load);
    }

    /**
     * Create a new serializable sided capability provider that uses the capability's default
     * {@link Capability.IStorage} to handle serialization and deserialization.
     *
     * @param capabilitySupplier a supplier that will provide the capability definition.  If this supplier yields null
     *                           at the time a capability is requested, this capability provider will provide
     *                           {@link LazyOptional#empty()}.
     * @param supplierMap a map associating {@link Direction}s to suppliers.  For each side, the supplier will be
     *                    invoked once to retrieve the capability instance, then the instance will be cached for later
     *                    requests.
     */
    public <T> SerializableSidedCapabilityProvider(Supplier<Capability<T>> capabilitySupplier,
                                                   Map<Direction, NonNullSupplier<T>> supplierMap) {
        this(capabilitySupplier, supplierMap, (u, d) -> capabilitySupplier.get().writeNBT(u, d), (u, d, n) -> capabilitySupplier.get().readNBT(u, d, n));
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return worker.getCapability(cap, side);
    }

    public void invalidate() {
        worker.invalidate();
    }

    @Override
    public INBT serializeNBT() {
        return worker.serializeNBT();
    }

    @Override
    public void deserializeNBT(INBT nbt) {
        worker.deserializeNBT(nbt);
    }

    private static class Worker<T, U extends T> {
        private final Supplier<Capability<T>> capabilitySupplier;
        private final Map<Direction, LazyOptional<U>> handlerMap;
        private final BiFunction<U, Direction, INBT> store;
        private final TriConsumer<U, Direction, INBT> load;

        public Worker(Supplier<Capability<T>> capabilitySupplier,
                      Map<Direction, NonNullSupplier<U>> supplierMap,
                      BiFunction<U, Direction, INBT> store,
                      TriConsumer<U, Direction, INBT> load) {
            this.capabilitySupplier = capabilitySupplier;
            this.handlerMap = new HashMap<>();
            for (Direction d : supplierMap.keySet()) {
                handlerMap.put(d, LazyOptional.of(supplierMap.get(d)));
            }
            this.store = store;
            this.load = load;
        }

        public <V> LazyOptional<V> getCapability(@Nonnull Capability<V> cap, @Nullable Direction side) {
            Capability<T> capability = capabilitySupplier.get();
            if (capability == null) {
                return LazyOptional.empty();
            } else {
                // Casting U back to T.  LazyOptional uses U in covariant position, so the upcast is safe.
                return capability.orEmpty(cap, handlerMap.get(side).cast());
            }
        }

        public void invalidate() {
            handlerMap.values().forEach(LazyOptional::invalidate);
        }

        public INBT serializeNBT() {
            CompoundNBT nbt = new CompoundNBT();
            for (Direction d : handlerMap.keySet()) {
                if (handlerMap.containsKey(d)) {
                    handlerMap.get(d).ifPresent( t -> {
                        INBT sideNbt = store.apply(t, d);
                        if (sideNbt != null) {
                            nbt.put(d.getName(), sideNbt);
                        }
                    });
                }
            }
            return nbt;
        }

        public void deserializeNBT(INBT nbt) {
            if (nbt instanceof CompoundNBT) {
                CompoundNBT tag = (CompoundNBT) nbt;
                for (Direction d : handlerMap.keySet()) {
                    if (tag.contains(d.getName())) {
                        handlerMap.get(d).ifPresent(t ->
                            load.accept(t, d, tag.get(d.getName()))
                        );
                    }
                }
            }
        }
    }
}
