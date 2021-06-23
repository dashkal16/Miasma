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

import com.google.common.math.IntMath;
import name.dashkal.minecraft.miasma.api.IInfection;
import name.dashkal.minecraft.miasma.api.InfectionMode;
import name.dashkal.minecraft.miasma.api.InfectionStage;
import name.dashkal.minecraft.miasma.api.property.MiasmaPropertyModifierType;
import name.dashkal.minecraft.miasma.api.property.MiasmaPropertyModifiers;
import name.dashkal.minecraft.miasma.common.config.CommonConfig;
import name.dashkal.minecraft.miasma.lib.VersionedNBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;
import org.apache.commons.lang3.math.Fraction;

import java.util.Optional;

public class Infection implements IInfection, INBTSerializable<CompoundNBT> {
    // Infection
    private InfectionStage stage;
    private InfectionMode mode;

    // Calculated Fields (in terms of Stage and multipliers)
    private int cleansingRatio;
    private int intensifyingRatio;
    private int targetTicks;
    private int ticksPerPulse;

    // Tick tracking
    private int cleansingTicks;
    private int intensifyingTicks;
    private int gameTicks;

    private Infection(InfectionStage stage, InfectionMode mode, int cleansingRatio, int intensifyingRatio, int targetTicks, int ticksPerPulse, int cleansingTicks, int intensifyingTicks, int gameTicks) {
        this.stage = stage;
        this.mode = mode;
        this.cleansingRatio = cleansingRatio;
        this.intensifyingRatio = intensifyingRatio;
        this.targetTicks = targetTicks;
        this.ticksPerPulse = ticksPerPulse;
        this.cleansingTicks = cleansingTicks;
        this.intensifyingTicks = intensifyingTicks;
        this.gameTicks = gameTicks;
    }

    public Infection(InfectionStage stage, InfectionMode mode, MiasmaPropertyModifiers modifiers) {
        this(stage, mode, 0, 0, 0, 0, 0, 0, 0);
        recalculateTargets(modifiers);
    }

    public Infection(CompoundNBT nbt) {
        this(InfectionStage.WARNING, InfectionMode.PAUSED, 0, 0, 0, 0, 0, 0, 0);
        recalculateTargets(MiasmaPropertyModifiers.empty());
        deserializeNBT(nbt);
    }

    /**
     * Called to re-calculate the ratios and target ticks to change stage.
     * <p>
     *     Should be invoked if the stage or modifiers have changed.
     * </p>
     */
    private void recalculateTargets(MiasmaPropertyModifiers modifiers) {
        int tC = fracMult(MiasmaLogic.getTotalTicksToCleanseStage(stage), modifiers.getModifier(MiasmaPropertyModifierType.CLEANSE_STAGE_TIME));
        int tI = fracMult(MiasmaLogic.getTotalTicksToAdvanceStage(stage), modifiers.getModifier(MiasmaPropertyModifierType.INTENSIFY_STAGE_TIME));
        int gcd = IntMath.gcd(tC, tI);
        this.cleansingRatio = tC / gcd;
        this.intensifyingRatio = tI / gcd;
        this.targetTicks = tI;
        this.ticksPerPulse = MiasmaLogic.getGameTicksPerPulse(stage);
    }

    /** Returns an immutable copy of this infection. */
    public IInfection getSnapshot() {
        return new Infection(stage, mode, cleansingRatio, intensifyingRatio, targetTicks, ticksPerPulse, cleansingTicks, intensifyingTicks, gameTicks);
    }

    @Override
    public InfectionStage getStage() {
        return stage;
    }

    @Override
    public InfectionMode getMode() {
        return mode;
    }

    @Override
    public float getStageProgress() {
        return ((float) (intensifyingTicks - ((cleansingTicks * intensifyingRatio) / cleansingRatio)) / (float) targetTicks);
    }

    /**
     * Returns the number of ticks that have elapsed in {@link InfectionMode#CLEANSING} mode.
     */
    public int getCleansingTicks() {
        return cleansingTicks;
    }

    /**
     * Returns the number of ticks that have elapsed in {@link InfectionMode#INTENSIFYING} mode.
     */
    public int getIntensifyingTicks() {
        return intensifyingTicks;
    }

    /**
     * Returns the number of in-game ticks until the infection will cleanse a stage, assuming continued safety.
     */
    @Override
    public Optional<Integer> getGameTicksToCleanseStage() {
        return Optional.of(((intensifyingTicks * cleansingRatio) / intensifyingRatio) - cleansingTicks);
    }

    /**
     * Returns the number of in-game ticks until the infection will intensify a stage, assuming continued exposure.
     */
    @Override
    public Optional<Integer> getGameTicksToIntensifyStage() {
        if (stage == InfectionStage.KILLING && !CommonConfig.INSTANCE.killStageKillsOnExpiry()) {
            return Optional.empty();
        } else {
            return Optional.of(targetTicks - (intensifyingTicks - ((cleansingTicks * intensifyingRatio) / cleansingRatio)));
        }
    }

