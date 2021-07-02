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
package name.dashkal.minecraft.miasma.common;

import name.dashkal.minecraft.miasma.MiasmaMod;
import name.dashkal.minecraft.miasma.api.imc.MiasmaModifierLocator;
import name.dashkal.minecraft.miasma.common.capability.MiasmaHandlerCapability;
import name.dashkal.minecraft.miasma.common.capability.MiasmaModifierCapability;
import name.dashkal.minecraft.miasma.common.command.MiasmaCommand;
import name.dashkal.minecraft.miasma.common.effect.MiasmaEffect;
import name.dashkal.minecraft.miasma.common.event.MiasmaEventHandlers;
import name.dashkal.minecraft.miasma.common.imc.IMCHandler;
import name.dashkal.minecraft.miasma.common.item.MaskItem;
import name.dashkal.minecraft.miasma.common.network.MiasmaChannel;
import net.minecraft.item.Item;
import net.minecraft.potion.Effect;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class responsible for the common (client and server) lifecycle management of Miasma.
 */
public class CommonSetup {
    private static final Logger LOGGER = LogManager.getLogger(MiasmaMod.MODID);

    /**
     * Common mod Initialization.
     */
    public static void init() {
        LOGGER.trace("CommonSetup.init()");

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Mod lifecycle events
        modEventBus.addListener(CommonSetup::onFMLCommonSetupEvent);
        modEventBus.addListener(CommonSetup::onInterModProcessEvent);

        // Registration events
        modEventBus.addGenericListener(Effect.class, CommonSetup::registerEffects);
        modEventBus.addGenericListener(Item.class, CommonSetup::registerItems);

        // Event handlers
        registerEventHandlers();
    }

    /**
     * Common mod setup
     */
    private static void onFMLCommonSetupEvent(FMLCommonSetupEvent event) {
        LOGGER.trace("CommonSetup.onFMLCommonSetupEvent()");

        // Capabilities
        MiasmaHandlerCapability.register();
        MiasmaModifierCapability.register();

        // Packets
        MiasmaChannel.registerPackets();

        // Commands
        MinecraftForge.EVENT_BUS.addListener(CommonSetup::registerCommands);

        // Default Miasma modifiers
        MiasmaModifierCapability.addDefaultLocators();
    }

    /**
     * Handle incoming IMC
     */
    private static void onInterModProcessEvent(InterModProcessEvent event) {
        LOGGER.trace("CommonSetup.onInterModProcessEvent()");
        IMCHandler.handleIMCMessages(event.getIMCStream());
    }

    /** Registers the Miasma effect */
    private static void registerEffects(RegistryEvent.Register<Effect> event) {
        event.getRegistry().register(MiasmaEffect.INSTANCE);
    }

    /** Registers the Miasma items */
    private static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(MaskItem.CLOTH_MASK);
    }

    /** Registers all event handlers */
    private static void registerEventHandlers() {
        MiasmaEventHandlers.registerHandlers();
    }

    /** Registers all commands */
    public static void registerCommands(RegisterCommandsEvent ev) {
        MiasmaCommand.register(ev.getDispatcher());
    }
}
