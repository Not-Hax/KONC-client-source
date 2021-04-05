package meow.konc.hack.modules.hidden.util;

import meow.konc.hack.command.Command;
import meow.konc.hack.module.Module;

@Module.Info(name = "HoleChangeInfo", category = Module.Category.UTIL, description = "tell u hole changes")
public class HoleDetect extends Module {

	public static boolean inhole;
	public static boolean lastinhole;

	public void onUpdate() {
		if (!inhole && lastinhole) {
			Command.sendChatMessage("\u00a7cYou are now not in a hole!");
		}
		if(inhole && !lastinhole) {
			Command.sendChatMessage("\u00A7aYou are now in a hole!");
		}
	}
}
		   