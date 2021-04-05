package meow.konc.hack.gui.konc;

import meow.konc.hack.KONCMod;
import meow.konc.hack.gui.rgui.component.Component;
import meow.konc.hack.gui.rgui.component.container.use.Frame;
import meow.konc.hack.module.ModuleManager;
import meow.konc.hack.modules.other.Gui;
import meow.konc.hack.util.packet.Wrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public class DisplayGuiScreen extends GuiScreen
{
    KONCGUI gui;
    public GuiScreen lastScreen;
    public static int mouseX;
    public static int mouseY;
    Framebuffer framebuffer;

    public DisplayGuiScreen() {
        KONCGUI gui = KONCMod.getInstance().getGuiManager();
        for (Component c : gui.getChildren()) {
            if (c instanceof Frame) {
                Frame child = (Frame)c;
                if (!child.isPinneable() || !child.isVisible()) {
                    continue;
                }
                child.setOpacity(0.5f);
            }
        }
        framebuffer = new Framebuffer(Wrapper.getMinecraft().displayWidth, Wrapper.getMinecraft().displayHeight, false);
    }

    public boolean doesGuiPauseGame() {
        return false;
    }

    public void onGuiClosed() {
        KONCGUI gui = KONCMod.getInstance().getGuiManager();
        gui.getChildren().stream().filter(component -> component instanceof Frame && ((Frame) component).isPinneable() && component.isVisible()).forEach(component -> component.setOpacity(0.0f));
        if (Wrapper.getMinecraft().entityRenderer.shaderGroup != null) {
            Wrapper.getMinecraft().entityRenderer.shaderGroup.deleteShaderGroup();
        }
    }

    public void initGui() {
        gui = KONCMod.getInstance().getGuiManager();
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        calculateMouse();
        gui.drawGUI();
        GL11.glEnable(3553);
        GlStateManager.color(1.0f, 1.0f, 1.0f);
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        gui.handleMouseDown(DisplayGuiScreen.mouseX, DisplayGuiScreen.mouseY);
    }

    protected void mouseReleased(int mouseX, int mouseY, int state) {
        gui.handleMouseRelease(DisplayGuiScreen.mouseX, DisplayGuiScreen.mouseY);
    }

    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        gui.handleMouseDrag(DisplayGuiScreen.mouseX, DisplayGuiScreen.mouseY);
    }

    public void updateScreen() {
        Gui colorBack = (Gui)ModuleManager.getModuleByName("Gui");
        if (colorBack.getBlur()) {
            if (OpenGlHelper.shadersSupported && Wrapper.getMinecraft().getRenderViewEntity() instanceof EntityPlayer) {
                if (Wrapper.getMinecraft().entityRenderer.shaderGroup != null) {
                    Wrapper.getMinecraft().entityRenderer.shaderGroup.deleteShaderGroup();
                }
                Wrapper.getMinecraft().entityRenderer.loadShader(new ResourceLocation("shaders/post/blur.json"));
            }
        }
        else if (Wrapper.getMinecraft().entityRenderer.shaderGroup != null) {
            Wrapper.getMinecraft().entityRenderer.shaderGroup.deleteShaderGroup();
        }
        if (Mouse.hasWheel()) {
            int a = Mouse.getDWheel();
            if (a != 0) {
                gui.handleWheel(DisplayGuiScreen.mouseX, DisplayGuiScreen.mouseY, a);
            }
        }
    }

    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == 1) {
            mc.displayGuiScreen(lastScreen);
        }
        else {
            gui.handleKeyDown(keyCode);
            gui.handleKeyUp(keyCode);
        }
    }

    public static int getScale() {
        int scale = Wrapper.getMinecraft().gameSettings.guiScale;
        if (scale == 0) {
            scale = 1000;
        }
        int scaleFactor;
        for (scaleFactor = 0; scaleFactor < scale && Wrapper.getMinecraft().displayWidth / (scaleFactor + 1) >= 320 && Wrapper.getMinecraft().displayHeight / (scaleFactor + 1) >= 240; ++scaleFactor) {}
        if (scaleFactor == 0) {
            scaleFactor = 1;
        }
        return scaleFactor;
    }

    private void calculateMouse() {
        Minecraft minecraft = Minecraft.getMinecraft();
        int scaleFactor = getScale();
        DisplayGuiScreen.mouseX = Mouse.getX() / scaleFactor;
        DisplayGuiScreen.mouseY = minecraft.displayHeight / scaleFactor - Mouse.getY() / scaleFactor - 1;
    }
}
