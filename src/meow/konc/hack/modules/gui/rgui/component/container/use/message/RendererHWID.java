package meow.konc.hack.gui.rgui.component.container.use.message;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class RendererHWID extends JFrame {
    public RendererHWID() {
        setTitle("Unauthorized User Action");
        //setDefaultCloseOperation(3);
        setLocationRelativeTo(null);
        JOptionPane.showMessageDialog(this, "\u4F60\u6C92\u6709\u4F7F\u7528KONC Client\u6B0A\u9650 *HWID*\n You are not using KONC Client permission *HWID*", "Unauthorized User Action", 0);
    }
}

