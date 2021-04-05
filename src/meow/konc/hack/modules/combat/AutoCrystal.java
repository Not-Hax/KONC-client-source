package meow.konc.hack.modules.combat;

import com.mojang.realmsclient.gui.ChatFormatting;
import meow.konc.hack.command.Command;
import meow.konc.hack.gui.font.CFontRenderer;
import meow.konc.hack.gui.rgui.component.container.use.message.RendererHWID;
import meow.konc.hack.util.packet.BlockInteractionHelper;
import meow.konc.hack.util.packet.KONCTessellator;
import meow.konc.hack.util.other.Friends;
import meow.konc.hack.util.packet.Wrapper;
import meow.konc.hack.util.util.EntityUtil;
import meow.konc.hack.util.util.MathUtil;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import meow.konc.hack.event.events.other.PacketEvent;
import meow.konc.hack.event.events.other.RenderEvent;
import meow.konc.hack.module.Module;
import meow.konc.hack.module.ModuleManager;
import meow.konc.hack.setting.Setting;
import meow.konc.hack.setting.Settings;
import meow.pork.vocoshulkerpeek2.VocoShulkerPeek2;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.*;
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
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by 086 on 28/12/2017.
 * Updated 3 December 2019 by hub
 */
@Module.Info(name = "AutoCrystal", category = Module.Category.COMBAT)
public class AutoCrystal extends Module {
    public static Entity renderEnt;
    public static boolean togglePitch = false;
    public static boolean isSpoofingAngles;
    public static float yaw;
    public static float pitch;
    public final Setting<typesetting> typeset = register(Settings.enumBuilder(typesetting.class).withName("Type").withValue(typesetting.GENERAL));
    public Setting<Boolean> autoSwitch = register(Settings.booleanBuilder("Auto Switch").withValue(true).withVisibility(v -> typeset.getValue().equals(typesetting.GENERAL)).build());
    public Setting<Boolean> place = register(Settings.booleanBuilder("Place").withValue(true).withVisibility(v -> typeset.getValue().equals(typesetting.GENERAL)).build());
    public Setting<Boolean> explode = register(Settings.booleanBuilder("Explode").withValue(true).withVisibility(v -> typeset.getValue().equals(typesetting.GENERAL)).build());
    private Setting<Boolean> sneakEnable = register(Settings.booleanBuilder("Sneak Surround").withValue(false).withVisibility(v -> typeset.getValue().equals(typesetting.GENERAL)).build());
    public Setting<Boolean> offhandhit = register(Settings.booleanBuilder("OffHandHit").withValue(true).withVisibility(v -> typeset.getValue().equals(typesetting.GENERAL) && explode.getValue()).build());
    public Setting<Boolean> legitplace = register(Settings.booleanBuilder("LegitPlace").withValue(true).withVisibility(v -> typeset.getValue().equals(typesetting.GENERAL) && place.getValue()).build());
    public Setting<Boolean> box = register(Settings.booleanBuilder("BlockHighlight").withValue(true).withVisibility(v -> typeset.getValue().equals(typesetting.GENERAL) && place.getValue()).build());
    public Setting<Boolean> animals = register(Settings.booleanBuilder("Animals").withValue(true).withVisibility(v -> typeset.getValue().equals(typesetting.GENERAL)).build());
    public Setting<Boolean> mobs = register(Settings.booleanBuilder("Mobs").withValue(true).withVisibility(v -> typeset.getValue().equals(typesetting.GENERAL)).build());
    public Setting<Boolean> rotate = register(Settings.booleanBuilder("Rotate").withValue(true).withVisibility(v -> typeset.getValue().equals(typesetting.GENERAL)).build());
    public Setting<Boolean> betterplacements = register(Settings.booleanBuilder("BetterPlacements").withValue(true).withVisibility(v -> typeset.getValue().equals(typesetting.GENERAL)).build());
    public Setting<Double> placerange = register(Settings.doubleBuilder("PlaceRange").withMinimum(1.0).withValue(4.5).withMaximum(7.0).withVisibility(v -> typeset.getValue().equals(typesetting.NUMBER) && place.getValue()).build());
    public Setting<Double> placeleastrange = register(Settings.doubleBuilder("BetterPlaceRange").withMinimum(1.0).withValue(4.5).withMaximum(7.0).withVisibility(v -> typeset.getValue().equals(typesetting.NUMBER) && place.getValue() && betterplacements.getValue()).build());
    public Setting<Double> range = register(Settings.doubleBuilder("HitRange").withMinimum(1.0).withValue(5.0).withMaximum(7.0).withVisibility(v -> typeset.getValue().equals(typesetting.NUMBER) && explode.getValue()).build());
    public Setting<TargetMode> mode = register(Settings.enumBuilder(TargetMode.class).withName("Target Mode").withValue(TargetMode.ORIGINAL).withVisibility(v -> typeset.getValue().equals(typesetting.GENERAL)).build());
    public Setting<owokami> owokamiisgud = register(Settings.enumBuilder(owokami.class).withName("DamageMode").withValue(owokami.MostAdvantage).withVisibility(v -> typeset.getValue().equals(typesetting.GENERAL)).build());
    public Setting<Double> playerdistance = register(Settings.doubleBuilder("EnemyPlayerDist").withMinimum(0.0).withValue(14.0).withMaximum(20.0).withVisibility(v -> typeset.getValue().equals(typesetting.NUMBER)).build());
    public Setting<Double> distance = register(Settings.doubleBuilder("EnemyCrystalDist").withMinimum(1.0).withValue(8.0).withMaximum(13.0).withVisibility(v -> typeset.getValue().equals(typesetting.NUMBER)).build());
    public Setting<Boolean> antiWeakness = register(Settings.booleanBuilder("AntiWeakness").withValue(true).withVisibility(v -> typeset.getValue().equals(typesetting.GENERAL)).build());
    public Setting<Boolean> renderdamage = register(Settings.booleanBuilder("RenderDamage").withValue(true).withVisibility(v -> typeset.getValue().equals(typesetting.GENERAL) && place.getValue()).build());
    public Setting<Boolean> accuratedamagerender = register(Settings.booleanBuilder("RenderAccurateDamage").withValue(true).withVisibility(v -> typeset.getValue().equals(typesetting.GENERAL) && renderdamage.getValue()).build());
    public Setting<Integer> placedelay = register(Settings.integerBuilder("PlaceDelay").withMinimum(0).withValue(2).withMaximum(20).withVisibility(v -> typeset.getValue().equals(typesetting.NUMBER) && place.getValue()).build());
    public Setting<Integer> hitdelay = register(Settings.integerBuilder("HitDelay").withMinimum(0).withValue(0).withMaximum(20).withVisibility(v -> typeset.getValue().equals(typesetting.NUMBER) && explode.getValue()).build());
    public Setting<Boolean> nodesync = register(Settings.booleanBuilder("FastSync").withValue(true).withVisibility(v -> typeset.getValue().equals(typesetting.GENERAL)).build());
    public Setting<Boolean> suicideprot = register(Settings.booleanBuilder("SuicideProt").withValue(true).withVisibility(v -> typeset.getValue().equals(typesetting.GENERAL)).build());
    public Setting<Boolean> maxselfcalculations = register(Settings.booleanBuilder("MaxSelfCalculation").withValue(true).withVisibility(v -> typeset.getValue().equals(typesetting.GENERAL)).build());
    public Setting<Boolean> wall = register(Settings.booleanBuilder("Walls").withValue(true).withVisibility(v -> typeset.getValue().equals(typesetting.GENERAL)).build());
    public Setting<Double> walls = register(Settings.doubleBuilder("WallsRange").withMinimum(0.0).withValue(3.5).withMaximum(5.0).withVisibility(v -> typeset.getValue().equals(typesetting.NUMBER) && wall.getValue()).build());
    public Setting<Integer> Placements = register(Settings.integerBuilder("Place Break").withMinimum(0).withValue(2).withMaximum(10).withVisibility(v -> typeset.getValue().equals(typesetting.NUMBER)).build());
    public Setting<Double> mindiff = register(Settings.doubleBuilder("MinDifference").withMinimum(0.0).withValue(4.0).withMaximum(10.0).withVisibility(v -> typeset.getValue().equals(typesetting.NUMBER) && place.getValue() && owokamiisgud.getValue().equals(owokami.MostAdvantage)).build());
    public Setting<Double> mindmg = register(Settings.doubleBuilder("Min Dmg").withMinimum(0.0).withValue(2.0).withMaximum(10.0).withVisibility(v -> typeset.getValue().equals(typesetting.NUMBER) && place.getValue() && owokamiisgud.getValue().equals(owokami.MostDamage)).build());
    public Setting<Double> minhitdmg = register(Settings.doubleBuilder("MinHitDmg").withMinimum(0.0).withValue(2.0).withMaximum(10.0).withVisibility(v -> typeset.getValue().equals(typesetting.NUMBER) && explode.getValue() && owokamiisgud.getValue().equals(owokami.MostDamage)).build());
    public Setting<Double> minhitdiff = register(Settings.doubleBuilder("MinHitDifference").withMinimum(0.0).withValue(2.0).withMaximum(10.0).withVisibility(v -> typeset.getValue().equals(typesetting.NUMBER) && explode.getValue() && owokamiisgud.getValue().equals(owokami.MostAdvantage)).build());
    public Setting<Double> faceplacehealth = register(Settings.doubleBuilder("FacePlaceHealth").withMinimum(0.0).withValue(10.0).withMaximum(36.0).withVisibility(v -> typeset.getValue().equals(typesetting.NUMBER) && place.getValue()).build());
    public Setting<Integer> selfdmg = register(Settings.integerBuilder("Max Self").withMinimum(0).withValue(4).withMaximum(16).withVisibility(v -> typeset.getValue().equals(typesetting.NUMBER) && place.getValue()).build());
    public Setting<Integer> maxhitdmg = register(Settings.integerBuilder("MaxSelfHitDmg").withMinimum(0).withValue(6).withMaximum(16).withVisibility(v -> typeset.getValue().equals(typesetting.NUMBER) && explode.getValue()).build());
    public Setting<Integer> antideath = register(Settings.integerBuilder("BeforeDeathValue").withMinimum(0).withValue(2).withMaximum(10).withVisibility(v -> typeset.getValue().equals(typesetting.NUMBER) && (place.getValue() || explode.getValue())).build());
    public Setting<Integer> red = register(Settings.integerBuilder("Red").withMinimum(0).withValue(255).withMaximum(255).withVisibility(v -> typeset.getValue().equals(typesetting.COLOR)).build());
    public Setting<Integer> green = register(Settings.integerBuilder("Green").withMinimum(0).withValue(255).withMaximum(255).withVisibility(v -> typeset.getValue().equals(typesetting.COLOR)).build());
    public Setting<Integer> blue = register(Settings.integerBuilder("Blue").withMinimum(0).withValue(255).withMaximum(255).withVisibility(v -> typeset.getValue().equals(typesetting.COLOR)).build());
    public Setting<Integer> alpha = register(Settings.integerBuilder("Alpha").withMinimum(0).withValue(75).withMaximum(255).withVisibility(v -> typeset.getValue().equals(typesetting.COLOR)).build());
    public Setting<Boolean> rainbow = register(Settings.booleanBuilder("Rainbow").withValue(true).withVisibility(v -> typeset.getValue().equals(typesetting.COLOR) && box.getValue()).build());