    /**
     * Called once a miasma pulse.
     * @param mode the mode this pulse occurs in
     * @param modifiers the modifiers for this pulse
     * @return {@code true} if it is time to change infection stage.
     */
    public boolean pulse(InfectionMode mode, MiasmaPropertyModifiers modifiers) {
        this.mode = mode;
        recalculateTargets(modifiers);
        switch (mode) {
            case CLEANSING:
                return cleansingTicks >= (intensifyingTicks * cleansingRatio) / intensifyingRatio;
            case INTENSIFYING:
                return intensifyingTicks - targetTicks >= ((cleansingTicks * intensifyingRatio) / cleansingRatio);
        }
        return false;
    }

    /** Sets the current infection mode. */
    public void setMode(InfectionMode mode) {
        this.mode = mode;
    }

    /**
     * Updates the state to the given miasma stage at the specified progress.
     * @param stage the stage to set the state to
     * @param startAtCleanseEdge if {@code true}, start right on the edge of cleansing, otherwise start on the edge of
     *                           intensifying.
     */
    public void setStage(InfectionStage stage, boolean startAtCleanseEdge, MiasmaPropertyModifiers modifiers) {
        this.stage = stage;
        recalculateTargets(modifiers);
        this.cleansingTicks = 0;
        this.intensifyingTicks = startAtCleanseEdge ? 0 : targetTicks;
    }

    /**
     * Increments the tick counter.
     * @return {@code true} if it is time for an infection pulse
     */
    public boolean gameTick() {
        switch (mode) {
            case CLEANSING:
                cleansingTicks++;
                break;
            case INTENSIFYING:
                intensifyingTicks++;
                break;
        }

        if (++gameTicks >= ticksPerPulse) {
            gameTicks = 0;
            return true;
        }
        return false;
    }

    public int getGameTicks() {
        return gameTicks;
    }

    /** Multiplies an integer by a fraction, then return only the whole number portion of that fraction. */
    private static int fracMult(int a, Fraction b) {
        return Fraction.getFraction(a).multiplyBy(b).intValue();
    }

    // Compound tag keys when (de)serializing NBT
    private static final int CURRENT_VERSION = 1;
    private static final String KEY_STAGE = "stage";
    private static final String KEY_MODE = "mode";
    private static final String KEY_CLEANSING_RATIO = "cRatio";
    private static final String KEY_INTENSIFYING_RATIO = "iRatio";
    private static final String KEY_TARGET_TICKS = "targetTicks";
    private static final String KEY_TICKS_PER_PULSE = "ticksPerPulse";
    private static final String KEY_CLEANSING_TICKS = "cTicks";
    private static final String KEY_INTENSIFYING_TICKS = "iTicks";
    private static final String KEY_GAME_TICKS = "gTicks";

    @Override
    public CompoundNBT serializeNBT() {
        return VersionedNBT.serialize(CURRENT_VERSION, nbt -> {
            nbt.putString(KEY_STAGE, stage.name());
            nbt.putString(KEY_MODE, mode.name());
            nbt.putInt(KEY_CLEANSING_RATIO, cleansingRatio);
            nbt.putInt(KEY_INTENSIFYING_RATIO, intensifyingRatio);
            nbt.putInt(KEY_TARGET_TICKS, targetTicks);
            nbt.putInt(KEY_TICKS_PER_PULSE, ticksPerPulse);
            nbt.putInt(KEY_CLEANSING_TICKS, cleansingTicks);
            nbt.putInt(KEY_INTENSIFYING_TICKS, intensifyingTicks);
            nbt.putInt(KEY_GAME_TICKS, gameTicks);
        });
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        VersionedNBT.deserialize(nbt, (v -> v == CURRENT_VERSION), (v, tag) -> {
            if (v == CURRENT_VERSION) {
                this.stage = InfectionStage.fromName(tag.getString(KEY_STAGE)).orElse(InfectionStage.WARNING);
                this.mode = InfectionMode.fromName(tag.getString(KEY_MODE)).orElse(InfectionMode.PAUSED);
                this.cleansingRatio = tag.getInt(KEY_CLEANSING_RATIO);
                this.intensifyingRatio = tag.getInt(KEY_INTENSIFYING_RATIO);
                this.targetTicks = tag.getInt(KEY_TARGET_TICKS);
                this.ticksPerPulse = tag.getInt(KEY_TICKS_PER_PULSE);
                this.cleansingTicks = tag.getInt(KEY_CLEANSING_TICKS);
                this.intensifyingTicks = tag.getInt(KEY_INTENSIFYING_TICKS);
                this.gameTicks = tag.getInt(KEY_GAME_TICKS);
            } else {
                recalculateTargets(MiasmaPropertyModifiers.empty());
            }
        }, () -> recalculateTargets(MiasmaPropertyModifiers.empty()) );
    }
}
