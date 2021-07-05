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
package name.dashkal.minecraft.miasma.integration.common.curios;

import name.dashkal.minecraft.miasma.api.MiasmaAPI;
import name.dashkal.minecraft.miasma.api.capability.IMiasmaModifier;
import name.dashkal.minecraft.miasma.api.imc.MiasmaModifierLocator;
import name.dashkal.minecraft.miasma.integration.MiasmaIntegrationMod;
import name.dashkal.minecraft.miasma.integration.client.curios.MaskCurio;
import name.dashkal.minecraft.miasma.lib.capability.SimpleCapabilityProvider;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.InterModComms;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotTypeMessage;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Integration module for Curios by TheIllusiveC4.
 *
 * https://www.curseforge.com/minecraft/mc-mods/curios
 */
public class CuriosIntegration {
    private static final ResourceLocation MASK_SLOT_SPRITE = new ResourceLocation(MiasmaIntegrationMod.MODID, "item/mask_slot");
    private static final ResourceLocation CAP_CURIO_MASK = new ResourceLocation(MiasmaIntegrationMod.MODID, "icurio/mask");
    private static final String MIASMA_MODID = "miasma"; // MiasmaAPI.getInstance().getMiasmaModId(); // Parallel loading. Not safe to call this in the static initializer.

    public CuriosIntegration() {
        MinecraftForge.EVENT_BUS.addGenericListener(ItemStack.class, this::attachCapabilities);
    }

    public void setupCurioSlots() {
        InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE, () ->
                new SlotTypeMessage.Builder("mask").size(1).icon(MASK_SLOT_SPRITE).cosmetic().build()
        );
    }

    public MiasmaModifierLocator getMiasmaModifierLocator() {
        return new MiasmaModifierLocator(
                new ResourceLocation(MiasmaIntegrationMod.MODID, "mod_curios"),
                200,
                this::findMiasmaModifierCurios
        );
    }

    private List<IMiasmaModifier> findMiasmaModifierCurios(LivingEntity entity) {
        return CuriosApi.getCuriosHelper().getCuriosHandler(entity).map(handler -> {
            List<IMiasmaModifier> foundCurios = new LinkedList<>();
            Map<String, ICurioStacksHandler> curios = handler.getCurios();

            for (String slotId : curios.keySet()) {
                IDynamicStackHandler stackHandler = curios.get(slotId).getStacks();

                for (int i = 0; i < stackHandler.getSlots(); i++) {
                    ItemStack is = stackHandler.getStackInSlot(i);
                    is.getCapability(MiasmaAPI.getInstance().getModifierUtils().getCapability()).ifPresent(foundCurios::add);
                }
            }

            return foundCurios;
        }).orElse(new LinkedList<>());
    }

    private void attachCapabilities(AttachCapabilitiesEvent<ItemStack> event) {
        Item item = event.getObject().getItem().getItem();
        ResourceLocation itemName = item.getRegistryName();
        if (itemName != null && itemName.getNamespace().equals(MIASMA_MODID)) {
            if (itemName.getPath().endsWith("_mask")) {
                SimpleCapabilityProvider<ICurio> provider = new SimpleCapabilityProvider<>(() -> CuriosCapability.ITEM, MaskCurio::new);
                event.addCapability(CAP_CURIO_MASK, provider);
                event.addListener(provider::invalidate);
            }
        }
    }
}
