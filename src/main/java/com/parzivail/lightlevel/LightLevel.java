package com.parzivail.lightlevel;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding;
import net.fabricmc.fabric.impl.client.keybinding.KeyBindingRegistryImpl;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class LightLevel implements ClientModInitializer
{
	private static final String KEYBIND_CATEGORY = "key.lightlevel.category";
	private static final Identifier TOGGLE_KEYBIND = new Identifier("lightlevel", "toggle");
	private static FabricKeyBinding keyToggle;

	private static boolean enabled;

	public static void handleInputEvents()
	{
		while (keyToggle.wasPressed())
			enabled = !enabled;
	}

	public static boolean isEnabled()
	{
		return enabled;
	}

	@Override
	public void onInitializeClient()
	{
		KeyBindingRegistryImpl.INSTANCE.addCategory(KEYBIND_CATEGORY);
		KeyBindingRegistryImpl.INSTANCE.register(keyToggle = FabricKeyBinding.Builder.create(TOGGLE_KEYBIND, InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_F9, KEYBIND_CATEGORY).build());
	}
}
