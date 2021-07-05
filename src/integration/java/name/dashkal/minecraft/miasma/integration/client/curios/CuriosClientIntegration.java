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
package name.dashkal.minecraft.miasma.integration.client.curios;

import name.dashkal.minecraft.miasma.integration.MiasmaIntegrationMod;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.commons.lang3.tuple.ImmutablePair;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICurio;

import java.util.Optional;

/**
 * Integration module (Client Side) for Curios by TheIllusiveC4.
 *
 * https://www.curseforge.com/minecraft/mc-mods/curios
 */
public class CuriosClientIntegration {
    private static final ResourceLocation MASK_SLOT_SPRITE = new ResourceLocation(MiasmaIntegrationMod.MODID, "item/mask_slot");

    public void onClientSetup(FMLClientSetupEvent event) {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onPreTextureStitchEvent);
    }

    private void onPreTextureStitchEvent(TextureStitchEvent.Pre event) {
        if (event.getMap().location().equals(PlayerContainer.BLOCK_ATLAS)) {
            event.addSprite(MASK_SLOT_SPRITE);
        }
    }

    /**
     * Finds the the ItemStack for a visible (rendering) curio, given an entity, slot identifier, and slot index.
     */
    public static Optional<ItemStack> findRenderingItemStack(LivingEntity livingEntity, String identifier, int slot) {
        return CuriosApi.getCuriosHelper().getCuriosHandler(livingEntity).resolve().flatMap(h -> h.getStacksHandler(identifier)).flatMap(sh -> {
            if (slot >= 0 && sh.getSlots() > slot) {
                if (sh.getRenders().get(slot)) {
                    ItemStack cosmetic = sh.getCosmeticStacks().getStackInSlot(slot);
                    if (cosmetic != ItemStack.EMPTY) {
                        return Optional.of(cosmetic);
                    }
                    ItemStack active = sh.getStacks().getStackInSlot(slot);
                    if (active != ItemStack.EMPTY) {
                        return Optional.of(active);
                    }
                }
            }
            return Optional.empty();
        });
    }
}
