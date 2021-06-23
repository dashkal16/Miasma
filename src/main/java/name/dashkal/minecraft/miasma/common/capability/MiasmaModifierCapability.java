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

import name.dashkal.minecraft.miasma.MiasmaMod;
import name.dashkal.minecraft.miasma.api.IInfection;
import name.dashkal.minecraft.miasma.api.InfectionStage;
import name.dashkal.minecraft.miasma.api.capability.IMiasmaModifier;
import name.dashkal.minecraft.miasma.api.imc.MiasmaModifierLocator;
import name.dashkal.minecraft.miasma.api.property.MiasmaPropertyModifiers;
import name.dashkal.minecraft.miasma.lib.capability.SimpleCapabilityProvider;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.NonNullConsumer;
import net.minecraftforge.common.util.NonNullSupplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Capability implementation for {@link IMiasmaModifier}.
 * <p>Does basically nothing. Gear is expected to attach custom implementations to cover their specific behaviour.</p>
 */
public class MiasmaModifierCapability {
    @CapabilityInject(IMiasmaModifier.class)
    public static final Capability<IMiasmaModifier> CAPABILITY = injectNull();

    private static final Map<Integer, List<MiasmaModifierLocator>> modifierLocatorMap = new TreeMap<>();
    private static List<Function<LivingEntity, List<IMiasmaModifier>>> modifierLocators = new LinkedList<>();

    /** Called to register the {@link IMiasmaModifier} capability. */
    public static void register() {
        CapabilityManager.INSTANCE.register(IMiasmaModifier.class, new DefaultStorage(), () -> new SimpleMiasmaModifier(MiasmaPropertyModifiers.empty()));
    }

    /**
     * Checks the given {@link ItemStack} to see if it has the {@link IMiasmaModifier} capability attached. If so, the
     * capability instance is passed to the given {@link NonNullConsumer}.
     */
    private static void whenPresent(ItemStack itemStack, NonNullConsumer<IMiasmaModifier> consumer) {
        itemStack.getCapability(CAPABILITY).ifPresent(consumer);
    }

    /**
     * Returns all applicable {@link IMiasmaModifier}s for the given entity.
     *
     * @param entity the entity to obtain the infection modifiers for
     * @return a list all applicable modifiers for the entity
     */
    public static List<IMiasmaModifier> getModifiers(LivingEntity entity) {
        return modifierLocators.stream()
                .flatMap(f -> f.apply(entity).stream())
                .collect(Collectors.toList());
    }

