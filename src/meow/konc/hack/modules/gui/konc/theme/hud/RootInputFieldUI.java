package meow.konc.hack.gui.konc.theme.hud;

import meow.konc.hack.gui.konc.RenderHelper;
import meow.konc.hack.gui.rgui.component.container.Container;
import meow.konc.hack.gui.rgui.component.use.InputField;
import meow.konc.hack.gui.rgui.render.AbstractComponentUI;
import meow.konc.hack.gui.rgui.render.font.FontRenderer;
import org.lwjgl.opengl.GL11;

public class RootInputFieldUI<T extends InputField> extends AbstractComponentUI<InputField> {
    @Override
    public void renderComponent(InputField component, FontRenderer fontRenderer) {
        GL11.glColor3f(0.33F, 0.22F, 0.22F);
        RenderHelper.drawFilledRectangle(0.0F, 0.0F, component.getWidth(), component.getHeight());
        GL11.glLineWidth(1.5F);
        GL11.glColor4f(0.33F, 0.33F, 1.0F, 0.6F);
        RenderHelper.drawRectangle(0.0F, 0.0F, component.getWidth(), component.getHeight());
    }

    @Override
    public void handleAddComponent(InputField component, Container container) {
        component.setWidth(200);
        component.setHeight(component.getTheme().getFontRenderer().getFontHeight());
    }
}
