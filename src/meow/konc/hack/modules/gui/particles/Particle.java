package meow.konc.hack.gui.particles;

import java.util.*;

import meow.konc.hack.util.packet.Wrapper;
import meow.konc.hack.util.util.RenderUtils;
import net.minecraft.client.gui.*;
import net.minecraft.client.*;

class Particle {
    public float x;

    public float y;

    public final float size;

    private final float ySpeed = (new Random()).nextInt(5);

    private final float xSpeed = (new Random()).nextInt(5);

    private int height;

    private int width;

    Particle(int x, int y) {
        this.x = x;
        this.y = y;
        size = genRandom();
    }

    private float lint1(float f) {
        return 1.02F * (1.0F - f) + 1.0F * f;
    }

    private float lint2(float f) {
        return 1.02F + f * -0.01999998F;
    }

    void connect(float x, float y) {
        RenderUtils.connectPoints(getX(), getY(), x, y);
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public float getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    void interpolation() {
        for (int n = 0; n <= 64; n++) {
            float f = n / 64.0F;
            float p1 = lint1(f);
            float p2 = lint2(f);
            if (p1 != p2) {
                y -= f;
                x -= f;
            }
        }
    }

    void fall() {
        Minecraft mc = Wrapper.getMinecraft();
        ScaledResolution scaledResolution = new ScaledResolution(mc);
        y += ySpeed;
        x += xSpeed;
        if (y > mc.displayHeight)
            y = 1.0F;
        if (x > mc.displayWidth)
            x = 1.0F;
        if (x < 1.0F)
            x = scaledResolution.getScaledWidth();
        if (y < 1.0F)
            y = scaledResolution.getScaledHeight();
    }

    private float genRandom() {
        return (float)(0.30000001192092896D + Math.random() * 1.2999999523162842D);
    }
}
