package meow.konc.hack.gui.font.ouo;

import net.minecraft.client.Minecraft;

public class License
{
	private static final String uuids = "UUID";
	private static final Minecraft mc = Minecraft.getMinecraft();
	public static boolean hasAccess() {
		String uuid = mc.player.getUniqueID().toString();
		return uuids.contains(uuid);
	}
	public static boolean isExist(){return true;}
}
