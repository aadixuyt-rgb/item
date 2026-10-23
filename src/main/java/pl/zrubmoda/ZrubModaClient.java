package pl.zrubmoda;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import pl.zrubmoda.config.AppearanceConfig;
import pl.zrubmoda.gui.AppearanceScreen;

public final class ZrubModaClient implements ClientModInitializer {
    public static KeyBinding OPEN_GUI;
    @Override public void onInitializeClient() {
        AppearanceConfig.load();
        OPEN_GUI = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.zrub_moda.open", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_N, "category.zrub_moda"));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (OPEN_GUI.wasPressed()) {
                if (client.player != null && client.currentScreen == null) client.setScreen(new AppearanceScreen());
            }
        });
    }
}