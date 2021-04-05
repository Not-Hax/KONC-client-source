package meow.konc.hack.modules.combat;

import meow.konc.hack.command.Command;
import meow.konc.hack.module.Module;
import meow.konc.hack.module.ModuleManager;
import meow.konc.hack.setting.Setting;
import meow.konc.hack.setting.Settings;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemSword;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static meow.konc.hack.modules.hidden.util.HoleDetect.inhole;

@Module.Info(name = "SmartOffHand", category = Module.Category.COMBAT, description = "smartoffhand that isnt smart")
public class SmartOffHand extends Module {
    private Setting<Boolean> gap = register(Settings.b("RightClickGapple", true));
    private Setting<Boolean> crystal = register(Settings.b("OffHandCrystalWhenCa", true));
    private Setting<Integer> detectrange = register(Settings.i("Range", 6));
    public Setting<Integer> totemhealth = register(Settings.integerBuilder("TotemHealth").withMinimum(0).withValue(19).withMaximum(36).build());

    public List<BlockPos> findCrystalBlocks() {
        final NonNullList<BlockPos> positions = NonNullList.create();
        positions.addAll(getSphere(new BlockPos(mc.player.posX, mc.player.posY + 1, mc.player.posZ), detectrange.getValue().floatValue(), detectrange.getValue().intValue(), false, true, 0).stream().filter(this::canPlaceCrystal).collect(Collectors.toList()));
        return positions;
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

    public boolean canPlaceCrystal(BlockPos blockPos) {
        final BlockPos boost = blockPos.add(0, 1, 0);
        final BlockPos boost2 = blockPos.add(0, 2, 0);
        return ((mc.world.getBlockState(blockPos).getBlock() == Blocks.BEDROCK || mc.world.getBlockState(blockPos).getBlock() == Blocks.OBSIDIAN) && mc.world.getBlockState(boost).getBlock() == Blocks.AIR && mc.world.getBlockState(boost2).getBlock() == Blocks.AIR && mc.world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(boost)).isEmpty() && mc.world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(boost2)).isEmpty());
    }

    public void onUpdate() {
        if (mc.currentScreen instanceof GuiContainer || mc.player == null) {
            return;
        }
        int gaps = -1;
        int crystals = -1;
        int totem = -1;
        double totemhealths = 0;
        List<BlockPos> uwu = findCrystalBlocks();
        List<EntityEnderCrystal> crystalList = mc.world.loadedEntityList.stream().filter(e -> e instanceof EntityEnderCrystal && e.getDistance(mc.player) <= detectrange.getValue()).map(e -> (EntityEnderCrystal) e).collect(Collectors.toList());
        for (BlockPos awa : uwu) {
            double w = AutoCrystal.calculateDamage(awa.x + 0.5, awa.y + 1, awa.z + 0.5, mc.player);
            if (w > totemhealths) {
                totemhealths = w;
            }
        }
        for (EntityEnderCrystal crystal : crystalList) {
            double w = AutoCrystal.calculateDamage(crystal, mc.player);
            if (w > totemhealths) {
                totemhealths = w;
            }
        }
        totemhealths = Math.max(totemhealths, totemhealth.getValue());
        if (mc.player.getHeldItemOffhand().getItem() != Items.GOLDEN_APPLE) {
            for (int i = 0; i < 45; i++) {
                if (mc.player.inventory.getStackInSlot(i).getItem() == Items.GOLDEN_APPLE) {
                    gaps = i;
                }
            }
        }
        if (mc.player.getHeldItemOffhand().getItem() != Items.END_CRYSTAL) {
            for (int i = 0; i < 45; i++) {
                if (mc.player.inventory.getStackInSlot(i).getItem() == Items.END_CRYSTAL) {
                    crystals = i;
                }
            }
        }
        if (mc.player.getHeldItemOffhand().getItem() != Items.TOTEM_OF_UNDYING) {
            for (int i = 0; i < 45; i++) {
                if (mc.player.inventory.getStackInSlot(i).getItem() == Items.TOTEM_OF_UNDYING) {
                    totem = i;
                }
            }
        }
        if (mc.player.getHealth() + mc.player.getAbsorptionAmount() > totemhealths || (inhole && mc.player.getHealth() + mc.player.getAbsorptionAmount() > 4 && Math.floor(mc.player.posY) == mc.player.posY)) {
            if (mc.player.getHeldItemMainhand().getItem() instanceof ItemSword && mc.gameSettings.keyBindUseItem.isKeyDown() && gap.getValue()) {
                if (mc.player.getHeldItemOffhand().getItem() != Items.GOLDEN_APPLE && gaps != -1) {
                    Command.sendChatMessage("Switching to Gapple");
                    mc.playerController.windowClick(0, gaps < 9 ? gaps + 36 : gaps, 0, ClickType.PICKUP, mc.player);
                    mc.playerController.windowClick(0, 45, 0, ClickType.PICKUP, mc.player);
                    mc.playerController.windowClick(0, gaps < 9 ? gaps + 36 : gaps, 0, ClickType.PICKUP, mc.player);
                    return;
                }
                return;
            }
            if (!ModuleManager.getModuleByName("AutoCrystal").isDisabled() && crystal.getValue()) {
                if (mc.player.getHeldItemOffhand().getItem() != Items.END_CRYSTAL && crystals != -1) {
                    Command.sendChatMessage("Switching to Crystals");
                    mc.playerController.windowClick(0, crystals < 9 ? crystals + 36 : crystals, 0, ClickType.PICKUP, mc.player);
                    mc.playerController.windowClick(0, 45, 0, ClickType.PICKUP, mc.player);
                    mc.playerController.windowClick(0, crystals < 9 ? crystals + 36 : crystals, 0, ClickType.PICKUP, mc.player);
                    return;
                }
                return;
            }

            if ((totem != -1) && mc.player.getHeldItemOffhand().getItem() != Items.TOTEM_OF_UNDYING) {
                Command.sendChatMessage("Switching to Totem");
                mc.playerController.windowClick(0, totem < 9 ? totem + 36 : totem, 0, ClickType.PICKUP, mc.player);
                mc.playerController.windowClick(0, 45, 0, ClickType.PICKUP, mc.player);
                mc.playerController.windowClick(0, totem < 9 ? totem + 36 : totem, 0, ClickType.PICKUP, mc.player);
                return;
            }

        } else if (totem != -1 && mc.player.getHeldItemOffhand().getItem() != Items.TOTEM_OF_UNDYING) {
            Command.sendChatMessage("Not enough health and not in hole, switching to Totem");
            mc.playerController.windowClick(0, totem < 9 ? totem + 36 : totem, 0, ClickType.PICKUP, mc.player);
            mc.playerController.windowClick(0, 45, 0, ClickType.PICKUP, mc.player);
            mc.playerController.windowClick(0, totem < 9 ? totem + 36 : totem, 0, ClickType.PICKUP, mc.player);
            return;
        }

    }
}