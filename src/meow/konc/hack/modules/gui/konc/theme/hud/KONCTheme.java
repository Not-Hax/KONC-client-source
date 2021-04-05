package meow.konc.hack.gui.konc.theme.hud;

import meow.konc.hack.gui.konc.KONCGUI;
import meow.konc.hack.gui.konc.theme.staticui.RadarUI;
import meow.konc.hack.gui.konc.theme.staticui.TabGuiUI;
import meow.konc.hack.gui.rgui.render.AbstractComponentUI;
import meow.konc.hack.gui.rgui.render.font.FontRenderer;
import meow.konc.hack.gui.rgui.render.theme.AbstractTheme;

public class KONCTheme extends AbstractTheme {

    FontRenderer fontRenderer;

    public KONCTheme() {
        installUI(new RootButtonUI<>());
        installUI(new GUIUI());
        installUI(new RootGroupboxUI());
        installUI(new KONCFrameUI<>());
        installUI(new RootScrollpaneUI());
        installUI(new RootInputFieldUI());
        installUI(new RootLabelUI());
        installUI(new RootChatUI());
        installUI(new RootCheckButtonUI());
        installUI(new KONCActiveModulesUI());
        installUI(new KONCSettingsPanelUI());
        installUI(new RootSliderUI());
        installUI(new KONCEnumButtonUI());
        installUI(new RootColorizedCheckButtonUI());
        installUI(new KONCUnboundSliderUI());

        installUI(new RadarUI());
        installUI(new TabGuiUI());

        fontRenderer=KONCGUI.fontRenderer;
    }

    @Override
    public FontRenderer getFontRenderer() {
        return fontRenderer;
    }

    public class GUIUI extends AbstractComponentUI<KONCGUI> {
    }
}
