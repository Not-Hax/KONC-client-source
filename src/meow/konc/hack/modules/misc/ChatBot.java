package meow.konc.hack.modules.misc;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import meow.konc.hack.event.events.other.PacketEvent;
import meow.konc.hack.module.Module;
import meow.konc.hack.setting.Setting;
import meow.konc.hack.setting.Settings;
import meow.konc.hack.util.other.Friends;
import meow.konc.hack.util.other.LagCompensator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.network.play.server.SPacketChat;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Module.Info(name = "ChatBot", category = Module.Category.MISC, description = "uwu", showOnArray = Module.ShowOnArray.OFF)
public class ChatBot extends Module {

    //public Setting<Boolean> icegay = register(Settings.b("Hello everyone", false));

    private final Pattern CHAT_PATTERN = Pattern.compile("<.*?> ");
    private final Pattern CHAT_PATTERN2 = Pattern.compile("(.*?)");

    @EventHandler
    private Listener<PacketEvent.Receive> receiveListener = new Listener<>(event -> {
        if (event.getPacket() instanceof SPacketChat) {
            String s = ((SPacketChat) event.getPacket()).getChatComponent().getUnformattedText();

            Matcher matcher = CHAT_PATTERN.matcher(s);
            String username = "unnamed";
            Matcher matcher2 = CHAT_PATTERN2.matcher(s);
            if (matcher2.find()) {
                matcher2.group();
                s = matcher2.replaceFirst("");
            }
            if (matcher.find()) {
                username = matcher.group();
                username = username.substring(1, username.length() - 2);
                s = matcher.replaceFirst("");
            }
            /*if (s.toLowerCase().contains("notallowice") && icegay.getValue() && !s.startsWith("!")) {
                mc.player.connection.sendPacket(new CPacketChatMessage("Hello everyone awa"));
            }*/

            StringBuilder builder = new StringBuilder();
            if (!s.startsWith("!")) {
                return;
            }
            s = s.substring(Math.min(s.length(), 1));
            if (s.startsWith("ping")) {
                s = s.substring(Math.min(s.length(), 5));
                ArrayList<NetworkPlayerInfo> infoMap = new ArrayList<NetworkPlayerInfo>(Minecraft.getMinecraft().getConnection().getPlayerInfoMap());

                for (Entity qwq : mc.world.loadedEntityList) {
                    if (qwq instanceof EntityPlayer) {
                        if (s.contains(qwq.getName())) {
                            s = qwq.getName();
                        }
                    }

                }
                String finalS = s;
                NetworkPlayerInfo profile = infoMap.stream().filter(networkPlayerInfo -> finalS.toLowerCase().contains(networkPlayerInfo.getGameProfile().getName().toLowerCase())).findFirst().orElse(null);
                if (profile != null) {
                    final StringBuilder message = new StringBuilder();
                    message.append(profile.getGameProfile().getName());
                    message.append("'s ping is ");
                    message.append(profile.getResponseTime());
                    message.append(" uwu");
                    String messageSanitized = message.toString().replaceAll("§", "");
                    if (messageSanitized.length() > 255) {
                        messageSanitized = messageSanitized.substring(0, 255);
                    }
                    mc.player.connection.sendPacket(new CPacketChatMessage(messageSanitized));
                }

            } else if (s.startsWith("myping")) { //myping
                ArrayList<NetworkPlayerInfo> infoMap = new ArrayList<NetworkPlayerInfo>(Minecraft.getMinecraft().getConnection().getPlayerInfoMap());
                String finalUsername = username;
                NetworkPlayerInfo profile = infoMap.stream().filter(networkPlayerInfo -> networkPlayerInfo.getGameProfile().getName().equalsIgnoreCase(finalUsername)).findFirst().orElse(null);
                if (profile != null) {
                    final StringBuilder message = new StringBuilder();
                    message.append("Your ping is ");
                    message.append(profile.getResponseTime());
                    message.append(" uwu");
                    String messageSanitized = message.toString().replaceAll("§", "");
                    if (messageSanitized.length() > 255) {
                        messageSanitized = messageSanitized.substring(0, 255);
                    }
                    mc.player.connection.sendPacket(new CPacketChatMessage(messageSanitized));
                }

            } else if (s.startsWith("tps")) { //tps
                final StringBuilder message = new StringBuilder();
                message.append("The tps is now ");
                message.append(LagCompensator.INSTANCE.getTickRate());
                message.append(" uwu");
                String messageSanitized = message.toString().replaceAll("§", "");
                if (messageSanitized.length() > 255) {
                    messageSanitized = messageSanitized.substring(0, 255);
                }
                mc.player.connection.sendPacket(new CPacketChatMessage(messageSanitized));

            } else if (s.startsWith("ouo")) { //ouo
                s = s.substring(Math.min(s.length(), 4));
                ArrayList<NetworkPlayerInfo> infoMap = new ArrayList<NetworkPlayerInfo>(Minecraft.getMinecraft().getConnection().getPlayerInfoMap());

                for (Entity qwq : mc.world.loadedEntityList) {
                    if (qwq instanceof EntityPlayer) {
                        if (s.contains(qwq.getName())) {
                            s = qwq.getName();
                        }
                    }
                }
                String finalS = s;
                NetworkPlayerInfo profile = infoMap.stream().filter(networkPlayerInfo -> finalS.toLowerCase().contains(networkPlayerInfo.getGameProfile().getName().toLowerCase())).findFirst().orElse(null);
                if (profile != null) {
                    final StringBuilder message = new StringBuilder();
                    message.append(username);
                    message.append(" uwu'd at ");
                    message.append(profile.getGameProfile().getName());
                    String messageSanitized = message.toString().replaceAll("§", "");
                    if (messageSanitized.length() > 255) {
                        messageSanitized = messageSanitized.substring(0, 255);
                    }
                    mc.player.connection.sendPacket(new CPacketChatMessage(messageSanitized));
                }

            } else if (s.startsWith("help")) { //help
                String uwu = "The commands are : tps, myping, ping playername, fight playername, ouo playername ";
                String messageSanitized = uwu.replaceAll("§", "");
                if (messageSanitized.length() > 255) {
                    messageSanitized = messageSanitized.substring(0, 255);
                }
                mc.player.connection.sendPacket(new CPacketChatMessage(messageSanitized));

            } else if (s.startsWith("fight")) { //fight
                s = s.substring(Math.min(s.length(), 6));
                ArrayList<NetworkPlayerInfo> infoMap = new ArrayList<NetworkPlayerInfo>(Minecraft.getMinecraft().getConnection().getPlayerInfoMap());

                for (Entity qwq : mc.world.loadedEntityList) {
                    if (qwq instanceof EntityPlayer) {
                        if (s.contains(qwq.getName())) {
                            s = qwq.getName();
                        }
                    }
                }
                String finalS = s;
                NetworkPlayerInfo profile = infoMap.stream().filter(networkPlayerInfo -> finalS.toLowerCase().contains(networkPlayerInfo.getGameProfile().getName().toLowerCase())).findFirst().orElse(null);
                if (profile != null) {
                    final StringBuilder message = new StringBuilder();
                    message.append(username);
                    message.append(" fighted with ");
                    message.append(profile.getGameProfile().getName() + "! ");
                    message.append(Friends.uwu.contains(username) ? username : Math.ceil(Math.random() * 2) == 1 ? username : profile.getGameProfile().getName());
                    message.append(" won!");
                    String messageSanitized = message.toString().replaceAll("§", "");
                    if (messageSanitized.length() > 255) {
                        messageSanitized = messageSanitized.substring(0, 255);
                    }
                    mc.player.connection.sendPacket(new CPacketChatMessage(messageSanitized));
                }

            } /*else if (s.startsWith("gay")) {
                s = s.substring(Math.min(s.length(), 4));
                ArrayList<NetworkPlayerInfo> infoMap = new ArrayList<NetworkPlayerInfo>(Minecraft.getMinecraft().getConnection().getPlayerInfoMap());

                for (Entity qwq : mc.world.loadedEntityList) {
                    if (qwq instanceof EntityPlayer) {
                        if (s.contains(qwq.getName())) {
                            s = qwq.getName();
                        }
                    }
                }
                String finalS = s;
                NetworkPlayerInfo profile = infoMap.stream().filter(networkPlayerInfo -> finalS.toLowerCase().contains(networkPlayerInfo.getGameProfile().getName().toLowerCase())).findFirst().orElse(null);
                if (profile != null) {
                    mc.player.connection.sendPacket(new CPacketChatMessage(profile.getGameProfile().getName() + " is " + ((!profile.getGameProfile().getName().equalsIgnoreCase("icecreammnn") || (!profile.getGameProfile().getName().equalsIgnoreCase("notallowice") || (!profile.getGameProfile().getName().equalsIgnoreCase("antichainpop"))) ? Math.random() * 100 : 100) + "% gay")));
                }

            } else {
                String uwu = "Sorry, I cant understand this command";
                String messageSanitized = uwu.replaceAll("§", "");
                if (messageSanitized.length() > 255) {
                    messageSanitized = messageSanitized.substring(0, 255);
                }
                mc.player.connection.sendPacket(new CPacketChatMessage(messageSanitized));
            }*/
        }
    });
}
