package meow.konc.hack.gui.rgui.component.container.use;

import java.util.HashMap;
import java.util.Map;

import meow.konc.hack.gui.konc.DisplayGuiScreen;
import meow.konc.hack.gui.rgui.component.Component;
import meow.konc.hack.gui.rgui.component.container.OrganisedContainer;
import meow.konc.hack.gui.rgui.component.listen.MouseListener;
import meow.konc.hack.gui.rgui.component.listen.RenderListener;
import meow.konc.hack.gui.rgui.component.listen.UpdateListener;
import meow.konc.hack.gui.rgui.layout.Layout;
import meow.konc.hack.gui.rgui.layout.UselessLayout;
import meow.konc.hack.gui.rgui.poof.PoofInfo;
import meow.konc.hack.gui.rgui.poof.use.FramePoof;
import meow.konc.hack.gui.rgui.poof.use.Poof;
import meow.konc.hack.gui.rgui.render.theme.Theme;
import meow.konc.hack.gui.rgui.util.Docking;

public class Frame extends OrganisedContainer {

    String title;

    int trueheight = 0;
    int truemaxheight = 0;

    int dx = 0;
    int dy = 0;
    boolean doDrag = false;
    boolean startDrag = false;

    boolean isMinimized = false;
    boolean isMinimizeable = true;
    boolean isCloseable = true;
    boolean isPinned = false;
    boolean isPinneable = false;

    boolean isLayoutWorking = false;
    boolean boxEnabled = true;
    boolean titleEnabled = true;
    boolean isPinDisplayed = true;

    Docking docking = Docking.NONE;

    HashMap<Component, Boolean> visibilityMap = new HashMap<>();

    public Frame(Theme theme, String title) {
        this(theme, new UselessLayout(), title);
    }

    public Frame(Theme theme, Layout layout, String title) {
        super(theme, layout);
        this.title = title;

        addPoof(new FramePoof<Frame, FramePoof.FramePoofInfo>() {
            @Override
            public void execute(Frame component, FramePoofInfo info) {
                switch (info.getAction()) {
                    case MINIMIZE:
                        if (isMinimizeable)
                            setMinimized(true);
                        break;
                    case MAXIMIZE:
                        if (isMinimizeable)
                            setMinimized(false);
                        break;
                    case CLOSE:
                        if (isCloseable)
                            getParent().removeChild(Frame.this);
                        break;
                }
            }
        });

        addUpdateListener(new UpdateListener() {
            @Override
            public void updateSize(Component component, int oldWidth, int oldHeight) {
                if (isLayoutWorking) return;
                if (!component.equals(Frame.this)) {
                    isLayoutWorking = true;
                    layout.organiseContainer(Frame.this);
                    isLayoutWorking = false;
                }
            }

            @Override
            public void updateLocation(Component component, int oldX, int oldY) {
                if (isLayoutWorking) return;

                if (!component.equals(Frame.this)) {
                    isLayoutWorking = true;
                    layout.organiseContainer(Frame.this);
                    isLayoutWorking = false;
                }
            }
        });

        addRenderListener(new RenderListener() {
            @Override
            public void onPreRender() {
                if (startDrag) {
                    FrameDragPoof.DragInfo info = new FrameDragPoof.DragInfo(DisplayGuiScreen.mouseX - dx, DisplayGuiScreen.mouseY - dy);
                    callPoof(FrameDragPoof.class, info);
                    setX(info.getX());
                    setY(info.getY());
                }
            }

            @Override
            public void onPostRender() {

            }
        });

        addMouseListener(new GayMouseListener());
    }

    // what is this naming??
    public class GayMouseListener implements MouseListener {
        @Override
        public void onMouseDown(MouseButtonEvent event) {
            dx = event.getX() + getOriginOffsetX();
            dy = event.getY() + getOriginOffsetY();
            if (dy <= getOriginOffsetY() && event.getButton() == 0 && dy > 0)
                doDrag = true;
            else
                doDrag = false;

            if (isMinimized && event.getY() > getOriginOffsetY())
                event.cancel();
        }

        @Override
        public void onMouseRelease(MouseButtonEvent event) {
            doDrag = false;
            startDrag = false;
        }

        @Override
        public void onMouseDrag(MouseButtonEvent event) {
            if (!doDrag) return;
            startDrag = true;
        }

        @Override
        public void onMouseMove(MouseMoveEvent event) {

        }

        @Override
        public void onScroll(MouseScrollEvent event) {

        }
    }

    public void setCloseable(boolean closeable) {
        isCloseable = closeable;
    }

    public void setMinimizeable(boolean minimizeable) {
        isMinimizeable = minimizeable;
    }

    public boolean isMinimizeable() {
        return isMinimizeable;
    }

    public boolean isMinimized() {
        return isMinimized;
    }

    public void setMinimized(boolean minimized) {
        if (minimized && !isMinimized) {
            trueheight = getHeight();
            truemaxheight = getMaximumHeight();
            setHeight(0);
            setMaximumHeight(getOriginOffsetY());
            for (Component c : getChildren()) {
                visibilityMap.put(c, c.isVisible());
                c.setVisible(false);
            }
        } else if (!minimized && isMinimized) {
            setMaximumHeight(truemaxheight);
            setHeight(trueheight - getOriginOffsetY());
            for (Map.Entry<Component, Boolean> entry : visibilityMap.entrySet()) {
                entry.getKey().setVisible(entry.getValue());
            }
        }

        isMinimized = minimized;
    }

    public boolean isCloseable() { return isCloseable; }

    public boolean isPinned() {
        return isPinned;
    }

    public void setPinned(boolean pinned) {
        isPinned = pinned && isPinneable;
    }

    public String getTitle() {
        return title;
    }

    public Docking getDocking() {
        return docking;
    }

    public void setDocking(Docking docking) {
        this.docking = docking;
    }

    public abstract static class FrameDragPoof<T extends Frame, S extends FrameDragPoof.DragInfo> extends Poof<T, S> {

        public static class DragInfo extends PoofInfo {
            int x;
            int y;

            public DragInfo(int x, int y) {
                this.x = x;
                this.y = y;
            }

            public int getX() {
                return x;
            }

            public int getY() {
                return y;
            }

            public void setX(int x) {
                this.x = x;
            }

            public void setY(int y) {
                this.y = y;
            }
        }
    }

    public void setBox(boolean boxToggle) { boxEnabled = boxToggle; }

    public void setTitleEnabled(boolean titleToggle) { titleEnabled = titleToggle; }

    public boolean isPinneable() {
        return isPinneable;
    }

    public void setPinneable(boolean pinneable) { isPinneable = pinneable; }

    public void displayPinButton(boolean displayPin) { isPinDisplayed = displayPin; }

    public boolean isBoxEnabled() { return boxEnabled; }

}

