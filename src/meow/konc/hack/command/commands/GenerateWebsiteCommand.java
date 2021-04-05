package meow.konc.hack.command.commands;

import meow.konc.hack.KONCMod;
import meow.konc.hack.command.Command;
import meow.konc.hack.module.Module;
import meow.konc.hack.module.ModuleManager;

import java.util.ArrayList;
import java.util.List;

/**
 * @author S-B99
 * Updated by S-B99 on 18/03/20
 */
public class GenerateWebsiteCommand extends Command {
    public GenerateWebsiteCommand() {
        super("genwebsite", null);
        setDescription("Generates the module page for the website");
    }

    private static String nameAndDescription(Module module) {
        return "<li>" + module.getName() + "<p><i>" + module.getDescription() + "</i></p></li>";
    }

    @Override
    public void call(String[] args) {
        List<Module> mods = new ArrayList<>(ModuleManager.getModules());
        String[] modCategories = new String[]{"Chat", "Combat", "Gui", "Misc", "Movement", "Player", "Render", "Utils"};
        List<String> modCategoriesList = new ArrayList<>(java.util.Arrays.asList(modCategories));

        List<String> modsChat = new ArrayList<>();
        List<String> modsCombat = new ArrayList<>();
        List<String> modsGui = new ArrayList<>();
        List<String> modsMisc = new ArrayList<>();
        List<String> modsMovement = new ArrayList<>();
        List<String> modsPlayer = new ArrayList<>();
        List<String> modsRender = new ArrayList<>();
        List<String> modsUtils = new ArrayList<>();

        mods.forEach(module -> {
            switch (module.getCategory()) {
                case COMBAT:
                    modsCombat.add(nameAndDescription(module));
                case OTHER:
                    modsGui.add(nameAndDescription(module));
                case MISC:
                    modsMisc.add(nameAndDescription(module));
                case MOVEMENT:
                    modsMovement.add(nameAndDescription(module));
                case PLAYER:
                    modsPlayer.add(nameAndDescription(module));
                case RENDER:
                    modsRender.add(nameAndDescription(module));
            }
        });

        modCategoriesList.forEach(modCategory -> {
            KONCMod.log.info("<details>");
            KONCMod.log.info("    <summary>" + modCategory + "</summary>");
            KONCMod.log.info("    <p><ul>");
            mods.forEach(module -> {
                if (module.getCategory().toString().equalsIgnoreCase(modCategory)) {
                    KONCMod.log.info("        <li>" + module.getName() + "<p><i>" + module.getDescription() + "</i></p></li>");
                }
            });
            KONCMod.log.info("    </ul></p>");
            KONCMod.log.info("</details>");

        });

        Command.sendChatMessage(getLabel().substring(0, 1).toUpperCase() + getLabel().substring(1) + ": Generated website to log file!");
    }
}
