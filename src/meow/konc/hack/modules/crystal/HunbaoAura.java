/*package meow.konc.hack.modules.crystal;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import meow.konc.hack.KONCMod;
import meow.konc.hack.command.Command;
import meow.konc.hack.event.events.other.PacketEvent;
import meow.konc.hack.event.events.other.RenderEvent;
import meow.konc.hack.module.Module;
import meow.konc.hack.module.ModuleManager;
import meow.konc.hack.setting.Setting;
import meow.konc.hack.setting.Settings;
import meow.konc.hack.util.other.Friends;
import meow.konc.hack.util.other.LagCompensator;
import meow.konc.hack.util.render.KONCTessellator;
import meow.konc.hack.util.util.EntityUtil;
import meow.konc.hack.util.util.MathUtil;
import net.minecraft.block.state.IBlockState;
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

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Module.Info(name = "HunbaoAura", category = Module.Category.CRYSTAL)
public class HunbaoAura extends Module {


    private Setting<Boolean> explode = register(Settings.b("Explode"));
    private Setting<Boolean> autoTickDelay = register(Settings.b("2B2T Hit Delay", false));
    private Setting<Double> waitTick = register(Settings.d("Hit Delay", 1.0));
    private Setting<Double> range = register(Settings.d("Hit Range", 5.0));
    private Setting<Double> walls = register(Settings.d("Walls Range", 3.5));
    private Setting<Boolean> antiWeakness = register(Settings.b("Anti Weakness", true));
    private Setting<Boolean> announceUsage = register(Settings.b("Announce Usage", true));
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
    private Setting<RenderMode> mode = register(Settings.e("Render Mode", RenderMode.SOLID));
    private Setting<Boolean> renderplace = register(Settings.b("Render", false));
    private Setting<Integer> red = register(Settings.integerBuilder("Red").withRange(0, 255).withValue(255).withVisibility(b -> renderplace.getValue()).build());
    private Setting<Integer> green = register(Settings.integerBuilder("Green").withRange(0, 255).withValue(255).withVisibility(b -> renderplace.getValue()).build());
    private Setting<Integer> blue = register(Settings.integerBuilder("Blue").withRange(0, 255).withValue(255).withVisibility(b -> renderplace.getValue()).build());
    private Setting<Integer> alpha = register(Settings.integerBuilder("Alpha").withRange(0, 255).withValue(70).withVisibility(b -> renderplace.getValue()).build());
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
    private Listener<PacketEvent.Send> packetSendListener = new Listener<PacketEvent.Send>(event -> {
        Packet packet = event.getPacket();
        if (packet instanceof CPacketPlayer && spoofRotations.getValue().booleanValue() && isSpoofingAngles) {
            ((CPacketPlayer) packet).yaw = (float) yaw;
            ((CPacketPlayer) packet).pitch = (float) pitch;
        }
    }, new Predicate[0]);
    @EventHandler
    private Listener<PacketEvent.Receive> packetReceiveListener = new Listener<PacketEvent.Receive>(event -> {
        SPacketSoundEffect packet;
        if (event.getPacket() instanceof SPacketSoundEffect && nodesync.getValue().booleanValue() && (packet = (SPacketSoundEffect) event.getPacket()).getCategory() == SoundCategory.BLOCKS && packet.getSound() == SoundEvents.ENTITY_GENERIC_EXPLODE) {
            for (Entity e : Minecraft.getMinecraft().world.loadedEntityList) {
                if (!(e instanceof EntityEnderCrystal) || !(e.getDistance(packet.getX(), packet.getY(), packet.getZ()) <= 6.0)) {
                    continue;
                }
                e.setDead();
            }
        }
    }, new Predicate[0]);

    @Override
    public void onUpdate() {
        int crystalSlot;
        isActive = false;
        if (mc.player == null || mc.player.isDead) {
            return;
        }
        if (ModuleManager.getModuleByName("Surround").isEnabled()) {
            return;
        }
        EntityEnderCrystal crystal = mc.world.loadedEntityList.stream().filter(entity -> entity instanceof EntityEnderCrystal).filter(e -> (double) mc.player.getDistance(e) <= range.getValue()).map(entity -> (EntityEnderCrystal) entity).min(Comparator.comparing(c -> Float.valueOf(mc.player.getDistance((Entity) c)))).orElse(null);
        Iterator<BlockPos> it = ownCrystalPositions.iterator();
        while (it.hasNext()) {
            BlockPos pos = it.next();
            boolean exists = mc.world.loadedEntityList.stream().filter(entity -> entity instanceof EntityEnderCrystal).anyMatch(entity -> entity.getPosition().equals((Object) pos));
            if (exists) {
                continue;
            }
            it.remove();
        }
        if (explode.getValue().booleanValue() && crystal != null && (!explodeOwnOnly.getValue().booleanValue() || ownCrystalPositions.contains((Object) crystal.getPosition()))) {
            if (!mc.player.canEntityBeSeen((Entity) crystal) && (double) mc.player.getDistance((Entity) crystal) > walls.getValue()) {
                return;
            }
            if (waitTick.getValue() > 0.0) {
                int waitValue = (int) (autoTickDelay.getValue() != false ? Math.ceil(20.0 - (double) LagCompensator.INSTANCE.getTickRate()) : waitTick.getValue());
                if (waitCounter < waitValue) {
                    ++waitCounter;
                    return;
                }
                waitCounter = 0;
            }
            if (antiWeakness.getValue().booleanValue() && mc.player.isPotionActive(MobEffects.WEAKNESS)) {
                if (!isAttacking) {
                    oldSlot = mc.player.inventory.currentItem;
                    isAttacking = true;
                }
                newSlot = -1;
                for (int i = 0; i < 9; ++i) {
                    ItemStack stack = mc.player.inventory.getStackInSlot(i);
                    if (stack == ItemStack.EMPTY) {
                        continue;
                    }
                    if (stack.getItem() instanceof ItemSword) {
                        newSlot = i;
                        break;
                    }
                    if (!(stack.getItem() instanceof ItemTool)) {
                        continue;
                    }
                    newSlot = i;
                    break;
                }
                if (newSlot != -1) {
                    mc.player.inventory.currentItem = newSlot;
                    switchCooldown = true;
                }
            }
            isActive = true;
            if (rotate.getValue().booleanValue()) {
                lookAtPacket(crystal.posX, crystal.posY, crystal.posZ, (EntityPlayer) mc.player);
            }
            mc.playerController.attackEntity((EntityPlayer) mc.player, (Entity) crystal);
            mc.player.swingArm(EnumHand.MAIN_HAND);
            isActive = false;
            return;
        }
        resetRotation();
        if (oldSlot != -1) {
            mc.player.inventory.currentItem = oldSlot;
            oldSlot = -1;
        }
        isAttacking = false;
        isActive = false;
        int n = crystalSlot = mc.player.getHeldItemMainhand().getItem() == Items.END_CRYSTAL ? mc.player.inventory.currentItem : -1;
        if (crystalSlot == -1) {
            for (int l = 0; l < 9; ++l) {
                if (mc.player.inventory.getStackInSlot(l).getItem() != Items.END_CRYSTAL) {
                    continue;
                }
                crystalSlot = l;
                break;
            }
        }
        boolean offhand = false;
        if (mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL) {
            offhand = true;
        } else if (crystalSlot == -1) {
            return;
        }
        List<BlockPos> blocks = findCrystalBlocks();
        ArrayList<Entity> entities = new ArrayList<>();
        if (targetPlayers.getValue().booleanValue()) {
            entities.addAll(mc.world.playerEntities.stream().filter(entityPlayer -> !Friends.isFriend(entityPlayer.getName())).sorted(Comparator.comparing(e -> Float.valueOf(mc.player.getDistance((Entity) e)))).collect(Collectors.toList()));
        }
        if (targetAnimals.getValue().booleanValue()) {
            entities.addAll(mc.world.getLoadedEntityList().stream().filter(e -> e instanceof EntityAnimal).sorted(Comparator.comparing(e -> Float.valueOf(mc.player.getDistance(e)))).collect(Collectors.toList()));
        }
        if (targetMobs.getValue().booleanValue()) {
            entities.addAll(mc.world.getLoadedEntityList().stream().filter(e -> e instanceof EntityMob).sorted(Comparator.comparing(e -> Float.valueOf(mc.player.getDistance(e)))).collect(Collectors.toList()));
        }
        BlockPos q = null;
        double damage = 0.5;
        for (Entity entity2 : entities) {
            if (entity2 == mc.player || ((EntityLivingBase) entity2).getHealth() <= 0.0f || entity2.isDead || mc.player == null) {
                continue;
            }
            for (BlockPos blockPos : blocks) {
                double self;
                double d;
                double b = entity2.getDistanceSq(blockPos);
                if (b >= 169.0 || (d = (double) calculateDamage((double) blockPos.getX() + 0.5, blockPos.getY() + 1, (double) blockPos.getZ() + 0.5, entity2)) < minDmg.getValue() && (double) (((EntityLivingBase) entity2).getHealth() + ((EntityLivingBase) entity2).getAbsorptionAmount()) > facePlace.getValue() || !(d > damage) || (self = (double) calculateDamage((double) blockPos.getX() + 0.5, blockPos.getY() + 1, (double) blockPos.getZ() + 0.5, (Entity) mc.player)) > d && !(d < (double) ((EntityLivingBase) entity2).getHealth()) || self - 0.5 > (double) mc.player.getHealth() || self > maxSelfDmg.getValue()) {
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
        if (place.getValue().booleanValue()) {
            if (mc.player == null) {
                return;
            }
            isActive = true;
            if (rotate.getValue().booleanValue()) {
                lookAtPacket((double) q.getX() + 0.5, (double) q.getY() - 0.5, (double) q.getZ() + 0.5, (EntityPlayer) mc.player);
            }
            RayTraceResult result = mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + (double) mc.player.getEyeHeight(), mc.player.posZ), new Vec3d((double) q.getX() + 0.5, (double) q.getY() - 0.5, (double) q.getZ() + 0.5));
            if (raytrace.getValue().booleanValue()) {
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
                if (autoSwitch.getValue().booleanValue()) {
                    if (noGappleSwitch.getValue().booleanValue() && isEatingGap()) {
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
                if (raytrace.getValue().booleanValue() && f != null) {
                    mc.player.connection.sendPacket((Packet) new CPacketPlayerTryUseItemOnBlock(q, f, offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, 0.0f, 0.0f, 0.0f));
                    if (explodeOwnOnly.getValue().booleanValue()) {
                        ownCrystalPositions.add(q);
                    }
                } else {
                    mc.player.connection.sendPacket((Packet) new CPacketPlayerTryUseItemOnBlock(q, EnumFacing.UP, offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, 0.0f, 0.0f, 0.0f));
                    if (explodeOwnOnly.getValue().booleanValue()) {
                        ownCrystalPositions.add(q.up());
                    }
                }
            }
            isActive = false;
        }
    }

    @Override
    public void onWorldRender(RenderEvent event) {
        if (render != null) {
            switch (mode.getValue()) {
                case SOLID: {
                    KONCTessellator.prepare(7);
                    KONCTessellator.drawBox(render, red.getValue(), green.getValue(), blue.getValue(), alpha.getValue(), 63);
                    KONCTessellator.release();
                    break;
                }
                case OUTLINE: {
                    IBlockState iBlockState2 = mc.world.getBlockState(render);
                    Vec3d interp2 = MathUtil.interpolateEntity((Entity) mc.player, mc.getRenderPartialTicks());
                    KONCTessellator.drawBoundingBox(iBlockState2.getSelectedBoundingBox((World) mc.world, render).grow(0.0020000000949949026).offset(-interp2.x, -interp2.y, -interp2.z), 1.5f, red.getValue(), green.getValue(), blue.getValue(), alpha.getValue());
                    break;
                }
                case SOLIDFLAT: {
                    KONCTessellator.prepare(7);
                    KONCTessellator.drawFace(render, red.getValue(), green.getValue(), blue.getValue(), alpha.getValue(), 63);
                    KONCTessellator.release();
                    break;
                }
                case FULL: {
                    IBlockState iBlockState3 = mc.world.getBlockState(render);
                    Vec3d interp3 = MathUtil.interpolateEntity((Entity) mc.player, mc.getRenderPartialTicks());
                    KONCTessellator.drawFullBox(iBlockState3.getSelectedBoundingBox((World) mc.world, render).grow(0.0020000000949949026).offset(-interp3.x, -interp3.y, -interp3.z), render, 1.5f, red.getValue(), green.getValue(), blue.getValue(), alpha.getValue());
                    break;
                }
            }
            if (renderEnt != null) {
                EntityUtil.getInterpolatedRenderPos(renderEnt, mc.getRenderPartialTicks());
            }
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
        return (mc.world.getBlockState(blockPos).getBlock() == Blocks.BEDROCK || mc.world.getBlockState(blockPos).getBlock() == Blocks.OBSIDIAN) && mc.world.getBlockState(boost).getBlock() == Blocks.AIR && mc.world.getBlockState(boost2).getBlock() == Blocks.AIR && mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost)).isEmpty() && mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost2)).isEmpty();
    }

    public static BlockPos getPlayerPos() {
        return new BlockPos(Math.floor(mc.player.posX), Math.floor(mc.player.posY), Math.floor(mc.player.posZ));
    }

    private List<BlockPos> findCrystalBlocks() {
        NonNullList positions = NonNullList.create();
        double range = placeRange.getValue();
        positions.addAll((Collection) getSphere(getPlayerPos(), (float) range, (int) range, false, true, 0).stream().filter(this::canPlaceCrystal).collect(Collectors.toList()));
        return positions;
    }

    public List<BlockPos> getSphere(BlockPos loc, float r, int h, boolean hollow, boolean sphere, int plus_y) {
        ArrayList<BlockPos> circleblocks = new ArrayList<BlockPos>();
        int cx = loc.getX();
        int cy = loc.getY();
        int cz = loc.getZ();
        int x = cx - (int) r;
        while ((float) x <= (float) cx + r) {
            int z = cz - (int) r;
            while ((float) z <= (float) cz + r) {
                int y = sphere ? cy - (int) r : cy;
                do {
                    float f = sphere ? (float) cy + r : (float) (cy + h);
                    if (!((float) y < f)) {
                        break;
                    }
                    double dist = (cx - x) * (cx - x) + (cz - z) * (cz - z) + (sphere ? (cy - y) * (cy - y) : 0);
                    if (!(!(dist < (double) (r * r)) || hollow && dist < (double) ((r - 1.0f) * (r - 1.0f)))) {
                        BlockPos l = new BlockPos(x, y + plus_y, z);
                        circleblocks.add(l);
                    }
                    ++y;
                } while (true);
                ++z;
            }
            ++x;
        }
        return circleblocks;
    }

    public static float calculateDamage(double posX, double posY, double posZ, Entity entity) {
        float doubleExplosionSize = 12.0f;
        double distancedsize = entity.getDistance(posX, posY, posZ) / (double) doubleExplosionSize;
        Vec3d vec3d = new Vec3d(posX, posY, posZ);
        double blockDensity = entity.world.getBlockDensity(vec3d, entity.getEntityBoundingBox());
        double v = (1.0 - distancedsize) * blockDensity;
        float damage = (int) ((v * v + v) / 2.0 * 7.0 * (double) doubleExplosionSize + 1.0);
        double finald = 1.0;
        if (entity instanceof EntityLivingBase) {
            finald = getBlastReduction((EntityLivingBase) entity, getDamageMultiplied(damage), new Explosion((World) mc.world, null, posX, posY, posZ, 6.0f, false, true));
        }
        return (float) finald;
    }

    public static float getBlastReduction(EntityLivingBase entity, float damage, Explosion explosion) {
        if (entity instanceof EntityPlayer) {
            EntityPlayer ep = (EntityPlayer) entity;
            DamageSource ds = DamageSource.causeExplosionDamage((Explosion) explosion);
            damage = CombatRules.getDamageAfterAbsorb((float) damage, (float) ep.getTotalArmorValue(), (float) ((float) ep.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue()));
            int k = EnchantmentHelper.getEnchantmentModifierDamage((Iterable) ep.getArmorInventoryList(), (DamageSource) ds);
            float f = MathHelper.clamp((float) k, (float) 0.0f, (float) 20.0f);
            damage *= 1.0f - f / 25.0f;
            if (entity.isPotionActive(Potion.getPotionById((int) 11))) {
                damage -= damage / 4.0f;
            }
            return damage;
        }
        damage = CombatRules.getDamageAfterAbsorb((float) damage, (float) entity.getTotalArmorValue(), (float) ((float) entity.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue()));
        return damage;
    }

    private static float getDamageMultiplied(float damage) {
        int diff = mc.world.getDifficulty().getId();
        return damage * (diff == 0 ? 0.0f : (diff == 2 ? 1.0f : (diff == 1 ? 0.5f : 1.5f)));
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
        double pitch = Math.asin(diry /= len);
        double yaw = Math.atan2(dirz /= len, dirx /= len);
        pitch = pitch * 180.0 / 3.141592653589793;
        yaw = yaw * 180.0 / 3.141592653589793;
        return new double[]{yaw += 90.0, pitch};
    }

    @Override
    public void onEnable() {
        KONCMod.EVENT_BUS.subscribe(this);
        isActive = false;
        if (announceUsage.getValue()) {
            Command.sendChatMessage("[HunbaoAura] " + ChatFormatting.GREEN.toString() + "Enabled!");
        }
    }

    @Override
    public void onDisable() {
        KONCMod.EVENT_BUS.unsubscribe(this);
        render = null;
        renderEnt = null;
        resetRotation();
        isActive = false;
        if (announceUsage.getValue()) {
            Command.sendChatMessage("[HunbaoAura] " + ChatFormatting.RED.toString() + "Disabled!");
        }
    }

    private enum RenderMode {
        SOLID,
        OUTLINE,
        FULL,
        SOLIDFLAT;
    }
}*/

