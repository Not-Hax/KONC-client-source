package meow.konc.hack.event.events.other;

import meow.konc.hack.event.ForgeEventProcessor;

import java.awt.*;

public class Rainbow {
    public static int getInt(){
        return ForgeEventProcessor.INSTANCE.getRgb();
    }

    public static Color getColor(){
        return ForgeEventProcessor.INSTANCE.getC();
    }

    public static Color getColorWithOpacity(int opacity){
        return new Color(getColor().getRed(), getColor().getGreen(), getColor().getBlue(), opacity);
    }

    public static int getIntWithOpacity(int opacity){
        return getColorWithOpacity(opacity).getRGB();
    }
}