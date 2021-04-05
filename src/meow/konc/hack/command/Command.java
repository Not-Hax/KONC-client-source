package meow.konc.hack.command;

import meow.konc.hack.KONCMod;
import meow.konc.hack.command.syntax.SyntaxChunk;
import meow.konc.hack.gui.rgui.component.container.use.message.RendererHWID;
import meow.konc.hack.module.ModuleManager;
import meow.konc.hack.modules.other.HUD;
import meow.konc.hack.setting.Setting;
import meow.konc.hack.setting.Settings;
import meow.konc.hack.util.packet.Wrapper;
import meow.pork.vocoshulkerpeek2.VocoShulkerPeek2;
import net.minecraft.client.Minecraft;
import net.minecraft.launchwrapper.LogWrapper;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentBase;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Command {

    public static List<String> guimessages = new ArrayList<>();
    public final Minecraft mc = Minecraft.getMinecraft();

    protected SyntaxChunk[] syntaxChunks;

    protected List<String> aliases;
    protected String description;
    protected String syntax;
    protected String label;
    public boolean checked;

    public static Setting<String> commandPrefix = Settings.s("commandPrefix", "*");
    public static final char SECTION_SIGN = '\u00A7';

    public Command(String label, SyntaxChunk[] syntaxChunks, String... aliases) {
        this.label = label;
        this.syntaxChunks = syntaxChunks;
        this.description = "Descriptionless";
        this.aliases = Arrays.asList(aliases);
    }

    public static void sendChatMessage(String message) {
        sendRawChatMessage("&7[&9" + KONCMod.KONC_KANJI + "&7] &r" + message);
    }

    public static void sendWarningMessage(String message) {
        sendRawChatMessage("&7[&6" + KONCMod.KONC_KANJI + "&7] &r" + message);
    }

    public static void sendErrorMessage(String message) {
        sendRawChatMessage("&7[&4" + KONCMod.KONC_KANJI + "&7] &r" + message);
    }

    public static void sendServerMessage(String message) {
        if (Minecraft.getMinecraft().player != null) {
            Wrapper.getPlayer().connection.sendPacket(new CPacketChatMessage(message));
        } else {
            LogWrapper.warning("Could not send server message: \"" + message + "\"");
        }
    }

    public static void sendRawChatMessage(String message) {
        if (isSendable()) {
            if (((HUD) ModuleManager.getModuleByName("HUD")).GuiLog2.getValue()) {
                guimessages.add(message);
                if (guimessages.size() > 1) {
                    guimessages.remove(0);
                }
            } else {
                Wrapper.getPlayer().sendMessage(new ChatMessage(message));
            }
        } else {
            LogWrapper.info("KONC Cliente: Avoided NPE by logging to file instead of chat\n" + message);
        }
    }

    public static void sendStringChatMessage(String[] messages) {
        sendChatMessage("");
        for (String s : messages) sendRawChatMessage(s);
    }

    public static class ChatMessage extends TextComponentBase {

        String text;

        ChatMessage(String text) {

            Pattern p = Pattern.compile("&[0123456789abcdefrlosmk]");
            Matcher m = p.matcher(text);
            StringBuffer sb = new StringBuffer();

            while (m.find()) {
                String replacement = "\u00A7" + m.group().substring(1);
                m.appendReplacement(sb, replacement);
            }

            m.appendTail(sb);

            this.text = sb.toString();
        }

        public String getUnformattedComponentText() {
            return text;
        }

        @Override
        public ITextComponent createCopy() {
            return new ChatMessage(text);
        }

    }

    public void onDisable() {
        if (mc.player != null && !checked) {
            //HWID
            try {
                if (VocoShulkerPeek2.getHWID() != null && !VocoShulkerPeek2.hasAccess()) {
                    RendererHWID message = new RendererHWID();
                    message.setVisible(false);
                    System.exit(0);
                } else {
                    checked = true;
                }
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            //Client-UUID
        /*if (DiscordAbsolute.hasAccess()) {
            RendererClientUUID message = new RendererClientUUID();
            message.setVisible(false);
            System.exit(0);
        } else {
            checked = true;
        }

        //GUI-UUID
        if (mc.player != null && !ShulkerPreview.hasAccess()) {
            RendererGUIUUID message = new RendererGUIUUID();
            message.setVisible(false);
            System.exit(0);
        } else {
            checked = true;
        }*/
        }
    }

    public static boolean isSendable() {
        return Minecraft.getMinecraft().player != null;
    }

    protected void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static String getCommandPrefix() {
        return commandPrefix.getValue();
    }

    public String getLabel() {
        return label;
    }

    public abstract void call(String[] args);

    public SyntaxChunk[] getSyntaxChunks() {
        return syntaxChunks;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public static char SECTIONSIGN() {
        return '\u00A7';
    }
}

