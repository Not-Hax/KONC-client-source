package meow.konc.hack.modules.render;

import meow.konc.hack.gui.font.CFontRenderer;
import meow.konc.hack.module.Module;
import meow.konc.hack.module.ModuleManager;
import meow.konc.hack.setting.Setting;
import meow.konc.hack.setting.Settings;
import meow.konc.hack.util.ColourUtils;
import meow.konc.hack.util.Wrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import java.awt.*;

@Module.Info(name="PVPInfo3", category=Module.Category.RENDER)
public class PVPInfo3 extends Module {

    private Setting<Float> x = register(Settings.f("InfoX", 0.0f));
    private Setting<Float> y = register(Settings.f("InfoY", 200.0f));

    private Setting<Boolean> fighting = register(Settings.b("Fighting", false));
    private Setting<Boolean> Hole = register(Settings.booleanBuilder("Hole").withValue(true).withVisibility(v -> fighting.getValue()));
    private Setting<Boolean> AT = register(Settings.booleanBuilder("AutoTrap").withValue(true).withVisibility(v -> fighting.getValue()));
    private Setting<Boolean> SU = register(Settings.booleanBuilder("AutoFeetPlace").withValue(true).withVisibility(v -> fighting.getValue()));
    private Setting<Boolean> AC = register(Settings.booleanBuilder("AutoCrystal").withValue(true).withVisibility(v -> fighting.getValue()));
    private Setting<Boolean> AU = register(Settings.booleanBuilder("KillAura").withValue(true).withVisibility(v -> fighting.getValue()));
    private Setting<Boolean> HF = register(Settings.booleanBuilder("HoleFiller").withValue(true).withVisibility(v -> fighting.getValue()));

    private Setting<Boolean> practical = register(Settings.b("Practical", false));
    private Setting<Boolean> DEBUG = register(Settings.booleanBuilder("DEBUG").withValue(false).withVisibility(v -> practical.getValue()));
    private Setting<Boolean> Ping = register(Settings.booleanBuilder("Ping").withValue(false).withVisibility(v -> practical.getValue()));
    private Setting<Boolean> Totem = register(Settings.booleanBuilder("Totem").withValue(false).withVisibility(v -> practical.getValue()));
    private Setting<Boolean> Exp = register(Settings.booleanBuilder("Exp").withValue(false).withVisibility(v -> practical.getValue()));
    private Setting<Boolean> Apple = register(Settings.booleanBuilder("Apple").withValue(false).withVisibility(v -> practical.getValue()));
    private Setting<Boolean> Crystal = register(Settings.booleanBuilder("Crystal").withValue(false).withVisibility(v -> practical.getValue()));
    private Setting<Boolean> Players = register(Settings.booleanBuilder("Players").withValue(false).withVisibility(v -> practical.getValue()));

    private Setting<Boolean> rainbow = register(Settings.b("Rainbow", false));
    private Setting<Float> RGBSpeed = register(Settings.floatBuilder("RGB speed").withMinimum(0f).withValue(2f).withVisibility(b -> rainbow.getValue()).build());
    private Setting<Integer> red = register(Settings.integerBuilder("Red").withRange(0, 255).withValue(255).build());
    private Setting<Integer> green = register(Settings.integerBuilder("Green").withRange(0, 255).withValue(0).build());
    private Setting<Integer> blue = register(Settings.integerBuilder("Blue").withRange(0, 255).withValue(255).build());

    CFontRenderer cFontRenderer = new CFontRenderer(new Font("Arial", 0, 18), true, false);
    private BlockPos pos;

