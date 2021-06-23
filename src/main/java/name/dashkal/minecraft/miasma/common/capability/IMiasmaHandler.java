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
package name.dashkal.minecraft.miasma.common.capability;

import name.dashkal.minecraft.miasma.api.InfectionMode;
import name.dashkal.minecraft.miasma.api.InfectionStage;
import name.dashkal.minecraft.miasma.api.property.MiasmaPropertyModifiers;
import name.dashkal.minecraft.miasma.common.logic.Infection;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.IFormattableTextComponent;

import java.util.List;
import java.util.Optional;

/**
 * Capability interface representing a LivingEntity that is susceptible to miasma.
 * <p>
 *     This capability is <strong>not</strong> intended to be implemented by other mods.
 * </p>
 *
 * @see MiasmaHandler
 */
public interface IMiasmaHandler {
    /**
     * Returns {@code true} if it's time to attempt an infection.
     */
    boolean isInfectionAttemptTick();

    /**
     * Returns the state of the active infection, if present.
     */
    Optional<Infection> getInfection();

    /**
     * Begins an infection overriding any existing one.
     *
     * @param stage the stage to set the infection to
     * @param mode the mode to set the infection to
     * @param modifiers the initial modifiers to apply
     * @return the new infection
     */
    Infection applyInfection(InfectionStage stage, InfectionMode mode, MiasmaPropertyModifiers modifiers);

    /**
     * Removes the infection, if present.
     */
    void removeInfection();

    /**
     * Builds and returns a debug report of the current state.
     */
    List<IFormattableTextComponent> getDebugReport();

    /**
     * Copies all state from the other handler into this one.
     */
    void copyFrom(IMiasmaHandler other);

    /**
     * Serializes the state of this miasma handler to NBT.
     */
    CompoundNBT serializeNBT();

    /**
     * Loads the state of this miasma handler from NBT.
     */
    void deserializeNBT(CompoundNBT nbt);
}
