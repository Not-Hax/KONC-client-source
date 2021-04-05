package meow.konc.hack.gui.font.ouo;

import net.minecraft.client.Minecraft;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

public class HWIDdtest {

    private static final String hwids = "547d00573d03e87d3d57c2bdd45481022b803e40";
    private static final Minecraft mc = Minecraft.getMinecraft();
    public static boolean hasAccess() throws UnsupportedEncodingException, NoSuchAlgorithmException {
        String hwid = HWIDUTIL.getHWID();
        return hwids.contains(hwid);
    }
    public static boolean isExist(){
        return true;
    }
}
