package meow.konc.hack.gui.rgui.util;

import java.util.ArrayList;
import java.util.List;
import meow.konc.hack.gui.rgui.GUI;
import meow.konc.hack.gui.rgui.component.AlignedComponent;
import meow.konc.hack.gui.rgui.component.Component;
import meow.konc.hack.gui.rgui.component.container.Container;
import meow.konc.hack.gui.rgui.render.theme.Theme;

public class ContainerHelper {

    public static void setTheme(Container parent, Theme newTheme) {
        Theme old = parent.getTheme();
        parent.setTheme(newTheme);
        for (Component c : parent.getChildren()) {
            if (c.getTheme().equals(old))
                c.setTheme(newTheme);
        }
    }

    public static void setAlignment(Container container, AlignedComponent.Alignment alignment) {
        for (Component component : container.getChildren()) {
            if (component instanceof Container)
                setAlignment((Container) component, alignment);
            if (component instanceof AlignedComponent)
                ((AlignedComponent) component).setAlignment(alignment);
        }
    }

    public static AlignedComponent.Alignment getAlignment(Container container) {
        for (Component component : container.getChildren()) {
            if (component instanceof Container)
                return getAlignment((Container) component);
            if (component instanceof AlignedComponent)
                return ((AlignedComponent) component).getAlignment();
        }
        return AlignedComponent.Alignment.LEFT;
    }

    public static Component getHighParent(Component child) {
        if (child.getParent() instanceof GUI || child.getParent() == null)
            return child;
        return getHighParent(child.getParent());
    }

    public static <T extends Component> T getFirstParent(Class<? extends T> parentClass, Component component) {
        if (component.getClass().equals(parentClass))
            return (T) component;
        if (component == null)
            return null;
        return getFirstParent(parentClass, component.getParent());
    }

    public static <S extends Component> List<S> getAllChildren(Class<? extends S> childClass, Container parent) {
        ArrayList<S> list = new ArrayList<>();
        for (Component c : parent.getChildren()) {
            if (childClass.isAssignableFrom(c.getClass()))
                list.add((S) c);
            if (c instanceof Container) {
                list.addAll(getAllChildren(childClass, (Container) c));
            }
        }
        return list;
    }

}
