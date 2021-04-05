package meow.konc.hack.mixin.client.render;

import meow.konc.hack.module.ModuleManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.EntityLivingBase;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(value = { RenderLivingBase.class }, priority = 9999)
public abstract class MixinRenderLiving <T extends EntityLivingBase>
{

    @Inject(method = {"doRender"}, at = {@At("HEAD")})
    public void doRenderPre(T entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo info) {
        if (ModuleManager.isModuleEnabled("Chams") && entity != null) {
            GL11.glEnable(32823);
            GL11.glPolygonOffset(1.0F, -1100000.0F);
        }
    }

    @Inject(method = {"doRender"}, at = {@At("RETURN")})
    public void doRenderPost(T entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo info) {
        if (ModuleManager.isModuleEnabled("Chams") && entity != null) {
            GL11.glPolygonOffset(1.0F, 1000000.0F);
            GL11.glDisable(32823);
        }
    }
}