package autismclient.util;

import autismclient.AutismClientAddon;
import autismclient.api.module.Setting;
import autismclient.compat263.Compat263;
import autismclient.compat263.CompatSwingPacket;
import autismclient.modules.Module;
import autismclient.modules.j;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundDamageEventPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundHurtAnimationPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPunchPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.AttackRange;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.item.component.BlocksAttacks.DamageReduction;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class v {
   private static final List<String> init_List = List.of(
      "entities",
      "aim",
      "fov",
      "targeting",
      "attack-mode",
      "interactions",
      "criticals",
      "auto-sword",
      "shield-break",
      "auto-block",
      "perfect-hit",
      "perfect-hit-chance",
      "miss-chance",
      "swing-offset",
      "technique",
      "rage-cps",
      "butterfly-cps",
      "normal-cps",
      "jitter-cps",
      "respect-cooldown",
      "reaction",
      "legacy-reaction",
      "edge-tolerance",
      "pre-reach",
      "hesitation",
      "modern-hold"
   );
   private static volatile cQ init_cQ;
   private static volatile Vec3 init_Vec3 = Vec3.ZERO;
   private static volatile int init_I;
   private static volatile int load_I = -1;
   private static volatile long init_J;
   private static final AtomicLong init_AtomicLong = new AtomicLong();
   private static Object init_Obj;
   private static Object load_Obj;
   private static int read = -2147483648;
   private static long load_J;
   private static float init_F = 0.0F / 0.0F;
   private static float load_F = 0.0F / 0.0F;
   private static String init_Str = "";
   private static volatile Path init_Path;
   private static volatile boolean init_Z;
   private static boolean load_Z;

   private v() {
   }

   public static void init_V() {
      if (!load_Z) {
         load_Z = true;
         ClientTickEvents.END_CLIENT_TICK.register(v::init);
         Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            cQ current = init_cQ;
            if (current != null) {
               try {
                  current.init("client_shutdown").get(1500L, TimeUnit.MILLISECONDS);
               } catch (Exception var2) {
               }
            }
         }, "autism-combat-recorder-shutdown"));
      }
   }

   public static boolean init_Z() {
      cQ current = init_cQ;
      return current != null && !current.init_Z();
   }

   public static String init_Str(String label) {
      Minecraft mc = Minecraft.getInstance();
      if (init_cQ != null) {
         return "Combat recording is already running or finishing. Use .combatlog status.";
      } else if (init_Z) {
         return "The previous combat recording is still being saved.";
      } else if (mc.player != null && mc.level != null) {
         init_J = System.nanoTime();
         init_AtomicLong.set(0L);
         init_I = 1;
         load_I = mc.player.getId();
         init_Obj = mc.level;
         load_Obj = mc.player;
         read = -2147483648;
         load_J = 0L;
         load_F = 0.0F / 0.0F;
         init_F = 0.0F / 0.0F;
         init_Str = "";
         init_Vec3 = mc.player.position();
         String stamp = DateTimeFormatter.ofPattern("uuuuMMdd-HHmmss").withZone(ZoneOffset.UTC).format(Instant.now());
         Path archive = AutismClientAddon.FOLDER
            .toPath()
            .resolve("combat-logs")
            .resolve("combat-" + stamp + "-" + UUID.randomUUID().toString().substring(0, 8) + ".zip");
         JsonObject metadata = new JsonObject();
         metadata.addProperty("kind", "metadata");
         metadata.addProperty("schema", 1);
         metadata.addProperty("started_utc", Instant.now().toString());
         metadata.addProperty("label", init(label, 120));
         metadata.addProperty(
            "mod_version", FabricLoader.getInstance().getModContainer("autism").map(c -> c.getMetadata().getVersion().getFriendlyString()).orElse("unknown")
         );
         metadata.addProperty("policy", "combat-defense-2026-10-16-block-hit-v2");
         metadata.addProperty("positions", "relative_to_segment_origin");
         metadata.addProperty("observability", "client_only; no assumed legacy block success or unknown hit attribution");
         cQ next = new cQ(archive, metadata);
         init_cQ = next;
         JsonObject first = init_JsonObject("segment_start");
         first.addProperty("dimension", mc.level.dimension().identifier().toString());
         init(first);
         next.init_CompletableFuture()
            .whenComplete(
               (path, error) -> mc.execute(
                  () -> {
                     if (init_cQ == next) {
                        init_cQ = null;
                     }

                     init_Z = false;
                     if (error == null) {
                        init_Path = path;
                        AutismClientMessaging.sendPrefixed(
                           "Combat recording saved: " + path + (next.init_J() == 0L ? "" : " (dropped rows: " + next.init_J() + ")")
                        );
                     } else {
                        AutismClientMessaging.sendPrefixed(
                           "Combat recording could not be saved. Partial data, if available: "
                              + next.init_Path()
                              + ".partial.jsonl.gz ("
                              + error.getClass().getSimpleName()
                              + ")"
                        );
                     }
                  }
               )
            );
         return "Combat recording started. Use .combatlog mark <note> between rounds, then .combatlog stop. Local file: " + archive;
      } else {
         return "Join a world or server before starting a combat recording.";
      }
   }

   public static String init_Str() {
      cQ current = init_cQ;
      if (current == null) {
         return init_Z ? "Combat recording is being saved." : "No combat recording is running.";
      }

      init_V("user_stop");
      return "Saving combat recording: " + current.init_Path();
   }

   private static void init_V(String reason) {
      cQ current = init_cQ;
      if (current != null) {
         init_Z = true;
         init_cQ = null;
         current.init(reason);
      }
   }

   public static String load() {
      cQ current = init_cQ;
      if (current != null) {
         return "Recording " + (System.nanoTime() - init_J) / 1000000000L + "s; dropped rows: " + current.init_J() + "; " + current.init_Path();
      } else if (init_Z) {
         return "Saving combat recording.";
      } else {
         return init_Path == null ? "No combat recording yet. Use .combatlog start." : "Last combat recording: " + init_Path;
      }
   }

   public static String load(String note) {
      if (!init_Z()) {
         return "Start a combat recording first.";
      }

      JsonObject row = init_JsonObject("mark");
      row.addProperty("note", init(note, 1000));
      init(row);
      return "Combat mark added: " + init(note, 120);
   }

   private static String init(String value, int limit) {
      return value == null ? "" : value.substring(0, Math.min(limit, value.length()));
   }

   public static JsonObject init_JsonObject(String kind) {
      JsonObject row = new JsonObject();
      row.addProperty("kind", kind);
      row.addProperty("seq", init_AtomicLong.incrementAndGet());
      row.addProperty("tick", ck.init_ck().state_I());
      row.addProperty("t_ms", (System.nanoTime() - init_J) / 1000000.0);
      row.addProperty("segment", init_I);
      row.addProperty("self_id", load_I);
      return row;
   }

   public static void init(JsonObject row) {
      cQ current = init_cQ;
      if (current != null) {
         current.init(row);
      }
   }

   public static void init(RuntimeException failure) {
      if (init_Z()) {
         JsonObject error = init_JsonObject("capture_error");
         error.addProperty("error", failure.getClass().getSimpleName());
         init(error);
         init_V("capture_error");
      }
   }

   public static void init(Packet<?> packet) {
      if (init_Z()) {
         JsonObject row;
         if (packet instanceof ServerboundAttackPacket attack) {
            row = init_JsonObject("attack_written");
            row.addProperty("entity_id", attack.entityId());
         } else if (packet instanceof ServerboundUseItemPacket use) {
            row = init_JsonObject("use_written");
            row.addProperty("hand", use.hand().name());
            row.addProperty("yaw", use.yRot());
            row.addProperty("pitch", use.xRot());
            row.addProperty("prediction_sequence", use.sequence());
         } else if (packet instanceof ServerboundPlayerActionPacket action && action.getAction() == Action.RELEASE_USE_ITEM) {
            row = init_JsonObject("release_written");
         } else if (packet instanceof CompatSwingPacket || packet instanceof ServerboundPunchPacket) {
            row = init_JsonObject("swing_written");
         } else {
            if (!(packet instanceof ServerboundMovePlayerPacket movement)) {
               return;
            }

            row = init_JsonObject("movement_written");
            row.addProperty("ground", movement.isOnGround());
            if (movement.hasPosition()) {
               row.add("pos", load(new Vec3(movement.getX(0.0), movement.getY(0.0), movement.getZ(0.0))));
            }

            if (movement.hasRotation()) {
               row.addProperty("yaw", movement.getYRot(0.0F));
               row.addProperty("pitch", movement.getXRot(0.0F));
            }
         }

         init(row);
      }
   }

   public static void load(Packet<?> packet) {
      if (init_Z()) {
         try {
            read(packet);
         } catch (RuntimeException failure) {
            init(failure);
         }
      }
   }

   private static void read(Packet<?> packet) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null && mc.player != null) {
         JsonObject row;
         if (packet instanceof ClientboundDamageEventPacket damage) {
            row = init_JsonObject("damage_event");
            row.addProperty("entity_id", damage.entityId());
            row.addProperty("cause_id", damage.sourceCauseId());
            row.addProperty("direct_id", damage.sourceDirectId());
            row.addProperty("damage_type", damage.sourceType().unwrapKey().map(k -> k.identifier().toString()).orElse("unknown"));
         } else if (packet instanceof ClientboundHurtAnimationPacket hurt) {
            row = init_JsonObject("hurt_animation");
            row.addProperty("entity_id", hurt.id());
            row.addProperty("yaw", hurt.yaw());
         } else if (packet instanceof ClientboundEntityEventPacket event) {
            Entity entity = event.getEntity(mc.level);
            if (entity == null) {
               return;
            }

            int id = event.getEventId();
            if (id != 2 && id != 3 && id != 29 && id != 30 && id != 35 && id != 47 && id != 48) {
               return;
            }

            row = init_JsonObject("entity_event");
            row.addProperty("entity_id", entity.getId());
            row.addProperty("event_id", id);
         } else if (packet instanceof ClientboundSetHealthPacket value) {
            row = init_JsonObject("health_packet");
            row.addProperty("health", value.getHealth());
         } else if (packet instanceof ClientboundAnimatePacket animate) {
            row = init_JsonObject("animation_packet");
            row.addProperty("entity_id", animate.getId());
            row.addProperty("action", animate.getAction());
         } else if (packet instanceof ClientboundSetEntityMotionPacket motion) {
            row = init_JsonObject("motion_packet");
            row.addProperty("entity_id", motion.id());
            row.add("motion", init(motion.movement()));
         } else if (packet instanceof ClientboundPlayerPositionPacket) {
            row = init_JsonObject("position_correction");
            row.add("pos", load(mc.player.position()));
         } else if (packet instanceof ClientboundSoundEntityPacket sound) {
            String name = sound.getSound().value().location().toString();
            if (!init_Z(name)) {
               return;
            }

            row = init_JsonObject("entity_sound");
            row.addProperty("entity_id", sound.getId());
            row.addProperty("sound", name);
         } else {
            if (!(packet instanceof ClientboundSoundPacket sound)) {
               return;
            }

            String name = sound.getSound().value().location().toString();
            if (!init_Z(name)) {
               return;
            }

            Vec3 at = new Vec3(sound.getX(), sound.getY(), sound.getZ());
            if (at.distanceToSqr(mc.player.position()) > 144.0) {
               return;
            }

            row = init_JsonObject("positional_sound");
            row.addProperty("sound", name);
            row.add("pos", load(at));
         }

         init(row);
      }
   }

   private static boolean init_Z(String name) {
      return name.contains("shield.block") || name.contains("shield.break") || name.contains("player.attack") || name.contains("player.hurt");
   }

   private static void init(Minecraft mc) {
      if (init_Z()) {
         try {
            if (mc.getConnection() == null) {
               init_V("disconnect");
               return;
            }

            if (System.nanoTime() - init_J > 7200000000000L) {
               init_V("duration_limit");
               return;
            }

            if (mc.level == null || mc.player == null) {
               return;
            }

            int tick = ck.init_ck().state_I();
            if (tick == read) {
               return;
            }

            read = tick;
            if (init_Obj != mc.level || load_Obj != mc.player) {
               init_Obj = mc.level;
               load_Obj = mc.player;
               load_I = mc.player.getId();
               init_I++;
               init_Vec3 = mc.player.position();
               load_F = 0.0F / 0.0F;
               init_F = 0.0F / 0.0F;
               JsonObject change = init_JsonObject("segment_start");
               change.addProperty("dimension", mc.level.dimension().identifier().toString());
               init(change);
            }

            JsonObject config = init_JsonObject();
            String configText = config.toString();
            if (!init_Str.equals(configText)) {
               init_Str = configText;
               JsonObject change = init_JsonObject("settings");
               change.add("modules", config);
               init(change);
            }

            JsonObject frame = init_JsonObject("frame");
            long now = System.nanoTime();
            if (load_J != 0L) {
               frame.addProperty("sample_interval_ms", (now - load_J) / 1000000.0);
            }

            load_J = now;
            frame.add("self", init_JsonObject(mc.player));
            frame.addProperty("screen", mc.gui.screen() == null ? "" : mc.gui.screen().getClass().getSimpleName());
            frame.addProperty("guard_owner", s.init_Str());
            frame.addProperty("rotation_owner", aD.init_Str());
            frame.addProperty("clutch_reserves_movement", j.read_Z_20());
            frame.addProperty("scaffold_rotation", autismclient.modules.aI.flush_Z_5());
            frame.addProperty("attack_input_owned", s.close_Z());
            ch.Inner_CachePool wire = ch.init_ch_CachePool();
            frame.addProperty("wire_yaw", wire.init_Z() ? wire.read() : mc.player.getYRot());
            frame.addProperty("wire_pitch", wire.init_Z() ? wire.write() : mc.player.getXRot());
            frame.addProperty("charge", mc.player.getAttackStrengthScale(0.5F));
            frame.addProperty("attack_delay", mc.player.getCurrentItemAttackStrengthDelay());
            frame.addProperty("reach", u.init_D(mc.player));
            frame.addProperty("slot", mc.player.getInventory().getSelectedSlot());
            frame.addProperty("main_cooldown", mc.player.getCooldowns().isOnCooldown(mc.player.getMainHandItem()));
            frame.addProperty("off_cooldown", mc.player.getCooldowns().isOnCooldown(mc.player.getOffhandItem()));
            frame.addProperty("legacy", aI.init(mc.player));
            Input keys = mc.player.input.keyPresses;
            JsonObject input = new JsonObject();
            input.addProperty("forward", keys.forward());
            input.addProperty("back", keys.backward());
            input.addProperty("left", keys.left());
            input.addProperty("right", keys.right());
            input.addProperty("jump", keys.jump());
            input.addProperty("sneak", keys.shift());
            input.addProperty("sprint", keys.sprint());
            input.addProperty("attack", mc.options.keyAttack.isDown());
            input.addProperty("use", mc.options.keyUse.isDown());
            input.addProperty("physical_attack", aB.init(mc.options.keyAttack).autism$isActuallyDown());
            input.addProperty("physical_use", aB.init(mc.options.keyUse).autism$isActuallyDown());
            frame.add("keys", input);
            if (!Float.isNaN(init_F) && (init_F != mc.player.getHealth() || load_F != mc.player.getAbsorptionAmount())) {
               JsonObject change = init_JsonObject("self_health_change");
               change.addProperty("health_delta", mc.player.getHealth() - init_F);
               change.addProperty("absorption_delta", mc.player.getAbsorptionAmount() - load_F);
               init(change);
            }

            init_F = mc.player.getHealth();
            load_F = mc.player.getAbsorptionAmount();
            LivingEntity target = autismclient.modules.al.init_Module_Str("kill-aura") instanceof autismclient.modules.aa aura
               ? aura.init_LivingEntity_3()
               : null;
            List<LivingEntity> nearby = new ArrayList<>();

            for (Entity entity : mc.level.entitiesForRendering()) {
               if (entity instanceof LivingEntity living && entity != mc.player && (entity == target || entity.distanceToSqr(mc.player) <= 144.0)) {
                  nearby.add(living);
               }
            }

            nearby.sort(Comparator.comparingDouble(e -> e == target ? -1.0 : e.distanceToSqr(mc.player)));
            JsonArray actors = new JsonArray();

            for (LivingEntity entity : nearby.subList(0, Math.min(16, nearby.size()))) {
               actors.add(init_JsonObject(entity));
            }

            frame.add("nearby", actors);
            frame.addProperty("nearby_omitted", Math.max(0, nearby.size() - 16));
            init(frame);
         } catch (RuntimeException failure) {
            init(failure);
         }
      }
   }

   private static JsonObject init_JsonObject() {
      JsonObject result = new JsonObject();

      for (String id : List.of("kill-aura", "trigger-bot", "aim-assist", "sprint", "freecam", "clutch", "scaffold", "velocity")) {
         Module module = autismclient.modules.al.init_Module_Str(id);
         if (module != null) {
            JsonObject value = new JsonObject();
            value.addProperty("enabled", module.isEnabled());
            if (id.equals("kill-aura") || id.equals("trigger-bot")) {
               for (Setting<?, ?> setting : module.settings()) {
                  if (init_List.contains(setting.id())) {
                     value.addProperty(setting.id(), setting.serialize());
                  }
               }
            }

            if (id.equals("kill-aura") || id.equals("trigger-bot")) {
               for (String extra : List.of("technique", "rage-cps", "butterfly-cps", "normal-cps", "jitter-cps")) {
                  value.addProperty(extra, module.value(extra));
               }
            }

            result.add(id, value);
         }
      }

      return result;
   }

   public static JsonObject init_JsonObject(LivingEntity entity) {
      JsonObject value = new JsonObject();
      value.addProperty("entity_id", entity.getId());
      value.addProperty("type", BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString());
      value.add("pos", load(entity.position()));
      value.add("latest_pos", load(init_Vec3(entity)));
      value.add("motion", init(entity.getDeltaMovement()));
      value.add("drawn_step", init(new Vec3(entity.getX() - entity.xo, entity.getY() - entity.yo, entity.getZ() - entity.zo)));
      value.add("box", init(entity.getBoundingBox()));
      value.add("look", init(load(entity)));
      value.addProperty("yaw", entity.getYRot());
      value.addProperty("pitch", entity.getXRot());
      value.addProperty("eye_height", entity.getEyeHeight());
      value.addProperty("pose", entity.getPose().name());
      value.addProperty("fall_distance", entity.fallDistance);
      value.addProperty("armor", entity.getArmorValue());
      value.addProperty("health_observed", entity.getHealth());
      value.addProperty("absorption", entity.getAbsorptionAmount());
      value.addProperty("hurt", entity.hurtTime);
      value.addProperty("alive", entity.isAlive());
      value.addProperty("ground", entity.onGround());
      value.addProperty("sprint", entity.isSprinting());
      value.addProperty("using", entity.isUsingItem());
      value.addProperty("blocking", entity.isBlocking());
      value.addProperty("use_ticks", entity.getTicksUsingItem());
      value.addProperty("swinging", Compat263.isSwinging(entity));
      value.addProperty("swing_phase", Compat263.swingTime(entity));
      value.add("main", init(entity.getMainHandItem()));
      value.add("off", init(entity.getOffhandItem()));
      if (entity.getAttribute(Attributes.ATTACK_SPEED) != null) {
         value.addProperty("attack_speed", entity.getAttributeValue(Attributes.ATTACK_SPEED));
      }

      if (entity.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
         value.addProperty("attack_damage_observed", entity.getAttributeValue(Attributes.ATTACK_DAMAGE));
      }

      if (entity.getAttribute(Attributes.MOVEMENT_SPEED) != null) {
         value.addProperty("movement_attribute", entity.getAttributeValue(Attributes.MOVEMENT_SPEED));
      }

      JsonArray effects = new JsonArray();

      for (MobEffectInstance effect : entity.getActiveEffects()) {
         JsonObject e = new JsonObject();
         e.addProperty("id", BuiltInRegistries.MOB_EFFECT.getKey(effect.getEffect().value()).toString());
         e.addProperty("amplifier", effect.getAmplifier());
         e.addProperty("ticks", effect.getDuration());
         effects.add(e);
      }

      value.add("effects", effects);
      DamageSource source = entity.getLastDamageSource();
      if (source != null) {
         value.addProperty("last_damage_cause", source.getEntity() == null ? -1 : source.getEntity().getId());
      }

      return value;
   }

   private static JsonObject init(ItemStack stack) {
      JsonObject value = new JsonObject();
      value.addProperty("id", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
      value.addProperty("damage", stack.getDamageValue());
      value.addProperty("max_damage", stack.getMaxDamage());
      value.addProperty("use_animation", stack.getUseAnimation().name());
      JsonObject enchantments = new JsonObject();

      for (Entry<Holder<Enchantment>> enchantment : stack.getEnchantments().entrySet()) {
         enchantments.addProperty(enchantment.getKey().unwrapKey().map(k -> k.identifier().toString()).orElse("unknown"), enchantment.getIntValue());
      }

      value.add("enchantments", enchantments);
      BlocksAttacks blocks = stack.get(DataComponents.BLOCKS_ATTACKS);
      value.addProperty("block_data", blocks != null);
      if (blocks != null) {
         value.addProperty("raise_ticks", blocks.blockDelayTicks());
         JsonArray arcs = new JsonArray();

         for (DamageReduction reduction : blocks.damageReductions()) {
            arcs.add(reduction.horizontalBlockingAngle());
         }

         value.add("block_arcs", arcs);
      }

      AttackRange range = stack.get(DataComponents.ATTACK_RANGE);
      if (range != null) {
         value.addProperty("min_reach", range.minReach());
         value.addProperty("max_reach", range.maxReach());
         value.addProperty("hitbox_margin", range.hitboxMargin());
      }

      return value;
   }

   public static Vec3 init_Vec3(LivingEntity entity) {
      InterpolationHandler interpolation = entity.getInterpolation();
      return interpolation != null && interpolation.hasActiveInterpolation() ? Compat263.interpPos(interpolation) : entity.position();
   }

   private static Vec3 load(LivingEntity entity) {
      InterpolationHandler interpolation = entity.getInterpolation();
      boolean latest = interpolation != null && interpolation.hasActiveInterpolation();
      return Vec3.directionFromRotation(
         latest ? Compat263.interpXRot(interpolation) : entity.getXRot(), latest ? Compat263.interpYRot(interpolation) : entity.getYRot()
      );
   }

   public static JsonArray init(Vec3 value) {
      JsonArray result = new JsonArray();
      result.add(value.x);
      result.add(value.y);
      result.add(value.z);
      return result;
   }

   public static JsonArray load(Vec3 value) {
      return init(value.subtract(init_Vec3));
   }

   public static JsonArray init(AABB value) {
      JsonArray result = load(new Vec3(value.minX, value.minY, value.minZ));

      for (JsonElement coordinate : load(new Vec3(value.maxX, value.maxY, value.maxZ))) {
         result.add(coordinate);
      }

      return result;
   }
}
