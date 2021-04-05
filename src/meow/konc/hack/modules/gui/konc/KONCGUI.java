package meow.konc.hack.gui.konc;

import com.mojang.realmsclient.gui.ChatFormatting;
import meow.konc.hack.KONCMod;
import meow.konc.hack.command.Command;
import meow.konc.hack.gui.font.CFontRenderer;
import meow.konc.hack.gui.konc.component.ActiveModules;
import meow.konc.hack.gui.konc.component.Radar;
import meow.konc.hack.gui.konc.component.SettingsPanel;
import meow.konc.hack.gui.konc.component.TabGUI;
import meow.konc.hack.gui.konc.theme.hud.KONCTheme;
import meow.konc.hack.gui.rgui.GUI;
import meow.konc.hack.gui.rgui.component.container.use.Frame;
import meow.konc.hack.gui.rgui.component.container.use.Scrollpane;
import meow.konc.hack.gui.rgui.component.listen.MouseListener;
import meow.konc.hack.gui.rgui.component.listen.TickListener;
import meow.konc.hack.gui.rgui.component.use.CheckButton;
import meow.konc.hack.gui.rgui.component.use.Label;
import meow.konc.hack.gui.rgui.render.theme.Theme;
import meow.konc.hack.gui.rgui.util.ContainerHelper;
import meow.konc.hack.gui.rgui.util.Docking;
import meow.konc.hack.module.Module;
import meow.konc.hack.module.ModuleManager;
import meow.konc.hack.modules.combat.AutoCrystal;
import meow.konc.hack.modules.hidden.util.HoleDetect;
import meow.konc.hack.modules.other.HUD;
import meow.konc.hack.modules.other.InfoOverlay;
import meow.konc.hack.util.colour.ColourHolder;
import meow.konc.hack.util.colour.ColourTextFormatting;
import meow.konc.hack.util.other.Friends;
import meow.konc.hack.util.other.Pair;
import meow.konc.hack.util.packet.Wrapper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockObsidian;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.entity.projectile.EntityWitherSkull;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nonnull;
import java.awt.*;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import static meow.konc.hack.util.colour.ColourTextFormatting.toTextMap;
import static meow.konc.hack.util.other.InfoCalculator.cardinalToAxis;

public class KONCGUI extends GUI {
    public static CFontRenderer cFontRenderer = new CFontRenderer(new Font("MS Reference Sans Serif", 0, 18), true, true);
    public static ColourHolder primaryColour = new ColourHolder(29, 29, 29);
    public static RootFontRenderer fontRenderer = new RootFontRenderer(1.0f);
    protected static final Minecraft mc = Minecraft.getMinecraft();
    public static ArrayList<Frame> framesArray = new ArrayList();

    private static int DOCK_OFFSET = 0;
    public Theme theme = getTheme();

    public KONCGUI() {
        super(new KONCTheme());
    }

    @Override
    public void drawGUI() {
        super.drawGUI();
    }

