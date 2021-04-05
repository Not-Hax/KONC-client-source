package meow.konc.hack.modules.misc;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import meow.konc.hack.command.Command;
import meow.konc.hack.event.events.other.PacketEvent;
import meow.konc.hack.module.Module;
import meow.konc.hack.setting.Setting;
import meow.konc.hack.setting.Settings;
import meow.konc.hack.util.colour.ColourTextFormatting;
import meow.konc.hack.util.util.TimeUtil;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.util.text.TextFormatting;

import static meow.konc.hack.util.colour.ColourTextFormatting.toTextMap;

@Module.Info(name = "ChatTimestamp", category = Module.Category.MISC, description = "Shows the time a message was sent beside the message", showOnArray = Module.ShowOnArray.OFF)
public class ChatTimestamp extends Module {
    private Setting<ColourTextFormatting.ColourCode> firstColour = register(Settings.e("First Colour", ColourTextFormatting.ColourCode.GRAY));
    private Setting<ColourTextFormatting.ColourCode> secondColour = register(Settings.e("Second Colour", ColourTextFormatting.ColourCode.WHITE));
    private Setting<TimeUtil.TimeType> timeTypeSetting = register(Settings.e("Time Format", TimeUtil.TimeType.HHMM));
    private Setting<TimeUtil.TimeUnit> timeUnitSetting = register(Settings.e("Time Unit", TimeUtil.TimeUnit.H12));
    private Setting<Boolean> doLocale = register(Settings.b("Show AMPM", true));

    @EventHandler
    public Listener<PacketEvent.Receive> listener = new Listener<>(event -> {
        if (mc.player == null || isDisabled()) {
            return;
        }

        if (!(event.getPacket() instanceof SPacketChat)) {
            return;
        }
        SPacketChat sPacketChat = (SPacketChat) event.getPacket();

        if (addTime(sPacketChat.getChatComponent().getUnformattedText())) {
            event.cancel();
        }
    });

    private boolean addTime(String message) {
        Command.sendRawChatMessage("<" + TimeUtil.getFinalTime(setToText(secondColour.getValue()), setToText(firstColour.getValue()), timeUnitSetting.getValue(), timeTypeSetting.getValue(), doLocale.getValue()) + TextFormatting.RESET + "> " + message);
        return true;
    }

    private TextFormatting setToText(ColourTextFormatting.ColourCode colourCode) {
        return toTextMap.get(colourCode);
    }
}