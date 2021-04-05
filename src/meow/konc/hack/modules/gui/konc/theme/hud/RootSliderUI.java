package meow.konc.hack.gui.konc.theme.hud;

import meow.konc.hack.gui.konc.RenderHelper;
import meow.konc.hack.gui.konc.RootSmallFontRenderer;
import meow.konc.hack.gui.rgui.component.container.Container;
import meow.konc.hack.gui.rgui.component.use.Slider;
import meow.konc.hack.gui.rgui.render.AbstractComponentUI;
import meow.konc.hack.gui.rgui.render.font.FontRenderer;
import meow.konc.hack.module.ModuleManager;
import meow.konc.hack.modules.other.Gui;
import meow.konc.hack.setting.Setting;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class RootSliderUI extends AbstractComponentUI<Slider>
{
    RootSmallFontRenderer smallFontRenderer = new RootSmallFontRenderer();
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
                alphaForBG = (Float)setting.getValue();
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
    
    @Override
    public void renderComponent(Slider component, FontRenderer aa) {
        ModuleManager.getModuleByName("Gui").settingList.forEach(setting -> checkSettingGuiColour(setting));
        GL11.glColor4f(redForBorder, greenForBorder, blueForBorder, component.getOpacity());
        GL11.glLineWidth(2.5F);
        int height = component.getHeight();
        double value = component.getValue();
        double w = component.getWidth() * ((value - component.getMinimum()) / (component.getMaximum() - component.getMinimum()));
        float downscale = 1.1f;

        float w_ = (int) w;
        //RGB
        if (((Gui) ModuleManager.getModuleByName("Gui")).rainbow.getValue()) {
            float[] tick_color1 = {(System.currentTimeMillis()  % 11520L) / 11520.0f *  2};
            int color_rgb1  = Color.HSBtoRGB(tick_color1[0], 1, 1);
            float r = (color_rgb1 >> 16 & 0xFF) / 255.0F;
            float g = (color_rgb1 >> 8 & 0xFF)  / 255.0F;
            float b = (color_rgb1  & 0xFF)      / 255.0F;
            GL11.glColor3f(r, g, b);
        } else{
            GL11.glColor3f(redForBorder, greenForBorder, blueForBorder);
        }
        RenderHelper.drawRectangle(0, 0, component.getWidth(), height/downscale);
        RenderHelper.drawFilledRectangle(0, 0, w_, height/downscale);
        GL11.glColor3f(1.0F, 1.0F, 1.0F);

        String s = value + "";
        if (component.isPressed()){
            w_ -= smallFontRenderer.getStringWidth(s)/2;
            w_ = Math.max(0, Math.min(w_, component.getWidth() - smallFontRenderer.getStringWidth(s)));
            smallFontRenderer.drawString((int) w_, 2, s);
        }else{
            smallFontRenderer.drawString(2, 2, component.getText());
            smallFontRenderer.drawString(component.getWidth() - smallFontRenderer.getStringWidth(s) - 2, 2, s);
        }
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }

    @Override
    public void handleAddComponent(Slider component, Container container) {
        component.setHeight(component.getTheme().getFontRenderer().getFontHeight() + 2);
        component.setWidth(smallFontRenderer.getStringWidth(component.getText()) + smallFontRenderer.getStringWidth(component.getMaximum() + "") + 3);
    }
}