package meow.konc.hack.modules.misc;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import meow.konc.hack.event.events.other.PacketEvent;
import meow.konc.hack.module.Module;
import meow.konc.hack.setting.Setting;
import meow.konc.hack.setting.Settings;
import net.minecraft.network.play.client.CPacketChatMessage;

import java.util.Random;

import static meow.konc.hack.util.other.InfoCalculator.isNumberEven;

/**
 * @author S-B99
 * Updated by S-B99 on 12/03/20
 */
@Module.Info(name = "FancyChat", category = Module.Category.MISC, description = "Makes messages you send fancy", showOnArray = Module.ShowOnArray.OFF)
public class FancyChat extends Module {
    private Setting<Boolean> uwu = register(Settings.b("uwu", true));
    private Setting<Boolean> leet = register(Settings.b("1337", false));
    private Setting<Boolean> mock = register(Settings.b("mOcK", false));
    private Setting<Boolean> green = register(Settings.b(">", false));
    private Setting<Boolean> randomSetting = register(Settings.booleanBuilder("Random Case").withValue(true).withVisibility(v -> mock.getValue()).build());

    private static Random random = new Random();

    private String getText(String s) {
        if (uwu.getValue()) {
            s = uwuConverter(s);
        }
        if (leet.getValue()) {
            s = leetConverter(s);
        }
        if (mock.getValue()) {
            s = mockingConverter(s);
        }
        if (green.getValue()) {
            s = greenConverter(s);
        }
        return s;
    }

    private String greenConverter(String input) {
        return "> " + input;
    }

    @EventHandler
    public Listener<PacketEvent.Send> listener = new Listener<>(event -> {
        if (event.getPacket() instanceof CPacketChatMessage) {
            String s = ((CPacketChatMessage) event.getPacket()).getMessage();
            s = getText(s);
            if (s.length() >= 256) {
                s = s.substring(0, 256);
            }
            ((CPacketChatMessage) event.getPacket()).message = s;
        }
    });

    @Override
    public String getHudInfo() {
        StringBuilder returned = new StringBuilder();
        if (uwu.getValue()) {
            returned.append("uwu");
        }
        if (leet.getValue()) {
            returned.append(" 1337");
        }
        if (mock.getValue()) {
            returned.append(" mOcK");
        }
        if (green.getValue()) {
            returned.append(" >");
        }
        return returned.toString();
    }

    private String leetConverter(String input) {
        StringBuilder message = new StringBuilder();
        for (int i = 0 ; i < input.length() ; i++) {
            String inputChar = input.charAt(i) + "";
            inputChar = inputChar.toLowerCase();
            inputChar = leetSwitch(inputChar);
            message.append(inputChar);
        }
        return message.toString();
    }

    private String mockingConverter(String input) {
        StringBuilder message = new StringBuilder();
        for (int i = 0 ; i < input.length() ; i++) {
            String inputChar = input.charAt(i) + "";

            int rand = 0;
            if (randomSetting.getValue()) {
                rand = random.nextBoolean() ? 1 : 0;
            }

            if (!isNumberEven(i + rand)) {
                inputChar = inputChar.toUpperCase();
            } else {
                inputChar = inputChar.toLowerCase();
            }
            message.append(inputChar);
        }
        return message.toString();
    }

    private String uwuConverter(String input) {
        input = input.replace("ove", "uv");
        input = input.replace("the", "da");
        input = input.replace("is", "ish");
        input = input.replace("r", "w");
        input = input.replace("ve", "v");
        input = input.replace("l", "w");
        return input;
    }

    private String leetSwitch(String i) {
        switch (i) {
            case "a":
                return "4";
            case "e":
                return "3";
            case "g":
                return "6";
            case "l":
            case "i":
                return "1";
            case "o":
                return "0";
            case "s":
                return "$";
            case "t":
                return "7";
            default: return i;
        }
    }
}
