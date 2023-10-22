package dev.eliux.monumentaitemdictionary;

import dev.eliux.monumentaitemdictionary.gui.DictionaryController;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Mid implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("monumentaitemdictionary");
	private static KeyBinding openMenuKey;

	public DictionaryController controller = null;

	@Override
	public void onInitialize() {
		openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.monumentaitemdictionary.openitemdictionary", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_H, "category.monumentaitemdictionary"));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (controller != null)
				controller.tick();

			if (openMenuKey.wasPressed()) {
				if (client.player == null) return;

				// open the item dictionary menu
				if (controller == null) 
					controller = new DictionaryController();
			
				controller.open();
			}
		});
	}
}
