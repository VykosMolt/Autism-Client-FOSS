package autismclient.aggressivepvp;

import autismclient.modules.Module;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Predicate;

import static autismclient.aggressivepvp.PlayerUtils.mc;

/**
 * Where AUTISM's two modules and the game's hooks meet the engines. While neither module runs Aggressive, every hook
 * here returns at once, apart from ActionWindow's record of what was sent and received, which only writes its own
 * fields. An engine that throws is marked failed; at the next tick start its module goes back to AUTISM's own code
 * (Default) for the session.
 */
public final class Versions {
    public static CrystalEngine crystal;
    public static TotemEngine totem;
    /** AUTISM's Module.isEnabled. Headless checks may swap it when AUTISM's gate keeps every module off. */
    static Predicate<Module> enabled = Module::isEnabled;
    /** The client tick both engines were last driven in (tickKey). */
    private static long drivenKey;

    private Versions() {}

    public static void crystalAuraCreated(Module module) {
        if ("crystal-aura".equals(module.id())) crystal = new CrystalEngine(module);
    }

    public static void autoTotemCreated(Module module) {
        if ("auto-totem".equals(module.id())) totem = new TotemEngine(module);
    }

    /** AUTISM's hint line above a module's settings: why the menu shows Default's settings although Aggressive is chosen. */
    public static String hint(Module module) {
        Engine engine = crystal != null && crystal.module == module ? crystal : totem != null && totem.module == module ? totem : null;
        if (engine == null || !engine.aggressive()) return null;
        if (engine.failed) return "Aggressive is off until restart.";
        return null;
    }

    private static boolean on(Engine engine) {
        return enabled.test(engine.module);
    }

    public static boolean running(Engine engine) {
        return engine != null && engine.running;
    }

    private static boolean live(Engine engine) {
        return engine != null && engine.running && engine.activated && !engine.failed && !Autism.panicMode() && on(engine);
    }

    // The module's lifecycle. True: the engine took the call, skip AUTISM's own code

    public static boolean onEnable(Engine engine) {
        if (engine == null) return false;
        engine.running = wanted(engine);
        engine.activated = false;
        return engine.running;
    }

    public static boolean onDisable(Engine engine) {
        if (!running(engine)) return false;
        stop(engine);
        return true;
    }

    public static boolean onGameLeft(Engine engine) {
        if (!running(engine)) return false;
        if (engine.activated) deactivate(engine);
        engine.activated = false;
        return true;
    }

    private static boolean wanted(Engine engine) {
        return engine.aggressive() && !engine.failed && !Autism.panicMode() && Autism.verified();
    }

    /**
     * AUTISM's preMovementTick, at the start of the client tick: both engines run here, Auto Totem first. A module that
     * is on but runs the other version than the one selected (a switch, an engine that failed, or Aggressive at startup,
     * where AUTISM calls no onEnable) changes over here, without switching the module off and on. Both change over before
     * either is driven: AUTISM asks auto-totem first, and that call drives the crystal engine too.
     */
    public static boolean preMovementTick(Engine engine) {
        if (engine == null) return false;
        if (Autism.panicMode()) {
            if (running(totem)) stop(totem);
            if (running(crystal)) stop(crystal);
            ActionWindow.releaseHold();
            return false;
        }
        switchOver(totem);
        switchOver(crystal);
        LocalPlayer player = mc == null ? null : mc.player;
        // An engine that acted this tick and was taken off since: AUTISM's own code starts next tick, not twice in one
        if (!engine.running) return player != null && engine.ranAt == tickKey(player);

        if (player != null && mc.level != null && mc.gameMode != null && mc.getConnection() != null && tickKey(player) != drivenKey) {
            drivenKey = tickKey(player);
            ActionWindow.releaseHold();
            tick(totem);
            tick(crystal);
        }
        return true;
    }

    /** Respawn and dimension change make a new LocalPlayer, whose tickCount starts again. */
    private static long tickKey(LocalPlayer player) {
        return (long) System.identityHashCode(player) << 32 | player.tickCount & 0xffffffffL;
    }

    private static void switchOver(Engine engine) {
        if (engine == null || !on(engine) || engine.running == wanted(engine)) return;
        if (engine.running) {
            stop(engine);
            autism(engine, engine.module::onEnable);
        } else {
            autism(engine, engine.module::onDisable);
            engine.running = true;
            engine.activated = false;
        }
    }

