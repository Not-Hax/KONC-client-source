package meow.konc.hack.gui.konc.theme.hud;

import meow.konc.hack.gui.font.CFontRenderer;
import meow.konc.hack.gui.rgui.component.container.Container;
import meow.konc.hack.gui.rgui.component.container.use.Groupbox;
import meow.konc.hack.gui.rgui.render.AbstractComponentUI;
import meow.konc.hack.gui.rgui.render.font.FontRenderer;
import meow.konc.hack.module.ModuleManager;
import meow.konc.hack.modules.other.ClientConfig;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class RootGroupboxUI extends AbstractComponentUI<Groupbox> {
    CFontRenderer cFontRenderer = new CFontRenderer(new Font("MS Reference Sans Serif", 0, 18), true, true);

    @Override
    public void renderComponent(Groupbox component, FontRenderer fontRenderer) {
        GL11.glLineWidth(1.0F);
        if (((ClientConfig) ModuleManager.getModuleByName("ClientConfig")).smoothFont.getValue()) {
            cFontRenderer.drawString(component.getName(), 1.0F, 1.0F, Color.white.getRGB());
        } else {
            fontRenderer.drawString(1, 1, component.getName());
        }
        GL11.glColor3f(0.0F, 0.0F, 1.0F);
        GL11.glDisable(3553);
        GL11.glBegin(1);
        GL11.glVertex2d(0.0D, 0.0D);
        GL11.glVertex2d(component.getWidth(), 0.0D);
        GL11.glVertex2d(component.getWidth(), 0.0D);
        GL11.glVertex2d(component.getWidth(), component.getHeight());
        GL11.glVertex2d(component.getWidth(), component.getHeight());
        GL11.glVertex2d(0.0D, component.getHeight());
        GL11.glVertex2d(0.0D, component.getHeight());
        GL11.glVertex2d(0.0D, 0.0D);
        GL11.glEnd();
    }

    @Override
    public void handleMouseDown(Groupbox component, int x, int y, int button) {}

    @Override
    public void handleAddComponent(Groupbox component, Container container) {
        component.setWidth(100);
        component.setHeight(200);
        component.setOriginOffsetY(component.getTheme().getFontRenderer().getFontHeight() + 3);
    }
}
