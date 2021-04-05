package meow.konc.hack.gui.konc.theme.hud;

import meow.konc.hack.gui.font.CFontRenderer;
import meow.konc.hack.gui.konc.RootSmallFontRenderer;
import meow.konc.hack.gui.konc.component.EnumButton;
import meow.konc.hack.gui.rgui.component.container.Container;
import meow.konc.hack.gui.rgui.render.AbstractComponentUI;
import meow.konc.hack.gui.rgui.render.font.FontRenderer;
import meow.konc.hack.module.ModuleManager;
import meow.konc.hack.modules.other.ClientConfig;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class KONCEnumButtonUI extends AbstractComponentUI<EnumButton> {
    RootSmallFontRenderer smallFontRenderer = new RootSmallFontRenderer();

    CFontRenderer CFontRenderer = new CFontRenderer(new Font("MS Reference Sans Serif", 0, 18), true, true);

    EnumButton modeComponent;

    long lastMS = System.currentTimeMillis();

    public void renderComponent(EnumButton component, FontRenderer aa) {
        if (System.currentTimeMillis() - lastMS > 3000L && modeComponent != null)
            modeComponent = null;
        int c = component.isPressed() ? 11184810 : 14540253;
        if (component.isHovered())
            c = (c & 0x7F7F7F) << 1;
        GL11.glColor3f(1.0F, 1.0F, 1.0F);
        GL11.glEnable(3553);
        int parts = (component.getModes()).length;
        double step = component.getWidth() / parts;
        double startX = step * component.getIndex();
        double endX = step * (component.getIndex() + 1);
        int height = component.getHeight();
        float downscale = 1.1F;
        GL11.glDisable(3553);
        GL11.glColor3f(1.0F, 1.0F, 1.0F);
        GL11.glBegin(1);
        GL11.glVertex2d(startX, (height / downscale));
        GL11.glVertex2d(endX, (height / downscale));
        GL11.glEnd();
        if (modeComponent == null || !modeComponent.equals(component)) {
            if (((ClientConfig) ModuleManager.getModuleByName("ClientConfig")).smoothFont.getValue()) {
                GL11.glDisable(2884);
                GL11.glEnable(3042);
                GL11.glEnable(3553);
                CFontRenderer.drawStringWithShadow(component.getName(), 0.0D, 0.0D, c);
                CFontRenderer.drawStringWithShadow(component.getIndexMode(), (component.getWidth() - smallFontRenderer.getStringWidth(component.getIndexMode())), 0.0D, c);
                GL11.glEnable(2884);
                GL11.glDisable(3042);
                GL11.glDisable(3553);
            } else {
                smallFontRenderer.drawString(0, 0, c, component.getName());
                smallFontRenderer.drawString(component.getWidth() - smallFontRenderer.getStringWidth(component.getIndexMode()), 0, c, component.getIndexMode());
            }
        } else if (((ClientConfig) ModuleManager.getModuleByName("ClientConfig")).smoothFont.getValue()) {
            GL11.glDisable(2884);
            GL11.glEnable(3042);
            GL11.glEnable(3553);
            CFontRenderer.drawStringWithShadow(component.getIndexMode(), (component.getWidth() - smallFontRenderer.getStringWidth(component.getIndexMode())), 0.0D, c);
            GL11.glEnable(2884);
            GL11.glDisable(3042);
            GL11.glDisable(3553);
        } else {
            smallFontRenderer.drawString(component.getWidth() / 2 - smallFontRenderer.getStringWidth(component.getIndexMode()) / 2, 0, c, component.getIndexMode());
        }
        GL11.glDisable(3042);
    }

    public void handleSizeComponent(EnumButton component) {
        int width = 0;
        for (String s : component.getModes())
            width = Math.max(width, smallFontRenderer.getStringWidth(s));
        component.setWidth(smallFontRenderer.getStringWidth(component.getName()) + width + 1);
        component.setHeight(smallFontRenderer.getFontHeight() + 2);
    }

    public void handleAddComponent(EnumButton component, Container container) {
        component.addPoof(new EnumButton.EnumbuttonIndexPoof<EnumButton, EnumButton.EnumbuttonIndexPoof.EnumbuttonInfo>() {
            public void execute(EnumButton component, EnumbuttonInfo info) {
                modeComponent = component;
                lastMS = System.currentTimeMillis();
            }
        });
    }
}
