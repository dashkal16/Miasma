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
package name.dashkal.minecraft.miasma.api.property;

import java.util.*;

/**
 * An immutable collection of modifications to properties of a Miasma infection.
 * <p>
 *     New instances can be created by use of {@link MiasmaPropertyModifiers.Builder}.
 * </p>
 */
public class MiasmaPropertyModifiers {
    private static final MiasmaPropertyModifiers EMPTY = new MiasmaPropertyModifiers(new HashMap<>());

    private final Map<MiasmaPropertyModifierType<?>, Modifier<?>> modifiers;

    private MiasmaPropertyModifiers(Map<MiasmaPropertyModifierType<?>, Modifier<?>> modifiers) {
        this.modifiers = Collections.unmodifiableMap(modifiers);
    }

    /** Returns a handle to the singleton empty {@link MiasmaPropertyModifiers} instance. */
    public static MiasmaPropertyModifiers empty() {
        return EMPTY;
    }

    /**
     * Returns the value for the modifier of the given type.  If no such modifier has been added, this returns the
     * zero value for that type.
     */
    public <T> T getModifier(MiasmaPropertyModifierType<T> type) {
        if (modifiers.containsKey(type)) {
            return modifiers.get(type).castOrZero(type).value;
        } else {
            return type.getZero();
        }
    }

    /**
     * A mutable builder used to construct  an {@link MiasmaPropertyModifiers} instance.
     */
    @SuppressWarnings({"UnusedReturnValue", "unused"})
    public static class Builder {
        private final Map<MiasmaPropertyModifierType<?>, Modifier<?>> modifiers;

        /** Constructs a new empty {@link MiasmaPropertyModifiers.Builder} */
        public Builder() {
            modifiers = new HashMap<>();
        }

        /** Constructs a {@link MiasmaPropertyModifiers.Builder} that starts with the provided modifiers. */
        public Builder(MiasmaPropertyModifiers modifiers) {
            this.modifiers = new HashMap<>(modifiers.modifiers);
        }

        /** Adds a modifier to this builder. */
        public <T> Builder addModifier(MiasmaPropertyModifierType<T> type, T value) {
            Modifier<T> newMod = new Modifier<>(type, value);
            if (modifiers.containsKey(type)) {
                modifiers.put(type, modifiers.get(type).tryMerge(newMod));
            } else {
                modifiers.put(type, newMod);
            }
            return this;
        }

        /** Adds all modifiers from another builder into this one. */
        public Builder addAll(Builder other) {
            for (Modifier<?> newMod : other.modifiers.values()) {
                if (modifiers.containsKey(newMod.type)) {
                    modifiers.put(newMod.type, modifiers.get(newMod.type).tryMerge(newMod));
                } else {
                    modifiers.put(newMod.type, newMod);
                }
            }
            return this;
        }

        /** Adds all modifiers from the provided {@link MiasmaPropertyModifiers} to this builder. */
        public Builder addAll(MiasmaPropertyModifiers other) {
            for (Modifier<?> newMod : other.modifiers.values()) {
                if (modifiers.containsKey(newMod.type)) {
                    modifiers.put(newMod.type, modifiers.get(newMod.type).tryMerge(newMod));
                } else {
                    modifiers.put(newMod.type, newMod);
                }
            }
            return this;
        }

        /**
         * Builds an immutable {@link MiasmaPropertyModifiers} instance. This builder is still valid after calling this
         * method.  Repeated invocations will make distinct {@link MiasmaPropertyModifiers} instances.
         */
        public MiasmaPropertyModifiers build() {
            return new MiasmaPropertyModifiers(new HashMap<>(modifiers));
        }
    }

    /** Helper class to hold onto the pair of a modifier type and a value of that type. */
    private static class Modifier<T> {
        public final MiasmaPropertyModifierType<T> type;
        public final T value;

        public Modifier(MiasmaPropertyModifierType<T> type, T value) {
            this.type = type;
            this.value = value;
        }

        public <U> Modifier<T> tryMerge(Modifier<U> other) {
            return mergeValue(other.castOrZero(type).value);
        }

        private Modifier<T> mergeValue(T value) {
            return new Modifier<>(type, type.merge(this.value, value));
        }

        private <U> Modifier<U> castOrZero(MiasmaPropertyModifierType<U> expectedType) {
            return type.checkedCast(expectedType, value)
                    .map(t -> new Modifier<>(expectedType, t))
                    .orElseGet(() -> new Modifier<>(expectedType, expectedType.getZero()));
        }
    }
}
