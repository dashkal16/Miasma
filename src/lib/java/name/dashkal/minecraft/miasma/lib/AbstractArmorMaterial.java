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
package name.dashkal.minecraft.miasma.lib;

import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.LazyValue;
import net.minecraft.util.SoundEvent;

import java.util.function.Supplier;

public class AbstractArmorMaterial implements IArmorMaterial {
    private final String name;
    private final int[] durabilityBySlot;
    private final int[] defenseBySlot;
    private final int enchantmentValue;
    private final SoundEvent equipSound;
    private final float toughness;
    private final float knockbackResistance;
    private final LazyValue<Ingredient> repairIngredient;

    public AbstractArmorMaterial(String name, int[] durabilityBySlot, int[] defenseBySlot, int enchantmentValue, SoundEvent equipSound, float toughness, float knockbackResistance, Supplier<Ingredient> repairIngredient) {
        if (defenseBySlot.length != 4) {
            throw new IllegalArgumentException("defenseBySlot must have exactly 4 elements");
        }
        if (durabilityBySlot.length != 4) {
            throw new IllegalArgumentException("durabilityBySlot must have exactly 4 elements");
        }

        this.name = name;
        this.durabilityBySlot = durabilityBySlot;
        this.defenseBySlot = defenseBySlot;
        this.enchantmentValue = enchantmentValue;
        this.equipSound = equipSound;
        this.toughness = toughness;
        this.knockbackResistance = knockbackResistance;
        this.repairIngredient = new LazyValue<>(repairIngredient);
    }

    public AbstractArmorMaterial(String name, int durabilityMultiplier, int[] defenseBySlot, int enchantmentValue, SoundEvent equipSound, float toughness, float knockbackResistance, Supplier<Ingredient> repairIngredient) {
        this(
                name,
                new int[] {13 * durabilityMultiplier, 15 * durabilityMultiplier, 16 * durabilityMultiplier, 11 * durabilityMultiplier},
                defenseBySlot, enchantmentValue, equipSound, toughness, knockbackResistance, repairIngredient
        );
    }

    @Override
    public int getDurabilityForSlot(EquipmentSlotType slotType) {
        return durabilityBySlot[slotType.getIndex()];
    }

    @Override
    public int getDefenseForSlot(EquipmentSlotType slotType) {
        return defenseBySlot[slotType.getIndex()];
    }

    @Override
    public int getEnchantmentValue() {
        return enchantmentValue;
    }

    @Override
    public SoundEvent getEquipSound() {
        return equipSound;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return repairIngredient.get();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public float getToughness() {
        return toughness;
    }

    @Override
    public float getKnockbackResistance() {
        return knockbackResistance;
    }
}
