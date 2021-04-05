package meow.konc.hack.gui.rgui.render;

import meow.konc.hack.gui.rgui.component.Component;
import meow.konc.hack.gui.rgui.component.container.Container;
import meow.konc.hack.gui.rgui.render.font.FontRenderer;

public interface ComponentUI<T extends Component> {

    public void renderComponent(T component, FontRenderer fontRenderer);

    public void handleMouseDown(T component, int x, int y, int button);

    public void handleMouseRelease(T component, int x, int y, int button);

    public void handleMouseDrag(T component, int x, int y, int button);

    public void handleScroll(T component, int x, int y, int amount, boolean up);

    public void handleKeyDown(T component, int key);

    public void handleKeyUp(T component, int key);

    public void handleAddComponent(T component, Container container);

    public void handleSizeComponent(T component);

    public Class<? extends Component> getHandledClass();

}

