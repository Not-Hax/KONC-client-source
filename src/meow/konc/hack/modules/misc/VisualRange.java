package meow.konc.hack.modules.misc;

import com.mojang.realmsclient.gui.ChatFormatting;
import meow.konc.hack.command.Command;
import meow.konc.hack.module.Module;
import meow.konc.hack.setting.Setting;
import meow.konc.hack.setting.Settings;
import meow.konc.hack.util.other.Friends;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 26 October 2019 by hub
 * Updated 12 January 2020 by hub
 * Updated by polymer on 23/02/20
 */
@Module.Info(name = "VisualRange", description = "Shows players who enter and leave range in chat", category = Module.Category.MISC)
public class VisualRange extends Module {
    private Setting<Boolean> leaving = register(Settings.b("Leaving", false));
    private Setting<Boolean> uwuKillAura = register(Settings.b("UwU KillAura", false));

    private List<String> knownPlayers;
    
    @Override
    public void onUpdate() {
        if (mc.player == null) return;

        List<String> tickPlayerList = new ArrayList<>();

        for (Entity entity : mc.world.getLoadedEntityList()) {
            if (entity instanceof EntityPlayer) tickPlayerList.add(entity.getName());
        }

        if (tickPlayerList.size() > 0) {
            for (String playerName : tickPlayerList) {
                if (playerName.equals(mc.player.getName())) continue;

                if (!knownPlayers.contains(playerName)) {
                    knownPlayers.add(playerName);

                    if (Friends.isFriend(playerName)) {
                        sendNotification(ChatFormatting.GREEN.toString() + playerName + ChatFormatting.RESET.toString() + " entered the Battlefield!");
                    } else {
                        sendNotification(ChatFormatting.RED.toString() + playerName + ChatFormatting.RESET.toString() + " entered the Battlefield!");
                    }
                    if (uwuKillAura.getValue()) Command.sendServerMessage("/w "+ playerName + " hi uwu");

                    return;
                }
            }
        }

        if (knownPlayers.size() > 0) {
            for (String playerName : knownPlayers) {
                if (!tickPlayerList.contains(playerName)) {
                    knownPlayers.remove(playerName);

                    if (leaving.getValue()) {
                        if (Friends.isFriend(playerName)) {
                            sendNotification(ChatFormatting.GREEN.toString() + playerName + ChatFormatting.RESET.toString() + " left the Battlefield!");
                        } else {
                            sendNotification(ChatFormatting.RED.toString() + playerName + ChatFormatting.RESET.toString() + " left the Battlefield!");
                        }
                        if (uwuKillAura.getValue()) Command.sendServerMessage(("/w "+ playerName + " bye uwu"));
                    }

                    return;
                }
            }
        }

    }

    private void sendNotification(String s) {
        Command.sendChatMessage(s);
    }

    @Override
    public void onEnable() {
        this.knownPlayers = new ArrayList<>();
    }
}
