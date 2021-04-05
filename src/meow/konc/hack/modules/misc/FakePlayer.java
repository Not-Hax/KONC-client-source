package meow.konc.hack.modules.misc;

import com.mojang.authlib.GameProfile;
import meow.konc.hack.module.Module;
import meow.konc.hack.setting.Setting;
import meow.konc.hack.setting.Settings;
import net.minecraft.client.entity.EntityOtherPlayerMP;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Module.Info(name = "FakePlayer", category = Module.Category.MISC, description = "Spawns a fake Player")
public class FakePlayer extends Module {
    private Setting<SpawnMode> spawnMode = register(Settings.e("Spawn Mode", SpawnMode.SINGLE));

    private List<Integer> fakePlayerIdList = null;
    private static final String[][] fakePlayerInfo;

    @Override
    protected void onEnable() {
        if (mc.player == null || mc.world == null) {
            disable();
            return;
        }
        fakePlayerIdList = new ArrayList<Integer>();
        int entityId = -101;
        for (final String[] data : fakePlayerInfo) {
            if (spawnMode.getValue().equals(SpawnMode.SINGLE)) {
                addFakePlayer(data[0], data[1], entityId, 0, 0);
                break;
            }
            addFakePlayer(data[0], data[1], entityId, Integer.parseInt(data[2]), Integer.parseInt(data[3]));
            --entityId;
        }
    }

    private void addFakePlayer(final String uuid, final String name, final int entityId, final int offsetX, final int offsetZ) {
        final EntityOtherPlayerMP fakePlayer = new EntityOtherPlayerMP(mc.world, new GameProfile(UUID.fromString(uuid), name));
        fakePlayer.copyLocationAndAnglesFrom(mc.player);
        fakePlayer.posX += offsetX;
        fakePlayer.posZ += offsetZ;
        mc.world.addEntityToWorld(entityId, fakePlayer);
        fakePlayerIdList.add(entityId);
    }

    @Override
    public void onUpdate() {
        if (fakePlayerIdList == null || fakePlayerIdList.isEmpty()) {
            disable();
        }
    }

    @Override
    protected void onDisable() {
        if (mc.player == null || mc.world == null) {
            return;
        }
        if (fakePlayerIdList != null) {
            for (final int id : fakePlayerIdList) {
                mc.world.removeEntityFromWorld(id);
            }
        }
    }

    static {
        fakePlayerInfo = new String[][]{{
                "14241c74-2f8e-4f82-8b84-0de28cfa9856", "XxHunbao0703xX", "-3", "0"}
                , {"07bd8c5e-9c21-48f4-8b80-64a85eec11c2", "samse11", "0", "-3"}
                , {"6c8a0935-3846-4f80-93dd-b94ee9912bab", "Meow_Nightnight", "0", "-3"}
                , {"7e7e8000-4fdb-4939-aaee-aba5fb41916c", "Zab_Zab_Meow", "3", "0"}
                , {"a399567e-1907-4810-a87c-fd355c7d8a06", "WwNightPVPwW", "0", "3"}
                , {"0064cb1c-20d9-49ea-9387-a3e80b93b786", "Shanks_Ace", "-6", "0"}
                , {"207c75d9-e872-477d-9429-d8b422d479e6", "snowmii", "0", "-6"}
                , {"b5067484-37a0-4e14-b4ee-21ce46ce3a50", "Monika922", "6", "0"}
                , {"d71977ab-ef2f-444a-bc64-354e6a9b3d90", "wEnHaOoOoo", "0", "6"}
                , {"8aa592ab-4908-4962-a479-18d85cb84d23", "CKJMQAQ_YT", "-9", "0"}
                , {"762b2b65-f9b7-46dd-87ae-0f2dcac1d436", "Zheo", "0", "-9"}
                , {"53d5416f-09e7-43e3-a44e-2b59c189101b", "Icesky1206", "9", "0"}
                , {"78077be2-7540-4ab9-90a4-03861633592e", "WhiteGG2", "0", "9"}};
    }

    private enum SpawnMode {
        SINGLE,
        MULTI;
    }

    @Override
    public String getHudInfo() {
        return spawnMode.getValue().toString();
    }
}
