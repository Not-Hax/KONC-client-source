package meow.konc.hack.modules.combat;

import com.mojang.realmsclient.gui.ChatFormatting;
import meow.konc.hack.command.Command;
import meow.konc.hack.module.Module;
import meow.konc.hack.module.ModuleManager;
import meow.konc.hack.modules.exploits.NoBreakAnimation;
import meow.konc.hack.setting.Setting;
import meow.konc.hack.setting.Settings;
import meow.konc.hack.util.other.Friends;
import meow.konc.hack.util.packet.BlockInteractionHelper;
import meow.konc.hack.util.util.EntityUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockObsidian;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static meow.konc.hack.command.Command.sendChatMessage;
import static meow.konc.hack.util.packet.BlockInteractionHelper.canBeClicked;
import static meow.konc.hack.util.packet.BlockInteractionHelper.faceVectorPacketInstant;


/**
 * @author hub
 * @since 2019-8-6
 */
@Module.Info(name = "AutoTrap", category = Module.Category.COMBAT, description = "Traps your enemies in obsidian")
public class AutoTrap extends Module {

    private Setting<Double> range = register(Settings.doubleBuilder("Range").withMinimum(3.5).withValue(5.5).withMaximum(10.0).build());
    private Setting<Integer> blocksPerTick = register(Settings.integerBuilder("BlocksPerTick").withMinimum(1).withValue(2).withMaximum(23).build());
    private Setting<Integer> tickDelay = register(Settings.integerBuilder("TickDelay").withMinimum(0).withValue(2).withMaximum(10).build());

    private Setting<Boolean> triggerable = register(Settings.b("Triggerable", false));
    private Setting<Integer> timeoutTicks = register(Settings.integerBuilder("TimeoutTicks").withMinimum(1).withValue(40).withMaximum(100).withVisibility(b -> triggerable.getValue()).build());

    private Setting<Cage> cage = register(Settings.e("Cage", Cage.TRAP));
    private Setting<Boolean> rotate = register(Settings.b("Rotate", true));
    private Setting<Boolean> noGlitchBlocks = register(Settings.b("NoGlitchBlocks", true));
    private Setting<Boolean> activeInFreecam = register(Settings.b("Active In Freecam", true));
    private Setting<Boolean> infoMessage = register(Settings.b("Debug", true));
    private Setting<Boolean> announceUsage = register(Settings.b("AnnounceUsage", true));
    private Setting<Boolean> Web = register(Settings.b("Web", true));
    private Setting<Boolean> selfTrap = register(Settings.b("Self Trap", false));
    private Setting<Boolean> extrablock = register(Settings.b("Extra Block", false));

    private EntityPlayer closestTarget;
    private String lastTickTargetName;

    private int playerHotbarSlot = -1;
    private int totalTicksRunning = 0;
    private int lastHotbarSlot = -1;
    private boolean isSneaking = false;

    private int delayStep = 0;
    private int offsetStep = 0;
    private boolean firstRun;
    private boolean missingObiDisable = false;

    private Vec3d[] offsetsExtra = new Vec3d[]{new Vec3d(0.0D, 4.0D, 0.0D)};

    private static EnumFacing getPlaceableSide(BlockPos pos) {
        for (EnumFacing side : EnumFacing.values()) {
            BlockPos neighbour = pos.offset(side);
            if (!mc.world.getBlockState(neighbour).getBlock().canCollideCheck(mc.world.getBlockState(neighbour), false)) {
                continue;
            }

            IBlockState blockState = mc.world.getBlockState(neighbour);
            if (!blockState.getMaterial().isReplaceable()) {
                return side;
            }
        }

        return null;
    }

    @Override
    protected void onEnable() {
        if (mc.player == null || mc.player.getHealth() <= 0) return;

        firstRun = true;

        // save initial player hand
        playerHotbarSlot = mc.player.inventory.currentItem;
        lastHotbarSlot = -1;

        if (Web.getValue()) {
            ModuleManager.getModuleByName("AutoWeb").enable();
        }

    }

