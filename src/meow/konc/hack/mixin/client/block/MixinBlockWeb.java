package meow.konc.hack.mixin.client.block;

import meow.konc.hack.module.ModuleManager;
import meow.konc.hack.modules.movement.NoSlow;
import net.minecraft.block.BlockWeb;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @see MixinBlockSoulSand
 * @author 086
 */
@Mixin(BlockWeb.class)
public class MixinBlockWeb {

    @Inject(method = "onEntityCollision", at = @At("HEAD"), cancellable = true)
    public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn, CallbackInfo info) {
        // If NoSlowdown is on, just don't do anything else in this method (slow the player)
        if (ModuleManager.isModuleEnabled("NoSlow") && ((NoSlow) ModuleManager.getModuleByName("NoSlow")).cobweb.getValue()) info.cancel();
    }

}
