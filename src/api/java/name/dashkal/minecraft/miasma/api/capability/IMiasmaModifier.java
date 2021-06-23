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
import name.dashkal.minecraft.miasma.api.MiasmaAPI;
import name.dashkal.minecraft.miasma.api.imc.MiasmaModifierLocator;
import name.dashkal.minecraft.miasma.api.property.MiasmaPropertyModifiers;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.NonNullSupplier;

import javax.annotation.Nonnull;

/**
 * Interface for an entity-specific modifier of miasma infection.
 * <p>
 *     Implementations of this interface are able to block actions or tweak properties of a miasma infection.<br/>
 *     It can be used as a capability interface or directly.
 * </p>
 * <p>
 *     This interface may be implemented as an item specific alternative to responding to
 *     {@link name.dashkal.minecraft.miasma.api.events.MiasmaEvent} events. Implementations will be consulted
 *     <em>before</em> firing an event.
 * </p>
 * <p>
 *     When used as an item capability, certain items are automatically checked and will be applied if present.<br/>
 *     Any item implementing this capability that is held in the main hand, the off hand, or own in any of the four
 *     armor slots will be considered "active" and will be consulted for infection actions.<br/>
 *     If the Curios mod is installed, the curio slots are checked as well.
 * </p>
 * <p>
 *     Note: The default implementation for this capability does nothing.
 * </p>
 * <p>
 *     To use this interface in a non-capability context, or to have Miasma search for items in other places,
 *     {@link MiasmaModifierLocator}s may be registered during the
 *     {@link net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent} mod lifecycle event.
 * </p>
 * @see MiasmaModifierUtils
 */
public interface IMiasmaModifier {
    /**
     * Called when trying to apply the miasma infection to the protected entity.
     *
     * @param entity the entity the effect is being applied to
     * @param stage the stage of the Miasma infection
     * @param forced if {@code true}, the infection is being force-applied by administrative active and returning
     *               {@code false} will not block it.
     * @return {@code true} if the effect should be applied
     */
    boolean checkApply(@Nonnull LivingEntity entity, @Nonnull InfectionStage stage, boolean forced);

    /**
     * Called when the miasma infection pulses.
     *
     * @param entity the entity the effect is being applied to
     * @param infection a view of the active infection
     * @return {@code true} if the tick should be allowed to run
     */
    boolean checkPulse(@Nonnull LivingEntity entity, @Nonnull IInfection infection);

    /**
     * Called when the {@link InfectionStage#KILLING} stage infection tries to kill the entity.
     * <p>
     *     Returning {@code false} will cause the timer to be reset to its full duration.
     * </p>
     *
     * @param entity the entity the effect is being applied to
     * @param infection a view of the active infection
     * @return {@code true} if the kill should be allowed
     */
    boolean checkKill(@Nonnull LivingEntity entity, @Nonnull IInfection infection);

    /**
     * Returns a collection of property modifiers to apply to an active miasma infection on the entity.
     */
    @Nonnull
    MiasmaPropertyModifiers getPropertyModifiers();

    /** Returns the capability definition for this capability. */
    static Capability<IMiasmaModifier> getCapability() {
        return MiasmaAPI.getInstance().getModifierUtils().getCapability();
    }

    /**
     * Returns a capability provider for this capability interface from the given supplier.
     */
    @Nonnull
    static ICapabilityProvider fromSupplier(NonNullSupplier<IMiasmaModifier> supplier) {
        return MiasmaAPI.getInstance().getModifierUtils().fromSupplier(supplier);
    }

    /**
     * Returns a capability provider for this capability interface that allows all events and modifies the miasma
     * infection with the given modifiers.
     */
    @Nonnull
    static ICapabilityProvider fromModifiers(MiasmaPropertyModifiers modifiers)  {
        return MiasmaAPI.getInstance().getModifierUtils().fromModifiers(modifiers);
    }
}
