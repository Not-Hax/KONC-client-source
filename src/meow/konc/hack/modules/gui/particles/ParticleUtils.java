package meow.konc.hack.gui.particles;

public final class ParticleUtils {
    private static ParticleGenerator particleGenerator = new ParticleGenerator(100);

    public static void drawParticles(int mouseX, int mouseY) {
        particleGenerator.draw(mouseX, mouseY);
    }

    public static void setAmount(int amount) {
        particleGenerator.particles = null;
        particleGenerator.amount = amount;
        particleGenerator.create();
    }
}

