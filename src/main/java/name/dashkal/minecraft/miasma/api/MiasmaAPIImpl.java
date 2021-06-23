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

import name.dashkal.minecraft.miasma.MiasmaMod;
import name.dashkal.minecraft.miasma.api.capability.MiasmaModifierUtils;
import name.dashkal.minecraft.miasma.api.capability.MiasmaModifierUtilsImpl;
import name.dashkal.minecraft.miasma.common.capability.IMiasmaHandler;
import name.dashkal.minecraft.miasma.common.capability.MiasmaHandlerCapability;
import name.dashkal.minecraft.miasma.common.logic.Infection;
import name.dashkal.minecraft.miasma.common.logic.MiasmaLogic;
import net.minecraft.entity.LivingEntity;

import java.util.Optional;

/**
 * Implementation class for the Miasma API.
 * <p>
 *     THIS CLASS SHOULD NOT BE ACCESSED BY API USERS!
 * </p>
 * <p>
 *     Provides API classes access into the Miasma mod code.
 * </p>
 */
public class MiasmaAPIImpl extends MiasmaAPI {
    private static final MiasmaAPI IMPL_INSTANCE = new MiasmaAPIImpl();
    private static final MiasmaModifierUtils MIASMA_GEAR_UTILS = new MiasmaModifierUtilsImpl();

    public static void init() {
        MiasmaAPI.INSTANCE = IMPL_INSTANCE;
    }

    @Override
    public String getMiasmaModId() {
        return MiasmaMod.MODID;
    }

    @Override
    public Optional<IInfection> getInfection(LivingEntity entity) {
        return MiasmaHandlerCapability.getHandler(entity).resolve().flatMap(IMiasmaHandler::getInfection).map(Infection::getSnapshot);
    }

    @Override
    public boolean tryApplyInfection(LivingEntity entity, InfectionStage stage) {
        return MiasmaHandlerCapability.withCapability(entity, miasmaHandler ->
                MiasmaLogic.tryApplyInfection(entity, miasmaHandler, stage)
        ).orElse(false);
    }

    @Override
    public boolean tryMiasmaPulse(LivingEntity entity) {
        return MiasmaHandlerCapability.getHandler(entity).resolve().flatMap(miasmaHandler ->
                miasmaHandler.getInfection().map(infection ->
                        MiasmaLogic.miasmaPulse(entity, miasmaHandler, infection)
                )
        ).orElse(false);
    }

    @Override
    public void removeInfection(LivingEntity entity) {
        MiasmaHandlerCapability.ifPresent(entity, miasmaHandler -> MiasmaLogic.removeMiasma(entity, miasmaHandler));
    }

    @Override
    public MiasmaModifierUtils getModifierUtils() {
        return MIASMA_GEAR_UTILS;
    }
}
