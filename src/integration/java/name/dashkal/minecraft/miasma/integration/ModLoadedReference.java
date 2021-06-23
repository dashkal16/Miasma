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
package name.dashkal.minecraft.miasma.integration;

import net.minecraftforge.fml.ModList;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Helper class used to hold an object of a class that is only safe to initialize when a specific mod is present.
 * <p>The intended use is for integration classes for optional dependencies.</p>
 */
public class ModLoadedReference<T> {
    private T guarded = null;

    /**
     * Creates a new reference that will only initialize when the specified mod is present.
     *
     * @param modId the modId of the mod to check
     * @param supplier a supplier that will only be invoked if the mod is present
     */
    public ModLoadedReference(String modId, Supplier<T> supplier) {
        if (ModList.get().isLoaded(modId)) {
            guarded = supplier.get();
        }
    }

    /**
     * Calls the given function with the guarded object if and only if the mod is present.
     *
     * @param function the function to invoke
     * @return the result of the function when the mod is present or {@link Optional#empty()} if not.
     */
    public <R> Optional<R> callWithIntegration(Function<T, R> function) {
        return Optional.ofNullable(guarded).map(function);
    }

    /**
     * Invokes the consumer with the guarded object if and only if the mod is present.
     *
     * @param consumer the consumer to invoke
     */
    public void withIntegration(Consumer<T> consumer) {
        if (guarded != null) {
            consumer.accept(guarded);
        }
    }
}
