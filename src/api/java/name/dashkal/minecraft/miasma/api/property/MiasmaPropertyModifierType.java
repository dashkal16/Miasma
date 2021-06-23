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
package name.dashkal.minecraft.miasma.api.property;

import org.apache.commons.lang3.math.Fraction;

import java.util.Optional;
import java.util.function.BinaryOperator;

/**
 * Type of a modifier to some aspect of an active miasma infection.
 * <p>
 *     An infection modifier type is a triplet of some type, a binary, associative, closed binary operation on that
 *     type, and a zero value that acts as an identity with respect to the operator.  In formal terms, a Monoid.
 * </p>
 * @see MiasmaPropertyModifiers
 */
public final class MiasmaPropertyModifierType<T> {
    private static final BinaryOperator<Fraction> fractionMultiplier = Fraction::multiplyBy;

    /** Modifier for the amount of time it takes to cleanse an infection by one stage. */
    public static MiasmaPropertyModifierType<Fraction> CLEANSE_STAGE_TIME = new MiasmaPropertyModifierType<>("miasma.modifier.cleanse_stage_time", Fraction.class, fractionMultiplier, Fraction.ONE);
    /** Modifier for the amount of time it takes to intensify an infection by one stage. */
    public static MiasmaPropertyModifierType<Fraction> INTENSIFY_STAGE_TIME = new MiasmaPropertyModifierType<>("miasma.modifier.intensify_stage_time", Fraction.class, fractionMultiplier, Fraction.ONE);
    /** Modifier for the amount of damage an entity takes when a miasma infection pulses. */
    public static MiasmaPropertyModifierType<Fraction> DAMAGE = new MiasmaPropertyModifierType<>("miasma.modifier.damage", Fraction.class, fractionMultiplier, Fraction.ONE);

    private final String unlocalizedName;
    private final Class<T> valueClass;
    private final BinaryOperator<T> combiner;
    private final T zero;

    // Private to ensure that we have a closed set.
    private MiasmaPropertyModifierType(String unlocalizedName, Class<T> valueClass, BinaryOperator<T> combiner, T zero) {
        this.unlocalizedName = unlocalizedName;
        this.valueClass = valueClass;
        this.combiner = combiner;
        this.zero = zero;
    }

    /** Returns the unlocalized name of this modifier */
    public String getUnlocalizedName() {
        return unlocalizedName;
    }

    /** Merges two modification values together */
    public T merge(T t1, T t2) {
        return combiner.apply(t1, t2);
    }

    /**
     * Returns the identity value of this modifier.
     * <p>
     *     This value is expected to result in no change to the associated property of an infection.<br/>
     *     This if this value is passed to {@link #merge(Object, Object)}, the other value will be returned unchanged.
     * </p>
     */
    public T getZero() {
        return zero;
    }

    /**
     * Checked cast of a modifier value.
     * <p>
     *     Note: This function depends on {@link MiasmaPropertyModifierType} using reference equality to determine type equality.
     * </p>
     *
     * @return If {@code expectedType} is this type, this function returns {@code t} cast {@code U}.  If not, this
     *         function returns {@link Optional#empty()}.
     */
    public <U> Optional<U> checkedCast(MiasmaPropertyModifierType<U> expectedType, T t) {
        if (expectedType.valueClass.isAssignableFrom(valueClass)) {
            return Optional.of(expectedType.valueClass.cast(t));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public boolean equals(Object obj) {
        // Because this class includes a function, only reference equality can be meaningful.
        return this == obj;
    }
}
