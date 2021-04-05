/*package meow.konc.hack.modules.combat;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import meow.konc.hack.module.Module;
import meow.konc.hack.module.ModuleManager;
import meow.konc.hack.modules.hidden.util.KamiCrystal;
import meow.konc.hack.setting.Setting;
import meow.konc.hack.setting.Settings;
import meow.konc.hack.util.other.Friends;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.play.server.SPacketDisconnect;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;

import java.util.List;
import java.util.stream.Collectors;

@Module.Info(name = "AutoLog", description = "Automatically log when in danger or on low health", category = Module.Category.COMBAT)
public class AutoLog extends Module {

    private Setting<Integer> health = register(Settings.integerBuilder("Health").withRange(0, 36).withValue(6).build());
    private boolean shouldLog = false;
    private Setting<Boolean> kdetectlog = register(Settings.b("32kLog"));
    public Setting<Double> range = register(Settings.doubleBuilder("DetectRange").withMinimum(1.0).withValue(5.0).withMaximum(7.0).withVisibility(v -> kdetectlog.getValue()).build());
    long lastLog = System.currentTimeMillis();

    @EventHandler
    private Listener<LivingDamageEvent> livingDamageEventListener = new Listener<>(event -> {
        if (mc.player == null) return;
        if (event.getEntity() == mc.player) {
            if (mc.player.getHealth() - event.getAmount() < health.getValue()) {
                log();
            }
        }
    });

    @EventHandler
    private Listener<EntityJoinWorldEvent> entityJoinWorldEventListener = new Listener<>(event -> {
        if (mc.player == null) return;
        if (event.getEntity() instanceof EntityEnderCrystal) {
            if (mc.player.getHealth() - KamiCrystal.calculateDamage((EntityEnderCrystal) event.getEntity(), mc.player) < health.getValue()) {
                log();
            }
        }
    });

    private boolean is32k(final ItemStack stack) {
        if (stack.getItem() instanceof ItemSword) {
            final NBTTagList enchants = stack.getEnchantmentTagList();
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
        if (!shouldLog) {
            List<EntityPlayer> w = mc.world.playerEntities.stream().filter(e -> !Friends.isFriend(e.getName())).collect(Collectors.toList());
            if (kdetectlog.getValue()) {
                for (EntityPlayer player : w) {
                    if (mc.player == null || mc.player == player) {
                        continue;
                    }
                    if (is32k(player.itemStackMainHand)) {
                        if (player.getDistance(mc.player) < range.getValue()) {
                            shouldLog = true;
                            break;
                        }
                    }
                }
            }
        }
        if (shouldLog) {
            shouldLog = false;
            if (System.currentTimeMillis() - lastLog < 2000) return;
            Minecraft.getMinecraft().getConnection().handleDisconnect(new SPacketDisconnect(new TextComponentString("AutoLogged")));
        }
    }

    private void log() {
        ModuleManager.getModuleByName("AutoReconnect").disable();
        shouldLog = true;
        lastLog = System.currentTimeMillis();
    }

}*/
