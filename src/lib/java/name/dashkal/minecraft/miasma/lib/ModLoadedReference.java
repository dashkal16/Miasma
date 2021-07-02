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
package name.dashkal.minecraft.miasma.lib;

import net.minecraftforge.fml.ModList;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Helper class used to hold an object of a class that is only safe to initialize when a specific mod is present.
 * <p>
 *     The intended use is for integration classes for optional dependencies.
 * </p>
 */
public class ModLoadedReference<T> {
    private T guarded = null;

    /**
     * Creates a new reference that will only initialize when the specified mod is present.
     * <p>
     *     If the mod is present, this will immediately invoke the supplier.<br/>
     *     If not, the reference will never try again.
     * </p>
     * <p>
     *     It <em>is</em> safe to instantiate these from a mod's constructor, but remember that mod loading is
     *     performed in parallel, so be sure <em>not</em> to actually attempt to access a mod's classes until
     *     their API documentation indicates that it is safe to do so.  Generally this means at least
     *     {@link net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent}.  Also as the loading is parallel, be sure
     *     to follow appropriate thread safety practices.  Use
     *     {@link net.minecraftforge.fml.event.lifecycle.ParallelDispatchEvent#enqueueWork(Runnable)} as required.
     * </p>
     * <p>
     *     Note: The only way to be sure to avoid classloader errors is if the caller passes a direct static method
     *     reference or constructor reference.  Capturing a closure can lead to problems.<br/>
     *     See {@link net.minecraftforge.fml.DistExecutor} for details.<br/>
     *     This class does <em>not</em> throw exceptions if such is attempted.
     * </p>
     * <p>
     *     The purpose of the double supplier is to avoid classloading the target class.
     * </p>
     *
     * @param modId the modId of the mod to check
     * @param supplier a supplier that will only be invoked if the mod is present
     */
    public <U extends T> ModLoadedReference(String modId, Supplier<Supplier<U>> supplier) {
        if (ModList.get().isLoaded(modId)) {
            guarded = supplier.get().get();
        }
    }

    /**
     * Returns {@code true} if the reference is defined (if the mod is loaded).
     */
    public boolean isDefined() {
        return guarded != null;
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
