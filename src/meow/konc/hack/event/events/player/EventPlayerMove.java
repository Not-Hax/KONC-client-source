package meow.konc.hack.event.events.player;

import meow.konc.hack.event.KONCEvent;
import net.minecraft.entity.MoverType;

public class EventPlayerMove extends KONCEvent
{
    public MoverType Type;
    public double X;
    public double Y;
    public double Z;

    public EventPlayerMove(MoverType p_Type, double p_X, double p_Y, double p_Z)
    {
        Type = p_Type;
        X = p_X;
        Y = p_Y;
        Z = p_Z;
    }
    public void setX(final double x) {
        this.X = x;
    }

    public void setY(final double y) {
        this.Y = y;
    }

    public void setZ(final double z) {
        this.Z = z;
    }
}