    @Override
    protected void onDisable() {
        if (Web.getValue()) {
            ModuleManager.getModuleByName("AutoWeb").disable();
        }
        if (mc.player == null || mc.player.getHealth() <= 0) return;

        if (lastHotbarSlot != playerHotbarSlot && playerHotbarSlot != -1) {
            mc.player.inventory.currentItem = playerHotbarSlot;
        }

        if (isSneaking) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
            isSneaking = false;
        }

        playerHotbarSlot = -1;
        lastHotbarSlot = -1;

        missingObiDisable = false;

        if (announceUsage.getValue()) {
            Command.sendChatMessage("[AutoTrap] " + ChatFormatting.RED.toString() + "Disabled" + ChatFormatting.RESET.toString() + "!");
        }

    }

    @Override
    public void onUpdate() {
        if (mc.player == null || mc.player.getHealth() <= 0) return;

        if (!activeInFreecam.getValue() && ModuleManager.isModuleEnabled("Freecam")) {
            return;
        }

        if (triggerable.getValue() && totalTicksRunning >= timeoutTicks.getValue()) {
            totalTicksRunning = 0;
            disable();
            return;
        }

        if (firstRun) {
            if (findObiInHotbar() == -1) {
                if (infoMessage.getValue()) {
                    sendChatMessage(getChatName() + " " + ChatFormatting.RED + "Disabled" + ChatFormatting.RESET + ", Obsidian missing!");
                }
                disable();
                return;
            }
        } else {
            if (delayStep < tickDelay.getValue()) {
                delayStep++;
                return;
            } else {
                delayStep = 0;
            }
        }

        findClosestTarget();
        totalTicksRunning++;

        if (closestTarget == null) {
            if (firstRun) {
                firstRun = false;
                if (announceUsage.getValue()) {
                    Command.sendChatMessage("[AutoTrap] " + ChatFormatting.GREEN.toString() + "Enabled" + ChatFormatting.RESET.toString() + ", waiting for target.");
                }
            }
            return;
        }

        if (firstRun) {
            firstRun = false;
            lastTickTargetName = closestTarget.getName();
            if (announceUsage.getValue()) {
                Command.sendChatMessage("[AutoTrap] " + ChatFormatting.GREEN.toString() + "Enabled" + ChatFormatting.RESET.toString() + ", target: " + lastTickTargetName);
            }
        } else if (!lastTickTargetName.equals(closestTarget.getName())) {
            lastTickTargetName = closestTarget.getName();
            offsetStep = 0;
            if (announceUsage.getValue()) {
                Command.sendChatMessage("[AutoTrap] New target: " + lastTickTargetName);
            }
        }

        List<Vec3d> placeTargets = new ArrayList<>();

        if (extrablock.getValue()) {
            Collections.addAll(placeTargets, offsetsExtra);
        }

        if (cage.getValue().equals(Cage.TRAP)) {
            Collections.addAll(placeTargets, Offsets.TRAP);
        }

        if (cage.getValue().equals(Cage.TRAPFULLROOF)) {
            Collections.addAll(placeTargets, Offsets.TRAPFULLROOF);
        }

        if (cage.getValue().equals(Cage.CRYSTALEXA)) {
            Collections.addAll(placeTargets, Offsets.CRYSTALEXA);
        }

        if (cage.getValue().equals(Cage.CRYSTAL)) {
            Collections.addAll(placeTargets, Offsets.CRYSTAL);
        }

        if (cage.getValue().equals(Cage.CRYSTALFULLROOF)) {
            Collections.addAll(placeTargets, Offsets.CRYSTALFULLROOF);
        }

        int blocksPlaced = 0;

        while (blocksPlaced < blocksPerTick.getValue()) {
            if (offsetStep >= placeTargets.size()) {
                offsetStep = 0;
                break;
            }

            BlockPos offsetPos = new BlockPos(placeTargets.get(offsetStep));
            BlockPos targetPos = new BlockPos(closestTarget.getPositionVector()).down().add(offsetPos.x, offsetPos.y, offsetPos.z);

            if (placeBlockInRange(targetPos, range.getValue())) {
                blocksPlaced++;
            }
            offsetStep++;
        }

        if (blocksPlaced > 0) {
            if (lastHotbarSlot != playerHotbarSlot && playerHotbarSlot != -1) {
                mc.player.inventory.currentItem = playerHotbarSlot;
                lastHotbarSlot = playerHotbarSlot;
            }

            if (isSneaking) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
                isSneaking = false;
            }
        }

        if (missingObiDisable) {
            missingObiDisable = false;
            if (infoMessage.getValue()) {
                sendChatMessage(getChatName() + " " + ChatFormatting.RED + "Disabled" + ChatFormatting.RESET + ", Obsidian missing!");
            }
            disable();
        }
    }

    private boolean placeBlockInRange(BlockPos pos, double range) {
        // check if block is already placed
        Block block = mc.world.getBlockState(pos).getBlock();
        if (!(block instanceof BlockAir) && !(block instanceof BlockLiquid)) {
            return false;
        }

        // check if entity blocks placing
        for (Entity entity : mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(pos))) {
            if (!(entity instanceof EntityItem) && !(entity instanceof EntityXPOrb)) {
                return false;
            }
        }

        EnumFacing side = getPlaceableSide(pos);

        // check if we have a block adjacent to blockpos to click at
        if (side == null) {
            return false;
        }

        BlockPos neighbour = pos.offset(side);
        EnumFacing opposite = side.getOpposite();

        // check if neighbor can be right clicked
        if (!canBeClicked(neighbour)) {
            return false;
        }

        Vec3d hitVec = new Vec3d(neighbour).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));
        Block neighbourBlock = mc.world.getBlockState(neighbour).getBlock();

        if (mc.player.getPositionVector().distanceTo(hitVec) > range) {
            return false;
        }

        int obiSlot = findObiInHotbar();

        if (obiSlot == -1) {
            missingObiDisable = true;
            return false;
        }

        if (lastHotbarSlot != obiSlot) {
            mc.player.inventory.currentItem = obiSlot;
            lastHotbarSlot = obiSlot;
        }

        if (!isSneaking && BlockInteractionHelper.blackList.contains(neighbourBlock) || BlockInteractionHelper.shulkerList.contains(neighbourBlock)) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
            isSneaking = true;
        }

        if (rotate.getValue()) faceVectorPacketInstant(hitVec);

        mc.playerController.processRightClickBlock(mc.player, mc.world, neighbour, opposite, hitVec, EnumHand.MAIN_HAND);
        mc.player.swingArm(EnumHand.MAIN_HAND);
        mc.rightClickDelayTimer = 4;

        if (noGlitchBlocks.getValue() && !mc.playerController.getCurrentGameType().equals(GameType.CREATIVE)) {
            mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, neighbour, opposite));
        }

        if (ModuleManager.getModuleByName("NoBreakAnimation").isEnabled()) {
            ((NoBreakAnimation) ModuleManager.getModuleByName("NoBreakAnimation")).resetMining();
        }
        return true;
    }

    private int findObiInHotbar() {
        // search blocks in hotbar
        int slot = -1;
        for (int i = 0; i < 9; i++) {
            // filter out non-block items
            ItemStack stack = mc.player.inventory.getStackInSlot(i);

            if (stack == ItemStack.EMPTY || !(stack.getItem() instanceof ItemBlock)) {
                continue;
            }

            Block block = ((ItemBlock) stack.getItem()).getBlock();
            if (block instanceof BlockObsidian) {
                slot = i;
                break;
            }
        }
        return slot;
    }

    private void findClosestTarget() {
        List<EntityPlayer> playerList = mc.world.playerEntities;
        closestTarget = null;

        for (EntityPlayer target : playerList) {
            if (target == mc.player && !selfTrap.getValue()) continue;

            if (mc.player.getDistance(target) > range.getValue() + 3) continue;

            if (!EntityUtil.isLiving(target)) continue;

            if ((target).getHealth() <= 0) continue;

            if (Friends.isFriend(target.getName())) continue;

            if (closestTarget == null) {
                closestTarget = target;
                continue;
            }

            if (mc.player.getDistance(target) < mc.player.getDistance(closestTarget)) closestTarget = target;
        }
    }

    @Override
    public String getHudInfo() {
        if (closestTarget != null) {
            return closestTarget.getName().toUpperCase();
        }
        return "NO TARGET";
    }

    private enum Cage {
        TRAP, TRAPFULLROOF, CRYSTALEXA, CRYSTAL, CRYSTALFULLROOF
    }

    private static class Offsets {

        private static final Vec3d[] TRAP = {
                new Vec3d(0, 0, -1),
                new Vec3d(1, 0, 0),
                new Vec3d(0, 0, 1),
                new Vec3d(-1, 0, 0),
                new Vec3d(0, 1, -1),
                new Vec3d(1, 1, 0),
                new Vec3d(0, 1, 1),
                new Vec3d(-1, 1, 0),
                new Vec3d(0, 2, -1),
                new Vec3d(1, 2, 0),
                new Vec3d(0, 2, 1),
                new Vec3d(-1, 2, 0),
                new Vec3d(0, 3, -1),
                new Vec3d(0, 3, 0)
        };

        private static final Vec3d[] TRAPFULLROOF = {
                new Vec3d(0, 0, -1),
                new Vec3d(1, 0, 0),
                new Vec3d(0, 0, 1),
                new Vec3d(-1, 0, 0),
                new Vec3d(0, 1, -1),
                new Vec3d(1, 1, 0),
                new Vec3d(0, 1, 1),
                new Vec3d(-1, 1, 0),
                new Vec3d(0, 2, -1),
                new Vec3d(1, 2, 0),
                new Vec3d(0, 2, 1),
                new Vec3d(-1, 2, 0),
                new Vec3d(0, 3, -1),
                new Vec3d(1, 3, 0),
                new Vec3d(0, 3, 1),
                new Vec3d(-1, 3, 0),
                new Vec3d(0, 3, 0)
        };

        private static final Vec3d[] CRYSTALEXA = {
                new Vec3d(0, 0, -1),
                new Vec3d(0, 1, -1),
                new Vec3d(0, 2, -1),
                new Vec3d(1, 2, 0),
                new Vec3d(0, 2, 1),
                new Vec3d(-1, 2, 0),
                new Vec3d(-1, 2, -1),
                new Vec3d(1, 2, 1),
                new Vec3d(1, 2, -1),
                new Vec3d(-1, 2, 1),
                new Vec3d(0, 3, -1),
                new Vec3d(0, 3, 0)
        };

        private static final Vec3d[] CRYSTAL = {
                new Vec3d(0, 0, -1),
                new Vec3d(1, 0, 0),
                new Vec3d(0, 0, 1),
                new Vec3d(-1, 0, 0),
                new Vec3d(-1, 0, 1),
                new Vec3d(1, 0, -1),
                new Vec3d(-1, 0, -1),
                new Vec3d(1, 0, 1),
                new Vec3d(-1, 1, -1),
                new Vec3d(1, 1, 1),
                new Vec3d(-1, 1, 1),
                new Vec3d(1, 1, -1),
                new Vec3d(0, 2, -1),
                new Vec3d(1, 2, 0),
                new Vec3d(0, 2, 1),
                new Vec3d(-1, 2, 0),
                new Vec3d(-1, 2, 1),
                new Vec3d(1, 2, -1),
                new Vec3d(0, 3, -1),
                new Vec3d(0, 3, 0)
        };

        private static final Vec3d[] CRYSTALFULLROOF = {
                new Vec3d(0, 0, -1),
                new Vec3d(1, 0, 0),
                new Vec3d(0, 0, 1),
                new Vec3d(-1, 0, 0),
                new Vec3d(-1, 0, 1),
                new Vec3d(1, 0, -1),
                new Vec3d(-1, 0, -1),
                new Vec3d(1, 0, 1),
                new Vec3d(-1, 1, -1),
                new Vec3d(1, 1, 1),
                new Vec3d(-1, 1, 1),
                new Vec3d(1, 1, -1),
                new Vec3d(0, 2, -1),
                new Vec3d(1, 2, 0),
                new Vec3d(0, 2, 1),
                new Vec3d(-1, 2, 0),
                new Vec3d(-1, 2, 1),
                new Vec3d(1, 2, -1),
                new Vec3d(0, 3, -1),
                new Vec3d(1, 3, 0),
                new Vec3d(0, 3, 1),
                new Vec3d(-1, 3, 0),
                new Vec3d(0, 3, 0)
        };

    }

}
