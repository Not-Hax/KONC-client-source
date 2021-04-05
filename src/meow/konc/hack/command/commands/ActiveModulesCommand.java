package meow.konc.hack.command.commands;

import meow.konc.hack.command.Command;
import meow.konc.hack.command.syntax.ChunkBuilder;
import meow.konc.hack.module.Module;
import meow.konc.hack.module.ModuleManager;
import meow.konc.hack.modules.other.ActiveModules;

import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * @author dominikaaaa
 * Updated by dominikaaaa on 05/04/20
 */
public class ActiveModulesCommand extends Command {
    public ActiveModulesCommand() {
        super("activemodules", new ChunkBuilder().append("r").append("g").append("b").append("category").build(), "activemods", "modules");
        setDescription("Allows you to customize ActiveModule's category colours");
    }

    @Override
    public void call(String[] args) {
        if (args[0] == null || args[1] == null || args[2] == null || args[3] == null) {
            return;
        }

        int argPos = 0;
        for (String arg : args) {
            argPos++;
            if (argPos < 3) {
                if (Pattern.compile("[^0-9]").matcher(arg).find()) { // this HAS to be a separate if statement otherwise it nulls
                    return;
                }
            }
        }

        ActiveModules am = (ActiveModules) ModuleManager.getModuleByName("ActiveModules");
        switch (args[3].toLowerCase()) {
            case "crystal":
                am.crystal.setValue(args[0] + "," + args[1] + "," + args[2]);
                sendChatMessage("Set " + am.crystal.getName() + " colour to " + args[0] + " " + args[1] + " " + args[2]);
                return;
            case "combat":
                am.combat.setValue(args[0] + "," + args[1] + "," + args[2]);
                sendChatMessage("Set " + am.combat.getName() + " colour to " + args[0] + " " + args[1] + " " + args[2]);
                return;
            case "experimental":
                am.experimental.setValue(args[0] + "," + args[1] + "," + args[2]);
                sendChatMessage("Set " + am.experimental.getName() + " colour to " + args[0] + " " + args[1] + " " + args[2]);
                return;
            case "other":
                am.other.setValue(args[0] + "," + args[1] + "," + args[2]);
                sendChatMessage("Set " + am.other.getName() + " colour to " + args[0] + " " + args[1] + " " + args[2]);
                return;
            case "render":
                am.render.setValue(args[0] + "," + args[1] + "," + args[2]);
                sendChatMessage("Set " + am.render.getName() + " colour to " + args[0] + " " + args[1] + " " + args[2]);
                return;
            case "player":
                am.player.setValue(args[0] + "," + args[1] + "," + args[2]);
                sendChatMessage("Set " + am.player.getName() + " colour to " + args[0] + " " + args[1] + " " + args[2]);
                return;
            case "movement":
                am.movement.setValue(args[0] + "," + args[1] + "," + args[2]);
                sendChatMessage("Set " + am.movement.getName() + " colour to " + args[0] + " " + args[1] + " " + args[2]);
                return;
            case "misc":
                am.misc.setValue(args[0] + "," + args[1] + "," + args[2]);
                sendChatMessage("Set " + am.misc.getName() + " colour to " + args[0] + " " + args[1] + " " + args[2]);
                return;
            default:
                sendErrorMessage("Category '" + args[3] + "' not found! Valid categories: \n" + Arrays.toString(Arrays.stream(Module.Category.values()).filter(Module.Category::isHidden).toArray()));
        }

    }
}
