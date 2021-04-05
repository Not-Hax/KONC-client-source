package meow.konc.hack.mixin.client.other;

import meow.konc.hack.module.ModuleManager;
import meow.konc.hack.modules.movement.InventoryMove;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.MovementInput;
import net.minecraft.util.MovementInputFromOptions;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * @author Finz0
 * Used with LGPLv3 license permission
 * https://github.com/S-B99/osiris/blob/master/src/main/java/me/finz0/osiris/mixin/mixins/MixinMovementInputFromOptions.java
 *
 * @see InventoryMove
 */
@Mixin(value = MovementInputFromOptions.class, priority = Integer.MAX_VALUE)
public abstract class MixinMovementInputFromOptions extends MovementInput {
    @Redirect(method = "updatePlayerMoveState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/settings/KeyBinding;isKeyDown()Z"))
    public boolean isKeyPressed(KeyBinding keyBinding) {
        if (Minecraft.getMinecraft().player != null
                && Minecraft.getMinecraft().currentScreen != null
                && !(Minecraft.getMinecraft().currentScreen instanceof GuiChat)
                && ModuleManager.isModuleEnabled("InventoryMove")
                && (((InventoryMove) ModuleManager.getModuleByName("InventoryMove")).sneak.getValue() || !((Minecraft.getMinecraft().gameSettings.keyBindSneak.getKeyCode() == keyBinding.getKeyCode()) && !(((InventoryMove) ModuleManager.getModuleByName("InventoryMove")).sneak.getValue())))) {
            return Keyboard.isKeyDown(keyBinding.getKeyCode());
        }
        return keyBinding.isKeyDown();
    }
}
