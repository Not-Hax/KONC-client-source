package meow.konc.hack.modules.hidden.util;

import meow.konc.hack.module.Module;
import meow.konc.hack.module.ModuleManager;

/**
 * @author S-B99
 * Horribly designed class for uh, running things only once.
 */
@Module.Info(name = "RunConfig", category = Module.Category.HIDDEN, showOnArray = Module.ShowOnArray.OFF, description = "Default manager for first runs")
public class RunConfig extends Module {

    public void onEnable() {
        ModuleManager.getModuleByName("HUD").setEnabled(true);
        ModuleManager.getModuleByName("ChatBot").setEnabled(true);
        ModuleManager.getModuleByName("InfoOverlay").setEnabled(true);
        ModuleManager.getModuleByName("InventoryViewer").setEnabled(true);
        ModuleManager.getModuleByName("CustomChat").setEnabled(true);
        ModuleManager.getModuleByName("CleanGUI").setEnabled(true);
        ModuleManager.getModuleByName("TabFriends").setEnabled(true);
        ModuleManager.getModuleByName("Watermark").setEnabled(true);
        ModuleManager.getModuleByName("Gui").setEnabled(true);
        ModuleManager.getModuleByName("ClientConfig").setEnabled(true);
        ModuleManager.getModuleByName("ActiveModules").setEnabled(true);
        ModuleManager.getModuleByName("FixGui").setEnabled(true);
		ModuleManager.getModuleByName("Capes").setEnabled(true);
        disable();
    }
}
