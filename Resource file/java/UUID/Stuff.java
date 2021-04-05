package meow.konc.hack.gui.font.ouo;

import net.minecraft.client.Minecraft;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public class Stuff {

private static String uuids = null;
private static final Minecraft mc = Minecraft.getMinecraft();
public static boolean hasAccess() {
	try {
            if (uuids == null) { 
            URL url = new URL("https://raw.githubusercontent.com/KONCHack/KONCmaterial/master/UUID2.json");
             
            // read text returned by server
            BufferedReader in = new BufferedReader (new InputStreamReader (url.openStream()));
            
            String line;
            while ((line = in.readLine()) != null) {
                uuids += line;
            }
            in.close();
             
	}}
        catch (MalformedURLException e) {
            System.out.println("Malformed URL: " + e.getMessage());
        }
        catch (IOException e) {
            System.out.println("I/O Error: " + e.getMessage());
        }

		String uuid = mc.player.getUniqueID().toString();
		return uuids.contains(uuid);

	}

	public static boolean isExist(){return true;}
}