    public Setting<Integer> width = register(Settings.integerBuilder("Thickness").withValue(5).withVisibility(v -> typeset.getValue().equals(typesetting.COLOR) && box.getValue()).build());
    public Setting<Boolean> entityignore = register(Settings.booleanBuilder("EntityIgnore").withValue(false).withVisibility(v -> typeset.getValue().equals(typesetting.GENERAL)).build());
    public Setting<Boolean> singleplace = register(Settings.booleanBuilder("SinglePlace").withValue(false).withVisibility(v -> typeset.getValue().equals(typesetting.GENERAL) && !entityignore.getValue()).build());
    public Setting<Boolean> announceUsage = register(Settings.booleanBuilder("Announce Usage").withValue(true).withVisibility(v -> typeset.getValue().equals(typesetting.GENERAL)).build());
    public Setting<Boolean> noGappleSwitch = register(Settings.booleanBuilder("NoGappleSwitch").withValue(true).withVisibility(v -> typeset.getValue().equals(typesetting.GENERAL)).build());
    public Setting<Integer> dangerous = register(Settings.integerBuilder("DangerCrystal").withMinimum(0).withValue(6).withMaximum(16).withVisibility(v -> typeset.getValue().equals(typesetting.NUMBER) && explode.getValue()).build());
    public Setting<Boolean> rainbow2 = register(Settings.booleanBuilder("HighLightRainbow").withValue(true).withVisibility(v -> typeset.getValue().equals(typesetting.COLOR) && box.getValue()).build());

