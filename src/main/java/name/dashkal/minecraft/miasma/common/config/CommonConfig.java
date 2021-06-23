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
package name.dashkal.minecraft.miasma.common.config;

import com.google.common.collect.ImmutableList;
import name.dashkal.minecraft.miasma.api.InfectionStage;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.*;
import java.util.stream.Collectors;

public class CommonConfig {
    public static final CommonConfig INSTANCE = new CommonConfig();

    private final ForgeConfigSpec config;

    private final Set<ResourceLocation> susceptibleEntityTypes;

    private final ForgeConfigSpec.IntValue STAGE_WARN_DURATION;
    private final ForgeConfigSpec.IntValue STAGE_WARN_CLEANSE_TIME;

    private final ForgeConfigSpec.IntValue STAGE_HARM_DURATION;
    private final ForgeConfigSpec.IntValue STAGE_HARM_DAMAGE;
    private final ForgeConfigSpec.IntValue STAGE_HARM_CLEANSE_TIME;

    private final ForgeConfigSpec.IntValue STAGE_KILL_DURATION;
    private final ForgeConfigSpec.IntValue STAGE_KILL_DAMAGE;
    private final ForgeConfigSpec.IntValue STAGE_KILL_CLEANSE_TIME;
    private final ForgeConfigSpec.BooleanValue STAGE_KILL_TERMINATES_ON_EXPIRY;

    /**
     * Builds the configuration for the common (client/server) settings.
     */
    private CommonConfig() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        // General
        builder.comment("General Settings").push("general");
        builder.comment("A list of entity types that will be marked as susceptible to the miasma.");
        ForgeConfigSpec.ConfigValue<List<? extends String>> SUSCEPTIBLE_ENTITY_TYPES = builder.defineList(
                "susceptibleEntities",
                ImmutableList.of(
                    rn(EntityType.PLAYER)
                ),
                this::resourceLocationPredicate
        );
        builder.pop();

        // Warning Stage
        builder.comment("Warning Stage", "First stage where the miasma only warns the player that they're in danger.").push("stage_1");
        builder.comment("Duration of stage 1 in seconds before moving onto stage 2.");
        STAGE_WARN_DURATION = builder.defineInRange("duration", 30, 0, Integer.MAX_VALUE);
        builder.comment("Time in seconds it should take to for the miasma to be cleaned off when out of sunlight.");
        STAGE_WARN_CLEANSE_TIME = builder.defineInRange("cleanseTime", 10, 0, Integer.MAX_VALUE);
        builder.pop();

        // Harming Stage
        builder.comment("Harming Stage", "Second stage where the miasma hurts the entity with damage that ignores armor.").push("stage_2");
        builder.comment("Duration of stage 2 in seconds before moving onto stage 3.");
        STAGE_HARM_DURATION = builder.defineInRange("duration", 60, 0, Integer.MAX_VALUE);
        builder.comment("Damage done every second.");
        STAGE_HARM_DAMAGE = builder.defineInRange("damage", 2, 0, Integer.MAX_VALUE);
        builder.comment("Time in seconds it should take to for the miasma to return to the warning stage when out of sunlight.");
        STAGE_HARM_CLEANSE_TIME = builder.defineInRange("cleanseTime", 20, 0, Integer.MAX_VALUE);
        builder.pop();

        // Killing Stage
        builder.comment("Killing Stage", "Third stage where the miasma damage that ignores armor and absorption.")
                .comment("At the end of the timer, it kills the entity outright.").push("stage_3");
        builder.comment("Duration of stage 3 in seconds before simply killing the entity.");
        STAGE_KILL_DURATION = builder.defineInRange("duration", 30, 0, Integer.MAX_VALUE);
        builder.comment("Damage done every half second.");
        STAGE_KILL_DAMAGE = builder.defineInRange("damage", 2, 0, Integer.MAX_VALUE);
        builder.comment("Time in seconds it should take to for the miasma to return to the harming stage when out of sunlight.");
        STAGE_KILL_CLEANSE_TIME = builder.defineInRange("cleanseTime", 30, 0, Integer.MAX_VALUE);
        builder.comment("If true, the victim will be killed outright when the timer expires. If false, duration is ignored.");
        STAGE_KILL_TERMINATES_ON_EXPIRY = builder.define("terminateOnExpiration", true);
        builder.pop();

        // Build the configuration
        config = builder.build();

        // Load the susceptible entities
        susceptibleEntityTypes = Collections.unmodifiableSet(
                SUSCEPTIBLE_ENTITY_TYPES.get().stream()
                        .map(ResourceLocation::tryParse)
                        .collect(Collectors.toSet())
        );
    }

    /** Returns the forge config spec for the common configuration. */
    public ForgeConfigSpec getConfig() {
        return config;
    }

    /** Returns a list of types of entities that should be subject to the miasma. */
    public Set<ResourceLocation> getSusceptibleEntityTypes() {
        return susceptibleEntityTypes;
    }

    /**
     * Returns the duration of the requested miasma stage, in seconds.
     */
    public int getMiasmaDurationSeconds(InfectionStage stage) {
        switch (stage) {
            case WARNING: return STAGE_WARN_DURATION.get();
            case HARMING: return STAGE_HARM_DURATION.get();
            case KILLING: return STAGE_KILL_DURATION.get();
            default: return 0;
        }
    }

    /** Returns the cleansing time of the requested miasma stage, in seconds. */
    public int getMiasmaCleanseTimeSeconds(InfectionStage stage) {
        switch (stage) {
            case WARNING: return STAGE_WARN_CLEANSE_TIME.get();
            case HARMING: return STAGE_HARM_CLEANSE_TIME.get();
            case KILLING: return STAGE_KILL_CLEANSE_TIME.get();
            default: return 0;
        }
    }

    /**
     * Returns the damage the miasma deals to infected entities when the effect ticks.
     * <p>
     *     Note that the miasma ticks once a second in the warning and harming stages and twice a second in the killing
     *     stage.
     * </p>
     */
    public int getMiasmaTickDamage(InfectionStage stage) {
        switch (stage) {
            //case Warning: return 0;
            case HARMING: return STAGE_HARM_DAMAGE.get();
            case KILLING: return STAGE_KILL_DAMAGE.get();
            default: return 0;
        }
    }

    /**
     * Returns {@code true} if the killing stage outright kills the entity when its duration has passed.
     * <p>
     *     Note that if this is configured to {@code false}, {@link #getMiasmaDurationSeconds(InfectionStage)} will return
     *     {@code Integer.MAX_VALUE} for {@link InfectionStage#KILLING} instead of its configured value.
     * </p>
     */
    public boolean killStageKillsOnExpiry() {
        return STAGE_KILL_TERMINATES_ON_EXPIRY.get();
    }

    @SuppressWarnings("SameParameterValue")
    private String rn(EntityType<?> entityType) {
        return Objects.toString(entityType.getRegistryName());
    }

    private boolean resourceLocationPredicate(Object o) {
        if (o instanceof String) {
            try {
                new ResourceLocation((String) o);
                return true;
            } catch (ResourceLocationException ex) {
                return false;
            }
        }
        return false;
    }
}
