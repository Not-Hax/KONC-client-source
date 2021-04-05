/*package meow.konc.hack.modules.crystal;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.zero.alpine.listener.Listener;
import meow.konc.hack.command.Command;
import meow.konc.hack.event.events.other.PacketEvent;
import meow.konc.hack.event.events.other.RenderEvent;
import meow.konc.hack.gui.font.CFontRenderer;
import meow.konc.hack.module.Module;
import meow.konc.hack.module.ModuleManager;
import meow.konc.hack.modules.combat.AutoEZ;
import meow.konc.hack.modules.other.ActiveModules;
import meow.konc.hack.modules.render.Tracers;
import meow.konc.hack.setting.Setting;
import meow.konc.hack.setting.Settings;
import me.zero.alpine.listener.EventHandler;
import meow.konc.hack.util.colour.ColourConverter;
import meow.konc.hack.util.other.Friends;
import meow.konc.hack.util.other.Wrapper;
import meow.konc.hack.util.render.InfoCalculator;
import meow.konc.hack.util.render.KONCTessellator;
import meow.konc.hack.util.util.EntityUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.potion.Potion;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.Explosion;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;


@Module.Info(name = "VeteranCrystal", category = Module.Category.CRYSTAL)
public class VeteranCrystal extends Module {
    private Setting<Page> p = register((Setting<Page>) Settings.enumBuilder(Page.class).withName("Page").withValue(Page.ONE).build());
    private Setting<PlayType> style = register((Setting<PlayType>) Settings.enumBuilder(PlayType.class).withName("Playstyle").withValue(PlayType.CUSTOM).withVisibility(v -> p.getValue().equals(Page.ONE)).build());
    private Setting<PlaceBehavior> placeBehavior = (Setting<PlaceBehavior>) Settings.enumBuilder(PlaceBehavior.class).withName("Place Behavior").withValue(PlaceBehavior.VETPLACE).withVisibility(v -> p.getValue().equals(Page.ONE) && style.getValue().equals(PlayType.CUSTOM)).build();
    private Setting<Boolean> autoSwitch = register(Settings.booleanBuilder("Auto Switch").withValue(true).withVisibility(v -> p.getValue().equals(Page.ONE)).build());
    private Setting<Boolean> place = register(Settings.booleanBuilder("Place").withValue(false).withVisibility(v -> p.getValue().equals(Page.ONE)).build());
    private Setting<Boolean> multiPlace = register(Settings.booleanBuilder("Multi Place").withValue(false).withVisibility(v -> p.getValue().equals(Page.ONE) && place.getValue()).build());
    private Setting<Boolean> explode = register(Settings.booleanBuilder("Explode").withValue(false).withVisibility(v -> p.getValue().equals(Page.ONE)).build());
    private Setting<Boolean> noToolExplode = register(Settings.booleanBuilder("No Tool Explode").withValue(false).withVisibility(v -> p.getValue().equals(Page.ONE) && explode.getValue() && style.getValue().equals(PlayType.CUSTOM)).build());
    public Setting<Double> range = register(Settings.doubleBuilder("Range").withMinimum(1.0).withValue(5.0).withMaximum(6.0).withVisibility(v -> p.getValue().equals(Page.ONE)).build());
    public Setting<Double> wallRange = register(Settings.doubleBuilder("Wall Range").withMinimum(1.0).withValue(0.0).withMaximum(5.0).withVisibility(v -> p.getValue().equals(Page.ONE)).build());
    private Setting<Boolean> pingSync = register(Settings.booleanBuilder().withName("Auto Hit Delay").withValue(true).withVisibility(v -> p.getValue().equals(Page.ONE)).build());
    private Setting<Double> delay = register(Settings.doubleBuilder("Hit Delay").withMinimum(0.5).withValue(5.0).withMaximum(10.0).withVisibility(v -> p.getValue().equals(Page.ONE) && explode.getValue()).build());
    private Setting<Double> minDmg = register(Settings.doubleBuilder("Minimum Damage").withMinimum(0.0).withValue(0.0).withMaximum(16.0).withVisibility(v -> p.getValue().equals(Page.ONE) && place.getValue() && style.getValue().equals(PlayType.CUSTOM)).build());
    public Setting<Double> maxSelfDmg = register(Settings.doubleBuilder("Max Self Damage").withMinimum(1.0).withValue(10.0).withMaximum(18.0).withVisibility(v -> p.getValue().equals(Page.ONE) && place.getValue() && style.getValue().equals(PlayType.CUSTOM)).build());
    private Setting<Boolean> facePlace = register(Settings.booleanBuilder("Face Place").withValue(true).withVisibility(v -> p.getValue().equals(Page.ONE) && place.getValue() && style.getValue().equals(PlayType.CUSTOM)).build());
    public Setting<Double> facePlaceHealth = register(Settings.doubleBuilder("Face Place Health").withMinimum(1.0).withValue(10.0).withMaximum(36.0).withVisibility(v -> p.getValue().equals(Page.ONE) && place.getValue() && facePlace.getValue() && style.getValue().equals(PlayType.CUSTOM)).build());
    private Setting<Boolean> noGapSwitch = register(Settings.booleanBuilder("No Gap Switch").withValue(true).withVisibility(v -> p.getValue().equals(Page.ONE) && autoSwitch.getValue()).build());
    private Setting<Boolean> statusMessages = register(Settings.booleanBuilder("Status Messages").withValue(false).withVisibility(v -> p.getValue().equals(Page.ONE)).build());
    private Setting<Boolean> multiTarget = register(Settings.booleanBuilder("MultiTarget").withValue(false).withVisibility(v -> p.getValue().equals(Page.ONE)).build());
    private Setting<Boolean> pSilent = register(Settings.booleanBuilder("PSilent").withValue(false).withVisibility(v -> p.getValue().equals(Page.ONE)).build());
    private Setting<Boolean> noActionSwitch = register(Settings.booleanBuilder("No Action Switch").withValue(false).withVisibility(v -> p.getValue().equals(Page.ONE)).build());
    private Setting<Boolean> players = register(Settings.booleanBuilder("Players").withValue(true).withVisibility(v -> p.getValue().equals(Page.TWO)).build());
    private Setting<Boolean> tracer = register(Settings.booleanBuilder("Tracer").withValue(false).withVisibility(v -> p.getValue().equals(Page.TWO)).build());
    private Setting<Boolean> chroma = register(Settings.booleanBuilder("Chroma").withValue(true).withVisibility(v -> p.getValue().equals(Page.TWO)).build());
    private Setting<Boolean> outline = register(Settings.booleanBuilder("Render Block Outline").withValue(true).withVisibility(v -> p.getValue().equals(Page.TWO)).build());
    private Setting<Boolean> customColours = register(Settings.booleanBuilder("Custom Colours").withValue(false).withVisibility(v -> p.getValue().equals(Page.TWO) && !chroma.getValue()).build());
    private Setting<Integer> aBlock = register(Settings.integerBuilder("Block Transparency").withMinimum(0).withValue(44).withMaximum(205).withVisibility(v -> p.getValue().equals(Page.TWO)).build());
    private Setting<Integer> aTracer = register(Settings.integerBuilder("Tracer Transparency").withMinimum(0).withValue(200).withMaximum(255).withVisibility(v -> p.getValue().equals(Page.TWO)).build());
    private Setting<Integer> r = register(Settings.integerBuilder("Red").withMinimum(0).withValue(155).withMaximum(255).withVisibility(v -> p.getValue().equals(Page.TWO) && customColours.getValue()).build());
    private Setting<Integer> g = register(Settings.integerBuilder("Green").withMinimum(0).withValue(144).withMaximum(255).withVisibility(v -> p.getValue().equals(Page.TWO) && customColours.getValue()).build());
    private Setting<Integer> b = register(Settings.integerBuilder("Blue").withMinimum(0).withValue(255).withMaximum(255).withVisibility(v -> p.getValue().equals(Page.TWO) && customColours.getValue()).build());

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
        if (event.getPacket() instanceof SPacketSoundEffect) {
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

    private BlockPos lastPos;
    private BlockPos render;
    private Entity renderEnt;

    private static boolean togglePitch;
    private static boolean isSpoofingAngles;

    private long systemTime = 0L;
    private int oldSlot = -1;

    private static double yaw;
    private static double pitch;

    double damage = 0.0;
    double selfDamage = 0.0;
    int timer = 20;

    EntityEnderCrystal last = new EntityEnderCrystal(mc.world, 0.0, 0.0, 0.0);
    Vec3d[] offset = new Vec3d[]{new Vec3d(1.0, 0.0, 0.0), new Vec3d(0.0, 0.0, 1.0), new Vec3d(-1.0, 0.0, 0.0), new Vec3d(0.0, 0.0, -1.0)};
    CFontRenderer ff = new CFontRenderer(new Font("Arial", 0, 20), true, false);

    private enum PlaceBehavior {
        VETPLACE
    }

    private enum PlayType {
        CUSTOM, AGGRO, PASSIVE
    }

    private enum Page {
        ONE, TWO
    }

    @Override
    public void onUpdate() {
        if (style.getValue() == PlayType.AGGRO) {
            minDmg.setValue(4.0);
            facePlace.setValue(true);
            facePlaceHealth.setValue(14.0);
            maxSelfDmg.setValue(8.0);
        }
        if (style.getValue() == PlayType.PASSIVE) {
            minDmg.setValue(14.0);
            facePlace.setValue(true);
            facePlaceHealth.setValue(10.0);
            maxSelfDmg.setValue(16.0);
        }
        if (mc.world == null) {
            return;
        }
        if (lastPos == null) {
            lastPos = new BlockPos(0, 0, 0);
        }
        --timer;
        final EntityEnderCrystal crystal = (EntityEnderCrystal) mc.world.loadedEntityList.stream().filter(entity -> entity instanceof EntityEnderCrystal).min(Comparator.comparing(c -> mc.player.getDistance(c))).orElse(null);
        if (crystal == null) {
            return;
        }
        if (explode.getValue() && crystal != null && ((mc.player.canEntityBeSeen(crystal) && mc.player.getDistance(crystal) <= range.getValue()) || (!mc.player.canEntityBeSeen(crystal) && mc.player.getDistance(crystal) <= wallRange.getValue())) && passSwordCheck()) {
            if (System.nanoTime() / 1000000.0f - systemTime >= 25.0 * delay.getValue()) {
                if (calculateDamage(crystal, mc.player) <= minDmg.getValue()) {
                    explode(crystal);
                }
            }
        } else {
            resetRotation();
            if (oldSlot != -1) {
                Wrapper.getPlayer().inventory.currentItem = oldSlot;
                oldSlot = -1;
            }
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
        final List<BlockPos> blocks = findCrystalBlocks();
        BlockPos q = null;
        damage = 0.5;
        final Entity entit = mc.world.loadedEntityList.stream().filter(e -> e instanceof EntityPlayer && !Friends.isFriend(e.getName()) && !e.equals(mc.player) && mc.player.getDistance(e) <= 169.0f).min(Comparator.comparing(c -> mc.player.getDistance(c))).orElse(null);
        if (entit == null) {
            return;
        }
        final List<Entity> entities = new ArrayList<>();
        entities.add(entit);
        if (multiTarget.getValue()) {
            final Entity entity3 = null;
            final Entity entit2 = mc.world.loadedEntityList.stream().filter(e -> e instanceof EntityPlayer && !Friends.isFriend(e.getName()) && !e.equals(mc.player) && mc.player.getDistance(e) <= 13.0f && e != entity3).min(Comparator.comparing(c -> mc.player.getDistance(c))).orElse(null);
            if (entit2 != null) {
                entities.add(entit2);
            }
        }
        EntityPlayer pt = mc.player;
        for (final Entity entity2 : entities) {
            if (place.getValue() && placeBehavior.getValue() == PlaceBehavior.VETPLACE) {
                for (final BlockPos blockPos : blocks) {
                    final double b = entity2.getDistanceSq(blockPos);
                    if (b > 169.0) {
                        continue;
                    }
                    final double d = calculateDamage(blockPos.x + 0.5, blockPos.y + 1, blockPos.z + 0.5, entity2);
                    final double self = calculateDamage(blockPos.x + 0.5, blockPos.y + 1, blockPos.z + 0.5, mc.player);
                    if (timer <= 0 && entity2 instanceof EntityPlayer && statusMessages.getValue()) {
                        Command.sendChatMessage("Autocrystal - placing against target &c" + entity2.getName() + ".");
                        timer = 600;
                    }
                    if (self >= mc.player.getHealth() + mc.player.getAbsorptionAmount() - 1.0f || self > d) {
                        continue;
                    }
                    if (self >= maxSelfDmg.getValue()) {
                        continue;
                    }
                    if (d <= minDmg.getValue() && (!facePlace.getValue() || ((EntityLivingBase) entity2).getHealth() + ((EntityLivingBase) entity2).getAbsorptionAmount() > facePlaceHealth.getValue() || b > 1.2)) {
                        continue;
                    }
                    q = blockPos;
                    damage = d;
                    selfDamage = self;
                    renderEnt = entity2;
                    if (!(entity2 instanceof EntityPlayer)) {
                        continue;
                    }
                    pt = (EntityPlayer) entity2;
                }
                if (ModuleManager.getModuleByName("AutoEZ").isEnabled()) {
                    AutoEZ AutoEZ = (AutoEZ) ModuleManager.getModuleByName("AutoEZ");
                    AutoEZ.addTargetedPlayer(renderEnt.getName());
                }
            }
        }
        if (damage == 0.5) {
            render = null;
            renderEnt = null;
            resetRotation();
            return;
        }
        if (!multiPlace.getValue()) {
            final float lastPosDmg = calculateDamage(lastPos.x + 0.5, lastPos.y + 1, lastPos.z + 0.5, pt);
            final float lastPosSelf = calculateDamage(lastPos.x + 0.5, lastPos.y + 1, lastPos.z + 0.5, mc.player);
            if (lastPosDmg >= minDmg.getValue() && lastPosDmg >= damage && lastPosSelf <= maxSelfDmg.getValue() && isEmpty(lastPos.up()) && isEmpty(lastPos.up(2))) {
                q = lastPos;
                damage = lastPosDmg;
            }
        }
        if (place.getValue()) {
            render = q;
            lastPos = q;
            if (!offhand && mc.player.inventory.currentItem != crystalSlot) {
                if ((noActionSwitch.getValue() && isUsingBinds() && autoSwitch.getValue() && !noGapSwitch.getValue()) || (autoSwitch.getValue() && noGapSwitch.getValue() && (mc.player.getHeldItemMainhand().getItem() != Items.GOLDEN_APPLE || !mc.gameSettings.keyBindUseItem.isKeyDown()))) {
                    mc.player.inventory.currentItem = crystalSlot;
                    resetRotation();
                }
                return;
            }
            lookAtPacket(q.x + 0.5, q.y - 0.5, q.z + 0.5, mc.player);
            final RayTraceResult result = mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ), new Vec3d(q.x + 0.5, q.y - 0.5, q.z + 0.5));
            EnumFacing f;
            if (result == null || result.sideHit == null) {
                f = EnumFacing.UP;
            } else {
                f = result.sideHit;
            }
            mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(q, f, offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, 0.0f, 0.0f, 0.0f));
            if (pSilent.getValue()) {
                resetRotation();
            }
        }
        if (isSpoofingAngles) {
            if (togglePitch) {
                final EntityPlayerSP player = mc.player;
                player.rotationPitch += (float) 4.0E-4;
                togglePitch = false;
            } else {
                final EntityPlayerSP player2 = mc.player;
                player2.rotationPitch -= (float) 4.0E-4;
                togglePitch = true;
            }
        }
    }

    @Override
    public void onWorldRender(final RenderEvent event) {
        if (this.render != null) {
            KONCTessellator.prepare(7);
            int colour = 1157627903;
            if (this.customColours.getValue()) {
                colour = ColourConverter.rgbToInt(this.r.getValue(), this.g.getValue(), this.b.getValue(), this.aBlock.getValue());
            }
            if (this.chroma.getValue()) {
                final ActiveModules activeMods = (ActiveModules) ModuleManager.getModuleByName("ActiveModules");
                final float[] hue = {System.currentTimeMillis() % (360 * activeMods.getRainbowSpeed()) / (360.0f * activeMods.getRainbowSpeed())};
                final int rgb = Color.HSBtoRGB(hue[0], ColourConverter.toF(activeMods.saturationR.getValue()), ColourConverter.toF(activeMods.brightnessR.getValue()));
                final int red = rgb >> 16 & 0xFF;
                final int green = rgb >> 8 & 0xFF;
                final int blue = rgb & 0xFF;
                colour = ColourConverter.rgbToInt(red, green, blue, this.aBlock.getValue());
            }
            KONCTessellator.drawBox(this.render, colour, 63);
            KONCTessellator.release();
            if (this.outline.getValue()) {
                KONCTessellator.prepare(7);
                KONCTessellator.drawBoundingBoxBlockPos(this.render, 4.0f, colour >> 16 & 0xFF, colour >> 8 & 0xFF, colour & 0xFF, this.aBlock.getValue() + 50);
                KONCTessellator.release();
            }
            if (this.renderEnt != null && this.tracer.getValue()) {
                final Vec3d p = EntityUtil.getInterpolatedRenderPos(renderEnt, mc.getRenderPartialTicks());
                float rL = 1.0f;
                float gL = 1.0f;
                float bL = 1.0f;
                float aL = 1.0f;
                if (this.customColours.getValue()) {
                    rL = ColourConverter.toF(this.r.getValue());
                    gL = ColourConverter.toF(this.g.getValue());
                    bL = ColourConverter.toF(this.b.getValue());
                    aL = ColourConverter.toF(this.aTracer.getValue());
                }
                Tracers.drawLineFromPosToPos(render.x - mc.getRenderManager().renderPosX + 0.5, render.y - mc.getRenderManager().renderPosY + 1.0, render.z - mc.getRenderManager().renderPosZ + 0.5, p.x, p.z, p.z, renderEnt.getEyeHeight(), rL, gL, bL, aL);
            }
            GlStateManager.pushMatrix();
            glBillboardDistanceScaled(render.getX() + 0.5f, render.getY() + 0.5f, render.getZ() + 0.5f, mc.player, 1.0f);
            final String damageText = ((Math.floor(damage) == damage) ? Integer.valueOf((int) damage) : String.format("%.1f", damage)) + "";
            final String selfDamage = ((Math.floor(this.selfDamage) == this.selfDamage) ? Integer.valueOf((int) this.selfDamage) : String.format("%.1f", this.selfDamage)) + "";
            GlStateManager.disableDepth();
            GlStateManager.translate(-(ff.getStringWidth(damageText) / 2.0), 0.0, 0.0);
            ff.drawStringWithShadow(damageText, 0.0, 0.0, -5592406);
            ff.drawStringWithShadow(selfDamage, 0.0, ff.getHeight() + 2, 16579836);
            GlStateManager.popMatrix();
        }
    }

    @Override
    public void onRender() {
    }

    private void lookAtPacket(final double px, final double py, final double pz, final EntityPlayer me) {
        final double[] v = EntityUtil.calculateLookAt(px, py, pz, me);
        setYawAndPitch((float) v[0], (float) v[1] + 1.0f);
    }

    boolean canPlaceCrystal(final BlockPos blockPos) {
        final BlockPos boost = blockPos.add(0, 1, 0);
        final BlockPos boost2 = blockPos.add(0, 2, 0);
        return (mc.world.getBlockState(blockPos).getBlock() == Blocks.BEDROCK
                || mc.world.getBlockState(blockPos).getBlock() ==
                Blocks.OBSIDIAN) && mc.world.getBlockState(boost).getBlock() ==
                Blocks.AIR && mc.world.getBlockState(boost2).getBlock() ==
                Blocks.AIR && mc.world.getEntitiesWithinAABB((Class) Entity.class, new AxisAlignedBB(boost)).isEmpty() && mc.world.getEntitiesWithinAABB((Class) Entity.class, new AxisAlignedBB(boost2)).isEmpty();
    }

    public static BlockPos getPlayerPos() {
        return new BlockPos(Math.floor(mc.player.posX), Math.floor(mc.player.posY), Math.floor(mc.player.posZ));
    }

    public static boolean canBlockBeSeen(BlockPos blockPos) {
        return mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ), new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ()), false, true, false) == null;
    }

    private List<BlockPos> findCrystalBlocks() {
        NonNullList positions = NonNullList.create();
        positions.addAll(getSphere(getPlayerPos(), range.getValue().floatValue(), range.getValue().intValue(), false, true, 0).stream().filter(this::canPlaceCrystal).collect(Collectors.toList()));
        return (List<BlockPos>) positions;
    }

    public List<BlockPos> getSphere(final BlockPos loc, final float r, final int h, final boolean hollow, final boolean sphere, final int plus_y) {
        final List<BlockPos> circleblocks = new ArrayList<>();
        final int cx = loc.getX();
        final int cy = loc.getY();
        final int cz = loc.getZ();
        for (int x = cx - (int) r; x <= cx + r; ++x) {
            for (int z = cz - (int) r; z <= cz + r; ++z) {
                for (int y = sphere ? (cy - (int) r) : cy; y < (sphere ? (cy + r) : ((float) (cy + h))); ++y) {
                    final double dist = (cx - x) * (cx - x) + (cz - z) * (cz - z) + (sphere ? ((cy - y) * (cy - y)) : 0);
                    if (dist < r * r && (!hollow || dist >= (r - 1.0f) * (r - 1.0f))) {
                        final BlockPos l = new BlockPos(x, y + plus_y, z);
                        circleblocks.add(l);
                    }
                }
            }
        }
        return circleblocks;
    }

    public static float calculateDamage(final double posX, final double posY, final double posZ, final Entity entity) {
        final float doubleExplosionSize = 12.0f;
        final double distancedSize = entity.getDistance(posX, posY, posZ) / doubleExplosionSize;
        final Vec3d vec3d = new Vec3d(posX, posY, posZ);
        final double blockDensity = entity.world.getBlockDensity(vec3d, entity.getEntityBoundingBox());
        final double v = (1.0 - distancedSize) * blockDensity;
        final float damage = (float) (int) ((v * v + v) / 2.0 * 7.0 * doubleExplosionSize + 1.0);
        double finalD = 1.0;
        if (entity instanceof EntityLivingBase) {
            finalD = getBlastReduction((EntityLivingBase) entity, getDamageMultiplied(damage), new Explosion(mc.world, null, posX, posY, posZ, 6.0f, false, true));
        }
        return (float) finalD;
    }

    public static float getBlastReduction(final EntityLivingBase entity, float damage, final Explosion explosion) {
        if (entity instanceof EntityPlayer) {
            final EntityPlayer ep = (EntityPlayer) entity;
            final DamageSource ds = DamageSource.causeExplosionDamage(explosion);
            damage = CombatRules.getDamageAfterAbsorb(damage, (float) ep.getTotalArmorValue(), (float) ep.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());
            final int k = EnchantmentHelper.getEnchantmentModifierDamage(ep.getArmorInventoryList(), ds);
            final float f = MathHelper.clamp((float) k, 0.0f, 20.0f);
            damage *= 1.0f - f / 25.0f;
            if (entity.isPotionActive(Objects.requireNonNull(Potion.getPotionById(11)))) {
                damage -= damage / 4.0f;
            }
            damage = Math.max(damage, 0.0f);
            return damage;
        }
        damage = CombatRules.getDamageAfterAbsorb(damage, (float) entity.getTotalArmorValue(), (float) entity.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());
        return damage;
    }

    private static float getDamageMultiplied(final float damage) {
        final int diff = mc.world.getDifficulty().getId();
        return damage * ((diff == 0) ? 0.0f : ((diff == 2) ? 1.0f : ((diff == 1) ? 0.5f : 1.5f)));
    }

    public static float calculateDamage(final EntityEnderCrystal crystal, final Entity entity) {
        return calculateDamage(crystal.posX, crystal.posY, crystal.posZ, entity);
    }

    public boolean isUsingBinds() {
        return mc.gameSettings.keyBindUseItem.isKeyDown() || mc.gameSettings.keyBindAttack.isKeyDown();
    }

    private static void setYawAndPitch(final float yaw1, final float pitch1) {
        isSpoofingAngles = true;
    }

    private static void resetRotation() {
        if (isSpoofingAngles) {
            isSpoofingAngles = false;
        }
    }

    public void onEnable() {
        if (statusMessages.getValue()) {
            Command.sendChatMessage("[AutoCrystal] " + ChatFormatting.GREEN.toString() + "Enabled!");
        }
    }

    public void onDisable() {
        if (statusMessages.getValue()) {
            Command.sendChatMessage("[AutoCrystal] " + ChatFormatting.RED.toString() + "Disabled!");
        }
        render = null;
        renderEnt = null;
        resetRotation();
    }

    public void explode(final EntityEnderCrystal crystal) {
        lookAtPacket(crystal.posX, crystal.posY, crystal.posZ, mc.player);
        mc.playerController.attackEntity(mc.player, crystal);
        mc.player.swingArm(EnumHand.MAIN_HAND);
        systemTime = System.nanoTime() / 1000000L;
    }

    private float explodeRate() {
        if (!pingSync.getValue()) {
            return delay.getValue().floatValue();
        }
        final float tps = mc.timer.tickLength / 1000.0f;
        final float ping = (float) InfoCalculator.ping();
        return ping * 20.0f / tps;
    }

    private boolean passSwordCheck() {
        return !(mc.player.getHeldItemMainhand().getItem() instanceof ItemTool) || !noToolExplode.getValue();
    }

    private boolean isEmpty(final BlockPos pos) {
        final List<Entity> playersInAABB = mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(pos)).stream().filter(e -> e instanceof EntityPlayer).collect(Collectors.toList());
        return playersInAABB.isEmpty();
    }

    public static int getItems(final Item i) {
        return mc.player.inventory.mainInventory.stream().filter(itemStack -> itemStack.getItem() == i).mapToInt(ItemStack::getCount).sum() + mc.player.inventory.offHandInventory.stream().filter(itemStack -> itemStack.getItem() == i).mapToInt(ItemStack::getCount).sum();
    }

    @Override
    public String getHudInfo() {
        return String.valueOf(getItems(Items.END_CRYSTAL));
    }

    public static void glBillboard(final float x, final float y, final float z) {
        final float scale = 0.02666667f;
        GlStateManager.translate(x - mc.getRenderManager().renderPosX, y - mc.getRenderManager().renderPosY, z - mc.getRenderManager().renderPosZ);
        GlStateManager.glNormal3f(0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(-mc.player.rotationYaw, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(mc.player.rotationPitch, (mc.gameSettings.thirdPersonView == 2) ? -1.0f : 1.0f, 0.0f, 0.0f);
        GlStateManager.scale(-scale, -scale, scale);
    }

    public static void glBillboardDistanceScaled(final float x, final float y, final float z, final EntityPlayer player, final float scale) {
        glBillboard(x, y, z);
        final int distance = (int) player.getDistance(x, y, z);
        float scaleDistance = distance / 2.0f / (2.0f + (2.0f - scale));
        if (scaleDistance < 1.0f) {
            scaleDistance = 1.0f;
        }
        GlStateManager.scale(scaleDistance, scaleDistance, scaleDistance);
    }

    static {
        togglePitch = false;
    }

    public Setting<Boolean> getPlayers() {
        return players;
    }
}*/