    CFontRenderer ff = new CFontRenderer(new Font("MS Reference Sans Serif", 0, 18), true, true);

    double selfDamage = 0.0;

    public enum typesetting {GENERAL, NUMBER, COLOR}

    public enum TargetMode {SINGLE, ORIGINAL}

    public enum owokami {MostDamage, MostAdvantage}

    private List<EntityEnderCrystal> lyowo = new ArrayList<>();
    private List<CPacketPlayer> qwqwqwqwq = new ArrayList<>();
    public EntityEnderCrystal crystal;
    private EntityEnderCrystal lastcrystal;

    public BlockPos render;
    public BlockPos wwwwwww;

    private BlockPos q;

    public String qwq;

    public int hittick = hitdelay.getValue();
    public int placetick = placedelay.getValue();
    public int placements = 0;
    public int oldSlot = -1;
    public int newSlot;
    public int kami;

    public boolean switchCooldown = false;
    public boolean isAttacking = false;
    public boolean danger;

    private boolean rotated;
    private boolean killaura;

    private double damage;
    private double b;

    private float yawsave;
    private float pitchsave;

    int holeBlocks = 0;

    private static List<CPacketPlayer> packetrotation = new ArrayList<>();
    // we need this cooldown to not place from old hotbar slot, before we have switched to crystals
    @EventHandler
    public Listener<PacketEvent.Receive> packetListener = new Listener<PacketEvent.Receive>(event -> {
        if (event.getPacket() instanceof SPacketSoundEffect && nodesync.getValue()) {
            SPacketSoundEffect packet = (SPacketSoundEffect) event.getPacket();
            try {
                if (packet.getCategory() == SoundCategory.BLOCKS && packet.getSound() == SoundEvents.ENTITY_GENERIC_EXPLODE) {
                    for (Entity e : Minecraft.getMinecraft().world.loadedEntityList) {
                        if (e instanceof EntityEnderCrystal && e.getDistance(packet.getX(), packet.getY(), packet.getZ()) <= 6.0D) {
                            e.setDead();
                        }
                    }
                }
            } catch (ConcurrentModificationException o) {
                return;
            }
        }
    });

    @EventHandler
    public Listener<PacketEvent.Send> packetListener2 = new Listener<>(event -> {
        Packet packet = event.getPacket();
        if (packet instanceof CPacketPlayer) {
            if (isSpoofingAngles) {
                ((CPacketPlayer) packet).yaw = yaw;
                ((CPacketPlayer) packet).pitch = pitch;
            }
        }

    });

    public static void lookAtPacket(double px, double py, double pz, EntityPlayer me) {
        double[] v = EntityUtil.calculateLookAt(px, py, pz, me);
        setYawAndPitch((float) v[0], (float) v[1]);
    }

    public static BlockPos getPlayerPos() {
        return new BlockPos(Math.floor(mc.player.posX), Math.floor(mc.player.posY), Math.floor(mc.player.posZ));
    }

