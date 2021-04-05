package meow.konc.hack.modules.misc;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import meow.konc.hack.command.Command;
import meow.konc.hack.event.events.other.PacketEvent;
import meow.konc.hack.module.Module;
import meow.konc.hack.setting.Setting;
import meow.konc.hack.setting.Settings;
import net.minecraft.network.play.client.CPacketChatMessage;

import static meow.konc.hack.KONCMod.separator;
import static meow.konc.hack.command.Command.sendWarningMessage;

/**
 * Created by 086 on 8/04/2018.
 * Updated by S-B99 on 12/03/20
 */
@Module.Info(name = "CustomChat", category = Module.Category.MISC, description = "Add a custom suffix to the end of your message!", showOnArray = Module.ShowOnArray.OFF)
public class CustomChat extends Module {
    public Setting<TextMode> textMode = register(Settings.e("Message", TextMode.KONC));
    private Setting<DecoMode> decoMode = register(Settings.e("Separator", DecoMode.SEPARATOR));
    private Setting<Boolean> commands = register(Settings.b("Commands", false));
    public Setting<String> customText = register(Settings.stringBuilder("Custom Text").withValue("unchanged").withConsumer((old, value) -> {}).build());

    private enum DecoMode { SEPARATOR, CLASSIC, NONE }
    public enum TextMode { NAME, ON_TOP, KONC, CUSTOM }
    public static String[] cmdCheck = new String[]{"/", ",", ".", "-", ";", "?", "*", "^", "&", Command.getCommandPrefix()};

    private String getText(TextMode t) {
        switch (t) {
            case NAME: return "KONC'Client";
            case ON_TOP: return "KONC'Client NO TOP";
            case KONC: return "KONC";
            case CUSTOM: return customText.getValue();
            default: return "";
        }
    }

    private String getFull(DecoMode d) {
        switch (d) {
            case NONE: return " " + getText(textMode.getValue());
            case CLASSIC: return  " \u00ab " + getText(textMode.getValue()) + " \u00bb";
            case SEPARATOR: return " " + separator + " " + getText(textMode.getValue());
            default: return "";
        }
    }

    @EventHandler
    public Listener<PacketEvent.Send> listener = new Listener<>(event -> {
        if (event.getPacket() instanceof CPacketChatMessage) {
            String s = ((CPacketChatMessage) event.getPacket()).getMessage();
            if (!commands.getValue() && isCommand(s)) return;
            s += getFull(decoMode.getValue());
            if (s.length() >= 256) s = s.substring(0, 256);
            ((CPacketChatMessage) event.getPacket()).message = s;
        }
    });

    private boolean isCommand(String s) {
        for (String value : cmdCheck) {
            if (s.startsWith(value)) return true;
        }
        return false;
    }

    private static long startTime = 0;
    @Override
    public void onUpdate() {
        if (startTime == 0) startTime = System.currentTimeMillis();
        if (startTime + 5000 <= System.currentTimeMillis()) { // 5 seconds in milliseconds
            if (textMode.getValue().equals(TextMode.CUSTOM) && customText.getValue().equalsIgnoreCase("unchanged") && mc.player != null) {
                sendWarningMessage(getChatName() + " Warning: In order to use the custom " + getName() + ", please run the &7" + Command.getCommandPrefix() + "customchat&r command to change it");
            }
            startTime = System.currentTimeMillis();
        }
    }
}
