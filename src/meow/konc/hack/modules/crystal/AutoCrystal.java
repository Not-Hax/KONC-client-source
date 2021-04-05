/*package meow.konc.hack.modules.crystal;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.zero.alpine.listener.Listener;
import meow.konc.hack.command.Command;
import meow.konc.hack.event.events.other.PacketEvent;
import meow.konc.hack.event.events.other.RenderEvent;
import meow.konc.hack.module.Module;
import meow.konc.hack.module.ModuleManager;
import meow.konc.hack.modules.combat.AutoEZ;
import meow.konc.hack.setting.Setting;
import meow.konc.hack.setting.Settings;
import me.zero.alpine.listener.EventHandler;
import meow.konc.hack.util.other.Friends;
import meow.konc.hack.util.render.KONCTessellator;
import meow.konc.hack.util.util.EntityUtil;
import meow.konc.hack.util.util.MathUtil;
import net.minecraft.block.state.IBlockState;
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
import net.minecraft.init.SoundEvents;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.potion.Potion;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.Explosion;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Module.Info(name = "AutoCrystal", category = Module.Category.CRYSTAL)
public class AutoCrystal extends Module {
    public Setting backToDefault = register(Settings.b("Default", false));

    private Setting<Boolean> place = register(Settings.b("Place", true));
    private Setting<Boolean> multiPlace = register(Settings.booleanBuilder("Multi Place").withValue(false).withVisibility(b -> place.getValue()).build());
    private Setting<Integer> multiPlaceSpeed = register(Settings.integerBuilder("Multi Place Speed").withMinimum(1).withMaximum(10).withValue(4).withVisibility(b -> place.getValue()).build());

    private Setting<Boolean> Break = register(Settings.b("Break", true));
    private Setting<Integer> attackSpeed = register(Settings.integerBuilder("Attack Speed").withMinimum(0).withMaximum(20).withValue(14).withVisibility(b -> Break.getValue()).build());
    private Setting<Integer> placeDelay = register(Settings.integerBuilder("Place Delay").withMinimum(0).withMaximum(50).withValue(2).withVisibility(b -> Break.getValue()).build());

    private Setting<Boolean> Range = register(Settings.b("Range", false));
    private Setting<Integer> placeRange = register(Settings.integerBuilder("Place Range").withMinimum(1).withMaximum(6).withValue(4).withVisibility(b -> Range.getValue()).build());
    private Setting<Integer> breakRange = register(Settings.integerBuilder("Break Range").withMinimum(1).withMaximum(6).withValue(4).withVisibility(b -> Range.getValue()).build());
    private Setting<Integer> enemyRange = register(Settings.integerBuilder("Enemy Range").withMinimum(1).withMaximum(13).withValue(9).withVisibility(b -> Range.getValue()).build());

    private Setting<Boolean> Dmg = register(Settings.b("Dmg", false));
    private Setting<Integer> minDamage = register(Settings.integerBuilder("Min Damage").withMinimum(0).withMaximum(16).withValue(4).withVisibility(b -> Dmg.getValue()).build());
    private Setting<Integer> facePlace = register(Settings.integerBuilder("Face Place").withMinimum(0).withMaximum(16).withValue(7).withVisibility(b -> Dmg.getValue()).build());

    private Setting<Boolean> general = register(Settings.b("General", false));
    private Setting<Boolean> autoOffhand = register(Settings.booleanBuilder("Auto Offhand Crystal").withValue(false).withVisibility(b -> general.getValue()).build());
    private Setting<Boolean> LegCrystals = register(Settings.booleanBuilder("Auto LegCrystals").withValue(false).withVisibility(b -> general.getValue()).build());
    private Setting<Boolean> raytrace = register(Settings.booleanBuilder("Ray Trace").withValue(false).withVisibility(b -> general.getValue()).build());
    private Setting<Boolean> autoSwitch = register(Settings.booleanBuilder("Auto Switch").withValue(true).withVisibility(b -> general.getValue()).build());
    private Setting<Boolean> antiStuck = register(Settings.booleanBuilder("Anti Stuck").withValue(true).withVisibility(b -> general.getValue()).build());
    private Setting<Boolean> alert = register(Settings.booleanBuilder("Chat Alerts").withValue(true).withVisibility(b -> general.getValue()).build());
    private Setting<Boolean> antiSui = register(Settings.booleanBuilder("Anti Suicide").withValue(true).withVisibility(b -> general.getValue()).build());
    private Setting<Boolean> nodesync = register(Settings.booleanBuilder("No Desync").withValue(true).withVisibility(b -> general.getValue()).build());
    private Setting<Boolean> sneakEnable = register(Settings.booleanBuilder("Sneak Surround").withValue(false).withVisibility(b -> general.getValue()).build());
    private Setting<Boolean> rotate = register(Settings.booleanBuilder("Rotate").withValue(true).withVisibility(b -> general.getValue()).build());
    private Setting<Boolean> EntityIgnore = register(Settings.booleanBuilder("Entity Ignore").withValue(false).withVisibility(b -> general.getValue()).build());

    private Setting<Boolean> renderplace = register(Settings.b("RGB", false));
    private Setting<RenderMode> mode = register(Settings.enumBuilder(RenderMode.class).withName("Render Mode").withValue(RenderMode.SOLID).withVisibility(b -> renderplace.getValue()).build());
    private Setting<Integer> red = register(Settings.integerBuilder("Red").withRange(0, 255).withValue(255).withVisibility(b -> renderplace.getValue()).build());
    private Setting<Integer> green = register(Settings.integerBuilder("Green").withRange(0, 255).withValue(65).withVisibility(b -> renderplace.getValue()).build());
    private Setting<Integer> blue = register(Settings.integerBuilder("Blue").withRange(0, 255).withValue(40).withVisibility(b -> renderplace.getValue()).build());
    private Setting<Integer> alpha = register(Settings.integerBuilder("Alpha").withRange(0, 255).withValue(65).withVisibility(b -> renderplace.getValue()).build());

    private EntityPlayer target;
    private BlockPos render;
    private Entity renderEnt;

    private static boolean isSpoofingAngles;
    private static boolean togglePitch;
    private static double yaw;
    private static double pitch;

    private long multiPlaceSystemTime = -1L;
    private long antiStuckSystemTime = -1L;
    private long placeSystemTime = -1L;
    private long breakSystemTime = -1L;

    private boolean switchCooldown = false;

    int holeBlocks = 0;

    @EventHandler
    private Listener<PacketEvent.Send> packetListener2 = new Listener<PacketEvent.Send>(event -> {
        Packet[] packet = new Packet[1];
        ;
        packet[0] = event.getPacket();
        if (packet[0] instanceof CPacketPlayer && isSpoofingAngles) {
            ((CPacketPlayer) packet[0]).yaw = (float) yaw;
            ((CPacketPlayer) packet[0]).pitch = (float) pitch;
        }
    }, new Predicate[0]);
    private Listener<PacketEvent.Receive> packetListener = new Listener<PacketEvent.Receive>(event -> {
        if (event.getPacket() instanceof SPacketSoundEffect && nodesync.getValue()) {
            SPacketSoundEffect packet = (SPacketSoundEffect) event.getPacket();
            if (packet.getCategory() == SoundCategory.BLOCKS && packet.getSound() == SoundEvents.ENTITY_GENERIC_EXPLODE) {
                Iterator var3 = Minecraft.getMinecraft().world.loadedEntityList.iterator();
                while (var3.hasNext()) {
                    Entity e = (Entity) var3.next();
                    if (e instanceof EntityEnderCrystal && e.getDistance(packet.getX(), packet.getY(), packet.getZ()) <= 6.0D) {
                        e.setDead();
                    }
                }
            }
        }
    }, new Predicate[0]);

    @Override
    public void onUpdate() {
        if ((Boolean) backToDefault.getValue()) {
            backToDefault.setValue(false);

            place.setValue(true);
            multiPlace.setValue(false);
            multiPlaceSpeed.setValue(4);

            Break.setValue(true);
            attackSpeed.setValue(14);
            placeDelay.setValue(2);

            Range.setValue(false);
            placeRange.setValue(4);
            breakRange.setValue(4);
            enemyRange.setValue(9);

            Dmg.setValue(false);
            minDamage.setValue(4);
            facePlace.setValue(7);

            general.setValue(false);
            autoOffhand.setValue(false);
            LegCrystals.setValue(false);
            raytrace.setValue(true);
            autoSwitch.setValue(true);
            antiStuck.setValue(true);
            alert.setValue(true);
            antiSui.setValue(true);
            nodesync.setValue(true);
            sneakEnable.setValue(false);
            rotate.setValue(true);

            renderplace.setValue(false);
            mode.setValue(RenderMode.SOLID);
            red.setValue(255);
            green.setValue(65);
            blue.setValue(40);
            alpha.setValue(65);
        }
        EntityEnderCrystal crystal = (EntityEnderCrystal) mc.world.loadedEntityList.stream().filter(entity -> entity instanceof EntityEnderCrystal).min(Comparator.comparing(c -> mc.player.getDistance(c))).orElse(null);
        if (crystal != null && mc.player.getDistance(crystal) <= breakRange.getValue()) {
            if (System.nanoTime() / 1000000L - breakSystemTime >= 420 - attackSpeed.getValue() * 20) {
                if (rotate.getValue()) {
                    lookAtPacket(crystal.posX, crystal.posY, crystal.posZ, mc.player);
                }
                mc.playerController.attackEntity(mc.player, crystal);
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
        Entity lastTarget = null;

        BlockPos finalPos = null;
        List<BlockPos> blocks = findCrystalBlocks();
        List<Entity> entities = new ArrayList<Entity>();
        entities.addAll(mc.world.playerEntities.stream().filter(entityPlayer -> !Friends.isFriend(entityPlayer.getName())).collect(Collectors.toList()));
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
                    double self = calculateDamage(blockPos.x + 0.5, blockPos.y + 1, blockPos.z + 0.5, mc.player);
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
                    lastTarget = entity2;

                }
            }
        }
        if (damage == 0.5) {
            render = null;
            resetRotation();
            return;
        }
        if (sneakEnable.getValue() && mc.player.isSneaking() && holeBlocks != 5) {
            ModuleManager.getModuleByName("Surround").enable();
        }
        if (lastTarget instanceof EntityPlayer && ModuleManager.getModuleByName("AutoEZ").isEnabled()) {
            AutoEZ AutoEZ = (AutoEZ) ModuleManager.getModuleByName("AutoEZ");
            AutoEZ.addTargetedPlayer(lastTarget.getName());
        }
        render = finalPos;

        if (place.getValue()) {
            if (!offhand && mc.player.inventory.currentItem != crystalSlot) {
                if (autoSwitch.getValue()) {
                    mc.player.inventory.currentItem = crystalSlot;
                    resetRotation();
                    switchCooldown = true;
                }
                return;
            }
            if (rotate.getValue()) {
                lookAtPacket(finalPos.x + 0.5, finalPos.y - 0.5, finalPos.z + 0.5, mc.player);
            }
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
                mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(finalPos, f, offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, 0.0f, 0.0f, 0.0f));
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
            switch (mode.getValue()) {
                case SOLID: {
                    KONCTessellator.prepare(7);
                    KONCTessellator.drawBox(render, red.getValue(), green.getValue(), blue.getValue(), alpha.getValue(), 63);
                    KONCTessellator.release();
                    break;
                }
                case OUTLINE: {
                    IBlockState iBlockState2 = mc.world.getBlockState(render);
                    Vec3d interp2 = MathUtil.interpolateEntity(mc.player, mc.getRenderPartialTicks());
                    KONCTessellator.drawBoundingBox(iBlockState2.getSelectedBoundingBox(mc.world, render).grow(0.0020000000949949026).offset(-interp2.x, -interp2.y, -interp2.z), 1.5f, red.getValue(), green.getValue(), blue.getValue(), alpha.getValue());
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
                    Vec3d interp3 = MathUtil.interpolateEntity(mc.player, mc.getRenderPartialTicks());
                    KONCTessellator.drawFullBox(iBlockState3.getSelectedBoundingBox(mc.world, render).grow(0.0020000000949949026).offset(-interp3.x, -interp3.y, -interp3.z), render, 1.5f, red.getValue(), green.getValue(), blue.getValue(), alpha.getValue());
                    break;
                }
            }
        }
        if (renderEnt != null) {
            EntityUtil.getInterpolatedRenderPos(renderEnt, mc.getRenderPartialTicks());
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
            return ((mc.world.getBlockState(blockPos).getBlock() == Blocks.BEDROCK
                    || mc.world.getBlockState(blockPos).getBlock() == Blocks.OBSIDIAN)
                    && mc.world.getBlockState(boost).getBlock() == Blocks.AIR
                    && mc.world.getBlockState(boost2).getBlock() == Blocks.AIR);
        } else {
            return (mc.world.getBlockState(blockPos).getBlock() == Blocks.BEDROCK || mc.world.getBlockState(blockPos).getBlock() == Blocks.OBSIDIAN) && mc.world.getBlockState(boost).getBlock() == Blocks.AIR && mc.world.getBlockState(boost2).getBlock() == Blocks.AIR && mc.world.getEntitiesWithinAABB((Class) Entity.class, new AxisAlignedBB(boost)).isEmpty() && mc.world.getEntitiesWithinAABB((Class) Entity.class, new AxisAlignedBB(boost2)).isEmpty();
        }
    }

    public static BlockPos getPlayerPos() {
        return new BlockPos(Math.floor(mc.player.posX), Math.floor(mc.player.posY), Math.floor(mc.player.posZ));
    }

    private List<BlockPos> findCrystalBlocks() {
        NonNullList positions = NonNullList.create();
        positions.addAll(getSphere(getPlayerPos(), placeRange.getValue().floatValue(), placeRange.getValue(), false, true, 0).stream().filter(this::canPlaceCrystal).collect(Collectors.toList()));
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
            finald = getBlastReduction((EntityLivingBase) entity, getDamageMultiplied(damage), new Explosion(mc.world, null, posX, posY, posZ, 6.0f, false, true));
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
        return mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ), new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ()), false, true, false) == null;
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
            Command.sendChatMessage("[AutoCrystal] " + ChatFormatting.GREEN.toString() + "Enabled!");
        }
        if (autoOffhand.getValue()) {
            ModuleManager.getModuleByName("AutoOffhandCrystal").enable();
        }
        if (LegCrystals.getValue()) {
            ModuleManager.getModuleByName("LegCrystals").enable();
        }
        if (autoOffhand.getValue()) {
            ModuleManager.getModuleByName("AutoTotem2").enable();
        }
    }

    @Override
    public void onDisable() {
        if (alert.getValue() && mc.world != null) {
            Command.sendChatMessage("[AutoCrystal] " + ChatFormatting.RED.toString() + "Disabled!");
        }
        if (autoOffhand.getValue()) {
            ModuleManager.getModuleByName("AutoOffhandCrystal").disable();
        }
        if (LegCrystals.getValue()) {
            ModuleManager.getModuleByName("LegCrystals").disable();
        }
        if (autoOffhand.getValue()) {
            ModuleManager.getModuleByName("AutoTotem2").disable();
        }
        render = null;
        target = null;
        resetRotation();
    }

    @Override
    public String getHudInfo() {
        if (target == null) {
            return "";
        } else {
            return target.getName().toUpperCase();
        }
    }

    static {
        togglePitch = false;
    }

    public Setting<Boolean> getAntiStuck() {
        return antiStuck;
    }

    public void setRenderEnt(Entity renderEnt) {
        this.renderEnt = renderEnt;
    }

    public Listener<PacketEvent.Receive> getPacketListener() {
        return packetListener;
    }

    private enum RenderMode {
        SOLID,
        OUTLINE,
        FULL,
        SOLIDFLAT,
    }
}*/
