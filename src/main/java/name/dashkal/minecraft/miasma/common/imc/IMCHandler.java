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
package name.dashkal.minecraft.miasma.common.imc;

import name.dashkal.minecraft.miasma.MiasmaMod;
import name.dashkal.minecraft.miasma.api.imc.MiasmaModifierLocator;
import name.dashkal.minecraft.miasma.common.capability.MiasmaModifierCapability;
import net.minecraftforge.fml.InterModComms;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.stream.Stream;

public class IMCHandler {
    private static final Logger LOGGER = LogManager.getLogger(MiasmaMod.MODID);

    /**
     * Handles incoming IMC messages.
     */
    public static void handleIMCMessages(Stream<InterModComms.IMCMessage> imcMessageStream) {
        imcMessageStream.forEach(message -> {
            if (message.getMethod().equals(MiasmaModifierLocator.IMC_METHOD_ADD_MODIFIER)) {
                handleAddModifierLocator(message);
            } else {
                rejectIMCMessage(message);
            }
        });
    }

    /**
     * Handles registration of miasma modifier locators.
     */
    private static void handleAddModifierLocator(InterModComms.IMCMessage message) {
        Object rawMessage = message.getMessageSupplier().get();
        if (rawMessage instanceof MiasmaModifierLocator) {
            MiasmaModifierCapability.addModifierLocator((MiasmaModifierLocator) rawMessage);
        } else {
            rejectIMCMessage(message);
        }
    }

    /**
     * Complaints to the uncaring logs when we get an unexpected message.
     */
    private static void rejectIMCMessage(InterModComms.IMCMessage message) {
        LOGGER.error("Got unexpected IMC message from {} with method {} and class {}",
                message.getSenderModId(),
                message.getMethod(),
                message.getMessageSupplier().get().getClass().getCanonicalName()
        );
    }
}
