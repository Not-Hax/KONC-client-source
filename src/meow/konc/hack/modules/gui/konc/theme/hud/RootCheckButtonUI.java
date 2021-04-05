package meow.konc.hack.gui.konc.theme.hud;

import meow.konc.hack.gui.font.CFontRenderer;
import meow.konc.hack.gui.konc.KONCGUI;
import meow.konc.hack.gui.konc.RenderHelper;
import meow.konc.hack.gui.rgui.component.container.Container;
import meow.konc.hack.gui.rgui.component.use.CheckButton;
import meow.konc.hack.gui.rgui.render.AbstractComponentUI;
import meow.konc.hack.gui.rgui.render.font.FontRenderer;
import meow.konc.hack.module.ModuleManager;
import meow.konc.hack.modules.other.ClientConfig;
import meow.konc.hack.modules.other.Gui;
import meow.konc.hack.setting.Setting;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class RootCheckButtonUI<T extends CheckButton> extends AbstractComponentUI<CheckButton> {
    CFontRenderer cFontRenderer = new CFontRenderer(new Font("MS Reference Sans Serif", 0, 18), true, true);

    protected Color backgroundColour = new Color(0, 0, 0);
    protected Color backgroundColourHover = new Color(81, 184, 255);
    protected Color idleColourNormal = new Color(200, 200, 200);
    protected Color downColourNormal = new Color(190, 190, 190);
    protected Color idleColourToggle = new Color(126, 206, 250);
    protected Color downColourToggle = idleColourToggle.brighter();
    public float redForBG;
    public float greenForBG;
    public float blueForBG;
    public float redForBorder;
    public float greenForBorder;
    public float blueForBorder;
    public float alphaForBG;

    private void checkSettingGuiColour(Setting setting) {
        String var2 = setting.getName();
        byte var3 = -1;
        switch (var2.hashCode()) {
            case -780023768:
                if (var2.equals("Red Main")) {
                    var3 = 0;
                }
                break;
            case -400719425:
                if (var2.equals("Blue Main")) {
                    var3 = 2;
                }
                break;
            case 110097449:
                if (var2.equals("Green Border")) {
                    var3 = 5;
                }
                break;
            case 721272699:
                if (var2.equals("Alpha Main")) {
                    var3 = 3;
                }
                break;
            case 1153959602:
                if (var2.equals("Blue Border")) {
                    var3 = 6;
                }
                break;
            case 1595957494:
                if (var2.equals("Green Main")) {
                    var3 = 1;
                }
                break;
            case 1714706139:
                if (var2.equals("Red Border")) {
                    var3 = 4;
                }
        }

        switch (var3) {
            case 0:
                redForBG = (Float) setting.getValue();
                break;
            case 1:
                greenForBG = (Float) setting.getValue();
                break;
            case 2:
                blueForBG = (Float) setting.getValue();
                break;
            case 3:
                alphaForBG = (Float) setting.getValue();
                break;
            case 4:
                redForBorder = (Float) setting.getValue();
                break;
            case 5:
                greenForBorder = (Float) setting.getValue();
                break;
            case 6:
                blueForBorder = (Float) setting.getValue();
        }
    }

    public void renderComponent(CheckButton component, FontRenderer ff) {
        GL11.glColor4f(backgroundColour.getRed() / 255.0F, backgroundColour.getGreen() / 255.0F, backgroundColour.getBlue() / 255.0F, component.getOpacity());
        Gui colorBack = (Gui) ModuleManager.getModuleByName("Gui");
        ModuleManager.getModuleByName("Gui").settingList.forEach(setting -> checkSettingGuiColour(setting));
        if (colorBack.getBTMode() == Gui.ButtonMode.HIGHLIGHT) {
            //RGB
            if (((Gui) ModuleManager.getModuleByName("Gui")).rainbow.getValue()) {
                float[] tick_color1 = {(System.currentTimeMillis() % 11520L) / 11520.0f * 2};
                int color_rgb1 = Color.HSBtoRGB(tick_color1[0], 1, 1);
                float r = (color_rgb1 >> 16 & 0xFF) / 255.0F;
                float g = (color_rgb1 >> 8 & 0xFF) / 255.0F;
                float b = (color_rgb1 & 0xFF) / 255.0F;
                GL11.glColor3f(r, g, b);
            } else {
                GL11.glColor3f(redForBorder, greenForBorder, blueForBorder);
            }
            if (component.isToggled()) {
                RenderHelper.drawFilledRectangle(0.0F, -1.0F, component.getWidth(), (component.getHeight() + 2));
                GL11.glColor3f(0.0F, 0.0F, 0.0F);
            }

            if (component.isHovered()) {
                GL11.glColor4f(0.59F, 0.59F, 0.59F, 0.9F);
                RenderHelper.drawFilledRectangle(0.0F, -1.0F, component.getWidth(), (component.getHeight() + 2));
                GL11.glColor3f(0.0F, 0.0F, 0.0F);
            }
            if (component.isPressed()) {
                GL11.glColor4f(1.02F, 1.01F, 1.01F, 0.9F);
                RenderHelper.drawFilledRectangle(0.0F, -1.0F, component.getWidth(), (component.getHeight() + 2));
                GL11.glColor3f(0.0F, 0.0F, 0.0F);
            }
        }
        String text = component.getName();
        int c = component.isToggled() ? 16777215 : 13750737;
        if (colorBack.getBTMode() == Gui.ButtonMode.FONT) {
            c = component.isToggled() ? 1769216 : 16777215;
            if (component.isHovered())
                c = (c & 0x7F7F7F) << 1;
        }
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        if (((ClientConfig) ModuleManager.getModuleByName("ClientConfig")).smoothFont.getValue()) {
            GL11.glEnable(3553);
            GL11.glEnable(3042);
            GL11.glDisable(2884);
            cFontRenderer.drawString(text, 1.0F, (KONCGUI.fontRenderer.getFontHeight() / 2 - 2), c);
            GL11.glDisable(3553);
            GL11.glDisable(3042);
            GL11.glEnable(2884);
        } else {
            KONCGUI.fontRenderer.drawString(1, KONCGUI.fontRenderer.getFontHeight() / 2 - 2, c, text);
        }
    }

    @Override
    public void handleAddComponent(CheckButton component, Container container) {
        component.setWidth(KONCGUI.fontRenderer.getStringWidth("Dispenser32kne") + 10);
        component.setHeight(KONCGUI.fontRenderer.getFontHeight() + 1);
    }
}