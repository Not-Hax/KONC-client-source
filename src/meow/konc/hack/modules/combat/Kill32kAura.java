package meow.konc.hack.modules.combat;

import meow.konc.hack.module.Module;
import meow.konc.hack.module.Module.Info;
import meow.konc.hack.setting.Setting;
import meow.konc.hack.setting.Settings;
import meow.konc.hack.util.other.Friends;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumHand;

@Info(name = "Kill32kAura", category = Module.Category.COMBAT)
public class Kill32kAura extends Module {
    private int hasWaited;

    private Setting<Float> range = register(Settings.f("Range", 5.0F));
    private Setting<Boolean> only32k = register(Settings.b("32k Only", true));
    private Setting<Boolean> playersOnly = register(Settings.b("Players only", true));
    private Setting<Boolean> switch32k = register(Settings.b("32k Switch", true));
    private Setting<Double> delay = register(Settings.d("Delay in ticks", 0.0D));

    @Override
    public void onUpdate() {
        if (!isEnabled() || mc.player.isDead || mc.world == null)
            return;
        if (hasWaited <  delay.getValue()) {
            hasWaited++;
            return;
        }
        hasWaited = 0;
        for (Entity entity : mc.world.loadedEntityList) {
            if (!(entity instanceof EntityLivingBase) ||
                    entity == mc.player)
                continue;
            if (mc.player.getDistance(entity) >  range.getValue()
                    || ((EntityLivingBase)entity).getHealth() <= 0.0F
                    || (!(entity instanceof EntityPlayer) && (playersOnly.getValue()).booleanValue())
                    || (!isSuperWeapon(mc.player.getHeldItemMainhand())
                    && (only32k.getValue()).booleanValue()))
                continue;
            if ( switch32k.getValue())
                equipBestWeapon();
            if (!Friends.isFriend(entity.getName())) {
                mc.playerController.attackEntity(mc.player, entity);
                mc.player.swingArm(EnumHand.MAIN_HAND);
            }
        }
    }

    private boolean isSuperWeapon(ItemStack item) {
        if (item == null)
            return false;
        if (item.getTagCompound() == null)
            return false;
        if (item.getEnchantmentTagList().getTagType() == 0)
            return false;
        NBTTagList enchants = (NBTTagList)item.getTagCompound().getTag("ench");
        int i = 0;
        while (i < enchants.tagCount()) {
            NBTTagCompound enchant = enchants.getCompoundTagAt(i);
            if (enchant.getInteger("id") == 16) {
                int lvl = enchant.	getInteger("lvl");
                if (lvl >= 16)
                    return true;
                break;
            }
            i++;
        }
        return false;
    }

    public static void equipBestWeapon() {
        int bestSlot = -1;
        double maxDamage = 0.0D;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (!stack.isEmpty)
                if (stack.getItem() instanceof ItemTool) {
                    double damage = ((ItemTool)stack.getItem()).attackDamage + EnchantmentHelper.getModifierForCreature(stack, EnumCreatureAttribute.UNDEFINED);
                    if (damage > maxDamage) {
                        maxDamage = damage;
                        bestSlot = i;
                    }
                } else if (stack.getItem() instanceof ItemSword) {
                    double damage = ((ItemSword)stack.getItem()).getAttackDamage() + EnchantmentHelper.getModifierForCreature(stack, EnumCreatureAttribute.UNDEFINED);
                    if (damage > maxDamage) {
                        maxDamage = damage;
                        bestSlot = i;
                    }
                }
        }
        if (bestSlot != -1)
            equip(bestSlot);
    }

    private static void equip(int slot) {
        mc.player.inventory.currentItem = slot;
        mc.playerController.syncCurrentPlayItem();
    }
}
