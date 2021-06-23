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
import name.dashkal.minecraft.miasma.api.capability.MiasmaModifierUtils;
import name.dashkal.minecraft.miasma.api.property.MiasmaPropertyModifierType;
import name.dashkal.minecraft.miasma.api.property.MiasmaPropertyModifiers;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.apache.commons.lang3.math.Fraction;

import javax.annotation.Nullable;

public class ProtectionHelmetItem extends ArmorItem {
    public static final Item INSTANCE = new ProtectionHelmetItem();
    private static final MiasmaModifierUtils MIASMA_GEAR_UTILS = MiasmaAPI.getInstance().getModifierUtils();

    private ProtectionHelmetItem() {
        super(ArmorMaterial.IRON, EquipmentSlotType.HEAD, (new Item.Properties()).stacksTo(1).tab(ItemGroup.TAB_COMBAT));
        setRegistryName(new ResourceLocation(MiasmaTestMod.MODID, "prot_helm"));
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
        MiasmaPropertyModifiers.Builder builder = new MiasmaPropertyModifiers.Builder();
        builder.addModifier(MiasmaPropertyModifierType.CLEANSE_STAGE_TIME, Fraction.ONE_THIRD);
        builder.addModifier(MiasmaPropertyModifierType.INTENSIFY_STAGE_TIME, Fraction.getFraction(3, 1));
        builder.addModifier(MiasmaPropertyModifierType.DAMAGE, Fraction.ONE_THIRD);
        return MIASMA_GEAR_UTILS.fromModifiers(builder.build());
    }
}
