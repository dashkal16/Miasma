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

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import name.dashkal.minecraft.miasma.api.InfectionStage;
import net.minecraft.command.CommandSource;

import java.util.Optional;

public class InfectionStageArgument implements ArgumentType<InfectionStage> {
    private static final SimpleCommandExceptionType READER_INVALID_INFECTION_STAGE = new SimpleCommandExceptionType(new LiteralMessage("Expected 'Warning', 'Harming', or 'Killing'"));

    protected InfectionStageArgument() {}

    @Override
    public InfectionStage parse(StringReader reader) throws CommandSyntaxException {
        Optional<InfectionStage> stage = InfectionStage.fromName(reader.readUnquotedString());
        if (stage.isPresent()) {
            return stage.get();
        } else {
            throw READER_INVALID_INFECTION_STAGE.createWithContext(reader);
        }
    }

    public static InfectionStageArgument stage() {
        return new InfectionStageArgument();
    }

    public static InfectionStage getStage(CommandContext<CommandSource> context, String name) {
        return context.getArgument(name, InfectionStage.class);
    }
}
