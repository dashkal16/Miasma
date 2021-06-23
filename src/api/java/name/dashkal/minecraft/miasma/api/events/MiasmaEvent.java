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
package name.dashkal.minecraft.miasma.api.events;

import name.dashkal.minecraft.miasma.api.IInfection;
import name.dashkal.minecraft.miasma.api.InfectionMode;
import name.dashkal.minecraft.miasma.api.InfectionStage;
import name.dashkal.minecraft.miasma.api.capability.IMiasmaModifier;
import name.dashkal.minecraft.miasma.api.property.MiasmaPropertyModifierType;
import name.dashkal.minecraft.miasma.api.property.MiasmaPropertyModifiers;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.Cancelable;

/**
 * Superclass for all events relating to the Miasma infection.
 * <p>
 *     Mods may listen to these events to be informed of changes to a Miasma infection.
 * </p>
 * <p>
 *     Some events are {@code Cancellable}.  Canceling such an event blocks the action from occurring.  This may be used
 *     to implement protection mechanics.
 * </p>
 * <p>
 *     There is also a gear specific method of responding to miasma infection events available by attaching the
 *     {@link IMiasmaModifier} capability to an item.  Implementations of
 *     that capability will be consulted <em>before</em> firing a {@link MiasmaEvent}.
 * </p>
 * <p>
 *     All of these events are dispatched on the {@link net.minecraftforge.common.MinecraftForge#EVENT_BUS} bus on both
 *     the client and server.
 * </p>
 */
@SuppressWarnings("unused")
public abstract class MiasmaEvent extends LivingEvent {
    MiasmaEvent(LivingEntity entity) {
        super(entity);
    }

    /**
     * Returns the entity the miasma event applies to.
     */
    @Override
    public LivingEntity getEntityLiving() {
        return super.getEntityLiving();
    }

    /**
     * Event dispatched when an entity is to be infected by the miasma.
     * <p>
     *     Handlers may apply infection modifiers through use of {@link #addModifier(MiasmaPropertyModifierType, Object)}.
     * </p>
     * <p>
     *     This event is {@link Cancelable}.<br/>
     *     If this event is canceled, generally the miasma infection will not be applied.</br>
     *     Note that if {@link #isForced()} returns {@code true}, the application is being forcibly applied and
     *     cancellation will not stop it.
     * </p>
     * <p>
     *     If the application was caused by a forcing function, such as via an admin command, the infection will be
     *     applied even if this event is cancelled.
     * </p>
     * </p>
     */
    @Cancelable
    public static class InfectionPreApplyEvent extends MiasmaEvent {
        private final InfectionStage stage;
        private final boolean forced;
        private final MiasmaPropertyModifiers.Builder modifiersBuilder;

        /**
         * Creates a new miasma pre-application event.
         *
         * @param entity the entity to be infected
         * @param stage the stage the infection is being applied at
         * @param forced if {@code true}, this application is being forced and event cancellation will be ignored.
         * @param modifiersBuilder a modifiers builder to use to collect modifiers from event handlers
         */
        public InfectionPreApplyEvent(LivingEntity entity, InfectionStage stage, boolean forced, MiasmaPropertyModifiers.Builder modifiersBuilder) {
            super(entity);
            this.stage = stage;
            this.forced = false;
            this.modifiersBuilder = modifiersBuilder;
        }

        /** Returns the stage the infection is being applied at. */
        public InfectionStage getStage() {
            return stage;
        }

        /**
         * Returns {@code true} if this infection is being force.<br/>
         * In this case, cancellation of the event will have no effect.
         */
        public boolean isForced() {
            return forced;
        }

        /**
         * Adds a modifier to apply to the infection.
         * @param type the type of modifier to apply
         * @param value the modifier value to apply
         * @param <T> the type of the modifier value
         */
        public <T> void addModifier(MiasmaPropertyModifierType<T> type, T value) {
            modifiersBuilder.addModifier(type, value);
        }
    }

    /**
     * Event dispatched when an entity has been affected by the miasma.
     * <p>
     *     This event is not {@link Cancelable}.
     * </p>
     */
    public static class InfectionPostApplyEvent extends MiasmaEvent {
        private final IInfection infection;

        /**
         * Creates a new miasma post application event.
         * @param entity the entity the miasma was applied to
         * @param infection a snapshot of the infection immediately after application
         */
        public InfectionPostApplyEvent(LivingEntity entity, IInfection infection) {
            super(entity);
            this.infection = infection;
        }

        /** Returns a snapshot of the infection immediately after application */
        public IInfection getInfection() {
            return infection;
        }
    }

    /**
     * Event dispatched when the stage of an existing miasma changes.
     * <p>
     *     This event is not {@link Cancelable}.
     * </p>
     */
    public static class InfectionStageChangeEvent extends MiasmaEvent {
        private final InfectionStage oldStage;
        private final IInfection infection;

