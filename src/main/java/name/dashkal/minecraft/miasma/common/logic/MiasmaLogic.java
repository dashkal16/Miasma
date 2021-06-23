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
package name.dashkal.minecraft.miasma.common.logic;

import name.dashkal.minecraft.miasma.api.InfectionMode;
import name.dashkal.minecraft.miasma.api.InfectionStage;
import name.dashkal.minecraft.miasma.api.capability.IMiasmaModifier;
import name.dashkal.minecraft.miasma.api.events.MiasmaEvent.*;
import name.dashkal.minecraft.miasma.api.property.MiasmaPropertyModifierType;
import name.dashkal.minecraft.miasma.api.property.MiasmaPropertyModifiers;
import name.dashkal.minecraft.miasma.common.capability.IMiasmaHandler;
import name.dashkal.minecraft.miasma.common.capability.MiasmaModifierCapability;
import name.dashkal.minecraft.miasma.common.capability.MiasmaHandlerCapability;
import name.dashkal.minecraft.miasma.common.config.CommonConfig;
import name.dashkal.minecraft.miasma.common.effect.MiasmaEffect;
import name.dashkal.minecraft.miasma.common.network.MiasmaChannel;
import name.dashkal.minecraft.miasma.common.network.MiasmaHandlerSyncPacket;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import org.apache.commons.lang3.math.Fraction;

import java.util.Optional;

import static net.minecraftforge.common.MinecraftForge.EVENT_BUS;

/**
 * Utility class containing most of the logic behind the miasma infection.
 */
@SuppressWarnings("unused")
public class MiasmaLogic {
    private static final CommonConfig COMMON_CONFIG = CommonConfig.INSTANCE;

    /** Damage type used when in the warning phase of the Miasma effect. Bypasses Armor. */
    public static final DamageSource DAMAGE_HARM = new DamageSource("miasma_harm").bypassArmor();
    /** Damage type used when in the kill phase of the Miasma effect. Bypasses both armor and magic (absorption). */
    public static final DamageSource DAMAGE_KILL = new DamageSource("miasma_kill").bypassArmor().bypassMagic();

    /**
     * Logic to run every game tick for any entity with the {@link IMiasmaHandler} capability.
     */
    public static void gameTick(LivingEntity entity, IMiasmaHandler miasmaHandler) {
        Optional<Infection> infection = miasmaHandler.getInfection();
        if (infection.isPresent()) {
            // The entity is already infected, run the miasma pulse when the handler says its time
            if (infection.get().gameTick()) {
                MiasmaLogic.miasmaPulse(entity, miasmaHandler, infection.get());
            }
        } else {
            // The entity is not infected. Try now.
            if (miasmaHandler.isInfectionAttemptTick() && MiasmaLogic.mayBeAffected(entity)) {
                MiasmaLogic.tryApplyInfection(entity, miasmaHandler, InfectionStage.WARNING);
            }
        }
    }

    /**
     * Check if the given entity is subject to infection by miasma.
     * <p>
     *     Conditions:
     *     <ul>
     *         <li>The entity must be alive</li>
     *         <li>It must not be raining</li>
     *         <li>It must be daytime</li>
     *         <li>The entity must be able to see the sky</li>
     *     </ul>
     * </p>
     */
    private static boolean mayBeAffected(LivingEntity entity) {
        World world = entity.getCommandSenderWorld();

        // The predicates should be roughly in order of CPU cost
        return (entity.isAlive()
                && !world.getLevelData().isRaining()
                && world.getSkyDarken() < 4 // Constant from world.isDay()
                && world.canSeeSky(new BlockPos(entity.position()).above())
        );
    }

