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

import name.dashkal.minecraft.miasma.api.IInfection;
import name.dashkal.minecraft.miasma.api.InfectionStage;
import name.dashkal.minecraft.miasma.api.capability.IMiasmaModifier;
import name.dashkal.minecraft.miasma.api.property.MiasmaPropertyModifiers;
import net.minecraft.entity.LivingEntity;

import javax.annotation.Nonnull;

/**
 * Implementation of {@link IMiasmaModifier} that only modifies infection properties.
 */
public class PropertyOnlyMiasmaModifier implements IMiasmaModifier {
    private final MiasmaPropertyModifiers modifiers;

    /**
     * Creates a new property-only miasma modifier.
     * @param propertyModifiers the property modifiers this implementation will apply.
     */
    public PropertyOnlyMiasmaModifier(MiasmaPropertyModifiers propertyModifiers) {
        this.modifiers = propertyModifiers;
    }

    @Override
    public boolean checkApply(LivingEntity entity, InfectionStage stage, boolean forced) {
        return true;
    }

    @Override
    public boolean checkPulse(LivingEntity entity, IInfection infection) {
        return true;
    }

    @Override
    public boolean checkKill(LivingEntity entity, IInfection infection) {
        return true;
    }

    @Nonnull
    @Override
    public MiasmaPropertyModifiers getPropertyModifiers() {
        return modifiers;
    }
}
