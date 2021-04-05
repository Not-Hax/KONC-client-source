/*package meow.pork.vocoshulkerpeek2;

import meow.konc.hack.command.Command;
import meow.konc.hack.module.ModuleManager;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemShulkerBox;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;

public class ShulkerPreview {
    public static int metadataTicks = -1;
    public static int guiTicks = -1;
    public static EntityItem drop;
    public static InventoryBasic toOpen;

    @SubscribeEvent
    public void onEntitySpawn(EntityJoinWorldEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof EntityItem) {
            drop = (EntityItem) entity;
            metadataTicks = 0;
        }

    }

    @SubscribeEvent
    public void onTick(ClientTickEvent event) {
        if (event.phase == Phase.END && metadataTicks > -1) {
            ++metadataTicks;
        }

        if (event.phase == Phase.END && guiTicks > -1) {
            ++guiTicks;
        }

        if (metadataTicks == 20) {
            if (Minecraft.getMinecraft().player == null) {
                return;
            }
            metadataTicks = -1;
            if (drop.getItem().getItem() instanceof ItemShulkerBox && (ModuleManager.getModuleByName("ShulkerBypass").isEnabled())) {
                Command.sendChatMessage("[ShulkerBypass] New shulker found! use /peek to view its content");
                VocoShulkerPeek2.shulker = drop.getItem();
            }
        }

        if (guiTicks == 20) {
            guiTicks = -1;
            VocoShulkerPeek2.mc.player.displayGUIChest(toOpen);
        }

    }
}*/
    //GUI-UUID
    /*private static String uuids = null;
    private static final Minecraft mc = Minecraft.getMinecraft();
    public static boolean hasAccess() {
        try {
            if (uuids == null) {
                URL url = new URL(new String(Base64.getDecoder().decode("aHR0cHM6Ly9yYXcuZ2l0aHVidXNlcmNvbnRlbnQuY29tL0tPTkNIYWNrL0tPTkNtYXRlcmlhbC9tYXN0ZXIvR1VJLVVVSUQ=")));

                // read text returned by server
                BufferedReader in = new BufferedReader (new InputStreamReader (url.openStream()));

                String line;
                while ((line = in.readLine()) != null) {
                    uuids += line;
                }
                in.close();

            }}
        catch (MalformedURLException e) {
            System.out.println("Malformed URL: " + e.getMessage());
        }
        catch (IOException e) {
            System.out.println("I/O Error: " + e.getMessage());
        }

        String uuid = mc.player.getUniqueID().toString();
        return uuids.contains(uuid);

    }

    public static boolean isExist(){return true;}
}*/
