package meow.konc.hack.modules.combat;

import meow.konc.hack.command.Command;
import meow.konc.hack.module.Module;
import meow.konc.hack.module.ModuleManager;
import meow.konc.hack.setting.Setting;
import meow.konc.hack.setting.Settings;
import meow.konc.hack.util.packet.BlockInteractionHelper;
import meow.konc.hack.util.util.EntityUtil;
import meow.konc.hack.util.other.Friends;
import meow.konc.hack.util.packet.Wrapper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockWeb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.List;

@Module.Info(name = "AutoWeb", category = Module.Category.COMBAT)
public class AutoWeb extends Module
{
    private Setting<Double> range = register(Settings.d("Range", 5.5));
    private Setting<Boolean> rotate = register(Settings.b("Rotate", true));
    private Setting<Double> blockPerTick = register(Settings.d("Blocks per Tick", 8.0));
    private Setting<Boolean> spoofRotations = register(Settings.b("Spoof Rotations", true));
    private Setting<Boolean> spoofHotbar = register(Settings.b("Spoof Hotbar", false));
    private Setting<Boolean> debugMessages = register(Settings.b("Debug Messages", false));

    private Vec3d[] offsetList =
            new Vec3d[] { new Vec3d(0.0, 2.0, 0.0),
            new Vec3d(0.0, 1.0, 0.0),
            new Vec3d(0.0, 0.0, 0.0) };

    private boolean slowModeSwitch = false;
    private int playerHotbarSlot = -1;
    private EntityPlayer closestTarget;
    private int lastHotbarSlot = -1;
    private int offsetStep = 0;

    @Override
    public void onUpdate() {
        if (isDisabled() || mc.player == null || ModuleManager.isModuleEnabled("Freecam")) {
            return;
        }
        if (closestTarget == null) {
            return;
        }
        if (slowModeSwitch) {
            slowModeSwitch = false;
            return;
        }
        for (int i = 0; i < (int)Math.floor(blockPerTick.getValue()); ++i) {
            if (debugMessages.getValue()) {
                Command.sendChatMessage("[AutoWeb] Loop iteration: " + offsetStep);
            }
            if (offsetStep >= offsetList.length) {
                endLoop();
                return;
            }
            Vec3d offset = offsetList[offsetStep];
            placeBlock(new BlockPos(closestTarget.getPositionVector()).down().add(offset.x, offset.y, offset.z));
            ++offsetStep;
        }
        slowModeSwitch = true;
    }

    private void placeBlock(BlockPos blockPos) {
        if (!Wrapper.getWorld().getBlockState(blockPos).getMaterial().isReplaceable()) {
            if (debugMessages.getValue()) {
                Command.sendChatMessage("[AutoWeb] Block is already placed, skipping");
            }
            return;
        }
        if (!BlockInteractionHelper.checkForNeighbours(blockPos)) {
            return;
        }
        placeBlockExecute(blockPos);
    }

    public void placeBlockExecute(BlockPos pos) {
        Vec3d eyesPos = new Vec3d(Wrapper.getPlayer().posX, Wrapper.getPlayer().posY + Wrapper.getPlayer().getEyeHeight(), Wrapper.getPlayer().posZ);
        for (EnumFacing side : EnumFacing.values()) {
            BlockPos neighbor = pos.offset(side);
            EnumFacing side2 = side.getOpposite();
            if (!BlockInteractionHelper.canBeClicked(neighbor)) {
                if (debugMessages.getValue()) {
                    Command.sendChatMessage("[AutoWeb] No neighbor to click at!");
                }
            }
            else {
                Vec3d hitVec = new Vec3d(neighbor).add(0.5, 0.5, 0.5).add(new Vec3d(side2.getDirectionVec()).scale(0.5));
                if (eyesPos.squareDistanceTo(hitVec) > 18.0625) {
                    if (debugMessages.getValue()) {
                        Command.sendChatMessage("[AutoWeb] Distance > 4.25 blocks!");
                    }
                }
                else {
                    if (spoofRotations.getValue()) {
                        BlockInteractionHelper.faceVectorPacketInstant(hitVec);
                    }
                    boolean needSneak = false;
                    Block blockBelow = mc.world.getBlockState(neighbor).getBlock();
                    if (BlockInteractionHelper.blackList.contains(blockBelow) || BlockInteractionHelper.shulkerList.contains(blockBelow)) {
                        if (debugMessages.getValue()) {
                            Command.sendChatMessage("[AutoWeb] Sneak enabled!");
                        }
                        needSneak = true;
                    }
                    if (needSneak) {
                        mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
                    }
                    int obiSlot = findObiInHotbar();
                    if (obiSlot == -1) {
                        if (debugMessages.getValue()) {
                            Command.sendChatMessage("[AutoWeb] No Obi in Hotbar, disabling!");
                        }
                        disable();
                        return;
                    }
                    if (lastHotbarSlot != obiSlot) {
                        if (debugMessages.getValue()) {
                            Command.sendChatMessage("[AutoWeb Setting Slot to Obi at  = " + obiSlot);
                        }
                        if (spoofHotbar.getValue()) {
                            mc.player.connection.sendPacket(new CPacketHeldItemChange(obiSlot));
                        }
                        else {
                            Wrapper.getPlayer().inventory.currentItem = obiSlot;
                        }
                        lastHotbarSlot = obiSlot;
                    }
                    if (rotate.getValue()) {
                        BlockInteractionHelper.faceVectorPacketInstant(hitVec);
                    }
                    mc.playerController.processRightClickBlock(mc.player, mc.world, neighbor, side2, hitVec, EnumHand.MAIN_HAND);
                    mc.player.swingArm(EnumHand.MAIN_HAND);
                    mc.rightClickDelayTimer = 4;

                    if (needSneak) {
                        if (debugMessages.getValue()) {
                            Command.sendChatMessage("[WebAurav] Sneak disabled!");
                        }
                        mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
                    }
                    return;
                }
            }
        }
    }

