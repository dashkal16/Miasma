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
package name.dashkal.minecraft.miasma.testmod;

import name.dashkal.minecraft.miasma.api.MiasmaAPI;
import name.dashkal.minecraft.miasma.api.events.MiasmaEvent;
import name.dashkal.minecraft.miasma.api.property.MiasmaPropertyModifierType;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.commons.lang3.math.Fraction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.theillusivec4.curios.api.SlotTypeMessage;

/**
 * Content and event handlers for MiasmaTestMod.
 */
public class TestModContent {
    private static final Logger LOGGER = LogManager.getLogger(MiasmaTestMod.MODID);

    public TestModContent() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addGenericListener(Item.class, this::registerItems);
        modEventBus.addListener(this::onFMLCommonSetupEvent);
        modEventBus.addListener(this::onInterModEnqueueEvent);
    }

    private void registerItems(RegistryEvent.Register<Item> event) {
        LOGGER.info("Registering Items");
        event.getRegistry().register(ProtectionRingItem.INSTANCE);
        event.getRegistry().register(ProtectionHelmetItem.INSTANCE);
    }

    private void onFMLCommonSetupEvent(FMLCommonSetupEvent event) {
        LOGGER.info("Registering Event Handlers");
        IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;
        forgeEventBus.addListener(this::onMiasmaPreApplyEvent);
        forgeEventBus.addListener(this::onMiasmaPrePulseEvent);
        forgeEventBus.addListener(this::onMiasmaKillEvent);
    }

    private void onInterModEnqueueEvent(InterModEnqueueEvent event) {
        LOGGER.info("Registering Curios");
        InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE, () -> new SlotTypeMessage.Builder("ring").size(2).build());
    }

    private void onMiasmaPreApplyEvent(MiasmaEvent.InfectionPreApplyEvent event) {
        if (event.getEntityLiving().getMainHandItem().getItem() == Items.STICK) {
            event.setCanceled(true);
        } else if (event.getEntityLiving().getMainHandItem().getItem() == Items.IRON_INGOT) {
            event.addModifier(MiasmaPropertyModifierType.INTENSIFY_STAGE_TIME, Fraction.getFraction(1, 10));
        }
    }

    private void onMiasmaPrePulseEvent(MiasmaEvent.InfectionPrePulseEvent event) {
        if (event.getEntityLiving().getMainHandItem().getItem() == Items.STICK) {
            event.setCanceled(true);
        } else if (event.getEntityLiving().getMainHandItem().getItem() == Items.IRON_INGOT) {
            event.addModifier(MiasmaPropertyModifierType.INTENSIFY_STAGE_TIME, Fraction.getFraction(1, 10));
        } else if (event.getEntityLiving().getMainHandItem().getItem() == Items.GOLD_INGOT) {
            // What happens if we misbehave badly
            MiasmaAPI.getInstance().removeInfection(event.getEntityLiving());
            event.setCanceled(true);
        }
    }

    private void onMiasmaKillEvent(MiasmaEvent.InfectionKillEvent event) {
        if (event.getEntityLiving().getMainHandItem().getItem() == Items.STICK) {
            event.setCanceled(true);
        } else if (event.getEntityLiving().getMainHandItem().getItem() == Items.IRON_INGOT) {
            event.setCanceled(true);
        }
    }
}
