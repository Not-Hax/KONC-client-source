package meow.konc.hack.modules.other;

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

@Module.Info(name="PVPInfo2", category=Module.Category.CLIENT)
public class PVPInfo2 extends Module {

    private final Setting<Float> x = register(Settings.f("InfoX", 0.0f));
    private final Setting<Float> y = register(Settings.f("InfoY", 200.0f));
    private final Setting<mode> smode = register(Settings.e("StringMode",mode.One));
    private final Setting<Boolean> select = register(Settings.b("Select",false));
    //private final Setting<Boolean> Hole = register(Settings.booleanBuilder("Hole").withValue(true).withVisibility(v -> select.getValue()));
    private final Setting<Boolean> AT = register(Settings.booleanBuilder("AutoTrap").withValue(true).withVisibility(v -> select.getValue()));
    private final Setting<Boolean> AFP = register(Settings.booleanBuilder("AutoFeetPlace").withValue(true).withVisibility(v -> select.getValue()));
    private final Setting<Boolean> loveCrystal = register(Settings.booleanBuilder("loveCrystal").withValue(true).withVisibility(v -> select.getValue()));
    private final Setting<Boolean> AU = register(Settings.booleanBuilder("Aura").withValue(true).withVisibility(v -> select.getValue()));
    private final Setting<Boolean> rainbow = register(Settings.b("Rainbow", false));
    private final Setting<Float> RGBSpeed = register(Settings.floatBuilder("RGB speed").withMinimum(0f).withValue(2f));
    private final Setting<Integer> red = register(Settings.integerBuilder("Red").withRange(0, 255).withValue(255).build());
    private final Setting<Integer> green = register(Settings.integerBuilder("Green").withRange(0, 255).withValue(0).build());
    private final Setting<Integer> blue = register(Settings.integerBuilder("Blue").withRange(0, 255).withValue(255).build());
    CFontRenderer cFontRenderer = new CFontRenderer(new Font("Arial", 0, 18), true, false);
    private BlockPos pos;

