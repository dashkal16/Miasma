/*
 * Miasma Minecraft Mod
 * Copyright © 2021 Dashkal
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the “Software”), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package name.dashkal.minecraft.miasma.lib;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Utility class providing a versioned NBT serializer.
 * <p>
 *     Users of this class must avoid using {@code "version"} as a tag key in their serializer.
 * </p>
 */
public class VersionedNBT {
    public static final String KEY_VERSION = "version";

    public static CompoundNBT serialize(int version, Consumer<CompoundNBT> serializer) {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt(KEY_VERSION, version);
        serializer.accept(nbt);
        return nbt;
    }

    public static void deserialize(INBT nbt, Predicate<Integer> acceptedVersions, BiConsumer<Integer, CompoundNBT> deserializer, Runnable whenEmpty) {
        if (nbt instanceof CompoundNBT) {
            CompoundNBT tag = (CompoundNBT) nbt;
            int version = tag.getInt(KEY_VERSION);
            if (acceptedVersions.test(version)) {
                deserializer.accept(tag.getInt(KEY_VERSION), tag);
                return;
            }
        }
        whenEmpty.run();
    }
}
