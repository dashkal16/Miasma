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
package name.dashkal.minecraft.miasma;

import name.dashkal.minecraft.miasma.api.MiasmaAPIImpl;
import name.dashkal.minecraft.miasma.client.ClientSetup;
import name.dashkal.minecraft.miasma.common.CommonSetup;
import name.dashkal.minecraft.miasma.common.config.CommonConfig;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.forgespi.language.IModInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Mod class for Miasma.
 */
@Mod(MiasmaMod.MODID)
public class MiasmaMod {
    public static final String MODID = "miasma";
    private static final Logger LOGGER = LogManager.getLogger(MODID);
    private static IModInfo modInfo;

    public MiasmaMod() {
        LOGGER.info("Mod Initialization");

        // Save our mod information
        modInfo = ModLoadingContext.get().getActiveContainer().getModInfo();

        // Initialize the API
        MiasmaAPIImpl.init();

        // Configuration
        // TODO Consider moving the common config to server, so it synchronizes automatically.
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CommonConfig.INSTANCE.getConfig());

        // Common setup
        CommonSetup.init();

        // Client setup
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ClientSetup::init);
    }

    public static String getModVersion() {
        return modInfo.getVersion().toString();
    }
}
