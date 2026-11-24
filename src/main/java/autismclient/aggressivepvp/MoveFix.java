/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package autismclient.aggressivepvp;

/**
 * Moving by the look that is sent. A silent rotation puts another yaw into the movement packet than the camera's; a
 * server that works out where the keys should have taken the player from that yaw then pulls them back (DonutSMP,
 * 2026-11-11 20:33 and 20:36: setbacks of 0.16 to 0.45 blocks after every turned click, and none of those clicks
 * answered). So for a tick with a sent yaw the player really moves by it, with the keys turned the other way so the
 * direction over the ground stays what the camera and the real keys ask for, to the nearest eighth of a turn.
 * No Minecraft types, so MoveFixCheck can run it.
 */
public final class MoveFix {
    private MoveFix() {}

    /** The keys {forward, backward, left, right} to press under sentYaw to go where these go under cameraYaw. */
    public static boolean[] remap(boolean forward, boolean backward, boolean left, boolean right, float cameraYaw, float sentYaw) {
        int x = (left ? 1 : 0) - (right ? 1 : 0), z = (forward ? 1 : 0) - (backward ? 1 : 0);
        if (x == 0 && z == 0) return new boolean[4];

        // The game turns the keys' vector (x to the left, z ahead) by the yaw; turn it by the difference instead
        double turn = Math.toRadians(cameraYaw - sentYaw);
        double nx = x * Math.cos(turn) - z * Math.sin(turn), nz = z * Math.cos(turn) + x * Math.sin(turn);
        double eighth = Math.round(Math.atan2(nz, nx) / (Math.PI / 4)) * (Math.PI / 4);
        long kx = Math.round(Math.cos(eighth)), kz = Math.round(Math.sin(eighth));
        return new boolean[]{kz > 0, kz < 0, kx > 0, kx < 0};
    }
}
