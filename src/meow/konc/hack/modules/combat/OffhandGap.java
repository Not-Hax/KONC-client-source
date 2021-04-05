package meow.konc.hack.modules.combat;

import meow.konc.hack.event.events.other.PacketEvent;
import meow.konc.hack.module.Module;
import meow.konc.hack.module.ModuleManager;
import meow.konc.hack.setting.Setting;
import meow.konc.hack.setting.Settings;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.*;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;

import java.util.Comparator;
import java.util.Objects;

import static meow.konc.hack.modules.other.InfoOverlay.getItems;

@Module.Info(name = "OffHandGap", category = Module.Category.COMBAT, description = "Holds a God apple when right clicking your sword!")
public class OffhandGap extends Module {
    private Setting<Boolean> autoapple = register(Settings.b("AutoApple", false));
    private Setting<Boolean> soft = register(Settings.booleanBuilder("Soft").withValue(false).withVisibility(v -> autoapple.getValue()).build());
    private Setting<Boolean> totemdisable = register(Settings.booleanBuilder("TotemOnDisable").withValue(false).withVisibility(v -> autoapple.getValue()).build());
    private Setting<Boolean> offhandcaDisable = register(Settings.b("OffhandCADisable", false));

    private Setting<Double> disableHealth = register(Settings.doubleBuilder("Disable Health").withMinimum(0.0).withValue(4.0).withMaximum(20.0).build());
    private Setting<Boolean> eatWhileAttacking = register(Settings.b("Eat While Attacking", true));
    private Setting<Boolean> swordOrAxeOnly = register(Settings.b("Sword or Axe Only", true));
    private Setting<Boolean> preferBlocks = register(Settings.booleanBuilder("Prefer Placing Blocks").withValue(false).withVisibility(v -> !swordOrAxeOnly.getValue()).build());
    private Setting<Boolean> crystalCheck = register(Settings.b("Crystal Check", true));

    int gaps = -1;
    boolean autoTotemWasEnabled = false;
    boolean cancelled = false;
    Item usedItem;
    Item toUseItem;
    private int gapples;
    private boolean moving = false;
    private boolean returnI = false;
    meow.konc.hack.modules.hidden.util.KamiCrystal KamiCrystal;

    @Override
    public void onEnable() {
        if (totemdisable.getValue()) {
            ModuleManager.getModuleByName("AutoTotem").disable();
        } else {
            return;
        }
        if (offhandcaDisable.getValue()) {
            ModuleManager.getModuleByName("AutoCrystal").disable();
        } else {
            return;
        }
    }

    @Override
    public void onDisable() {
        if (totemdisable.getValue()) {
            ModuleManager.getModuleByName("AutoTotem").enable();
        } else {
            return;
        }
        if (offhandcaDisable.getValue()) {
            ModuleManager.getModuleByName("AutoCrystal").enable();
        } else {
            return;
        }
    }

    @EventHandler
    private Listener<PacketEvent.Send> sendListener = new Listener<>(e -> {
        if (e.getPacket() instanceof CPacketPlayerTryUseItem) {
            if (cancelled) {
                disableGaps();
                return;
            }
            if (mc.player.getHeldItemMainhand().getItem() instanceof ItemSword || mc.player.getHeldItemMainhand().getItem() instanceof ItemAxe || passItemCheck()) {
                if (ModuleManager.isModuleEnabled("AutoTotem")) {
                    autoTotemWasEnabled = true;
                    ModuleManager.getModuleByName("AutoTotem").disable();
                }
                if (!eatWhileAttacking.getValue()) {
                    usedItem = mc.player.getHeldItemMainhand().getItem();
                }
                enableGaps(gaps);
            }
        }
        try {
            /* If you stop holding right click move totem back */
            if (!mc.gameSettings.keyBindUseItem.isKeyDown() && mc.player.getHeldItemOffhand().getItem() == Items.GOLDEN_APPLE)
                disableGaps();
                /* In case you didn't stop right clicking but you switched items by scrolling or something */
            else if ((usedItem != mc.player.getHeldItemMainhand().getItem()) && mc.player.getHeldItemOffhand().getItem() == Items.GOLDEN_APPLE) {
                if (!eatWhileAttacking.getValue()) {
                    usedItem = mc.player.getHeldItemMainhand().getItem();
                    disableGaps();
                }
            }
            /* Force disable if under health limit */
            else if (mc.player.getHealth() + mc.player.getAbsorptionAmount() <= disableHealth.getValue()) {
                disableGaps();
            }

            if (crystalCheck.getValue() && KamiCrystal.isEnabled()) {
                EntityEnderCrystal crystal = mc.world.loadedEntityList.stream()
                        .filter(entity -> entity instanceof EntityEnderCrystal)
                        .map(entity -> (EntityEnderCrystal) entity)
                        .min(Comparator.comparing(c -> mc.player.getDistance(c)))
                        .orElse(null);
                if (Objects.requireNonNull(crystal).getPosition().distanceSq(mc.player.getPosition().x, mc.player.getPosition().y, mc.player.getPosition().z) <= KamiCrystal.range.getValue()) {
                    disableGaps();
                }
            }
        } catch (NullPointerException ignored) {
        }
    });