    /** AUTISM's own onEnable/onDisable, run for a version change: whatever it throws, the change goes through. */
    private static void autism(Engine engine, Runnable code) {
        try {
            code.run();
        } catch (Throwable t) {
            Autism.LOG.error("aggressive-pvp: AUTISM's {} failed while changing version", engine.module.id(), t);
        }
    }

    private static void tick(Engine engine) {
        if (engine == null || !engine.running || engine.failed || !on(engine)) return;
        engine.ranAt = drivenKey;
        if (!engine.activated) {
            engine.activated = true;
            if (!guard(engine, engine::onActivate)) return;
        }
        guard(engine, engine::onPreTick);
    }

    private static void stop(Engine engine) {
        boolean activated = engine.activated;
        engine.running = engine.activated = false;
        if (activated) deactivate(engine);
    }

    private static void deactivate(Engine engine) {
        try {
            engine.onDeactivate();
        } catch (Throwable t) {
            Autism.LOG.error("aggressive-pvp: {} failed to stop", engine.module.id(), t);
        }
    }

    /**
     * Runs engine code. On any error the engine is marked failed and stays quiet from then on; the hand-over to AUTISM's
     * own code waits for the next tick start (switchOver), not the middle of whatever call failed.
     */
    private static boolean guard(Engine engine, Runnable code) {
        try {
            code.run();
            return true;
        } catch (Throwable t) {
            if (!engine.failed) {
                engine.failed = true;
                Autism.LOG.error("aggressive-pvp: {} stopped after an error", engine.module.id(), t);
                if (mc != null) mc.execute(() -> Autism.chat("§c" + engine.module.name() + " (Aggressive) errored; Default runs until restart."));
            }
            return false;
        }
    }

    /** Work an engine hands from the network thread to the client thread: only while it still runs, and guarded. */
    static void later(Engine engine, Runnable code) {
        if (mc == null) return;
        mc.execute(() -> {
            if (live(engine)) guard(engine, code);
        });
    }

    public static String info(Engine engine) {
        String[] text = {""};
        if (live(engine)) guard(engine, () -> text[0] = engine.info());
        return text[0];
    }

    // What the crystal engine asks of Auto Totem

    public static boolean totemReselectPending() {
        if (live(totem)) return totem.reselectPending();
        return totem != null && on(totem) && Autism.defaultTotemBusy();
    }

    /** True while Aggressive Auto Totem is holding input or waiting to reselect the hotbar slot (never calls defaultTotemBusy). */
    public static boolean aggressiveTotemBusy() {
        return live(totem) && (ActionWindow.inputHeld() || totem.reselectPending());
    }

    /** Auto Totem is on and runs Aggressive: the instant hotbar swap trade-totem counts on (the fork asks for its own). */
    public static boolean aggressiveTotemOn() {
        return live(totem);
    }

    // Game hooks

    /** Connection.send of the player's own connection, after AUTISM's own handlers let the packet through. */
    public static void onSend(Packet<?> packet) {
        ActionWindow.onSend(packet);
        if (live(crystal)) guard(crystal, () -> crystal.onPacketSend(packet));
    }

    /** Connection.channelRead0 of the player's own connection, network thread. */
    public static void onReceive(Packet<?> packet) {
        ActionWindow.onReceive(packet);
        if (live(totem)) guard(totem, () -> totem.onPacketReceive(packet));
        if (live(crystal)) guard(crystal, () -> {
            FightLog.onPacket(packet);
            crystal.onPacketReceive(packet);
        });
    }

    /** LocalPlayer.aiStep HEAD: after the player's own clicks of this tick, before the movement packet. */
    public static void onMovementTick() {
        if (live(crystal)) guard(crystal, crystal::onPlayerTickMovement);
    }

    public static void onEntityAdded(Entity entity) {
        if (live(crystal)) guard(crystal, () -> crystal.onEntityAdded(entity));
    }

    public static void onEntityRemoved(Entity entity) {
        if (entity != null && live(crystal)) guard(crystal, () -> crystal.onEntityRemoved(entity));
    }

    public static void onBlockUpdate(BlockPos pos, BlockState state) {
        if (live(crystal)) guard(crystal, () -> crystal.onBlockUpdate(pos, state));
    }
}
