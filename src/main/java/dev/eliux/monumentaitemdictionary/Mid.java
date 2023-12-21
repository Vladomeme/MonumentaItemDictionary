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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;

public class Mid implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("Monumenta Item Dictionary");
	private static KeyBinding openMenuKey;

	private HashMap<String, Object> data = new HashMap<>();

	public DictionaryController controller = null;

	@Override
	public void onInitialize() {
		openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.monumentaitemdictionary.openitemdictionary", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_H, "category.monumentaitemdictionary"));

		loadData();

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (controller == null) {
				controller = new DictionaryController();
			}

			controller.tick();

			if (openMenuKey.wasPressed()) {
				controller.open();
			}
		});

		Runtime.getRuntime().addShutdownHook(new Thread(this::saveData));
	}

	public void setKey(String key, Object value) {
		if (data.containsKey(key)) {
			data.replace(key, value);
		} else {
			data.put(key, value);
		}
	}

	public <T extends Serializable> T getKey(String key, Class<T> type) {
		return type.cast(data.get(key));
	}

	private void saveData() {
		try {
			File target = new File("config/mid/data.txt");
			FileOutputStream fileOutputStream = new FileOutputStream(target);

			target.getParentFile().mkdirs();
			target.createNewFile();

			ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
			objectOutputStream.writeObject(data);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void loadData() {
		try {
			FileInputStream fileInputStream = new FileInputStream("config/mid/data.txt");
			ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

			data = (HashMap<String, Object>)objectInputStream.readObject();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
