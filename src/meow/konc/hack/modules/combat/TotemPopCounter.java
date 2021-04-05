package meow.konc.hack.modules.combat;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import meow.konc.hack.KONCMod;
import meow.konc.hack.command.Command;
import meow.konc.hack.event.events.other.EntityUseTotemEvent;
import meow.konc.hack.event.events.other.PacketEvent;
import meow.konc.hack.module.Module;
import meow.konc.hack.setting.Setting;
import meow.konc.hack.setting.Settings;
import meow.konc.hack.util.colour.ColourTextFormatting;
import meow.konc.hack.util.other.Friends;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.util.text.TextFormatting;

import java.util.HashMap;

import static meow.konc.hack.KONCMod.EVENT_BUS;
import static meow.konc.hack.util.colour.ColourTextFormatting.toTextMap;

@Module.Info(name = "TotemPopCounter", description = "Counts how many times players pop", category = Module.Category.COMBAT)
public class TotemPopCounter extends Module {
    private Setting<Boolean> countFriends = register(Settings.b("Count Friends", true));
    private Setting<Boolean> countSelf = register(Settings.b("Count Self", false));
    private Setting<Boolean> resetDeaths = register(Settings.b("Reset On Death", true));
    private Setting<Boolean> resetSelfDeaths = register(Settings.b("Reset Self Death", true));
    private Setting<Announce> announceSetting = register(Settings.e("Announce", Announce.CLIENT));
    private Setting<Boolean> thanksTo = register(Settings.b("Thanks to", false));
    private Setting<ColourTextFormatting.ColourCode> colourCode = register(Settings.e("Color Name", ColourTextFormatting.ColourCode.DARK_PURPLE));
    private Setting<ColourTextFormatting.ColourCode> colourCode1 = register(Settings.e("Color Number", ColourTextFormatting.ColourCode.LIGHT_PURPLE));

    private enum Announce { CLIENT, EVERYONE }
    private HashMap<String, Integer> playerList = new HashMap();
    private boolean isDead = false;

    @Override
    public void onUpdate() {
        if (!isDead
                && resetSelfDeaths.getValue()
                && 0 >= mc.player.getHealth()) {
            sendMessage(formatName(mc.player.getName()) + " died and " + grammar(mc.player.getName()) + " pop list was reset!");
            isDead = true;
            playerList.clear();
            return;
        }
        if (isDead && 0 < mc.player.getHealth()) {
            isDead = false;
        }

        for (EntityPlayer player : mc.world.playerEntities) {
            if (
                    resetDeaths.getValue()
                            && 0 >= player.getHealth()
                            && friendCheck(player.getName())
                            && selfCheck(player.getName())
                            && playerList.containsKey(player.getName())) {
                sendMessage(formatName(player.getName()) + " died after popping " + formatNumber(playerList.get(player.getName())) + " totems" + ending());
                playerList.remove(player.getName(), playerList.get(player.getName()));
            }
        }
    }

    @EventHandler
    public Listener<EntityUseTotemEvent> listListener = new Listener<>(event -> {
        if (playerList == null) {
            playerList = new HashMap<>();
        }

        if (playerList.get(event.getEntity().getName()) == null) {
            playerList.put(event.getEntity().getName(), 1);
            sendMessage(formatName(event.getEntity().getName()) + " popped " + formatNumber(1) + " totem" + ending());
        }

        else if (!(playerList.get(event.getEntity().getName()) == null)) {
            int popCounter = playerList.get(event.getEntity().getName());
            popCounter += 1;
            playerList.put(event.getEntity().getName(), popCounter);
            sendMessage(formatName(event.getEntity().getName()) + " popped " + formatNumber(popCounter) + " totems" + ending());
        }
    });

    private boolean friendCheck(String name) {
        if (isDead) {
            return false;
        }
        for (Friends.Friend names : Friends.friends.getValue()) {
            if (names.getUsername().equalsIgnoreCase(name)) {
                return countFriends.getValue();
            }
        }
        return true;
    }

    private boolean selfCheck(String name) {
        if (isDead) {
            return false;
        }
        if (countSelf.getValue() && name.equalsIgnoreCase(mc.player.getName())) {
            return true;
        }
        else if (!countSelf.getValue() && name.equalsIgnoreCase(mc.player.getName())) {
            return false;
        }
        return true;
    }

    private boolean isSelf(String name) {
        return name.equalsIgnoreCase(mc.player.getName());
    }

    private boolean isFriend(String name) {
        for (Friends.Friend names : Friends.friends.getValue()) {
            if (names.getUsername().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    private String formatName(String name) {
        String extraText = "";
        if (isFriend(name) && !isPublic()) {
            extraText = "Your friend, ";
        } else if (isFriend(name) && isPublic()) {
            extraText = "My friend, ";
        }
        if (isSelf(name)) { extraText = ""; name = "I"; }

        if (announceSetting.getValue().equals(Announce.EVERYONE)) {
            return extraText + name;
        }
        return extraText + setToText(colourCode.getValue()) + name + TextFormatting.RESET;
    }

    private String grammar(String name) {
        if (isSelf(name)) {
            return "my";
        }
        else {
            return "their";
        }
    }

    private String ending() {
        if (thanksTo.getValue()) {
            return " thanks to " + KONCMod.MODNAME + "!";
        }
        else {
            return "!";
        }
    }

    private boolean isPublic() {
        return announceSetting.getValue().equals(Announce.EVERYONE);
    }

    private String formatNumber(int message) {
        if (announceSetting.getValue().equals(Announce.EVERYONE)) {
            return "" + message;
        }
        return setToText(colourCode1.getValue()) + "" + message + TextFormatting.RESET;
    }

    private void sendMessage(String message) {
        switch (announceSetting.getValue()) {
            case CLIENT:
                Command.sendRawChatMessage(message);
                return;
            case EVERYONE:
                Command.sendServerMessage(message);
                return;
            default:
        }
    }

    @EventHandler
    public Listener<PacketEvent.Receive> popListener = new Listener<>(event -> {
        if (mc.player == null) {
            return;
        }

        if (event.getPacket() instanceof SPacketEntityStatus) {
            SPacketEntityStatus packet = (SPacketEntityStatus) event.getPacket();
            if (packet.getOpCode() == 35) {
                Entity entity = packet.getEntity(mc.world);
                if (friendCheck(entity.getName()) || selfCheck(entity.getName())) {
                    EVENT_BUS.post(new EntityUseTotemEvent(entity));
                }
            }
        }

    });

    private TextFormatting setToText(ColourTextFormatting.ColourCode colourCode) {
        return toTextMap.get(colourCode);
    }
}
