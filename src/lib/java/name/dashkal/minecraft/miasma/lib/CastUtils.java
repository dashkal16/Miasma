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

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/** Utility class for {@link Optional} type casting. */
public class CastUtils {
    public static <A, B> Optional<B> optCast(Class<B> expectedClass, A a) {
        if (expectedClass.isAssignableFrom(a.getClass())) {
            return Optional.of(expectedClass.cast(a));
        }
        return Optional.empty();
    }

    public static <A, B> void ifCast(Class<B> expectedClass, A a, Consumer<B> consumer) {
        if (expectedClass.isAssignableFrom(a.getClass())) {
            consumer.accept(expectedClass.cast(a));
        }
    }

    public static <A, B, C> C foldCast(Class<B> expectedClass, A a, Function<B, C> function, Supplier<C> defaultSupplier) {
        if (expectedClass.isAssignableFrom(a.getClass())) {
            return function.apply(expectedClass.cast(a));
        } else {
            return defaultSupplier.get();
        }
    }
}
