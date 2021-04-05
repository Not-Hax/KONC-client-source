package meow.konc.hack.mixin.client.other;

import meow.konc.hack.modules.hidden.capes.Capes;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public class MixinAbstractClientPlayer {

    @Inject(method = "getLocationCape", at = @At(value = "RETURN"), cancellable = true)
    public void getCape(CallbackInfoReturnable<ResourceLocation> callbackInfo) {
        if (Capes.INSTANCE == null)
            return;
        if ((!Capes.INSTANCE.overrideOtherCapes.getValue()) && (callbackInfo.getReturnValue() != null))
            return;
        // This is weird with a capital W.
        // Essentially, the "mixin class" content is actually aliased over to the actual target class.
        // But that's a runtime thing, so the Java Compiler doesn't know anything about this.
        ResourceLocation KONCCape = Capes.getCapeResource((AbstractClientPlayer) (Object) this);
        if (KONCCape != null)
            callbackInfo.setReturnValue(KONCCape);
    }
}