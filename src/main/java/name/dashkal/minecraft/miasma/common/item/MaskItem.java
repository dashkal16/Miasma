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
package name.dashkal.minecraft.miasma.common.item;

import name.dashkal.minecraft.miasma.MiasmaMod;
import name.dashkal.minecraft.miasma.api.MiasmaAPI;
import name.dashkal.minecraft.miasma.api.capability.IMiasmaModifier;
import name.dashkal.minecraft.miasma.api.property.MiasmaPropertyModifierType;
import name.dashkal.minecraft.miasma.api.property.MiasmaPropertyModifiers;
import name.dashkal.minecraft.miasma.api.capability.PropertyOnlyMiasmaModifier;
import name.dashkal.minecraft.miasma.common.material.ClothMaskArmorMaterial;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.NonNullSupplier;
import org.apache.commons.lang3.math.Fraction;

import javax.annotation.Nullable;

/**
 * Class for mask type items to provide basic protection from the miasma.
 * <p>
 *     Can be worn in the head slot, or if curios and the integrations module are installed, the mask curio slot.
 * </p>
 */
public class MaskItem extends ArmorItem {
    public static final MaskItem CLOTH_MASK = new MaskItem(
            new ResourceLocation(MiasmaMod.MODID, "cloth_mask"),
            ClothMaskArmorMaterial.INSTANCE,
            (new MiasmaPropertyModifiers.Builder())
                .addModifier(MiasmaPropertyModifierType.CLEANSE_STAGE_TIME, Fraction.getFraction(3, 4))
                .addModifier(MiasmaPropertyModifierType.INTENSIFY_STAGE_TIME, Fraction.getFraction(5, 4))
                .build()
    );

    private final NonNullSupplier<IMiasmaModifier> miasmaModifier;

    /**
     * Creates a new mask item.
     * @param registryName the registry name for the mask item
     * @param armorMaterial the material the mask is made from
     * @param miasmaModifier a supplier yielding the miasma modifier this mask supplies as a capability
     */
    public MaskItem(ResourceLocation registryName, IArmorMaterial armorMaterial, NonNullSupplier<IMiasmaModifier> miasmaModifier) {
        super(armorMaterial, EquipmentSlotType.HEAD, (new Item.Properties()).stacksTo(1).tab(ItemGroup.TAB_COMBAT));
        this.miasmaModifier = miasmaModifier;
        setRegistryName(registryName);
    }

    /**
     * Creates a new mask item.
     * @param registryName the registry name for the mask item
     * @param armorMaterial the material the mask is made from
     * @param modifiers the miasma property modifiers this mask applies to infections
     */
    public MaskItem(ResourceLocation registryName, IArmorMaterial armorMaterial, MiasmaPropertyModifiers modifiers) {
        this(registryName, armorMaterial, () -> new PropertyOnlyMiasmaModifier(modifiers));
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
        return MiasmaAPI.getInstance().getModifierUtils().fromSupplier(miasmaModifier);
    }
}
