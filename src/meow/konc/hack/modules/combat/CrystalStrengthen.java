package meow.konc.hack.modules.combat;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import meow.konc.hack.event.events.other.PacketEvent;
import meow.konc.hack.module.Module;
import meow.konc.hack.module.ModuleManager;
import meow.konc.hack.setting.Setting;
import meow.konc.hack.setting.Settings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;

import java.util.function.Predicate;

@Module.Info(name = "CrystalStrengthen", category = Module.Category.COMBAT, description = "Fast Place But For Crystals", showOnArray = Module.ShowOnArray.OFF)
public class CrystalStrengthen extends Module {

    private Setting<Boolean> weaknessattack = register(Settings.b("Weakness Attack", true));
    private Setting<Boolean> fastcrystal = register(Settings.b("Fast Crystal", true));

    int slotBefore;
    int bestSlot;

    @EventHandler
    private Listener<PacketEvent.Receive> receiveListener = new Listener<PacketEvent.Receive>(event -> {
        if (fastcrystal.getValue()) {
            if (mc.player != null && (mc.player.getHeldItemMainhand().getItem() == Items.END_CRYSTAL || mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL)) {
                mc.rightClickDelayTimer = 0;
            }
        }
    }, new Predicate[0]);


    @Override
    public void onUpdate() {
        if (weaknessattack.getValue()) {
            if (!isEnabled()) {
                return;
            }
            if (ModuleManager.getModuleByName("AutoCrystal").isEnabled() && mc.player.isPotionActive(MobEffects.WEAKNESS)) {
                for (final Entity e : mc.world.loadedEntityList) {
                    if (mc.player.getDistanceSq(e) <= 36.0 && e instanceof EntityEnderCrystal) {
                        mc.player.inventory.currentItem = bestSlot;
                    }
                }
            }
            bestSlot = -1;
            final int PrevSlot = mc.player.inventory.currentItem;
            for (int i = 0; i < 9; ++i) {
                final ItemStack item = mc.player.inventory.getStackInSlot(i);
                if (item != null) {
                    if (item.getItem() instanceof ItemSword) {
                        bestSlot = i;
                    }
                }
            }
            if (bestSlot == -1) {
                return;
            }
            slotBefore = mc.player.inventory.currentItem;
            if (slotBefore == -1) {
                return;
            }
        }
    }
}
