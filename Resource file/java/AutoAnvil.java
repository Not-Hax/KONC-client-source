package meow.konc.hack.modules.combat;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAnvil;
import net.minecraft.block.BlockObsidian;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameType;
import meow.konc.hack.command.Command;
import meow.konc.hack.module.Module;
import meow.konc.hack.module.ModuleManager;
import meow.konc.hack.modules.exploits.NoPlayerAnimation;
import meow.konc.hack.setting.Setting;
import meow.konc.hack.setting.Settings;
import meow.konc.hack.util.EntityUtil;
import meow.konc.hack.util.WorldUtils;

import static net.minecraft.util.EnumHand.MAIN_HAND;
import static meow.konc.hack.util.BlockInteractionHelper.canBeClicked;
import static meow.konc.hack.util.BlockInteractionHelper.faceVectorPacketInstant;

@Module.Info(name = "AutoAnvil", category = Module.Category.COMBAT, description = "Do not use with a Mod!")
public class AutoAnvil extends Module {
    private Setting<Integer> delay = register(Settings.integerBuilder("Tick Delay").withValue(4).withMinimum(0).withMaximum(20));
    private Setting<Float> renge = register(Settings.f("Renge", 8));
    private Setting<Boolean> top = register(Settings.b("TopSurround", true));
    private Setting<Boolean> rotate = register(Settings.b("Rotate", true));
    private Setting<Boolean> noGlitchBlocks = register(Settings.b("NoGlitchBlocks", true));

    private int hasWaited;
    private boolean traped = false;
    private EntityPlayer e;

    public void onUpdate() {
        if (mc.player == null) {
            return;
        }

        int oldSlot = mc.player.inventory.currentItem;

        EntityPlayer target = EntityUtil.findClosestTarget(renge.getValue());
        e = target;

        if (this.hasWaited % delay.getValue() != 0) {
            ++this.hasWaited;
            return;
        }

        if (target == null){
            return;
        }

        if(findAnvilInHotbar() == -1 || findObiInHotbar() == -1) {
            Command.sendChatMessage("Missing Item during Trap!!");
            disable();
            return;
        }

        final BlockPos[] array = new BlockPos[] {
                WorldUtils.getRelativeBlockPos(target, 1, -1, 0),
                WorldUtils.getRelativeBlockPos(target, 1, 0, 0),
                WorldUtils.getRelativeBlockPos(target, 1, 1, 0),
                WorldUtils.getRelativeBlockPos(target, 1, 2, 0)
        };
        final BlockPos[] array2 = new BlockPos[] {
                WorldUtils.getRelativeBlockPos(target, 0, 2, 0),
                WorldUtils.getRelativeBlockPos(target, 0, 3, 0)
        };
        final BlockPos[] array3 = new BlockPos[] {
                WorldUtils.getRelativeBlockPos(target, 1, 2, 0),
                WorldUtils.getRelativeBlockPos(target, -1, 2, 0),
                WorldUtils.getRelativeBlockPos(target, 0, 2, -1),
                WorldUtils.getRelativeBlockPos(target, 0, 2, 1),
        };

        for (final BlockPos pos : array) {
            if (AutoTrap.mc.world.getBlockState(pos).getMaterial().isReplaceable()) {
                mc.player.inventory.currentItem = findObiInHotbar();
                placeBlockScaffold(pos);
                ++this.hasWaited;
                return;
            }
        }
        for (final BlockPos pos : array2) {
            if (AutoTrap.mc.world.getBlockState(pos).getMaterial().isReplaceable()) {
                mc.player.inventory.currentItem = findAnvilInHotbar();
                placeBlockScaffold(pos);
                ++this.hasWaited;
                return;
            }
        }

        if (top.getValue() && !traped) {
            for (final BlockPos pos : array3) {
                if (AutoTrap.mc.world.getBlockState(pos).getMaterial().isReplaceable()) {
                    mc.player.inventory.currentItem = findObiInHotbar();
                    placeBlockScaffold(pos);
                    ++this.hasWaited;
                    return;
                }
            }
            traped = true;
        }
        mc.player.inventory.currentItem = oldSlot;
    }
    private void placeBlockScaffold(BlockPos pos) {
        //Vec3d eyesPos = new Vec3d(Surround.mc.player.posX, Surround.mc.player.posY + (double)Surround.mc.player.getEyeHeight(), Surround.mc.player.posZ);
        for (EnumFacing side : EnumFacing.values()) {
            BlockPos neighbor = pos.offset(side);
            EnumFacing side2 = side.getOpposite();
            if (!canBeClicked(neighbor)) continue;
            Vec3d hitVec = new Vec3d(neighbor).add(0.5, 0.5, 0.5).add(new Vec3d(side2.getDirectionVec()).scale(0.5));
            if(rotate.getValue()) {faceVectorPacketInstant(hitVec);}
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
            mc.playerController.processRightClickBlock(mc.player, mc.world, neighbor, side2, hitVec, MAIN_HAND);
            mc.player.swingArm(EnumHand.MAIN_HAND);
            mc.rightClickDelayTimer = 0;
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
            if (noGlitchBlocks.getValue() && !mc.playerController.getCurrentGameType().equals(GameType.CREATIVE)) {
                mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, neighbor, side2));
                if (ModuleManager.getModuleByName("NoPlayerAnimation").isEnabled()) {
                    ((NoPlayerAnimation) ModuleManager.getModuleByName("NoPlayerAnimation")).resetMining();
                }
            }
            return;
        }
    }
    private static int findObiInHotbar() {
        // search blocks in hotbar
        int Obislot = -1;
        for (int i = 0; i < 9; i++) {
            // filter out non-block items
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack == ItemStack.EMPTY || !(stack.getItem() instanceof ItemBlock)) {
                continue;
            }
            Block block = ((ItemBlock) stack.getItem()).getBlock();
            if (block instanceof BlockObsidian) {
                Obislot = i;
                break;
            }
        }
        return Obislot;
    }

    private static int findAnvilInHotbar() {
        // search blocks in hotbar
        int Anvilslot = -1;
        for (int i = 0; i < 9; i++) {
            // filter out non-block items
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack == ItemStack.EMPTY || !(stack.getItem() instanceof ItemBlock)) {
                continue;
            }
            Block block = ((ItemBlock) stack.getItem()).getBlock();
            if (block instanceof BlockAnvil) {
                Anvilslot = i;
                break;
            }
        }
        return Anvilslot;
    }

    @Override
    public String getHudInfo() {
        if (e != null) {
            return e.getName().toUpperCase();
        }
        return "NO TARGET";
    }

}