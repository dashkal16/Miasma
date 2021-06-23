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
package name.dashkal.minecraft.miasma.client.eventhandlers;

import name.dashkal.minecraft.miasma.MiasmaMod;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Event handler responsible for ensuring Miasma textures are inserted into the correct texture atlases.
 */
public class TextureEventHandlers {
    // The resource location of the PotionSpriteUploader textureAtlas
    private static final ResourceLocation effectsMap = new ResourceLocation("minecraft", "textures/atlas/mob_effects.png");

    public static void registerListeners() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        eventBus.addListener(TextureEventHandlers::onTextureStitchPre);
    }

    public static void onTextureStitchPre(TextureStitchEvent.Pre event) {
        if (event.getMap().location().equals(effectsMap)) {
            // Adjust the texture location since we're tapping into a SpriteUploader owned atlas
            // "textures/mob_effect/miasma_real.png" -> "mob_effect/miasma_real"
            event.addSprite(new ResourceLocation(MiasmaMod.MODID, "mob_effect/miasma_real"));
        }
    }
}
