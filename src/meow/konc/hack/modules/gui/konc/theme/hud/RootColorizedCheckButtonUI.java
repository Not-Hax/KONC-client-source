package meow.konc.hack.gui.konc.theme.hud;

import meow.konc.hack.gui.konc.KONCGUI;
import meow.konc.hack.gui.konc.RootSmallFontRenderer;
import meow.konc.hack.gui.konc.component.ColorizedCheckButton;
import meow.konc.hack.gui.rgui.component.use.CheckButton;
import meow.konc.hack.gui.rgui.render.font.FontRenderer;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class RootColorizedCheckButtonUI extends RootCheckButtonUI<ColorizedCheckButton> {
    RootSmallFontRenderer ff = new RootSmallFontRenderer();

    public RootColorizedCheckButtonUI() {
        backgroundColour = new Color(backgroundColour.getRed(), backgroundColour.getGreen(), backgroundColour.getBlue());
        backgroundColourHover = new Color(backgroundColourHover.getRed(), backgroundColourHover.getGreen(), backgroundColourHover.getBlue());
        downColourNormal = new Color(190, 190, 190);
    }

    @Override
    public void renderComponent(CheckButton component, FontRenderer aa) {
        GL11.glColor4f(backgroundColour.getRed() / 255.0F, backgroundColour.getGreen() / 255.0F, backgroundColour.getBlue() / 255.0F, component.getOpacity());
        if (component.isHovered() || component.isPressed()) {
            GL11.glColor4f(backgroundColourHover.getRed() / 255.0F, backgroundColourHover.getGreen() / 255.0F, backgroundColourHover.getBlue() / 255.0F, component.getOpacity());
        }
        if (component.isToggled()) {
            GL11.glColor3f(backgroundColour.getRed() / 255.0F, backgroundColour.getGreen() / 255.0F, backgroundColour.getBlue() / 255.0F);
        }
        GL11.glLineWidth(2.5F);
        GL11.glBegin(1);
        GL11.glVertex2d(0.0D, component.getHeight());
        GL11.glVertex2d(component.getWidth(), component.getHeight());
        GL11.glEnd();
        Color idleColour = component.isToggled() ? idleColourToggle : idleColourNormal;
        Color downColour = component.isToggled() ? downColourToggle : downColourNormal;
        GL11.glColor3f(1.0F, 1.0F, 1.0F);
        GL11.glEnable(3553);
        ff.drawString(component.getWidth() / 2 - KONCGUI.fontRenderer.getStringWidth(component.getName()) / 2, 0, component.isPressed() ? downColour : idleColour, component.getName());
        GL11.glDisable(3553);
    }
}
