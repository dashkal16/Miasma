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
package name.dashkal.minecraft.miasma.api;

import name.dashkal.minecraft.miasma.api.capability.MiasmaModifierUtils;
import net.minecraft.entity.LivingEntity;

import java.util.Optional;

/**
 * Public API for the Miasma mod.
 */
@SuppressWarnings("unused")
public abstract class MiasmaAPI {
    static MiasmaAPI INSTANCE;
    MiasmaAPI() { }

    /**
     * Returns a handle to the singleton API instance.
     */
    public static MiasmaAPI getInstance() {
        return INSTANCE;
    }

    /** Returns the mod id for Miasma. */
    public abstract String getMiasmaModId();

    /**
     * If the entity is infected, returns a snapshot of the infection state.
     *
     * @param entity the entity to check for a miasma infection on.
     * @return an {@link Optional} containing the infection state, if present.
     */
    public abstract Optional<IInfection> getInfection(LivingEntity entity);

    /**
     * Attempts to infect an entity with the miasma infection, starting at the specified infection stage.
     *
     * @param entity the entity to apply the infection to
     * @param stage the stage to start the miasma infection at
     * @return {@code true} if the miasma was applied or {@code false} if the infection was blocked or entity is incapable of being infected.
     */
    public abstract boolean tryApplyInfection(LivingEntity entity, InfectionStage stage);

    /**
     * Causes an active miasma infection to pulse, without affecting the timer.
     * <p>
     *     This requires an active infection on the entity.
     * </p>
     * @return {@code true} if the miasma was pulsed {@code false} if not.
     */
    public abstract boolean tryMiasmaPulse(LivingEntity entity);

    /**
     * Removes the miasma from the target entity.
     */
    public abstract void removeInfection(LivingEntity entity);

    /**
     * Returns a handle to the {@link MiasmaModifierUtils} api class.
     */
    public abstract MiasmaModifierUtils getModifierUtils();
}
