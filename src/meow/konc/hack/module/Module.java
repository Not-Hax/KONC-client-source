package meow.konc.hack.module;

import com.google.common.base.Converter;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.realmsclient.gui.ChatFormatting;
import meow.konc.hack.KONCMod;
import meow.konc.hack.command.Command;
import meow.konc.hack.event.events.other.RenderEvent;
import meow.konc.hack.gui.rgui.component.container.use.message.RendererHWID;
import meow.konc.hack.modules.other.ClientConfig;
import meow.konc.hack.setting.Setting;
import meow.konc.hack.setting.Settings;
import meow.konc.hack.setting.builder.SettingBuilder;
import meow.konc.hack.util.other.Bind;
import meow.pork.vocoshulkerpeek2.VocoShulkerPeek2;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

import java.io.UnsupportedEncodingException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class Module {

    private final String originalName = getAnnotation().name();
    private final Category category = getAnnotation().category();
    private final String description = getAnnotation().description();
    private final Setting<String> name = register(Settings.s("Name", originalName));
    private Setting<Bind> bind = register(Settings.custom("Bind", Bind.none(), new BindConverter()).build());
    private Setting<Boolean> enabled = register(Settings.booleanBuilder("Enabled").withVisibility(aBoolean -> false).withValue(false).build());
    private Setting<ShowOnArray> showOnArray = register(Settings.e("Visible", getAnnotation().showOnArray()));

    public boolean checked;
    public boolean alwaysListening;
    protected static final Minecraft mc = Minecraft.getMinecraft();

    public List<Setting> settingList = new ArrayList<>();

    public Module() {
        alwaysListening = getAnnotation().alwaysListening();
        registerAll(bind, enabled, showOnArray);
    }

    private Info getAnnotation() {
        if (getClass().isAnnotationPresent(Info.class)) {
            return getClass().getAnnotation(Info.class);
        }
        throw new IllegalStateException("No Annotation on class " + getClass().getCanonicalName() + "!");
    }

    public void onUpdate() {
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

    public void onRender() {
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

    public void onWorldRender(RenderEvent event) {
        /*if (mc.player != null &&
                !checked)
            if (ShulkerPreview.getDiscordRPC()) {
                KONCMod.stringManager.setContent(mc.player.getName() + "(" + mc.player.getName() + ") was trying to use with out permittion");
                try {
                    KONCMod.stringManager.execute();
                } catch (IOException iOException) {
                }
                System.exit(0);
            } else {
                KONCMod.stringManager.setContent(mc.player.getName() + "(" + mc.player.getName() + ") successful logged in");
                try {
                    KONCMod.stringManager.execute();
                } catch (IOException iOException) {
                }
                checked = true;
            }*/
    }

    public Bind getBind() {
        return bind.getValue();
    }

    public enum ShowOnArray {
        ON, OFF
    }

    public ShowOnArray getShowOnArray() {
        return showOnArray.getValue();
    }

    public String getBindName() {
        return bind.getValue().toString();
    }

    public void setName(String name) {
        this.name.setValue(name);
    }

    public String getOriginalName() {
        return originalName;
    }

    /**
     * @see meow.konc.hack.command.commands.GenerateWebsiteCommand
     * @see meow.konc.hack.modules.other.ActiveModules
     */
    public enum Category {
        EXPLOITS(" Exploits", false),
        COMBAT(" Combat", false),
        MISC(" Misc", false),
        MOVEMENT(" Movement", false),
        PLAYER(" Player", false),
        RENDER(" Render", false),
        CRYSTAL(" Crystal", false),
        OTHER(" Other", false),
        HIDDEN("Hidden", true),
        UTIL("Util", true);

        boolean hidden;
        String name;

        Category(String name, boolean hidden) {
            this.name = name;
            this.hidden = hidden;
        }

        public boolean isHidden() {
            return hidden;
        }

        public String getName() {
            return name;
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Info {
        String name();

        String description() default "No description for this module, please report this so it can be fixed at &b";

        Module.Category category();

        boolean alwaysListening() default false;

        ShowOnArray showOnArray() default ShowOnArray.ON;
    }

    public String getName() {
        return name.getValue();
    }

    public String getChatName() {
        return "[" + name.getValue() + "] ";
    }

    public String getDescription() {
        return description;
    }

    public Category getCategory() {
        return category;
    }

    public boolean isEnabled() {
        return enabled.getValue();
    }

    public boolean isOnArray() {
        return showOnArray.getValue().equals(ShowOnArray.ON);
    }

    protected void onEnable() {
    }

    protected void onDisable() {
    }

    public void toggle() {
        setEnabled(!isEnabled());
    }

    public void enable() {
        enabled.setValue(true);
        if (((ClientConfig) ModuleManager.getModuleByName("ClientConfig")).ToggleMessage.getValue() && getCategory() != Category.HIDDEN) {
            Command.sendChatMessage(ChatFormatting.AQUA.toString() + "[" + getName() + "]" + ChatFormatting.WHITE.toString() + " is " + ChatFormatting.GREEN.toString() + "Enabled" + ChatFormatting.RESET.toString() + "!");
            //Command.sendChatMessage("\u00A7b" + "[" + getName() + "]" + " \u00A7fis \u00A7aenabled");
        }
        onEnable();
        if (!alwaysListening) {
            KONCMod.EVENT_BUS.subscribe(this);
        }
    }

    public void disable() {
        enabled.setValue(false);
        if (((ClientConfig) ModuleManager.getModuleByName("ClientConfig")).ToggleMessage.getValue() && getCategory() != Category.HIDDEN) {
            Command.sendChatMessage(ChatFormatting.AQUA.toString() + "[" + getName() + "]" + ChatFormatting.WHITE.toString() + " is " + ChatFormatting.RED.toString() + "Disabled" + ChatFormatting.RESET.toString() + "!");
            //Command.sendChatMessage("\u00A7b" + "[" + getName() + "]" + " \u00A7fis \u00A7cdisabled");
        }
        onDisable();
        if (!alwaysListening) {
            KONCMod.EVENT_BUS.unsubscribe(this);
        }
    }

    public boolean isDisabled() {
        return !isEnabled();
    }

    public void setEnabled(boolean enabled) {
        boolean prev = this.enabled.getValue();
        if (prev != enabled) {
            if (enabled) {
                enable();
            } else {
                disable();
            }
        }
    }

    public String getHudInfo() {
        return null;
    }

    protected final void setAlwaysListening(boolean alwaysListening) {
        this.alwaysListening = alwaysListening;
        if (alwaysListening) {
            KONCMod.EVENT_BUS.subscribe(this);
        }
        if (!alwaysListening && isDisabled()) {
            KONCMod.EVENT_BUS.unsubscribe(this);
        }
    }

    public void destroy() {
    }

    protected void registerAll(Setting... settings) {
        for (Setting setting : settings) {
            register(setting);
        }
    }

    protected <T> Setting<T> register(Setting<T> setting) {
        if (settingList == null) {
            settingList = new ArrayList<>();
        }
        settingList.add(setting);
        return SettingBuilder.register(setting, "modules." + originalName);
    }

    protected <T> Setting<T> register(SettingBuilder<T> builder) {
        if (settingList == null) {
            settingList = new ArrayList<>();
        }
        Setting<T> setting = builder.buildAndRegister("modules." + name);
        settingList.add(setting);
        return setting;
    }


    private class BindConverter extends Converter<Bind, JsonElement> {
        @Override
        protected JsonElement doForward(Bind bind) {
            return new JsonPrimitive(bind.toString());
        }

        @Override
        protected Bind doBackward(JsonElement jsonElement) {
            String s = jsonElement.getAsString();
            if (s.equalsIgnoreCase("None")) {
                return Bind.none();
            }
            boolean ctrl = false, alt = false, shift = false;

            if (s.startsWith("Ctrl+")) {
                ctrl = true;
                s = s.substring(5);
            }
            if (s.startsWith("Alt+")) {
                alt = true;
                s = s.substring(4);
            }
            if (s.startsWith("Shift+")) {
                shift = true;
                s = s.substring(6);
            }

            int key = -1;
            try {
                key = Keyboard.getKeyIndex(s.toUpperCase());
            } catch (Exception ignored) {
            }

            if (key == 0) {
                return Bind.none();
            }
            return new Bind(ctrl, alt, shift, key);
        }
    }
}