    @Override
    public void initializeGUI() {
        HashMap<Module.Category, Pair<Scrollpane, SettingsPanel>> categoryScrollpaneHashMap = new HashMap<>();
        for (Module module : ModuleManager.getModules()) {
            if (module.getCategory().isHidden()) continue;
            Module.Category moduleCategory = module.getCategory();
            if (!categoryScrollpaneHashMap.containsKey(moduleCategory)) {
                Stretcherlayout stretcherlayout = new Stretcherlayout(1);
                stretcherlayout.setComponentOffsetWidth(0);
                Scrollpane scrollpane = new Scrollpane(getTheme(), stretcherlayout, 300, 260);
                scrollpane.setMaximumHeight(388);
                categoryScrollpaneHashMap.put(moduleCategory, new Pair<>(scrollpane, new SettingsPanel(getTheme(), null)));
            }

            Pair<Scrollpane, SettingsPanel> pair = categoryScrollpaneHashMap.get(moduleCategory);
            Scrollpane scrollpane = pair.getKey();
            CheckButton checkButton = new CheckButton(module.getName());
            checkButton.setToggled(module.isEnabled());

            checkButton.addTickListener(() -> { // dear god
                checkButton.setToggled(module.isEnabled());
                checkButton.setName(module.getName());
            });

            checkButton.addMouseListener(new MouseListener() {
                @Override
                public void onMouseDown(MouseButtonEvent event) {
                    if (event.getButton() == 1) { // Right click
                        pair.getValue().setModule(module);
                        pair.getValue().setX(event.getX() + checkButton.getX());
                        pair.getValue().setY(event.getY() + checkButton.getY());
                    }
                }

                @Override
                public void onMouseRelease(MouseButtonEvent event) {

                }

                @Override
                public void onMouseDrag(MouseButtonEvent event) {

                }

                @Override
                public void onMouseMove(MouseMoveEvent event) {

                }

                @Override
                public void onScroll(MouseScrollEvent event) {

                }
            });
            checkButton.addPoof(new CheckButton.CheckButtonPoof<CheckButton, CheckButton.CheckButtonPoof.CheckButtonPoofInfo>() {
                @Override
                public void execute(CheckButton component, CheckButtonPoofInfo info) {
                    if (info.getAction().equals(CheckButton.CheckButtonPoof.CheckButtonPoofInfo.CheckButtonPoofInfoAction.TOGGLE)) {
                        module.setEnabled(checkButton.isToggled());
                    }
                }
            });
            scrollpane.addChild(checkButton);
        }

        int x = 10;
        int y = 10;
        int nexty = y;
        for (Map.Entry<Module.Category, Pair<Scrollpane, SettingsPanel>> entry : categoryScrollpaneHashMap.entrySet()) {
            Stretcherlayout stretcherlayout = new Stretcherlayout(1);
            stretcherlayout.COMPONENT_OFFSET_Y = 1;
            Frame frame = new Frame(getTheme(), stretcherlayout, entry.getKey().getName());
            Scrollpane scrollpane = entry.getValue().getKey();
            frame.addChild(scrollpane);
            frame.addChild(entry.getValue().getValue());
            scrollpane.setOriginOffsetY(0);
            scrollpane.setOriginOffsetX(0);
            frame.setCloseable(false);

            frame.setX(x);
            frame.setY(y);

            addChild(frame);

            nexty = Math.max(y + frame.getHeight() + 10, nexty);
            x += frame.getWidth() + 10;
            if (x > Wrapper.getMinecraft().displayWidth / 1.2f) {
                y = nexty;
                nexty = y;
            }
        }

        this.addMouseListener(new MouseListener() {
            private boolean isBetween(int min, int val, int max) {
                return !(val > max || val < min);
            }

            @Override
            public void onMouseDown(MouseButtonEvent event) {
                List<SettingsPanel> panels = ContainerHelper.getAllChildren(SettingsPanel.class, KONCGUI.this);
                for (SettingsPanel settingsPanel : panels) {
                    if (!settingsPanel.isVisible()) continue;
                    int[] real = GUI.calculateRealPosition(settingsPanel);
                    int pX = event.getX() - real[0];
                    int pY = event.getY() - real[1];
                    if (!isBetween(0, pX, settingsPanel.getWidth()) || !isBetween(0, pY, settingsPanel.getHeight()))
                        settingsPanel.setVisible(false);
                }
            }

            @Override
            public void onMouseRelease(MouseButtonEvent event) {

            }

            @Override
            public void onMouseDrag(MouseButtonEvent event) {

            }

            @Override
            public void onMouseMove(MouseMoveEvent event) {

            }

            @Override
            public void onScroll(MouseScrollEvent event) {

            }
        });

        ArrayList<Frame> frames = new ArrayList<>();

        Frame frame = new Frame(getTheme(), new Stretcherlayout(1), "Active modules");
        frame.setCloseable(false);
        frame.setPinneable(true);
        frame.addChild(new ActiveModules());
        frames.add(frame);

        frame = new Frame(getTheme(), new Stretcherlayout(1), "HoleInfo");
        frame.setCloseable(false);
        frame.setPinneable(true);
        Label meowowo = new Label("");
        meowowo.setShadow(true);

        meowowo.addTickListener(() -> {
            Minecraft mc = Minecraft.getMinecraft();
            int holeBlocks = 0;
            int brockBlocks = 0;

            Vec3d[] holeOffset = {
                    mc.player.getPositionVector().add(1, 0, 0),
                    mc.player.getPositionVector().add(-1, 0, 0),
                    mc.player.getPositionVector().add(0, 0, 1),
                    mc.player.getPositionVector().add(0, 0, -1),
                    mc.player.getPositionVector().add(0, -1, 0)
            };
            meowowo.setText("");
            BlockPos a = new BlockPos(mc.player.getPositionVector().x, mc.player.getPositionVector().y, mc.player.getPositionVector().z);
            boolean b = false;
            if (mc.world.getBlockState(a).getBlock() == Blocks.ENDER_CHEST) {
                b = true;
            }
            for (Vec3d vecOffset : holeOffset) {
                /* for placeholder offset for each BlockPos in the list holeOffset */
                BlockPos offset = new BlockPos(vecOffset.x, b ? vecOffset.y + 1 : vecOffset.y, vecOffset.z);
                if (mc.world.getBlockState(offset).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(offset).getBlock() == Blocks.ENDER_CHEST || mc.world.getBlockState(offset).getBlock() == Blocks.BEDROCK) {
                    holeBlocks++;
                }
                if (mc.world.getBlockState(offset).getBlock() == Blocks.BEDROCK) {
                    brockBlocks++;
                }
            }
            HoleDetect.lastinhole = HoleDetect.inhole;
            if (holeBlocks == 5) {
                if (brockBlocks == 5) {
                    meowowo.addLine("\u00A7aBedrock");
                    HoleDetect.inhole = true;
                } else {
                    meowowo.addLine("\u00a76Obby");
                    HoleDetect.inhole = true;
                }
            } else {
                meowowo.addLine("None");
                HoleDetect.inhole = false;
            }
        });
        frame.addChild(meowowo);
        meowowo.setFontRenderer(fontRenderer);
        frames.add(frame);

        frame = new Frame(getTheme(), new Stretcherlayout(1), "PVP Info");
        frame.setCloseable(false);
        frame.setPinneable(true);
        Label te = new Label("");
        te.setShadow(true);
        te.addTickListener(() -> {
            te.setText("");
            int totemCount = 0;
            int XPCount = 0;
            int crystalCount = 0;
            int Apple = 0;
            int Obby = 0;
            int i;
            for (i = 0; i < 45; i++) {
                ItemStack itemStack = (Wrapper.getMinecraft()).player.inventory.getStackInSlot(i);
                if (itemStack.getItem() == Items.TOTEM_OF_UNDYING)
                    totemCount += itemStack.stackSize;
            }
            for (i = 0; i < 45; i++) {
                ItemStack itemStack = (Wrapper.getMinecraft()).player.inventory.getStackInSlot(i);
                if (itemStack.getItem() == Items.END_CRYSTAL)
                    crystalCount += itemStack.stackSize;
            }
            for (i = 0; i < 45; i++) {
                ItemStack itemStack = (Wrapper.getMinecraft()).player.inventory.getStackInSlot(i);
                if (itemStack.getItem() instanceof ItemBlock) {
                    Block block = ((ItemBlock) itemStack.getItem()).getBlock();
                    if (block instanceof BlockObsidian) {
                        Obby += itemStack.stackSize;
                    }
                }
            }

            for (i = 0; i < 45; i++) {
                ItemStack itemStack = (Wrapper.getMinecraft()).player.inventory.getStackInSlot(i);
                if (itemStack.getItem() == Items.EXPERIENCE_BOTTLE)
                    XPCount += itemStack.stackSize;
            }
            for (i = 0; i < 45; i++) {
                ItemStack itemStack = (Wrapper.getMinecraft()).player.inventory.getStackInSlot(i);
                if (itemStack.getItem() == Items.GOLDEN_APPLE)
                    Apple += itemStack.stackSize;
            }
            te.addLine("\u00A77Totem: \u00A7f" + totemCount);
            te.addLine("\u00A77XP: \u00A7f" + XPCount);
            te.addLine("\u00A77Crystal: \u00A7f" + crystalCount);
            te.addLine("\u00A77Gapple: \u00A7f" + Apple);
            te.addLine("\u00A77Obsidian: \u00A7f" + Obby);
        });
        frame.addChild(te);
        te.setFontRenderer(fontRenderer);
        frames.add(frame);

        frame = new Frame(getTheme(), new Stretcherlayout(1), "AutoCrystal Info");
        frame.setCloseable(false);
        frame.setPinneable(true);
        Label WaterMark = new Label("");

        WaterMark.addTickListener(() -> {
            WaterMark.setText("");
            AutoCrystal owo = (AutoCrystal) ModuleManager.getModuleByName("AutoCrystal");
            String target = owo.renderEnt != null ? owo.renderEnt.getName() : "No cat";
            float d = owo.renderEnt != null ? (Wrapper.getMinecraft().player != null ? ((Entity) Wrapper.getMinecraft().player).getDistance(owo.renderEnt) : 0) : 0;
            String c = null;
            String awa = owo.danger ? "yes" : "no";
            int crystalCount = 0;
            if (Wrapper.getMinecraft().player != null) {
                for (int i = 0; i < 45; i++) {
                    ItemStack itemStack = (Wrapper.getMinecraft()).player.inventory.getStackInSlot(i);
                    if (itemStack.getItem() == Items.END_CRYSTAL) {
                        crystalCount += itemStack.stackSize;
                    }
                }
            }

            if (d <= 6) {
                c = "\u00A7c";
            } else if (d <= 13) {
                c = "\u00A7e";
            } else {
                c = "\u00A7a";
            }
            String finalC = c;
            int finalCrystalCount = crystalCount;
            WaterMark.addLine("\u00A77AutoCrystal " + (ModuleManager.getModuleByName("AutoCrystal").isDisabled() ? "\u00A74Off" : "\u00A7aON"));
            WaterMark.addLine("\u00A77Target :\u00A7f " + target);
            WaterMark.addLine("\u00A77Distance : " + finalC + d);
            WaterMark.addLine("\u00A77Crystals Remaining :\u00A7f " + finalCrystalCount);
            WaterMark.addLine("\u00A77In Danger? :\u00A7f " + awa);

        });
        frame.addChild(WaterMark);
        WaterMark.setFontRenderer(fontRenderer);
        frames.add(frame);

        frame = new Frame(getTheme(), new Stretcherlayout(1), "TabGUI");
        frame.setCloseable(false);
        frame.setPinneable(true);
        Label tabgui = new Label("");
        frame.setWidth(50);
        frame.setHeight(58);
        frame.addChild(new TabGUI());
        tabgui.setFontRenderer(fontRenderer);
        frames.add(frame);

        frame = new Frame(getTheme(), new Stretcherlayout(1), "Log");
        frame.setCloseable(false);
        frame.setPinneable(true);
        frame.setMinimumWidth(60);
        frame.setMinimumHeight(10);
        Label informationqwq = new Label("");
        informationqwq.setShadow(true);
        informationqwq.addTickListener(() -> {
            informationqwq.setText("");
            if (Command.guimessages.size() > 0) {
                try {
                    Command.guimessages.forEach(stringtodisplay -> {
                        String KONC = stringtodisplay.replaceAll("&", "\u00a7");
                        informationqwq.addLine(KONC);
                    });
                } catch (ConcurrentModificationException e) {
                    informationqwq.addLine("An Error has occurred!");
                }
            }
        });
        frame.addChild(informationqwq);
        informationqwq.setFontRenderer(fontRenderer);
        frames.add(frame);

        frame = new Frame(getTheme(), new Stretcherlayout(1), "Info");
        frame.setCloseable(false);
        frame.setPinneable(true);
        Label information = new Label("");
        information.setShadow(true);
        information.addTickListener(() -> {
            InfoOverlay info = (InfoOverlay) ModuleManager.getModuleByName("InfoOverlay");
            information.setText("");
            info.infoContents().forEach(information::addLine);
        });
        frame.addChild(information);
        information.setFontRenderer(fontRenderer);
        frames.add(frame);

        frame = new Frame(getTheme(), new Stretcherlayout(1), "Time");
        frame.setCloseable(false);
        frame.setPinneable(true);
        Label time = new Label("");
        time.addTickListener(() -> {
            DateFormat df = new SimpleDateFormat("HH:mm:ss");
            Date dateobj = new Date();
            time.setText("\u00a7a\u00a7l" + df.format(dateobj));
        });
        frame.addChild(time);
        information.setFontRenderer(fontRenderer);
        frames.add(frame);

        frame = new Frame(getTheme(), new Stretcherlayout(1), "Date");
        frame.setCloseable(false);
        frame.setPinneable(true);
        Label date = new Label("");
        date.addTickListener(() -> {
            DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
            Date dateobj = new Date();
            date.setText("\u00a7a\u00a7l" + df.format(dateobj));
        });
        frame.addChild(date);
        information.setFontRenderer(fontRenderer);
        frames.add(frame);

        frame = new Frame(getTheme(), new Stretcherlayout(1), "Inventory Viewer");
        frame.setCloseable(false);
        frame.setPinneable(true);
        frame.setMinimumWidth(162);
        frame.setMaximumHeight(12);
        Label inventory = new Label("");
        inventory.setShadow(false);
        frame.addChild(inventory);
        inventory.setFontRenderer(fontRenderer);
        frames.add(frame);

        frame = new Frame(getTheme(), new Stretcherlayout(1), "Friends");
        frame.setCloseable(false);
        frame.setPinneable(true);
        frame.setMinimumWidth(60);
        frame.setMinimumHeight(10);
        Label finalFrame = new Label("");
        finalFrame.setShadow(true);
        finalFrame.addTickListener(() -> {
            finalFrame.setText("");
            String f = "";
            finalFrame.addLine("Your friends: ");
            ArrayList<NetworkPlayerInfo> infoMap = new ArrayList<NetworkPlayerInfo>(Minecraft.getMinecraft().getConnection().getPlayerInfoMap());
            NetworkPlayerInfo profile;
            for (Friends.Friend friend : Friends.INSTANCE.friends.getValue()) {
                profile = infoMap.stream().filter(networkPlayerInfo -> networkPlayerInfo.getGameProfile().getName().equalsIgnoreCase(friend.getUsername())).findFirst().orElse(null);
                if (profile != null && !friend.getUsername().equalsIgnoreCase(Wrapper.getMinecraft().player.getName())) {
                    finalFrame.addLine(friend.getUsername() + ",");
                }
                continue;

            }
        });
        frame.addChild(finalFrame);
        finalFrame.setFontRenderer(fontRenderer);
        frames.add(frame);

        frame = new Frame(getTheme(), new Stretcherlayout(1), "Text Radar");
        Label list = new Label("");
        DecimalFormat dfHealth = new DecimalFormat("#.#");
        dfHealth.setRoundingMode(RoundingMode.HALF_UP);
        StringBuilder healthSB = new StringBuilder();
        list.addTickListener(() -> {
            if (!list.isVisible()) {
                return;
            }
            list.setText("");

            Minecraft mc = Wrapper.getMinecraft();

            if (mc.player == null) {
                return;
            }
            List<EntityPlayer> entityList = mc.world.playerEntities;

            Map<String, Integer> players = new HashMap<>();
            for (Entity e : entityList) {
                if (e.getName().equals(mc.player.getName())) {
                    continue;
                }
                String posString = (e.posY > mc.player.posY ? ChatFormatting.DARK_GREEN + "+ " : (e.posY == mc.player.posY ? " " : ChatFormatting.DARK_RED + "- "));

                String strengthfactor = "";
                EntityPlayer eplayer = (EntityPlayer) e;
                if (eplayer.isPotionActive(MobEffects.STRENGTH) && ModuleManager.isModuleEnabled("StrengthDetect")) {
                    strengthfactor = "S";
                }
                float hpRaw = ((EntityLivingBase) e).getHealth() + ((EntityLivingBase) e).getAbsorptionAmount();
                String hp = dfHealth.format(hpRaw);
                healthSB.append(Command.SECTIONSIGN());
                if (hpRaw >= 20) {
                    healthSB.append("a");
                } else if (hpRaw >= 10) {
                    healthSB.append("e");
                } else if (hpRaw >= 5) {
                    healthSB.append("6");
                } else {
                    healthSB.append("c");
                }
                healthSB.append(hp);
                players.put(ChatFormatting.AQUA + posString + " " + healthSB.toString() + " " + ChatFormatting.RED + strengthfactor + (strengthfactor.equals("S") ? " " : "") + (Friends.isFriend(e.getName()) ? ChatFormatting.GREEN : ChatFormatting.WHITE) + e.getName(), (int) mc.player.getDistance(e));
                healthSB.setLength(0);
            }

            if (players.isEmpty()) {
                list.setText("");
                return;
            }

            players = sortByValue(players);

            for (Map.Entry<String, Integer> player : players.entrySet()) {
                list.addLine(Command.SECTIONSIGN() + "7" + player.getKey() + " " + Command.SECTIONSIGN() + "4" + player.getValue());
            }
        });
        frame.setCloseable(false);
        frame.setPinneable(true);
        frame.setMinimumWidth(75);
        list.setShadow(true);
        frame.addChild(list);
        list.setFontRenderer(fontRenderer);
        frames.add(frame);

        frame = new Frame(getTheme(), new Stretcherlayout(1), "Entities");
        Label entityLabel = new Label("");
        frame.setCloseable(false);
        Frame finalFrame1 = frame;
        entityLabel.addTickListener(new TickListener() {
            Minecraft mc = Wrapper.getMinecraft();

            @Override
            public void onTick() {
                if (!finalFrame1.isMinimized()) {
                    if (mc.player == null || !entityLabel.isVisible()) {
                        return;
                    }

                    List<Entity> entityList = new ArrayList<>(mc.world.loadedEntityList);
                    if (entityList.size() <= 1) {
                        entityLabel.setText("");
                        return;
                    }
                    Map<String, Integer> entityCounts = entityList.stream()
                            .filter(Objects::nonNull)
                            .filter(e -> !(e instanceof EntityPlayer))
                            .collect(Collectors.groupingBy(KONCGUI::getEntityName,
                                    Collectors.reducing(0, ent -> {
                                        if (ent instanceof EntityItem) {
                                            return ((EntityItem) ent).getItem().getCount();
                                        }
                                        return 1;
                                    }, Integer::sum)
                            ));

                    entityLabel.setText("");
                    finalFrame1.setWidth(50);
                    entityCounts.entrySet().stream()
                            .sorted(Map.Entry.comparingByValue())
                            .map(entry -> TextFormatting.GRAY + entry.getKey() + " " + TextFormatting.DARK_GRAY + "x" + entry.getValue())
                            .forEach(entityLabel::addLine);

                } else {
                    finalFrame1.setWidth(50);
                }
            }
        });
        frame.addChild(entityLabel);
        frame.setPinneable(true);
        entityLabel.setShadow(true);
        entityLabel.setFontRenderer(fontRenderer);
        frames.add(frame);

        frame = new Frame(getTheme(), new Stretcherlayout(1), "Coordinates");
        frame.setCloseable(false);
        frame.setPinneable(true);
        Label coordsLabel = new Label("");
        coordsLabel.addTickListener(new TickListener() {
            Minecraft mc = Minecraft.getMinecraft();

            @Override
            public void onTick() {
                boolean inHell = (mc.world.getBiome(mc.player.getPosition()).getBiomeName().equals("Hell"));

                int posX = (int) mc.player.posX;
                int posY = (int) mc.player.posY;
                int posZ = (int) mc.player.posZ;

                float f = !inHell ? 0.125f : 8;
                int hposX = (int) (mc.player.posX * f);
                int hposZ = (int) (mc.player.posZ * f);

                /* The 7 and f in the string formatter is the color */

                HUD HUD = (HUD) ModuleManager.getModuleByName("HUD");
                switch (HUD.mode.getValue()) {
                    case Future:
                        String cardinal = cardinalToAxis(Character.toUpperCase(mc.player.getHorizontalFacing().toString().charAt(0)));
                        String xyz = KONCMod.colour + "7" + "XYZ " + KONCMod.colour + "r";

                        /*String ow = String.format("%s%,d%s, %s%,d%s, %s%,d %s7",
                                getStringColour(setToText(HUD.Color.getValue())),
                                posX,
                                getStringColour(setToText(HUD.Color.getValue())),
                                getStringColour(setToText(HUD.Color.getValue())),
                                posY,
                                getStringColour(setToText(HUD.Color.getValue())),
                                getStringColour(setToText(HUD.Color.getValue())),
                                posZ,
                                getStringColour(setToText(HUD.Color.getValue())) + KONCMod.colour
                        );*/

                        String ow = String.format("%sf%,d%s7, %sf%,d%s7, %sf%,d %s7",
                                KONCMod.colour,
                                posX,
                                KONCMod.colour,
                                KONCMod.colour,
                                posY,
                                KONCMod.colour,
                                KONCMod.colour,
                                posZ,
                                KONCMod.colour
                        );

                        String nether = String.format(" [%s4%,d%s7, %s4%,d%s7]",
                                KONCMod.colour,
                                hposX,
                                KONCMod.colour,
                                KONCMod.colour,
                                hposZ,
                                KONCMod.colour
                        );
                        coordsLabel.setText("");
                        coordsLabel.addLine(cardinal);
                        coordsLabel.addLine(xyz + ow + nether);
                }
                switch (HUD.mode.getValue()) {
                    case wwe:
                        String xyz2 = KONCMod.colour + "f" + "X:Y:Z: " + KONCMod.colour + "r";
                        String xyz3 = KONCMod.colour + "4" + "X:Y:Z: " + KONCMod.colour + "r";

                        String ow = String.format("%sf%,d%s7, %sf%,d%s7, %sf%,d %s7",
                                KONCMod.colour,
                                posX,
                                KONCMod.colour,
                                KONCMod.colour,
                                posY,
                                KONCMod.colour,
                                KONCMod.colour,
                                posZ,
                                KONCMod.colour
                        );

                        String nether = String.format("%s4%,d%s4, %s4%,d%s4, %s4%,d%s4",
                                KONCMod.colour,
                                hposX,
                                KONCMod.colour,
                                KONCMod.colour,
                                posY,
                                KONCMod.colour,
                                KONCMod.colour,
                                hposZ,
                                KONCMod.colour
                        );
                        coordsLabel.setText("");
                        coordsLabel.addLine(xyz2 + ow);
                        coordsLabel.addLine(xyz3 + nether);
                }
            }
        });
        frame.addChild(coordsLabel);
        coordsLabel.setFontRenderer(fontRenderer);
        coordsLabel.setShadow(true);
        frame.setHeight(20);
        frames.add(frame);

        frame = new Frame(getTheme(), new Stretcherlayout(1), "Radar");
        frame.setCloseable(false);
        frame.setMinimizeable(true);
        frame.setPinneable(true);
        frame.addChild(new Radar());
        frame.setWidth(100);
        frame.setHeight(100);
        frames.add(frame);

        frame = new Frame(getTheme(), new Stretcherlayout(1), "Welcomer");
        frame.setCloseable(false);
        frame.setPinneable(true);
        Label welcomer = new Label("");

        welcomer.addTickListener(() -> {
            welcomer.setText("");
            welcomer.addLine("\u00a7d" + KONCMod.KONC_KANJI + "\u00A7c " + KONCMod.MODVER);
            welcomer.addLine("\u00A7c" + "Welcome " + "\u00a7d" + Wrapper.getPlayer().getDisplayNameString() + "\u00A7c" + " owo");

        });
        frame.addChild(welcomer);
        information.setFontRenderer(fontRenderer);
        frames.add(frame);

        for (Frame frame1 : frames) {
            frame1.setX(x);
            frame1.setY(y);

            nexty = Math.max(y + frame1.getHeight() + 10, nexty);
            x += frame1.getWidth() + 10;
            if (x * DisplayGuiScreen.getScale() > Wrapper.getMinecraft().displayWidth / 1.2f) {
                y = nexty;
                nexty = y;
                x = 10;
            }

            addChild(frame1);
        }
        framesArray.addAll(frames);
    }

