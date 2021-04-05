package meow.konc.hack.modules.other;

import java.awt.Color;
import java.awt.Font;

import meow.konc.hack.gui.font.CFontRenderer;
import meow.konc.hack.module.Module;
import meow.konc.hack.module.ModuleManager;
import meow.konc.hack.setting.Setting;
import meow.konc.hack.setting.Settings;
import meow.konc.hack.util.ColourUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

@Module.Info(name="PVPInfo", category=Module.Category.CLIENT)
public class PVPInfo extends Module {
    private Setting<Float> x = register(Settings.f("InfoX", 0.0f));
    private Setting<Float> y = register(Settings.f("InfoY", 200.0f));
    private Setting<Boolean> rainbow = register(Settings.b("Rainbow", false));
    private Setting<Integer> red = register(Settings.integerBuilder("Red").withRange(0, 255).withValue(255).build());
    private Setting<Integer> green = register(Settings.integerBuilder("Green").withRange(0, 255).withValue(255).build());
    private Setting<Integer> blue = register(Settings.integerBuilder("Blue").withRange(0, 255).withValue(255).build());
    CFontRenderer cFontRenderer = new CFontRenderer(new Font("Arial", 0, 18), true, false);

    public Boolean getPlayerPos() {
        try {
            pos = new BlockPos(Math.floor(Minecraft.getMinecraft().player.posX), Math.floor(Minecraft.getMinecraft().player.posY), Math.floor(Minecraft.getMinecraft().player.posZ));
            return false;
        } catch (Exception var2) {
            return true;
        }
    }
    private BlockPos pos;
    private String holeType = "\u00A74 0";

    @Override
    public void onRender() {
        int drgb;
        float yCount = y.getValue().floatValue();
        int ared = red.getValue();
        int bgreen = green.getValue();
        int cblue = blue.getValue();
        int color = drgb = ColourUtils.toRGBA(ared, bgreen, cblue, 255);
        int totems = mc.player.inventory.mainInventory.stream().filter(itemStack -> {
            if (itemStack.getItem() != Items.TOTEM_OF_UNDYING) return false;
            return true;
        }).mapToInt(ItemStack::getCount).sum();
        if (mc.player.getHeldItemOffhand().getItem() == Items.TOTEM_OF_UNDYING) {
            ++totems;
        }
        if (rainbow.getValue().booleanValue()) {
            int argb;
            float[] hue = new float[]{(float) (System.currentTimeMillis() % 11520L) / 11520.0f};
            int rgb = Color.HSBtoRGB(hue[0], 1.0f, 1.0f);
            int red = rgb >> 16 & 255;
            int green = rgb >> 8 & 255;
            int blue = rgb & 255;
            color = argb = ColourUtils.toRGBA(red, green, blue, 255);
        }
        {
            cFontRenderer.drawStringWithShadow("FPS: " + Minecraft.getDebugFPS(), x.getValue().floatValue(), yCount - (float) cFontRenderer.getHeight() - 1.0f, color);
            cFontRenderer.drawStringWithShadow("PING: " + (mc.getCurrentServerData() != null ? Long.valueOf(mc.getCurrentServerData().pingToServer) : "0"), x.getValue().floatValue(), (yCount += 10.0f) - (float) cFontRenderer.getHeight() - 1.0f, color);
            cFontRenderer.drawStringWithShadow("Hole: " + getHoleType(), x.getValue().floatValue(), (yCount += 10.0f) - (float) cFontRenderer.getHeight() - 1.0f, color);
            cFontRenderer.drawStringWithShadow("AT: " + getAutoTrap(), x.getValue().floatValue(), (yCount += 10.0f) - (float) cFontRenderer.getHeight() - 1.0f, color);
            cFontRenderer.drawStringWithShadow("HF: " + getHoleFiller(), x.getValue().floatValue(), (yCount += 10.0f) - (float) cFontRenderer.getHeight() - 1.0f, color);
            cFontRenderer.drawStringWithShadow("SU: " + getSurround(), x.getValue().floatValue(), (yCount += 10.0f) - (float) cFontRenderer.getHeight() - 1.0f, color);
            cFontRenderer.drawStringWithShadow("CA: " + getCaura(), x.getValue().floatValue(), (yCount += 10.0f) - (float) cFontRenderer.getHeight() - 1.0f, color);
            cFontRenderer.drawStringWithShadow("KA: " + getKA(), x.getValue().floatValue(), (yCount + 10.0f) - (float) cFontRenderer.getHeight() - 1.0f, color);
            return;
        }
    }

    public String getHoleType() {
        if (getPlayerPos()) {
            return "\u00A74 0";
        } else {
            getPlayerPos();
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