    /**
     * Iterates over all modifiers for a given entity until the supplied function returns {@code false}.
     * <p>
     *     Used to call the "try" methods on {@link IMiasmaModifier} in turn, stopping if a piece returns false, blocking
     *     the event.
     * </p>
     *
     * @param entity the entity to check the modifiers of
     * @param function the function to run
     * @return {@code true} if every function call returned {@code true}
     * @see name.dashkal.minecraft.miasma.api.events.MiasmaEvent
     */
    public static boolean checkModifiersUntilFalse(LivingEntity entity, Function<IMiasmaModifier, Boolean> function) {
        for (IMiasmaModifier modifier : getModifiers(entity)) {
            if (!function.apply(modifier)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns a {@link MiasmaPropertyModifiers} that aggregates any modifiers for an entity.
     */
    public static MiasmaPropertyModifiers getTotalGearModifiers(LivingEntity entity) {
        MiasmaPropertyModifiers.Builder builder = new MiasmaPropertyModifiers.Builder();

        for (IMiasmaModifier miasmaModifier : getModifiers(entity)) {
            builder.addAll(miasmaModifier.getPropertyModifiers());
        }

        return builder.build();
    }

    /**
     * Returns a capability provider for this capability interface from the given supplier.
     */
    @Nonnull
    public static ICapabilityProvider fromSupplier(NonNullSupplier<IMiasmaModifier> supplier) {
        return new SimpleCapabilityProvider<>(() -> MiasmaModifierCapability.CAPABILITY, supplier);
    }

    /**
     * Returns a capability provider for this capability interface that allows all events and modifies the miasma
     * infection with the given modifiers.
     */
    @Nonnull
    public static ICapabilityProvider fromModifiers(MiasmaPropertyModifiers modifiers) {
        return new SimpleCapabilityProvider<>(() -> MiasmaModifierCapability.CAPABILITY, () -> new SimpleMiasmaModifier(modifiers));
    }

    /**
     * Adds a modifier locator that will be used to find {@link IMiasmaModifier} instances given an entity.
     *
     * @param miasmaModifierLocator the locator to add and use when searching for modifiers for an entity.
     */
    public static void addModifierLocator(MiasmaModifierLocator miasmaModifierLocator) {
        modifierLocatorMap.compute(miasmaModifierLocator.getPriority(), (p, ls) -> {
            List<MiasmaModifierLocator> miasmaModifierLocators = (ls == null ? new LinkedList<>() : ls);
            miasmaModifierLocators.add(miasmaModifierLocator);
            return miasmaModifierLocators;
        });
        modifierLocators = modifierLocatorMap.values().stream()
                .flatMap(Collection::stream)
                .map(MiasmaModifierLocator::getLocatorFunction)
                .collect(Collectors.toList());
    }

    /**
     * Adds the default Miasma Modifier.
     * <p>
     *     Held items are checked at priority 0.<br/>
     *     Armor pieces are checked at priority 100.<br/>
     * </p>
     * <p>
     *     Curios is added later via the Integrations Module at priority 200.
     * </p>
     */
    public static void addDefaultLocators() {
        // Main and Off hands
        addModifierLocator(new MiasmaModifierLocator(
                new ResourceLocation(MiasmaMod.MODID, "mod_held"),
                0,
                entity -> {
                    List<IMiasmaModifier> modifiers = new LinkedList<>();
                    whenPresent(entity.getMainHandItem(), modifiers::add);
                    whenPresent(entity.getOffhandItem(), modifiers::add);
                    return modifiers;
                }
        ));

        // Armor
        addModifierLocator(new MiasmaModifierLocator(
                new ResourceLocation(MiasmaMod.MODID, "mod_armor"),
                100,
                entity -> {
                    List<IMiasmaModifier> modifiers = new LinkedList<>();
                    for (ItemStack is : entity.getArmorSlots()) {
                        whenPresent(is, modifiers::add);
                    }
                    return modifiers;
                }
        ));
    }

    // https://stackoverflow.com/questions/46512161/disable-constant-conditions-exceptions-inspection-for-field-in-intellij-idea
    @Nonnull
    @SuppressWarnings("ConstantConditions")
    private static <T> T injectNull() { return null; }

    private static class DefaultStorage implements Capability.IStorage<IMiasmaModifier> {
        @Nullable
        @Override
        public INBT writeNBT(Capability<IMiasmaModifier> capability, IMiasmaModifier instance, Direction side) { return null; }

        @Override
        public void readNBT(Capability<IMiasmaModifier> capability, IMiasmaModifier instance, Direction side, INBT nbt) { }
    }

    private static class SimpleMiasmaModifier implements IMiasmaModifier {
        private final MiasmaPropertyModifiers modifiers;

        public SimpleMiasmaModifier(MiasmaPropertyModifiers modifiers) {
            this.modifiers = modifiers;
        }

        @Override
        public boolean checkApply(LivingEntity entity, InfectionStage stage, boolean forced) {
            return true;
        }

        @Override
        public boolean checkPulse(LivingEntity entity, IInfection infection) {
            return true;
        }

        @Override
        public boolean checkKill(LivingEntity entity, IInfection infection) {
            return true;
        }

        @Nonnull
        @Override
        public MiasmaPropertyModifiers getPropertyModifiers() {
            return modifiers;
        }
    }
}
