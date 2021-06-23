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
import name.dashkal.minecraft.miasma.common.config.CommonConfig;
import name.dashkal.minecraft.miasma.lib.capability.SerializableUnsidedCapabilityProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullConsumer;
import net.minecraftforge.common.util.NonNullFunction;
import net.minecraftforge.event.AttachCapabilitiesEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Capability implementation for {@link IMiasmaHandler}.
 */
public class MiasmaHandlerCapability {
    /** Handle to the actual {@link Capability} for {@link IMiasmaHandler}. */
    @CapabilityInject(IMiasmaHandler.class)
    public static final Capability<IMiasmaHandler> CAPABILITY = injectNull();

    /** Registers the {@link IMiasmaHandler} capability. */
    public static void register() {
        CapabilityManager.INSTANCE.register(IMiasmaHandler.class, new MiasmaCapabilityStorage(), MiasmaHandler::new);
        MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, MiasmaHandlerCapability::attach);
    }

    /** Returns {@code true} if the given entity has the {@link IMiasmaHandler} capability attached. */
    public static boolean isPresent(LivingEntity entity) {
        return entity.getCapability(CAPABILITY).isPresent();
    }

    /**
     * Checks the given {@link LivingEntity} to see if it has the {@link IMiasmaHandler} capability attached. If so, the
     * capability instance is passed to the given {@link NonNullConsumer}.
     */
    public static void ifPresent(LivingEntity entity, NonNullConsumer<IMiasmaHandler> consumer) {
        entity.getCapability(CAPABILITY).ifPresent(consumer);
    }

    /**
     * Returns the {@link IMiasmaHandler} capability instance for the given entity, if present.
     */
    public static LazyOptional<IMiasmaHandler> getHandler(LivingEntity entity) {
        return entity.getCapability(CAPABILITY);
    }

    /**
     * Checks the given {@link LivingEntity} to see if it has the {@link IMiasmaHandler} capability attached. If so, the
     * capability instance is passed to the given {@link NonNullFunction}.  The result of the function call is returned
     * as an {@link Optional}.
     */
    public static <R> Optional<R> withCapability(LivingEntity entity, NonNullFunction<IMiasmaHandler, R> function) {
        return entity.getCapability(CAPABILITY).map(function);
    }

    private static final ResourceLocation RESOURCE_LOCATION = new ResourceLocation(MiasmaMod.MODID, "miasma_affected");

    /** Attaches the capability to any living entity specified in the configuration. */
    private static void attach(AttachCapabilitiesEvent<Entity> ev) {
        if (ev.getObject() instanceof LivingEntity
                && CommonConfig.INSTANCE.getSusceptibleEntityTypes().contains(ev.getObject().getType().getRegistryName())) {
            // It's safe to invoke the default instance as attach isn't called until entity creation, long after registration.
            //noinspection NullableProblems
            SerializableUnsidedCapabilityProvider provider = new SerializableUnsidedCapabilityProvider(() -> CAPABILITY, CAPABILITY::getDefaultInstance);
            ev.addCapability(RESOURCE_LOCATION, provider);
            ev.addListener(provider::invalidate);
        }
    }

    /** Default storage implementation.  Delegates to {@link MiasmaHandler}. Ignores other implementations. */
    private static class MiasmaCapabilityStorage implements Capability.IStorage<IMiasmaHandler> {
        @Nullable
        @Override
        public INBT writeNBT(Capability<IMiasmaHandler> capability, IMiasmaHandler instance, Direction side) {
            return instance.serializeNBT();
        }

        @Override
        public void readNBT(Capability<IMiasmaHandler> capability, IMiasmaHandler instance, Direction side, INBT nbt) {
            if (nbt instanceof CompoundNBT) {
                instance.deserializeNBT((CompoundNBT) nbt);
            }
        }
    }

    // https://stackoverflow.com/questions/46512161/disable-constant-conditions-exceptions-inspection-for-field-in-intellij-idea
    @Nonnull
    @SuppressWarnings("ConstantConditions")
    private static <T> T injectNull() { return null; }
}
