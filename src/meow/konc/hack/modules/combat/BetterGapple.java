package meow.konc.hack.modules.combat;

import meow.konc.hack.event.events.other.PacketEvent;
import meow.konc.hack.module.Module;
import meow.konc.hack.module.Module.Info;
import meow.konc.hack.setting.Setting;
import meow.konc.hack.setting.Settings;
import meow.konc.hack.util.packet.Wrapper;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;

import java.util.function.Predicate;

@Info(name = "BetterGapple", description = "Attempts to stop gapple disease and makes eating easier in low tps", category = Module.Category.COMBAT)
public class BetterGapple extends Module {
    boolean timer;
    boolean cancelUseItem = false;
    double waitTicks;
    int lastSlot;
    private Setting<Boolean> fastEat = register(Settings.b("Fast Eat", true));
    private Setting<Boolean> tpsCheck = register(Settings.booleanBuilder("TPS Check").withValue(true).withVisibility(v -> fastEat.getValue().equals(true)).build());
    private Setting<Double> startTPS = register(Settings.doubleBuilder("Enable TPS").withMinimum(0.0).withValue(13.0).withMaximum(20.0).withVisibility(v -> fastEat.getValue().equals(true)).build());
    private Setting<Boolean> noGapGlitch = register(Settings.b("Anti Gap Disease", false));
    private Setting<Double> wait = register(Settings.doubleBuilder("Switch Wait").withMinimum(0.0).withValue(5.0).withMaximum(20.0).withVisibility(v -> noGapGlitch.getValue().equals(true)).build());
    @EventHandler
    private Listener<PacketEvent.Send> sendListener;

    public BetterGapple() {
        timer = false;
        sendListener = new Listener<PacketEvent.Send>(e -> {
            if (e.getPacket() instanceof CPacketPlayerTryUseItem) {
                timer = true;
                if (cancelUseItem) {
                    e.cancel();
                }
            }
        }, new Predicate[0]);
    }

    @Override
    public void onUpdate() {
        if (!timer) {
            waitTicks = wait.getValue() * 20.0 + 50.0;
        }
        if (noGapGlitch.getValue().booleanValue()) {
            waitTicks--;
            if (waitTicks <= 14.0D) {
                cancelUseItem = true;
                lastSlot = (Wrapper.getPlayer()).inventory.currentItem;
                mc.player.connection.sendPacket(new CPacketHeldItemChange(6));
                (Wrapper.getPlayer()).inventory.currentItem = 1;
            }
            if (waitTicks <= 4.0D) {
                timer = false;
                mc.player.connection.sendPacket(new CPacketHeldItemChange(lastSlot));
                (Wrapper.getPlayer()).inventory.currentItem = lastSlot;
            }
        }
    }
}