    /**
     * Attempts to infect the given entity with the miasma.
     * @return {@code true} if the miasma was successfully applied
     */
    public static boolean tryApplyInfection(LivingEntity entity, IMiasmaHandler miasmaHandler, InfectionStage stage) {
        /*
         * Conditions to check:
         *
         * Server side only (We sync the application back to the client)
         * Entity must be alive
         * Entity must not have an infection of equal or greater stage
         * Entity must not be in creative mode
         * Gear must not block the application
         * The MiasmaPreApplyEvent must not be canceled
         */
        if (!entity.getCommandSenderWorld().isClientSide()
            && entity.isAlive()
            && miasmaHandler.getInfection().map(i -> i.getStage().compareTo(stage) < 0).orElse(true)
            && !isCreative(entity)
            && MiasmaModifierCapability.checkModifiersUntilFalse(entity, miasmaModifier -> miasmaModifier.checkApply(entity, stage, false))
        ) {
            MiasmaPropertyModifiers.Builder builder = new MiasmaPropertyModifiers.Builder(MiasmaModifierCapability.getTotalGearModifiers(entity));
            if (!EVENT_BUS.post(new InfectionPreApplyEvent(entity, stage, false, builder))) {
                applyInfectionReal(entity, miasmaHandler, stage, builder.build());
                return true;
            }
        }
        return false;
    }

    /**
     * Infects the target entity with the miasma at the given rank, overriding any protections and overwriting any
     * existing infection.
     * <p>
     *     The entity must be alive and have the {@link IMiasmaHandler} capability to be infected.
     * </p>
     * <p>
     *     Note: Calling this function will check gear and fire the
     *     {@link InfectionPreApplyEvent} event, but it will ignore
     *     gear blocking or event cancellation.
     * </p>
     * @return {@code true} if the infection was successfully applied or false if the entity cannot be infected.
     */
    public static boolean forceApplyInfection(LivingEntity entity, InfectionStage stage) {
        if (entity.isAlive()) {
            return MiasmaHandlerCapability.withCapability(entity, miasmaHandler -> {
                MiasmaPropertyModifiers.Builder builder = new MiasmaPropertyModifiers.Builder();
                for (IMiasmaModifier miasmaModifier : MiasmaModifierCapability.getModifiers(entity)) {
                    miasmaModifier.checkApply(entity, stage, true);
                    builder.addAll(miasmaModifier.getPropertyModifiers());
                }
                EVENT_BUS.post(new InfectionPreApplyEvent(entity, stage, false, builder));
                applyInfectionReal(entity, miasmaHandler, stage, builder.build());
                return true;
            }).orElse(false);
        }
        return false;
    }

    /**
     * Infects the given entity with the miasma at the given rank, overriding any protections.
     */
    private static void applyInfectionReal(LivingEntity entity, IMiasmaHandler miasmaHandler, InfectionStage stage, MiasmaPropertyModifiers modifiers) {
        Infection infection = miasmaHandler.applyInfection(stage, InfectionMode.INTENSIFYING, modifiers);
        entity.addEffect(MiasmaEffect.createEffectInstance(stage));
        EVENT_BUS.post(new InfectionPostApplyEvent(entity, infection.getSnapshot()));
        MiasmaChannel.sendToPlayerClient(entity, new MiasmaHandlerSyncPacket(miasmaHandler));
    }

