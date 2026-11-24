/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package autismclient.aggressivepvp;

import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static autismclient.aggressivepvp.PlayerUtils.mc;

/**
 * A timeline of one crystal fight, written to aggressive-pvp/fights/&lt;time&gt;.csv while Crystal Aura (Aggressive) is on.
 * Columns: milliseconds since the file was opened, client tick, event, details.
 */
public final class FightLog {
    private static final DateTimeFormatter NAME = DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss");

    private static BufferedWriter out;
    private static String header;
    private static long startNanos;
    private static int tick;

    private static final Map<BlockPos, long[]> placedAt = new HashMap<>();
    /** A crystal that appears later than this after our click on its base is not taken for ours. */
    private static final long OURS_MS = 500;
    private static final Int2LongOpenHashMap spawnedAt = new Int2LongOpenHashMap(), firstHit = new Int2LongOpenHashMap(), lastHit = new Int2LongOpenHashMap();
    private static final Object2FloatOpenHashMap<String> health = new Object2FloatOpenHashMap<>();

    private FightLog() {}

    /** Arms the log; the file appears with the first event, so switching the module on and off leaves nothing behind. */
    public static synchronized void start(String settings) {
        stop();
        header = settings;
        tick = 0;
        placedAt.clear();
        spawnedAt.clear();
        firstHit.clear();
        lastHit.clear();
        health.clear();
    }

    public static synchronized void stop() {
        if (header == null) return;
        header = null;
        if (out == null) return;
        try {
            out.close();
        } catch (IOException ignored) {}
        out = null;
    }

    private static long ms() {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }

    private static synchronized void event(String type, String format, Object... args) {
        if (header == null) return;
        try {
            if (out == null) {
                Path dir = net.fabricmc.loader.api.FabricLoader.getInstance().getGameDir().resolve("aggressive-pvp").resolve("fights");
                Files.createDirectories(dir);
                out = Files.newBufferedWriter(dir.resolve(NAME.format(LocalDateTime.now()) + ".csv"));
                startNanos = System.nanoTime();
                out.write("ms,tick,event,details\n0,0,SETTINGS," + header + "\n");
            }
            out.write(ms() + "," + tick + "," + type + "," + String.format(Locale.ROOT, format, args) + "\n");
        } catch (IOException e) {
            Autism.LOG.warn("[FightLog] stopped: {}", e.toString());
            stop();
        }
    }

    private static String at(Vec3 v) {
        return String.format(Locale.ROOT, "%.2f %.2f %.2f", v.x, v.y, v.z);
    }

    /** Set by the aura around a click or hit it turned the look for in the tick before. */
    public static boolean turned;

    public static void place(net.minecraft.world.phys.BlockHitResult click, net.minecraft.world.InteractionHand hand, double damage, double self, Entity target) {
        BlockPos base = click.getBlockPos();
        long now = System.nanoTime();
        placedAt.values().removeIf(sent -> now - sent[0] > 5_000_000_000L);
        placedAt.put(base.immutable(), new long[]{now});
        event("PLACE", "on %d %d %d; predicted %.1f to %s at %.1f blocks; %.1f to self; ping %d; %s; click %.2f blocks from eyes on face %s; %s hand, slot %d holds %s", base.getX(), base.getY(), base.getZ(),
            damage, target == null ? "nobody" : EntityUtils.getName(target), target == null ? 0 : target.distanceTo(mc.player), self, PlayerUtils.getPing(),
            look(click.getLocation(), new net.minecraft.world.phys.AABB(base)) + (turned ? " (turned)" : ""), Math.sqrt(click.getLocation().distanceToSqr(mc.player.getEyePosition())), click.getDirection().getName(),
            hand == net.minecraft.world.InteractionHand.MAIN_HAND ? "main" : "off", mc.player.getInventory().getSelectedSlot() + 1,
            net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(mc.player.getItemInHand(hand).getItem()).getPath());
    }

    private static String look(Vec3 point, net.minecraft.world.phys.AABB box) {
        Vec3 eye = mc.player.getEyePosition(), dir = Vec3.directionFromRotation(ActionWindow.pitch(), ActionWindow.yaw());
        double cos = dir.dot(point.subtract(eye).normalize());
        double degrees = Math.toDegrees(Math.acos(Math.max(-1, Math.min(1, cos))));
        boolean through = box.contains(eye) || box.clip(eye, eye.add(dir.scale(8))).isPresent();
        return String.format(Locale.ROOT, "look %.0f deg off, crosshair %s", degrees, through ? "on it" : "elsewhere");
    }

    public static void spawn(Entity crystal) {
        long now = System.nanoTime();
        spawnedAt.put(crystal.getId(), now);
        long[] placed = placedAt.remove(crystal.blockPosition().below());
        long ms = placed == null ? -1 : (now - placed[0]) / 1_000_000;
        event("SPAWN", "id %d at %s; %s", crystal.getId(), at(crystal.position()), placed == null ? "not ours"
            : ms <= OURS_MS ? ms + " ms after our place" : "not ours (our last click on this base was " + ms + " ms before)");
    }

