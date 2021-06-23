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
package name.dashkal.minecraft.miasma.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import name.dashkal.minecraft.miasma.api.InfectionStage;
import name.dashkal.minecraft.miasma.common.capability.IMiasmaHandler;
import name.dashkal.minecraft.miasma.common.capability.MiasmaHandlerCapability;
import name.dashkal.minecraft.miasma.common.logic.MiasmaLogic;
import name.dashkal.minecraft.miasma.common.network.MiasmaChannel;
import name.dashkal.minecraft.miasma.common.network.MiasmaDebugOverlayPacket;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Unit;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * Miasma Command
 *
 * <p>
 *     <dl>
 *         <dt><code>debug toggle</code></dt><dd>toggles the debug overlay</dd>
 *         <dt><code>debug show</code></dt><dd>shows the debug overlay</dd>
 *         <dt><code>debug hide</code></dt><dd>hides the debug overlay</dd>
 *         <dt><code>debug dumpEntity [entity]</code></dt><dd>displays debugging information for the target entity</dd>
 *         <dt><code>infect [entity]</code></dt><dd>infects the target entity with the miasma</dd>
 *         <dt><code>cleanse [entity]</code></dt><dd>cleanses the target entity of the miasma</dd>
 *     </dl>
 * </p>
 */
public class MiasmaCommand {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("miasma")
            .requires(sender -> sender.hasPermission(2)) // Op required
            .then(debugCommand)
            .then(infectCommand)
            .then(cleanseCommand)
        );
    }

    private static final ArgumentBuilder<CommandSource, LiteralArgumentBuilder<CommandSource>> debugCommand = Commands.literal("debug")
            .then(Commands.literal("toggle").executes(c -> debugOverlay(c.getSource(), MiasmaDebugOverlayPacket.Command.TOGGLE)))
            .then(Commands.literal("show").executes(c -> debugOverlay(c.getSource(), MiasmaDebugOverlayPacket.Command.SHOW)))
            .then(Commands.literal("hide").executes(c -> debugOverlay(c.getSource(), MiasmaDebugOverlayPacket.Command.HIDE)))
            .then(Commands.literal("dumpEntity")
                    .then(Commands.argument("entity", EntityArgument.entity())
                            .executes(c -> miasmaCommand(MiasmaCommand::debugDump, c.getSource(), EntityArgument.getEntity(c, "entity"), Unit.INSTANCE))));

    public static final ArgumentBuilder<CommandSource, LiteralArgumentBuilder<CommandSource>> infectCommand = Commands.literal("infect")
            .executes(c -> miasmaCommand(MiasmaCommand::infect, c.getSource(), c.getSource().getEntity(), InfectionStage.HARMING))
            .then(Commands.argument("entity", EntityArgument.entity())
                    .executes(c -> miasmaCommand(MiasmaCommand::infect, c.getSource(), EntityArgument.getEntity(c, "entity"), InfectionStage.HARMING)));

    public static final ArgumentBuilder<CommandSource, LiteralArgumentBuilder<CommandSource>> cleanseCommand = Commands.literal("cleanse")
            .executes(c -> miasmaCommand(MiasmaCommand::cleanse, c.getSource(), c.getSource().getEntity(), Unit.INSTANCE))
            .then(Commands.argument("entity", EntityArgument.entity())
                    .executes(c -> miasmaCommand(MiasmaCommand::cleanse, c.getSource(), EntityArgument.getEntity(c, "entity"), Unit.INSTANCE)));

    private static int debugOverlay(CommandSource source, MiasmaDebugOverlayPacket.Command cmd) throws CommandSyntaxException {
        MiasmaChannel.sendToPlayerClient(source.getPlayerOrException(), new MiasmaDebugOverlayPacket(cmd));
        return 1;
    }

    private static boolean debugDump(CommandSource source, LivingEntity entity, IMiasmaHandler miasmaHandler, Unit unit) {
        miasmaHandler.getDebugReport().forEach(t -> source.sendSuccess(t, false));
        return true;
    }

    private static boolean infect(CommandSource source, LivingEntity entity, IMiasmaHandler miasmaHandler, InfectionStage stage) {
        if (MiasmaLogic.forceApplyInfection(entity, stage)) {
            source.sendSuccess(
                    new TranslationTextComponent(
                            "commands.miasma.infect.success",
                            entity.getDisplayName(),
                            new TranslationTextComponent("miasma.stage." + InfectionStage.HARMING.name().toLowerCase())
                    ),
                    true
            );
            return true;
        } else {
            source.sendFailure(new TranslationTextComponent(
                    "commands.miasma.infect.failure.immune",
                    entity.getDisplayName()
            ));
            return false;
        }
    }

    private static boolean cleanse(CommandSource source, LivingEntity entity, IMiasmaHandler miasmaHandler, Unit unit) {
        MiasmaLogic.removeMiasma(entity, miasmaHandler);
        source.sendSuccess(new TranslationTextComponent("commands.miasma.cleanse.success", entity.getDisplayName()), true);
        return true;
    }

    private static <A> int miasmaCommand(MiasmaCommandHandler<A> commandHandler, CommandSource source, Entity entity, A a) {
        if (entity instanceof LivingEntity) {
            boolean success = MiasmaHandlerCapability.withCapability((LivingEntity) entity, miasmaHandler ->
                commandHandler.handle(source, (LivingEntity) entity, miasmaHandler, a)
            ).orElse(false);

            if (success) return 1;

            source.sendFailure(new TranslationTextComponent("commands.miasma.failure.no_cap", entity.getDisplayName()));
            return 0;
        }
        source.sendFailure(new TranslationTextComponent("commands.miasma.failure.not_living", entity.getDisplayName()));
        return 0;
    }

    private interface MiasmaCommandHandler<A> {
        boolean handle(CommandSource source, LivingEntity entity, IMiasmaHandler miasmaHandler, A a);
    }
}
