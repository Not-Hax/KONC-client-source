/*package meow.konc.hack.gui.konc.theme.hud;

import meow.konc.hack.gui.font.CFontRenderer;
import meow.konc.hack.gui.konc.component.InfoOverlay;
import meow.konc.hack.gui.rgui.component.AlignedComponent;
import meow.konc.hack.gui.rgui.render.AbstractComponentUI;
import meow.konc.hack.gui.rgui.render.font.FontRenderer;
import meow.konc.hack.module.ModuleManager;
import meow.konc.hack.modules.other.Info;
import meow.konc.hack.util.colour.ColourConverter;
import meow.konc.hack.util.colour.ColourHolder;
import meow.konc.hack.util.other.LagCompensator;
import meow.konc.hack.util.other.Wrapper;
import meow.konc.hack.util.render.InfoCalculator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.glDisable;

public class UwUGodInfoUI extends AbstractComponentUI<InfoOverlay> {

    CFontRenderer cFontRenderer = new CFontRenderer(new Font("Arial", 0, 18), true, true);
    @Override
    public void renderComponent(InfoOverlay component, FontRenderer f) {
        final Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null) return;

        FontRenderer renderer = Wrapper.getFontRenderer();
        GlStateManager.pushMatrix();
        glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        int y = 2;
        boolean left = false;
        Function<Integer, Integer> xFunc;
        switch (component.getAlignment()) {
            case LEFT:
                xFunc = i -> 0;
                left = true;
                break;
            default:
                xFunc = i -> component.getWidth() - i;
                break;
        }
        if (Info.shouldPing()) {
            ColourHolder ch = new ColourHolder(192, 192, 192);
            int color = ColourConverter.rgbToInt(192, 192, 192);
            boolean a = ModuleManager.getModuleByName("SmoothFont").isEnabled();
            String text = "Ping: ";
            int textWidth = a ? cFontRenderer.getStringWidth(text + InfoCalculator.ping()) : renderer.getStringWidth(text + InfoCalculator.ping());
            int textHeight = renderer.getFontHeight() + 1;
            if (a) {
                cFontRenderer.drawStringWithShadow(text, xFunc.apply(textWidth), y, color);
            } else {
                renderer.drawStringWithShadow(xFunc.apply(textWidth), y, ch.getR(), ch.getG(), ch.getB(), text);
            }
            if (InfoCalculator.ping() < 100) {
                ch = new ColourHolder(102, 255, 102);
                color = ColourConverter.rgbToInt(102, 255, 102);
            }else if (InfoCalculator.ping() > 200) {
                ch = new ColourHolder(255, 0, 0);
                color = ColourConverter.rgbToInt(255, 0, 0);
            }else {
                ch = new ColourHolder(255, 255, 51);
                color = ColourConverter.rgbToInt(255, 255, 51);
            }
            text = Integer.toString(InfoCalculator.ping());
            textWidth = a ? cFontRenderer.getStringWidth(left ? "Ping: ": text) : renderer.getStringWidth(left ? "Ping: ": text);
            if (a) {
                cFontRenderer.drawStringWithShadow(text, !left ? xFunc.apply(textWidth) : textWidth, y, color);
            } else {
                renderer.drawStringWithShadow(!left ? xFunc.apply(textWidth) : textWidth, y, ch.getR(), ch.getG(), ch.getB(), text);
            }
            y += textHeight;
        }
        if (Info.shouldBps()) {
            ColourHolder ch = new ColourHolder(192, 192, 192);
            int color = ColourConverter.rgbToInt(192, 192, 192);
            boolean a = ModuleManager.getModuleByName("SmoothFont").isEnabled();
            String text = "BPS: ";
            int textWidth = a ? cFontRenderer.getStringWidth(text + InfoCalculator.speed(false, mc)) : renderer.getStringWidth(text + InfoCalculator.speed(false, mc));
            int textHeight = renderer.getFontHeight() + 1;
            if (a) {
                cFontRenderer.drawStringWithShadow(text, xFunc.apply(textWidth), y, color);
            } else {
                renderer.drawStringWithShadow(xFunc.apply(textWidth), y, ch.getR(), ch.getG(), ch.getB(), text);
            }
            ch = new ColourHolder(255, 255, 255);
            color = ColourConverter.rgbToInt(255, 255, 255);

            text = InfoCalculator.speed(false, mc);
            textWidth = a ? cFontRenderer.getStringWidth(left ? "BPS: ": text) : renderer.getStringWidth(left ? "BPS: ": text);
            if (a) {
                cFontRenderer.drawStringWithShadow(text, !left ? xFunc.apply(textWidth) : textWidth, y, color);
            } else {
                renderer.drawStringWithShadow(!left ? xFunc.apply(textWidth) : textWidth, y, ch.getR(), ch.getG(), ch.getB(), text);
            }
            y += textHeight;
        }
        if (Info.shouldTps()) {
            ColourHolder ch = new ColourHolder(192, 192, 192);
            int color = ColourConverter.rgbToInt(192, 192, 192);
            boolean a = ModuleManager.getModuleByName("SmoothFont").isEnabled();
            String text = "TPS: ";
            int textWidth = a ? cFontRenderer.getStringWidth(text + (Math.floor(LagCompensator.INSTANCE.getTickRate()) == LagCompensator.INSTANCE.getTickRate() ? Integer.toString((int) LagCompensator.INSTANCE.getTickRate()) : String.format("%.1f", LagCompensator.INSTANCE.getTickRate()))) : renderer.getStringWidth(text + (Math.floor(LagCompensator.INSTANCE.getTickRate()) == LagCompensator.INSTANCE.getTickRate() ? Integer.toString((int) LagCompensator.INSTANCE.getTickRate()) : String.format("%.1f", LagCompensator.INSTANCE.getTickRate())));
            int textHeight = renderer.getFontHeight() + 1;
            if (a) {
                cFontRenderer.drawStringWithShadow(text, xFunc.apply(textWidth), y, color);
            } else {
                renderer.drawStringWithShadow(xFunc.apply(textWidth), y, ch.getR(), ch.getG(), ch.getB(), text);
            }
            if ((Math.round(LagCompensator.INSTANCE.getTickRate() * 10) / 10) > 15) {
                ch = new ColourHolder(102, 255, 102);
                color = ColourConverter.rgbToInt(102, 255, 102);
            }else if ((Math.round(LagCompensator.INSTANCE.getTickRate() * 10) / 10) < 10) {
                ch = new ColourHolder(255, 0, 0);
                color = ColourConverter.rgbToInt(255, 0, 0);
            }else {
                ch = new ColourHolder(255, 255, 51);
                color = ColourConverter.rgbToInt(255, 255, 51);
            }
            text = Math.floor(LagCompensator.INSTANCE.getTickRate()) == LagCompensator.INSTANCE.getTickRate() ? Integer.toString((int) LagCompensator.INSTANCE.getTickRate()) : String.format("%.1f", LagCompensator.INSTANCE.getTickRate());
            textWidth = a ? cFontRenderer.getStringWidth(left ? "TPS: " : text) : renderer.getStringWidth(left ? "TPS: " : text);
            if (a) {
                cFontRenderer.drawStringWithShadow(text, !left ? xFunc.apply(textWidth) : textWidth, y, color);
            } else {
                renderer.drawStringWithShadow(!left ? xFunc.apply(textWidth) : textWidth, y, ch.getR(), ch.getG(), ch.getB(), text);
            }
            y += textHeight;
        }
        if (Info.shouldFps()) {
            ColourHolder ch = new ColourHolder(192, 192, 192);
            int color = ColourConverter.rgbToInt(192, 192, 192);
            boolean a = ModuleManager.getModuleByName("SmoothFont").isEnabled();
            String text = "FPS: ";
            int textWidth = a ? cFontRenderer.getStringWidth(text + Minecraft.getDebugFPS()) : renderer.getStringWidth(text + Minecraft.getDebugFPS());
            int textHeight = renderer.getFontHeight() + 1;
            if (a) {
                cFontRenderer.drawStringWithShadow(text, xFunc.apply(textWidth), y, color);
            } else {
                renderer.drawStringWithShadow(xFunc.apply(textWidth), y, ch.getR(), ch.getG(), ch.getB(), text);
            }
            ch = new ColourHolder(255, 255, 255);
            color = ColourConverter.rgbToInt(255, 255, 255);

            text = Integer.toString(Minecraft.getDebugFPS());
            textWidth = a ? cFontRenderer.getStringWidth(left ? "FPS: " : text) : renderer.getStringWidth(left ? "FPS: " : text);
            if (a) {
                cFontRenderer.drawStringWithShadow(text, !left ? xFunc.apply(textWidth) : textWidth, y, color);
            } else {
                renderer.drawStringWithShadow(!left ? xFunc.apply(textWidth) : textWidth, y, ch.getR(), ch.getG(), ch.getB(), text);
            }
            y += textHeight;
        }
        if (Info.shouldCoords() && Info.shouldNetherCoords()) {
            if (mc.player.dimension == 0) {
                ColourHolder ch = new ColourHolder(255, 255, 255);
                int color = ColourConverter.rgbToInt(255, 255, 255);
                boolean a = ModuleManager.getModuleByName("SmoothFont").isEnabled();
                String text = "X: " + Math.round(mc.player.posX) + " Y: " + Math.round(mc.player.posY) + " Z: " + Math.round(mc.player.posZ);
                int textWidth = a ? cFontRenderer.getStringWidth(text) : renderer.getStringWidth(text);
                int textHeight = renderer.getFontHeight() + 1;
                if (a) {
                    cFontRenderer.drawStringWithShadow(text, xFunc.apply(textWidth), y, color);
                } else {
                    renderer.drawStringWithShadow(xFunc.apply(textWidth), y, ch.getR(), ch.getG(), ch.getB(), text);
                }
                y += textHeight;
                ch = new ColourHolder(205, 0, 0);
                color = ColourConverter.rgbToInt(205, 0, 0);
                a = ModuleManager.getModuleByName("SmoothFont").isEnabled();
                text = "X: " + Math.round(mc.player.posX / 8) + " Y: " + Math.round(mc.player.posY) + " Z: " + Math.round(mc.player.posZ / 8);
                textWidth = a ? cFontRenderer.getStringWidth(text) : renderer.getStringWidth(text);
                textHeight = renderer.getFontHeight() + 1;
                if (a) {
                    cFontRenderer.drawStringWithShadow(text, xFunc.apply(textWidth), y, color);
                } else {
                    renderer.drawStringWithShadow(xFunc.apply(textWidth), y, ch.getR(), ch.getG(), ch.getB(), text);
                }
                y += textHeight;
            }else {
                ColourHolder ch = new ColourHolder(205, 0, 0);
                int color = ColourConverter.rgbToInt(205, 0, 0);
                boolean a = ModuleManager.getModuleByName("SmoothFont").isEnabled();
                String text = "X: " + Math.round(mc.player.posX) + " Y: " + Math.round(mc.player.posY) + " Z: " + Math.round(mc.player.posZ);
                int textWidth = a ? cFontRenderer.getStringWidth(text) : renderer.getStringWidth(text);
                int textHeight = renderer.getFontHeight() + 1;
                if (a) {
                    cFontRenderer.drawStringWithShadow(text, xFunc.apply(textWidth), y, color);
                } else {
                    renderer.drawStringWithShadow(xFunc.apply(textWidth), y, ch.getR(), ch.getG(), ch.getB(), text);
                }
                y += textHeight;
                ch = new ColourHolder(255, 255, 255);
                color = ColourConverter.rgbToInt(255, 255, 255);
                a = ModuleManager.getModuleByName("SmoothFont").isEnabled();
                text = "X: " + Math.round(mc.player.posX * 8) + " Y: " + Math.round(mc.player.posY) + " Z: " + Math.round(mc.player.posZ * 8);
                textWidth = a ? cFontRenderer.getStringWidth(text) : renderer.getStringWidth(text);
                textHeight = renderer.getFontHeight() + 1;
                if (a) {
                    cFontRenderer.drawStringWithShadow(text, xFunc.apply(textWidth), y, color);
                } else {
                    renderer.drawStringWithShadow(xFunc.apply(textWidth), y, ch.getR(), ch.getG(), ch.getB(), text);
                }
                y += textHeight;
            }
        }else if (Info.shouldCoords()) {
            ColourHolder ch = new ColourHolder(255, 255, 255);
            int color = ColourConverter.rgbToInt(255, 255, 255);
            boolean a = ModuleManager.getModuleByName("SmoothFont").isEnabled();
            String text = "X: " + Math.round(mc.player.posX) + " Y: " + Math.round(mc.player.posY) + " Z: " + Math.round(mc.player.posZ);
            int textWidth = a ? cFontRenderer.getStringWidth(text) : renderer.getStringWidth(text);
            int textHeight = renderer.getFontHeight() + 1;
            if (a) {
                cFontRenderer.drawStringWithShadow(text, xFunc.apply(textWidth), y, color);
            } else {
                renderer.drawStringWithShadow(xFunc.apply(textWidth), y, ch.getR(), ch.getG(), ch.getB(), text);
            }
            y += textHeight;
        }else if (Info.shouldNetherCoords()) {
            ColourHolder ch = new ColourHolder(205, 0, 0);
            int color = ColourConverter.rgbToInt(205, 0, 0);
            boolean a = ModuleManager.getModuleByName("SmoothFont").isEnabled();
            if (mc.player.dimension != 0) {
                String text = "X: " + Math.round(mc.player.posX) + " Y: " + Math.round(mc.player.posY) + " Z: " + Math.round(mc.player.posZ);
                int textWidth = a ? cFontRenderer.getStringWidth(text) : renderer.getStringWidth(text);
                int textHeight = renderer.getFontHeight() + 1;
                if (a) {
                    cFontRenderer.drawStringWithShadow(text, xFunc.apply(textWidth), y, color);
                } else {
                    renderer.drawStringWithShadow(xFunc.apply(textWidth), y, ch.getR(), ch.getG(), ch.getB(), text);
                }
                y += textHeight;
            }else {
                String text = "X: " + Math.round(mc.player.posX / 8) + " Y: " + Math.round(mc.player.posY) + " Z: " + Math.round(mc.player.posZ / 8);
                int textWidth = a ? cFontRenderer.getStringWidth(text) : renderer.getStringWidth(text);
                int textHeight = renderer.getFontHeight() + 1;
                if (a) {
                    cFontRenderer.drawStringWithShadow(text, xFunc.apply(textWidth), y, color);
                } else {
                    renderer.drawStringWithShadow(xFunc.apply(textWidth), y, ch.getR(), ch.getG(), ch.getB(), text);
                }
                y += textHeight;
            }
        }
        if (Info.shouldPotion()) {

            List<PotionInfo> potions = new ArrayList<>();
            mc.player.getActivePotionMap().forEach((potion, potionEffect) -> potions.add(new PotionInfo(
                    I18n.format(potion.getName()),
                    potionEffect.getAmplifier(),
                    potionEffect
            )));

            Collection<PotionInfo> sortedPotions = potions.stream().sorted(Comparator.comparing(potion -> renderer.getStringWidth(potion.formattedName(true)) * (component.sort_up ? -1 : 1))).collect(Collectors.toList());
            for (PotionInfo potion : potions) {
                int color = potion.getPotionEffect().getPotion().getLiquidColor();
                ColourHolder ch = ColourConverter.intToRgb(color);
                boolean a = ModuleManager.getModuleByName("SmoothFont").isEnabled();
                String text = potion.formattedName(component.getAlignment().equals(AlignedComponent.Alignment.RIGHT));
                int textWidth = a ? cFontRenderer.getStringWidth(text) : renderer.getStringWidth(text);
                int textHeight = renderer.getFontHeight() + 1;
                if (a) {
                    cFontRenderer.drawStringWithShadow(text, xFunc.apply(textWidth), y, color);
                } else {
                    renderer.drawStringWithShadow(xFunc.apply(textWidth), y, ch.getR(), ch.getG(), ch.getB(), text);
                }
                y += textHeight;
            }
        }
        component.setHeight(y);
        GL11.glEnable(GL11.GL_CULL_FACE);
        glDisable(GL_BLEND);
        GlStateManager.popMatrix();
    }

    @Override
    public void handleSizeComponent(InfoOverlay component) {
        component.setWidth(100);
        component.setHeight(100);
    }

}*/