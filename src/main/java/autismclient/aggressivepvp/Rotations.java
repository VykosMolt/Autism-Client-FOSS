package autismclient.aggressivepvp;

import static autismclient.aggressivepvp.PlayerUtils.mc;

/**
 * The one rotation path the ported Crystal Aura uses: Meteor's rotateOnce. The look goes into this tick's movement
 * packet only, and with correctMovement the player moves by that yaw for the tick (MoveFix), so the server's movement
 * prediction fits. Nothing here does anything unless rotateOnce was called this tick.
 */
public final class Rotations {
    public static boolean correctMovement;
    /** True while this tick's movement is computed with the sent yaw; cameraYaw is the yaw it replaced. */
    public static boolean movingBySentYaw;
    public static float cameraYaw;

    private static boolean pending, applied;
    private static int pendingPriority;
    private static float yaw, pitch, preYaw, prePitch;

    private Rotations() {}

    public static void rotateOnce(double yaw, double pitch, int priority) {
        if (pending && priority < pendingPriority) return;
        Rotations.yaw = (float) yaw;
        Rotations.pitch = (float) pitch;
        pendingPriority = priority;
        pending = true;
    }

    /** The yaw this tick's movement packet will carry when it is not the camera's, null otherwise. */
    public static Float movementYaw() {
        if (!pending || !correctMovement || mc == null || mc.player == null || mc.getCameraEntity() != mc.player) return null;
        if (mc.player.isFallFlying() || mc.player.isSwimming() || mc.player.isPassenger()) return null;
        return yaw;
    }

    /** LocalPlayer.sendPosition HEAD: the movement packet carries the look. */
    public static void beforeSend() {
        if (!pending || mc == null || mc.player == null || mc.getCameraEntity() != mc.player) return;
        preYaw = mc.player.getYRot();
        prePitch = mc.player.getXRot();
        mc.player.setYRot(yaw);
        mc.player.setXRot(pitch);
        applied = true;
    }

    /** LocalPlayer.sendPosition TAIL: the camera gets its look back, and nothing is held for the next tick. */
    public static void afterSend() {
        if (applied) {
            if (mc != null && mc.player != null) {
                mc.player.setYRot(preYaw);
                mc.player.setXRot(prePitch);
            }
            applied = false;
        }
        pending = false;
    }

    /** Clears any unconsumed rotation from a previous tick (e.g. if sendPosition was skipped while riding). */
    public static void clearPending() {
        pending = false;
    }

    public static void reset() {
        pending = applied = false;
        correctMovement = false;
        movingBySentYaw = false;
    }
}
