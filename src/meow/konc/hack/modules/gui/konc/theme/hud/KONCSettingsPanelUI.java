package meow.konc.hack.gui.konc.theme.hud;

import meow.konc.hack.gui.konc.RenderHelper;
import meow.konc.hack.gui.konc.component.SettingsPanel;
import meow.konc.hack.gui.rgui.render.AbstractComponentUI;
import meow.konc.hack.gui.rgui.render.font.FontRenderer;
import meow.konc.hack.module.ModuleManager;
import meow.konc.hack.modules.other.Gui;
import meow.konc.hack.setting.Setting;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class KONCSettingsPanelUI extends AbstractComponentUI<SettingsPanel>
{
    float redForBorder;
    float greenForBorder;
    float blueForBorder;
    float redForBG;
    float greenForBG;
    float blueForBG;
    float alphaForST;

    @Override
    public void renderComponent(final SettingsPanel component, final FontRenderer fontRenderer) {
        super.renderComponent(component, fontRenderer);
        ModuleManager.getModuleByName("Gui").settingList.forEach(setting -> checkSettingGui(setting));
        GL11.glLineWidth(2.0F);
        GL11.glColor4f(redForBG, greenForBG, blueForBG, alphaForST);
        Gui Gui = (Gui) ModuleManager.getModuleByName("Gui");
        switch (Gui.mode.getValue()) {
            case KONC:
                RenderHelper.drawFilledRectangle(0.0F, 0.0F, (float) component.getWidth(), (float) component.getHeight());
                break;
            case KONC2:
                RenderHelper.drawFilledRectangle(0.0F, 0.0F, (float) component.getWidth(), (float) component.getHeight());
                break;
            case KONC3:
                RenderHelper.drawFilledRectangle(0.0F, 0.0F, (float) component.getWidth(), (float) component.getHeight());
                break;
            case KAMI:
                RenderHelper.drawFilledRectangle(0.0F, 0.0F, (float) component.getWidth(), (float) component.getHeight());
                break;
            case Modern:
                RenderHelper.drawRoundedRectangle(2, 2, component.getWidth(), component.getHeight(), 10);
                break;
            case Modern2:
                RenderHelper.drawRoundedRectangle(0, 0, component.getWidth(), component.getHeight(), 10);
                break;
        }

        //RenderHelper.drawFilledRectangle(0.0F, 0.0F, (float)component.getWidth(), (float)component.getHeight());

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
        switch (Gui.mode.getValue()) {
            case KONC:
                GL11.glLineWidth(1.5F);
                RenderHelper.drawRectangle(0.0F, 0.0F, (float) component.getWidth(), (float) component.getHeight());
                GL11.glColor3f(1.0F, 1.0F, 1.0F);
                break;
            case KONC2:
                GL11.glLineWidth(1.5F);
                RenderHelper.drawRectangle(0.0F, 0.0F, (float) component.getWidth(), (float) component.getHeight());
                GL11.glColor3f(1.0F, 1.0F, 1.0F);
                break;
            case KONC3:
                GL11.glLineWidth(1.5F);
                RenderHelper.drawRectangle(0.0F, 0.0F, (float) component.getWidth(), (float) component.getHeight());
                GL11.glColor3f(1.0F, 1.0F, 1.0F);
                break;
            case KAMI:
                GL11.glLineWidth(1.5F);
                RenderHelper.drawRectangle(0.0F, 0.0F, (float) component.getWidth(), (float) component.getHeight());
                GL11.glColor3f(1.0F, 1.0F, 1.0F);
                break;
            case Modern:
                if (component.getHeight() != 12) {
                    GL11.glColor3f(0.13f, 0.13f, 0.13f);
                    GL11.glLineWidth(1.5f);
                    RenderHelper.drawRoundedRectangle(2, 2, component.getWidth(), component.getHeight(), 10);
                    GL11.glLineWidth(1);
                    GL11.glColor4f(.17f, .17f, .18f, .9f);
                    RenderHelper.drawRoundedRectangle(0, 0, component.getWidth(), component.getHeight(), 10);
                }
                GL11.glColor4f(0.13f, 0.13f, 0.13f, 1);
                RenderHelper.drawHalfRoundedRectangle(0, 0, component.getWidth(), 11, 10);
                break;
            case Modern2:
                if (component.getHeight() != 12) {
                    GL11.glColor3f(.17f, .17f, .18f);
                    RenderHelper.drawRoundedRectangle(0, 0, component.getWidth(), component.getHeight(), 10);
                }
                GL11.glColor3f(0.05f, 0.33f, 0.8f);
                RenderHelper.drawHalfRoundedRectangle(0, 0, component.getWidth(), 11, 10);
                break;
        }

        //GL11.glLineWidth(1.5F);
        //RenderHelper.drawRectangle(0.0F, 0.0F, (float)component.getWidth(), (float)component.getHeight());
        //GL11.glColor3f(1.0F, 1.0F, 1.0F);
    }

    private void checkSettingGui(Setting setting) {
        String var2 = setting.getName();
        byte var3 = -1;
        switch(var2.hashCode()) {
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
            case 1153959602:
                if (var2.equals("Blue Border")) {
                    var3 = 6;
                }
                break;
            case 1507972194:
                if (var2.equals("Alpha SettingUI")) {
                    var3 = 3;
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

        switch(var3) {
            case 0:
                redForBG = (Float)setting.getValue();
                break;
            case 1:
                greenForBG = (Float)setting.getValue();
                break;
            case 2:
                blueForBG = (Float)setting.getValue();
                break;
            case 3:
                alphaForST = (Float)setting.getValue();
                break;
            case 4:
                redForBorder = (Float)setting.getValue();
                break;
            case 5:
                greenForBorder = (Float)setting.getValue();
                break;
            case 6:
                blueForBorder = (Float)setting.getValue();
        }
    }
}
