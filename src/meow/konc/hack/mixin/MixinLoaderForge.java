package meow.konc.hack.mixin;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;

import java.util.Map;

public class MixinLoaderForge implements IFMLLoadingPlugin {

    /* This is NOT using KamiMod, as importing it causes the issue described here: https://github.com/SpongePowered/Mixin/issues/388 */
    public static final Logger log = LogManager.getLogger("KONC Client");
    private static boolean isObfuscatedEnvironment = false;

    public MixinLoaderForge() {
        log.info("KONC mixins initialized");
        MixinBootstrap.init();
        Mixins.addConfiguration("mixins.konc.json");
        MixinEnvironment.getDefaultEnvironment().setObfuscationContext("searge");
        log.info(MixinEnvironment.getDefaultEnvironment().getObfuscationContext());
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        isObfuscatedEnvironment = (boolean) data.get("runtimeDeobfuscationEnabled");
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
