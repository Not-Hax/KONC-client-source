package meow.konc.hack.gui.rgui.component.container;

import meow.konc.hack.gui.rgui.component.Component;
import meow.konc.hack.gui.rgui.layout.Layout;
import meow.konc.hack.gui.rgui.render.theme.Theme;

public class OrganisedContainer extends AbstractContainer {
    Layout layout;

    public OrganisedContainer(Theme theme, Layout layout) {
        super(theme);
        this.layout = layout;
    }

    public Layout getLayout() {
        return layout;
    }

    public void setLayout(Layout layout) {
        this.layout = layout;
    }

    @Override
    public Container addChild(Component... component) {
        super.addChild(component);
        layout.organiseContainer(this);
        return this;
    }

    @Override
    public void setOriginOffsetX(int originoffsetX) {
        super.setOriginOffsetX(originoffsetX);
        layout.organiseContainer(this);
    }

    @Override
    public void setOriginOffsetY(int originoffsetY) {
        super.setOriginOffsetY(originoffsetY);
        layout.organiseContainer(this);
    }

    @Override
    public String getTitle() {
        return null;
    }
}
