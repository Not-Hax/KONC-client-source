package meow.konc.hack.command.commands;

import meow.konc.hack.KONCMod;
import meow.konc.hack.command.Command;
import meow.konc.hack.command.syntax.SyntaxChunk;

import java.util.Comparator;

/**
 * Created by 086 on 12/11/2017.
 */
public class CommandsCommand extends Command {

    public CommandsCommand() {
        super("commands", SyntaxChunk.EMPTY, "cmds");
        setDescription("Gives you this list of commands");
    }

    @Override
    public void call(String[] args) {
        KONCMod.getInstance().getCommandManager().getCommands().stream().sorted(Comparator.comparing(command -> command.getLabel())).forEach(command ->
                Command.sendChatMessage("&f" + Command.getCommandPrefix() + command.getLabel() + "&r ~ &7" + command.getDescription())
        );
    }
}
