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

import name.dashkal.minecraft.miasma.api.imc.MiasmaModifierLocator;
import name.dashkal.minecraft.miasma.integration.common.curios.CuriosIntegration;
import name.dashkal.minecraft.miasma.lib.ModLoadedReference;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;

import java.util.LinkedList;
import java.util.List;

public class Integrations {
    public static final Integrations INSTANCE = new Integrations();

    private final ModLoadedReference<CuriosIntegration> curiosIntegration;

    private Integrations() {
        curiosIntegration = new ModLoadedReference<>("curios", () -> CuriosIntegration::new);
    }

    public void onIMCEnqueueEvent(InterModEnqueueEvent event) {
        curiosIntegration.withIntegration(CuriosIntegration::setupCurioSlots);
    }

    public List<MiasmaModifierLocator> getMiasmaModifierLocators() {
        List<MiasmaModifierLocator> locators = new LinkedList<>();
        curiosIntegration.callWithIntegration(CuriosIntegration::getMiasmaModifierLocator).ifPresent(locators::add);
        return locators;
    }
}
