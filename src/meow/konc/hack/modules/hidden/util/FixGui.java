package meow.konc.hack.modules.hidden.util;

import meow.konc.hack.module.Module;

import static meow.konc.hack.util.util.GuiFrameUtil.fixFrames;

@Module.Info(name = "FixGui", category = Module.Category.HIDDEN, showOnArray = Module.ShowOnArray.OFF, description = "Moves GUI elements back on screen")
public class FixGui extends Module {
    public void onUpdate() {
        if (mc.player == null) return;
        fixFrames(mc);
        disable();
    }
}