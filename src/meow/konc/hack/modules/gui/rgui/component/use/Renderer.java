package meow.konc.hack.gui.rgui.component.use;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class Renderer extends JFrame {
    public Renderer() {
        setTitle("Unauthorized User Action");
        setDefaultCloseOperation(3);
        setLocationRelativeTo(null);
        JOptionPane.showMessageDialog(this, "\u4F60\u6C92\u6709\u4F7F\u7528KONC Client\u6B0A\u9650\n You are not using KONC Client permission", "Unauthorized User Action", 0);
    }
}
