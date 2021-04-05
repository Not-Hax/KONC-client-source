package meow.konc.hack.gui.particles;

import meow.konc.hack.util.util.RenderUtils;
import net.minecraft.client.*;

import java.util.*;

public class ParticleGenerator {
    public List<Particle> particles = new ArrayList<>();

    public int amount;

    private int prevWidth;

    private int prevHeight;

    public ParticleGenerator(int amount) {
        this.amount = amount;
    }

    public void draw(int mouseX, int mouseY) {
        if (particles.isEmpty() || prevWidth != (Minecraft.getMinecraft()).displayWidth || prevHeight != (Minecraft.getMinecraft()).displayHeight) {
            particles.clear();
            create();
        }
        prevWidth = (Minecraft.getMinecraft()).displayWidth;
        prevHeight = (Minecraft.getMinecraft()).displayHeight;
        for (Particle particle : particles) {
            particle.fall();
            particle.interpolation();
            int range = 50;
            boolean mouseOver = (mouseX >= particle.x - range && mouseY >= particle.y - range && mouseX <= particle.x + range && mouseY <= particle.y + range);
            if (mouseOver)
                particles.stream()
                        .filter(part -> (part.getX() > particle.getX() && part.getX() - particle.getX() < range && particle.getX() - part.getX() < range && ((part.getY() > particle.getY() && part.getY() - particle.getY() < range) || (particle.getY() > part.getY() && particle.getY() - part.getY() < range))))

                        .forEach(connectable -> particle.connect(connectable.getX(), connectable.getY()));
            RenderUtils.drawCircle(particle.getX(), particle.getY(), particle.size, -1);
        }
    }

    public void create() {
        Random random = new Random();
        for (int i = 0; i < amount; i++)
            particles.add(new Particle(random.nextInt((Minecraft.getMinecraft()).displayWidth), random.nextInt((Minecraft.getMinecraft()).displayHeight)));
    }
}