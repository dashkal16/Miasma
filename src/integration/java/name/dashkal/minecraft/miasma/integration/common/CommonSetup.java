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
package name.dashkal.minecraft.miasma.integration.common;

import name.dashkal.minecraft.miasma.api.MiasmaAPI;
import name.dashkal.minecraft.miasma.api.imc.MiasmaModifierLocator;
import name.dashkal.minecraft.miasma.integration.MiasmaIntegrationMod;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CommonSetup {
    private static final Logger LOGGER = LogManager.getLogger(MiasmaIntegrationMod.MODID);

    public static void init() {
        // Initialize the integrations
        Integrations.init();

        // Lifecycle events
        FMLJavaModLoadingContext.get().getModEventBus().addListener(CommonSetup::onInterModEnqueueEvent);
    }

    private static void onInterModEnqueueEvent(InterModEnqueueEvent event) {
        MiasmaAPI api = MiasmaAPI.getInstance();
        LOGGER.trace("onInterModEnqueueEvent()");

        // Allow integrations to run their hooks
        Integrations.INSTANCE.onIMCEnqueueEvent(event);

        // Tell Miasma about our modifier locators.
        Integrations.INSTANCE.getMiasmaModifierLocators().forEach(locator ->
            InterModComms.sendTo(api.getMiasmaModId(), MiasmaModifierLocator.IMC_METHOD_ADD_MODIFIER, () -> locator)
        );
    }
}
