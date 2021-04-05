/*package meow.konc.hack.modules.crystal;

import me.zero.alpine.listener.Listener;
import meow.konc.hack.KONCMod;
import meow.konc.hack.command.Command;
import meow.konc.hack.event.events.other.PacketEvent;
import meow.konc.hack.event.events.other.RenderEvent;
import meow.konc.hack.module.Module;
import meow.konc.hack.setting.Setting;
import meow.konc.hack.setting.Settings;
import me.zero.alpine.listener.EventHandler;
import meow.konc.hack.util.other.Friends;
import meow.konc.hack.util.render.BlockInteractionHelper;
import meow.konc.hack.util.render.KONCTessellator;
import net.minecraft.client.Minecraft;
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

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Module.Info(name = "ACECrystal", category = Module.Category.CRYSTAL, description = "Best Kill Duh")
public class ACECrystal extends Module {
    private Setting<Boolean> explode = register(Settings.b("Explode", true));
    private Setting<Double> waitTick = register(Settings.doubleBuilder("TickDelay").withMinimum(0.0D).withMaximum(20.0D).withValue(1.0D));
    private Setting<Double> range = register(Settings.doubleBuilder("HitRange").withMinimum(0.0D).withMaximum(10.0D).withValue(5.0D));
    private Setting<Double> walls = register(Settings.doubleBuilder("WallBreakRange").withMinimum(0.0D).withMaximum(10.0D).withValue(3.5D));
    private Setting<Boolean> antiWeakness = register(Settings.b("AntiWeakness", true));
    private Setting<Boolean> nodesync = register(Settings.b("NoDesync", true));
    private Setting<Boolean> place = register(Settings.b("Place", true));
    private Setting<Boolean> autoSwitch = register(Settings.b("AutoSwitch", true));
    private Setting<Boolean> noGappleSwitch = register(Settings.b("NoGapSwitch", true));
    private Setting<Double> placeRange = register(Settings.doubleBuilder("PlaceRange").withMinimum(0.0D).withMaximum(10.0D).withValue(5.0D));
    private Setting<Double> minDmg = register(Settings.doubleBuilder("MinDmg").withMinimum(0.0D).withMaximum(40.0D).withValue(5.0D));
    private Setting<Double> facePlace = register(Settings.doubleBuilder("FacePlaceHP").withMinimum(0.0D).withMaximum(40.0D).withValue(6.0D));
    private Setting<Boolean> raytrace = register(Settings.b("RayTrace", false));
    private Setting<Boolean> rotate = register(Settings.b("Rotate", true));
    private Setting<Boolean> spoofRotations = register(Settings.b("SpoofAngles", true));
    private Setting<Double> maxSelfDmg = register(Settings.doubleBuilder("MaxSelfDmg").withMinimum(0.0D).withMaximum(40.0D).withValue(5.0D));
    private Setting<Boolean> chat = register(Settings.b("ToggleMessage", true));
    private Setting<Integer> espR = register(Settings.integerBuilder("ColorRed").withMinimum(0).withMaximum(255).withValue(200));
    private Setting<Integer> espG = register(Settings.integerBuilder("ColorGreen").withMinimum(0).withMaximum(255).withValue(50));
    private Setting<Integer> espB = register(Settings.integerBuilder("ColorBlue").withMinimum(0).withMaximum(255).withValue(200));
    private Setting<Integer> espA = register(Settings.integerBuilder("ColorAlpha").withMinimum(0).withMaximum(255).withValue(50));
    private Setting<Integer> renderMode = register(Settings.integerBuilder("RenderMode").withMinimum(1).withMaximum(5).withValue(1));

    private static boolean isSpoofingAngles;
    private static double yaw;
    private static double pitch;

    private boolean switchCooldown = false;
    private boolean isAttacking = false;
    public boolean isActive = false;

    private int oldSlot = -1;
    private int newSlot;
    private int waitCounter;

    private BlockPos render;
    EnumFacing f;

    @EventHandler
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
    private Listener<PacketEvent.Send> packetListener2 = new Listener<PacketEvent.Send>(event -> {
        if (spoofRotations.getValue()) {
            Packet packet = event.getPacket();
            if (packet instanceof CPacketPlayer && spoofRotations.getValue() && isSpoofingAngles) {
                ((CPacketPlayer) packet).yaw = (float) yaw;
                ((CPacketPlayer) packet).pitch = (float) pitch;
            }
        }
    }, new Predicate[0]);

    public void onUpdate() {
        isActive = false;
        if (mc.player != null && !mc.player.isDead) {
            EntityEnderCrystal crystal = mc.world.loadedEntityList.stream().filter((entityx) -> entityx instanceof EntityEnderCrystal).filter((e) -> mc.player.getDistance(e) <= range.getValue()).map((entityx) -> (EntityEnderCrystal) entityx).min(Comparator.comparing((c) -> mc.player.getDistance(c))).orElse((EntityEnderCrystal) null);
            int crystalSlot;
            if (explode.getValue() && crystal != null) {
                if (mc.player.canEntityBeSeen(crystal) || mc.player.getDistance(crystal) <= walls.getValue()) {
                    if (waitTick.getValue() > 0.0D) {
                        if (waitCounter < waitTick.getValue()) {
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

                        for (crystalSlot = 0; crystalSlot < 9; ++crystalSlot) {
                            ItemStack stack = mc.player.inventory.getStackInSlot(crystalSlot);
                            if (stack != ItemStack.EMPTY) {
                                if (stack.getItem() instanceof ItemSword) {
                                    newSlot = crystalSlot;
                                    break;
                                }

                                if (stack.getItem() instanceof ItemTool) {
                                    newSlot = crystalSlot;
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
                        lookAtPacket(crystal.posX, crystal.posY, crystal.posZ, mc.player);
                    }

                    mc.playerController.attackEntity(mc.player, crystal);
                    mc.player.swingArm(EnumHand.MAIN_HAND);
                    isActive = false;
                }
            } else {
                resetRotation();
                if (oldSlot != -1) {
                    mc.player.inventory.currentItem = oldSlot;
                    oldSlot = -1;
                }

                isAttacking = false;
                isActive = false;
                crystalSlot = mc.player.getHeldItemMainhand().getItem() == Items.END_CRYSTAL ? mc.player.inventory.currentItem : -1;
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

                List blocks = findCrystalBlocks();
                List entities = new ArrayList();
                entities.addAll(mc.world.playerEntities.stream().filter((entityPlayer) -> !Friends.isFriend(entityPlayer.getName())).sorted(Comparator.comparing((e) -> mc.player.getDistance(e))).collect(Collectors.toList()));
                BlockPos q = null;
                double damage = 0.5D;
                Iterator var9 = entities.iterator();

                label227:
                while (true) {
                    Entity entity;
                    do {
                        do {
                            do {
                                do {
                                    if (!var9.hasNext()) {
                                        if (damage == 0.5D) {
                                            render = null;
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
                                                lookAtPacket(q.getX() + 0.5D, q.getY() - 0.5D, q.getZ() + 0.5D, mc.player);
                                            }

                                            RayTraceResult result = mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ), new Vec3d(q.getX() + 0.5D, q.getY() - 0.5D, q.getZ() + 0.5D));
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
                                                    mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(q, f, offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, 0.0F, 0.0F, 0.0F));
                                                } else {
                                                    mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(q, EnumFacing.UP, offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, 0.0F, 0.0F, 0.0F));
                                                }
                                            }

                                            isActive = false;
                                        }

                                        return;
                                    }

                                    entity = (Entity) var9.next();
                                } while (entity == mc.player);
                            } while (((EntityLivingBase) entity).getHealth() <= 0.0F);
                        } while (entity.isDead);
                    } while (mc.player == null);

                    Iterator var11 = blocks.iterator();

                    while (true) {
                        BlockPos blockPos;
                        double d;
                        double self;
                        do {
                            do {
                                do {
                                    double b;
                                    do {
                                        if (!var11.hasNext()) {
                                            continue label227;
                                        }

                                        blockPos = (BlockPos) var11.next();
                                        b = entity.getDistanceSq(blockPos);
                                    } while (b >= 169.0D);

                                    d = calculateDamage(blockPos.getX() + 0.5D, blockPos.getY() + 1, blockPos.getZ() + 0.5D, entity);
                                } while (d < minDmg.getValue() && (((EntityLivingBase) entity).getHealth() + ((EntityLivingBase) entity).getAbsorptionAmount()) > facePlace.getValue());
                            } while (d <= damage);

                            self = calculateDamage(blockPos.getX() + 0.5D, blockPos.getY() + 1, blockPos.getZ() + 0.5D, mc.player);
                        } while (self > d && d >= ((EntityLivingBase) entity).getHealth());

                        if (self - 0.5D <= mc.player.getHealth() && self <= maxSelfDmg.getValue()) {
                            damage = d;
                            q = blockPos;
                        }
                    }
                }
            }
        }
    }

    private boolean isEatingGap() {
        return mc.player.getHeldItemMainhand().getItem() instanceof ItemAppleGold && mc.player.isHandActive();
    }

    public void onWorldRender(RenderEvent event) {
        if (render != null) {
            KONCTessellator.prepare(7);
            if (renderMode.getValue() == 1) {
                KONCTessellator.drawBox(render, espR.getValue(), espG.getValue(), espB.getValue(), espA.getValue(), 2);
            }

            if (renderMode.getValue() == 2) {
                KONCTessellator.drawBox(render, espR.getValue(), espG.getValue(), espB.getValue(), espA.getValue(), 63);
            }

            if (renderMode.getValue() == 3) {
                KONCTessellator.drawBox(render, espR.getValue(), espG.getValue(), espB.getValue(), espA.getValue(), 1);
            }

            KONCTessellator.release();
        }

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
        NonNullList<BlockPos> positions = NonNullList.create();
        positions.addAll(BlockInteractionHelper.getSphere(getPlayerPos(), placeRange.getValue().floatValue(), placeRange.getValue().intValue(), false, true, 0).stream().filter(this::canPlaceCrystal).collect(Collectors.toList()));
        return positions;
    }

    public List getSphere(BlockPos loc, float r, int h, boolean hollow, boolean sphere, int plus_y) {
        List circleblocks = new ArrayList();
        int cx = loc.getX();
        int cy = loc.getY();
        int cz = loc.getZ();

        for (int x = cx - (int) r; (float) x <= (float) cx + r; ++x) {
            for (int z = cz - (int) r; (float) z <= (float) cz + r; ++z) {
                for (int y = sphere ? cy - (int) r : cy; (float) y < (sphere ? (float) cy + r : (float) (cy + h)); ++y) {
                    double dist = ((cx - x) * (cx - x) + (cz - z) * (cz - z) + (sphere ? (cy - y) * (cy - y) : 0));
                    if (dist < (r * r) && (!hollow || dist >= ((r - 1.0F) * (r - 1.0F)))) {
                        BlockPos l = new BlockPos(x, y + plus_y, z);
                        circleblocks.add(l);
                    }
                }
            }
        }

        return circleblocks;
    }

    public static float calculateDamage(double posX, double posY, double posZ, Entity entity) {
        float doubleExplosionSize = 12.0F;
        double distancedsize = entity.getDistance(posX, posY, posZ) / doubleExplosionSize;
        Vec3d vec3d = new Vec3d(posX, posY, posZ);
        double blockDensity = entity.world.getBlockDensity(vec3d, entity.getEntityBoundingBox());
        double v = (1.0D - distancedsize) * blockDensity;
        float damage = (float) ((int) ((v * v + v) / 2.0D * 7.0D * doubleExplosionSize + 1.0D));
        double finald = 1.0D;
        if (entity instanceof EntityLivingBase) {
            finald = getBlastReduction((EntityLivingBase) entity, getDamageMultiplied(damage), new Explosion(mc.world, null, posX, posY, posZ, 6.0F, false, true));
        }

        return (float) finald;
    }

    public static float getBlastReduction(EntityLivingBase entity, float damage, Explosion explosion) {
        if (entity instanceof EntityPlayer) {
            EntityPlayer ep = (EntityPlayer) entity;
            DamageSource ds = DamageSource.causeExplosionDamage(explosion);
            damage = CombatRules.getDamageAfterAbsorb(damage, (float) ep.getTotalArmorValue(), (float) ep.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());
            int k = EnchantmentHelper.getEnchantmentModifierDamage(ep.getArmorInventoryList(), ds);
            float f = MathHelper.clamp((float) k, 0.0F, 20.0F);
            damage *= 1.0F - f / 25.0F;
            if (entity.isPotionActive(Potion.getPotionById(11))) {
                damage -= damage / 4.0F;
            }

            return damage;
        } else {
            damage = CombatRules.getDamageAfterAbsorb(damage, (float) entity.getTotalArmorValue(), (float) entity.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());
            return damage;
        }
    }

    private static float getDamageMultiplied(float damage) {
        int diff = mc.world.getDifficulty().getId();
        return damage * (diff == 0 ? 0.0F : (diff == 2 ? 1.0F : (diff == 1 ? 0.5F : 1.5F)));
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
        pitch = pitch * 180.0D / 3.141592653589793D;
        yaw = yaw * 180.0D / 3.141592653589793D;
        yaw += 90.0D;
        return new double[]{yaw, pitch};
    }

    public void onEnable() {
        KONCMod.EVENT_BUS.subscribe(this);
        isActive = false;
        if (chat.getValue() && mc.player != null) {
            Command.sendChatMessage("ACECrystal \u00A72ON");
        }

    }

    public void onDisable() {
        KONCMod.EVENT_BUS.unsubscribe(this);
        render = null;
        resetRotation();
        isActive = false;
        if (chat.getValue()) {
            Command.sendChatMessage("ACECrystal \u00A74OFF");
        }

    }
}*/
