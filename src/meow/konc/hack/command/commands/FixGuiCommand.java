
package meow.konc.hack.command.commands;

import meow.konc.hack.command.Command;
import meow.konc.hack.command.syntax.ChunkBuilder;

public class FixGuiCommand extends Command {
    public FixGuiCommand() {
        super("fixgui", new ChunkBuilder().build());
        setDescription("Allows you to disable the automatic gui positioning");
    }

    @Override
    public void call(String[] args) {
        sendChatMessage("[" + label + "] " + "Ran");
    }
}