    private static String getEntityName(@Nonnull Entity entity) {
        if (entity instanceof EntityItem) {
            return TextFormatting.DARK_AQUA + ((EntityItem) entity).getItem().getItem().getItemStackDisplayName(((EntityItem) entity).getItem());
        }
        if (entity instanceof EntityWitherSkull) {
            return TextFormatting.DARK_GRAY + "Wither skull";
        }
        if (entity instanceof EntityEnderCrystal) {
            return TextFormatting.LIGHT_PURPLE + "End crystal";
        }
        if (entity instanceof EntityEnderPearl) {
            return "Thrown ender pearl";
        }
        if (entity instanceof EntityMinecart) {
            return "Minecart";
        }
        if (entity instanceof EntityItemFrame) {
            return "Item frame";
        }
        if (entity instanceof EntityEgg) {
            return "Thrown egg";
        }
        if (entity instanceof EntitySnowball) {
            return "Thrown snowball";
        }

        return entity.getName();
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list =
                new LinkedList<>(map.entrySet());
        Collections.sort(list, Comparator.comparing(o -> (o.getValue())));

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    @Override
    public void destroyGUI() {
        kill();
    }

    public static void dock(Frame component) {
        Docking docking = component.getDocking();
        if (docking.isTop())
            component.setY(DOCK_OFFSET);
        if (docking.isBottom())
            component.setY((Wrapper.getMinecraft().displayHeight / DisplayGuiScreen.getScale()) - component.getHeight() - DOCK_OFFSET);
        if (docking.isLeft())
            component.setX(DOCK_OFFSET);
        if (docking.isRight())
            component.setX((Wrapper.getMinecraft().displayWidth / DisplayGuiScreen.getScale()) - component.getWidth() - DOCK_OFFSET);
        if (docking.isCenterHorizontal())
            component.setX((Wrapper.getMinecraft().displayWidth / (DisplayGuiScreen.getScale() * 2) - component.getWidth() / 2));
        if (docking.isCenterVertical())
            component.setY(Wrapper.getMinecraft().displayHeight / (DisplayGuiScreen.getScale() * 2) - component.getHeight() / 2);

    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public boolean isPinned() {
        return false;
    }

    @Override
    public boolean isMinimized() {
        return false;
    }

    @Override
    public Docking getDocking() {
        return null;
    }
}

