package com.parzivail.lightlevel;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.impl.client.keybinding.KeyBindingRegistryImpl;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;

public class LightLevel implements ClientModInitializer
{
	private static final String KEYBIND_CATEGORY = "key.lightlevel.category";
	private static final String TOGGLE_KEYBIND = "key.lightlevel.toggle";
	private static KeyBinding keyToggle;

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
		KeyBindingRegistryImpl.addCategory(KEYBIND_CATEGORY);
		KeyBindingHelper.registerKeyBinding(keyToggle = new KeyBinding(TOGGLE_KEYBIND, GLFW.GLFW_KEY_F9, KEYBIND_CATEGORY));
		WorldRenderEvents.AFTER_TRANSLUCENT.register(LLWorldRender::afterTranslucent);
		WorldRenderEvents.END.register(LLWorldRender::onEnd);
	}
}