    @Override
    public void onUpdate() {
        if (mc.player == null) return;

        /* If your health doesn't meet the cutoff then set it to true */
        cancelled = mc.player.getHealth() + mc.player.getAbsorptionAmount() <= disableHealth.getValue();
        if (cancelled) {
            disableGaps();
            return;
        }

        toUseItem = Items.GOLDEN_APPLE;
        if (mc.player.getHeldItemOffhand().getItem() != Items.GOLDEN_APPLE) {
            for (int i = 0; i < 45; i++) {
                if (mc.player.inventory.getStackInSlot(i).getItem() == Items.GOLDEN_APPLE) {
                    gaps = i;
                    break;
                }
            }
        }

        if (autoapple.getValue()) {
            cancelled = mc.player.getHealth() + mc.player.getAbsorptionAmount() <= disableHealth.getValue();
            if (cancelled) {
                disableGaps();
                return;
            }
            if (mc.currentScreen instanceof GuiContainer) {
                return;
            }
            if (returnI) {
                int t = -1;
                for (int i = 0; i < 45; i++) {
                    if (mc.player.inventory.getStackInSlot(i).isEmpty) {
                        t = i;
                        break;
                    }
                }
                if (t == -1) {
                    return;
                }
                mc.playerController.windowClick(0, t < 9 ? t + 36 : t, 0, ClickType.PICKUP, mc.player);
                returnI = false;
            }
            gapples = mc.player.inventory.mainInventory.stream().filter(itemStack -> itemStack.getItem() == Items.GOLDEN_APPLE).mapToInt(ItemStack::getCount).sum();
            if (mc.player.getHeldItemOffhand().getItem() == Items.GOLDEN_APPLE) {
                gapples++;
            } else {
                if (soft.getValue() && !mc.player.getHeldItemOffhand().isEmpty) {
                    return;
                }
                if (moving) {
                    mc.playerController.windowClick(0, 45, 0, ClickType.PICKUP, mc.player);
                    moving = false;
                    if (!mc.player.inventory.itemStack.isEmpty()) {
                        returnI = true;
                    }
                    return;
                }
                if (mc.player.inventory.itemStack.isEmpty()) {
                    if (gapples == 0) {
                        return;
                    }
                    int t = -1;
                    for (int i = 0; i < 45; i++) {
                        if (mc.player.inventory.getStackInSlot(i).getItem() == Items.GOLDEN_APPLE) {
                            t = i;
                            break;
                        }
                    }
                    if (t == -1) {
                        return; // Should never happen!
                    }
                    mc.playerController.windowClick(0, t < 9 ? t + 36 : t, 0, ClickType.PICKUP, mc.player);
                    moving = true;
                } else if (!soft.getValue()) {
                    int t = -1;
                    for (int i = 0; i < 45; i++) {
                        if (mc.player.inventory.getStackInSlot(i).isEmpty) {
                            t = i;
                            break;
                        }
                    }
                    if (t == -1) {
                        return;
                    }
                    mc.playerController.windowClick(0, t < 9 ? t + 36 : t, 0, ClickType.PICKUP, mc.player);
                }
            }
        }
    }

    private boolean passItemCheck() {
        if (swordOrAxeOnly.getValue()) return false;
        else {
            Item item = mc.player.getHeldItemMainhand().getItem();
            if (item instanceof ItemBow) return false;
            if (item instanceof ItemSnowball) return false;
            if (item instanceof ItemEgg) return false;
            if (item instanceof ItemPotion) return false;
            if (item instanceof ItemEnderEye) return false;
            if (item instanceof ItemEnderPearl) return false;
            if (item instanceof ItemFood) return false;
            if (item instanceof ItemShield) return false;
            if (item instanceof ItemFlintAndSteel) return false;
            if (item instanceof ItemFishingRod) return false;
            if (item instanceof ItemArmor) return false;
            if (item instanceof ItemExpBottle) return false;
            if (preferBlocks.getValue() && item instanceof ItemBlock) return false;
        }
        return true;
    }

    private void disableGaps() {
        if (autoTotemWasEnabled != ModuleManager.isModuleEnabled("AutoTotem")) {
            moveGapsToInventory(gaps);
            ModuleManager.getModuleByName("AutoTotem").enable();
            autoTotemWasEnabled = false;
        }
    }

    private void enableGaps(int slot) {
        if (mc.player.getHeldItemOffhand().getItem() != Items.GOLDEN_APPLE) {
            mc.playerController.windowClick(0, slot < 9 ? slot + 36 : slot, 0, ClickType.PICKUP, mc.player);
            mc.playerController.windowClick(0, 45, 0, ClickType.PICKUP, mc.player);
        }
    }

    private void moveGapsToInventory(int slot) {
        if (mc.player.getHeldItemOffhand().getItem() == Items.GOLDEN_APPLE) {
            mc.playerController.windowClick(0, 45, 0, ClickType.PICKUP, mc.player);
            mc.playerController.windowClick(0, slot < 9 ? slot + 36 : slot, 0, ClickType.PICKUP, mc.player);
        }
    }

    @Override
    public String getHudInfo() {
        return String.valueOf(getItems(Items.GOLDEN_APPLE));
    }
}
