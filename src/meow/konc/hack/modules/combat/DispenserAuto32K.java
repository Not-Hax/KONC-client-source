package meow.konc.hack.modules.combat;

import meow.konc.hack.command.Command;
import meow.konc.hack.module.Module;
import meow.konc.hack.setting.Setting;
import meow.konc.hack.setting.Settings;
import meow.konc.hack.util.packet.BlockInteractionHelper;
import meow.konc.hack.util.util.EntityUtil;
import meow.konc.hack.util.other.Friends;
import net.minecraft.block.*;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Module.Info(name = "DispenserAuto32k", category = Module.Category.COMBAT)
public class DispenserAuto32K extends Module {

    private int hopperSlot, redstoneSlot, shulkerSlot, dispenserSlot, obiSlot, stage, time;
    private BlockPos placeTarget;
    private Setting<Double> placerange = register(Settings.d("PlaceRange", 4.5));
    private Setting<Boolean> placeclosetoenemy = register(Settings.b("Place Close To Enemy"));
    private Setting<Boolean> hopperWait = register(Settings.b("HopperWait"));
    private EnumFacing q;
    private EnumFacing f;
    private int delay, delaycount;
    public List<BlockPos> getPlaceableBlocks() {
        List<BlockPos> toreturn = new ArrayList<>();
        for (BlockPos w : getSphere(new BlockPos(mc.player.posX, mc.player.posY + 1, mc.player.posZ), placerange.getValue().floatValue(), 4, false, true, 0)) {
            double[] lookat = EntityUtil.calculateLookAt(w.x, w.y + 1, w.z, mc.player);
            EnumFacing f;
            double lookatyaw = lookat[0] + 45 > 360 ? lookat[0] - 360 : lookat[0];
            float yaw = (float) lookatyaw;
            boolean isNegative = false;
            if (yaw < 0.0F) {
                isNegative = true;
            }
            int dir = Math.round(Math.abs(yaw)) % 360;
            if (135 < dir && dir < 225) {
                f = EnumFacing.SOUTH;
            } else if (225 < dir && dir < 315) {
                if (isNegative) {
                    f = EnumFacing.EAST;
                } else {
                    f = EnumFacing.WEST;
                }
            } else if (45 < dir && dir < 135) {
                if (isNegative) {
                    f = EnumFacing.WEST;
                } else {
                    f = EnumFacing.EAST;
                }
            } else {
                f = EnumFacing.NORTH;
            }

            if (isAreaPlaceable(w, f)) {
                toreturn.add(w);
            }
        }
        return toreturn;
    }
    public List<BlockPos> getSphere(final BlockPos loc, final float r, final int h, final boolean hollow, final boolean sphere, final int plus_y) {
        final List<BlockPos> circleblocks = new ArrayList<>();
        final int cx = loc.getX();
        final int cy = loc.getY();
        final int cz = loc.getZ();
        for (int x = cx - (int) r; x <= cx + r; ++x) {
            for (int z = cz - (int) r; z <= cz + r; ++z) {
                for (int y = sphere ? (cy - (int) r) : cy; y < (sphere ? (cy + r) : ((float) (cy + h))); ++y) {
                    final double dist = (cx - x) * (cx - x) + (cz - z) * (cz - z) + (sphere ? ((cy - y) * (cy - y)) : 0);
                    if (dist < r * r && (!hollow || dist >= (r - 1.0f) * (r - 1.0f))) {
                        final BlockPos l = new BlockPos(x, y + plus_y, z);
                        circleblocks.add(l);
                    }
                }
            }
        }
        return circleblocks;
    }
    private boolean isAreaPlaceable(BlockPos blockPos, EnumFacing f) {
        for (EntityPlayer w : mc.world.playerEntities) {
            if (Math.sqrt(w.getDistanceSq(blockPos.x, mc.player.posY, blockPos.z)) <= 2) {
                return false;
            }
        }
        List<Entity> entityList = new ArrayList<>();
        entityList.addAll(mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(blockPos)));
        entityList.addAll(mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(blockPos.add(0, 1, 0))));
        entityList.addAll(mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(blockPos.add(0, 2, 0))));
        entityList.addAll(mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(blockPos.add(0, 2, 0))));
        entityList.addAll(mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(blockPos.offset(f))));
        entityList.addAll(mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(blockPos.add(0, 1, 0).offset(f))));
        for (Entity entity : entityList) {
            if (entity instanceof EntityLivingBase) {
                return false; // entity on block
            }
        }
        if (!mc.world.getBlockState(blockPos).getMaterial().isReplaceable()) {
            return false; // no space for hopper
        }
        if (!mc.world.getBlockState(blockPos.add(0, 1, 0)).getMaterial().isReplaceable()) {
            return false; // no space for dispenser
        }
        if (Math.abs(blockPos.y + 1 - mc.player.posY) >= 2 && Math.sqrt(mc.player.getDistanceSq(blockPos.x, mc.player.posY, blockPos.z)) <= 2) {
            return false; // direction of dispenser
        }
        if (!mc.world.getBlockState(blockPos.add(0, 2, 0)).getMaterial().isReplaceable()) {
            return false; // no space for redstoneBlock
        }
        if (!mc.world.getBlockState(blockPos.offset(f)).getMaterial().isReplaceable()) {
            return false; // no space for redstoneBlock
        }

        if (mc.world.getBlockState(blockPos.add(0, -1, 0)).getBlock() instanceof BlockAir) {
            return false; // air below hopper
        }

        if (mc.world.getBlockState(blockPos.add(0, -1, 0)).getBlock() instanceof BlockLiquid) {
            return false; // liquid below hopper
        }
        return mc.world.getBlockState(blockPos.add(0, 1, 0).offset(f)).getMaterial().isReplaceable();
    }
    public void onEnable() {
        hopperSlot = redstoneSlot = shulkerSlot = dispenserSlot = obiSlot = -1;
        for (int i = 0; i < 9 && (obiSlot == -1 || dispenserSlot == -1 || shulkerSlot == -1 || redstoneSlot == -1 || hopperSlot == -1); ++i) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack != ItemStack.EMPTY) {
                if (stack.getItem() instanceof ItemBlock) {
                    Block block = ((ItemBlock) stack.getItem()).getBlock();
                    if (block == Blocks.HOPPER) {
                        hopperSlot = i;
                    } else if (BlockInteractionHelper.shulkerList.contains(block)) {
                        shulkerSlot = i;
                    } else if (block == Blocks.OBSIDIAN) {
                        obiSlot = i;
                    } else if (block == Blocks.DISPENSER) {
                        dispenserSlot = i;
                    } else if (block == Blocks.REDSTONE_BLOCK) {
                        redstoneSlot = i;
                    }
                }
            }
        }
        if (obiSlot == -1 || dispenserSlot == -1 || shulkerSlot == -1 || redstoneSlot == -1 || hopperSlot == -1) {
            Command.sendChatMessage("[Dispenser32k] Item missing, disabling.");
            disable();
        }

        stage = 0;
    }

    @Override
    public void onUpdate() {
        switch (stage) {
            case 0:
                delay = 10;
                delaycount = 0;
                List<BlockPos> canPlaceLocation = getPlaceableBlocks();
                if (placeclosetoenemy.getValue()) {
                    EntityPlayer targetPlayer = mc.world.playerEntities.stream().filter(e -> e != mc.player && !Friends.isFriend(e.getName())).min(Comparator.comparing(e -> mc.player.getDistance(e))).orElse(null);
                    placeTarget = targetPlayer != null ? canPlaceLocation.stream().min(Comparator.comparing(e -> BlockInteractionHelper.blockDistance(e.x, e.y, e.z, targetPlayer))).orElse(null) : canPlaceLocation.stream().max(Comparator.comparing(e -> BlockInteractionHelper.blockDistance(e.x, e.y, e.z, mc.player))).orElse(null);
                }else {
                    placeTarget = canPlaceLocation.stream().max(Comparator.comparing(e -> BlockInteractionHelper.blockDistance(e.x, e.y, e.z, mc.player))).orElse(null);
                }
                if (placeTarget == null) {
                    Command.sendChatMessage("[Dispenser32k] No suitable place to place, disabling");
                    disable();
                    break;
                }
                mc.player.inventory.currentItem = obiSlot;
                BlockInteractionHelper.placeBlockScaffold(new BlockPos(placeTarget));
                double[] lookat = EntityUtil.calculateLookAt(placeTarget.x, placeTarget.y + 1, placeTarget.z, mc.player);
                double lookatyaw = lookat[0] + 45 > 360 ? lookat[0] - 360 : lookat[0];
                float yaw = (float) lookatyaw;
                boolean isNegative = false;
                if (yaw < 0.0F) {
                    isNegative = true;
                }
                int dir = Math.round(Math.abs(yaw)) % 360;
                if (135 < dir && dir < 225) {
                    f = EnumFacing.SOUTH;
                } else if (225 < dir && dir < 315) {
                    if (isNegative) {
                        f = EnumFacing.EAST;
                    } else {
                        f = EnumFacing.WEST;
                    }
                } else if (45 < dir && dir < 135) {
                    if (isNegative) {
                        f = EnumFacing.WEST;
                    } else {
                        f = EnumFacing.EAST;
                    }
                } else {
                    f = EnumFacing.NORTH;
                }
                mc.player.inventory.currentItem = dispenserSlot;
                BlockInteractionHelper.placeBlockScaffold(new BlockPos(placeTarget.add(0, 1, 0)));
                mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(placeTarget.add(0, 1, 0), EnumFacing.DOWN, EnumHand.MAIN_HAND, 0.0f, 0.0f, 0.0f));
                stage++;
                break;
            case 1:
                if (mc.currentScreen instanceof GuiContainer) {
                    mc.playerController.windowClick(mc.player.openContainer.windowId, 1, shulkerSlot, ClickType.SWAP, mc.player);
                    mc.player.closeScreen();
                    stage++;
                }
                break;
            case 2:
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
                mc.player.inventory.currentItem = redstoneSlot;
                BlockInteractionHelper.placeBlockScaffold(new BlockPos(placeTarget.add(0, 2, 0)));
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
                stage++;
                break;
            case 3:
                if (!hopperWait.getValue()) {
                    mc.player.inventory.currentItem = hopperSlot;
                    BlockInteractionHelper.placeBlockScaffold(new BlockPos(placeTarget.offset(f)));
                    mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(placeTarget.offset(f), EnumFacing.DOWN, EnumHand.MAIN_HAND, 0, 0, 0));
                    mc.player.inventory.currentItem = shulkerSlot;
                    stage = 0;
                    disable();
                } else {
                    if (delaycount >= delay) {
                        mc.player.inventory.currentItem = hopperSlot;
                        BlockInteractionHelper.placeBlockScaffold(new BlockPos(placeTarget.offset(f)));
                        mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(placeTarget.offset(f), EnumFacing.DOWN, EnumHand.MAIN_HAND, 0, 0, 0));
                        mc.player.inventory.currentItem = shulkerSlot;
                        stage = 0;
                        disable();
                    } else {
                        delaycount++;
                    }
                }
                break;
        }
    }
}