        /**
         * Creates a miasma stage changed event.
         *
         * @param entity the infected entity
         * @param oldStage the previous stage of the infection
         * @param infection a snapshot of the infection after the stage was changed
         */
        public InfectionStageChangeEvent(LivingEntity entity, InfectionStage oldStage, IInfection infection) {
            super(entity);
            this.oldStage = oldStage;
            this.infection = infection;
        }

        /** Returns the previous stage before the change */
        public InfectionStage getOldStage() {
            return oldStage;
        }

        /** Returns a snapshot of the infection immediately after the stage was changed */
        public IInfection getInfection() {
            return infection;
        }
    }

    /**
     * Event dispatched when the stage of an existing miasma restarts.
     * <p>
     *     This event is not {@link Cancelable}.
     * </p>
     */
    public static class InfectionStageRestartedEvent extends MiasmaEvent {
        private final IInfection infection;

        /**
         * Create a new miasma stage restarted event.
         *
         * @param entity the infected entity
         * @param infection a snapshot of the infection after the stage was restarted
         */
        public InfectionStageRestartedEvent(LivingEntity entity, IInfection infection) {
            super(entity);
            this.infection = infection;
        }

        /** Returns a snapshot of the infection immediately after the stage was restarted */
        public IInfection getInfection() {
            return infection;
        }
    }

    /**
     * Event dispatched when a the miasma infection is about to pulse.
     * <p>
     *     Handlers may apply infection modifiers through use of {@link #addModifier(MiasmaPropertyModifierType, Object)}.
     * </p>
     * <p>
     *     This event is {@link Cancelable}.<br/>
     *     If this event is canceled, the infection will be {@link InfectionMode#PAUSED} for this pulse.<br/>
     * </p>
     */
    @Cancelable
    public static class InfectionPrePulseEvent extends MiasmaEvent {
        private final IInfection infection;
        private final MiasmaPropertyModifiers.Builder modifiersBuilder;

        /**
         * Creates a new miasma pre pulse event.
         * @param entity the infected entity
         * @param infection a snapshot of the infection from just before the pulse
         * @param modifiersBuilder a modifiers builder to use to collect modifiers from event handlers
         */
        public InfectionPrePulseEvent(LivingEntity entity, IInfection infection, MiasmaPropertyModifiers.Builder modifiersBuilder) {
            super(entity);
            this.infection = infection;
            this.modifiersBuilder = modifiersBuilder;
        }

        /** Returns a snapshot of the infection before the pulse */
        public IInfection getInfection() {
            return infection;
        }

        /**
         * Adds a modifier to apply to the infection.
         * @param type the type of modifier to apply
         * @param value the modifier value to apply
         * @param <T> the type of the modifier value
         */
        public <T> void addModifier(MiasmaPropertyModifierType<T> type, T value) {
            modifiersBuilder.addModifier(type, value);
        }
    }
    
    /**
     * Event dispatched after the miasma infection pulses.
     * <p>
     *     This event is not {@link Cancelable}.
     * </p>
     */
    public static class InfectionPostPulseEvent extends MiasmaEvent {
        private final IInfection infection;

        /**
         * Creates a new miasma post pulse event
         * @param entity the infected entity
         * @param infection a snapshot of the infection immediately after the pulse
         */
        public InfectionPostPulseEvent(LivingEntity entity, IInfection infection) {
            super(entity);
            this.infection = infection;
        }

        /** Returns a snapshot of the infection immediately after the pulse */
        public IInfection getInfection() {
            return infection;
        }
    }

    /**
     * Event dispatched when the miasma infection reaches its conclusion and attempts to kill the entity.
     * <p>
     *     This event is {@link Cancelable}.<br/>
     *     If this event is canceled, the kill is blocked and the {@link InfectionStage#KILLING} stage resets its timer.
     * </p>
     */
    @Cancelable
    public static class InfectionKillEvent extends MiasmaEvent {
        private final IInfection infection;

        /**
         * Creates a new miasma kill event
         * @param entity the infected entity
         * @param infection a snapshot of the infection immediately before the kill
         */
        public InfectionKillEvent(LivingEntity entity, IInfection infection) {
            super(entity);
            this.infection = infection;
        }

        /** Returns a snapshot of the infection immediately before the kill */
        public IInfection getInfection() {
            return infection;
        }
    }

    /**
     * Event dispatched when the miasma infection is removed.
     * <p>
     *     This event is not {@link Cancelable}.
     * </p>
     */
    public static class InfectionRemovedEvent extends MiasmaEvent {
        private final IInfection infection;

        /**
         * Creates a new miasma removed event
         * @param entity the previously infected entity
         * @param infection a snapshot of the infection immediately before its removal
         */
        public InfectionRemovedEvent(LivingEntity entity, IInfection infection) {
            super(entity);
            this.infection = infection;
        }

        /** Returns a snapshot of the infection immediately before its removal */
        public IInfection getInfection() {
            return infection;
        }
    }
}