    public static void target(net.minecraft.world.entity.LivingEntity target) {
        StringBuilder armour = new StringBuilder();
        int enchantments = 0;
        for (net.minecraft.world.entity.EquipmentSlot slot : net.minecraft.world.entity.EquipmentSlotGroup.ARMOR) {
            net.minecraft.world.item.ItemStack stack = target.getItemBySlot(slot);
            if (stack.isEmpty()) continue;
            int count = stack.getEnchantments().size();
            enchantments += count;
            if (!armour.isEmpty()) armour.append(", ");
            armour.append(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath()).append(" (").append(count).append(" enchantments)");
        }
        event("TARGET", "%s; armour value %.0f, toughness %.0f; %s; %d enchantments visible; explosion protection %s", EntityUtils.getName(target),
            target.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR), target.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR_TOUGHNESS),
            armour.isEmpty() ? "no armour" : armour, enchantments,
            DamageUtils.protectionHidden(target) ? "assumed full (none visible)" : "as visible");
    }

    public static void attack(Entity crystal, int attempt, String predicted) {
        long now = System.nanoTime();
        int id = crystal.getId();
        firstHit.putIfAbsent(id, now);
        lastHit.put(id, now);
        net.minecraft.world.phys.Vec3 eye = mc.player.getEyePosition();
        net.minecraft.world.phys.AABB box = crystal.getBoundingBox();
        double edge = Math.sqrt(box.distanceToSqr(eye));
        String using = mc.player.isUsingItem() ? net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(mc.player.getUseItem().getItem()).getPath() : "nothing";
        event("ATTACK", "id %d; attempt %d; crystal %s; %.2f blocks from eyes; %s; edge %.2f blocks; using %s; %s", id, attempt,
            spawnedAt.containsKey(id) ? (now - spawnedAt.get(id)) / 1_000_000 + " ms old" : "age unknown", Math.sqrt(crystal.distanceToSqr(mc.player.getEyePosition())),
            look(crystal.getBoundingBox().getCenter(), crystal.getBoundingBox()) + (turned ? " (turned)" : ""), edge, using, predicted);
    }

    public static void gone(Entity crystal) {
        long now = System.nanoTime();
        int id = crystal.getId();
        String hit = firstHit.containsKey(id) ? (now - firstHit.remove(id)) / 1_000_000 + " ms after our first hit, " + (now - lastHit.remove(id)) / 1_000_000 + " after the last" : "never hit by us";
        String lived = spawnedAt.containsKey(id) ? (now - spawnedAt.remove(id)) / 1_000_000 + " ms" : "unknown";
        event("GONE", "id %d; %s; lived %s", id, hit, lived);
    }

    public static void note(String type, String format, Object... args) {
        event(type, format, args);
    }

    public static void unanswered(int id, int attempts) {
        event("UNANSWERED", "id %d; %d hits sent", id, attempts);
    }

    private static Vec3 lastPosition;

    public static void tick() {
        tick++;
        if (mc.player != null) lastPosition = mc.player.position();
        if (header == null || mc.level == null || mc.player == null) return;

        for (Player player : mc.level.players()) {
            if (player != mc.player && player.distanceToSqr(mc.player) > 256) continue;
            String name = player == mc.player ? "SELF" : EntityUtils.getName(player);
            float now = player.getHealth() + player.getAbsorptionAmount();
            float before = health.getOrDefault(name, Float.NaN);
            if (now != before) {
                health.put(name, now);
                if (!Float.isNaN(before)) event("HEALTH", "%s; %.1f -> %.1f; change %.1f; %.1f blocks away", name, before, now, now - before, player.distanceTo(mc.player));
            }
        }

        if (out != null && tick % 20 == 0) {
            try {
                out.flush();
            } catch (IOException ignored) {}
        }
    }

    public static void onPacket(net.minecraft.network.protocol.Packet<?> received) {
        if (header == null) return;
        if (received instanceof ClientboundExplodePacket packet) {
            Vec3 center = packet.center();
            mc.execute(() -> {
                if (mc.player != null) event("EXPLOSION", "at %s; %.1f blocks from you", at(center), Math.sqrt(center.distanceToSqr(mc.player.position())));
            });
        }
        else if (received instanceof ClientboundPlayerPositionPacket packet) {
            Vec3 to = packet.change().position();
            boolean relative = packet.relatives().contains(Relative.X) || packet.relatives().contains(Relative.Y) || packet.relatives().contains(Relative.Z);
            mc.execute(() -> {
                if (mc.player == null) return;
                Vec3 from = lastPosition == null ? mc.player.position() : lastPosition;
                double distance = relative ? to.length() : to.distanceTo(from);
                event(distance > 16 ? "TELEPORT" : "SETBACK", "the server moved you %.2f blocks, from %s to %s", distance, at(from), at(mc.player.position()));
            });
        }
        else if (received instanceof ClientboundEntityEventPacket packet && packet.getEventId() == 35) {
            mc.execute(() -> {
                if (mc.level == null || mc.player == null) return;
                Entity entity = packet.getEntity(mc.level);
                if (entity instanceof Player) event("POP", "%s", entity == mc.player ? "SELF" : EntityUtils.getName(entity));
            });
        }
    }
}
