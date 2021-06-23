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
package name.dashkal.minecraft.miasma.api.imc;

import name.dashkal.minecraft.miasma.api.capability.IMiasmaModifier;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Description of a {@link IMiasmaModifier} locator.
 * <p>
 *     During {@link net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent}, send an instance of this class to the
 *     {@link MiasmaModifierLocator#IMC_METHOD_ADD_MODIFIER} method to register this locator.
 * </p>
 * <p>
 *     Example:
 *     <pre>
 *     private void onInterModEnqueueEvent(InterModEnqueueEvent event) {
 *         InterModComms.sendTo(
 *             "miasma",
 *             ModifierLocator.IMC_METHOD,
 *             new ModifierLocator(
 *                 new ResourceLocation(
 *                     MyMod.MODID,
 *                     "my_miasma_mods"
 *                 ),
 *                 250,
 *                 entity -> getModifiers(entity)
 *             )
 *         );
 *     }</pre>
 * </p>
 */
public final class MiasmaModifierLocator {
    private final ResourceLocation resourceLocation;
    private final int priority;
    private final Function<LivingEntity, List<IMiasmaModifier>> locatorFunction;

    /** Method to pass to {@link net.minecraftforge.fml.InterModComms#sendTo(String, String, Supplier)} as the method argument. */
    public static final String IMC_METHOD_ADD_MODIFIER = "addModifierLocator";

    /**
     * Creates a new Modifier Locator.
     * <p>
     *     Held items are checked at priority 0.<br/>
     *     Armor pieces are checked at priority 100.<br/>
     *     If both Miasma Integrations and Curios are installed, curios are checked at priority 200.
     * </p>
     * @param resourceLocation the unique identifier of this locator
     * @param priority the priority of this locator. Lower priority values get used before higher ones.
     * @param locatorFunction a function invoked to find {@link IMiasmaModifier}s applicable to the given entity.
     */
    public MiasmaModifierLocator(ResourceLocation resourceLocation, int priority, Function<LivingEntity, List<IMiasmaModifier>> locatorFunction) {
        this.resourceLocation = resourceLocation;
        this.priority = priority;
        this.locatorFunction = locatorFunction;
    }

    /**
     * Returns the unique identifier of this locator.
     */
    public ResourceLocation getResourceLocation() {
        return resourceLocation;
    }

    /**
     * Returns the priority of this locator.  Lower priority values get used before higher ones.
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Returns the locator function of this locator.
     */
    public Function<LivingEntity, List<IMiasmaModifier>> getLocatorFunction() {
        return locatorFunction;
    }
}