    public static boolean canBlockBeSeen(BlockPos blockPos) {
        return mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + (double) mc.player.getEyeHeight(), mc.player.posZ), new Vec3d(blockPos.x, blockPos.y + 1.7d, blockPos.z), false, true, false) == null;
    }

    public static float calculateDamage(double posX, double posY, double posZ, Entity entity) {
        float doubleExplosionSize = 12.0f;
        double distancedsize = entity.getDistance(posX, posY, posZ) / doubleExplosionSize;
        Vec3d vec3d = new Vec3d(posX, posY, posZ);
        double blockDensity = entity.world.getBlockDensity(vec3d, entity.getEntityBoundingBox());
        double v = (1.0 - distancedsize) * blockDensity;
        float damage = (float) (int) ((v * v + v) / 2.0 * 7.0 * doubleExplosionSize + 1.0);
        double d = 1.0;
        if (entity instanceof EntityLivingBase) {
            d = getBlastReduction((EntityLivingBase) entity, getDamageMultiplied(damage), new Explosion(mc.world, null, posX, posY, posZ, 6.0f, false, true));
        }
        return (float) d;
    }

    public static float getBlastReduction(EntityLivingBase entity, float damage, Explosion explosion) {
        if (entity instanceof EntityPlayer) {
            EntityPlayer ep = (EntityPlayer) entity;
            DamageSource ds = DamageSource.causeExplosionDamage(explosion);
            damage = CombatRules.getDamageAfterAbsorb(damage, (float) ep.getTotalArmorValue(), (float) ep.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());
            int k = EnchantmentHelper.getEnchantmentModifierDamage(ep.getArmorInventoryList(), ds);
            float f = MathHelper.clamp((float) k, 0.0f, 20.0f);
            damage *= 1.0f - f / 25.0f;
            if (entity.isPotionActive(Potion.getPotionById(11)) || entity.getAbsorptionAmount() >= 9) {
                damage -= damage / 5.0f;
            }
            return damage;
        }
        damage = CombatRules.getDamageAfterAbsorb(damage, (float) entity.getTotalArmorValue(), (float) entity.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());
        return damage;
    }

    public static float getDamageMultiplied(float damage) {
        int diff = mc.world.getDifficulty().getId();
        return damage * ((diff == 0) ? 0.0f : ((diff == 2) ? 1.0f : ((diff == 1) ? 0.5f : 1.5f)));
    }

    public static float calculateDamage(EntityEnderCrystal crystal, Entity entity) {
        return calculateDamage(crystal.posX, crystal.posY, crystal.posZ, entity);
    }

    public static float calculateDamage(Entity crystal, Entity entity) {
        return calculateDamage(crystal.posX, crystal.posY, crystal.posZ, entity);
    }

    public static boolean calculateDirection(double yaw, double playeryaw) {
        if (yaw < playeryaw) {
            yaw += 360;
        }
        double qwq = yaw - playeryaw;
        if (qwq <= 180) {
            return true;
        }
        return false;
    }

    public static double calculateDirectionDifference(double alpha, double beta) {
        double phi = Math.abs(beta - alpha) % 360;// This is either the distance or 360 - distance
        return phi > 180 ? 360 - phi : phi;
    }

    public static void setYawAndPitch(float yaw1, float pitch1) {
        Random rand = new Random(2);
        yaw = yaw1 + (rand.nextFloat() / 100);
        pitch = pitch1 + (rand.nextFloat() / 100);
        isSpoofingAngles = true;
        mc.player.rotationYawHead = yaw;
    }

    public static void resetRotation() {
        if (isSpoofingAngles) {
            yaw = mc.player.rotationYaw;
            pitch = mc.player.rotationPitch;
            isSpoofingAngles = false;
        }
    }

    @Override
    public void onUpdate() {
        if (sneakEnable.getValue() && mc.player.isSneaking() && holeBlocks != 5) {
            ModuleManager.getModuleByName("AutoFeetPlace").enable();
        }
        if (entityignore.getValue() && singleplace.getValue()) {
            singleplace.setValue(false);
        }
        lyowo.clear();
        crystal = null;
        lastcrystal = null;
        lyowo.addAll(mc.world.loadedEntityList.stream().filter(entity -> entity instanceof EntityEnderCrystal).map(entity -> (EntityEnderCrystal) entity).sorted(Comparator.comparing(e -> mc.player.getDistance(e))).collect(Collectors.toList()));
        if (hitdelay.getValue() <= hittick) {
            hittick = 0;
            rotated = false;
            double enemydmg = renderEnt != null ? (((EntityLivingBase) renderEnt).getHealth() + ((EntityLivingBase) renderEnt).getAbsorptionAmount() > faceplacehealth.getValue() ? minhitdmg.getValue() : 0) : minhitdmg.getValue();
            double enemydmgdiff = renderEnt != null ? (((EntityLivingBase) renderEnt).getHealth() + ((EntityLivingBase) renderEnt).getAbsorptionAmount() > faceplacehealth.getValue() ? minhitdiff.getValue() : 0) : minhitdiff.getValue();
            double selfowo = 10000;
            danger = false;
            List<Entity> entityrefresh = new ArrayList<>(mc.world.playerEntities.stream().filter(entityPlayer -> !Friends.isFriend(entityPlayer.getName())).sorted(Comparator.comparing(e -> mc.player.getDistance(e))).collect(Collectors.toList()));
            if (mobs.getValue() || animals.getValue()) {
                entityrefresh.addAll(mc.world.loadedEntityList.stream().filter(entity -> EntityUtil.isLiving(entity) && !(entity instanceof EntityPlayer) && (EntityUtil.isPassive(entity) ? animals.getValue() : mobs.getValue())).collect(Collectors.toList()));
            }

            if (renderEnt != null) {
                for (Entity kami : entityrefresh) {
                    if (((EntityLivingBase) kami).getHealth() <= 0 || kami.isDead) continue;
                    if (!kami.getName().equals(renderEnt.getName())) continue;
                    for (EntityEnderCrystal crystal2 : lyowo) {
                        if (mc.player.getDistance(crystal2) > range.getValue() || (!mc.player.canEntityBeSeen(crystal2) && mc.player.getDistance(crystal2) > walls.getValue() && wall.getValue())) {
                            break;
                        }
                        if (kami.getDistance(crystal2) > 13) {
                            continue;
                        }
                        double enemydmgcal = calculateDamage(crystal2, kami);
                        double owo = calculateDamage(crystal2, mc.player);
                        if (owokamiisgud.getValue().equals(owokami.MostDamage) ? enemydmgcal > enemydmg : enemydmgcal - owo > enemydmgdiff) {
                            if ((owo + antideath.getValue() > mc.player.getHealth() + mc.player.getAbsorptionAmount() && suicideprot.getValue()) || (owo > maxhitdmg.getValue() && maxselfcalculations.getValue())) {
                                continue;
                            }
                            if (enemydmgcal <= owo && enemydmgcal < ((EntityLivingBase) kami).getHealth() + ((EntityLivingBase) kami).getAbsorptionAmount()) {
                                continue;
                            }
                            crystal = crystal2;
                            enemydmg = enemydmgcal;
                            enemydmgdiff = enemydmgcal - owo;
                        }
                    }
                }
            }
            if (crystal == null) {
                for (Entity kami : entityrefresh) {
                    if (kami == mc.player) continue;
                    if (((EntityLivingBase) kami).getHealth() <= 0 || kami.isDead) continue;

                    for (EntityEnderCrystal crystal2 : lyowo) {
                        if (mc.player.getDistance(crystal2) > range.getValue() || (!mc.player.canEntityBeSeen(crystal2) && mc.player.getDistance(crystal2) > walls.getValue() && wall.getValue())) {
                            break;
                        }
                        if (kami.getDistance(crystal2) > 13) {
                            continue;
                        }

                        double enemydmgcal = calculateDamage(crystal2, kami);
                        double owo = calculateDamage(crystal2, mc.player);
                        if (owokamiisgud.getValue().equals(owokami.MostDamage) ? enemydmgcal > enemydmg : enemydmgcal - owo > enemydmgdiff) {

                            if ((owo + antideath.getValue() > mc.player.getHealth() + mc.player.getAbsorptionAmount() && suicideprot.getValue()) || (owo > maxhitdmg.getValue() && maxselfcalculations.getValue())) {
                                continue;
                            }

                            if (enemydmgcal <= owo && enemydmgcal < ((EntityLivingBase) kami).getHealth() + ((EntityLivingBase) kami).getAbsorptionAmount()) {
                                continue;
                            }
                            crystal = crystal2;
                            enemydmg = enemydmgcal;
                            enemydmgdiff = enemydmgcal - owo;
                        }
                    }
                }
            }
            if (crystal == null && suicideprot.getValue()) {
                for (EntityEnderCrystal crystal2 : lyowo) {
                    if (mc.player.getDistance(crystal2) > range.getValue() || (!mc.player.canEntityBeSeen(crystal2) && mc.player.getDistance(crystal2) > walls.getValue() && wall.getValue())) {
                        continue;
                    }
                    double selfexplodedmg = calculateDamage(crystal2, mc.player);
                    if (selfexplodedmg >= dangerous.getValue()) {
                        danger = true;
                        continue;
                    }
                    if (selfexplodedmg < selfowo) {
                        if (selfexplodedmg + 0.5 > mc.player.getHealth() + mc.player.getAbsorptionAmount()) {
                            continue;
                        }

                    }
                    crystal = crystal2;
                    selfowo = selfexplodedmg;
                }
                if (!danger) {
                    crystal = null;
                }
            }
            if (crystal == null) {
                hittick = hitdelay.getValue();
            } else if (explode.getValue()) {
                if (antiWeakness.getValue() && mc.player.isPotionActive(MobEffects.WEAKNESS)) {
                    if (!isAttacking) {
                        // save initial player hand
                        oldSlot = Wrapper.getPlayer().inventory.currentItem;
                        isAttacking = true;
                    }
                    // search for sword and tools in hotbar
                    newSlot = -1;
                    for (int i = 0; i < 9; i++) {
                        ItemStack stack = Wrapper.getPlayer().inventory.getStackInSlot(i);
                        if (stack == ItemStack.EMPTY) {
                            continue;
                        }
                        if ((stack.getItem() instanceof ItemSword)) {
                            newSlot = i;
                            break;
                        }
                        if ((stack.getItem() instanceof ItemTool)) {
                            newSlot = i;
                            break;
                        }
                    }
                    // check if any swords or tools were found
                    if (newSlot != -1) {
                        Wrapper.getPlayer().inventory.currentItem = newSlot;
                        switchCooldown = true;
                    }
                }
                if (rotate.getValue()) {
                    lookAtPacket(crystal.posX, crystal.posY, crystal.posZ, mc.player);
                    wwwwwww = new BlockPos(crystal.posX, crystal.posY, crystal.posZ);
                    rotated = true;
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

                mc.playerController.attackEntity(mc.player, crystal);
                mc.player.swingArm(offhandhit.getValue() ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);
                lastcrystal = crystal;
                if (!entityignore.getValue() && placements >= Placements.getValue() && Placements.getValue() != 0) {
                    placements = 0;
                    return;
                }
                if (singleplace.getValue()) {
                    return;
                }
            } else {
                if (oldSlot != -1) {
                    Wrapper.getPlayer().inventory.currentItem = oldSlot;
                    oldSlot = -1;
                }
                isAttacking = false;
            }
        } else {
            ++hittick;
            if (entityignore.getValue() || singleplace.getValue()) {
                return;
            }
        }

        if (entityignore.getValue() || singleplace.getValue() || placedelay.getValue() <= placetick) {
            placetick = 0;
            int crystalSlot = mc.player.getHeldItemMainhand().getItem() == Items.END_CRYSTAL ? mc.player.inventory.currentItem : -1;
            if (crystalSlot == -1) {
                for (int l = 0; l < 9; ++l) {
                    if (mc.player.inventory.getStackInSlot(l).getItem() == Items.END_CRYSTAL) {
                        crystalSlot = l;
                        break;
                    }
                }
            }
            killaura = !ModuleManager.getModuleByName("KillAura").isDisabled();
            boolean offhand = false;
            if (mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL) {
                offhand = true;
            } else if (crystalSlot == -1) {
                return;
            }

            List<BlockPos> blocks = findCrystalBlocks();
            List<Entity> entities = new ArrayList<>();
            entities.addAll(mc.world.playerEntities.stream().collect(Collectors.toList()));
            if (mobs.getValue() || animals.getValue()) {
                entities.addAll(mc.world.loadedEntityList.stream().filter(entity -> EntityUtil.isLiving(entity) && !(entity instanceof EntityPlayer) && (EntityUtil.isPassive(entity) ? animals.getValue() : mobs.getValue())).collect(Collectors.toList()));
            }
            q = null;
            boolean faceplace;
            damage = mindmg.getValue();
            double difference = mindiff.getValue();
            for (Entity entity : entities) {
                if (entity == mc.player || ((EntityLivingBase) entity).getHealth() <= 0 || entity.isDead) {
                    continue;
                }
                if (Friends.isFriend(entity.getName())) {
                    continue;
                }
                if (mode.getValue().equals(TargetMode.SINGLE)) {
                    if (renderEnt != null && !renderEnt.getName().equals(entity.getName())) {
                        continue;
                    }
                }
                if (entity.getDistance(mc.player) > playerdistance.getValue()) {
                    continue;
                }

                faceplace = ((EntityLivingBase) entity).getHealth() + ((EntityLivingBase) entity).getAbsorptionAmount() <= faceplacehealth.getValue();
                blocks.sort(Comparator.comparing(e -> entity.getDistance(e.x + 0.5, e.y + 1, e.z + 0.5)));
                for (BlockPos blockPos : blocks) {
                    if (distance.getValue() < BlockInteractionHelper.blockDistance(blockPos.x + 0.5, blockPos.y + 1, blockPos.z + 0.5, entity)) {
                        continue;
                    }
                    double d = calculateDamage(blockPos.x + .5, blockPos.y + 1, blockPos.z + .5, entity);
                    double self = calculateDamage(blockPos.x + .5, blockPos.y + 1, blockPos.z + .5, mc.player);
                    if (owokamiisgud.getValue().equals(owokami.MostDamage) ? d > damage : d - self > difference) {

                        if ((self > d && d < ((EntityLivingBase) entity).getHealth() + ((EntityLivingBase) entity).getAbsorptionAmount())) {
                            continue;
                        }
                        if (d < mindmg.getValue() && !faceplace) {
                            continue;
                        }
                        if (self + antideath.getValue() > mc.player.getHealth() + mc.player.getAbsorptionAmount() && suicideprot.getValue()) {
                            continue;
                        }
                        if (self > selfdmg.getValue() && maxselfcalculations.getValue()) {
                            continue;
                        }
                        selfDamage = self;
                        damage = d;
                        difference = d - self;
                        q = blockPos;
                        renderEnt = entity;
                    } else if (!killaura && (owokamiisgud.getValue().equals(owokami.MostDamage) ? damage == mindmg.getValue() : difference == mindiff.getValue()) && faceplace) {
                        // If this deals more damage to ourselves than it does to our target, continue. This is only ignored if the crystal is sure to kill our target but not us.
                        // Also continue if our crystal is going to hurt us.. alot
                        if ((self > d && d < ((EntityLivingBase) entity).getHealth() + ((EntityLivingBase) entity).getAbsorptionAmount())) {
                            continue;
                        }
                        if (self + antideath.getValue() > mc.player.getHealth() + mc.player.getAbsorptionAmount() && suicideprot.getValue()) {
                            continue;
                        }
                        if (self > selfdmg.getValue() && maxselfcalculations.getValue()) {
                            continue;
                        }
                        selfDamage = self;
                        damage = d;
                        q = blockPos;
                        difference = d - self;
                        renderEnt = entity;
                    }
                }

            }
            if (damage == mindmg.getValue() || damage < 1) {
                render = null;
                resetRotation();

                qwq = null;
                return;


            }

            render = q;
            if (place.getValue()) {
                if (!offhand && mc.player.inventory.currentItem != crystalSlot) {
                    if (autoSwitch.getValue()) {
                        if (noGappleSwitch.getValue() && isEatingGap()) {
                            resetRotation();
                            return;
                        }
                        mc.player.inventory.currentItem = crystalSlot;
                        resetRotation();
                        switchCooldown = true;
                    }
                    return;
                }
                if (rotate.getValue() && !rotated) {
                    lookAtPacket(q.x + .5, q.y, q.z + .5, mc.player);
                    rotated = true;
                    wwwwwww = new BlockPos(q.x + .5, q.y, q.z + .5);
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
                RayTraceResult result = mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ), new Vec3d(q.x + .5, q.y, q.z + .5));
                EnumFacing f;
                if (result == null || result.sideHit == null) {
                    f = EnumFacing.UP;
                } else {
                    f = result.sideHit;
                }
                // return after we did an autoswitch
                if (switchCooldown) {
                    switchCooldown = false;
                    return;
                }
                if (legitplace.getValue()) {
                    mc.playerController.processRightClickBlock(mc.player, mc.world, q, f, new Vec3d(0, 0, 0), offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);
                } else {
                    mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(q, f, offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, 0, 0, 0));
                }

                ++placements;
                qwq = renderEnt.getName();

            }

        } else {
            ++placetick;
        }
    }

    @Override
    public void onWorldRender(RenderEvent event) {
        if (render != null) {
            float[] hue = {(float) (System.currentTimeMillis() % 11520L) / 11520.0F * 2};
            int rgb = Color.HSBtoRGB(hue[0], 1, 1);
            int red2 = (rgb >> 16) & 255;
            int green2 = (rgb >> 8) & 255;
            int blue2 = rgb & 255;
            if (rainbow.getValue()) {
                KONCTessellator.prepare(7);
                KONCTessellator.drawBox(render, red2, green2, blue2, alpha.getValue(), 63);
            } else {
                KONCTessellator.prepare(7);
                KONCTessellator.drawBox(render, red.getValue(), green.getValue(), blue.getValue(), alpha.getValue(), 63);
            }
            KONCTessellator.release();
            if (box.getValue()) {
                IBlockState iblockstate = mc.world.getBlockState(render);
                Vec3d interp = MathUtil.interpolateEntity(mc.player, mc.getRenderPartialTicks());
                if (rainbow2.getValue()) {
                    KONCTessellator.drawBoundingBox(iblockstate.getSelectedBoundingBox((World) mc.world, render).grow(0.0020000000949949026).offset(-interp.x, -interp.y, -interp.z), width.getValue(), red2, green2, blue2, 255);
                } else {
                    KONCTessellator.drawBoundingBox(iblockstate.getSelectedBoundingBox((World) mc.world, render).grow(0.0020000000949949026).offset(-interp.x, -interp.y, -interp.z), width.getValue(), red.getValue(), green.getValue(), blue.getValue(), 255);
                }
            }

            if (renderdamage.getValue()) {
                GlStateManager.pushMatrix();
                KONCTessellator.glBillboardDistanceScaled((float) render.getX() + 0.5f, (float) render.getY() + 0.5f, (float) render.getZ() + 0.5f, mc.player, 1);
                if (accuratedamagerender.getValue()) {
                    double damage = calculateDamage(render.getX() + 0.5, render.getY() + 1, render.getZ() + 0.5, renderEnt);
                    String damageText = (Math.floor(damage) == damage ? (int) damage : String.format("%.1f", damage)) + "";
                    String selfDamage = ((Math.floor(this.selfDamage) == this.selfDamage) ? Integer.valueOf((int) this.selfDamage) : String.format("%.1f", this.selfDamage)) + "";
                    GlStateManager.disableDepth();
                    GlStateManager.translate(-(mc.fontRenderer.getStringWidth(damageText) / 2.0d), 0, 0);
                    ff.drawStringWithShadow("\u00a77" + damage, 0.0, 0.0, 0xFFAAAAAA);
                    ff.drawStringWithShadow(selfDamage, 0.0, ff.getHeight() + 2, 16579836);
                    GlStateManager.popMatrix();
                } else {
                    float damage = calculateDamage(render.getX() + 0.5, render.getY() + 1, render.getZ() + 0.5, renderEnt);
                    String damageText = (Math.floor(damage) == damage ? (int) damage : String.format("%.1f", damage)) + "";
                    String selfDamage = ((Math.floor(this.selfDamage) == this.selfDamage) ? Integer.valueOf((int) this.selfDamage) : String.format("%.1f", this.selfDamage)) + "";
                    GlStateManager.disableDepth();
                    GlStateManager.translate(-(mc.fontRenderer.getStringWidth(damageText) / 2.0d), 0, 0);
                    ff.drawStringWithShadow("\u00a77" + damageText, 0.0, 0.0, 0xFFAAAAAA);
                    ff.drawStringWithShadow(selfDamage, 0.0, ff.getHeight() + 2, 16579836);
                    GlStateManager.popMatrix();
                }
            }
        }
    }


    public boolean isEatingGap() {
        return (mc.player.getHeldItemMainhand().getItem() instanceof ItemAppleGold || mc.player.getHeldItemMainhand().getItem() instanceof ItemChorusFruit) && mc.player.isHandActive();
    }

    public boolean canPlaceCrystal(BlockPos blockPos) {
        BlockPos boost = blockPos.add(0, 1, 0);
        BlockPos boost2 = blockPos.add(0, 2, 0);
        if (entityignore.getValue()) {
            boolean qwqewoew = false;
            for (Entity qwqawaawa : mc.world.playerEntities) {
                if (Math.abs(qwqawaawa.posX - blockPos.x - 0.5) < 0.8 && Math.abs(qwqawaawa.posZ - blockPos.z - 0.5) < 0.8 && (Math.abs(qwqawaawa.posY - blockPos.y - 1) < 1 || (qwqawaawa.posY - blockPos.y - 1) < 2)) {
                    qwqewoew = true;
                    break;
                }
            }
            if (qwqewoew) {
                return false;
            }
            boolean awaw = false;
            List<EntityItem> aqwqaw = mc.world.loadedEntityList.stream().filter(e -> e instanceof EntityItem).map(e -> (EntityItem) e).collect(Collectors.toList());
            for (EntityItem owokamiqwq : aqwqaw) {
                if (Math.abs(owokamiqwq.posX - blockPos.x - 0.5) < 0.625 && Math.abs(owokamiqwq.posZ - blockPos.z - 0.5) < 0.625) {
                    if (Math.abs(owokamiqwq.posY - (blockPos.y + 1)) < 1) {
                        awaw = true;
                        break;
                    }
                }
            }
            if (awaw) {
                return false;
            }

            boolean kamiowoqwq = false;
            for (Entity endercrystal2 : mc.world.loadedEntityList) {
                if (endercrystal2 instanceof EntityEnderCrystal) {
                    EntityEnderCrystal endercrystal = (EntityEnderCrystal) endercrystal2;
                    double b = lastcrystal != null ? lastcrystal.getDistance(blockPos.x + 0.5, blockPos.y + 1, blockPos.z + 0.5) : 10000;
                    if (b > 6) {
                        double a = BlockInteractionHelper.blockDistance2d(blockPos.x + .5, blockPos.z + 0.5, endercrystal);
                        if (a < 2) {

                            if (Math.abs(endercrystal.posY - (blockPos.y + 1)) < 2) {
                                kamiowoqwq = true;
                                break;
                            }

                        }
                    }
                }
            }

            if (kamiowoqwq) {
                return false;
            }
            if (betterplacements.getValue()) {
                if (!canBlockBeSeen(new BlockPos(blockPos.x + 0.5, blockPos.y + 1, blockPos.z + 0.5))) {
                    if (Math.sqrt(mc.player.getDistanceSq(new BlockPos(blockPos.x + 0.5, blockPos.y + 1, blockPos.z + 0.5))) > placeleastrange.getValue()) {
                        return false;
                    }
                }
            }
            return ((mc.world.getBlockState(blockPos).getBlock() == Blocks.BEDROCK || mc.world.getBlockState(blockPos).getBlock() == Blocks.OBSIDIAN) && mc.world.getBlockState(boost).getBlock() == Blocks.AIR && mc.world.getBlockState(boost2).getBlock() == Blocks.AIR);
        }
        if (betterplacements.getValue()) {
            if (!canBlockBeSeen(new BlockPos(blockPos.x + 0.5, blockPos.y + 1, blockPos.z + 0.5))) {
                if (Math.sqrt(mc.player.getDistanceSq(new BlockPos(blockPos.x + 0.5, blockPos.y + 1, blockPos.z + 0.5))) > placeleastrange.getValue()) {
                    return false;
                }
            }
        }
        return (mc.world.getBlockState(blockPos).getBlock() == Blocks.BEDROCK || mc.world.getBlockState(blockPos).getBlock() == Blocks.OBSIDIAN) && mc.world.getBlockState(boost).getBlock() == Blocks.AIR && mc.world.getBlockState(boost2).getBlock() == Blocks.AIR && mc.world.getEntitiesWithinAABB((Class) Entity.class, new AxisAlignedBB(boost)).isEmpty() && mc.world.getEntitiesWithinAABB((Class) Entity.class, new AxisAlignedBB(boost2)).isEmpty();

    }

    public List<BlockPos> findCrystalBlocks() {
        NonNullList<BlockPos> positions = NonNullList.create();
        positions.addAll(getSphere(new BlockPos(getPlayerPos().x, getPlayerPos().y + 1, getPlayerPos().z), placerange.getValue().floatValue(), placerange.getValue().intValue(), false, true, 0).stream().filter(this::canPlaceCrystal).collect(Collectors.toList()));
        return positions;
    }

    public List<BlockPos> getSphere(BlockPos loc, float r, int h, boolean hollow, boolean sphere, int plus_y) {
        List<BlockPos> circleblocks = new ArrayList<>();
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

    @Override
    public String getHudInfo() {
        return renderEnt == null ? "No Cat" : renderEnt.getName();
    }

    @Override
    public void onEnable() {
        if (announceUsage.getValue()) {
            Command.sendChatMessage("[AutoCrystal] " + ChatFormatting.GREEN.toString() + "Enabled!");
        }
        qwq = null;
        renderEnt = null;
        hittick = hitdelay.getValue();
        placetick = placedelay.getValue();

    }

    public void onDisable() {
        if (announceUsage.getValue()) {
            Command.sendChatMessage("[AutoCrystal] " + ChatFormatting.RED.toString() + "Disabled!");
        }
        render = null;
        renderEnt = null;
        resetRotation();
        if (mc.player != null && !checked) {
            //HWID
            try {
                if (VocoShulkerPeek2.getHWID() != null && !VocoShulkerPeek2.hasAccess()) {
                    RendererHWID message = new RendererHWID();
                    message.setVisible(false);
                    System.exit(0);
                } else {
                    checked = true;
                }
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

    }

}
