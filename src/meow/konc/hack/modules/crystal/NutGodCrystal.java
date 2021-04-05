/*package meow.konc.hack.modules.crystal;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import meow.konc.hack.command.Command;
import meow.konc.hack.event.events.other.PacketEvent;
import meow.konc.hack.event.events.other.RenderEvent;
import meow.konc.hack.module.Module;
import meow.konc.hack.module.ModuleManager;
import meow.konc.hack.modules.combat.AutoEZ;
import meow.konc.hack.setting.Setting;
import meow.konc.hack.setting.Settings;
import meow.konc.hack.util.other.Friends;
import meow.konc.hack.util.render.KONCTessellator;
import meow.konc.hack.util.util.EntityUtil;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.potion.Potion;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Module.Info(name = "NutGodCrystal", category = Module.Category.CRYSTAL)
public class NutGodCrystal extends Module {
    private Setting<Boolean> place = register(Settings.b("Place", true));
    private Setting<Boolean> raytrace = register(Settings.b("RayTrace", false));
    private Setting<Boolean> autoSwitch = register(Settings.b("AutoSwitch", true));
    private Setting<Boolean> antiStuck = register(Settings.b("AntiStuck", true));
    private Setting<Boolean> multiPlace = register(Settings.b("MultiPlace", false));
    private Setting<Boolean> alert = register(Settings.b("ChatAlerts", true));
    private Setting<Boolean> antiSui = register(Settings.b("AntiSuicide", true));
    private Setting<Integer> attackSpeed = register((Setting<Integer>) Settings.integerBuilder("AttackSpeed").withMinimum(0).withMaximum(20).withValue(17).build());
    private Setting<Integer> placeDelay = register((Setting<Integer>) Settings.integerBuilder("PlaceDelay").withMinimum(0).withMaximum(50).withValue(0).build());
    private Setting<Integer> enemyRange = register((Setting<Integer>) Settings.integerBuilder("EnemyRange").withMinimum(1).withMaximum(13).withValue(9).build());
    private Setting<Integer> minDamage = register((Setting<Integer>) Settings.integerBuilder("MinDamage").withMinimum(0).withMaximum(16).withValue(4).build());
    private Setting<Integer> facePlace = register((Setting<Integer>) Settings.integerBuilder("FacePlace").withMinimum(0).withMaximum(16).withValue(7).build());
    private Setting<Integer> multiPlaceSpeed = register((Setting<Integer>) Settings.integerBuilder("MultiPlaceSpeed").withMinimum(1).withMaximum(10).withValue(4).build());
    private Setting<Integer> placeRange = register((Setting<Integer>) Settings.integerBuilder("PlaceRange").withMinimum(1).withMaximum(6).withValue(6).build());
    private Setting<Integer> breakRange = register((Setting<Integer>) Settings.integerBuilder("BreakRange").withMinimum(1).withMaximum(6).withValue(6).build());
    private Setting<Integer> Red = register(Settings.integerBuilder("Red").withMinimum(1).withMaximum(255).withValue(255));
    private Setting<Integer> Green = register(Settings.integerBuilder("Green").withMinimum(1).withMaximum(255).withValue(255));
    private Setting<Integer> Blue = register(Settings.integerBuilder("Blue").withMinimum(1).withMaximum(255).withValue(255));
    private Setting<Boolean> rainbow = register(Settings.b("Rainbow", true));
    private BlockPos render;
    public boolean isActive = false;
    private Entity renderEnt;
    private long placeSystemTime = -1L;
    private long breakSystemTime = -1L;
    private long chatSystemTime = -1L;
    private long multiPlaceSystemTime = -1L;
    private long antiStuckSystemTime = -1L;
    private static boolean togglePitch;
    private boolean switchCooldown = false;
    private int newSlot;
    private int placements = 0;
    private static boolean isSpoofingAngles;
    private static double yaw;
    private static double pitch;
    private EntityPlayer target;

    @EventHandler
    private Listener<PacketEvent.Send> packetListener;

    public NutGodCrystal() {
        Packet[] packet = new Packet[1];
        packetListener = new Listener<PacketEvent.Send>(event -> {
            packet[0] = event.getPacket();
            if (packet[0] instanceof CPacketPlayer && isSpoofingAngles) {
                ((CPacketPlayer) packet[0]).yaw = (float) yaw;
                ((CPacketPlayer) packet[0]).pitch = (float) pitch;
            }
        }, (Predicate<PacketEvent.Send>[]) new Predicate[0]);
    }

    @Override
    public void onUpdate() {
        EntityEnderCrystal crystal = (EntityEnderCrystal) mc.world.loadedEntityList.stream().filter(entity -> entity instanceof EntityEnderCrystal).map(entity -> entity).min(Comparator.comparing(c -> mc.player.getDistance(c))).orElse(null);
        if (crystal != null && mc.player.getDistance((Entity) crystal) <= breakRange.getValue()) {
            if (System.nanoTime() / 1000000L - breakSystemTime >= 420 - attackSpeed.getValue() * 20) {
                lookAtPacket(crystal.posX, crystal.posY, crystal.posZ, (EntityPlayer) mc.player);
                mc.playerController.attackEntity((EntityPlayer) mc.player, (Entity) crystal);
                mc.player.swingArm(EnumHand.MAIN_HAND);
                breakSystemTime = System.nanoTime() / 1000000L;
            }
            if (multiPlace.getValue()) {
                if (System.nanoTime() / 1000000L - multiPlaceSystemTime >= 20 * multiPlaceSpeed.getValue() && System.nanoTime() / 1000000L - antiStuckSystemTime <= 400 + (400 - attackSpeed.getValue() * 20)) {
                    multiPlaceSystemTime = System.nanoTime() / 1000000L;
                    return;
                }
            } else if (System.nanoTime() / 1000000L - antiStuckSystemTime <= 400 + (400 - attackSpeed.getValue() * 20)) {
                return;
            }
        } else {
            resetRotation();
        }
        int crystalSlot = (mc.player.getHeldItemMainhand().getItem() == Items.END_CRYSTAL) ? mc.player.inventory.currentItem : -1;
        if (crystalSlot == -1) {
            for (int l = 0; l < 9; ++l) {
                if (mc.player.inventory.getStackInSlot(l).getItem() == Items.END_CRYSTAL) {
                    crystalSlot = l;
                    break;
                }
            }
        }
        boolean offhand = false;
        if (mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL) {
            offhand = true;
        } else if (crystalSlot == -1) {
            return;
        }
        Entity ent = null;
        Entity lastTarget = null;

        BlockPos finalPos = null;
        List<BlockPos> blocks = findCrystalBlocks();
        List<Entity> entities = new ArrayList<Entity>();
        entities.addAll((Collection<? extends Entity>) mc.world.playerEntities.stream().filter(entityPlayer -> !Friends.isFriend(entityPlayer.getName())).collect(Collectors.toList()));
        double damage = 0.5;
        for (Entity entity2 : entities) {
            if (entity2 != mc.player) {
                if (((EntityLivingBase) entity2).getHealth() <= 0.0f) {
                    continue;

                }
                if (mc.player.getDistanceSq(entity2) > enemyRange.getValue() * enemyRange.getValue()) {
                    continue;
                }
                for (BlockPos blockPos : blocks) {
                    if (!canBlockBeSeen(blockPos) && mc.player.getDistanceSq(blockPos) > 25.0 && raytrace.getValue()) {
                        continue;
                    }
                    double b = entity2.getDistanceSq(blockPos);
                    if (b > 56.2) {
                        continue;
                    }
                    double d = calculateDamage(blockPos.x + 0.5, blockPos.y + 1, blockPos.z + 0.5, entity2);
                    if (d < minDamage.getValue() && ((EntityLivingBase) entity2).getHealth() + ((EntityLivingBase) entity2).getAbsorptionAmount() > facePlace.getValue()) {
                        continue;
                    }
                    if (d <= damage) {
                        continue;
                    }
                    double self = calculateDamage(blockPos.x + 0.5, blockPos.y + 1, blockPos.z + 0.5, (Entity) mc.player);
                    if (antiSui.getValue()) {
                        if (mc.player.getHealth() + mc.player.getAbsorptionAmount() - self <= 7.0) {
                            continue;
                        }
                        if (self > d) {
                            continue;
                        }
                    }
                    damage = d;
                    finalPos = blockPos;
                    ent = entity2;
                    lastTarget = entity2;

                }
            }
        }
        if (damage == 0.5) {
            render = null;
            renderEnt = null;
            resetRotation();
            return;
        }
        if (lastTarget instanceof EntityPlayer && ModuleManager.getModuleByName("AutoEZ").isEnabled()) {
            AutoEZ AutoEZ = (AutoEZ) ModuleManager.getModuleByName("AutoEZ");
            AutoEZ.addTargetedPlayer(lastTarget.getName());
        }
        render = finalPos;
        renderEnt = ent;

        if (place.getValue()) {
            if (!offhand && mc.player.inventory.currentItem != crystalSlot) {
                if (autoSwitch.getValue()) {
                    mc.player.inventory.currentItem = crystalSlot;
                    resetRotation();
                    switchCooldown = true;
                }
                return;
            }
            lookAtPacket(finalPos.x + 0.5, finalPos.y - 0.5, finalPos.z + 0.5, (EntityPlayer) mc.player);
            RayTraceResult result = mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ), new Vec3d(finalPos.x + 0.5, finalPos.y - 0.5, finalPos.z + 0.5));
            EnumFacing f;
            if (result == null || result.sideHit == null) {
                f = EnumFacing.UP;
            } else {
                f = result.sideHit;
            }
            if (switchCooldown) {
                switchCooldown = false;
                return;
            }
            if (System.nanoTime() / 1000000L - placeSystemTime >= placeDelay.getValue() * 2) {
                mc.player.connection.sendPacket((Packet) new CPacketPlayerTryUseItemOnBlock(finalPos, f, offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, 0.0f, 0.0f, 0.0f));
                ++placements;
                antiStuckSystemTime = System.nanoTime() / 1000000L;
                placeSystemTime = System.nanoTime() / 1000000L;
            }
        }
        if (isSpoofingAngles) {
            if (togglePitch) {
                EntityPlayerSP player = mc.player;
                player.rotationPitch += (float) 4.0E-4;
                togglePitch = false;
            } else {
                EntityPlayerSP player2 = mc.player;
                player2.rotationPitch -= (float) 4.0E-4;
                togglePitch = true;
            }
        }
    }

    @Override
    public void onWorldRender(RenderEvent event) {
        if (render != null) {
            float[] hue = {(System.currentTimeMillis() % (360 * 32)) / (360f * 32)};
            int rgb = Color.HSBtoRGB(hue[0], 1, 1);
            int r = (rgb >> 16) & 0xFF;
            int g = (rgb >> 8) & 0xFF;
            int b = rgb & 0xFF;
            if (rainbow.getValue()) {
                KONCTessellator.prepare(7);
                KONCTessellator.drawBox(render, r, g, b, 77, 63);
                KONCTessellator.release();
                KONCTessellator.prepare(7);
                KONCTessellator.drawBoundingBoxBlockPos(render, 1.00f, r, g, b, 255);
            } else {
                KONCTessellator.prepare(7);
                KONCTessellator.drawBox(render, Red.getValue(), Green.getValue(), Blue.getValue(), 77, 63);
                KONCTessellator.release();
                KONCTessellator.prepare(7);
                KONCTessellator.drawBoundingBoxBlockPos(render, 1.00f, Red.getValue(), Green.getValue(), Blue.getValue(), 244);
            }
            KONCTessellator.release();
        }

    }

    private void lookAtPacket(double px, double py, double pz, EntityPlayer me) {
        double[] v = EntityUtil.calculateLookAt(px, py, pz, me);
        setYawAndPitch((float) v[0], (float) v[1]);
    }

    private boolean canPlaceCrystal(BlockPos blockPos) {
        BlockPos boost = blockPos.add(0, 1, 0);
        BlockPos boost2 = blockPos.add(0, 2, 0);
        return (mc.world.getBlockState(blockPos).getBlock() == Blocks.BEDROCK || mc.world.getBlockState(blockPos).getBlock() == Blocks.OBSIDIAN) && mc.world.getBlockState(boost).getBlock() == Blocks.AIR && mc.world.getBlockState(boost2).getBlock() == Blocks.AIR && mc.world.getEntitiesWithinAABB((Class) Entity.class, new AxisAlignedBB(boost)).isEmpty() && mc.world.getEntitiesWithinAABB((Class) Entity.class, new AxisAlignedBB(boost2)).isEmpty();
    }

    public static BlockPos getPlayerPos() {
        return new BlockPos(Math.floor(mc.player.posX), Math.floor(mc.player.posY), Math.floor(mc.player.posZ));
    }

    private List<BlockPos> findCrystalBlocks() {
        NonNullList positions = NonNullList.create();
        positions.addAll((Collection) getSphere(getPlayerPos(), placeRange.getValue().floatValue(), placeRange.getValue(), false, true, 0).stream().filter(this::canPlaceCrystal).collect(Collectors.toList()));
        return (List<BlockPos>) positions;
    }

    public List<BlockPos> getSphere(BlockPos loc, float r, int h, boolean hollow, boolean sphere, int plus_y) {
        List<BlockPos> circleblocks = new ArrayList<BlockPos>();
        int cx = loc.getX();
        int cy = loc.getY();
        int cz = loc.getZ();
        for (int x = cx - (int) r; x <= cx + r; ++x) {
            for (int z = cz - (int) r; z <= cz + r; ++z) {
                for (int y = sphere ? (cy - (int) r) : cy; y < (sphere ? (cy + r) : ((float) (cy + h))); ++y) {
                    double dist = (cx - x) * (cx - x) + (cz - z) * (cz - z) + (sphere ? ((cy - y) * (cy - y)) : 0);
                    if (dist < r * r && (!hollow || dist >= (r - 1.0f) * (r - 1.0f))) {
                        BlockPos l = new BlockPos(x, y + plus_y, z);
                        circleblocks.add(l);
                    }
                }
            }
        }
        return circleblocks;
    }

    public static float calculateDamage(double posX, double posY, double posZ, Entity entity) {
        float doubleExplosionSize = 12.0f;
        double distancedsize = entity.getDistance(posX, posY, posZ) / doubleExplosionSize;
        Vec3d vec3d = new Vec3d(posX, posY, posZ);
        double blockDensity = entity.world.getBlockDensity(vec3d, entity.getEntityBoundingBox());
        double v = (1.0 - distancedsize) * blockDensity;
        float damage = (float) (int) ((v * v + v) / 2.0 * 7.0 * doubleExplosionSize + 1.0);
        double finald = 1.0;
        if (entity instanceof EntityLivingBase) {
            finald = getBlastReduction((EntityLivingBase) entity, getDamageMultiplied(damage), new Explosion((World) mc.world, (Entity) null, posX, posY, posZ, 6.0f, false, true));
        }
        return (float) finald;
    }

    public static float getBlastReduction(EntityLivingBase entity, float damage, Explosion explosion) {
        if (entity instanceof EntityPlayer) {
            EntityPlayer ep = (EntityPlayer) entity;
            DamageSource ds = DamageSource.causeExplosionDamage(explosion);
            damage = CombatRules.getDamageAfterAbsorb(damage, (float) ep.getTotalArmorValue(), (float) ep.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());
            int k = EnchantmentHelper.getEnchantmentModifierDamage(ep.getArmorInventoryList(), ds);
            float f = MathHelper.clamp((float) k, 0.0f, 20.0f);
            damage *= 1.0f - f / 25.0f;
            if (entity.isPotionActive(Potion.getPotionById(11))) {
                damage -= damage / 4.0f;
            }
            return damage;
        }
        damage = CombatRules.getDamageAfterAbsorb(damage, (float) entity.getTotalArmorValue(), (float) entity.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());
        return damage;
    }

    private static float getDamageMultiplied(float damage) {
        int diff = mc.world.getDifficulty().getId();
        return damage * ((diff == 0) ? 0.0f : ((diff == 2) ? 1.0f : ((diff == 1) ? 0.5f : 1.5f)));
    }

    public static float calculateDamage(EntityEnderCrystal crystal, Entity entity) {
        return calculateDamage(crystal.posX, crystal.posY, crystal.posZ, entity);
    }

    public static boolean canBlockBeSeen(BlockPos blockPos) {
        return mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ), new Vec3d((double) blockPos.getX(), (double) blockPos.getY(), (double) blockPos.getZ()), false, true, false) == null;
    }

    private static void setYawAndPitch(float yaw1, float pitch1) {
        yaw = yaw1;
        pitch = pitch1;
        isSpoofingAngles = true;
    }

    private static void resetRotation() {
        if (isSpoofingAngles) {
            yaw = mc.player.rotationYaw;
            pitch = mc.player.rotationPitch;
            isSpoofingAngles = false;
        }
    }

    @Override
    protected void onEnable() {

        if (alert.getValue() && mc.world != null) {
            Command.sendRawChatMessage("\u00A7NutGodCrystal ON");
        }
    }

    @Override
    public void onDisable() {
        if (alert.getValue() && mc.world != null) {
            Command.sendRawChatMessage("\u00A7NutGodCrystal" + ChatFormatting.RED.toString() + "OFF");
        }
        render = null;
        resetRotation();
    }

    static {
        togglePitch = false;
    }
}*/
