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

import name.dashkal.minecraft.miasma.api.IInfection;
import name.dashkal.minecraft.miasma.api.InfectionStage;
import name.dashkal.minecraft.miasma.api.MiasmaAPI;
import name.dashkal.minecraft.miasma.api.capability.IMiasmaModifier;
import name.dashkal.minecraft.miasma.api.capability.MiasmaModifierUtils;
import name.dashkal.minecraft.miasma.api.property.MiasmaPropertyModifierType;
import name.dashkal.minecraft.miasma.api.property.MiasmaPropertyModifiers;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.apache.commons.lang3.math.Fraction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class ProtectionRingItem extends Item {
    public static final Item INSTANCE = new ProtectionRingItem();
    private static final MiasmaModifierUtils MIASMA_GEAR_UTILS = MiasmaAPI.getInstance().getModifierUtils();

    private ProtectionRingItem() {
        super((new Item.Properties()).stacksTo(1).tab(ItemGroup.TAB_COMBAT));
        setRegistryName(new ResourceLocation(MiasmaTestMod.MODID, "prot_ring"));
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack item, @Nullable World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag tooltipFlag) {
        Optional<IMiasmaModifier> cap = item.getCapability(MIASMA_GEAR_UTILS.getCapability()).resolve();
        if (cap.isPresent() && cap.get() instanceof ProtectionRingModifier) {
            tooltip.add(new StringTextComponent("Capability detected"));
        }
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
        return MIASMA_GEAR_UTILS.fromSupplier(ProtectionRingModifier::new);
    }

    private static class ProtectionRingModifier implements IMiasmaModifier {
        private final MiasmaPropertyModifiers modifiers;

        public ProtectionRingModifier() {
            MiasmaPropertyModifiers.Builder builder = new MiasmaPropertyModifiers.Builder();
            builder.addModifier(MiasmaPropertyModifierType.CLEANSE_STAGE_TIME, Fraction.ONE_HALF);
            builder.addModifier(MiasmaPropertyModifierType.INTENSIFY_STAGE_TIME, Fraction.getFraction(2, 1));
            builder.addModifier(MiasmaPropertyModifierType.DAMAGE, Fraction.ONE_HALF);
            modifiers = builder.build();
        }

        @Override
        public boolean checkApply(@Nonnull LivingEntity entity, @Nonnull InfectionStage stage, boolean forced) {
            return false;
        }

        @Override
        public boolean checkPulse(@Nonnull LivingEntity entity, @Nonnull IInfection infection) {
            return false;
        }

        @Override
        public boolean checkKill(@Nonnull LivingEntity entity, @Nonnull IInfection infection) {
            return false;
        }

        @Nonnull
        @Override
        public MiasmaPropertyModifiers getPropertyModifiers() {
            return modifiers;
        }
    }
}
