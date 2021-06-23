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
package name.dashkal.minecraft.miasma.api.capability;

import name.dashkal.minecraft.miasma.api.property.MiasmaPropertyModifiers;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.NonNullSupplier;

import javax.annotation.Nonnull;

/**
 * API class for working with {@link IMiasmaModifier} capabilities.
 * <p>
 *     Note: These functions are also available as static functions on {@link IMiasmaModifier}.
 * </p>
 */
public abstract class MiasmaModifierUtils {
    MiasmaModifierUtils() {}

    /** Returns the capability definition for this capability. */
    public abstract Capability<IMiasmaModifier> getCapability();

    /**
     * Returns a capability provider for this capability interface from the given supplier.
     */
    @Nonnull
    public abstract ICapabilityProvider fromSupplier(NonNullSupplier<IMiasmaModifier> supplier);

    /**
     * Returns a capability provider for this capability interface that allows all events and modifies the miasma
     * infection with the given modifiers.
     */
    @Nonnull
    public abstract ICapabilityProvider fromModifiers(MiasmaPropertyModifiers modifiers);
}
