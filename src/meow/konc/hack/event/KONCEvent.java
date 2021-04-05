package meow.konc.hack.event;

import me.zero.alpine.type.Cancellable;
import meow.konc.hack.util.packet.Wrapper;

/**
 * Created by 086 on 16/11/2017.
 */
public class KONCEvent extends Cancellable {

    private Era era = Era.PRE;
    private final float partialTicks;

    public KONCEvent(Era p_Era) {
        partialTicks = Wrapper.getMinecraft().getRenderPartialTicks();
        era = p_Era;
    }

    public KONCEvent() {
        partialTicks = Wrapper.getMinecraft().getRenderPartialTicks();
    }

    public Era getEra() {
        return era;
    }

    public float getPartialTicks() {
        return partialTicks;
    }

    public enum Era {
        PRE, PERI, POST
    }

}

