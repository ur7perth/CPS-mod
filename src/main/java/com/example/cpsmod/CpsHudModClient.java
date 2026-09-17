package com.example.cpsmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class CpsHudModClient implements ClientModInitializer {

    private static boolean hudVisible = true;
    private KeyBinding toggleKey;

    @Override
    public void onInitializeClient() {
        // Unbound by default: bind it yourself in Options > Controls > CPS Mod
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.cpsmod.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                "category.cpsmod"
        ));

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            CpsTracker.poll(client);
            while (toggleKey.wasPressed()) {
                hudVisible = !hudVisible;
            }
        });

        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            MinecraftClient client = MinecraftClient.getInstance();

            // Poll every rendered frame too, so fast clicks between ticks aren't missed
            CpsTracker.poll(client);

            if (!hudVisible || client.options.hudHidden) return;

            int x = 5;
            int y = 5;
            int color = 0xFFFFFF;

            drawContext.drawTextWithShadow(client.textRenderer,
                    Text.literal("Left CPS: " + CpsTracker.getLeftCps()), x, y, color);
            drawContext.drawTextWithShadow(client.textRenderer,
                    Text.literal("Right CPS: " + CpsTracker.getRightCps()), x, y + 10, color);
        });
    }
}
