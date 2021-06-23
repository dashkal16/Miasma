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

import com.google.common.collect.ImmutableList;
import name.dashkal.minecraft.miasma.api.InfectionMode;
import name.dashkal.minecraft.miasma.api.InfectionStage;
import name.dashkal.minecraft.miasma.api.property.MiasmaPropertyModifiers;
import name.dashkal.minecraft.miasma.common.logic.Infection;
import name.dashkal.minecraft.miasma.common.logic.MiasmaLogic;
import name.dashkal.minecraft.miasma.lib.VersionedNBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of the {@link IMiasmaHandler} capability interface.
 * <p>
 *     Holds onto the status of the miasma for a single entity.
 * </p>
 */
public class MiasmaHandler implements IMiasmaHandler, INBTSerializable<CompoundNBT> {
    private Infection infection = null;

    // Game tick counter used to space out miasma infection actions
    private int infectionAttemptTicks = 0;

    /**
     * Returns the number of ticks since the last reset.
     */
    @Override
    public boolean isInfectionAttemptTick() {
        // Post-increment chosen intentionally so a 0 (which we reset to on infection) means try.
        return infectionAttemptTicks++ % 10 == 0;
    }

    @Override
    public Optional<Infection> getInfection() {
        return Optional.ofNullable(infection);
    }

    @Override
    public Infection applyInfection(InfectionStage stage, InfectionMode mode, MiasmaPropertyModifiers modifiers) {
        this.infection = new Infection(stage, mode, modifiers);
        this.infectionAttemptTicks = 0;
        return this.infection;
    }

    @Override
    public void removeInfection() {
        this.infection = null;
        this.infectionAttemptTicks = 0;
    }

    @Override
    public List<IFormattableTextComponent> getDebugReport() {
        if (infection == null) {
            return ImmutableList.of(debugLine("stage", "Not Infected"));
        } else {
            String progressPct = (int) (infection.getStageProgress() * 100) + "%";
            return ImmutableList.of(
                    debugLine("stage",              infection.getStage()),
                    debugLine("mode",               infection.getMode()),
                    debugLine("progress",           progressPct),
                    debugLine("cleansingTicks",     infection.getCleansingTicks()),
                    debugLine("intensifyingTicks",  infection.getIntensifyingTicks()),
                    debugLine("ticksToCleanse",     infection.getGameTicksToCleanseStage().orElse(-1)),
                    debugLine("ticksToIntensify",   infection.getGameTicksToIntensifyStage().orElse(-1)),
                    debugLine("gameTicks",          infection.getGameTicks()),
                    debugLine("ticksPerPulse",      MiasmaLogic.getGameTicksPerPulse(infection.getStage()))
            );
        }
    }

    private static IFormattableTextComponent debugLine(String label, Object value) {
        return new StringTextComponent(label + ": ").withStyle(TextFormatting.BLUE)
                .append(new StringTextComponent(value.toString()).withStyle(TextFormatting.WHITE));
    }

    @Override
    public void copyFrom(IMiasmaHandler other) {
        // Defensively copy, just in case something kept a hold of the previous one.
        deserializeNBT(other.serializeNBT());
    }

    // Compound tag keys when (de)serializing NBT
    private static final int CURRENT_VERSION = 1; // Increment if changing the serialization format
    private static final String KEY_INFECTION = "infection";

    @Override
    public CompoundNBT serializeNBT() {
        return VersionedNBT.serialize(CURRENT_VERSION, nbt -> {
            if (infection != null) {
                nbt.put(KEY_INFECTION, infection.serializeNBT());
            }
        });
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        VersionedNBT.deserialize(nbt, (v -> v == CURRENT_VERSION), (v, tag) -> {
            this.infection = null;
            if (tag.contains(KEY_INFECTION)) {
                // Infected
                INBT infectionNBT = tag.get(KEY_INFECTION);
                if (infectionNBT instanceof CompoundNBT) {
                    this.infection = new Infection((CompoundNBT) infectionNBT);
                }
            }
        }, () ->
            this.infection = null
        );
    }
}
