/*package meow.konc.hack.modules.crystal;

import me.zero.alpine.listener.Listener;
import meow.konc.hack.event.events.other.PacketEvent;
import meow.konc.hack.event.events.other.RenderEvent;
import meow.konc.hack.module.Module;
import meow.konc.hack.module.ModuleManager;
import meow.konc.hack.modules.combat.AutoEZ;
import meow.konc.hack.setting.Setting;
import meow.konc.hack.setting.Settings;
import me.zero.alpine.listener.EventHandler;
import meow.konc.hack.util.other.Friends;
import meow.konc.hack.util.other.Wrapper;
import meow.konc.hack.util.render.KONCTessellator;
import meow.konc.hack.util.util.EntityUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Module.Info(name = "CandyCatCrystal", category = Module.Category.CRYSTAL)
public class CandyCatCrystal extends Module {

    private Setting<Double> range;
    private Setting<Boolean> antiWeakness;
    private Setting<Double> Pdelay;
    private Setting<Boolean> autoSwitch;
    private boolean animals;
    private boolean mobs;
    private Setting<Boolean> players;
    private Setting<Boolean> place;
    private Setting<Boolean> explode;
    private Setting<Boolean> thing;
    private Setting<Integer> Placements;
    private Setting<Double> Bdelay;
    private Setting<Integer> MinDmg;
    private Setting<Integer> MinLegDmg;
    private Setting<Double> Red;
    private Setting<Double> Green;
    private Setting<Double> Blue;
    private Setting<Double> Alpha;
    private BlockPos render;
    private Entity renderEnt;
    private Setting<Boolean> EntityIgnore;
    private long systemTime;
    private static boolean togglePitch;
    private boolean switchCooldown;
    private boolean isAttacking;
    private int oldSlot;
    private int newSlot;
    private Setting<Boolean> raytrace;
    private int placements;
    private static boolean isSpoofingAngles;
    private Setting<Boolean> pvpcrystalhit;
    private static double yaw;
    private static double pitch;
    private Setting<Boolean> nodesync;
    private boolean b2;
    private Setting<Boolean> spoofRotations;
    private Setting<Boolean> suicideprot;
    private Setting<Double> walls;
    private Setting<Boolean> wall;
    private int waitCounter;
    private int waitCounter2;
    private Setting<Boolean> betterbypass;
    private Setting<Integer> waitTick;
    private Setting<Integer> waitTick2;
    private Setting<Double> betterdistance;
    private Setting<Double> distance;
    private Setting<Integer> MaxSelfDmg;

    private double damage2;

    @EventHandler
    private Listener<PacketEvent.Receive> packetListener;
    private Listener<PacketEvent.Send> packetListener2;

    public CandyCatCrystal() {
        //general

        autoSwitch = register(Settings.b("Auto Switch"));
        players = register(Settings.b("Players"));
        mobs = false;
        animals = false;
        place = register(Settings.b("Place", true));
        explode = register(Settings.b("Explode", true));
        range = register(Settings.d("Range", 4.5));
        distance = register(Settings.d("Enemy Distance", 7.0));
        thing = register(Settings.b("MultiPlace", true));
        Placements = register(Settings.integerBuilder("Placements").withMinimum(1).withMaximum(8).withValue(2));
        //special
        EntityIgnore = register(Settings.b("Entity Ignore", false));
        nodesync = register(Settings.b("NoDesync", true));
        wall = register(Settings.b("Walls", true));
        walls = register(Settings.d("WallsRange", 3.0));
        antiWeakness = register(Settings.b("Anti Weakness", false));
        betterbypass = register(Settings.b("BetterPlacements", false));
        betterdistance = register(Settings.d("BetterPlacementsRange", 2.5));
        suicideprot = register(Settings.b("Suicide Protect", true));
        pvpcrystalhit = register(Settings.b("CrystalHit", true));
        spoofRotations = register(Settings.b("Spoof Rotations", false));
        //range Settings


        //delay settings

        waitTick = register(Settings.integerBuilder("Place DelayTick").withMinimum(0).withValue(3));
        waitTick2 = register(Settings.integerBuilder("Hit DelayTick").withMinimum(0).withValue(1));
        //color setting
        raytrace = register(Settings.b("RayTrace", false));
        Red = register(Settings.d("Red", 255.0));
        Green = register(Settings.d("Green", 255.0));
        Blue = register(Settings.d("Blue", 255.0));
        Alpha = register(Settings.d("Alpha", 75.0));
        //Dmg Settings

        MinDmg = register(Settings.integerBuilder("Min Dmg").withMinimum(0).withMaximum(16).withValue(2));
        MinLegDmg = register(Settings.integerBuilder("Min LegDmg").withMinimum(0).withMaximum(16).withValue(9));
        MaxSelfDmg = register(Settings.integerBuilder("Max Self").withMinimum(0).withMaximum(16).withValue(3));
        systemTime = -1L;
        damage2 = 0.5;
        switchCooldown = false;
        isAttacking = false;
        oldSlot = -1;

        packetListener = new Listener<PacketEvent.Receive>(event -> {

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
        packetListener2 = new Listener<PacketEvent.Send>(event -> {
            if (spoofRotations.getValue()) {
                Packet packet = event.getPacket();
                if (packet instanceof CPacketPlayer && isSpoofingAngles) {
                    ((CPacketPlayer) packet).yaw = (float) yaw;
                    ((CPacketPlayer) packet).pitch = (float) pitch;
                }
            }
        }, (Predicate<PacketEvent.Send>[]) new Predicate[0]);

    }

    @Override
    public void onUpdate() {
        if (mc.player == null) {
            return;
        }
        EntityEnderCrystal crystal = (EntityEnderCrystal) mc.world.loadedEntityList.stream().filter(entity -> entity instanceof EntityEnderCrystal).map(entity -> entity).min(Comparator.comparing(c -> mc.player.getDistance(c))).orElse(null);
        if (crystal != null) {
            double owo = calculateDamage(crystal, mc.player);
            if (owo - 0.5 > mc.player.getHealth() && suicideprot.getValue()) {
                crystal = null;
            }
        }

        if (explode.getValue() && crystal != null && mc.player.getDistance(crystal) <= range.getValue()) {
            if (!mc.player.canEntityBeSeen((Entity) crystal) && mc.player.getDistance((Entity) crystal) > walls.getValue() && wall.getValue()) {
            } else {
                if (waitTick2.getValue() > 0) {
                    if (waitCounter2 < waitTick.getValue()) {
                        ++waitCounter2;
                    } else {
                        waitCounter2 = 0;
                        if (antiWeakness.getValue() && mc.player.isPotionActive(MobEffects.WEAKNESS)) {
                            if (!isAttacking) {
                                oldSlot = Wrapper.getPlayer().inventory.currentItem;
                                isAttacking = true;
                            }
                            newSlot = -1;
                            for (int i = 0; i < 9; ++i) {
                                ItemStack stack = Wrapper.getPlayer().inventory.getStackInSlot(i);
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
                                Wrapper.getPlayer().inventory.currentItem = newSlot;
                                switchCooldown = true;
                            }
                        }
                        lookAtPacket(crystal.posX, crystal.posY, crystal.posZ, (EntityPlayer) mc.player);
                        mc.playerController.attackEntity((EntityPlayer) mc.player, (Entity) crystal);
                        mc.player.swingArm(EnumHand.MAIN_HAND);
                        systemTime = System.nanoTime() / 1000000L;

                        if (!thing.getValue()) {
                            return;
                        }
                        if (placements == Placements.getValue()) {
                            placements = 0;
                            return;
                        }
                    }
                }
            }

        } else {
            resetRotation();
            if (oldSlot != -1) {
                Wrapper.getPlayer().inventory.currentItem = oldSlot;
                oldSlot = -1;
            }
            isAttacking = false;
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
        List<BlockPos> blocks = findCrystalBlocks();
        List<Entity> entities = new ArrayList<Entity>();
        if (players.getValue()) {
            entities.addAll((Collection<? extends Entity>) mc.world.playerEntities.stream().filter(entityPlayer -> !Friends.isFriend(entityPlayer.getName())).collect(Collectors.toList()));
        }

        entities.addAll((Collection<? extends Entity>) mc.world.loadedEntityList.stream().filter(entity -> {
            if (EntityUtil.isLiving(entity)) {
                if (EntityUtil.isPassive(entity) ? animals : mobs) {
                    return b2;
                }
            }
            return b2;
        }).collect(Collectors.toList()));
        BlockPos q = null;
        BlockPos q2 = null;
        double damage = 0.5;
        for (Entity entity2 : entities) {
            if (entity2 != mc.player) {
                if (((EntityLivingBase) entity2).getHealth() <= 0.0f) {
                    continue;
                }
                for (BlockPos blockPos : blocks) {
                    double b = entity2.getDistanceSq(blockPos);
                    if (b >= distance.getValue() * distance.getValue()) {
                        continue;
                    }
                    if (betterbypass.getValue()) {
                        if (blockPos.y >= getPlayerPos().y + 3) {
                            continue;
                        }
                        if (blockPos.y >= getPlayerPos().y + 2) {
                            if ((blockPos.x) * (blockPos.z) >= (getPlayerPos().x + betterdistance.getValue()) * (getPlayerPos().z + betterdistance.getValue()) || (blockPos.x) * (blockPos.z) <= (getPlayerPos().x - betterdistance.getValue()) * (getPlayerPos().z - betterdistance.getValue())) {
                                continue;
                            }
                        }
                    }

                    double d = calculateDamage(blockPos.x + 0.5, blockPos.y + 1, blockPos.z + 0.5, entity2);
                    if (d <= damage) {
                        continue;
                    }


                    double self = calculateDamage(blockPos.x + 0.5, blockPos.y + 1, blockPos.z + 0.5, (Entity) mc.player);
                    if (self >= d && d <= ((EntityLivingBase) entity2).getHealth()) {
                        continue;
                    }
                    if (d < MinDmg.getValue()) {
                        continue;
                    }
                    if (d - 3 < MinLegDmg.getValue() && entity2.getPosition().y + 1 <= blockPos.y && entity2.getPosition().y + 3 >= blockPos.y) {
                        if (damage2 < d && self <= MaxSelfDmg.getValue() && d >= MinLegDmg.getValue()) {
                            q2 = blockPos;
                            damage2 = d;
                        }
                        continue;

                    }


                    if (self - 0.5 > mc.player.getHealth() && suicideprot.getValue()) {
                        continue;
                    }

                    if (self > MaxSelfDmg.getValue()) {
                        continue;
                    }

                    damage = d;
                    q = blockPos;
                    renderEnt = entity2;
                }
            }
        }
        if (damage < damage2) {
            damage = damage2;
            q = q2;
        }
        if (damage == 0.5) {
            render = null;
            renderEnt = null;
            resetRotation();
            return;
        }

        render = q;
        if (place.getValue()) {
            if (!offhand && mc.player.inventory.currentItem != crystalSlot) {
                if (autoSwitch.getValue()) {
                    mc.player.inventory.currentItem = crystalSlot;
                    resetRotation();
                    switchCooldown = true;
                }
                return;
            }
            if (ModuleManager.getModuleByName("AutoEZ").isEnabled()) {
                AutoEZ AutoEZ = (AutoEZ) ModuleManager.getModuleByName("AutoEZ");
                AutoEZ.addTargetedPlayer(renderEnt.getName());
            }
            lookAtPacket(q.x + 0.5, q.y - 0.5, q.z + 0.5, (EntityPlayer) mc.player);
            EnumFacing f;
            if (raytrace.getValue()) {
                RayTraceResult result = mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ), new Vec3d(q.x + 0.5, q.y - 0.5, q.z + 0.5));
                if (result == null || result.sideHit == null) {
                    f = EnumFacing.UP;
                } else {
                    f = result.sideHit;
                }
            } else {
                f = EnumFacing.DOWN;
            }
            if (switchCooldown) {
                switchCooldown = false;
                return;
            }
            if (waitTick.getValue() > 0) {
                if (waitCounter < waitTick.getValue()) {
                    ++waitCounter;
                    return;
                }
                waitCounter = 0;
            }

            mc.player.connection.sendPacket((Packet) new CPacketPlayerTryUseItemOnBlock(q, f, offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, 0.0f, 0.0f, 0.0f));
            ++placements;
            systemTime = System.nanoTime() / 1000000L;

        }
        if (isSpoofingAngles && spoofRotations.getValue()) {
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
            KONCTessellator.prepare(7);
            KONCTessellator.drawBox(render, Red.getValue().intValue(), Green.getValue().intValue(), Blue.getValue().intValue(), Alpha.getValue().intValue(), 63);
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
        if (EntityIgnore.getValue()) {
            return ((mc.world.getBlockState(blockPos).getBlock() == Blocks.BEDROCK || mc.world.getBlockState(blockPos).getBlock() == Blocks.OBSIDIAN) && mc.world.getBlockState(boost).getBlock() == Blocks.AIR && mc.world.getBlockState(boost2).getBlock() == Blocks.AIR);
        } else {
            return (mc.world.getBlockState(blockPos).getBlock() == Blocks.BEDROCK || mc.world.getBlockState(blockPos).getBlock() == Blocks.OBSIDIAN) && mc.world.getBlockState(boost).getBlock() == Blocks.AIR && mc.world.getBlockState(boost2).getBlock() == Blocks.AIR && mc.world.getEntitiesWithinAABB((Class) Entity.class, new AxisAlignedBB(boost)).isEmpty() && mc.world.getEntitiesWithinAABB((Class) Entity.class, new AxisAlignedBB(boost2)).isEmpty();
        }
    }

    public static BlockPos getPlayerPos() {
        return new BlockPos(Math.floor(mc.player.posX), Math.floor(mc.player.posY), Math.floor(mc.player.posZ));
    }


    private List<BlockPos> findCrystalBlocks() {
        NonNullList<BlockPos> positions = NonNullList.create();
        positions.addAll((Collection) getSphere(getPlayerPos(), range.getValue().floatValue(), range.getValue().intValue(), false, true, 0).stream().filter(this::canPlaceCrystal).collect(Collectors.toList()));
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
            damage = Math.max(damage, 0.0f);
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

    @Override
    protected void onEnable() {
        if (pvpcrystalhit.getValue()) {
            ModuleManager.getModuleByName("AutoCrystal").enable();
        }
    }

    @Override
    public void onDisable() {
        render = null;
        renderEnt = null;
        resetRotation();
        if (pvpcrystalhit.getValue()) {
            ModuleManager.getModuleByName("AutoCrystal").disable();
        }
    }

    static {
        togglePitch = false;
    }
}*/