    public enum  mode {
        One, Two
    }

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
            float[] hue = new float[]{(float) (System.currentTimeMillis() % 11520L) / 11520.0f *RGBSpeed.getValue()};
            int rgb = Color.HSBtoRGB(hue[0], 1.0f, 1.0f);
            int red   = rgb >> 16 & 255;
            int green = rgb >> 8 & 255;
            int blue  = rgb & 255;
            color = ColourUtils.toRGBA(red, green, blue, 255);
        }
        {
            String[] args = Wrapper.getMinecraft().debug.split(",");
            String fps = args[0];
            if (smode.getValue() == mode.One) {
                //if (Hole.getValue()) {
                //    cFontRenderer.drawCenteredStringWidthShadow("Hole: " + getHoleType(), x.getValue(), (yCount + 10.0f) - (float) cFontRenderer.getHeight() - 1.0f, color);
                //}
                cFontRenderer.drawCenteredStringWidthShadow("DEBUG (" + fps + ")", x.getValue(), yCount - (float) cFontRenderer.getHeight() - 1.0f, color);
                cFontRenderer.drawCenteredStringWidthShadow("\ud834\udd21Ping: " + (mc.getCurrentServerData() != null ? Long.valueOf(mc.getCurrentServerData().pingToServer) : "0"), x.getValue(), (yCount += 10.0f) - (float) cFontRenderer.getHeight() - 1.0f, color);
                cFontRenderer.drawCenteredStringWidthShadow("\ud834\udd21Totem: " + totem(), x.getValue(), (yCount += 10.0f) - (float) cFontRenderer.getHeight() - 1.0f, color);
                cFontRenderer.drawCenteredStringWidthShadow("\ud834\udd21Exp: " + XPCount(), x.getValue(), (yCount += 10.0f) - (float) cFontRenderer.getHeight() - 1.0f, color);
                cFontRenderer.drawCenteredStringWidthShadow("\ud834\udd21Apple: " + godapple(), x.getValue(), (yCount += 10.0f) - (float) cFontRenderer.getHeight() - 1.0f, color);
                cFontRenderer.drawCenteredStringWidthShadow("\ud834\udd21Crystal: " + Crystal(), x.getValue(), (yCount += 10.0f) - (float) cFontRenderer.getHeight() - 1.0f, color);
                if (AT.getValue()) {
                    cFontRenderer.drawCenteredStringWidthShadow("\ud834\udd21AutoTrap: " + getAutoTrap(), x.getValue(), (yCount += 10.0f) - (float) cFontRenderer.getHeight() - 1.0f, color);
                }
                if (AFP.getValue()) {
                    cFontRenderer.drawCenteredStringWidthShadow("\ud834\udd21AutoFeetPlace: " + getSurround(), x.getValue(), (yCount += 10.0f) - (float) cFontRenderer.getHeight() - 1.0f, color);
                }
                if (loveCrystal.getValue()) {
                    cFontRenderer.drawCenteredStringWidthShadow("\ud834\udd21@loveCrystal: " + getCaura(), x.getValue(), (yCount += 10.0f) - (float) cFontRenderer.getHeight() - 1.0f, color);
                }
                if (AU.getValue()) {
                    cFontRenderer.drawCenteredStringWidthShadow("\ud834\udd21Aura: " + getKA(), x.getValue(), (yCount + 10.0f) - (float) cFontRenderer.getHeight() - 1.0f, color);
                }
                //cFontRenderer.drawCenteredStringWidthShadow("Players: " + mc.player.connection.getPlayerInfoMap().size(), x.getValue(), (yCount + 10.0f) - (float) cFontRenderer.getHeight() - 1.0f, color);
            }
            if (smode.getValue() == mode.Two){
                //if (Hole.getValue()) {
                //    cFontRenderer.drawStringWithShadow("Hole: " + getHoleType(), x.getValue(), (yCount + 10.0f) - (float) cFontRenderer.getHeight() - 1.0f, color);
                //}
                cFontRenderer.drawStringWithShadow("DEBUG (" + fps + ")", x.getValue(), yCount - (float) cFontRenderer.getHeight() - 1.0f, color);
                cFontRenderer.drawStringWithShadow("\ud834\udd21Ping: " + (mc.getCurrentServerData() != null ? Long.valueOf(mc.getCurrentServerData().pingToServer) : "0"), x.getValue(), (yCount += 10.0f) - (float) cFontRenderer.getHeight() - 1.0f, color);
                cFontRenderer.drawStringWithShadow("\ud834\udd21Totem: " + totem(), x.getValue(), (yCount += 10.0f) - (float) cFontRenderer.getHeight() - 1.0f, color);
                cFontRenderer.drawStringWithShadow("\ud834\udd21Exp: " + XPCount(), x.getValue(), (yCount += 10.0f) - (float) cFontRenderer.getHeight() - 1.0f, color);
                cFontRenderer.drawStringWithShadow("\ud834\udd21Apple: " + godapple(), x.getValue(), (yCount += 10.0f) - (float) cFontRenderer.getHeight() - 1.0f, color);
                cFontRenderer.drawStringWithShadow("\ud834\udd21Crystal: " + Crystal(), x.getValue(), (yCount += 10.0f) - (float) cFontRenderer.getHeight() - 1.0f, color);
                if (AT.getValue()) {
                    cFontRenderer.drawStringWithShadow("\ud834\udd21AutoTrap: " + getAutoTrap(), x.getValue(), (yCount += 10.0f) - (float) cFontRenderer.getHeight() - 1.0f, color);
                }
                if (AFP.getValue()) {
                    cFontRenderer.drawStringWithShadow("\ud834\udd21AutoFeetPlace: " + getSurround(), x.getValue(), (yCount += 10.0f) - (float) cFontRenderer.getHeight() - 1.0f, color);
                }
                if (loveCrystal.getValue()) {
                    cFontRenderer.drawStringWithShadow("\ud834\udd21@loveCrystal: " + getCaura(), x.getValue(), (yCount += 10.0f) - (float) cFontRenderer.getHeight() - 1.0f, color);
                }
                if (AU.getValue()) {
                    cFontRenderer.drawStringWithShadow("\ud834\udd21Aura: " + getKA(), x.getValue(), (yCount + 10.0f) - (float) cFontRenderer.getHeight() - 1.0f, color);
                }
                //cFontRenderer.drawStringWithShadow("Players: " + mc.player.connection.getPlayerInfoMap().size(), x.getValue(), (yCount + 10.0f) - (float) cFontRenderer.getHeight() - 1.0f, color);
            }
        }
    }

    public double round(final double wert, final int stellen) {
        return Math.round(wert * Math.pow(10.0, stellen)) / Math.pow(10.0, stellen);
    }


    public int Crystal(){
        int i;
        int crystalCount = 0;
        for (i = 0; i < 45; i++) {
            ItemStack itemStack = (Wrapper.getMinecraft()).player.inventory.getStackInSlot(i);
            if (itemStack.getItem() == Items.END_CRYSTAL)
                crystalCount += itemStack.stackSize;
        }
        return crystalCount;
    }

    public int godapple(){
        int i;
        int Apple = 0;
        for (i = 0; i < 45; i++) {
            ItemStack itemStack = (Wrapper.getMinecraft()).player.inventory.getStackInSlot(i);
            if (itemStack.getItem() == Items.GOLDEN_APPLE)
                Apple += itemStack.stackSize;
        }
        return Apple;
    }

    public int XPCount(){
        int i;
        int XPCount = 0;
        for (i = 0; i < 45; i++) {
            ItemStack itemStack = (Wrapper.getMinecraft()).player.inventory.getStackInSlot(i);
            if (itemStack.getItem() == Items.EXPERIENCE_BOTTLE)
                XPCount += itemStack.stackSize;
        }
        return XPCount;
    }

    public int totem(){
        int i;
        int totemCount = 0;
        for (i = 0; i < 45; i++) {
            ItemStack itemStack = (Wrapper.getMinecraft()).player.inventory.getStackInSlot(i);
            if (itemStack.getItem() == Items.TOTEM_OF_UNDYING)
                totemCount += itemStack.stackSize;
        }
        return totemCount;
    }

    private String getAutoTrap() {
        String x = "\u00A7OFF";
        if (ModuleManager.getModuleByName("AutoTrap") == null) return x;
        return Boolean.toString(ModuleManager.getModuleByName("AutoTrap").isEnabled()).toUpperCase();
    }

    private String getSurround() {
        String x = "\u00A74OFF";
        if (ModuleManager.getModuleByName("AutoFeetPlace") == null) return x;
        return Boolean.toString(ModuleManager.getModuleByName("AutoFeetPlace").isEnabled()).toUpperCase();
    }

    private String getCaura() {
        String x = "\u00A7OFF";
        if (ModuleManager.getModuleByName("@loveCrystal") == null) return x;
        return Boolean.toString(ModuleManager.getModuleByName("@loveCrystal").isEnabled()).toUpperCase();
    }

    private String getKA() {
        String x = "\u00A7OFF";
        if (ModuleManager.getModuleByName("Aura") == null) return x;
        return Boolean.toString(ModuleManager.getModuleByName("Aura").isEnabled()).toUpperCase();
    }

    public String getHoleType() {
        if (getPlayerPos()) {
            return "\u00A74 0";
        } else {
            getPlayerPos();
            String holeType = "\u00A74 0";
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
}

