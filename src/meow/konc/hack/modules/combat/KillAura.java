package meow.konc.hack.modules.combat;


import meow.konc.hack.setting.Setting;
import meow.konc.hack.KONCMod;
import meow.konc.hack.event.events.other.PacketEvent;
import meow.konc.hack.module.Module;
import meow.konc.hack.module.ModuleManager;
import meow.konc.hack.util.util.EntityUtil;
import meow.konc.hack.util.other.Friends;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemSword;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumHand;
import meow.konc.hack.setting.Settings;

import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Module.Info(name = "KillAura", description = "Better Killaura", category = Module.Category.COMBAT)
public class KillAura extends Module {

    private Setting<Double> range = register(Settings.d("Range", 4.5));
    private Setting<Boolean> swordOnly = register(Settings.b("kaswordonly" , false));
    private Setting<Boolean> caCheck = register(Settings.b("CAuraCheck" , false));
    private Setting<Boolean> criticals = register(Settings.b("kaCriticals", true));
    private boolean togglePitch;
    private boolean attack = false;
    private Entity lastentity;

    private boolean isAttacking = false;
    public void onUpdate(){
        if(mc.player == null || mc.player.isDead) return;
        List<Entity> targets = mc.world.loadedEntityList.stream()
                .filter(entity -> entity != mc.player)
                .filter(entity -> mc.player.getDistance(entity) <= range.getValue())
                .filter(entity -> !entity.isDead)
                .filter(entity -> entity instanceof EntityPlayer)
                .filter(entity -> ((EntityPlayer) entity).getHealth() > 0)
                .filter(entity -> !Friends.isFriend(entity.getName()))
                .sorted(Comparator.comparing(e->mc.player.getDistance(e)))
                .collect(Collectors.toList());
        attack = false;
        targets.forEach(target ->{
            if(swordOnly.getValue()) {
                if (!(mc.player.getHeldItemMainhand().getItem() instanceof ItemSword)) return;
            }
            if(caCheck.getValue()) {
                if (ModuleManager.isModuleEnabled("AutoCrystal")) return;
            }
            attack(target);
            if (!attack) {
                targets.forEach(e -> {
                    if (lastentity != null && e.getName() == lastentity.getName()) {
                        lookAtPacket(e.posX, e.posY, e.posZ, mc.player);
                        return;
                    }
                });
                if (isSpoofingAngles) {
                    if (togglePitch) {
                        mc.player.rotationPitch += 0.0004;
                        togglePitch = false;
                    } else {
                        mc.player.rotationPitch -= 0.0004;
                        togglePitch = true;
                    }
                }
            }
        });

    }

    @EventHandler
    private Listener<PacketEvent.Send> sendListener = new Listener<>(event -> {
        if (event.getPacket() instanceof CPacketUseEntity && ((CPacketUseEntity) event.getPacket()).getAction() == CPacketUseEntity.Action.ATTACK) {
            lastentity = ((CPacketUseEntity) event.getPacket()).getEntityFromWorld(mc.world);
        }
    });
    @EventHandler
    private Listener<PacketEvent.Send> listener = new Listener<>(event -> {
        if (event.getPacket() instanceof CPacketUseEntity) {
            if(criticals.getValue() && !ModuleManager.isModuleEnabled("Criticals") && ((CPacketUseEntity) event.getPacket()).getAction() == CPacketUseEntity.Action.ATTACK && mc.player.onGround && isAttacking) {
                mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, true));
                mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.10000000149011612, mc.player.posZ, false));
                mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, false));
            }
        }
    });


    public static void lookAtPacket(final double px, final double py, final double pz, final EntityPlayer me) {
        final double[] v = EntityUtil.calculateLookAt(px, py, pz, me);
        setYawAndPitch((float)v[0], (float)v[1]);
    }
    public void onEnable(){
        lastentity = null;
        KONCMod.EVENT_BUS.subscribe(this);
    }

    public void onDisable(){
        resetRotation();
        KONCMod.EVENT_BUS.unsubscribe(this);
    }

    public static boolean isSpoofingAngles;
    public static double yaw;
    public static double pitch;
    private static void setYawAndPitch(float yaw1, float pitch1) {
        Random rand = new Random(2);
        yaw = yaw1 + (rand.nextFloat() / 100);
        pitch = pitch1 + (rand.nextFloat() / 100);
        isSpoofingAngles = true;
        mc.player.rotationYawHead = yaw1;
    }
    public static void resetRotation() {
        if (isSpoofingAngles) {
            yaw = mc.player.rotationYaw;
            pitch = mc.player.rotationPitch;
            isSpoofingAngles = false;
        }
    }
    public void attack(Entity e){
        if(mc.player.getCooledAttackStrength(0) >= 1) {
            isAttacking = true;
            lookAtPacket(e.posX, e.posY, e.posZ, mc.player);
            mc.playerController.attackEntity(mc.player, e);
            mc.player.swingArm(EnumHand.MAIN_HAND);
            isAttacking = false;
            attack = true;
            if (!ModuleManager.getModuleByName("AutoCrystal").isDisabled()) {
                (((AutoCrystal)ModuleManager.getModuleByName("AutoCrystal")).renderEnt) = e;
            }
        }

    }
    @EventHandler
    private Listener<PacketEvent.Receive> packetListener = new Listener<>(event -> {
        Packet packet = event.getPacket();
        if (packet instanceof CPacketPlayer) {
            if (isSpoofingAngles) {
                ((CPacketPlayer) packet).yaw = (float) yaw;
                ((CPacketPlayer) packet).pitch = (float) pitch;
            }
        }
    });
}