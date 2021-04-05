package meow.konc.hack.modules.misc;

import meow.konc.hack.command.Command;
import meow.konc.hack.module.Module;
import meow.konc.hack.setting.Setting;
import meow.konc.hack.setting.Settings;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagList;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

@Module.Info(name = "PlayerDetect", category = Module.Category.MISC, description = "Hits entities around you")
public class PlayerDetect extends Module
{
    private Setting<Boolean> sharp32 = register(Settings.b("sharp32"));
    public Setting<Boolean> watermark = register(Settings.booleanBuilder("Watermark").withValue(Boolean.valueOf(true)).withVisibility(b -> sharp32.getValue()).build());
    public Setting<Boolean> color = register(Settings.booleanBuilder("Color").withValue(Boolean.valueOf(true)).withVisibility(b -> sharp32.getValue()).build());
    private Set<EntityPlayer> sword = Collections.newSetFromMap(new WeakHashMap<>());

    private Setting<Boolean> strength = register(Settings.b("Strength"));
    public Setting<Boolean> watermark2 = register(Settings.booleanBuilder("Watermark").withValue(Boolean.valueOf(true)).withVisibility(b -> strength.getValue()).build());
    public Setting<Boolean> color2 = register(Settings.booleanBuilder("Color").withValue(Boolean.valueOf(true)).withVisibility(b -> strength.getValue()).build());
    private Set<EntityPlayer> str = Collections.newSetFromMap(new WeakHashMap<>());
    
    public static Minecraft mc;
    
    private boolean is32k(EntityPlayer player, ItemStack stack) {
        if (stack.getItem() instanceof ItemSword) {
            NBTTagList enchants = stack.getEnchantmentTagList();
            if (enchants != null) {
                for (int i = 0; i < enchants.tagCount(); ++i) {
                    if (enchants.getCompoundTagAt(i).getShort("lvl") >= 32767) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    @Override
    public void onUpdate() {
        if(sharp32.getValue()) {
            for (EntityPlayer player : mc.world.playerEntities) {
                if (player.equals(mc.player)) {
                    continue;
                }
                if (is32k(player, player.itemStackMainHand) && !sword.contains(player)) {
                    if (watermark.getValue()) {
                        if (color.getValue()) {
                            Command.sendChatMessage("&4" + player.getDisplayNameString() + " is holding a 32k");
                        } else {
                            Command.sendChatMessage(player.getDisplayNameString() + " is holding a 32k");
                        }
                    } else if (color.getValue()) {
                        Command.sendRawChatMessage("&4" + player.getDisplayNameString() + " is holding a 32k");
                    } else {
                        Command.sendRawChatMessage(player.getDisplayNameString() + " is holding a 32k");
                    }
                    sword.add(player);
                }
                if (!sword.contains(player)) {
                    continue;
                }
                if (is32k(player, player.itemStackMainHand)) {
                    continue;
                }
                if (watermark.getValue()) {
                    if (color.getValue()) {
                        Command.sendChatMessage("&2" + player.getDisplayNameString() + " is no longer holding a 32k");
                    } else {
                        Command.sendChatMessage(player.getDisplayNameString() + " is no longer holding a 32k");
                    }
                } else if (color.getValue()) {
                    Command.sendRawChatMessage("&2" + player.getDisplayNameString() + " is no longer holding a 32k");
                } else {
                    Command.sendRawChatMessage(player.getDisplayNameString() + " is no longer holding a 32k");
                }
                sword.remove(player);
            }
        }
        if(strength.getValue()) {
            for (EntityPlayer player : mc.world.playerEntities) {
                if (player.equals(mc.player)) {
                    continue;
                }
                if (player.isPotionActive(MobEffects.STRENGTH) && !str.contains(player)) {
                    if (watermark2.getValue()) {
                        if (color.getValue()) {
                            Command.sendChatMessage("\u00a7a&a" + player.getDisplayNameString() + " \u00a7chas drank strength");
                        }
                        else {
                            Command.sendChatMessage(player.getDisplayNameString() + " \u00a7chas drank strength");
                        }
                    }
                    else if (color.getValue()) {
                        Command.sendRawChatMessage("\u00a7a&a" + player.getDisplayNameString() + " \u00a7chas drank strength");
                    }
                    else {
                        Command.sendRawChatMessage(player.getDisplayNameString() + " \u00a7chas drank strength");
                    }
                    str.add(player);
                }
                if (!str.contains(player)) {
                    continue;
                }
                if (player.isPotionActive(MobEffects.STRENGTH)) {
                    continue;
                }
                if (watermark2.getValue()) {
                    if (color.getValue()) {
                        Command.sendChatMessage("\u00a7a&c" + player.getDisplayNameString() + " \u00a7chas ran out of strength");
                    }
                    else {
                        Command.sendChatMessage(player.getDisplayNameString() + " \u00a7chas ran out of strength");
                    }
                }
                else if (color2.getValue()) {
                    Command.sendRawChatMessage("\u00a7a&c" + player.getDisplayNameString() + " \u00a7chas ran out of strength");
                }
                else {
                    Command.sendRawChatMessage(player.getDisplayNameString() + " \u00a7chas ran out of strength");
                }
                str.remove(player);
            }
        }
    }
    
    static {
        mc = Minecraft.getMinecraft();
    }
}
