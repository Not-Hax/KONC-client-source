package meow.konc.hack.command.commands;

import meow.konc.hack.command.Command;
import meow.konc.hack.command.syntax.ChunkBuilder;
import meow.konc.hack.module.Module;
import meow.konc.hack.module.ModuleManager;

/**
 * @author S-B99
 * Created by S-B99 on 17/02/20
 */
public class DescriptionCommand extends Command {
    public DescriptionCommand() {
        super("description", new ChunkBuilder().append("module").build(), "tooltip");
        setDescription("Prints a module's description into the chat");
    }

    @Override
    public void call(String[] args) {
        for (String s : args) {
            if (s == null)
                continue;
            Module module = ModuleManager.getModuleByName(s);
            Command.sendChatMessage(module.getChatName() + "Description: &7" + module.getDescription());
        }
    }
}