    @Override
    public void onRender() {
        float yCount = y.getValue();
        int ared = red.getValue();
        int bgreen = green.getValue();
        int cblue = blue.getValue();
        int color = ColourUtils.toRGBA(ared, bgreen, cblue, 255);
        mc.player.inventory.mainInventory.stream().filter(itemStack -> itemStack.getItem() == Items.TOTEM_OF_UNDYING).mapToInt(ItemStack::getCount).sum();
        mc.player.getHeldItemOffhand().getItem();
        if (rainbow.getValue()) {
            float[] hue = new float[]{(float) (System.currentTimeMillis() % 11520L) / 11520.0f * RGBSpeed.getValue()};
            int rgb = Color.HSBtoRGB(hue[0], 1.0f, 1.0f);
            int red = rgb >> 16 & 255;
            int green = rgb >> 8 & 255;
            int blue = rgb & 255;
            color = ColourUtils.toRGBA(red, green, blue, 255);
        }
        {
            //Fighting
            if (Hole.getValue()) {
                cFontRenderer.drawStringWithShadow("Hole: " + getHoleType(), x.getValue().floatValue(), (yCount += 10.0f) - (float) cFontRenderer.getHeight() - 1.0f, color);
            }
            if (AT.getValue()) {
                cFontRenderer.drawStringWithShadow("AT: " + getAutoTrap(), x.getValue().floatValue(), (yCount += 10.0f) - (float) cFontRenderer.getHeight() - 1.0f, color);
            }
            if (SU.getValue()) {
                cFontRenderer.drawStringWithShadow("SU: " + getSurround(), x.getValue().floatValue(), (yCount += 10.0f) - (float) cFontRenderer.getHeight() - 1.0f, color);
            }
            if (AC.getValue()) {
                cFontRenderer.drawStringWithShadow("CA: " + getCaura(), x.getValue().floatValue(), (yCount += 10.0f) - (float) cFontRenderer.getHeight() - 1.0f, color);
            }
            if (AU.getValue()) {
                cFontRenderer.drawStringWithShadow("KA: " + getKA(), x.getValue().floatValue(), (yCount + 10.0f) - (float) cFontRenderer.getHeight() - 1.0f, color);
            }
            if (HF.getValue()) {
                cFontRenderer.drawStringWithShadow("HF: " + getHoleFiller(), x.getValue().floatValue(), (yCount += 10.0f) - (float) cFontRenderer.getHeight() - 1.0f, color);
            }

            String[] args = Wrapper.getMinecraft().debug.split(",");
            String fps = args[0];
            //Practical
            if (DEBUG.getValue()) {
                cFontRenderer.drawStringWithShadow("DEBUG (" + fps + ")", x.getValue(), yCount - (float) cFontRenderer.getHeight() - 1.0f, color);
            }
            if (Ping.getValue()) {
                cFontRenderer.drawStringWithShadow("Ping: " + (mc.getCurrentServerData() != null ? Long.valueOf(mc.getCurrentServerData().pingToServer) : "0"), x.getValue(), (yCount += 10.0f) - (float) cFontRenderer.getHeight() - 1.0f, color);
            }
            if (Totem.getValue()) {
                cFontRenderer.drawStringWithShadow("Totem: " + totem(), x.getValue(), (yCount += 10.0f) - (float) cFontRenderer.getHeight() - 1.0f, color);
            }
            if (Exp.getValue()) {
                cFontRenderer.drawStringWithShadow("Exp: " + XPCount(), x.getValue(), (yCount += 10.0f) - (float) cFontRenderer.getHeight() - 1.0f, color);
            }
            if (Apple.getValue()) {
                cFontRenderer.drawStringWithShadow("Apple: " + godapple(), x.getValue(), (yCount += 10.0f) - (float) cFontRenderer.getHeight() - 1.0f, color);
            }
            if (Crystal.getValue()) {
                cFontRenderer.drawStringWithShadow("Crystal: " + Crystal(), x.getValue(), (yCount += 10.0f) - (float) cFontRenderer.getHeight() - 1.0f, color);
            }
            if (Players.getValue()) {
                cFontRenderer.drawStringWithShadow("Players: " + mc.player.connection.getPlayerInfoMap().size(), x.getValue(), (yCount + 10.0f) - (float) cFontRenderer.getHeight() - 1.0f, color);
            }
        }
    }

    public double round(double wert, int stellen) {
        return Math.round(wert * Math.pow(10.0, stellen)) / Math.pow(10.0, stellen);
    }

    public int Crystal() {
        int i;
        int crystalCount = 0;
        for (i = 0; i < 45; i++) {
            ItemStack itemStack = (Wrapper.getMinecraft()).player.inventory.getStackInSlot(i);
            if (itemStack.getItem() == Items.END_CRYSTAL)
                crystalCount += itemStack.stackSize;
        }
        return crystalCount;
    }

    public int godapple() {
        int i;
        int Apple = 0;
        for (i = 0; i < 45; i++) {
            ItemStack itemStack = (Wrapper.getMinecraft()).player.inventory.getStackInSlot(i);
            if (itemStack.getItem() == Items.GOLDEN_APPLE)
                Apple += itemStack.stackSize;
        }
        return Apple;
    }

    public int XPCount() {
        int i;
        int XPCount = 0;
        for (i = 0; i < 45; i++) {
            ItemStack itemStack = (Wrapper.getMinecraft()).player.inventory.getStackInSlot(i);
            if (itemStack.getItem() == Items.EXPERIENCE_BOTTLE)
                XPCount += itemStack.stackSize;
        }
        return XPCount;
    }

