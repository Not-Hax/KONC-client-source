//Client-UUID
/*package meow.pork.vocoshulkerpeek2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Base64;
import net.minecraft.client.Minecraft;

public class DiscordAbsolute {
    public static String discordLoader;

    private static final Minecraft mc = Minecraft.getMinecraft();

    public static boolean hasAccess() {
        try {
            URL url = new URL(new String(Base64.getDecoder().decode("aHR0cHM6Ly9yYXcuZ2l0aHVidXNlcmNvbnRlbnQuY29tL0tPTkNIYWNrL0tPTkNtYXRlcmlhbC9tYXN0ZXIvQ2xpZW50LVVVSUQ=")));
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream()));
            discordLoader = bufferedReader.readLine();
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String position = mc.player.getUniqueID().toString();
        return !discordLoader.contains(position);
    }

    public static boolean isEpic() { return true; }
}*/