    /**
     * Called to run the miasma pulse logic.
     * @return {@code true} if the pulse ran or {@code false} if not.
     */
    public static boolean miasmaPulse(LivingEntity entity, IMiasmaHandler miasmaHandler, Infection infection) {
        InfectionStage stage = infection.getStage();
        InfectionMode previousMode = infection.getMode();
        InfectionMode expectedMode = mayBeAffected(entity) ? InfectionMode.INTENSIFYING : InfectionMode.CLEANSING;
        InfectionMode newMode;
        boolean needsSync;

        // Check for creative, then check gear, then fire the pre-pulse event.
        // If any of these indicate we should not pulse, set the mode to Paused.
        // Note that the event mutates the modifiers builder!
        MiasmaPropertyModifiers.Builder modifiersBuilder = new MiasmaPropertyModifiers.Builder(MiasmaModifierCapability.getTotalGearModifiers(entity));
        infection.setMode(expectedMode);
        if (isCreative(entity)
        || !MiasmaModifierCapability.checkModifiersUntilFalse(entity, miasmaModifier -> miasmaModifier.checkPulse(entity, infection.getSnapshot()))
        || EVENT_BUS.post(new InfectionPrePulseEvent(entity, infection.getSnapshot(), modifiersBuilder))) {
            // Event was canceled, pause.
            newMode = InfectionMode.PAUSED;
        } else {
            // Event was not canceled, proceed.
            newMode = expectedMode;
        }
        MiasmaPropertyModifiers modifiers = modifiersBuilder.build();

        // Since it's technically possible for a piece of gear or event handler to forcibly remove the infection, check.
        if (!miasmaHandler.getInfection().isPresent()) {
            // Naughty naughty...
            updateEffect(entity, miasmaHandler);
            MiasmaChannel.sendToPlayerClient(entity, new MiasmaHandlerSyncPacket(miasmaHandler));
            return false;
        }

        // If the mode has changed, we need to synchronize.
        needsSync = newMode != previousMode;
        if (newMode == InfectionMode.INTENSIFYING) {
            if (runIntensifyingPulse(entity, miasmaHandler, infection, modifiers)) {
                intensifyInfectionStage(entity, miasmaHandler, infection, modifiers);
                needsSync = true; // Stage changed.  We need to synchronize.
            }
        } else if (newMode == InfectionMode.CLEANSING) {
            if (runCleansingPulse(entity, miasmaHandler, infection, modifiers)) {
                cleanseInfectionStage(entity, miasmaHandler, infection, modifiers);
                needsSync = true; // Stage changed.  We need to synchronize.
            }
        } else {
            // Even when paused, pulse so the mode and modifiers update.
            infection.pulse(newMode, modifiers);
        }

        // If not paused, fire the pulsed event
        if (newMode != InfectionMode.PAUSED) {
            // Fire the pulsed event
            EVENT_BUS.post(new InfectionPostPulseEvent(entity, infection.getSnapshot()));
        }

        // Update the status effect
        updateEffect(entity, miasmaHandler);

        // Synchronize the client if necessary
        if (!entity.getCommandSenderWorld().isClientSide && needsSync) {
            MiasmaChannel.sendToPlayerClient(entity, new MiasmaHandlerSyncPacket(miasmaHandler));
        }
        return newMode != InfectionMode.PAUSED;
    }

    /** Removes the miasma effect from the given entity. */
    public static void removeMiasma(LivingEntity entity, IMiasmaHandler miasmaHandler) {
        entity.removeEffect(MiasmaEffect.INSTANCE);
        miasmaHandler.getInfection().ifPresent(infection -> {
            miasmaHandler.removeInfection();
            EVENT_BUS.post(new InfectionRemovedEvent(entity, infection.getSnapshot()));
        });
        MiasmaChannel.sendToPlayerClient(entity, new MiasmaHandlerSyncPacket(miasmaHandler));
    }

    /**
     * Called when the {@link MiasmaEffect} is removed.
     * <p>
     *     The potion effect can be removed unexpectedly by way of commands or mods that ignore curative item
     *     restrictions.  If this happens, accept it, but clean up.
     * </p>
     */
    public static void onEffectRemoved(LivingEntity entity) {
        MiasmaHandlerCapability.ifPresent(entity, miasmaHandler -> {
            if (miasmaHandler.getInfection().isPresent()) {
                miasmaHandler.removeInfection();
            }
            MiasmaChannel.sendToPlayerClient(entity, new MiasmaHandlerSyncPacket(miasmaHandler));
        });
    }

    /**
     * Returns the number of game ticks (not miasma ticks!) until the current stage will be cleansed off.
     * <p>
     *     Note: This will return {@link Optional#empty()} if the entity is not infected or if the infection is in
     *     {@link InfectionMode#PAUSED} mode.
     * </p>
     */
    public static Optional<Integer> getGameTicksToCleanseStage(IMiasmaHandler miasmaHandler) {
        return miasmaHandler.getInfection()
                .filter(i -> i.getMode() != InfectionMode.PAUSED)
                .flatMap(Infection::getGameTicksToCleanseStage);
    }

