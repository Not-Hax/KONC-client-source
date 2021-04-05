package meow.konc.hack.gui.konc.theme.hud;

import meow.konc.hack.KONCMod;
import meow.konc.hack.gui.font.CFontRenderer;
import meow.konc.hack.gui.rgui.component.AlignedComponent;
import meow.konc.hack.gui.rgui.render.AbstractComponentUI;
import meow.konc.hack.gui.rgui.render.font.FontRenderer;
import meow.konc.hack.module.Module;
import meow.konc.hack.module.ModuleManager;
import meow.konc.hack.modules.other.ActiveModules;
import meow.konc.hack.modules.other.ClientConfig;
import meow.konc.hack.util.colour.ColourTextFormatting;
import meow.konc.hack.util.colour.ColourUtils;
import meow.konc.hack.util.packet.Wrapper;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static meow.konc.hack.util.colour.ColourConverter.toF;
import static meow.konc.hack.util.colour.ColourTextFormatting.toTextMap;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.glDisable;

public class KONCActiveModulesUI extends AbstractComponentUI<meow.konc.hack.gui.konc.component.ActiveModules> {
    CFontRenderer cFontRenderer = new CFontRenderer(new Font("MS Reference Sans Serif", Font.PLAIN, 18), true, false);

    ActiveModules activeMods;

    public void renderComponent(meow.konc.hack.gui.konc.component.ActiveModules component, FontRenderer f) {
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        activeMods = (ActiveModules) ModuleManager.getModuleByName("ActiveModules");
        FontRenderer renderer = Wrapper.getFontRenderer();

        List<Module> mods = ModuleManager.getModules().stream()
                .filter(Module::isEnabled)
                .filter(Module -> (activeMods.hidden.getValue() || Module.isOnArray()))
                .sorted(Comparator.comparing(module -> cFontRenderer.getStringWidth(module.getName() + ((module.getHudInfo() == null) ? "" : (module.getHudInfo() + " "))) * (component.sort_up ? -1 : 1)))
                .collect(Collectors.toList());

        final int[] y = {2};

        if (activeMods.potion.getValue() && component.getParent().getY() < 26 && Wrapper.getPlayer().getActivePotionEffects().size() > 0 && component.getParent().getOpacity() == 0.0F)
            y[0] = Math.max(component.getParent().getY(), 26 - component.getParent().getY());

        final float[] hue = {(System.currentTimeMillis() % (360 * activeMods.getRainbowSpeed())) / (360f * activeMods.getRainbowSpeed())};

        Function<Integer, Integer> xFunc;
        switch (component.getAlignment()) {
            case RIGHT:
                xFunc = (i -> component.getWidth() - i);
                break;
            case CENTER:
                xFunc = (i -> component.getWidth() / 2 - i / 2);
                break;
            default:
                xFunc = (i -> 0);
                break;
        }

        for (int i = 0; i < mods.size(); i++) {
            Module module = mods.get(i);
            int rgb;

            switch (activeMods.mode.getValue()) {
                case RAINBOW:
                    rgb = Color.HSBtoRGB(hue[0], toF(activeMods.saturationR.getValue()), toF(activeMods.brightnessR.getValue()));
                    break;
                case CATEGORY:
                    rgb = activeMods.getCategoryColour(module);
                    break;
                case CUSTOM:
                    rgb = Color.HSBtoRGB(toF(activeMods.hueC.getValue()), toF(activeMods.saturationC.getValue()), toF(activeMods.brightnessC.getValue()));
                    break;
                case INFO_OVERLAY:
                    rgb = activeMods.getInfoColour(i);
                    break;
                default:
                    rgb = 0;
            }

            String hudInfo = module.getHudInfo();
            String text = activeMods.getAlignedText(module.getName(), (hudInfo == null ? "" : KONCMod.colour + "7" + "[" + hudInfo + KONCMod.colour + "7" + "]" + KONCMod.colour + "r"), component.getAlignment().equals(AlignedComponent.Alignment.LEFT));
            int textWidth = cFontRenderer.getStringWidth(text);
            int textHeight = renderer.getFontHeight() + 1;
            int red = (rgb >> 16) & 0xFF;
            int green = (rgb >> 8) & 0xFF;
            int blue = rgb & 0xFF;
            int trgb = ColourUtils.toRGBA(red, green, blue, 255);
            if (((ClientConfig) ModuleManager.getModuleByName("ClientConfig")).smoothFont.getValue()) {
                cFontRenderer.drawStringWithShadow(text, xFunc.apply(textWidth), y[0], trgb);
            } else {
                renderer.drawStringWithShadow(xFunc.apply(textWidth), y[0], red, green, blue, text);
            }

            hue[0] = hue[0] - 0.02F;
            y[0] = y[0] + textHeight;
        }
        component.setHeight(y[0]);

        GL11.glEnable(GL11.GL_CULL_FACE);
        glDisable(GL_BLEND);
    }

    @Override
    public void handleSizeComponent(meow.konc.hack.gui.konc.component.ActiveModules component) {
        component.setWidth(100);
        component.setHeight(100);
    }
}