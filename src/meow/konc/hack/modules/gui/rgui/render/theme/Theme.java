package meow.konc.hack.gui.rgui.render.theme;

import meow.konc.hack.gui.rgui.component.Component;
import meow.konc.hack.gui.rgui.render.ComponentUI;
import meow.konc.hack.gui.rgui.render.font.FontRenderer;

/**
 * Created by 086 on 25/06/2017.
 */
public interface Theme {
    public ComponentUI getUIForComponent(Component component);

    public FontRenderer getFontRenderer();
}
