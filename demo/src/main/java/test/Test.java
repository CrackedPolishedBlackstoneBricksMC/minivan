package test;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

/**
 * Nonsensical code, but it uses some classes, methods, and fields from MC
 */
public class Test {
	private static final Minecraft mc = Minecraft.getInstance();
	static {
		LocalPlayer gamer = mc.player;
		if(gamer != null) System.out.println(gamer.isFallFlying());
	}
}
