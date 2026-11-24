package autismclient.util;

import autismclient.AutismClientAddon;
import autismclient.api.ai.AiOperation;
import autismclient.api.module.Setting;
import autismclient.compat263.CompatSwingPacket;
import autismclient.mixin.accessor.AutismKeyMappingAccessor;
import autismclient.mixin.accessor.AutismMinecraftAccessor;
import autismclient.mixin.accessor.AutismMultiPlayerGameModeAccessor;
import autismclient.mixin.accessor.AutismWindowAccessor;
import autismclient.modules.Module;
import autismclient.modules.f;
import autismclient.modules.j;
import autismclient.util.macro.MacroExecutor;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.User;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerData.Type;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundDamageEventPacket;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundClientTickEndPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundPunchPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class w {
   private static Path init_Path;
   private static String init_Str;
   private static String load_Str;
   private static int init_I;
   private static final long[] init_JArr = new long[2];
   private static final int[] init_IArr = new int[2];
   private static final String[] init_StrArr = new String[2];
   private static boolean init_Z;
   private static boolean load_Z;
   private static volatile String read_Str = "";
   private static boolean read_Z;
   private static volatile boolean write_Z;
   private static final ConcurrentMap<String, Set<BlockPos>> init_ConcurrentMap = new ConcurrentHashMap<>();
   private static final ConcurrentMap<String, Double> load_ConcurrentMap = new ConcurrentHashMap<>();
   private static final ConcurrentMap<String, String> read_ConcurrentMap = new ConcurrentHashMap<>();
   private static JsonObject init_JsonObject;
   private static BufferedWriter init_BufferedWriter;
   private static BufferedWriter load_BufferedWriter;
   private static BufferedWriter read_BufferedWriter;
   private static BufferedWriter write_BufferedWriter;
   private static BufferedWriter update_BufferedWriter;
   private static BufferedWriter sync_BufferedWriter;
   private static BufferedWriter flush_BufferedWriter;
   private static BufferedWriter close_BufferedWriter;
   private static BufferedWriter open_BufferedWriter;
   private static final ConcurrentLinkedQueue<String> init_ConcurrentLinkedQu = new ConcurrentLinkedQueue<>();
   private static final ConcurrentLinkedQueue<String> load_ConcurrentLinkedQu = new ConcurrentLinkedQueue<>();
   private static final ConcurrentLinkedQueue<String> read_ConcurrentLinkedQu = new ConcurrentLinkedQueue<>();
   private static final Map<Packet<?>, ce.Inner_DataUtil> init_Map = Collections.synchronizedMap(new WeakHashMap<>());
   private static String write_Str = "";
   private static String update_Str = "";
   private static String sync_Str = "";
   private static int load_I;
   private static String flush_Str = "";
   private static String close_Str = "";
   private static String open_Str = "";
   private static String poll_Str = "";
   private static String push_Str = "";
   private static String pop = "";
   private static int read_I;
   private static int write_I;
   private static final IdentityHashMap<KeyMapping, Boolean> init_IdentityHashMap = new IdentityHashMap<>();
   private static BufferedWriter poll_BufferedWriter;
   private static BufferedWriter push_BufferedWriter;
   private static volatile boolean update_Z;
   private static final ConcurrentLinkedQueue<String> write_ConcurrentLinkedQu = new ConcurrentLinkedQueue<>();
   private static final Map<String, Field> load_Map = new HashMap<>();
   private static Method init_Method;
   private static Vec3 init_Vec3;
   private static float init_F = 0.0F / 0.0F;
   private static float load_F = 0.0F / 0.0F;
   private static double init_D;
   private static double load_D;
   private static int update_I;
   private static int sync_I;
   private static String tick = "";
   private static final AtomicLong init_AtomicLong = new AtomicLong();
   private static final AtomicLong load_AtomicLong = new AtomicLong();
   private static long init_J;
   private static final Map<Packet<?>, JsonObject> read_Map = new IdentityHashMap<>();
   private static double read_D = 0.0 / 0.0;
   private static double write_D = 0.0 / 0.0;
   private static int flush_I;
   private static String run = "";

   private w() {
   }

   public static boolean init_Z() {
      return init_Path != null && Boolean.getBoolean("autism.combat.server.backgroundMouse");
   }

   public static boolean load() {
      if (!init_Z()) {
         return false;
      }

      init_J++;
      return true;
   }

   public static boolean read() {
      return init_Path != null && "attacker".equals(init_Str);
   }

   public static void init(String kind, Vec3 before, Vec3 after) {
      if (init_Path != null && Boolean.getBoolean("autism.combat.server.online")) {
         try {
            JsonObject row = new JsonObject();
            row.addProperty("phase", read_Str);
            row.addProperty("tick", ck.init_ck().state_I());
            row.addProperty("kind", kind);
            row.addProperty("before", before.toString());
            row.addProperty("after", after.toString());
            Files.writeString(init_Path.resolve("clutch-receipts.jsonl"), row + "\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND);
         } catch (IOException failure) {
            throw new UncheckedIOException(failure);
         }
      }
   }

   public static Boolean init(KeyMapping mapping) {
      return init_Path == null
            || init_JsonObject == null
            || !init("mode", "idle").equals("danger")
               && !init("mode", "idle").equals("clutch")
               && !Boolean.parseBoolean(init("manualUse", "false"))
               && !Boolean.parseBoolean(init("spamClicks", "false"))
               && !init_JsonObject.has("holdAttack")
         ? null
         : init_IdentityHashMap.get(mapping);
   }

   private static void init(KeyMapping mapping, boolean down) {
      init_IdentityHashMap.put(mapping, down);
      mapping.setDown(down);
   }

   public static void init(Packet<?> packet) {
      if (init_Path != null && "attacker".equals(init_Str)) {
         if (write()) {
            JsonObject row = new JsonObject();
            row.addProperty("time", System.currentTimeMillis());
            row.addProperty("phase", read_Str);
            row.addProperty("tick", ck.init_ck().state_I());
            if (packet instanceof ServerboundAttackPacket attack) {
               row.addProperty("kind", "attack");
               row.addProperty("entity", attack.entityId());
               synchronized (read_Map) {
                  JsonObject pose = read_Map.remove(packet);
                  if (pose != null && read_Str.equals(pose.get("phase").getAsString())) {
                     row.add("duelPose", pose);
                  }
               }
            } else if (packet instanceof ServerboundUseItemOnPacket use) {
               row.addProperty("kind", "useOn");
               ch.Inner_CachePool useWire = ch.init_ch_CachePool();
               if (useWire.init_Z()) {
                  row.addProperty("yaw", useWire.read());
                  row.addProperty("pitch", useWire.write());
               }

               row.addProperty("hand", use.hand().name());
               row.addProperty("support", use.hitResult().getBlockPos().toString());
               row.addProperty("face", use.hitResult().getDirection().name());
               row.addProperty("sequence", use.sequence());
            } else if (packet instanceof ServerboundUseItemPacket use) {
               row.addProperty("kind", "use");
               row.addProperty("hand", use.hand().name());
            } else if (packet instanceof ServerboundSetCarriedItemPacket carried) {
               row.addProperty("kind", "slot");
               row.addProperty("slot", carried.getSlot());
            } else if (packet instanceof ServerboundMovePlayerPacket move) {
               row.addProperty("kind", "move");
               row.addProperty("position", move.hasPosition());
               row.addProperty("rotation", move.hasRotation());
               if (move.hasPosition()) {
                  row.addProperty("x", move.getX(0.0));
                  row.addProperty("y", move.getY(0.0));
                  row.addProperty("z", move.getZ(0.0));
               }

               if (move.hasRotation()) {
                  row.addProperty("yaw", move.getYRot(0.0F));
                  row.addProperty("pitch", move.getXRot(0.0F));
               }
            } else {
               row = null;
            }

            if (row != null) {
               init(update_BufferedWriter, row);
            }
         }

         if (Boolean.getBoolean("autism.tools.server.check")) {
            init_ConcurrentLinkedQu.offer(
               String.format(Locale.ROOT, "%d,%s,%d,%s%n", System.currentTimeMillis(), read_Str, ck.init_ck().state_I(), packet.getClass().getSimpleName())
            );
         }

         if (packet instanceof ServerboundAttackPacket) {
            init_AtomicLong.incrementAndGet();
         }

         if (packet instanceof CompatSwingPacket || packet instanceof ServerboundPunchPacket) {
            load_AtomicLong.incrementAndGet();
         }

         String guardKind = packet instanceof ServerboundAttackPacket
            ? "attack"
            : (
               packet instanceof CompatSwingPacket || packet instanceof ServerboundPunchPacket
                  ? "swing"
                  : (
                     packet instanceof ServerboundClientTickEndPacket
                        ? "end"
                        : (
                           packet instanceof ServerboundMovePlayerPacket
                              ? "move"
                              : (
                                 packet instanceof ServerboundUseItemPacket
                                    ? "use"
                                    : (packet instanceof ServerboundPlayerActionPacket action && action.getAction() == Action.RELEASE_USE_ITEM ? "release" : "")
                              )
                        )
                  )
            );
         if (!guardKind.isEmpty()) {
            LocalPlayer player = Minecraft.getInstance().player;
            load_ConcurrentLinkedQu.offer(
               String.format(
                  Locale.ROOT,
                  "%d,%s,%d,%s,%s,%s%n",
                  System.currentTimeMillis(),
                  read_Str,
                  ck.init_ck().state_I(),
                  guardKind,
                  player != null && player.isUsingItem(),
                  s.init_Str()
               )
            );
         }

         if (packet instanceof ServerboundMovePlayerPacket movement && movement.hasRotation()) {
            read_ConcurrentLinkedQu.offer(
               String.format(
                  Locale.ROOT,
                  "%d,%s,%d,%s,%s,%s%n",
                  System.currentTimeMillis(),
                  read_Str,
                  ck.init_ck().state_I(),
                  Float.toString(movement.getYRot(0.0F)),
                  Float.toString(movement.getXRot(0.0F)),
                  aD.init_Str()
               )
            );
         }

         if (update_Z) {
            read(packet);
         }

         if (read_Z && write_Z && packet instanceof ServerboundUseItemOnPacket use) {
            init_ConcurrentMap.computeIfAbsent(read_Str, ignored -> ConcurrentHashMap.newKeySet())
               .add(use.hitResult().getBlockPos().relative(use.hitResult().getDirection()).immutable());
         }
      }
   }

   public static boolean write() {
      return init_Path != null && init_JsonObject != null && Boolean.parseBoolean(init("auraTrace", "false"));
   }

   public static void init(ClientboundExplodePacket packet) {
      if (write()) {
         JsonObject row = new JsonObject();
         row.addProperty("time", System.currentTimeMillis());
         row.addProperty("phase", read_Str);
         row.addProperty("tick", ck.init_ck().state_I());
         row.addProperty("role", init_Str);
         row.addProperty("center", packet.center().toString());
         init(sync_BufferedWriter, row);
      }
   }

   public static void init(ClientboundDamageEventPacket packet) {
      if (write()) {
         Minecraft mc = Minecraft.getInstance();
         if (mc.level != null) {
            JsonObject row = new JsonObject();
            row.addProperty("time", System.currentTimeMillis());
            row.addProperty("phase", read_Str);
            row.addProperty("tick", ck.init_ck().state_I());
            row.addProperty("role", init_Str);
            row.addProperty("entity", packet.entityId());
            row.addProperty("local", mc.player != null && packet.entityId() == mc.player.getId());
            row.addProperty("source", packet.getSource(mc.level).getMsgId());
            if (mc.level.getEntity(packet.entityId()) instanceof LivingEntity living) {
               row.addProperty("health", living.getHealth());
               row.addProperty("absorption", living.getAbsorptionAmount());
            }

            init(flush_BufferedWriter, row);
         }
      }
   }

   public static void init(ClientboundSetHealthPacket packet) {
      if (write()) {
         Minecraft mc = Minecraft.getInstance();
         if (mc.player != null) {
            JsonObject row = new JsonObject();
            row.addProperty("time", System.currentTimeMillis());
            row.addProperty("phase", read_Str);
            row.addProperty("tick", ck.init_ck().state_I());
            row.addProperty("role", init_Str);
            row.addProperty("entity", mc.player.getId());
            row.addProperty("local", true);
            row.addProperty("source", "setHealth");
            row.addProperty("health", packet.getHealth());
            row.addProperty("absorption", mc.player.getAbsorptionAmount());
            init(flush_BufferedWriter, row);
         }
      }
   }

   private static void init(BufferedWriter writer, JsonObject row) {
      if (writer != null) {
         try {
            writer.write(row + "\n");
            writer.flush();
         } catch (IOException failure) {
            throw new UncheckedIOException(failure);
         }
      }
   }

   public static void init_V() {
      String dir = System.getProperty("autism.combat.server.dir", "");
      String who = System.getProperty("autism.combat.server.role", "");
      if (!dir.isBlank()
         && (who.equals("attacker") || who.equals("target"))
         && Minecraft.getInstance().gameDirectory.toString().contains("autism-ai-qualification-")) {
         init_Path = Path.of(dir);
         init_Str = who;
         read_Z = Boolean.getBoolean("autism.combat.server.local");
         load_Str = who.equals("attacker") ? "CombatAttacker" : "CombatTarget";
         ClientTickEvents.START_CLIENT_TICK.register(mc -> init(mc, "tick-start"));
         ClientTickEvents.END_CLIENT_TICK.register(w::init_V);
         if (read_Z) {
            ServerTickEvents.END_SERVER_TICK.register(server -> {
               if (write_Z && server == Minecraft.getInstance().getSingleplayerServer()) {
                  for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                     load_ConcurrentMap.merge(read_Str, player.getY(), Math::min);
                  }
               }
            });
         }
      }
   }

   private static void init_V(Minecraft mc) {
      try {
         if (mc.level == null) {
            if (!read_Z && !init_Z && ++init_I >= 100 && mc.gui.screen() != null) {
               init_Z = true;
               if (!Boolean.getBoolean("autism.combat.server.online")) {
                  FieldCache.init(new User(load_Str, UUIDUtil.createOfflinePlayerUUID(load_Str), "", Optional.empty(), Optional.empty()));
               }

               String address = System.getProperty("autism.combat.server.address", "127.0.0.1:25565");
               ConnectScreen.startConnecting(
                  mc.gui.screen(), mc, ServerAddress.parseString(address), new ServerData("Local combat qualification", address, Type.OTHER), false, null
               );
            }

            return;
         }

         if (mc.player == null) {
            return;
         }

         init(mc, "tick-end-before-driver");
         if (!mc.player.isAlive()) {
            if (++init_I % 20 == 0) {
               mc.player.respawn();
            }

            return;
         }

         if (!load_Z) {
            load_Z = true;

            for (Module module : autismclient.modules.al.init_Collection()) {
               if (module.isEnabled()) {
                  module.setEnabled(false);
               }
            }

            Files.createDirectories(init_Path);
            poll_BufferedWriter = Files.newBufferedWriter(init_Path.resolve(init_Str + ".csv"));
            if (init_Str.equals("attacker")) {
               if (Boolean.getBoolean("autism.tools.server.check")) {
                  write_BufferedWriter = Files.newBufferedWriter(init_Path.resolve("tools-packets.csv"));
                  write_BufferedWriter.write("time,phase,tick,packet\n");
               }

               load_BufferedWriter = Files.newBufferedWriter(init_Path.resolve("movement-packets.csv"));
               load_BufferedWriter.write("time,phase,tick,yaw,pitch,owner\n");
               read_BufferedWriter = Files.newBufferedWriter(init_Path.resolve("guard-packets.csv"));
               read_BufferedWriter.write("time,phase,tick,kind,using,owner\n");
               update_BufferedWriter = Files.newBufferedWriter(init_Path.resolve("aura-packets.jsonl"));
            }

            sync_BufferedWriter = Files.newBufferedWriter(init_Path.resolve(init_Str + "-aura-explosions.jsonl"));
            flush_BufferedWriter = Files.newBufferedWriter(init_Path.resolve(init_Str + "-aura-damage.jsonl"));
            poll_BufferedWriter.write(
               "time,phase,tick,x,y,z,speed,targetSpeed,distance,selected,onBox,yawError,pitchError,yaw,pitch,attacking,hurtTime,attacks,swings,hand,observerYaw,ready,window,vmx,vmy,vmz,pYaw,pPitch,zza,xxa,onGround,sprint,pHurt,keyF,keyB,keyL,keyR,keyJump,keySprint,keyShift,moveYaw,packetYaw,liveYaw,moveTick,wireYawSync,atkTold,atkServerSprint,atkStrength,atkSlowed,atkLive,slowCount,guard,guardUp,using,ex,ey,ez\n"
            );
         }

         init_I++;
         if (init_Str.equals("attacker")) {
            load(mc);
         }

         if (write_BufferedWriter != null) {
            String row;
            while ((row = init_ConcurrentLinkedQu.poll()) != null) {
               write_BufferedWriter.write(row);
            }

            write_BufferedWriter.flush();
            if (init_JsonObject != null && Boolean.parseBoolean(init("postProbe", "false"))) {
               mc.getConnection().send(new ServerboundSetCarriedItemPacket(init_I % 2));
            }
         }

         if (load_BufferedWriter != null) {
            String row;
            while ((row = read_ConcurrentLinkedQu.poll()) != null) {
               load_BufferedWriter.write(row);
            }

            load_BufferedWriter.flush();
         }

         if (read_BufferedWriter != null) {
            String row;
            while ((row = load_ConcurrentLinkedQu.poll()) != null) {
               read_BufferedWriter.write(row);
            }

            read_BufferedWriter.flush();
         }

         Path command = init_Path.resolve(init_Str + "-control.json");
         if (Files.isRegularFile(command)) {
            init_JsonObject = JsonParser.parseString(Files.readString(command)).getAsJsonObject();
         }

         if (init_JsonObject == null) {
            return;
         }

         update_Z = init_Str.equals("attacker") && Boolean.parseBoolean(init("scaffoldTrace", "false"));
         String nextPhase = init("phase", "idle");
         if (!nextPhase.equals(read_Str)) {
            read_Str = nextPhase;
            read(mc);
         }

         if (init_JsonObject.has("moduleEnabled")) {
            for (Entry<String, JsonElement> entry : init_JsonObject.getAsJsonObject("moduleEnabled").entrySet()) {
               Module changed = autismclient.modules.al.init_Module_Str(entry.getKey());
               if (changed != null) {
                  changed.setEnabled(entry.getValue().getAsBoolean());
               }
            }
         }

         if (init_JsonObject.has("cameraYaw")) {
            mc.player.setYRot(mc.player.getYRot() + Mth.wrapDegrees((float)init("cameraYaw", mc.player.getYRot()) - mc.player.getYRot()));
         }

         if (mc.gui.screen() != null && !Boolean.parseBoolean(init("keepScreen", "false"))) {
            mc.gui.setScreen(null);
         }

         if (init_Str.equals("attacker")) {
            ((AutismWindowAccessor)(Object)mc.getWindow()).autism$setFocused(true);
            if (!mc.mouseHandler.isMouseGrabbed()) {
               mc.mouseHandler.grabMouse();
            }
         }

         Player enemy = mc.level
            .players()
            .stream()
            .filter(p -> p != mc.player && p.getName().getString().equals(init_Str.equals("attacker") ? "CombatTarget" : "CombatAttacker"))
            .findFirst()
            .orElse(null);
         init(mc, enemy);
         if (init_Str.equals("attacker")
            && init_JsonObject.has("auraAtBedTicks")
            && !read_Str.equals(close_Str)
            && autismclient.modules.al.init_Module_Str("bed-nuker") instanceof autismclient.modules.UtilCore finishing
            && autismclient.modules.UtilCore.read_Z_10()
            && finishing.init_D_6() > 0.0
            && finishing.init_D_6() <= init("auraAtBedTicks", 2.0)) {
            close_Str = read_Str;
            autismclient.modules.al.init_Module_Str("kill-aura").setEnabled(true);
         }

         if (init_Str.equals("attacker")
            && Boolean.parseBoolean(init("bedShot", "false"))
            && !read_Str.equals(flush_Str)
            && init_I % 8 == 0
            && autismclient.modules.al.init_Module_Str("bed-nuker") instanceof autismclient.modules.UtilCore nuker
            && nuker.init_BlockPos_4() != null) {
            flush_Str = read_Str;
            Screenshot.grab(init_Path.toFile(), "bed-" + read_Str + ".png", mc.gameRenderer.mainRenderTarget(), 1, component -> {});
         }

         boolean var10000;
         switch (init("mode", "idle")) {
            case "crystal-aura":
               var10000 = !autismclient.modules.t.init_List_18().isEmpty();
               break;
            case "anchor-aura":
               var10000 = !autismclient.modules.BufferHelper.init_List().isEmpty();
               break;
            default:
               var10000 = true;
         }

         boolean auraPreviewVisible = var10000;
         if (init_Str.equals("attacker") && Boolean.parseBoolean(init("auraShot", "false")) && auraPreviewVisible && !read_Str.equals(update_Str)) {
            update_Str = read_Str;
            Screenshot.grab(init_Path.toFile(), "aura-" + read_Str + ".png", mc.gameRenderer.mainRenderTarget(), 1, component -> {});
         }

         if (init_Str.equals("attacker")
            && Boolean.parseBoolean(init("backtrackShot", "false"))
            && !read_Str.equals(sync_Str)
            && autismclient.modules.al.init_Module_Str("backtrack") instanceof autismclient.modules.TaskTimer backtrack) {
            List<autismclient.modules.FileCache> previews = autismclient.modules.TaskTimer.init_List_9();
            boolean duel = read_Str.contains("-duel-");
            autismclient.modules.TaskTimer.Inner_StreamUtil tracking = backtrack.init_TaskTimer_StreamUt();
            if (tracking.init_Z() && previews.size() >= 3 && (!duel || tracking.load_I() > 0 && tracking.load_Z())) {
               if (++load_I >= (duel ? 1 : 3)) {
                  sync_Str = read_Str;
                  load_I = 0;
                  JsonObject record = new JsonObject();
                  record.addProperty("phase", read_Str);
                  record.addProperty("pending", backtrack.init_TaskTimer_StreamUt().load_I());
                  record.addProperty("style", backtrack.value("render-style"));
                  record.addProperty("boxes", previews.stream().filter(p -> p.init_AABB() != null).count());
                  record.addProperty("lines", previews.stream().filter(p -> p.init_Vec3() != null).count());
                  Files.writeString(init_Path.resolve(read_Str + "-visual.json"), record.toString());
                  Screenshot.grab(init_Path.toFile(), "backtrack-" + read_Str + ".png", mc.gameRenderer.mainRenderTarget(), 1, component -> {});
               }
            } else {
               load_I = 0;
            }
         }

         init(mc, "tick-end-after-driver");
         if (init_Str.equals("attacker") && Boolean.parseBoolean(init("rotationTrace", "false"))) {
            if (open_BufferedWriter == null) {
               open_BufferedWriter = Files.newBufferedWriter(init_Path.resolve("aura-rotation-states.jsonl"));
            }

            JsonObject state = new JsonObject();
            ce.Inner_DataUtil rotation = cn.init_ce_DataUtil(mc.player);
            state.addProperty("time", System.currentTimeMillis());
            state.addProperty("phase", read_Str);
            state.addProperty("tick", ck.init_ck().state_I());
            state.addProperty("owner", aD.init_Str());
            state.addProperty("windingDown", aD.init_Z());
            state.addProperty("cameraYaw", mc.player.getYRot());
            state.addProperty("cameraPitch", mc.player.getXRot());
            if (rotation != null) {
               state.addProperty("yaw", rotation.init());
               state.addProperty("pitch", rotation.load());
            }

            open_BufferedWriter.write(state + "\n");
            open_BufferedWriter.flush();
         }

         if (init_Str.equals("attacker")) {
            load(mc, enemy);
         }

         if (init_Str.equals("attacker") && init("mode", "idle").equals("danger")) {
            autismclient.modules.z danger = autismclient.modules.z.init_z();
            if (init_BufferedWriter == null) {
               init_BufferedWriter = Files.newBufferedWriter(init_Path.resolve("danger.csv"));
               init_BufferedWriter.write(
                  "time,phase,tick,health,x,y,z,yaw,pitch,wireYaw,wirePitch,owner,reserved,returns,threats,status,arrival,distance,ballSpeed,ballX,ballY,ballZ,rayHit,returnGate\n"
               );
            }

            ch.Inner_CachePool wire = ch.init_ch_CachePool();
            autismclient.modules.z.Inner_CachePool threat = danger.load_List_4().isEmpty() ? null : danger.load_List_4().get(0);
            Vec3 eye = mc.player.getEyePosition();
            boolean rayHit = threat != null
               && threat.init_Entity().getBoundingBox().clip(eye, eye.add(Vec3.directionFromRotation(wire.write(), wire.read()).scale(2.9))).isPresent();
            init_BufferedWriter.write(
               String.format(
                  Locale.ROOT,
                  "%d,%s,%d,%.1f,%.4f,%.4f,%.4f,%.3f,%.3f,%.3f,%.3f,%s,%s,%d,%d,%s,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f,%s,%s%n",
                  System.currentTimeMillis(),
                  read_Str,
                  init_I,
                  mc.player.getHealth(),
                  mc.player.getX(),
                  mc.player.getY(),
                  mc.player.getZ(),
                  mc.player.getYRot(),
                  mc.player.getXRot(),
                  wire.read(),
                  wire.write(),
                  aD.init_Str(),
                  autismclient.modules.z.sync_Z_12(),
                  danger.init_I_31(),
                  danger.load_List_4().size(),
                  danger.init_Str_17(),
                  threat == null ? -1.0 : threat.init_D(),
                  threat == null ? -1.0 : threat.load_D(),
                  threat == null ? 0.0 : threat.init_Entity().getDeltaMovement().length(),
                  threat == null ? 0.0 : threat.init_Entity().getX(),
                  threat == null ? 0.0 : threat.init_Entity().getY(),
                  threat == null ? 0.0 : threat.init_Entity().getZ(),
                  rayHit,
                  danger.load_Str_7()
               )
            );
            init_BufferedWriter.flush();
            if ((danger.close_Z_9() || danger.open_Z_6()) && !write_Str.equals(read_Str)) {
               write_Str = read_Str;
               write_I = init_I + 3;
            }

            if (write_I != 0 && init_I >= write_I) {
               write_I = 0;
               Screenshot.grab(init_Path.toFile(), "danger-" + read_Str + ".png", mc.gameRenderer.mainRenderTarget(), 1, component -> {});
            }
         }

         ch.Inner_CachePool wireNow = ch.init_ch_CachePool();
         if (wireNow.init_Z()) {
            if (!read_Str.equals(tick)) {
               tick = read_Str;
               load_D = 0.0;
               init_D = 0.0;
               sync_I = 0;
               update_I = 0;
            }

            float yawNow = wireNow.read();
            float pitchNow = wireNow.write();
            String owner = aD.init_Str();
            if (!Float.isNaN(init_F) && owner != null) {
               double step = Math.max(Math.abs(Mth.wrapDegrees(yawNow - init_F)), Math.abs(pitchNow - load_F));
               if (owner.equals("clutch")) {
                  init_D = Math.max(init_D, step);
                  if (step > 45.0) {
                     update_I++;
                  }
               }

               if (owner.equals("danger")) {
                  load_D = Math.max(load_D, step);
                  if (step > 45.0) {
                     sync_I++;
                  }
               }
            }

            init_F = yawNow;
            load_F = pitchNow;
         }

         if (init_Str.equals("attacker") && read_Str.contains("backtrack")) {
            if (close_BufferedWriter == null) {
               close_BufferedWriter = Files.newBufferedWriter(init_Path.resolve("backtrack-states.jsonl"));
            }

            JsonObject sample = new JsonObject();
            sample.addProperty("time", System.currentTimeMillis());
            sample.addProperty("phase", read_Str);
            sample.add("backtrack", init_JsonObject(mc));
            close_BufferedWriter.write(sample + "\n");
            close_BufferedWriter.flush();
         }

         if (init_I % 10 == 0
            || Boolean.getBoolean("autism.combat.server.techniques")
            || init("mode", "idle").equals("clutch")
            || init("mode", "idle").equals("bed-nuker")
            || init("mode", "idle").equals("anti-knockback")) {
            JsonObject state = new JsonObject();
            state.addProperty("phase", read_Str);
            if (write()) {
               for (String id : new String[]{"crystal-aura", "anchor-aura"}) {
                  Module aura = autismclient.modules.al.init_Module_Str(id);
                  if (aura != null) {
                     state.add(id + "Diagnostic", init(aura));
                  }
               }
            }

            state.add("backtrack", init_JsonObject(mc));
            if (autismclient.modules.al.init_Module_Str("bed-nuker") instanceof autismclient.modules.UtilCore nuker) {
               state.addProperty("bedStatus", nuker.info());
               state.addProperty("bedTarget", String.valueOf(nuker.init_BlockPos_4()));
               state.addProperty("bedRemaining", nuker.init_D_6());
               state.addProperty("bedMining", autismclient.modules.UtilCore.read_Z_10());
               state.addProperty("bedFace", nuker.init_Str_6());
               state.addProperty("bedMoveFix", Boolean.parseBoolean(nuker.value("move-fix")));
               state.addProperty("bedOwn", Boolean.parseBoolean(nuker.value("own-bed")));
               if (mc.gameMode instanceof AutismMultiPlayerGameModeAccessor mining) {
                  state.addProperty("destroying", mining.autism$isDestroying());
                  state.addProperty("destroyProgress", mining.autism$getDestroyProgress());
                  state.addProperty("destroyBlock", String.valueOf(mining.autism$getDestroyBlockPos().immutable()));
               }

               Module bedEsp = autismclient.modules.al.init_Module_Str("bed-esp");
               if (bedEsp != null) {
                  state.addProperty("bedEspEnabled", bedEsp.isEnabled());
                  JsonObject settings = new JsonObject();

                  for (Setting<?, ?> setting : bedEsp.settings()) {
                     settings.addProperty(setting.id(), bedEsp.value(setting.id()));
                  }

                  state.add("bedEspSettings", settings);
               }
            }

            Module autoTool = autismclient.modules.al.init_Module_Str("auto-tool");
            if (autoTool != null) {
               state.addProperty("autoToolEnabled", autoTool.isEnabled());
               JsonObject toolSettings = new JsonObject();

               for (Setting<?, ?> setting : autoTool.settings()) {
                  toolSettings.addProperty(setting.id(), autoTool.value(setting.id()));
               }

               state.add("autoToolSettings", toolSettings);
            }

            if (autismclient.modules.al.init_Module_Str("anti-knockback") instanceof autismclient.modules.StateCache anti) {
               state.addProperty("knockbackHits", anti.init_J_4());
               state.addProperty("jumpResets", anti.load_J());
               state.addProperty("counterFrames", anti.read());
            }

            if (write_BufferedWriter != null) {
               state.addProperty("toolBusy", autismclient.ai.o.init_Z());
               state.addProperty("toolId", autismclient.ai.o.init_Str());
               AiOperation.Result result = autismclient.ai.o.init_AiOperation_Result();
               if (result != null) {
                  state.addProperty("toolStatus", result.status().name());
                  state.addProperty("toolMessage", result.message());
                  state.addProperty("toolData", result.dataJson());
               }
            }

            state.addProperty("clutchMaxStep", init_D);
            state.addProperty("clutchSnaps", update_I);
            state.addProperty("dangerMaxStep", load_D);
            state.addProperty("dangerSnaps", sync_I);
            state.addProperty("name", load_Str);
            state.addProperty("tick", init_I);
            state.addProperty("time", System.currentTimeMillis());
            state.addProperty("x", mc.player.getX());
            state.addProperty("y", mc.player.getY());
            state.addProperty("z", mc.player.getZ());
            state.addProperty("vmx", mc.player.getDeltaMovement().x);
            state.addProperty("vmz", mc.player.getDeltaMovement().z);
            state.addProperty("vmy", mc.player.getDeltaMovement().y);
            state.addProperty("onGround", mc.player.onGround());
            state.addProperty("health", mc.player.getHealth());
            state.addProperty("usingItem", mc.player.isUsingItem());
            state.addProperty("blocking", mc.player.isBlocking());
            state.addProperty("swordBlockFrames", UtilCore.init_J());
            state.addProperty("guardUseRefusals", s.init_J());
            state.addProperty("pearls", mc.player.getInventory().countItem(Items.ENDER_PEARL));
            state.addProperty("selectedSlot", mc.player.getInventory().getSelectedSlot());
            state.addProperty("yaw", mc.player.getYRot());
            state.addProperty("pitch", mc.player.getXRot());
            ch.Inner_CachePool writtenRotation = ch.init_ch_CachePool();
            if (writtenRotation.init_Z()) {
               state.addProperty("wireYaw", ch.init_F());
            }

            state.addProperty("mainHand", BuiltInRegistries.ITEM.getKey(mc.player.getMainHandItem().getItem()).toString());
            if (autismclient.modules.al.init_Module_Str("kill-aura") instanceof autismclient.modules.aa liveAura) {
               state.addProperty("auraEnabled", liveAura.isEnabled());
               state.addProperty("auraTarget", liveAura.isEnabled() && liveAura.init_LivingEntity_3() != null);
            }

            state.addProperty("focused", mc.isWindowActive());
            if (Boolean.getBoolean("autism.combat.server.online")) {
               state.addProperty("playerName", mc.player.getName().getString());
               state.addProperty("yaw", mc.player.getYRot());
               state.addProperty("pitch", mc.player.getXRot());
               state.addProperty("screen", mc.gui.screen() == null ? "" : mc.gui.screen().getTitle().getString());
               JsonArray items = new JsonArray();

               for (Slot slot : mc.player.containerMenu.slots) {
                  if (!slot.getItem().isEmpty()) {
                     JsonObject item = new JsonObject();
                     item.addProperty("slot", slot.index);
                     item.addProperty("name", slot.getItem().getHoverName().getString());
                     items.add(item);
                  }
               }

               state.add("menuItems", items);
            }

            j clutch = j.init_j();
            if (clutch != null && !Boolean.getBoolean("autism.combat.server.techniques")) {
               state.addProperty("clutchReserved", j.init_Z_49());
               state.addProperty("clutchMovement", j.read_Z_20());
               state.addProperty("clutchJumpArmed", clutch.poll_Z_5());
               state.addProperty("clutchStatus", clutch.init_Str_16());
               state.addProperty("clutchClicks", clutch.load_I_15());
               state.addProperty("clutchRescues", clutch.init_I_29());

               for (String fieldName : new String[]{"impact", "motionTick", "plan", "returning", "clickGate", "lastCatch", "catchY", "brakeX", "detect"}) {
                  Field field = clutch.getClass().getDeclaredField(fieldName);
                  field.setAccessible(true);
                  state.addProperty("clutch" + fieldName, String.valueOf(field.get(clutch)));
               }
            }

            state.addProperty("mouseGrabbed", mc.mouseHandler.isMouseGrabbed());
            state.addProperty("backgroundMouse", init_Z());
            state.addProperty("desktopCursorCallsSuppressed", init_J);
            Module scaffold = autismclient.modules.al.init_Module_Str("scaffold");
            state.addProperty("scaffoldEnabled", scaffold != null && scaffold.isEnabled());
            state.addProperty("scaffoldMode", scaffold == null ? "" : scaffold.value("mode"));
            state.addProperty("tellyOwnsInput", autismclient.modules.aI.read_Z_12());
            state.addProperty("physicalForward", aB.init(mc.options.keyUp).autism$isActuallyDown());

            for (String id : new String[]{"kb-displacement", "w-tapping"}) {
               Module technique = autismclient.modules.al.init_Module_Str(id);
               state.addProperty(id + "Enabled", technique != null && technique.isEnabled());
               state.addProperty(id + "Info", technique == null ? "" : technique.info());
            }

            state.addProperty("effectiveForward", mc.player.input.keyPresses.forward());
            state.addProperty("effectiveSprint", mc.player.input.keyPresses.sprint());
            state.addProperty("liveSprint", mc.player.isSprinting());
            state.addProperty("serverSprint", ci.init_Z());
            state.addProperty("wTapRelease", autismclient.modules.bg.init_Z_36());
            state.addProperty("kbReserved", autismclient.modules.X.init_Z_24());
            state.addProperty("rotationOwner", String.valueOf(aD.init_Str()));
            state.addProperty("screenOpen", mc.gui.screen() != null);
            state.addProperty("kbPlan", autismclient.modules.X.load_Str_2());
            state.addProperty("kbLast", autismclient.modules.X.init_Str_7());
            state.addProperty("mainHand", mc.player.getMainHandItem().toString());
            if (scaffold != null) {
               for (String fieldName : new String[]{
                  "tellyPhase",
                  "tellyMotion",
                  "tellyLaneCenter",
                  "tellyGroundSteerOffset",
                  "tellyWalkingBridge",
                  "tellyDiagonalBridge",
                  "tellyTarget",
                  "grimAttemptResult"
               }) {
                  try {
                     Field field = scaffold.getClass().getDeclaredField(fieldName);
                     field.setAccessible(true);
                     state.addProperty(fieldName, String.valueOf(field.get(scaffold)));
                  } catch (ReflectiveOperationException var17) {
                  }
               }
            }

            autismclient.modules.z danger = autismclient.modules.z.init_z();
            if (danger != null) {
               state.addProperty("returns", danger.init_I_31());
               state.addProperty("threats", danger.load_List_4().size());
               state.addProperty("reserved", autismclient.modules.z.sync_Z_12());
               JsonArray predictions = new JsonArray();

               for (autismclient.modules.A.Inner_BufferHelper warning : danger.read_List_2()) {
                  JsonObject entry = new JsonObject();
                  entry.addProperty("kind", warning.init_A_StreamUtil().name());
                  entry.addProperty("preview", warning.init_Z());
                  entry.addProperty("tint", Integer.toHexString(warning.init_I()));
                  entry.addProperty("color", Integer.toHexString(danger.init_I_A_BufferHelper(warning)));
                  entry.addProperty("distance", mc.player.position().distanceTo(warning.init_Entity().position()));
                  entry.addProperty("indicator", danger.init_Z_A_StreamUtil_Str(warning.init_A_StreamUtil(), "hud"));
                  entry.addProperty("trajectory", danger.init_Z_A_StreamUtil_Str(warning.init_A_StreamUtil(), "trajectory"));
                  entry.addProperty("owner", warning.init_Player() == null ? "" : warning.init_Player().getGameProfile().name());
                  Vec3 landing = warning.init_A_DataUtil().load();
                  if (landing != null) {
                     entry.addProperty("x", landing.x);
                     entry.addProperty("y", landing.y);
                     entry.addProperty("z", landing.z);
                  }

                  predictions.add(entry);
               }

               state.add("predictions", predictions);
               JsonArray blastBlocks = new JsonArray();

               for (autismclient.modules.x.Inner_DataUtil block : danger.update_List()) {
                  JsonObject entry = new JsonObject();
                  entry.addProperty("x", block.init_BlockPos().getX());
                  entry.addProperty("y", block.init_BlockPos().getY());
                  entry.addProperty("z", block.init_BlockPos().getZ());
                  entry.addProperty("risk", block.init_D());
                  entry.addProperty("material", BuiltInRegistries.BLOCK.getKey(mc.level.getBlockState(block.init_BlockPos()).getBlock()).toString());
                  blastBlocks.add(entry);
               }

               state.add("blastBlocks", blastBlocks);
            }

            state.addProperty("attacks", init_AtomicLong.get());
            state.addProperty("enemySeen", enemy != null);
            state.addProperty("enemyDistance", enemy == null ? -1.0F : enemy.distanceTo(mc.player));
            state.addProperty("crosshair", mc.hitResult == null ? "NONE" : mc.hitResult.getType().name());
            if (init_Str.equals("attacker") && autismclient.modules.al.init_Module_Str("kill-aura") instanceof autismclient.modules.aa aura && aura.isEnabled()
               )
             {
               state.addProperty("auraClicks", aura.init_Str_Z(false));
               state.addProperty("legacyCombat", aI.init(mc.player));
               state.addProperty("autoBlock", aura.sync_Z_8());
               state.addProperty("autoBlockThreat", aura.init_I_20());
               state.addProperty("guardPredictionClears", aura.load_I_10());
               state.addProperty("blockHit", Boolean.parseBoolean(aura.value("block-hit")));
               state.addProperty("blockHitAfterHit", Boolean.parseBoolean(aura.value("block-hit-after-hit")));
               state.addProperty("blockHitRespectCps", Boolean.parseBoolean(aura.value("block-hit-cps")));
            }

            Files.writeString(init_Path.resolve(init_Str + "-state.json"), state.toString());
            Files.writeString(init_Path.resolve(init_Str + "-states.jsonl"), state + "\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            poll_BufferedWriter.flush();
         }
      } catch (Exception e) {
         AutismClientAddon.LOG.error("Combat server qualification failed", e);
      }
   }

   private static JsonObject init_JsonObject(Minecraft mc) {
      JsonObject data = new JsonObject();
      if (autismclient.modules.al.init_Module_Str("backtrack") instanceof autismclient.modules.TaskTimer backtrack) {
         autismclient.modules.TaskTimer.Inner_StreamUtil var7 = backtrack.init_TaskTimer_StreamUt();
         data.addProperty("enabled", backtrack.isEnabled());
         data.addProperty("targetId", var7.init_I());
         data.addProperty("pending", var7.load_I());
         data.addProperty("held", var7.init_J());
         data.addProperty("replayed", var7.load_J());
         data.addProperty("discarded", var7.read_J());
         data.addProperty("flushes", var7.write());
         data.addProperty("attackArms", var7.update());
         data.addProperty("peakAgeMs", var7.init_D());
         data.addProperty("generation", var7.sync());
         data.addProperty("selection", var7.init_Str());
         data.addProperty("pingMillis", var7.read_I());
         data.addProperty("visualVisible", var7.init_Z());
         data.addProperty("localHurtTime", mc.player.hurtTime);
         if (read_Str.contains("-duel-")) {
            data.addProperty("wTapRelease", autismclient.modules.bg.init_Z_36());
            data.addProperty("confirmedMelee", var7.load_Z());
            data.addProperty("rejectedLegacyAttacks", var7.flush());
            if (mc.level.getEntity(var7.init_I()) instanceof LivingEntity enemy && var7.init_Vec3() != null) {
               Vec3 eye = mc.player.getEyePosition();
               AABB body = enemy.getBoundingBox();
               data.addProperty("reach", mc.player.entityInteractionRange());
               data.addProperty("drawnRange", Math.sqrt(body.distanceToSqr(eye)));
               data.addProperty("receivedRange", Math.sqrt(body.move(var7.init_Vec3().subtract(enemy.position())).distanceToSqr(eye)));
            }
         }

         data.addProperty("smartTiming", backtrack.value("smart-timing"));
         data.addProperty("pauseOnHurt", backtrack.value("pause-on-hurt"));
         return data;
      } else {
         return data;
      }
   }

   private static void init(Minecraft mc, String stage) {
      if (!Boolean.getBoolean("autism.combat.server.techniques") && update_Z && init_Path != null && mc != null && mc.player != null) {
         try {
            if (push_BufferedWriter == null) {
               push_BufferedWriter = Files.newBufferedWriter(init_Path.resolve("scaffold-state.jsonl"));
            }

            String packetRow;
            while ((packetRow = write_ConcurrentLinkedQu.poll()) != null) {
               push_BufferedWriter.write(packetRow);
            }

            JsonObject state = init("state");
            state.addProperty("stage", stage);
            state.addProperty("driverTick", init_I);
            state.addProperty("alive", mc.player.isAlive());
            state.addProperty("x", mc.player.getX());
            state.addProperty("y", mc.player.getY());
            state.addProperty("z", mc.player.getZ());
            state.addProperty("vmx", mc.player.getDeltaMovement().x);
            state.addProperty("vmy", mc.player.getDeltaMovement().y);
            state.addProperty("vmz", mc.player.getDeltaMovement().z);
            state.addProperty("onGround", mc.player.onGround());
            state.addProperty("sprinting", mc.player.isSprinting());
            state.addProperty("eyeHeight", mc.player.getEyeHeight());
            state.addProperty("bodyHeight", mc.player.getBoundingBox().getYsize());
            state.addProperty("screen", mc.gui.screen() == null ? "" : mc.gui.screen().getClass().getName());
            state.addProperty("overlay", mc.gui.overlay() == null ? "" : mc.gui.overlay().getClass().getName());
            state.addProperty("focused", mc.isWindowActive());
            state.addProperty("physicalForward", aB.init(mc.options.keyUp).autism$isActuallyDown());
            state.addProperty("physicalBack", aB.init(mc.options.keyDown).autism$isActuallyDown());
            state.addProperty("physicalLeft", aB.init(mc.options.keyLeft).autism$isActuallyDown());
            state.addProperty("physicalRight", aB.init(mc.options.keyRight).autism$isActuallyDown());
            state.addProperty("physicalJump", aB.init(mc.options.keyJump).autism$isActuallyDown());
            state.addProperty("physicalSneak", aB.init(mc.options.keyShift).autism$isActuallyDown());
            if (mc.player.input != null) {
               state.add("input", init(mc.player.input.keyPresses));
            }

            j clutch = j.init_j();
            if (clutch != null && clutch.isEnabled()) {
               state.addProperty("clutchStatus", clutch.init_Str_16());

               for (String name : new String[]{
                  "controlling",
                  "returning",
                  "combatReleased",
                  "groundAnchor",
                  "groundTick",
                  "impact",
                  "plan",
                  "deadline",
                  "brakeX",
                  "clickGate",
                  "steeringMode",
                  "crouchPulseTick",
                  "plannedNextEye",
                  "safeNow",
                  "aimTrace",
                  "obstructionTrace"
               }) {
                  Field field = clutch.getClass().getDeclaredField(name);
                  field.setAccessible(true);
                  state.addProperty("clutch" + name, String.valueOf(field.get(clutch)));
               }
            }

            Module scaffold = autismclient.modules.al.init_Module_Str("scaffold");
            state.addProperty("enabled", scaffold != null && scaffold.isEnabled());
            state.addProperty("mode", scaffold == null ? "" : scaffold.value("mode"));
            state.addProperty("ownsInput", autismclient.modules.aI.read_Z_12());
            if (scaffold != null) {
               if (init_Method == null) {
                  init_Method = scaffold.getClass().getDeclaredMethod("canRun");
                  init_Method.setAccessible(true);
               }

               state.addProperty("canRun", (Boolean)init_Method.invoke(scaffold));

               for (String fieldName : new String[]{
                  "tellyPhase",
                  "tellyLookPhase",
                  "tellyAnchorYaw",
                  "tellyMotion",
                  "tellyOwnsInput",
                  "tellyWalkingBridge",
                  "tellyDiagonalBridge",
                  "tellyTarget",
                  "tellyLastBridge",
                  "tellyLastGroundedSupport",
                  "tellyLastGroundedTick",
                  "tellyBridgeY",
                  "tellyRaisedCell",
                  "tellyRiseBatchSupport",
                  "tellyRiseBatchPowerTicks",
                  "tellyRiseEntrySpent",
                  "tellyLineOrigin",
                  "tellyPlacementQueued",
                  "tellyQueuedBlock",
                  "tellyFailedClicks",
                  "tellyImperfectionPending",
                  "tellyCycleTopCount",
                  "tellyImperfectionQueued",
                  "tellyImperfectionCell",
                  "tellyPreparedLook",
                  "tellyHumanRotation",
                  "tellyInputLastYaw",
                  "tellyInputDecisionTick",
                  "tellyHoldRecoveryInput",
                  "tellyRecoveryTicks",
                  "tellyWalkingLaunchTick",
                  "tellyJumpThisTick",
                  "tellyRotationHeldForPlacement",
                  "tellyRotationStepTick",
                  "tellySmoothedRotation",
                  "grimSilentRotation",
                  "grimSentYaw",
                  "grimSentYawTick",
                  "grimAttemptState",
                  "grimAttemptSequence",
                  "grimAttemptGeneration",
                  "grimAttemptResult",
                  "grimHighestObservedAck",
                  "lastGrimPlacementTick",
                  "tellyPlacementCoast",
                  "tellyLandingBrakeThisTick",
                  "tellyFlatCarryPlannedThisTick",
                  "tellyFlatPowerTicks",
                  "tellyFlatCarryBudgetThisTick",
                  "tellyEaseLandingFooting",
                  "tellyTurnSettling",
                  "tellyStopCommit",
                  "tellyCycleRises",
                  "tellyAirTicks",
                  "tellySpaceHeld",
                  "tellyRiseQueued",
                  "grimLastPick",
                  "grimTraceWhy",
                  "grimLastPlanFail",
                  "grimPlanDetail",
                  "grimTraceReserveWhy",
                  "grimLaunchReservedSupport",
                  "grimLaunchReservedStep",
                  "grimFootingSurfaceY",
                  "grimAirborneBuiltRow",
                  "grimSprintNoForwardTick",
                  "grimLaunchReservationStage",
                  "currentMovementLine",
                  "grimTraceLaunchLedger",
                  "grimTraceArcStand",
                  "grimTraceBrake",
                  "grimTraceArcCarry"
               }) {
                  try {
                     Field field = load_Map.get(fieldName);
                     if (field == null) {
                        field = scaffold.getClass().getDeclaredField(fieldName);
                        field.setAccessible(true);
                        load_Map.put(fieldName, field);
                     }

                     state.addProperty(fieldName, String.valueOf(field.get(scaffold)));
                  } catch (ReflectiveOperationException missing) {
                     state.addProperty(fieldName, "unavailable: " + missing.getClass().getSimpleName());
                  }
               }
            }

            JsonObject gates = new JsonObject();
            gates.addProperty("noLevel", mc.level == null);
            gates.addProperty("noGameMode", mc.gameMode == null);
            gates.addProperty("noConnection", mc.getConnection() == null);
            gates.addProperty("screen", mc.gui.screen() != null);
            gates.addProperty("overlay", mc.gui.overlay() != null);
            gates.addProperty("spectator", mc.player.isSpectator());
            gates.addProperty("handsBusy", mc.player.isHandsBusy());
            gates.addProperty("hide", autismclient.modules.aD.init_Z());
            gates.addProperty("freecam", autismclient.modules.aC.init_Z());
            gates.addProperty("remoteView", cd.init_Z());
            gates.addProperty("fastExp", f.load());
            gates.addProperty("multiPilot", autismclient.util.g.PoolEntry.init_Z());
            gates.addProperty("packetTeleport", autismclient.util.g.PkgCls_a.init_Z());
            gates.addProperty("macro", MacroExecutor.write_Z());
            gates.addProperty("autoTotem", autismclient.modules.FieldCache.init_Z_6());
            gates.addProperty("autoArmor", autismclient.modules.TableIndex.init_Z_20());
            gates.addProperty("dangerRotation", autismclient.modules.z.read_Z_22());
            state.add("blockingGates", gates);
            JsonArray enabled = new JsonArray();

            for (Module module : autismclient.modules.al.init_Collection()) {
               if (module.isEnabled()) {
                  enabled.add(module.id());
               }
            }

            state.add("enabledModules", enabled);
            ch.Inner_CachePool wire = ch.init_ch_CachePool();
            state.addProperty("wireInitialized", wire.init_Z());
            if (wire.init_Z()) {
               state.addProperty("wireYaw", wire.read());
               state.addProperty("wirePitch", wire.write());
            }

            init(state);
            push_BufferedWriter.flush();
         } catch (Exception failure) {
            AutismClientAddon.LOG.error("Scaffold qualification diagnostics failed", failure);
         }
      }
   }

   private static JsonObject init(Input input) {
      JsonObject state = new JsonObject();
      state.addProperty("forward", input.forward());
      state.addProperty("backward", input.backward());
      state.addProperty("left", input.left());
      state.addProperty("right", input.right());
      state.addProperty("jump", input.jump());
      state.addProperty("shift", input.shift());
      state.addProperty("sprint", input.sprint());
      return state;
   }

   private static JsonObject init(String kind) {
      JsonObject state = new JsonObject();
      state.addProperty("kind", kind);
      state.addProperty("time", System.currentTimeMillis());
      state.addProperty("phase", read_Str);
      state.addProperty("tick", ck.init_ck().state_I());
      return state;
   }

   private static void init(JsonObject state) throws IOException {
      push_BufferedWriter.write(state.toString());
      push_BufferedWriter.newLine();
   }

   private static void read(Packet<?> packet) {
      JsonObject row;
      if (packet instanceof ServerboundAcceptTeleportationPacket teleport) {
         row = init("teleport-ack");
         row.addProperty("teleportId", teleport.id());
      } else if (packet instanceof ServerboundMovePlayerPacket movement) {
         row = init("movement-packet");
         row.addProperty("hasPosition", movement.hasPosition());
         row.addProperty("hasRotation", movement.hasRotation());
         if (movement.hasPosition()) {
            row.addProperty("x", movement.getX(0.0));
            row.addProperty("y", movement.getY(0.0));
            row.addProperty("z", movement.getZ(0.0));
         }

         if (movement.hasRotation()) {
            row.addProperty("yaw", movement.getYRot(0.0F));
            row.addProperty("pitch", movement.getXRot(0.0F));
         }

         row.addProperty("onGround", movement.isOnGround());
      } else if (packet instanceof ServerboundPlayerInputPacket input) {
         row = init("input-packet");
         row.add("input", init(input.input()));
      } else if (packet instanceof ServerboundPlayerActionPacket action) {
         row = init("dig-packet");
         row.addProperty("action", action.getAction().name());
         row.addProperty("block", action.getPos().immutable().toString());
         row.addProperty("face", action.getDirection().name());
      } else if (packet instanceof ServerboundPlayerCommandPacket command) {
         row = init("player-command");
         row.addProperty("action", command.getAction().name());
      } else {
         if (!(packet instanceof ServerboundUseItemOnPacket use)) {
            return;
         }

         row = init("use-packet");
         row.addProperty("sequence", use.sequence());
         row.addProperty("support", use.hitResult().getBlockPos().toString());
         row.addProperty("face", use.hitResult().getDirection().name());
         row.addProperty("hitX", use.hitResult().getLocation().x);
         row.addProperty("hitY", use.hitResult().getLocation().y);
         row.addProperty("hitZ", use.hitResult().getLocation().z);
         row.addProperty("rotationOwner", aD.init_Str());
         ch.Inner_CachePool wire = ch.init_ch_CachePool();
         row.addProperty("lookAgeTicks", ck.init_ck().state_I() - wire.init_I());
         row.addProperty("immediatePlacement", bL.init_Z());
         row.addProperty("wireYaw", wire.read());
         row.addProperty("wirePitch", wire.write());
         ce.Inner_DataUtil clicked = autismclient.modules.aI.init_ce_DataUtil_Packet(packet);
         ce.Inner_DataUtil captured = init_Map.remove(packet);
         if (clicked == null) {
            clicked = captured;
         }

         if (clicked != null) {
            row.addProperty("clickedYaw", clicked.init());
            row.addProperty("clickedPitch", clicked.load());
         }
      }

      write_ConcurrentLinkedQu.offer(row + "\n");
   }

   public static void load(Packet<?> packet) {
      if (packet instanceof ServerboundAttackPacket attack) {
         init(attack);
      } else if (init_Path != null && update_Z && packet instanceof ServerboundUseItemOnPacket && s.flush_Z()) {
         ce.Inner_DataUtil rotation = cn.init_ce_DataUtil(Minecraft.getInstance().player);
         if (rotation != null) {
            init_Map.putIfAbsent(packet, rotation);
         }
      }
   }

   private static void init(ServerboundAttackPacket attack) {
      if (init_Path != null && "attacker".equals(init_Str) && read_Str.contains("-duel-")) {
         Minecraft mc = Minecraft.getInstance();
         if (mc.isSameThread() && mc.player != null && mc.level != null && mc.level.getEntity(attack.entityId()) instanceof LivingEntity enemy) {
            JsonObject data = init_JsonObject(mc);
            data.addProperty("phase", read_Str);
            data.addProperty("submittedAt", System.currentTimeMillis());
            data.addProperty("attackTarget", enemy.getId());
            data.addProperty("legacyCombat", aI.init(mc.player));
            data.addProperty("hand", BuiltInRegistries.ITEM.getKey(mc.player.getMainHandItem().getItem()).toString());
            synchronized (read_Map) {
               if (read_Map.size() >= 64) {
                  read_Map.clear();
               }

               read_Map.putIfAbsent(attack, data);
            }
         }
      }
   }

   private static void load(Minecraft mc) {
      if (Boolean.getBoolean("autism.combat.placementView")) {
         mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
         if (!mc.gui.hud.isHidden()) {
            mc.gui.hud.toggle();
         }
      }

      for (cg.Inner_DataUtil source : cg.Inner_DataUtil.values()) {
         int i = source.ordinal();
         long latest = cg.init_J(source);
         if (latest > init_JArr[i]) {
            init_JArr[i] = latest;
            if (!read_Str.equals(init_StrArr[i])) {
               init_StrArr[i] = read_Str;
               init_IArr[i] = init_I + 2;
            }
         }

         if (init_IArr[i] != 0 && init_I >= init_IArr[i]) {
            init_IArr[i] = 0;
            Screenshot.grab(
               init_Path.toFile(),
               "placement-" + source.name().toLowerCase(Locale.ROOT) + "-" + read_Str + ".png",
               mc.gameRenderer.mainRenderTarget(),
               1,
               component -> {}
            );
         }
      }
   }

   private static void read(Minecraft mc) {
      if (init_JsonObject.has("combatLog")) {
         String command = init_JsonObject.get("combatLog").getAsString();
         autismclient.commands.DataUtil.read("combatlog " + command);
      }

      if (Boolean.getBoolean("autism.tools.server.check") && init_JsonObject.has("toolCommand")) {
         autismclient.commands.DataUtil.read(init_JsonObject.get("toolCommand").getAsString());
      }

      if (Boolean.getBoolean("autism.combat.server.online")) {
         if (init_JsonObject.has("menuSlot")) {
            av.init(mc, (int)init("menuSlot", -1.0), 0, ContainerInput.PICKUP);
         }

         if (init_JsonObject.has("useItem") && init_JsonObject.get("useItem").getAsBoolean()) {
            as.load_V();
         }

         if (init_JsonObject.has("hotbar")) {
            as.init_V_I((int)init("hotbar", 0.0));
         }

         if (init_JsonObject.has("shot")) {
            Screenshot.grab(init_Path.toFile(), init("shot", "probe.png"), mc.gameRenderer.mainRenderTarget(), 1, component -> {});
         }
      }

      write_Z = read_Z && init("movement", "still").equals("bridge");
      if (write_Z) {
         read_ConcurrentMap.put(read_Str, init("localExpectedBlock", "minecraft:stone"));
      }

      if (read_Z && mc.getSingleplayerServer() != null) {
         IntegratedServer server = mc.getSingleplayerServer();
         String commandPhase = read_Str;
         JsonObject commandControl = init_JsonObject;
         if (commandControl.has("commands")) {
            server.execute(() -> {
               for (JsonElement command : commandControl.getAsJsonArray("commands")) {
                  server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), command.getAsString());
               }

               if (commandControl.has("resetFixtureMotion") && commandControl.get("resetFixtureMotion").getAsBoolean()) {
                  for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                     player.setDeltaMovement(Vec3.ZERO);
                     player.connection.send(new ClientboundSetEntityMotionPacket(player.getId(), Vec3.ZERO));
                  }
               }

               JsonObject ready = new JsonObject();
               ready.addProperty("phase", commandPhase);

               try {
                  Files.writeString(init_Path.resolve("local-setup-ready.json"), ready.toString());
               } catch (IOException failurex) {
                  throw new UncheckedIOException(failurex);
               }
            });
         }

         if (commandControl.has("auditLocalPhase")) {
            server.execute(() -> {
               String audited = commandControl.get("auditLocalPhase").getAsString();
               ServerLevel level = server.overworld();
               Set<BlockPos> targets = init_ConcurrentMap.getOrDefault(audited, Set.of());
               int missing = 0;
               Block expected = BuiltInRegistries.BLOCK.getValue(Identifier.parse(read_ConcurrentMap.getOrDefault(audited, "minecraft:stone")));

               for (BlockPos target : targets) {
                  if (!level.getBlockState(target).is(expected)) {
                     missing++;
                  }
               }

               JsonObject result = new JsonObject();
               result.addProperty("phase", audited);
               result.addProperty("targets", targets.size());
               result.addProperty("missing", missing);
               result.addProperty("minimumY", load_ConcurrentMap.getOrDefault(audited, 0.0 / 0.0));

               try {
                  Files.writeString(init_Path.resolve("local-audit.json"), result.toString());
               } catch (IOException failurex) {
                  throw new UncheckedIOException(failurex);
               }
            });
         }
      }

      if (!Boolean.parseBoolean(init("keepModules", "false"))) {
         s.write_V();
         aD.load_V();

         for (String id : new String[]{
            "kill-aura",
            "aim-assist",
            "trigger-bot",
            "auto-clicker",
            "crystal-aura",
            "anchor-aura",
            "backtrack",
            "totem-hit",
            "surround",
            "bed-defender",
            "auto-trap",
            "auto-totem",
            "auto-armor",
            "danger",
            "scaffold",
            "clutch",
            "bed-nuker",
            "anti-knockback",
            "bed-esp",
            "auto-farm",
            "auto-tool",
            "kb-displacement",
            "w-tapping",
            "sprint",
            "safe-walk"
         }) {
            Module module = autismclient.modules.al.init_Module_Str(id);
            if (module != null) {
               module.setEnabled(false);
            }
         }

         if (init_JsonObject.has("resetModuleDefaults")) {
            for (JsonElement id : init_JsonObject.getAsJsonArray("resetModuleDefaults")) {
               Module configured = autismclient.modules.al.init_Module_Str(id.getAsString());
               if (configured != null) {
                  configured.resetSettings();
               }
            }
         }

         if (init_JsonObject.has("moduleSettings")) {
            for (Entry<String, JsonElement> entry : init_JsonObject.getAsJsonObject("moduleSettings").entrySet()) {
               Module configured = autismclient.modules.al.init_Module_Str(entry.getKey());
               if (configured != null) {
                  for (Entry<String, JsonElement> setting : entry.getValue().getAsJsonObject().entrySet()) {
                     configured.setValueTransient(setting.getKey(), setting.getValue().getAsString());
                  }
               }
            }
         }

         if (init_JsonObject.has("extraModules")) {
            for (JsonElement id : init_JsonObject.getAsJsonArray("extraModules")) {
               Module extra = autismclient.modules.al.init_Module_Str(id.getAsString());
               if (extra != null) {
                  extra.setEnabled(true);
               }
            }
         }

         if (init_Str.equals("attacker")) {
            if (init_JsonObject.has("selectedSlot")) {
               mc.player.getInventory().setSelectedSlot((int)init("selectedSlot", 0.0));
            }

            String mode = init("mode", "idle");
            Module module = autismclient.modules.al.init_Module_Str(mode);
            if (module != null) {
               if (mode.equals("clutch")) {
                  module.setValueTransient("own-knockback", init("ownKnockback", "false"));
                  module.setValueTransient("own-void-only", init("ownVoidOnly", "true"));
                  module.setValueTransient("own-fall-height", init("ownFallHeight", "10"));
                  module.setValueTransient("steer", init("steer", "true"));
                  module.setValueTransient("own-steer", init("ownSteer", init("steer", "true")));
                  module.setValueTransient("safe-fall", init("fallDistance", "3"));
                  module.setValueTransient("guard-time", init("guardTime", "0"));
                  if (Boolean.parseBoolean(init("scaffold", "false"))) {
                     Module scaffold = autismclient.modules.al.init_Module_Str("scaffold");
                     scaffold.setValueTransient("mode", init("bridgeMode", "Telly"));
                     scaffold.setEnabled(true);
                  }

                  if (Boolean.parseBoolean(init("aura", "false"))) {
                     autismclient.modules.al.init_Module_Str("kill-aura").setEnabled(true);
                  }

                  if (Boolean.parseBoolean(init("danger", "false"))) {
                     autismclient.modules.al.init_Module_Str("danger").setEnabled(true);
                  }
               }

               if (mode.equals("danger")) {
                  module.setValueTransient("auto-return", init("autoReturn", "true"));
                  module.setValueTransient("rotation", init("rotation", "Silent"));

                  for (autismclient.modules.A.Inner_StreamUtil kind : autismclient.modules.A.Inner_StreamUtil.values()) {
                     module.setValueTransient(kind.init_Str() + "-enabled", init(kind.init_Str() + "Enabled", "true"));
                     module.setValueTransient(kind.init_Str() + "-aim-preview", init(kind.init_Str() + "AimPreview", init("aimPreview", "false")));
                     double radius = Double.parseDouble(init("radius", "15")) * (kind == autismclient.modules.A.Inner_StreamUtil.write ? 2 : 1);
                     module.setValueTransient(kind.init_Str() + "-radius", init(kind.init_Str() + "Radius", Double.toString(radius)));
                     module.setValueTransient(
                        kind.init_Str() + "-trajectory", init(kind.init_Str() + "Trajectory", init(kind.init_Str() + "Trajectories", "true"))
                     );
                     module.setValueTransient(kind.init_Str() + "-hud", init(kind.init_Str() + "Hud", "true"));
                  }

                  module.setValueTransient("fireball-blast-blocks", init("blastBlocks", "false"));
                  module.setValueTransient("ghost-opacity", init("ghostOpacity", "0.35"));
                  Module scaffold = autismclient.modules.al.init_Module_Str("scaffold");
                  if (scaffold != null && init_JsonObject.has("bridgeMode")) {
                     scaffold.setValueTransient("mode", init("bridgeMode", "Legit"));
                     scaffold.setValueTransient("allow-diagonal", init("allowDiagonal", "false"));
                     scaffold.setEnabled(true);
                     if (Boolean.parseBoolean(init("scaffoldTrace", "false"))) {
                        for (String fieldName : new String[]{"tellyLiveTraceTicks", "grimLiveTraceTicks"}) {
                           try {
                              Field field = scaffold.getClass().getDeclaredField(fieldName);
                              field.setAccessible(true);
                              field.setInt(scaffold, 0);
                           } catch (ReflectiveOperationException failure) {
                              throw new IllegalStateException("Missing Scaffold qualification trace", failure);
                           }
                        }
                     }
                  }

                  if (init_JsonObject.has("aura") && init_JsonObject.get("aura").getAsBoolean()) {
                     Module aura = autismclient.modules.al.init_Module_Str("kill-aura");
                     aura.setValueTransient("entities", "minecraft:player");
                     aura.setEnabled(true);
                  }
               }

               module.setValueTransient("entities", init("entities", "minecraft:player"));
               if (mode.equals("kill-aura")) {
                  module.setValueTransient("aim", "Human");
                  module.setValueTransient("fov", "360");
                  if (init_JsonObject.has("missChance")) {
                     module.setValueTransient("miss-chance", init("missChance", "0"));
                  }

                  module.setValueTransient(
                     "block-hit",
                     init("blockHit", Boolean.toString(Boolean.parseBoolean(init("autoBlock", "false")) || Boolean.parseBoolean(init("perfectHit", "false"))))
                  );
                  module.setValueTransient("block-hit-after-hit", init("blockHitAfterHit", "true"));
                  module.setValueTransient("block-hit-cps", init("blockHitRespectCps", "false"));
                  if (init_JsonObject.has("interactions")) {
                     module.setValueTransient("interactions", init("interactions", "Legit"));
                  }

                  module.setValueTransient("attack-mode", init("attackMode", "Real Input"));
                  if (init_JsonObject.has("technique")) {
                     module.setValueTransient("technique", init("technique", "Butterfly"));
                  }

                  if (init_JsonObject.has("rageCps")) {
                     module.setValueTransient("rage-cps", init("rageCps", "30"));
                  }
               } else if (mode.equals("aim-assist")) {
                  module.setValueTransient("fov", "180");
                  module.setValueTransient("horizontal-speed", init("strength", "80"));
                  module.setValueTransient("vertical-speed", init("strength", "80"));
                  module.setValueTransient("x-axis", "true");
                  module.setValueTransient("y-axis", "true");
               }

               mc.player.setYRot(mc.player.getYRot() + ce.init((float)init("yaw", 0.0), mc.player.getYRot()));
               mc.player.setXRot((float)init("pitch", 0.0));
               if (init_JsonObject.has("selectedSlot")) {
                  mc.player.getInventory().setSelectedSlot((int)init("selectedSlot", 0.0));
               }

               module.setEnabled(!mode.equals("danger") || Boolean.parseBoolean(init("dangerEnabled", "true")));
            }
         }
      }
   }

   private static void write(Minecraft mc) {
      if (!read_Str.equals(run)) {
         run = read_Str;
         write_D = 0.0 / 0.0;
         read_D = 0.0 / 0.0;
         flush_I = 0;
      }

      double z = mc.player.getZ();
      double v = mc.player.getDeltaMovement().z;
      boolean shift = false;
      if (flush_I > 0) {
         shift = true;
         if (--flush_I == 0) {
            flush_I = -1;
         }
      } else if (flush_I == 0 && mc.player.onGround() && Double.isFinite(read_D)) {
         double accel = z - read_D - write_D;
         double overlap = init("edgeZ", 3.0) - (z + v + accel - 0.3);
         double now = init("edgeZ", 3.0) - (z - 0.3);
         String at = init("sneakAt", "deadzone");
         boolean start = at.equals("deadzone")
            ? overlap > 0.0 && overlap < 0.05
            : (at.equals("deferred") ? now > 0.0 && now < 0.05 && overlap <= 0.0 : overlap <= 0.0);
         if (start) {
            shift = true;
            flush_I = (int)init("sneakTicks", 2.0) - 1;
            if (flush_I == 0) {
               flush_I = -1;
            }
         }
      }

      read_D = z;
      write_D = v;
      mc.player.setYRot(mc.player.getYRot() + ce.init(180.0F, mc.player.getYRot()));
      mc.player.setXRot(0.0F);
      init(mc.options.keyUp, false);
      init(mc.options.keyDown, true);
      init(mc.options.keyLeft, false);
      init(mc.options.keyRight, false);
      init(mc.options.keySprint, false);
      init(mc.options.keyJump, false);
      init(mc.options.keyShift, shift);
   }

   private static void init(Minecraft mc, Player enemy) {
      if (Boolean.parseBoolean(init("manualUse", "false"))) {
         if (init_JsonObject.has("cameraYaw")) {
            mc.player.setYRot(mc.player.getYRot() + Mth.wrapDegrees((float)init("cameraYaw", 0.0) - mc.player.getYRot()));
         }

         if (init_JsonObject.has("cameraPitch")) {
            mc.player.setXRot((float)init("cameraPitch", 0.0));
         }

         int pulse = (int)init("pulseUseEvery", 0.0);
         boolean pulseDown = pulse > 1 && init_I % pulse < pulse / 2;
         boolean wasDown = Boolean.TRUE.equals(init_IdentityHashMap.get(mc.options.keyUse));
         init(mc.options.keyUse, Boolean.parseBoolean(init("holdUse", "false")) || pulseDown);
         if (pulseDown && !wasDown) {
            ((AutismKeyMappingAccessor)mc.options.keyUse).autism$setClickCount(1);
         }

         int every = (int)init("tapUseEvery", 0.0);
         if (every > 0 && init_I % every == 0) {
            ((AutismKeyMappingAccessor)mc.options.keyUse).autism$setClickCount(1);
         }
      } else if (Boolean.TRUE.equals(init_IdentityHashMap.get(mc.options.keyUse))) {
         init(mc.options.keyUse, false);
      }

      if (enemy != null && init_JsonObject.has("enemyHeadYaw")) {
         enemy.setYHeadRot((float)init("enemyHeadYaw", 0.0));
      }

      if (!init("mode", "idle").equals("tools")) {
         String movement = init("movement", "still");
         if (movement.equals("edge-sneak")) {
            write(mc);
         } else {
            double dx = 0.0;
            double dz = 0.0;
            boolean duel = movement.equals("melee-duel");
            if (duel && enemy != null) {
               double x = enemy.getX() - mc.player.getX();
               double z = enemy.getZ() - mc.player.getZ();
               double distance = Math.max(0.1, Math.hypot(x, z));
               double direction = (init_I / 22 + (init_Str.equals("target") ? 1 : 0) & 1) == 0 ? 1.0 : -1.0;
               double chase = Math.clamp((distance - init("radius", 2.35)) * 1.5 + 0.7, -0.35, 1.5);
               double strafe = distance > 3.2 ? 0.32 : 0.82;
               dx = (x * chase - direction * z * strafe) / distance;
               dz = (z * chase + direction * x * strafe) / distance;
            } else if (movement.equals("orbit") && enemy != null) {
               double x = mc.player.getX() - enemy.getX();
               double z = mc.player.getZ() - enemy.getZ();
               double distance = Math.max(0.1, Math.hypot(x, z));
               double direction = init("direction", 1.0);
               double radial = Math.max(-1.5, Math.min(1.5, (init("radius", 1.8) - distance) * 2.0));
               dx = (-direction * z + radial * x) / distance;
               dz = (direction * x + radial * z) / distance;
            } else if (movement.equals("loop")) {
               double x = mc.player.getX();
               double z = mc.player.getZ();
               double distance = Math.max(0.1, Math.hypot(x, z));
               double direction = init("direction", 1.0);
               double radial = Math.max(-2.0, Math.min(2.0, (init("radius", 2.0) - distance) * 2.0));
               dx = (-direction * z + radial * x) / distance;
               dz = (direction * x + radial * z) / distance;
            } else if (movement.equals("bed-strafe")) {
               dx = (0.5 + Math.sin(init_I * 0.055) * 1.25 - mc.player.getX()) * 3.0;
               dz = (-1.8 - mc.player.getZ()) * 3.0;
            } else if (movement.equals("strafe")) {
               double goal = Math.sin(init_I * 0.11) * 1.5;
               dx = (goal - mc.player.getX()) * 3.0;
               dz = (2.1 - mc.player.getZ()) * 3.0;
            } else if (movement.equals("bridge")) {
               double courseYaw = init_JsonObject.has("courseYaw") ? init("courseYaw", 0.0) : mc.player.getYRot() + init("courseOffset", 0.0);
               boolean stopped = init_JsonObject.has("stopZ") && mc.player.getZ() >= init("stopZ", 0.0)
                  || init_JsonObject.has("stopX") && mc.player.getX() >= init("stopX", 0.0);
               dx = stopped ? 0.0 : -Math.sin(Math.toRadians(courseYaw));
               dz = stopped ? 0.0 : Math.cos(Math.toRadians(courseYaw));
            } else if (movement.equals("zigzag")) {
               double goal = init("zigzagCenter", mc.player.getX())
                  + Math.sin(init_I * 2 * 3.141592653589793 / Math.max(4.0, init("zigzagPeriod", 30.0))) * init("zigzagAmp", 0.3);
               dx = (goal - mc.player.getX()) * 3.0;
               dz = init_JsonObject.has("zigzagHoldZ") ? (init("zigzagHoldZ", 0.0) - mc.player.getZ()) * 3.0 : init("zigzagAlong", 0.0) * 4.0;
            }

            boolean moving = Math.hypot(dx, dz) > 0.12;
            boolean clutchEnemy = init_Str.equals("target") && init("mode", "idle").equals("clutch-enemy");
            boolean aimsAtEnemy = clutchEnemy
               || init("mode", "idle").equals("manual-attack")
               || init("mode", "idle").equals("trigger-bot")
               || init("mode", "idle").equals("auto-clicker");
            if (!Boolean.parseBoolean(init("fixedAim", "false"))
               && ((init_Str.equals("attacker") || clutchEnemy) && aimsAtEnemy || Boolean.parseBoolean(init("aimAtEnemy", "false")))
               && enemy != null) {
               float cameraGoal = mc.player.getYRot()
                  + Mth.wrapDegrees(
                     (float)(Math.toDegrees(Math.atan2(enemy.getZ() - mc.player.getZ(), enemy.getX() - mc.player.getX())) - 90.0) - mc.player.getYRot()
                  );
               ch.Inner_CachePool cameraWire = ch.init_ch_CachePool();
               if (cameraWire.init_Z()) {
                  cameraGoal = ch.init_F() + ce.init(cameraGoal, cameraWire.read());
               }

               if (duel && cameraWire.init_Z()) {
                  cameraGoal = ch.init_F() + Math.clamp(ce.init(cameraGoal, cameraWire.read()), -46.0F, 46.0F);
               }

               mc.player.setYRot(cameraGoal);
               if (duel) {
                  Vec3 delta = enemy.getBoundingBox().getCenter().subtract(mc.player.getEyePosition());
                  float pitch = (float)(-Math.toDegrees(Math.atan2(delta.y, Math.hypot(delta.x, delta.z))));
                  mc.player.setXRot(mc.player.getXRot() + Math.clamp(pitch - mc.player.getXRot(), -24.0F, 24.0F));
               } else if (clutchEnemy) {
                  Vec3 delta = enemy.getBoundingBox().getCenter().subtract(mc.player.getEyePosition());
                  float wantedYaw = mc.player.getYRot();
                  ch.Inner_CachePool wire = ch.init_ch_CachePool();
                  if (wire.init_Z()) {
                     wantedYaw = ch.init_F() + ce.init(wantedYaw, wire.read());
                  }

                  mc.player.setYRot(wantedYaw);
                  mc.player.setXRot((float)(-Math.toDegrees(Math.atan2(delta.y, Math.hypot(delta.x, delta.z)))));
               }
            } else if (moving && init_Str.equals("target")) {
               float goal = mc.player.getYRot() + Mth.wrapDegrees((float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0) - mc.player.getYRot());
               ch.Inner_CachePool wire = ch.init_ch_CachePool();
               if (wire.init_Z()) {
                  goal = ch.init_F() + ce.init(goal, wire.read());
               }

               mc.player.setYRot(goal);
            }

            double yaw = Math.toRadians(movement.equals("bed-strafe") ? cn.init_F(mc.player) : mc.player.getYRot());
            double length = Math.max(0.01, Math.hypot(dx, dz));
            double forward = (-Math.sin(yaw) * dx + Math.cos(yaw) * dz) / length;
            double left = (Math.cos(yaw) * dx + Math.sin(yaw) * dz) / length;
            boolean up = moving && forward > 0.38;
            boolean down = moving && forward < -0.38;
            boolean leftKey = moving && left > 0.38;
            boolean rightKey = moving && left < -0.38;
            if (moving && Boolean.parseBoolean(init("chatter", "false"))) {
               int roll = (int)Math.floorMod(init_I * 2654435761L >>> 9, 10L);
               if (!mc.player.onGround()) {
                  if (roll < 3) {
                     up = false;
                  } else if (roll < 5) {
                     up = false;
                     down = true;
                  } else if (roll == 5) {
                     down = true;
                  } else if (roll == 6) {
                     leftKey = true;
                  } else if (roll == 7) {
                     rightKey = true;
                  }
               } else if (roll == 0) {
                  up = false;
               }
            }

            init(mc.options.keyUp, up);
            init(mc.options.keyDown, down);
            init(mc.options.keyLeft, leftKey);
            init(mc.options.keyRight, rightKey);
            init(mc.options.keySprint, moving && Boolean.parseBoolean(init("sprint", "true")));
            init(mc.options.keyShift, false);
            init(
               mc.options.keyJump,
               moving && init_JsonObject.has("jump") && init_JsonObject.get("jump").getAsBoolean() || Boolean.parseBoolean(init("jumpInPlace", "false"))
            );
            if (clutchEnemy && (init_I % Math.max(1, (int)init("clickEvery", 20.0)) == 0 || !read_Str.equals(poll_Str))) {
               poll_Str = read_Str;
               ((AutismKeyMappingAccessor)mc.options.keyAttack).autism$setClickCount(1);
            }

            if (init_JsonObject.has("attackEvery") && init_I % Math.max(1, (int)init("attackEvery", 10.0)) == 0) {
               ((AutismKeyMappingAccessor)mc.options.keyAttack).autism$setClickCount(1);
            }

            if (init_Str.equals("attacker") && init_JsonObject.has("hotbarCycle")) {
               String[] slots = init("hotbarCycle", "0").split(",");
               int slot = Integer.parseInt(slots[Math.floorMod(init_I, slots.length)].trim());
               ((AutismKeyMappingAccessor)mc.options.keyHotbarSlots[Math.clamp(slot, 0, 8)]).autism$setClickCount(1);
            }

            if (init_Str.equals("attacker") && init_JsonObject.has("hotbarTap") && !read_Str.equals(pop)) {
               pop = read_Str;
               read_I = init_I;
            }

            if (init_Str.equals("attacker")
               && init_JsonObject.has("hotbarTap")
               && !read_Str.equals(push_Str)
               && init_I - read_I >= (int)init("hotbarTapAfter", 0.0)) {
               push_Str = read_Str;
               ((AutismKeyMappingAccessor)mc.options.keyHotbarSlots[Math.clamp((int)init("hotbarTap", 0.0), 0, 8)]).autism$setClickCount(1);
            }

            if (init_Str.equals("attacker")
               && init("mode", "idle").equals("clutch")
               && Boolean.parseBoolean(init("ownLaunch", "false"))
               && !read_Str.equals(open_Str)
               && mc.player.getMainHandItem().is(Items.FIRE_CHARGE)) {
               open_Str = read_Str;
               ((AutismKeyMappingAccessor)mc.options.keyUse).autism$setClickCount(1);
            }

            if (init_Str.equals("attacker") && init_JsonObject.has("holdAttack")) {
               init(mc.options.keyAttack, Boolean.parseBoolean(init("holdAttack", "false")));
            }

            if (init_Str.equals("attacker") && Boolean.parseBoolean(init("spamClicks", "false"))) {
               boolean clickHeld = init_I % 4 < 2;
               init(mc.options.keyAttack, clickHeld);
               if (init_I % 4 == 0) {
                  ((AutismKeyMappingAccessor)mc.options.keyAttack).autism$setClickCount(1);
               }
            }

            if (init_Str.equals("attacker")
               && (init("mode", "idle").equals("aim-assist") || init("mode", "idle").equals("manual-attack"))
               && init_I % 14 == 0
               && !Boolean.parseBoolean(init("suppressAttack", "false"))) {
               ((AutismKeyMappingAccessor)mc.options.keyAttack).autism$setClickCount(1);
            }
         }
      }
   }

   private static void load(Minecraft mc, Player enemy) throws IOException, IllegalAccessException, NoSuchFieldException {
      Vec3 position = mc.player.position();
      double speed = init_Vec3 == null ? 0.0 : Math.hypot(position.x - init_Vec3.x, position.z - init_Vec3.z);
      init_Vec3 = position;
      ch.Inner_CachePool wire = ch.init_ch_CachePool();
      double yaw = wire.init_Z() ? wire.read() : mc.player.getYRot();
      double pitch = wire.init_Z() ? wire.write() : mc.player.getXRot();
      boolean onBox = false;
      boolean selected = false;
      String handState = "";
      double observer = 0.0;
      double ready = 0.0;
      boolean window = false;
      double yawError = 0.0;
      double pitchError = 0.0;
      double distance = 0.0;
      double targetSpeed = 0.0;
      if (enemy != null) {
         Vec3 eyes = mc.player.getEyePosition();
         Vec3 delta = enemy.getBoundingBox().getCenter().subtract(eyes);
         double horizontal = Math.hypot(delta.x, delta.z);
         yawError = Math.abs(Mth.wrapDegrees(yaw - (Math.toDegrees(Math.atan2(delta.z, delta.x)) - 90.0)));
         pitchError = Math.abs(pitch + Math.toDegrees(Math.atan2(delta.y, horizontal)));
         Vec3 end = eyes.add(Vec3.directionFromRotation((float)pitch, (float)yaw).scale(4.5));
         onBox = enemy.getBoundingBox().clip(eyes, end).isPresent() || enemy.getBoundingBox().contains(eyes);
         distance = enemy.distanceTo(mc.player);
         targetSpeed = Math.hypot(enemy.getX() - enemy.xo, enemy.getZ() - enemy.zo);
         Module module = autismclient.modules.al.init_Module_Str("kill-aura");
         selected = module instanceof autismclient.modules.aa aura && aura.isEnabled() && aura.init_LivingEntity_3() == enemy;
         if (selected && !Boolean.getBoolean("autism.combat.server.techniques")) {
            Field handField = autismclient.modules.aa.class.getDeclaredField("init");
            handField.setAccessible(true);
            q hand = (q)handField.get(module);
            Field frameField = autismclient.modules.aa.class.getDeclaredField("init");
            frameField.setAccessible(true);
            q.Inner_DataUtil frame = (q.Inner_DataUtil)frameField.get(module);
            handState = hand == null ? "" : hand.init_Str();
            Field observerField = q.Inner_DataUtil.class.getDeclaredField("get");
            observerField.setAccessible(true);
            observer = observerField.getDouble(frame);
            ready = frame.sync_D;
            window = frame.init_Z;
         }

         for (String id : new String[]{"aim-assist", "trigger-bot"}) {
            if (!Boolean.getBoolean("autism.combat.server.techniques")) {
               Module visible = autismclient.modules.al.init_Module_Str(id);
               if (visible != null && visible.isEnabled()) {
                  Field targetField = visible.getClass().getDeclaredField(id.equals("aim-assist") ? "trackedTargetId" : "targetId");
                  targetField.setAccessible(true);
                  selected = targetField.getInt(visible) == enemy.getId();
               }
            }
         }
      }

      float[] rotationTick = cn.init_FArr();
      Input keys = mc.player.input == null ? Input.EMPTY : mc.player.input.keyPresses;
      poll_BufferedWriter.write(
         String.format(
            Locale.ROOT,
            "%d,%s,%d,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f,%s,%s,%.4f,%.4f,%.4f,%.4f,%s,%d,%d,%d,%s,%.4f,%.4f,%s,%.5f,%.5f,%.5f,%.5f,%.5f,%.4f,%.4f,%s,%d,%d,%s,%s,%s,%s,%s,%s,%s,%.3f,%.3f,%.3f,%.0f,%.3f,%s,%s,%.3f,%s,%s,%d,%s,%s,%s,%.5f,%.5f,%.5f%n",
            System.currentTimeMillis(),
            read_Str,
            init_I,
            position.x,
            position.y,
            position.z,
            speed,
            targetSpeed,
            distance,
            selected,
            onBox,
            yawError,
            pitchError,
            yaw,
            pitch,
            mc.options.keyAttack.isDown(),
            enemy == null ? 0 : enemy.hurtTime,
            init_AtomicLong.get(),
            load_AtomicLong.get(),
            handState,
            observer,
            ready,
            window,
            mc.player.getDeltaMovement().x,
            mc.player.getDeltaMovement().y,
            mc.player.getDeltaMovement().z,
            mc.player.getYRot(),
            mc.player.getXRot(),
            mc.player.input == null ? 0.0F : mc.player.input.getMoveVector().y,
            mc.player.input == null ? 0.0F : mc.player.input.getMoveVector().x,
            mc.player.onGround(),
            mc.player.isSprinting() ? 1 : 0,
            mc.player.hurtTime,
            keys.forward(),
            keys.backward(),
            keys.left(),
            keys.right(),
            keys.jump(),
            keys.sprint(),
            keys.shift(),
            rotationTick.length < 4 ? 0.0F / 0.0F : rotationTick[0],
            rotationTick.length < 4 ? 0.0F / 0.0F : rotationTick[1],
            rotationTick.length < 4 ? 0.0F / 0.0F : rotationTick[2],
            rotationTick.length < 4 ? 0.0F / 0.0F : rotationTick[3],
            rotationTick.length < 5 ? 0.0F / 0.0F : rotationTick[4],
            ci.write() ? "true" : "false",
            ci.update() ? "true" : "false",
            ci.init_F(),
            ci.flush() ? "true" : "false",
            ci.sync() ? "true" : "false",
            ci.init_I(),
            init_Str(),
            update(),
            mc.player.isUsingItem(),
            enemy == null ? 0.0 / 0.0 : enemy.getX(),
            enemy == null ? 0.0 / 0.0 : enemy.getY(),
            enemy == null ? 0.0 / 0.0 : enemy.getZ()
         )
      );
   }

   private static JsonObject init(Module aura) {
      JsonObject result = new JsonObject();
      result.addProperty("enabled", aura.isEnabled());
      result.addProperty("missTime", ((AutismMinecraftAccessor)Minecraft.getInstance()).autism$getMissTime());

      try {
         Field target = aura.getClass().getDeclaredField("target");
         target.setAccessible(true);
         result.addProperty("target", String.valueOf(target.get(aura)));
         Method canRun = aura.getClass().getDeclaredMethod("canRun");
         canRun.setAccessible(true);
         result.addProperty("canRun", (Boolean)canRun.invoke(aura));
         if (target.get(aura) instanceof LivingEntity enemy) {
            String methodName = aura.id().equals("crystal-aura") ? "rankSupports" : "rankCells";
            Method ranking = aura.getClass().getDeclaredMethod(methodName, LivingEntity.class, double.class);
            ranking.setAccessible(true);
            Object ranked = ranking.invoke(aura, enemy, Minecraft.getInstance().player.blockInteractionRange());
            result.addProperty("rankedCount", ((List)ranked).size());
            if (aura.id().equals("anchor-aura")) {
               Method optionsFor = aura.getClass().getDeclaredMethod("optionsFor", BlockPos.class);
               optionsFor.setAccessible(true);
               JsonArray samples = new JsonArray();

               for (int dx = -1; dx <= 1; dx++) {
                  for (int dz = -1; dz <= 1; dz++) {
                     if (dx != 0 || dz != 0) {
                        BlockPos cell = enemy.blockPosition().offset(dx, 0, dz);
                        Vec3 source = Vec3.atCenterOf(cell);
                        S.Inner_CachePool options = (S.Inner_CachePool)optionsFor.invoke(aura, cell);
                        JsonObject sample = new JsonObject();
                        sample.addProperty("cell", cell.toString());
                        sample.addProperty("target", S.init_D(enemy, source, 5.0F, options));
                        sample.addProperty("self", S.init_D_Vec3(source, 5.0F, options));
                        samples.add(sample);
                     }
                  }
               }

               result.add("anchorSamples", samples);
            }

            if (aura.id().equals("crystal-aura")) {
               result.addProperty("heldAttackDamage", UiHelper.init(Minecraft.getInstance().player, Minecraft.getInstance().player.getMainHandItem()));
               result.addProperty("weakness", Minecraft.getInstance().player.hasEffect(MobEffects.WEAKNESS));
               List<EndCrystal> crystals = Minecraft.getInstance()
                  .level
                  .getEntitiesOfClass(EndCrystal.class, Minecraft.getInstance().player.getBoundingBox().inflate(8.0));
               result.addProperty("crystalCount", crystals.size());
               Method destroy = aura.getClass().getDeclaredMethod("planDestroy", LivingEntity.class);
               destroy.setAccessible(true);
               result.addProperty("destroyPlan", String.valueOf(destroy.invoke(aura, enemy)));
               if (!crystals.isEmpty()) {
                  EndCrystal crystal = crystals.get(0);
                  ch.Inner_CachePool wire = ch.init_ch_CachePool();
                  Vec3 eye = Minecraft.getInstance().player.getEyePosition();
                  Vec3 look = Vec3.directionFromRotation(wire.write(), wire.read());
                  Optional<Vec3> clip = crystal.getBoundingBox().clip(eye, eye.add(look.scale(3.0)));
                  result.addProperty("crystalBox", crystal.getBoundingBox().toString());
                  result.addProperty("eye", eye.toString());
                  result.addProperty("wireLook", wire.read() + "/" + wire.write());
                  result.addProperty("destroyClip", clip.map(Object::toString).orElse("empty"));
                  if (clip.isPresent()) {
                     BlockHitResult terrain = Minecraft.getInstance()
                        .level
                        .clip(
                           new ClipContext(eye, clip.get(), net.minecraft.world.level.ClipContext.Block.COLLIDER, Fluid.NONE, Minecraft.getInstance().player)
                        );
                     result.addProperty("destroyTerrain", terrain.getType() + ":" + terrain.getLocation());
                  }
               }
            }

            if (aura.id().equals("crystal-aura")) {
               BlockPos support = enemy.blockPosition().below();
               Vec3 source = new Vec3(support.getX() + 0.5, support.getY() + 1.0, support.getZ() + 0.5);
               Field optionsField = aura.getClass().getDeclaredField("options");
               optionsField.setAccessible(true);
               S.Inner_CachePool options = (S.Inner_CachePool)optionsField.get(aura);
               result.addProperty("sampleTarget", S.init_D(enemy, source, 6.0F, options));
               result.addProperty("sampleSelf", S.init_D_Vec3(source, 6.0F, options));
               Method free = aura.getClass().getDeclaredMethod("cellIsFree", BlockPos.class, AABB.class);
               free.setAccessible(true);
               result.addProperty(
                  "sampleCellFree",
                  (Boolean)free.invoke(aura, support, Minecraft.getInstance().player.getBoundingBox().move(Minecraft.getInstance().player.getDeltaMovement()))
               );
               Method visible = aura.getClass().getDeclaredMethod("supportVisible", Vec3.class, BlockPos.class, Vec3.class);
               visible.setAccessible(true);
               Method passes = aura.getClass().getDeclaredMethod("damagePasses", LivingEntity.class, S.Inner_BufferHelper.class);
               passes.setAccessible(true);
               int passing = 0;
               int freeCells = 0;
               int visibleCells = 0;
               int damageCells = 0;
               LocalPlayer player = Minecraft.getInstance().player;

               for (int dx = -3; dx <= 3; dx++) {
                  for (int dz = -3; dz <= 3; dz++) {
                     BlockPos candidate = player.blockPosition().offset(dx, -1, dz);
                     if (Minecraft.getInstance().level.getBlockState(candidate).getBlock() == Blocks.BEDROCK) {
                        Vec3 point = new Vec3(candidate.getX() + 0.5, candidate.getY() + 1.0, candidate.getZ() + 0.5);
                        if (!(S.load_D(enemy, point, 6.0F, options) < 5.0)
                           && (Boolean)free.invoke(aura, candidate, player.getBoundingBox().move(player.getDeltaMovement()))) {
                           freeCells++;
                           if ((Boolean)visible.invoke(aura, player.getEyePosition(), candidate, point)) {
                              visibleCells++;
                              S.Inner_BufferHelper rank = S.load_S_BufferHelper(enemy, point, 6.0F, options);
                              if ((Boolean)passes.invoke(aura, enemy, rank)) {
                                 damageCells++;
                                 passing++;
                              }
                           }
                        }
                     }
                  }
               }

               result.addProperty("manualPassing", passing);
               result.addProperty("manualFree", freeCells);
               result.addProperty("manualVisible", visibleCells);
               result.addProperty("manualDamage", damageCells);
            }

            if (aura.id().equals("anchor-aura") && !((List)ranked).isEmpty()) {
               BlockPos cell = (BlockPos)((List)ranked).get(0);
               Method place = aura.getClass()
                  .getDeclaredMethod(
                     "placePlan", BlockPos.class, BlockPos.class, boolean.class, double.class, U.Inner_StreamUtil.class, U.Inner_JsonUtil[].class
                  );
               place.setAccessible(true);
               U.Inner_JsonUtil[] refusal = new U.Inner_JsonUtil[1];
               Object plan = place.invoke(aura, cell, null, false, Minecraft.getInstance().player.blockInteractionRange(), new U.Inner_StreamUtil(48), refusal);
               result.addProperty("placePlan", String.valueOf(plan));
               result.addProperty("placeRefusal", String.valueOf(refusal[0]));
               BlockHitResult top = new BlockHitResult(Vec3.atCenterOf(cell.above()), Direction.UP, cell.below(), false);
               BlockPlaceContext context = new BlockPlaceContext(
                  Minecraft.getInstance().player, InteractionHand.MAIN_HAND, Minecraft.getInstance().player.getInventory().getItem(2), top
               );
               result.addProperty("vanillaCanPlace", context.canPlace());
               result.addProperty("vanillaClickedPos", context.getClickedPos().toString());
            }
         }

         for (String name : new String[]{"cycleCell", "pendingCell", "committedPlaceSupport", "committedObsidianCell"}) {
            try {
               Field field = aura.getClass().getDeclaredField(name);
               field.setAccessible(true);
               Object value = field.get(aura);
               result.addProperty(name, String.valueOf(value));
               if (aura.id().equals("anchor-aura") && name.equals("cycleCell") && value instanceof BlockPos cell) {
                  result.addProperty(
                     "anchorUpFaces", U.init_AABBArr_BlockState(Minecraft.getInstance().level.getBlockState(cell), cell, Direction.UP, 3).length
                  );
               }
            } catch (NoSuchFieldException var30) {
            }
         }

         if (aura.id().equals("anchor-aura")) {
            try {
               Field cycle = aura.getClass().getDeclaredField("cycleCell");
               cycle.setAccessible(true);
               if (cycle.get(aura) instanceof BlockPos cell) {
                  Class<?> stepType = Class.forName(aura.getClass().getName() + "$Step");
                  Object charge = Enum.valueOf((Class)stepType, "CHARGE");
                  Method use = aura.getClass()
                     .getDeclaredMethod("usePlan", BlockPos.class, stepType, double.class, U.Inner_StreamUtil.class, U.Inner_JsonUtil[].class);
                  use.setAccessible(true);
                  U.Inner_JsonUtil[] refusal = new U.Inner_JsonUtil[1];
                  Object plan = use.invoke(aura, cell, charge, Minecraft.getInstance().player.blockInteractionRange(), new U.Inner_StreamUtil(48), refusal);
                  result.addProperty("chargePlan", String.valueOf(plan));
                  result.addProperty("chargeRefusal", String.valueOf(refusal[0]));
               }
            } catch (Exception failure) {
               result.addProperty("chargeProbeError", failure.toString());
            }
         }

         try {
            Field debug = aura.getClass().getDeclaredField("lastDestroyDebug");
            debug.setAccessible(true);
            result.addProperty("destroyDebug", String.valueOf(debug.get(aura)));
         } catch (NoSuchFieldException var28) {
         }

         try {
            Field status = aura.getClass().getDeclaredField("status");
            status.setAccessible(true);
            result.addProperty("status", String.valueOf(status.get(aura)));
         } catch (NoSuchFieldException var27) {
         }

         try {
            Field trace = aura.getClass().getDeclaredField("lastPlaceTrace");
            trace.setAccessible(true);
            result.addProperty("scanTrace", String.valueOf(trace.get(aura)));
         } catch (NoSuchFieldException var26) {
         }

         try {
            Field reasons = aura.getClass().getDeclaredField("supportReasons");
            reasons.setAccessible(true);
            result.addProperty("supportReasons", String.valueOf(reasons.get(aura)));
         } catch (NoSuchFieldException var25) {
         }

         JsonObject settings = new JsonObject();

         for (Setting<?, ?> setting : aura.settings()) {
            settings.addProperty(setting.id(), aura.value(setting.id()));
         }

         result.add("settings", settings);
         result.addProperty("planningMeanUs", (Double)aura.getClass().getDeclaredMethod("planningMeanMicros").invoke(null));
         result.addProperty("planningMaxUs", (Double)aura.getClass().getDeclaredMethod("planningMaxMicros").invoke(null));
      } catch (ReflectiveOperationException failure) {
         result.addProperty("error", failure.toString());
      }

      return result;
   }

   private static String init_Str() {
      try {
         Module module = autismclient.modules.al.init_Module_Str("kill-aura");
         if (module instanceof autismclient.modules.aa aura && aura.isEnabled()) {
            Field field = autismclient.modules.aa.class.getDeclaredField("load");
            field.setAccessible(true);
            return String.valueOf(field.get(module));
         } else {
            return "";
         }
      } catch (ReflectiveOperationException e) {
         return "?";
      }
   }

   private static boolean update() {
      return autismclient.modules.al.init_Module_Str("kill-aura") instanceof autismclient.modules.aa aura && aura.isEnabled() && aura.sync_Z_8();
   }

   private static String init(String key, String fallback) {
      return init_JsonObject.has(key) ? init_JsonObject.get(key).getAsString() : fallback;
   }

   private static double init(String key, double fallback) {
      return init_JsonObject.has(key) ? init_JsonObject.get(key).getAsDouble() : fallback;
   }
}