    private int findObiInHotbar() {
        int slot = -1;
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = Wrapper.getPlayer().inventory.getStackInSlot(i);
            if (stack != ItemStack.EMPTY && stack.getItem() instanceof ItemBlock) {
                Block block = ((ItemBlock)stack.getItem()).getBlock();
                if (block instanceof BlockWeb) {
                    slot = i;
                    break;
                }
            }
        }
        return slot;
    }

    private void findTarget() {
        List<EntityPlayer> playerList = Wrapper.getWorld().playerEntities;
        for (EntityPlayer target : playerList) {
            if (target == mc.player) {
                continue;
            }
            if (Friends.isFriend(target.getName())) {
                continue;
            }
            if (!EntityUtil.isLiving(target)) {
                continue;
            }
            if (target.getHealth() <= 0.0f) {
                continue;
            }
            double currentDistance = Wrapper.getPlayer().getDistance(target);
            if (currentDistance > range.getValue()) {
                continue;
            }
            if (closestTarget == null) {
                closestTarget = target;
            }
            else {
                if (currentDistance >= Wrapper.getPlayer().getDistance(closestTarget)) {
                    continue;
                }
                closestTarget = target;
            }
        }
    }

    private void endLoop() {
        offsetStep = 0;
        if (debugMessages.getValue()) {
            Command.sendChatMessage("[AutoWeb] Ending Loop");
        }
        if (lastHotbarSlot != playerHotbarSlot && playerHotbarSlot != -1) {
            if (debugMessages.getValue()) {
                Command.sendChatMessage("[AutoWeb] Setting Slot back to  = " + playerHotbarSlot);
            }
            if (spoofHotbar.getValue()) {
                mc.player.connection.sendPacket(new CPacketHeldItemChange(playerHotbarSlot));
            }
            else {
                Wrapper.getPlayer().inventory.currentItem = playerHotbarSlot;
            }
            lastHotbarSlot = playerHotbarSlot;
        }
        findTarget();
    }

    @Override
    protected void onEnable() {
        if (mc.player == null) {
            disable();
            return;
        }
        if (debugMessages.getValue()) {
            Command.sendChatMessage("[AutoWeb] Enabling");
        }
        playerHotbarSlot = Wrapper.getPlayer().inventory.currentItem;
        lastHotbarSlot = -1;
        if (debugMessages.getValue()) {
            Command.sendChatMessage("[AutoWeb] Saving initial Slot  = " + playerHotbarSlot);
        }
        findTarget();
    }

    @Override
    protected void onDisable() {
        if (mc.player == null) {
            return;
        }
        if (debugMessages.getValue()) {
            Command.sendChatMessage("[AutoWeb] Disabling");
        }
        if (lastHotbarSlot != playerHotbarSlot && playerHotbarSlot != -1) {
            if (debugMessages.getValue()) {
                Command.sendChatMessage("[AutoWeb] Setting Slot to  = " + playerHotbarSlot);
            }
            if (spoofHotbar.getValue()) {
                mc.player.connection.sendPacket(new CPacketHeldItemChange(playerHotbarSlot));
            }
            else {
                Wrapper.getPlayer().inventory.currentItem = playerHotbarSlot;
            }
        }
        playerHotbarSlot = -1;
        lastHotbarSlot = -1;
    }
}
