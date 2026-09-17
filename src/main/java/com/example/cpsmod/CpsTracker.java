package com.example.cpsmod;

import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Tracks REAL left/right mouse click timestamps and reports how many
 * happened in the last 1000ms (i.e. actual clicks-per-second).
 *
 * This reads the raw GLFW mouse button state every frame, so it reflects
 * exactly what your hand/mouse/autoclicker is physically doing.
 * It does not fake, cap, or alter the number in any way, and it has
 * no network or server-side component.
 */
public class CpsTracker {

    private static final Deque<Long> leftClicks = new ArrayDeque<>();
    private static final Deque<Long> rightClicks = new ArrayDeque<>();

    private static boolean wasLeftDown = false;
    private static boolean wasRightDown = false;

    public static void poll(MinecraftClient client) {
        if (client == null || client.getWindow() == null) return;

        long handle = client.getWindow().getHandle();

        boolean leftDown = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean rightDown = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;

        long now = System.currentTimeMillis();

        // Rising edge = a new click
        if (leftDown && !wasLeftDown) {
            leftClicks.addLast(now);
        }
        if (rightDown && !wasRightDown) {
            rightClicks.addLast(now);
        }

        wasLeftDown = leftDown;
        wasRightDown = rightDown;

        purgeOlderThanOneSecond(leftClicks, now);
        purgeOlderThanOneSecond(rightClicks, now);
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
