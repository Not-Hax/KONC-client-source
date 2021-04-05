/*package meow.konc.hack.modules.crystal;

import meow.konc.hack.module.Module;
import meow.konc.hack.setting.Setting;
import meow.konc.hack.setting.Settings;
import meow.konc.hack.util.other.Friends;
import meow.konc.hack.util.render.BlockInteractionHelper;
import meow.konc.hack.util.util.RenderUtils;
import meow.konc.hack.util.util.WorldUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.util.CombatRules;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@Module.Info(name = "BDCrystal", category = Module.Category.CRYSTAL
)
public class BDCrystal extends Module {
   private Setting<Double> Tick = register(Settings.d("Place Per Tick", 1.0D));
   private Setting<Boolean> Place = register(Settings.b("Place", true));
   private Setting<Boolean> Break = register(Settings.b("Break", true));
   private Setting<Boolean> Switch = register(Settings.b("Switch", true));
   private Setting<Boolean> noGapple = register(Settings.b("No Gapple Switch", true));
   private Setting<Boolean> Mining = register(Settings.b("Don't cancel mining", true));
   private Setting<Float> placeRange = register(Settings.f("Place Range", 4.0F));
   private Setting<Double> breakRange = register(Settings.d("Break Range", 4.0D));
   private Setting<Double> WallsRange = register(Settings.d("Raytrace Place Range", 3.0D));
   private Setting<Double> minDamage = register(Settings.d("Min Damage", 4.0D));

   private BlockPos currentTarget;
   private Entity currentEntTarget;
   private long systemTime = -1L;
   private double[] damage = new double[]{0.0D, 0.0D};

   public void onUpdate() {
      if (isEnabled()) {
         if (Break.getValue()) {
            breakCrystals();
         }

         if (Place.getValue()) {
            for(int i = 0; (double)i < Tick.getValue(); ++i) {
               placeCrystals();
            }
         }

      }
   }

   private void placeCrystals() {
      currentTarget = null;
      currentEntTarget = null;
      boolean gapplingAllow = !noGapple.getValue() || mc.player.getActiveItemStack().getItem() != Items.GOLDEN_APPLE;
      boolean miningAllow = !Mining.getValue() || mc.player.getActiveItemStack().getItem() != Items.DIAMOND_PICKAXE;
      if (gapplingAllow && miningAllow) {
         List validBlocks = findAvailableCrystalBlocks();
         List targets = new ArrayList();
         Iterator var5 = mc.world.playerEntities.iterator();

         while(var5.hasNext()) {
            EntityPlayer player = (EntityPlayer)var5.next();
            if (!Friends.isFriend(player.getName())) {
               targets.add(player);
            }
         }

         double bestDamage = 0.1D;
         double bestSelfDamage = 1000.0D;
         BlockPos bestSpot = null;
         Iterator var10 = targets.iterator();

         label132:
         while(true) {
            EntityPlayer player2;
            do {
               do {
                  if (!var10.hasNext()) {
                     if (bestDamage >= minDamage.getValue() && bestSpot != null) {
                        boolean isHoldingCrystal = mc.player.getHeldItemMainhand().getItem() == Items.END_CRYSTAL || mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL;
                        if (!isHoldingCrystal && !Switch.getValue()) {
                           return;
                        }

                        if (!isHoldingCrystal) {
                           int index = WorldUtils.findItem(Items.END_CRYSTAL);
                           if (index >= 0) {
                              mc.player.inventory.currentItem = index;
                           }

                           return;
                        }

                        damage[0] = bestDamage;
                        damage[1] = bestSelfDamage;
                        RayTraceResult result2 = mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + (double)mc.player.getEyeHeight(), mc.player.posZ), new Vec3d((double)bestSpot.getX() + 0.5D, (double)bestSpot.getY() - 0.5D, (double)bestSpot.getZ() + 0.5D));
                        EnumFacing face;
                        if (result2 != null && result2.sideHit != null) {
                           face = result2.sideHit;
                        } else {
                           face = EnumFacing.UP;
                        }

                        EnumHand hand = EnumHand.MAIN_HAND;
                        if (mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL) {
                           hand = EnumHand.OFF_HAND;
                        }

                        Vec3d hitVec = (new Vec3d(bestSpot)).add(0.5D, 0.5D, 0.5D).add((new Vec3d(face.getDirectionVec())).scale(0.5D));
                        mc.playerController.processRightClickBlock(mc.player, mc.world, bestSpot, face, hitVec, hand);
                        mc.player.swingArm(hand);
                        return;
                     }

                     return;
                  }

                  player2 = (EntityPlayer)var10.next();
               } while(player2.getUniqueID().equals(mc.player.getUniqueID()));
            } while(player2.isDead);

            Iterator var12 = validBlocks.iterator();

            while(true) {
               BlockPos blockPos;
               double enemyDamage;
               double selfDamage;
               do {
                  do {
                     if (!var12.hasNext()) {
                        continue label132;
                     }

                     blockPos = (BlockPos)var12.next();
                  } while(player2.getDistanceSq(blockPos) >= 169.0D);

                  enemyDamage = calculateDamage((double)blockPos.getX() + 0.5D, blockPos.getY() + 1, (double)blockPos.getZ() + 0.5D, player2) / 10.0F;
                  selfDamage = calculateDamage((double)blockPos.getX() + 0.5D, blockPos.getY() + 1, (double)blockPos.getZ() + 0.5D, mc.player) / 10.0F;
               } while(enemyDamage < minDamage.getValue());

               boolean matchesThroughWallsRange;
               RayTraceResult result = mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + (double)mc.player.getEyeHeight(), mc.player.posZ), new Vec3d((double)blockPos.getX() + 0.5D, (double)blockPos.getY() - 0.5D, (double)blockPos.getZ() + 0.5D));
               matchesThroughWallsRange = result != null && result.typeOfHit == Type.BLOCK || mc.player.getDistance(blockPos.getX(), blockPos.getY(), blockPos.getZ()) <= WallsRange.getValue();
               boolean matchesRange = mc.player.getDistance(blockPos.getX(), blockPos.getY(), blockPos.getZ()) <= (double) placeRange.getValue();
               if (matchesRange && matchesThroughWallsRange) {
                  if (enemyDamage > bestDamage) {
                     bestDamage = enemyDamage;
                     bestSelfDamage = selfDamage;
                     bestSpot = blockPos;
                     currentEntTarget = player2;
                     currentTarget = blockPos;
                  } else if (enemyDamage == bestDamage && selfDamage < bestSelfDamage) {
                     bestDamage = enemyDamage;
                     bestSelfDamage = selfDamage;
                     bestSpot = blockPos;
                     currentEntTarget = player2;
                     currentTarget = blockPos;
                  }
               }
            }
         }
      }
   }

   private void breakCrystals() {
      EntityEnderCrystal crystal = (EntityEnderCrystal) mc.world.loadedEntityList.stream().filter((entity) -> entity instanceof EntityEnderCrystal).min(Comparator.comparing((c) -> mc.player.getDistance(c))).orElse(null);
      if (crystal != null && (double)mc.player.getDistance(crystal) <= breakRange.getValue() && System.nanoTime() / 1000000L - systemTime >= 250L) {
         mc.playerController.attackEntity(mc.player, crystal);
         mc.player.swingArm(EnumHand.MAIN_HAND);
         systemTime = System.nanoTime() / 1000000L;
      }

   }

   @SubscribeEvent
   public void onRenderWorld(RenderWorldLastEvent event) {
      RenderUtils.glStart(255.0F, 255.0F, 255.0F, 1.0F);
      if (currentTarget != null) {
         AxisAlignedBB bb = RenderUtils.getBoundingBox(currentTarget);
         RenderUtils.drawOutlinedBox(bb);
      }

      if (currentEntTarget != null) {
         RenderUtils.drawOutlinedBox(currentEntTarget.getEntityBoundingBox());
      }

      RenderUtils.glEnd();
   }

   public void onRender() {
   }

   public void onDisabled() {
      currentTarget = null;
      currentEntTarget = null;
   }

   private List findAvailableCrystalBlocks() {
      NonNullList<BlockPos> positions = NonNullList.create();
      positions.addAll(BlockInteractionHelper.getSphere(getPlayerPos(), placeRange.getValue().floatValue(), placeRange.getValue().intValue(), false, true, 0).stream().filter(this::canPlaceCrystal).collect(Collectors.toList()));
      return positions;
   }

   private List getSphere(BlockPos loc, float r, int h, boolean hollow, boolean sphere, int plus_y) {
      List circleblocks = new ArrayList();
      int cx = loc.getX();
      int cy = loc.getY();
      int cz = loc.getZ();

      for(int x = cx - (int)r; (float)x <= (float)cx + r; ++x) {
         for(int z = cz - (int)r; (float)z <= (float)cz + r; ++z) {
            for(int y = sphere ? cy - (int)r : cy; (float)y < (sphere ? (float)cy + r : (float)(cy + h)); ++y) {
               double dist = (cx - x) * (cx - x) + (cz - z) * (cz - z) + (sphere ? (cy - y) * (cy - y) : 0);
               if (dist < (double)(r * r) && (!hollow || dist >= (double)((r - 1.0F) * (r - 1.0F)))) {
                  BlockPos l = new BlockPos(x, y + plus_y, z);
                  circleblocks.add(l);
               }
            }
         }
      }

      return circleblocks;
   }

   private boolean canPlaceCrystal(BlockPos blockPos) {
      BlockPos boost = blockPos.add(0, 1, 0);
      BlockPos boost2 = blockPos.add(0, 2, 0);
      return (mc.world.getBlockState(blockPos).getBlock() == Blocks.BEDROCK || mc.world.getBlockState(blockPos).getBlock() == Blocks.OBSIDIAN) && mc.world.getBlockState(boost).getBlock() == Blocks.AIR && mc.world.getBlockState(boost2).getBlock() == Blocks.AIR && mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost)).isEmpty();
   }

   private static float calculateDamage(double posX, double posY, double posZ, Entity entity) {
      double distancedsize = entity.getDistance(posX, posY, posZ) / 12.0D;
      Vec3d vec3d = new Vec3d(posX, posY, posZ);
      double blockDensity = entity.world.getBlockDensity(vec3d, entity.getEntityBoundingBox());
      double v = (1.0D - distancedsize) * blockDensity;
      float damage = (float)((int)((v * v + v) / 2.0D * 7.0D * 12.0D + 1.0D));
      double finald = 1.0D;
      if (entity instanceof EntityLivingBase) {
         finald = getBlastReduction((EntityLivingBase)entity, getDamageMultiplied(damage), new Explosion(mc.world, null, posX, posY, posZ, 6.0F, false, true));
      }

      return (float)finald;
   }

   private static float getBlastReduction(EntityLivingBase entity, float damage, Explosion explosion) {
      if (entity instanceof EntityPlayer) {
         EntityPlayer ep = (EntityPlayer)entity;
         damage = CombatRules.getDamageAfterAbsorb(damage, (float)ep.getTotalArmorValue(), (float)ep.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());
         return damage;
      } else {
         damage = CombatRules.getDamageAfterAbsorb(damage, (float)entity.getTotalArmorValue(), (float)entity.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());
         return damage;
      }
   }

   public static BlockPos getPlayerPos() {
      return new BlockPos(Math.floor(mc.player.posX), Math.floor(mc.player.posY), Math.floor(mc.player.posZ));
   }

   private static float getDamageMultiplied(float damage) {
      int diff = mc.world.getDifficulty().getId();
      return damage * (diff == 0 ? 0.0F : (diff == 2 ? 1.0F : (diff == 1 ? 0.5F : 1.5F)));
   }

   private static Float lambdabreakCrystals2(EntityEnderCrystal c) {
      return mc.player.getDistance(c);
   }

   private static EntityEnderCrystal lambdabreakCrystals1(Entity entity) {
      return (EntityEnderCrystal)entity;
   }

   private static boolean lambdabreakCrystals0(Entity entity) {
      return entity instanceof EntityEnderCrystal;
   }

   public static final class GeometryMasks {
      public final class Line {
         static final int DOWN_WEST = 17;
         static final int UP_WEST = 18;
         static final int DOWN_EAST = 33;
         static final int UP_EAST = 34;
         static final int DOWN_NORTH = 5;
         static final int UP_NORTH = 6;
         static final int DOWN_SOUTH = 9;
         static final int UP_SOUTH = 10;
         static final int NORTH_WEST = 20;
         static final int NORTH_EAST = 36;
         static final int SOUTH_WEST = 24;
         static final int SOUTH_EAST = 40;
         static final int ALL = 63;
         final GeometryMasks this0;

         public Line(GeometryMasks this0, GeometryMasks this01) {
            this.this0 = this01;
            this0 = this0;
         }
      }

      final class Quad {
         static final int DOWN = 1;
         static final int UP = 2;
         static final int NORTH = 4;
         static final int SOUTH = 8;
         static final int WEST = 16;
         static final int EAST = 32;
         static final int ALL = 63;
         final GeometryMasks this0;

         Quad(GeometryMasks this0, GeometryMasks this01) {
            this.this0 = this01;
            this0 = this0;
         }
      }
   }
}*/
