package meow.konc.hack.modules.combat;

import meow.konc.hack.module.Module;
import meow.konc.hack.setting.Setting;
import meow.konc.hack.setting.Settings;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import static meow.konc.hack.modules.other.InfoOverlay.getItems;

@Module.Info(name = "AutoTotem", category = Module.Category.COMBAT, description = "Refills your offhand with totems or other items")
public class AutoTotem extends Module {
    private Setting<Mode> modeSetting = register(Settings.e("Mode", Mode.REPLACE_OFFHAND));
    private Setting<Boolean> hotbar = register(Settings.b("HotBar", false));
    private Setting<Boolean> smartOffhand = register(Settings.booleanBuilder("Custom Item").withValue(false).withVisibility(v -> modeSetting.getValue().equals(Mode.REPLACE_OFFHAND)).build());
    private Setting<Double> healthSetting = register(Settings.doubleBuilder("Custom Item Health").withValue(14.0).withVisibility(v -> smartOffhand.getValue() && modeSetting.getValue().equals(Mode.REPLACE_OFFHAND)).build());
    private Setting<CustomItem> smartItemSetting = register(Settings.enumBuilder(CustomItem.class).withName("Item").withValue(CustomItem.GAPPLE).withVisibility(v -> smartOffhand.getValue()).build());

    public static float healthSetting() {
        return 0;
    }

    private enum Mode {NEITHER, REPLACE_OFFHAND, INVENTORY;}

    private enum CustomItem {CRYSTAL, GAPPLE}

    int totems;
    boolean moving = false;
    boolean returnI = false;

    @Override
    public void onUpdate() {
        if (!modeSetting.getValue().equals(Mode.INVENTORY) && mc.currentScreen instanceof GuiContainer)
            return;
        if (returnI) {
            int t = -1;
            for (int i = 0; i < 45; i++)
                if (mc.player.inventory.getStackInSlot(i).isEmpty) {
                    t = i;
                    break;
                }
            if (t == -1) return;
            mc.playerController.windowClick(0, t < 9 ? t + 36 : t, 0, ClickType.PICKUP, mc.player);
            returnI = false;
        }
        totems = mc.player.inventory.mainInventory.stream().filter(itemStack -> itemStack.getItem() == settingToItem()).mapToInt(ItemStack::getCount).sum();
        if (mc.player.getHeldItemOffhand().getItem() == settingToItem()) totems++;
        else {
            if (!modeSetting.getValue().equals(Mode.REPLACE_OFFHAND) && !mc.player.getHeldItemOffhand().isEmpty)
                return;
            if (moving) {
                mc.playerController.windowClick(0, 45, 0, ClickType.PICKUP, mc.player);
                moving = false;
                if (!mc.player.inventory.itemStack.isEmpty()) returnI = true;
                return;
            }
            if (mc.player.inventory.itemStack.isEmpty()) {
                if (totems == 0) return;
                int t = -1;
                for (int i = 0; i < 45; i++)
                    if (mc.player.inventory.getStackInSlot(i).getItem() == settingToItem()) {
                        t = i;
                        break;
                    }
                if (t == -1) return; // Should never happen!
                mc.playerController.windowClick(0, t < 9 ? t + 36 : t, 0, ClickType.PICKUP, mc.player);
                moving = true;
            } else if (modeSetting.getValue().equals(Mode.REPLACE_OFFHAND)) {
                int t = -1;
                for (int i = 0; i < 45; i++)
                    if (mc.player.inventory.getStackInSlot(i).isEmpty) {
                        t = i;
                        break;
                    }
                if (t == -1) return;
                mc.playerController.windowClick(0, t < 9 ? t + 36 : t, 0, ClickType.PICKUP, mc.player);
            }
        }
        if (hotbar.getValue()) {
            if (mc.player.inventory.getStackInSlot(0).getItem() == Items.TOTEM_OF_UNDYING) {
                return;
            }
            for (int i = 9; i < 35; ++i) {
                if (mc.player.inventory.getStackInSlot(i).getItem() == Items.TOTEM_OF_UNDYING) {
                    mc.playerController.windowClick(mc.player.inventoryContainer.windowId, i, 0, ClickType.SWAP, (EntityPlayer) mc.player);
                    mc.playerController.windowClick(mc. player.inventoryContainer.windowId, 45, 0, ClickType.PICKUP, (EntityPlayer)mc.player);
                    break;
                }
            }
        }
    }


    private Item settingToItem() {
        if (!smartOffhand.getValue() || passHealthCheck()) return Items.TOTEM_OF_UNDYING;
        switch (smartItemSetting.getValue()) {
            case GAPPLE:
                return Items.GOLDEN_APPLE;
            case CRYSTAL:
                return Items.END_CRYSTAL;
        }
        return null;
    }

    private boolean passHealthCheck() {
        if (modeSetting.getValue().equals(Mode.REPLACE_OFFHAND)) {
            return mc.player.getHealth() + mc.player.getAbsorptionAmount() <= healthSetting.getValue();
        }
        return true;
    }

    @Override
    public String getHudInfo() {
        return "" + getItems(settingToItem());
    }
}
