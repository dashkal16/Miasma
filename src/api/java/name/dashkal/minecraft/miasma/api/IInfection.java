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

import java.util.Optional;

/**
 * Interface representing a view of the current state an active Miasma infection.
 */
public interface IInfection {
    /**
     * Returns the current stage of the miasma.
     */
    InfectionStage getStage();

    /**
     * Returns the current mode of the miasma.
     */
    InfectionMode getMode();

    /**
     * Returns the progress through this stage the infection is in.
     *
     * @return a floating point number where {@code 0.0f} is the cleansing edge of the stage and {@code 1.0f} is the
     * intensifying edge of the stage.  If the return value is outside these bounds, it is time to change stage.
     */
    float getStageProgress();

    /**
     * Returns the number of in-game ticks until the infection will cleanse a stage, assuming continued safety.
     */
    Optional<Integer> getGameTicksToCleanseStage();

    /**
     * Returns the number of in-game ticks until the infection will intensify a stage, assuming continued exposure.
     */
    Optional<Integer> getGameTicksToIntensifyStage();
}
