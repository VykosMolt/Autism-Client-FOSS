package autismclient.aggressivepvp;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundClientTickEndPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPickItemFromBlockPacket;
import net.minecraft.network.protocol.game.ServerboundPickItemFromEntityPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.entity.player.Input;

import static autismclient.aggressivepvp.PlayerUtils.mc;

/**
 * The client tick as the server sees it: everything sent since the last tick-end packet, whoever sent it (the player's
 * own clicks, a module), and the look the last movement packet carried. Fed from Connection.send after AUTISM has had
 * its say (a packet it cancels never gets here). Only records; it sends nothing.
 */
public final class ActionWindow {
    private static final ActionOrder order = new ActionOrder();
    private static final ThreadLocal<Boolean> own = ThreadLocal.withInitial(() -> Boolean.FALSE);
    private static volatile boolean foreign;
    private static volatile float yaw, pitch;
    private static volatile boolean lookKnown;
    /** What the server last heard of the movement keys and sprint, and whether this tick's keys are held back. */
    private static volatile boolean inputMoving, inputSneaking, sprinting, held;
    /** Client ticks seen (tick-end packets), and when the server last sent the time (Meteor's TickRate). */
    private static volatile int ticks;
    private static volatile long joinedAt, timeUpdateAt;

    private ActionWindow() {}

    /** Network thread. */
    public static void onReceive(Packet<?> packet) {
        if (packet instanceof ClientboundLoginPacket) {
            synchronized (order) {
                order.reset();
            }
            foreign = false;
            lookKnown = false;
            inputMoving = inputSneaking = sprinting = held = false;
            joinedAt = timeUpdateAt = System.currentTimeMillis();
        } else if (packet instanceof ClientboundRespawnPacket respawn && !respawn.shouldKeep(ClientboundRespawnPacket.KEEP_ENTITY_DATA)) {
            // A dimension change keeps entity data: the new player carries the last sent input and sprint
            // (ClientPacketListener.handleRespawn), and so does Grim. Only a real respawn starts from no keys
            inputMoving = inputSneaking = sprinting = false;
        }
        else if (packet instanceof ClientboundSetTimePacket) timeUpdateAt = System.currentTimeMillis();
    }

    public static void onSend(Packet<?> packet) {
        if (packet instanceof ServerboundClientTickEndPacket) {
            synchronized (order) {
                order.reset();
            }
            foreign = false;
            held = false;
            ticks++;
            return;
        }

        if (packet instanceof ServerboundMovePlayerPacket move) {
            synchronized (order) {
                order.ended();
            }
            if (move.hasRotation()) {
                yaw = move.getYRot(yaw);
                pitch = move.getXRot(pitch);
                lookKnown = true;
            }
            return;
        }

        if (packet instanceof ServerboundPlayerInputPacket || packet instanceof ServerboundPlayerCommandPacket) {
            if (packet instanceof ServerboundPlayerInputPacket p) {
                Input in = p.input();
                inputMoving = in.forward() || in.backward() || in.left() || in.right() || in.jump();
                inputSneaking = in.shift();
            } else if (packet instanceof ServerboundPlayerCommandPacket p) {
                if (p.getAction() == ServerboundPlayerCommandPacket.Action.START_SPRINTING) sprinting = true;
                else if (p.getAction() == ServerboundPlayerCommandPacket.Action.STOP_SPRINTING) sprinting = false;
            }
            synchronized (order) {
                order.ended();
            }
            return;
        }

        boolean action = true;
        synchronized (order) {
            if (packet instanceof ServerboundAttackPacket attack) order.attacked(attack.entityId());
            else if (packet instanceof ServerboundUseItemOnPacket || packet instanceof ServerboundUseItemPacket || packet instanceof ServerboundInteractPacket) order.used();
            else if (packet instanceof ServerboundPickItemFromBlockPacket || packet instanceof ServerboundPickItemFromEntityPacket) order.picked();
            else if (packet instanceof ServerboundPlayerActionPacket act) {
                switch (act.getAction()) {
                    case START_DESTROY_BLOCK, ABORT_DESTROY_BLOCK, STOP_DESTROY_BLOCK -> order.dug();
                    case RELEASE_USE_ITEM -> order.released();
                    case SWAP_ITEM_WITH_OFFHAND, DROP_ITEM, DROP_ALL_ITEMS -> order.swapped();
                    case STAB -> order.attacked(Integer.MIN_VALUE);
                    default -> action = false;
                }
            }
            else action = false;
        }

        if (action && !Boolean.TRUE.equals(own.get())) foreign = true;
    }

    /** Runs a module's own sends, so they are not taken for the player's. */
    public static void own(Runnable sends) {
        Boolean prev = own.get();
        own.set(Boolean.TRUE);
        try {
            sends.run();
        } finally {
            if (Boolean.TRUE.equals(prev)) own.set(Boolean.TRUE);
            else own.remove();
        }
    }

    public static boolean foreign() {
        return foreign;
    }

    public static boolean mayAttack(int entityId) {
        synchronized (order) {
            return order.mayAttack(entityId);
        }
    }

    public static boolean mayPlace() {
        synchronized (order) {
            return order.mayPlace();
        }
    }

    public static boolean maySwitchSlot() {
        synchronized (order) {
            return order.maySwitchSlot();
        }
    }

    /** Whether the server would take an inventory click now (Grim MultiActionsC: no keys, sprint or sneak last heard). */
    public static boolean mayClickInventory() {
        return !inputMoving && !inputSneaking && !sprinting;
    }

    /** Holds every movement key, jump, sneak and sprint up for the rest of this tick (AggressiveKeyboardInputMixin). */
    public static void holdInput() {
        held = true;
    }

    /** A new client tick (Versions): whatever was held is let go; Auto Totem holds again if it still needs to. */
    public static void releaseHold() {
        held = false;
    }

    /** Only while Auto Totem runs Aggressive: a hold left over when it stopped (or its tick-end packet held back) is dropped. */
    public static boolean inputHeld() {
        if (held && !Versions.aggressiveTotemOn()) held = false;
        return held;
    }

    public static float yaw() {
        return lookKnown || mc == null || mc.player == null ? yaw : mc.player.getYRot();
    }

    public static float pitch() {
        return lookKnown || mc == null || mc.player == null ? pitch : mc.player.getXRot();
    }

    public static int ticks() {
        return ticks;
    }

    /** Seconds since the server last sent the time; 0 for the first 4 s after joining (Meteor's TickRate). */
    public static float timeSinceLastTick() {
        long now = System.currentTimeMillis();
        if (now - joinedAt < 4000) return 0;
        return (now - timeUpdateAt) / 1000f;
    }
}
