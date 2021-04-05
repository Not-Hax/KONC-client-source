package meow.konc.hack;


import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRichPresence;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import meow.konc.hack.event.events.other.PacketEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraftforge.fml.common.FMLLog;

import java.util.function.Predicate;

import static meow.konc.hack.KONCMod.MODNAME;
import static meow.konc.hack.KONCMod.MODVER;

public class DiscordRPC
{
    private static String APP_ID = "689485040832348178";
    private static club.minnced.discord.rpc.DiscordRPC rpc;
    private static DiscordRichPresence presence;
    private static boolean hasStarted;
    public static Minecraft mc;
    private static String details;
    private static String state;
    static String lastChat;
    private static int players;
    private static int maxPlayers;
    private static ServerData svr;
    private static String[] popInfo;
    private static int players2;
    private static int maxPlayers2;
    @EventHandler
    private Listener<PacketEvent.Receive> receiveListener;

    public DiscordRPC() {
        receiveListener = new Listener<PacketEvent.Receive>(event -> {
            if (event.getPacket() instanceof SPacketChat) {
                DiscordRPC.lastChat = ((SPacketChat)event.getPacket()).chatComponent.getFormattedText();
            }
        }, new Predicate[0]);
    }

    public static boolean start() {
        FMLLog.log.info("Starting Discord RPC");
        if (DiscordRPC.hasStarted) {
            return false;
        }
        DiscordRPC.hasStarted = true;
        DiscordEventHandlers handlers = new DiscordEventHandlers();
        handlers.disconnected = ((var1, var2) -> System.out.println("Discord RPC disconnected, var1: " + var1 + ", var2: " + var2));
        DiscordRPC.rpc.Discord_Initialize("689485040832348178", handlers, true, "");
        DiscordRPC.presence.startTimestamp = System.currentTimeMillis() / 1000L;
        DiscordRPC.presence.details = "Main Menu";
        DiscordRPC.presence.state = MODNAME;
        DiscordRPC.presence.largeImageKey = "logo";
        DiscordRPC.presence.largeImageText = (MODNAME + MODVER);
        /*DiscordPresence.presence.smallImageKey = "konc02";
        DiscordPresence.presence.smallImageText = MODVER + "";*/
        DiscordRPC.rpc.Discord_UpdatePresence(DiscordRPC.presence);
        new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    DiscordRPC.rpc.Discord_RunCallbacks();
                    DiscordRPC.details = "";
                    DiscordRPC.state = "";
                    DiscordRPC.players = 0;
                    DiscordRPC.maxPlayers = 0;
                    if (DiscordRPC.mc.isIntegratedServerRunning()) {
                        DiscordRPC.details = "Single Player";
                    }
                    else if (DiscordRPC.mc.getCurrentServerData() != null) {
                        DiscordRPC.svr = DiscordRPC.mc.getCurrentServerData();
                        if (!DiscordRPC.svr.serverIP.equals("")) {
                            DiscordRPC.details = "Multi Player";
                            DiscordRPC.state = DiscordRPC.svr.serverIP;
                            if (DiscordRPC.svr.populationInfo != null) {
                                DiscordRPC.popInfo = DiscordRPC.svr.populationInfo.split("/");
                                if (DiscordRPC.popInfo.length > 2) {
                                    DiscordRPC.players2 = Integer.parseInt(DiscordRPC.popInfo[0]);
                                    DiscordRPC.maxPlayers2 = Integer.parseInt(DiscordRPC.popInfo[1]);
                                }
                            }
                            if (DiscordRPC.svr.serverIP.equals("2b2t.org")) {
                                try {
                                    if (DiscordRPC.lastChat.contains("Position in queue: ")) {
                                        DiscordRPC.state = DiscordRPC.state + " in queue" + Integer.parseInt(DiscordRPC.lastChat.substring(19));
                                    }
                                }
                                catch (Throwable e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    else {
                        DiscordRPC.details = "Main Menu";
                        DiscordRPC.state = MODNAME;
                    }
                    if (!DiscordRPC.details.equals(DiscordRPC.presence.details) || !DiscordRPC.state.equals(DiscordRPC.presence.state)) {
                        DiscordRPC.presence.startTimestamp = System.currentTimeMillis() / 1000L;
                    }
                    DiscordRPC.presence.details = DiscordRPC.details;
                    DiscordRPC.presence.state = DiscordRPC.state;
                    DiscordRPC.rpc.Discord_UpdatePresence(DiscordRPC.presence);
                }
                catch (Exception e2) {
                    e2.printStackTrace();
                }
                try {
                    Thread.sleep(5000L);
                }
                catch (InterruptedException e3) {
                    e3.printStackTrace();
                }
            }
            return;
        }, "Discord-RPC-Callback-Handler").start();
        FMLLog.log.info("Discord RPC initialised succesfully");
        return true;
    }

    static {
        rpc = club.minnced.discord.rpc.DiscordRPC.INSTANCE;
        DiscordRPC.presence = new DiscordRichPresence();
        DiscordRPC.hasStarted = false;
        mc = Minecraft.getMinecraft();
    }

    public static void disable() {
        DiscordRPC.presence.state = MODNAME;
    }
}
