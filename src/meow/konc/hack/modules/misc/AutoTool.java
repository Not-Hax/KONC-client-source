package meow.konc.hack.modules.misc;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import meow.konc.hack.module.Module;
//import meow.konc.hack.modules.combat.KillAura2;
import meow.konc.hack.setting.Setting;
import meow.konc.hack.setting.Settings;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

@Module.Info(name = "AutoTool", description = "Automatically switch to the best tools when mining or attacking", category = Module.Category.MISC)
public class AutoTool extends Module {
    //private Setting<KillAura2.HitMode> preferTool = register(Settings.e("Prefer", KillAura2.HitMode.NONE));

    @EventHandler
    private Listener<PlayerInteractEvent.LeftClickBlock> leftClickListener = new Listener<>(event -> equipBestTool(mc.world.getBlockState(event.getPos())));

    /*@EventHandler
    private Listener<AttackEntityEvent> attackListener = new Listener<>(event -> equipBestWeapon(preferTool.getValue()));*/

    private void equipBestTool(IBlockState blockState) {
        int bestSlot = -1;
        double max = 0;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.isEmpty) {
                continue;
            }
            float speed = stack.getDestroySpeed(blockState);
            int eff;
            if (speed > 1) {
                speed += ((eff = EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, stack)) > 0 ? (Math.pow(eff, 2) + 1) : 0);
                if (speed > max) {
                    max = speed;
                    bestSlot = i;
                }
            }
        }
        if (bestSlot != -1) {
            equip(bestSlot);
        }
    }

    /*public static void equipBestWeapon(KillAura2.HitMode hitMode) {
        int bestSlot = -1;
        double maxDamage = 0;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.isEmpty) {
                continue;
            }
            if (!(stack.getItem() instanceof ItemAxe) && hitMode.equals(KillAura2.HitMode.AXE)) {
                continue;
            }
            if (!(stack.getItem() instanceof ItemSword) && hitMode.equals(KillAura2.HitMode.SWORD)) {
                continue;
            }

            if (stack.getItem() instanceof ItemSword && (hitMode.equals(KillAura2.HitMode.SWORD) || hitMode.equals(KillAura2.HitMode.NONE))) {
                double damage = (((ItemSword) stack.getItem()).getAttackDamage() + (double) EnchantmentHelper.getModifierForCreature(stack, EnumCreatureAttribute.UNDEFINED));
                if (damage > maxDamage) {
                    maxDamage = damage;
                    bestSlot = i;
                }
            } else if (stack.getItem() instanceof ItemAxe && (hitMode.equals(KillAura2.HitMode.AXE) || hitMode.equals(KillAura2.HitMode.NONE))) {
                double damage = (((ItemTool) stack.getItem()).attackDamage + (double) EnchantmentHelper.getModifierForCreature(stack, EnumCreatureAttribute.UNDEFINED));
                if (damage > maxDamage) {
                    maxDamage = damage;
                    bestSlot = i;
                }
            } else if (stack.getItem() instanceof ItemTool) {
                double damage = (((ItemTool) stack.getItem()).attackDamage + (double) EnchantmentHelper.getModifierForCreature(stack, EnumCreatureAttribute.UNDEFINED));
                if (damage > maxDamage) {
                    maxDamage = damage;
                    bestSlot = i;
                }
            }
        }
        if (bestSlot != -1) {
            equip(bestSlot);
        }
    }*/

    private static void equip(int slot) {
        mc.player.inventory.currentItem = slot;
        mc.playerController.syncCurrentPlayItem();
    }
}
