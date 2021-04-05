package meow.pork.vocoshulkerpeek2;

import meow.konc.hack.command.Command;
import meow.konc.hack.module.ModuleManager;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntityShulkerBox;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.IClientCommand;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Mod(
        modid = "vocoshulkerpeek2",
        name = "Peek Bypass for KONC hack",
        version = "1.1",
        acceptedMinecraftVersions = "[1.12.2]"
)
public class VocoShulkerPeek2 {
    public static ItemStack shulker;
    public static Minecraft mc;


    static {
        shulker = ItemStack.EMPTY;
        mc = Minecraft.getMinecraft();
    }

    @EventHandler
    public void postinit(FMLPostInitializationEvent event) {
        ClientCommandHandler.instance.registerCommand(new PeekCommand());
        //MinecraftForge.EVENT_BUS.register(new ShulkerPreview());
    }

    public static NBTTagCompound getShulkerNBT(ItemStack stack) {
        if (mc.player == null) return null;
        NBTTagCompound compound = stack.getTagCompound();
        if (compound != null && compound.hasKey("BlockEntityTag", 10)) {
            NBTTagCompound tags = compound.getCompoundTag("BlockEntityTag");
            if (ModuleManager.getModuleByName("ShulkerBypass").isEnabled()) {
                if (tags.hasKey("Items", 9)) {
                    return tags;
                } else {
                    Command.sendWarningMessage("[ShulkerBypass] Shulker is empty!");
                }
            }
        }

        return null;
    }

    public static class PeekCommand extends CommandBase implements IClientCommand {
        @Override
        public boolean allowUsageWithoutPrefix(ICommandSender sender, String message) {
            return false;
        }

        public String getName() {
            return "peek";
        }

        public String getUsage(ICommandSender sender) {
            return null;
        }

        @Override
        public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
            if (mc.player != null && ModuleManager.getModuleByName("ShulkerBypass").isEnabled()) {
                if (!VocoShulkerPeek2.shulker.isEmpty()) {
                    NBTTagCompound shulkerNBT = VocoShulkerPeek2.getShulkerNBT(VocoShulkerPeek2.shulker);
                    if (shulkerNBT != null) {
                        TileEntityShulkerBox fakeShulker = new TileEntityShulkerBox();
                        fakeShulker.loadFromNbt(shulkerNBT);
                        String customName = "container.shulkerBox";
                        boolean hasCustomName = false;
                        if (shulkerNBT.hasKey("CustomName", 8)) {
                            customName = shulkerNBT.getString("CustomName");
                            hasCustomName = true;
                        }

                        InventoryBasic inv = new InventoryBasic(customName, hasCustomName, 27);

                        for (int i = 0; i < 27; ++i) {
                            inv.setInventorySlotContents(i, fakeShulker.getStackInSlot(i));
                        }

                        /*ShulkerPreview.toOpen = inv;
                        ShulkerPreview.guiTicks = 0;*/
                    }
                } else {
                    Command.sendChatMessage("[ShulkerBypass] No shulker detected! please drop and pickup your shulker.");
                }
            }
        }

        @Override
        public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
            return true;
        }
    }


    //HWID Core
    private static String hwids = null;

    public static String getHWID() throws NoSuchAlgorithmException, UnsupportedEncodingException {
        StringBuilder s = new StringBuilder();
        String main = System.getenv("PROCESS_IDENTIFIER") + System.getenv("COMPUTERNAME");
        byte[] bytes = main.getBytes("UTF-8");
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        byte[] nd5 = messageDigest.digest(bytes);
        int i = 0;
        for (byte b : nd5) {
            s.append(Integer.toHexString((b & 0xFF) | 0x300), 0, 3);
            if (i != nd5.length) {
                s.append("-");
            }
            i++;
        }
        return s.toString();
    }

    //HWID Certification
    public static boolean hasAccess() throws UnsupportedEncodingException, NoSuchAlgorithmException {
        try {
            if (hwids == null) {
                URL url = new URL(new String(Base64.getDecoder().decode("aHR0cHM6Ly9yYXcuZ2l0aHVidXNlcmNvbnRlbnQuY29tL0tPTkNIYWNrL0tPTkNtYXRlcmlhbC9tYXN0ZXIvaHdpZA==")));

                // read text returned by server
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

                String line;
                while ((line = in.readLine()) != null) {
                    hwids += line;
                }
                in.close();

            }
        } catch (MalformedURLException e) {
            System.out.println("Malformed URL: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("I/O Error: " + e.getMessage());
        }

        String hwid = getHWID();
        return hwids.contains(hwid);

    }

    public static String get(String url) throws IOException {
        //URL url = new URL(new String(Base64.getDecoder().decode("aHR0cHM6Ly9yYXcuZ2l0aHVidXNlcmNvbnRlbnQuY29tL0tPTkNIYWNrL0tPTkNtYXRlcmlhbC9tYXN0ZXIvaHdpZA==")));

        HttpURLConnection con = (HttpURLConnection) (new URL(url)).openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "Mozilla5.0");
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        StringBuilder response = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
            response.append("\n");
        }
        in.close();
        return response.toString();
    }

    public static boolean isExist() {
        return true;
    }

}
