package meow.konc.hack.modules.misc;

import meow.konc.hack.command.Command;
import meow.konc.hack.module.Module;
import meow.konc.hack.setting.Setting;
import meow.konc.hack.setting.Settings;
import net.minecraft.client.Minecraft;
import net.minecraft.world.GameType;

/**
 * Created by @S-B99 on 20/11/19
 * Yes, this is 100% original code. Go away
 */
@Module.Info(name = "FakeGamemode", description = "Fakes your current gamemode client side", category = Module.Category.MISC)
public class FakeGamemode extends Module {
    private Setting<GamemodeChanged> gamemode = register(Settings.e("Mode", GamemodeChanged.CREATIVE));
    private Setting<Boolean> disable2b = register(Settings.b("AntiKick 2b2t", true));
    private GameType gameType;

    @Override
    public void onUpdate() {
        if (mc.player == null) return;
        if (Minecraft.getMinecraft().getCurrentServerData() == null || (Minecraft.getMinecraft().getCurrentServerData() != null && Minecraft.getMinecraft().getCurrentServerData().serverIP.equalsIgnoreCase("2b2t.org"))) {
            if (mc.player.dimension == 1) {
                if (disable2b.getValue()) {
                    Command.sendWarningMessage(getChatName() + " Using this on 2b2t queue might get you kicked, please disable the AntiKick option if you're sure");
                    disable();
                }
            }
            return;
        } else if (gamemode.getValue().equals(GamemodeChanged.CREATIVE)) {
            mc.playerController.setGameType(gameType);
            mc.playerController.setGameType(GameType.CREATIVE);
        } else if (gamemode.getValue().equals(GamemodeChanged.SURVIVAL)) {
            mc.playerController.setGameType(gameType);
            mc.playerController.setGameType(GameType.SURVIVAL);
        } else if (gamemode.getValue().equals(GamemodeChanged.ADVENTURE)) {
            mc.playerController.setGameType(gameType);
            mc.playerController.setGameType(GameType.ADVENTURE);
        } else if (gamemode.getValue().equals(GamemodeChanged.SPECTATOR)) {
            mc.playerController.setGameType(gameType);
            mc.playerController.setGameType(GameType.SPECTATOR);
        }
    }

    @Override
    public void onEnable() {
        if (mc.player == null) disable();
        else gameType = mc.playerController.getCurrentGameType();
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;
        mc.playerController.setGameType(gameType);
    }

    private enum GamemodeChanged {
        SURVIVAL, CREATIVE, ADVENTURE, SPECTATOR
    }

}
