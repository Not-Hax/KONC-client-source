package meow.konc.hack.modules.combat;

import java.util.concurrent.TimeUnit;

import com.mojang.realmsclient.gui.ChatFormatting;
import meow.konc.hack.command.Command;
import meow.konc.hack.module.Module;
import meow.konc.hack.module.Module.Info;
import meow.konc.hack.setting.Setting;
import meow.konc.hack.setting.Settings;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

// made by travsi :D

@Info(name = "SelfWeb", category = Module.Category.COMBAT)
public class SelfWeb extends Module {

    private Setting<Integer> delay = register(Settings.integerBuilder("Delay").withRange(0, 10).withValue(3).build());
    private Setting<Boolean> announceUsage = register(Settings.b("Announce Usage", true));

    BlockPos feet;

    public static float yaw;
    public static float pitch;

    int d;

    public boolean isInBlockRange(Entity target) {
        return (target.getDistance(mc.player) <= 4.0F);
    }

    public static boolean canBeClicked(BlockPos pos) {
        return mc.world.getBlockState(pos).getBlock().canCollideCheck(mc.world.getBlockState(pos),
                false);
    }

    private boolean isStackObby(ItemStack stack) {
        return (stack != null && stack.getItem() == Item.getItemById(30));
    }

    private boolean doesHotbarHaveWeb() {
        for (int i = 36; i < 45; i++) {
            ItemStack stack = mc.player.inventoryContainer.getSlot(i).getStack();
            if (stack != null && isStackObby(stack)) {
                return true;
            }
        }
        return false;
    }

    public static Block getBlock(BlockPos pos) {
        return getState(pos).getBlock();
    }

    public static IBlockState getState(BlockPos pos) {
        return mc.world.getBlockState(pos);
    }

    public static boolean placeBlockLegit(BlockPos pos) {
        Vec3d eyesPos = new Vec3d(mc.player.posX,
                mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ);
        Vec3d posVec = (new Vec3d(pos)).add(0.5D, 0.5D, 0.5D);
        for (EnumFacing side : EnumFacing.values()) {
            BlockPos neighbor = pos.offset(side);
            if (canBeClicked(neighbor)) {
                Vec3d hitVec = posVec.add((new Vec3d(side.getDirectionVec())).scale(0.5D));
                if (eyesPos.squareDistanceTo(hitVec) <= 36.0D) {
                    mc.playerController.processRightClickBlock(mc.player, mc.world, neighbor,
                            side.getOpposite(), hitVec, EnumHand.MAIN_HAND);
                    mc.player.swingArm(EnumHand.MAIN_HAND);
                    try {
                        TimeUnit.MILLISECONDS.sleep(10L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public void onUpdate() {
        if (mc.player.isHandActive()) {
            return;
        }
        trap(mc.player);
    }

    public static double roundToHalf(double d) {
        return Math.round(d * 2.0D) / 2.0D;
    }

    public void onEnable() {
        if (mc.player == null) {
            disable();
            return;
        }
        if (announceUsage.getValue()) {
            Command.sendChatMessage("[SelfWeb] " + ChatFormatting.GREEN.toString() + "Enabled" + ChatFormatting.RESET.toString() + "!");
        }
        d = 0;
    }

    private void trap(EntityPlayer player) {
        if (player.moveForward == 0.0D && player.moveStrafing == 0.0D
                && player.moveForward == 0.0D) {
            d++;
        }
        if (player.moveForward != 0.0D || player.moveStrafing != 0.0D
                || player.moveForward != 0.0D) {
            d = 0;
        }
        if (!doesHotbarHaveWeb()) {
            d = 0;
        }
        if (d == delay.getValue() && doesHotbarHaveWeb()) {
            feet = new BlockPos(player.posX, player.posY, player.posZ);

            for (int i = 36; i < 45; i++) {
                ItemStack stack = mc.player.inventoryContainer.getSlot(i).getStack();
                if (stack != null && isStackObby(stack)) {
                    int oldSlot = mc.player.inventory.currentItem;
                    if (mc.world.getBlockState(feet).getMaterial().isReplaceable()) {
                        mc.player.inventory.currentItem = i - 36;
                        if (mc.world.getBlockState(feet).getMaterial().isReplaceable()) {
                            placeBlockLegit(feet);
                        }


                        mc.player.inventory.currentItem = oldSlot;
                        d = 0;
                        break;
                    }
                    d = 0;
                }
                d = 0;
            }
        }
    }

    public void onDisable() {
        d = 0;
        if (announceUsage.getValue()) {
            Command.sendChatMessage("[Selfweb] " + ChatFormatting.RED.toString() + "Disabled" + ChatFormatting.RESET.toString() + "!");
        }
        yaw = mc.player.rotationYaw;
        pitch = mc.player.rotationPitch;
    }

    public EnumFacing getEnumFacing(float posX, float posY, float posZ) {
        return EnumFacing.getFacingFromVector(posX, posY, posZ);
    }

    public BlockPos getBlockPos(double x, double y, double z) {
        return new BlockPos(x, y, z);
    }
}