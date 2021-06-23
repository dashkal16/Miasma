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
import name.dashkal.minecraft.miasma.common.capability.MiasmaModifierCapability;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.NonNullSupplier;

import javax.annotation.Nonnull;

/**
 * Implementation class for the Miasma Gear Utils API class.
 * <p>
 *     THIS CLASS SHOULD NOT BE ACCESSED BY API USERS!
 * </p>
 * <p>
 *     Provides API classes access into the Miasma mod code.
 * </p>
 */
public class MiasmaModifierUtilsImpl extends MiasmaModifierUtils {
    @Override
    public Capability<IMiasmaModifier> getCapability() {
        return MiasmaModifierCapability.CAPABILITY;
    }

    @Nonnull
    @Override
    public ICapabilityProvider fromSupplier(NonNullSupplier<IMiasmaModifier> supplier) {
        return MiasmaModifierCapability.fromSupplier(supplier);
    }

    @Nonnull
    @Override
    public ICapabilityProvider fromModifiers(MiasmaPropertyModifiers modifiers) {
        return MiasmaModifierCapability.fromModifiers(modifiers);
    }
}
