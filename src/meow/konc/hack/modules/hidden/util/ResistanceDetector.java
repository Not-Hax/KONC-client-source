package meow.konc.hack.modules.hidden.util;

import meow.konc.hack.module.Module;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Module.Info(name = "ResistanceDetector", category = Module.Category.HIDDEN, showOnArray = Module.ShowOnArray.OFF)
public class ResistanceDetector extends Module {
    public HashMap<String, Integer> resistanceList = new HashMap<>();
    //code 9 opcode for useitemend
    @Override
    public void onEnable() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                HashMap<String, Integer> a = new HashMap<>();
                List<String> i = new ArrayList<>();
                resistanceList.forEach((k, v) -> {
                    if (v > 0) {
                        a.put(k, v - 1);
                    }else {
                        i.add(k);
                    }
                });
                a.forEach((k, v) -> {
                    if (resistanceList.containsKey(k)) {
                        resistanceList.replace(k, v);
                    }
                });
                a.clear();
                i.forEach(w -> {
                    if(resistanceList.containsKey(i)) {
                        resistanceList.remove(i);
                    }
                });
            }
            catch (ConcurrentModificationException w) {
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    @Override
    public void onUpdate() {
        if (mc.world != null && mc.player != null) {
            for (EntityPlayer uwu : mc.world.playerEntities) {
                if (((EntityLivingBase) uwu).getAbsorptionAmount() >= 9) {
                    if (resistanceList.containsKey(uwu.getName())) {
                        resistanceList.remove(uwu.getName());
                    }
                    resistanceList.put(uwu.getName(), 180);
                }
            }
        }
    }
}