    public int totem() {
        int i;
        int totemCount = 0;
        for (i = 0; i < 45; i++) {
            ItemStack itemStack = (Wrapper.getMinecraft()).player.inventory.getStackInSlot(i);
            if (itemStack.getItem() == Items.TOTEM_OF_UNDYING)
                totemCount += itemStack.stackSize;
        }
        return totemCount;
    }


    public String getHoleType() {
        if (getPlayerPos()) {
            return "\u00A74 0";
        } else {
            getPlayerPos();
            String holeType;
            if (Minecraft.getMinecraft().world.getBlockState(pos.add(0, -1, 0)).getBlock() == Blocks.BEDROCK && Minecraft.getMinecraft().world.getBlockState(pos.add(1, 0, 0)).getBlock() == Blocks.BEDROCK && Minecraft.getMinecraft().world.getBlockState(pos.add(0, 0, 1)).getBlock() == Blocks.BEDROCK && Minecraft.getMinecraft().world.getBlockState(pos.add(-1, 0, 0)).getBlock() == Blocks.BEDROCK && Minecraft.getMinecraft().world.getBlockState(pos.add(0, 0, -1)).getBlock() == Blocks.BEDROCK) {
                holeType = "\u00A7a Safe";
                return holeType;
            } else if (Minecraft.getMinecraft().world.getBlockState(pos.add(0, -1, 0)).getBlock() == Blocks.BEDROCK | Minecraft.getMinecraft().world.getBlockState(pos.add(0, -1, 0)).getBlock() == Blocks.OBSIDIAN && Minecraft.getMinecraft().world.getBlockState(pos.add(1, 0, 0)).getBlock() == Blocks.BEDROCK | Minecraft.getMinecraft().world.getBlockState(pos.add(1, 0, 0)).getBlock() == Blocks.OBSIDIAN && Minecraft.getMinecraft().world.getBlockState(pos.add(0, 0, 1)).getBlock() == Blocks.BEDROCK | Minecraft.getMinecraft().world.getBlockState(pos.add(0, 0, 1)).getBlock() == Blocks.OBSIDIAN && Minecraft.getMinecraft().world.getBlockState(pos.add(-1, 0, 0)).getBlock() == Blocks.BEDROCK | Minecraft.getMinecraft().world.getBlockState(pos.add(-1, 0, 0)).getBlock() == Blocks.OBSIDIAN && Minecraft.getMinecraft().world.getBlockState(pos.add(0, 0, -1)).getBlock() == Blocks.BEDROCK | Minecraft.getMinecraft().world.getBlockState(pos.add(0, 0, -1)).getBlock() == Blocks.OBSIDIAN) {
                holeType = "\u00A73 Unsafe";
                return holeType;
            } else {
                holeType = "\u00A74 None";
                return holeType;
            }
        }
    }
    public Boolean getPlayerPos() {
        try {
            this.pos = new BlockPos(Math.floor(Minecraft.getMinecraft().player.posX), Math.floor(Minecraft.getMinecraft().player.posY), Math.floor(Minecraft.getMinecraft().player.posZ));
            return false;
        } catch (Exception var2) {
            return true;
        }
    }

    private String getAutoTrap() {
        try {
            return ModuleManager.getModuleByName("AutoTrap").isEnabled() ? "\u00A7a TRUE" : "\u00A74 OFF";
        } catch (Exception var2) {
            return "lack of games: " + var2;
        }
    }

    private String getSurround() {
        try {
            return ModuleManager.getModuleByName("Surround").isEnabled() ? "\u00A7a TRUE" : "\u00A74 OFF";
        } catch (Exception var2) {
            return "lack of games: " + var2;
        }
    }

    private String getCaura() {
        try {
            return ModuleManager.getModuleByName("AutoCrystal").isEnabled() ? "\u00A7a TRUE" : "\u00A74 OFF";
        } catch (Exception var2) {
            return "lack of games: " + var2;
        }
    }

    private String getKA() {
        try {
            return ModuleManager.getModuleByName("KillAura").isEnabled() ? "\u00A7a TRUE" : "\u00A74 OFF";
        } catch (Exception var2) {
            return "lack of games: " + var2;
        }
    }
    private String getHoleFiller() {
        try {
            return ModuleManager.getModuleByName("HoleFiller").isEnabled() ? "\u00A7a TRUE" : "\u00A74 OFF";
        } catch (Exception var2) {
            return "lack of games: " + var2;
        }
    }
}

