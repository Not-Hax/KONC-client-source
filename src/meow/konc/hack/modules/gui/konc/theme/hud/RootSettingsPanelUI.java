/*package meow.konc.hack.gui.konc.theme.hud;

import meow.konc.hack.gui.konc.RenderHelper;
import meow.konc.hack.gui.konc.component.SettingsPanel;
import meow.konc.hack.gui.rgui.render.AbstractComponentUI;
import meow.konc.hack.gui.rgui.render.font.FontRenderer;
import meow.konc.hack.module.ModuleManager;
import meow.konc.hack.setting.Setting;
import org.lwjgl.opengl.GL11;

public class RootSettingsPanelUI extends AbstractComponentUI<SettingsPanel>
{
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
    public void renderComponent(final SettingsPanel component, final FontRenderer fontRenderer) {
        ModuleManager.getModuleByName("Gui").settingList.forEach(setting -> checkSettingGuiColour(setting));
        GL11.glColor4f(redForBorder, greenForBorder, blueForBorder, 0.2f);
        RenderHelper.drawOutlinedRoundedRectangle(0, 0, component.getWidth(), component.getHeight(), 6.0f, 0.14f, 0.14f, 0.14f, component.getOpacity(), 1.0f);
    }
}*/