    /**
     * Returns the total number of game ticks (not miasma ticks!) required to cleanse the given stage.
     */
    public static int getTotalTicksToCleanseStage(InfectionStage stage) {
        return COMMON_CONFIG.getMiasmaCleanseTimeSeconds(stage) * 20;
    }

    /**
     * Returns the number of game ticks (not miasma ticks!) until the effect is to rank up due to infection.
     * <p>
     *     Note: This will return {@link Optional#empty()} if not infected, the infection is in
     *     {@link InfectionMode#PAUSED} mode, or if the miasma is in stage {@link InfectionStage#KILLING} and we are
     *     not configured to kill the player at the end of the stage.
     * </p>
     */
    public static Optional<Integer> getGameTicksToIntensifyStage(IMiasmaHandler miasmaHandler) {
        return miasmaHandler.getInfection()
                .filter(i -> i.getMode() != InfectionMode.PAUSED)
                .flatMap(Infection::getGameTicksToIntensifyStage);
    }

    /**
     * Returns the total number of game ticks (not miasma pulses!) required to advance through the given stage.
     */
    public static int getTotalTicksToAdvanceStage(InfectionStage stage) {
        return COMMON_CONFIG.getMiasmaDurationSeconds(stage) * 20;
    }

    /**
     * Returns the number of game ticks per miasma pulse during given a given stage.
     */
    public static int getGameTicksPerPulse(InfectionStage stage) {
        return stage == InfectionStage.KILLING ? 10 : 20;
    }

    /**
     * Runs the per effect tick logic for infection.
     * @return {@code true} if it's time to intensify the infection.
     */
    private static boolean runIntensifyingPulse(LivingEntity entity, IMiasmaHandler miasmaHandler, Infection infection, MiasmaPropertyModifiers modifiers) {
        InfectionStage stage = infection.getStage();
        boolean shouldIntensify = infection.pulse(InfectionMode.INTENSIFYING, modifiers);

        // Perform the appropriate action
        sendOverlayMessage(entity, "miasma.message." + stage.name().toLowerCase());
        int damage = modifiers.getModifier(MiasmaPropertyModifierType.DAMAGE).multiplyBy(Fraction.getFraction(COMMON_CONFIG.getMiasmaTickDamage(stage), 1)).intValue();
        switch (stage) {
            case WARNING:
                break;
            case HARMING:
                entity.hurt(MiasmaLogic.DAMAGE_HARM, damage);
                break;
            case KILLING:
                entity.hurt(MiasmaLogic.DAMAGE_KILL, damage);
                break;
        }

        return shouldIntensify;
    }

    /**
     * Intensifies the infection by one stage, killing the player if intensifying past {@link InfectionStage#KILLING}
     * and configured to do so.
     */
    private static void intensifyInfectionStage(LivingEntity entity, IMiasmaHandler miasmaHandler, Infection infection, MiasmaPropertyModifiers modifiers) {
        InfectionStage stage = infection.getStage();
        switch (stage) {
            case WARNING:
            case HARMING:
                // Escalate the miasma
                InfectionStage newStage = stage == InfectionStage.WARNING ? InfectionStage.HARMING : InfectionStage.KILLING;
                infection.setStage(newStage, true, modifiers);
                EVENT_BUS.post(new InfectionStageChangeEvent(entity, stage, infection));
                break;
            case KILLING:
                if (MiasmaModifierCapability.checkModifiersUntilFalse(entity, miasmaModifier -> miasmaModifier.checkKill(entity, infection.getSnapshot()))
                        && !EVENT_BUS.post(new InfectionKillEvent(entity, infection.getSnapshot()))
                ) {
                    // End of the line
                    if (COMMON_CONFIG.killStageKillsOnExpiry()) {
                        entity.hurt(MiasmaLogic.DAMAGE_KILL, Integer.MAX_VALUE);
                        if (entity.isDeadOrDying()) {
                            miasmaHandler.removeInfection();
                            return;
                        }
                    }
                }
                // Unable to kill the player, reset the timer.
                infection.setStage(InfectionStage.KILLING, true, modifiers);
                EVENT_BUS.post(new InfectionStageRestartedEvent(entity, infection.getSnapshot()));
                break;
        }
    }

