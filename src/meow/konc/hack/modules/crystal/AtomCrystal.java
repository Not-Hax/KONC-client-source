/*package meow.konc.hack.modules.crystal;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import meow.konc.hack.KONCMod;
import meow.konc.hack.event.events.other.PacketEvent;
import meow.konc.hack.event.events.other.RenderEvent;
import meow.konc.hack.module.Module;
import meow.konc.hack.module.ModuleManager;
import meow.konc.hack.setting.Setting;
import meow.konc.hack.setting.Settings;
import meow.konc.hack.util.other.Friends;
import meow.konc.hack.util.other.LagCompensator;
import meow.konc.hack.util.render.BlockInteractionHelper;
import meow.konc.hack.util.render.KONCTessellator;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemAppleGold;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.potion.Potion;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Module.Info(name = "AtomCrystal", category = Module.Category.CRYSTAL)
public class AtomCrystal extends Module {
    private Setting<Boolean> explode = register(Settings.b("Explode"));
    private Setting<Boolean> autoTickDelay = register(Settings.b("Auto Tick Delay", false));
    private Setting<Double> waitTick = register(Settings.d("Tick Delay", 1.0));
    private Setting<Double> range = register(Settings.d("Hit Range", 5.0));
    private Setting<Double> walls = register(Settings.d("Walls Range", 3.5));
    private Setting<Boolean> antiWeakness = register(Settings.b("Anti Weakness", true));
    private Setting<Boolean> nodesync = register(Settings.b("No Desync", true));
    private Setting<Boolean> place = register(Settings.b("Place", true));
    private Setting<Boolean> explodeOwnOnly = register(Settings.b("ExplodeOwnOnly", false));
    private Setting<Boolean> autoSwitch = register(Settings.b("Auto Switch", true));
    private Setting<Boolean> noGappleSwitch = register(Settings.b("No Gap Switch", false));
    private Setting<Double> placeRange = register(Settings.d("Place Range", 5.0));
    private Setting<Double> minDmg = register(Settings.d("Min Damage", 5.0));
    private Setting<Double> facePlace = register(Settings.d("Faceplace HP", 6.0));
    private Setting<Boolean> raytrace = register(Settings.b("Ray Trace", false));
    private Setting<Boolean> rotate = register(Settings.b("Rotate", true));
    private Setting<Boolean> spoofRotations = register(Settings.b("Spoof Angles", true));
    private Setting<Double> maxSelfDmg = register(Settings.d("Max Self Dmg", 10.0));
    private Setting<Boolean> targetPlayers = register(Settings.b("Players", true));
    private Setting<Boolean> targetAnimals = register(Settings.b("Animals", false));
    private Setting<Boolean> targetMobs = register(Settings.b("Mobs", false));
    private BlockPos render;
    private Entity renderEnt;
    private boolean switchCooldown = false;
    private boolean isAttacking = false;
    private int oldSlot = -1;
    private int newSlot;
    private int waitCounter;
    EnumFacing f;
    private List<BlockPos> ownCrystalPositions = new ArrayList<BlockPos>();
    private boolean isActive;
    private static boolean isSpoofingAngles;
    private static double yaw;
    private static double pitch;
    @EventHandler
    private Listener<PacketEvent.Receive> packetListener = new Listener<PacketEvent.Receive>(event -> {
        if (event.getPacket() instanceof SPacketSoundEffect && nodesync.getValue()) {
            SPacketSoundEffect packet = (SPacketSoundEffect) event.getPacket();
            if (packet.getCategory() == SoundCategory.BLOCKS && packet.getSound() == SoundEvents.ENTITY_GENERIC_EXPLODE) {
                for (Entity e : (Minecraft.getMinecraft()).world.loadedEntityList) {
                    if (e != null && e instanceof EntityEnderCrystal && e.getDistance(packet.getX(), packet.getY(), packet.getZ()) <= 6.0D) {
                        e.setDead();
                    }
                }
            }
        }
    });
    private Listener<PacketEvent.Send> packetListener2 = new Listener<PacketEvent.Send>(event -> {
        if (spoofRotations.getValue()) {
            Packet packet = event.getPacket();
            if (packet instanceof CPacketPlayer && isSpoofingAngles) {
                ((CPacketPlayer) packet).yaw = (float) yaw;
                ((CPacketPlayer) packet).pitch = (float) pitch;
            }
        }
    }, (Predicate<PacketEvent.Send>[]) new Predicate[0]);

    @Override
    public void onUpdate() {
        isActive = false;
        if (mc.player == null || mc.player.isDead) {
            return;
        }
        if (ModuleManager.getModuleByName("Surround").isEnabled()) {
            return;
        }
        EntityEnderCrystal crystal = (EntityEnderCrystal) mc.world.loadedEntityList.stream().filter(entity -> entity instanceof EntityEnderCrystal).filter(e -> mc.player.getDistance(e) <= range.getValue()).map(entity -> entity).min(Comparator.comparing(c -> mc.player.getDistance(c))).orElse(null);
        Iterator<BlockPos> it = ownCrystalPositions.iterator();
        while (it.hasNext()) {
            BlockPos pos = it.next();
            boolean exists = mc.world.loadedEntityList.stream().filter(entity -> entity instanceof EntityEnderCrystal).anyMatch(entity -> entity.getPosition().equals((Object) pos));
            if (!exists) {
                it.remove();
            }
        }
        if (explode.getValue() && crystal != null && (!explodeOwnOnly.getValue() || ownCrystalPositions.contains(crystal.getPosition()))) {
            if (!mc.player.canEntityBeSeen((Entity) crystal) && mc.player.getDistance((Entity) crystal) > walls.getValue()) {
                return;
            }
            if (waitTick.getValue() > 0.0) {
                int waitValue = (int) (autoTickDelay.getValue() ? Math.ceil(20.0 - LagCompensator.INSTANCE.getTickRate()) : waitTick.getValue());
                if (waitCounter < waitValue) {
                    ++waitCounter;
                    return;
                }
                waitCounter = 0;
            }
            if (antiWeakness.getValue() && mc.player.isPotionActive(MobEffects.WEAKNESS)) {
                if (!isAttacking) {
                    oldSlot = mc.player.inventory.currentItem;
                    isAttacking = true;
                }
                newSlot = -1;
                for (int i = 0; i < 9; ++i) {
                    ItemStack stack = mc.player.inventory.getStackInSlot(i);
                    if (stack != ItemStack.EMPTY) {
                        if (stack.getItem() instanceof ItemSword) {
                            newSlot = i;
                            break;
                        }
                        if (stack.getItem() instanceof ItemTool) {
                            newSlot = i;
                            break;
                        }
                    }
                }
                if (newSlot != -1) {
                    mc.player.inventory.currentItem = newSlot;
                    switchCooldown = true;
                }
            }
            isActive = true;
            if (rotate.getValue()) {
                lookAtPacket(crystal.posX, crystal.posY, crystal.posZ, (EntityPlayer) mc.player);
            }
            mc.playerController.attackEntity((EntityPlayer) mc.player, (Entity) crystal);
            mc.player.swingArm(EnumHand.MAIN_HAND);
            isActive = false;
        } else {
            resetRotation();
            if (oldSlot != -1) {
                mc.player.inventory.currentItem = oldSlot;
                oldSlot = -1;
            }
            isAttacking = false;
            isActive = false;
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
            List<BlockPos> blocks = findCrystalBlocks();
            List<Entity> entities = new ArrayList<Entity>();
            if (targetPlayers.getValue()) {
                entities.addAll((Collection<? extends Entity>) mc.world.playerEntities.stream().filter(entityPlayer -> !Friends.isFriend(entityPlayer.getName())).sorted(Comparator.comparing(e -> mc.player.getDistance(e))).collect(Collectors.toList()));
            }
            if (targetAnimals.getValue()) {
                entities.addAll((Collection<? extends Entity>) mc.world.getLoadedEntityList().stream().filter(e -> e instanceof EntityAnimal).sorted(Comparator.comparing(e -> mc.player.getDistance(e))).collect(Collectors.toList()));
            }
            if (targetMobs.getValue()) {
                entities.addAll((Collection<? extends Entity>) mc.world.getLoadedEntityList().stream().filter(e -> e instanceof EntityMob).sorted(Comparator.comparing(e -> mc.player.getDistance(e))).collect(Collectors.toList()));
            }
            BlockPos q = null;
            double damage = 0.5;
            for (Entity entity2 : entities) {
                if (entity2 == mc.player) {
                    continue;
                }
                if (((EntityLivingBase) entity2).getHealth() <= 0.0f || entity2.isDead) {
                    continue;
                }
                if (mc.player == null) {
                    continue;
                }
                for (BlockPos blockPos : blocks) {
                    double b = entity2.getDistanceSq(blockPos);
                    if (b >= 169.0) {
                        continue;
                    }
                    double d = calculateDamage(blockPos.getX() + 0.5, blockPos.getY() + 1, blockPos.getZ() + 0.5, entity2);
                    if (d < minDmg.getValue() && ((EntityLivingBase) entity2).getHealth() + ((EntityLivingBase) entity2).getAbsorptionAmount() > facePlace.getValue()) {
                        continue;
                    }
                    if (d <= damage) {
                        continue;
                    }
                    double self = calculateDamage(blockPos.getX() + 0.5, blockPos.getY() + 1, blockPos.getZ() + 0.5, (Entity) mc.player);
                    if (self > d && d >= ((EntityLivingBase) entity2).getHealth()) {
                        continue;
                    }
                    if (self - 0.5 > mc.player.getHealth()) {
                        continue;
                    }
                    if (self > maxSelfDmg.getValue()) {
                        continue;
                    }
                    damage = d;
                    q = blockPos;
                    renderEnt = entity2;
                }
            }
            if (damage == 0.5) {
                render = null;
                renderEnt = null;
                resetRotation();
                return;
            }
            render = q;
            if (place.getValue()) {
                if (mc.player == null) {
                    return;
                }
                isActive = true;
                if (rotate.getValue()) {
                    lookAtPacket(q.getX() + 0.5, q.getY() - 0.5, q.getZ() + 0.5, (EntityPlayer) mc.player);
                }
                RayTraceResult result = mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ), new Vec3d(q.getX() + 0.5, q.getY() - 0.5, q.getZ() + 0.5));
                if (raytrace.getValue()) {
                    if (result == null || result.sideHit == null) {
                        q = null;
                        f = null;
                        render = null;
                        resetRotation();
                        isActive = false;
                        return;
                    }
                    f = result.sideHit;
                }
                if (!offhand && mc.player.inventory.currentItem != crystalSlot) {
                    if (autoSwitch.getValue()) {
                        if (noGappleSwitch.getValue() && isEatingGap()) {
                            isActive = false;
                            resetRotation();
                            return;
                        }
                        isActive = true;
                        mc.player.inventory.currentItem = crystalSlot;
                        resetRotation();
                        switchCooldown = true;
                    }
                    return;
                }
                if (switchCooldown) {
                    switchCooldown = false;
                    return;
                }
                if (q != null && mc.player != null) {
                    isActive = true;
                    if (raytrace.getValue() && f != null) {
                        mc.player.connection.sendPacket((Packet) new CPacketPlayerTryUseItemOnBlock(q, f, offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, 0.0f, 0.0f, 0.0f));
                        if (explodeOwnOnly.getValue()) {
                            ownCrystalPositions.add(q);
                        }
                    } else {
                        mc.player.connection.sendPacket((Packet) new CPacketPlayerTryUseItemOnBlock(q, EnumFacing.UP, offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, 0.0f, 0.0f, 0.0f));
                        if (explodeOwnOnly.getValue()) {
                            ownCrystalPositions.add(q.up());
                        }
                    }
                }
                isActive = false;
            }
        }
    }

    @Override
    public void onWorldRender(RenderEvent event) {
        Color c = new Color(255, 0, 0, 255);
        if (render != null && mc.player != null) {
            KONCTessellator.prepare(7);
            drawCurrentBlock(render, c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
            KONCTessellator.release();
        }
    }

    private boolean isEatingGap() {
        return mc.player.getHeldItemMainhand().getItem() instanceof ItemAppleGold && mc.player.isHandActive();
    }

    private void drawCurrentBlock(BlockPos render, int r, int g, int b, int a) {
        KONCTessellator.drawBox(render, r, g, b, a, 63);
    }

    private void lookAtPacket(double px, double py, double pz, EntityPlayer me) {
        double[] v = calculateLookAt(px, py, pz, me);
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
        NonNullList<BlockPos> positions = NonNullList.create();
        positions.addAll(BlockInteractionHelper.getSphere(getPlayerPos(), placeRange.getValue().floatValue(), placeRange.getValue().intValue(), false, true, 0).stream().filter(this::canPlaceCrystal).collect(Collectors.toList()));
        return positions;
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

    public static double[] calculateLookAt(double px, double py, double pz, EntityPlayer me) {
        double dirx = me.posX - px;
        double diry = me.posY - py;
        double dirz = me.posZ - pz;
        double len = Math.sqrt(dirx * dirx + diry * diry + dirz * dirz);
        dirx /= len;
        diry /= len;
        dirz /= len;
        double pitch = Math.asin(diry);
        double yaw = Math.atan2(dirz, dirx);
        pitch = pitch * 180.0 / 3.141592653589793;
        yaw = yaw * 180.0 / 3.141592653589793;
        yaw += 90.0;
        return new double[]{yaw, pitch};
    }

    @Override
    public void onEnable() {
        KONCMod.EVENT_BUS.subscribe(this);
        isActive = false;
    }

    @Override
    public void onDisable() {
        KONCMod.EVENT_BUS.unsubscribe(this);
        render = null;
        renderEnt = null;
        resetRotation();
        isActive = false;
    }
}*/
