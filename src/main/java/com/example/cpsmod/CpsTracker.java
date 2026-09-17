package com.example.cpsmod;

import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Left CPS = actual attack hits registered on a player (via AttackEntityCallback).
 * Right CPS = raw right mouse button presses (still real, no fake numbers).
 */
public class CpsTracker {

    private static final Deque<Long> leftClicks = new ArrayDeque<>();
    private static final Deque<Long> rightClicks = new ArrayDeque<>();

    private static boolean wasRightDown = false;

    public static void poll(MinecraftClient client) {
        if (client == null || client.getWindow() == null) return;

        long handle = client.getWindow().getHandle();
        boolean rightDown = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;

        long now = System.currentTimeMillis();

        if (rightDown && !wasRightDown) {
            rightClicks.addLast(now);
        }
        wasRightDown = rightDown;

        purgeOlderThanOneSecond(leftClicks, now);
        purgeOlderThanOneSecond(rightClicks, now);
    }

    /** Call this exactly when the player successfully attacks another player. */
    public static void recordLeftHit() {
        long now = System.currentTimeMillis();
        leftClicks.addLast(now);
        purgeOlderThanOneSecond(leftClicks, now);
    }

    private static void purgeOlderThanOneSecond(Deque<Long> deque, long now) {
        while (!deque.isEmpty() && now - deque.peekFirst() > 1000) {
            deque.pollFirst();
        }
    }

    public static int getLeftCps() {
        return leftClicks.size();
    }

    public static int getRightCps() {
        return rightClicks.size();
    }
}
