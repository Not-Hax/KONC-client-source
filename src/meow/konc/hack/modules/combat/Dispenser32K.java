package meow.konc.hack.modules.combat;

import meow.konc.hack.command.Command;
import meow.konc.hack.module.Module;
import meow.konc.hack.module.ModuleManager;
import meow.konc.hack.setting.Setting;
import meow.konc.hack.setting.Settings;
import meow.konc.hack.util.packet.BlockInteractionHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockLiquid;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.math.RoundingMode;
import java.text.DecimalFormat;

@Module.Info(name = "Dispenser32k", category = Module.Category.COMBAT, description = "Do not use with any AntiGhostBlock Mod!")
public class Dispenser32K extends Module
{
    private static DecimalFormat df;
    private Setting<Boolean> rotate = register(Settings.b("Rotate", true));
    private Setting<Boolean> grabItem = register(Settings.b("Grab Item", true));
    private Setting<Boolean> autoEnableHitKillAura = register(Settings.b("Auto enable Hit KillAura", false));
    private Setting<Boolean> debugMessages = register(Settings.b("Debug Messages", true));
    private int stage;
    private BlockPos placeTarget;
    private int obiSlot;
    private int dispenserSlot;
    private int shulkerSlot;
    private int redstoneSlot;
    private int hopperSlot;
    private boolean isSneaking;

    @Override
    protected void onEnable() {
        if (mc.player == null || ModuleManager.isModuleEnabled("Freecam")) {
            disable();
            return;
        }
        df.setRoundingMode(RoundingMode.CEILING);
        stage = 0;
        placeTarget = null;
        obiSlot = -1;
        dispenserSlot = -1;
        shulkerSlot = -1;
        redstoneSlot = -1;
        hopperSlot = -1;
        isSneaking = false;
        for (int i = 0; i < 9 && (obiSlot == -1 || dispenserSlot == -1 || shulkerSlot == -1 || redstoneSlot == -1 || hopperSlot == -1); ++i) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack != ItemStack.EMPTY) {
                if (stack.getItem() instanceof ItemBlock) {
                    Block block = ((ItemBlock)stack.getItem()).getBlock();
                    if (block == Blocks.HOPPER) {
                        hopperSlot = i;
                    }
                    else if (BlockInteractionHelper.shulkerList.contains(block)) {
                        shulkerSlot = i;
                    }
                    else if (block == Blocks.OBSIDIAN) {
                        obiSlot = i;
                    }
                    else if (block == Blocks.DISPENSER) {
                        dispenserSlot = i;
                    }
                    else if (block == Blocks.REDSTONE_BLOCK) {
                        redstoneSlot = i;
                    }
                }
            }
        }
        if (obiSlot == -1 || dispenserSlot == -1 || shulkerSlot == -1 || redstoneSlot == -1 || hopperSlot == -1) {
            if (debugMessages.getValue()) {
                Command.sendChatMessage("[Auto32k] Items missing, disabling.");
            }
            disable();
            return;
        }
        placeTarget = mc.objectMouseOver.getBlockPos().up();
        if (placeTarget == null) {
            if (debugMessages.getValue()) {
                Command.sendChatMessage("[Auto32k] No valid position to place at!");
            }
            disable();
            return;
        }
        if (debugMessages.getValue()) {
            Command.sendChatMessage("[Auto32k] Place Target: " + placeTarget.x + " " + placeTarget.y + " " + placeTarget.z + " Distance: " + df.format(mc.player.getPositionVector().distanceTo(new Vec3d(placeTarget))));
        }
    }

    @Override
    public void onUpdate() {
        if (mc.player == null || ModuleManager.isModuleEnabled("Freecam")) {
            return;
        }
        if (stage == 0) {
            mc.player.inventory.currentItem = obiSlot;
            placeBlock(new BlockPos(placeTarget), EnumFacing.DOWN);
            mc.player.inventory.currentItem = dispenserSlot;
            placeBlock(new BlockPos(placeTarget.add(0, 1, 0)), EnumFacing.DOWN);
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
            isSneaking = false;
            mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(placeTarget.add(0, 1, 0), EnumFacing.DOWN, EnumHand.MAIN_HAND, 0.0f, 0.0f, 0.0f));
            stage = 1;
            return;
        }
        if (stage == 1) {
            if (!(mc.currentScreen instanceof GuiContainer)) {
                return;
            }
            mc.playerController.windowClick(mc.player.openContainer.windowId, 1, shulkerSlot, ClickType.SWAP, mc.player);
            mc.player.closeScreen();
            mc.player.inventory.currentItem = redstoneSlot;
            placeBlock(new BlockPos(placeTarget.add(0, 2, 0)), EnumFacing.DOWN);
            stage = 2;
        }
        else {
            if (stage != 2) {
                if (stage == 3) {
                    if (!(mc.currentScreen instanceof GuiContainer)) {
                        return;
                    }
                    if (((GuiContainer)mc.currentScreen).inventorySlots.getSlot(0).getStack().isEmpty) {
                        return;
                    }
                    mc.playerController.windowClick(mc.player.openContainer.windowId, 0, mc.player.inventory.currentItem, ClickType.SWAP, mc.player);
                    if (autoEnableHitKillAura.getValue()) {
                        ModuleManager.getModuleByName("Kill32kAura").enable();
                    }
                    disable();
                }
                return;
            }
            Block block = mc.world.getBlockState(placeTarget.offset(mc.player.getHorizontalFacing().getOpposite()).up()).getBlock();
            if (block instanceof BlockAir || block instanceof BlockLiquid) {
                return;
            }
            mc.player.inventory.currentItem = hopperSlot;
            placeBlock(new BlockPos(placeTarget.offset(mc.player.getHorizontalFacing().getOpposite())), mc.player.getHorizontalFacing());
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
            isSneaking = false;
            mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(placeTarget.offset(mc.player.getHorizontalFacing().getOpposite()), EnumFacing.DOWN, EnumHand.MAIN_HAND, 0.0f, 0.0f, 0.0f));
            mc.player.inventory.currentItem = shulkerSlot;
            if (!grabItem.getValue()) {
                disable();
                return;
            }
            stage = 3;
        }
    }

    private void placeBlock(BlockPos pos, EnumFacing side) {
        BlockPos neighbour = pos.offset(side);
        EnumFacing opposite = side.getOpposite();
        if (!isSneaking) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
            isSneaking = true;
        }
        Vec3d hitVec = new Vec3d(neighbour).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));
        if (rotate.getValue()) {
            BlockInteractionHelper.faceVectorPacketInstant(hitVec);
        }
        mc.playerController.processRightClickBlock(mc.player, mc.world, neighbour, opposite, hitVec, EnumHand.MAIN_HAND);
        mc.player.swingArm(EnumHand.MAIN_HAND);
    }

    static {
        df = new DecimalFormat("#.#");
    }
}
