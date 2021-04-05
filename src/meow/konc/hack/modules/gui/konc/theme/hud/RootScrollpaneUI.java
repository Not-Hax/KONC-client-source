package meow.konc.hack.gui.konc.theme.hud;

import meow.konc.hack.gui.konc.RenderHelper;
import meow.konc.hack.gui.rgui.component.Component;
import meow.konc.hack.gui.rgui.component.container.Container;
import meow.konc.hack.gui.rgui.component.container.use.Scrollpane;
import meow.konc.hack.gui.rgui.component.listen.MouseListener;
import meow.konc.hack.gui.rgui.component.listen.RenderListener;
import meow.konc.hack.gui.rgui.render.AbstractComponentUI;
import meow.konc.hack.gui.rgui.render.font.FontRenderer;
import meow.konc.hack.module.ModuleManager;
import meow.konc.hack.setting.Setting;
import org.lwjgl.opengl.GL11;

public class RootScrollpaneUI extends AbstractComponentUI<Scrollpane>
{
    long lastScroll = 0L;
    Component scrollComponent = null;
    float barLife = 1220.0f;
    boolean dragBar = false;
    int dY = 0;
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
    public void renderComponent(Scrollpane component, FontRenderer fontRenderer) {
    }

    @Override
    public void handleAddComponent(Scrollpane component, Container container) {
        component.addMouseListener(new MouseListener() {
            @Override
            public void onMouseDown(MouseButtonEvent event) {
                if (System.currentTimeMillis() - lastScroll < barLife && scrollComponent.liesIn(component) && component.canScrollY()) {
                    double progress = component.getScrolledY() / (double)component.getMaxScrollY();
                    int barHeight = 30;
                    int y = (int)((component.getHeight() - barHeight) * progress);
                    if (event.getX() > component.getWidth() - 10 && event.getY() > y && event.getY() < y + barHeight) {
                        dragBar = true;
                        dY = event.getY() - y;
                        event.cancel();
                    }
                }
            }

            @Override
            public void onMouseRelease(MouseButtonEvent event) {
                dragBar = false;
            }

            @Override
            public void onMouseDrag(MouseButtonEvent event) {
                if (dragBar) {
                    double progress = event.getY() / (double)component.getHeight();
                    progress = Math.max(Math.min(progress, 1.0), 0.0);
                    component.setScrolledY((int)(component.getMaxScrollY() * progress));
                    event.cancel();
                }
            }

            @Override
            public void onMouseMove(MouseMoveEvent event) {
            }

            @Override
            public void onScroll(MouseScrollEvent event) {
                lastScroll = System.currentTimeMillis();
                scrollComponent = event.getComponent();
            }
        });
        component.addRenderListener(new RenderListener() {
            @Override
            public void onPreRender() {
            }

            @Override
            public void onPostRender() {
                if (dragBar) {
                    lastScroll = System.currentTimeMillis();
                }
                if (System.currentTimeMillis() - lastScroll < barLife && scrollComponent.liesIn(component) && component.canScrollY()) {
                    ModuleManager.getModuleByName("Gui").settingList.forEach(setting -> checkSettingGuiColour(setting));
                    float alpha = Math.min(1.0f, (barLife - (System.currentTimeMillis() - lastScroll)) / 100.0f) / 3.0f;
                    if (dragBar) {
                        alpha = 0.4f;
                    }
                    GL11.glColor4f(redForBorder, greenForBorder, blueForBorder, alpha);
                    GL11.glDisable(3553);
                    int barHeight = 30;
                    double progress = component.getScrolledY() / (double)component.getMaxScrollY();
                    int y = (int)((component.getHeight() - barHeight) * progress);
                    RenderHelper.drawRoundedRectangle((float)(component.getWidth() - 6), (float)y, 4.0f, (float)barHeight, 1.0f);
                }
            }
        });
    }
}