    /**
     * Runs the per effect tick logic for cleansing.
     * @return {@code true} if it's time to cleanse the infection by a stage.
     */
    private static boolean runCleansingPulse(LivingEntity entity, IMiasmaHandler miasmaHandler, Infection infection, MiasmaPropertyModifiers modifiers) {
        return infection.pulse(InfectionMode.CLEANSING, modifiers);
    }

    /**
     * Cleanse the infection by one stage, or remove it if cleansing past {@link InfectionStage#WARNING}.
     */
    private static void cleanseInfectionStage(LivingEntity entity, IMiasmaHandler miasmaHandler, Infection infection, MiasmaPropertyModifiers modifiers) {
        InfectionStage stage = infection.getStage();

        switch (stage) {
            case WARNING:
                // Cleansing complete, remove the miasma entirely
                removeMiasma(entity, miasmaHandler);
                break;
            case HARMING:
            case KILLING:
                // Drop the stage by one
                InfectionStage newStage = stage == InfectionStage.KILLING ? InfectionStage.HARMING : InfectionStage.WARNING;
                infection.setStage(newStage, false, modifiers);
                EVENT_BUS.post(new InfectionStageChangeEvent(entity, stage, infection));
                break;
        }
    }

    /**
     * Updates the the status effect, if present.
     */
    public static void updateEffect(LivingEntity entity, IMiasmaHandler miasmaHandler) {
        if (miasmaHandler.getInfection().isPresent()) {
            Infection infection = miasmaHandler.getInfection().get();
            InfectionStage stage = infection.getStage();

            // Re-apply the potion effect if it's gone missing
            if (!entity.hasEffect(MiasmaEffect.INSTANCE)) {
                entity.addEffect(MiasmaEffect.createEffectInstance(stage));
            }

            // Update the effect's amp/duration
            Optional.ofNullable(entity.getEffect(MiasmaEffect.INSTANCE)).ifPresent(ei -> {
                ei.amplifier = stage.ordinal();
                ei.duration = 0;
                switch (infection.getMode()) {
                    case INTENSIFYING:
                        if (COMMON_CONFIG.killStageKillsOnExpiry()) {
                            switch (stage) {
                                case WARNING:
                                    ei.duration += getTotalTicksToAdvanceStage(InfectionStage.HARMING);
                                case HARMING:
                                    ei.duration += getTotalTicksToAdvanceStage(InfectionStage.KILLING);
                                case KILLING:
                                    infection.getGameTicksToIntensifyStage().ifPresent(t -> ei.duration += t);
                            }
                        } else {
                            // If non-terminal, there is no maximum duration when intensifying.
                            ei.duration = Integer.MAX_VALUE;
                        }
                        break;
                    case PAUSED:
                        ei.duration = Integer.MAX_VALUE;
                        break;
                    case CLEANSING:
                        switch (stage) {
                            case KILLING:
                                ei.duration += getTotalTicksToCleanseStage(InfectionStage.HARMING);
                            case HARMING:
                                ei.duration += getTotalTicksToCleanseStage(InfectionStage.WARNING);
                            case WARNING:
                                infection.getGameTicksToCleanseStage().ifPresent(t -> ei.duration += t);
                        }
                        break;
                }
            });
        } else {
            entity.removeEffect(MiasmaEffect.INSTANCE);
        }
    }

    /** Sends a message to the player as an overlay message. */
    private static void sendOverlayMessage(LivingEntity entity, String messageKey) {
        if (entity instanceof PlayerEntity) {
            ((PlayerEntity) entity).displayClientMessage(
                    new TranslationTextComponent(messageKey).withStyle(TextFormatting.RED),
                    true // Overlay message
            );
        }
    }

    /** Returns {@code true} if the entity is a player in creative mode. */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean isCreative(LivingEntity entity) {
        return entity instanceof PlayerEntity && ((PlayerEntity) entity).isCreative();
    }
}
