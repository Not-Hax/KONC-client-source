package meow.konc.hack.modules.combat;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import meow.konc.hack.KONCMod;
import meow.konc.hack.event.events.other.PacketEvent;
import meow.konc.hack.module.Module;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketUseEntity;

@Module.Info(name = "Criticals", description = "Crits", category = Module.Category.COMBAT)
public class Criticals extends Module {

    public void onEnable(){
        KONCMod.EVENT_BUS.subscribe(this);
    }

    public void onDisable(){
        KONCMod.EVENT_BUS.unsubscribe(this);
    }

    @EventHandler
    private Listener<PacketEvent.Send> sendListener = new Listener<>(event -> {
        if(mc.player.onGround && event.getPacket() instanceof CPacketUseEntity) {
            CPacketUseEntity w = (CPacketUseEntity) event.getPacket();
            if (w.getEntityFromWorld(mc.world) instanceof EntityPlayer) {
                mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, true));
                mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.10000000149011612, mc.player.posZ, false));
                mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, false));
            }
        }
    });
}