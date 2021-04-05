/*package meow.konc.hack.modules.misc;

import meow.konc.hack.command.Command;
import meow.konc.hack.module.Module;
import meow.konc.hack.module.ModuleManager;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Module.Info(name = "LoopPerSecond", category = Module.Category.MISC)
public class LoopPerSecond extends Module {
    private int loopcount = 0;
    public void onUpdate() {
        ++loopcount;
    }
    public void onEnable() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            if (ModuleManager.getModuleByName("LoopPerSecond").isDisabled()) {
                scheduler.shutdown();
            }
            Command.sendChatMessage("The client looped for " + loopcount + " times");
            loopcount = 0;

        }
        ,0, 1, TimeUnit.SECONDS);
    }
}*/
