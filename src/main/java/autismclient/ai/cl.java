package autismclient.ai;

import autismclient.AutismClientAddon;
import autismclient.ai.a.HashUtil;
import autismclient.api.ai.AiOperation;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap.Types;

final class cl {
   static final Map<String, Integer> init_Map = load();
   static final int init_I = 16;
   static final int load_I = 32;
   static final long init_J = 5000000000L;
   static final int read_I = 6;
   static final int write = 8;
   static final int update = 6;
   static final int sync = 16;
   static final int flush = 10;
   static final int close = 6;
   static final int open = 240;
   static final int poll = 6;
   static final int push = 32;
   static final int pop = 600;
   static final float init_F = 10.0F;
   static final int tick = 16;
   static final int run = 8;
   static final int get = 16;
   static final int set = 12;
   static final int put = 6;
   static final int add = 3;
   static final int clear = 3;
   static final long load_J = 60000000000L;
   static final int reset = 100;
   static final Map<String, String> load_Map = Map.of(
      "minecraft:cow",
      "minecraft:beef",
      "minecraft:mooshroom",
      "minecraft:beef",
      "minecraft:pig",
      "minecraft:porkchop",
      "minecraft:sheep",
      "minecraft:mutton",
      "minecraft:chicken",
      "minecraft:chicken",
      "minecraft:rabbit",
      "minecraft:rabbit"
   );
   static final Map<String, String> read_Map = Map.of(
      "minecraft:beef",
      "minecraft:cooked_beef",
      "minecraft:porkchop",
      "minecraft:cooked_porkchop",
      "minecraft:mutton",
      "minecraft:cooked_mutton",
      "minecraft:chicken",
      "minecraft:cooked_chicken",
      "minecraft:rabbit",
      "minecraft:cooked_rabbit"
   );

   private cl() {
   }

   private static Map<String, Integer> load() {
      Map<String, Integer> kit = new LinkedHashMap<>();
      kit.put("minecraft:diamond_axe", 1);
      kit.put("minecraft:diamond_shovel", 1);
      kit.put("minecraft:diamond_helmet", 1);
      kit.put("minecraft:diamond_chestplate", 1);
      kit.put("minecraft:diamond_leggings", 1);
      kit.put("minecraft:golden_boots", 1);
      kit.put("minecraft:bucket", 3);
      kit.put("minecraft:shield", 1);
      kit.put("minecraft:flint_and_steel", 1);
      return kit;
   }

   static Map<String, Integer> init_Map_bx_CachePool(bx.Inner_CachePool pick, int ironPickCount, Map<String, Integer> missing) {
      Map<String, Integer> targets = new LinkedHashMap<>();
      if (pick.ordinal() < bx.Inner_CachePool.write.ordinal()) {
         targets.put("minecraft:iron_pickaxe", Math.max(0, ironPickCount) + 1);
      }

      for (String id : List.of("minecraft:shield", "minecraft:bucket", "minecraft:flint_and_steel")) {
         if (missing.containsKey(id)) {
            targets.put(id, missing.get(id));
         }
      }

      return targets;
   }

   static boolean init_Z_Z(boolean attempted, int nutrition, int secondsLeft, Map<String, Integer> missing, Map<String, Integer> targets) {
      return !attempted && !targets.isEmpty() && nutrition >= 32 && secondsLeft >= 60 && nutrition < 40 && init_I_Map(missing) == 40;
   }

   static int init_I_I(int bread, int wheat, int hay, int needed) {
      int available = (Math.max(0, wheat) + 9 * Math.max(0, hay)) / 3;
      return Math.max(0, bread) + Math.min(Math.max(0, needed), available);
   }

   static g.Inner_CachePool init_g_CachePool_g_StreamUtil_I(g.Inner_StreamUtil graph, int needed) {
      int held = graph.init_I("minecraft:bread");
      int goal = init_I_I(held, graph.init_I("minecraft:wheat"), graph.init_I("minecraft:hay_block"), needed);
      if (goal <= held) {
         return null;
      }

      g.Inner_CachePool plan = g.init(graph, "minecraft:bread", goal);
      return plan.complete() && !plan.steps().isEmpty() ? plan : null;
   }

   static boolean init_Z_I_I(int trips, int secondsLeft) {
      return trips < 2 && secondsLeft >= 60;
   }

   static int init_I_Map(Map<String, Integer> missing) {
      return missing.keySet().stream().anyMatch(id -> id.startsWith("minecraft:diamond_")) ? 40 : 32;
   }

   static int init_I_I(int eyes, int pearls, int missing) {
      return Math.min(32, Math.max(0, eyes) + Math.max(0, pearls) + Math.max(1, missing));
   }

   static boolean init_Z_I(int hunger, int preparedNutrition, int rawMeat, int cookingSteps, int secondsLeft, boolean quiet, AiOperation current) {
      return hunger <= 16
         && preparedNutrition < Math.max(4, 20 - hunger)
         && rawMeat >= 4
         && cookingSteps < 6
         && secondsLeft >= 60
         && quiet
         && (current == null || current.canInterruptNow() && !current.requiresReconciliationAfterEmergency());
   }

   static autismclient.ai.a.ClassCache.Inner_JsonUtil init_ClassCache_JsonUti_BlockPos(BlockPos surface) {
      return autismclient.ai.a.ClassCache.Inner_JsonUtil.init_ClassCache_JsonUti_BlockPos_D(surface, 2.0);
   }

   static BlockPos init_BlockPos_BlockPos(BlockPos from, int x, int z, Function<BlockPos, BlockPos> observedSurface) {
      for (int quarter = 4; quarter >= 1; quarter--) {
         int cx = from.getX() + (int)Math.round((x - from.getX()) * quarter / 4.0);
         int cz = from.getZ() + (int)Math.round((z - from.getZ()) * quarter / 4.0);

         for (int radius = 0; radius <= 4; radius++) {
            BlockPos best = null;
            double nearest = 1.0 / 0.0;

            for (int dx = -radius; dx <= radius; dx++) {
               for (int dz = -radius; dz <= radius; dz++) {
                  if (Math.max(Math.abs(dx), Math.abs(dz)) == radius) {
                     BlockPos column = new BlockPos(cx + dx, from.getY(), cz + dz);
                     if (!(Math.hypot(column.getX() - from.getX(), column.getZ() - from.getZ()) < 8.0)) {
                        BlockPos surface = observedSurface.apply(column);
                        if (surface != null) {
                           double distance = surface.distSqr(from);
                           if (distance < nearest) {
                              best = surface;
                              nearest = distance;
                           }
                        }
                     }
                  }
               }
            }

            if (best != null) {
               return best;
            }
         }
      }

      return null;
   }

   static BlockPos init_BlockPos_Minecraft_BlockPos(Minecraft mc, BlockPos column) {
      if (!mc.level.hasChunkAt(column)) {
         return null;
      }

      int y = mc.level.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, column.getX(), column.getZ());
      BlockPos surface = new BlockPos(column.getX(), y, column.getZ());
      return HashUtil.init_Z(surface)
            && mc.level.getFluidState(surface).isEmpty()
            && mc.level.getFluidState(surface.above()).isEmpty()
            && !mc.level.getBlockState(surface.below()).is(BlockTags.LEAVES)
            && !mc.level.getBlockState(surface.below()).is(BlockTags.LOGS)
         ? surface
         : null;
   }

   static boolean init_Z_Map(Map<cl.Inner_BufferHelper, Integer> attemptsByPhase, cl.Inner_BufferHelper phase, int budget) {
      return init_Z_Map(attemptsByPhase, new HashMap<>(), phase, budget, System.nanoTime());
   }

   static boolean init_Z_Map(
      Map<cl.Inner_BufferHelper, Integer> attemptsByPhase, Map<cl.Inner_BufferHelper, Long> lastAtNanos, cl.Inner_BufferHelper phase, int budget, long now
   ) {
      return init_Z_Map(attemptsByPhase, lastAtNanos, new HashMap<>(), phase, budget, now);
   }

   static boolean init_Z_Map(
      Map<cl.Inner_BufferHelper, Integer> attemptsByPhase,
      Map<cl.Inner_BufferHelper, Long> lastAtNanos,
      Map<cl.Inner_BufferHelper, Integer> repeatsByPhase,
      cl.Inner_BufferHelper phase,
      int budget,
      long now
   ) {
      Long last = lastAtNanos.get(phase);
      if (last == null || now - last > 60000000000L || repeatsByPhase.merge(phase, 1, Integer::sum) >= 100) {
         attemptsByPhase.merge(phase, 1, Integer::sum);
         lastAtNanos.put(phase, now);
         repeatsByPhase.put(phase, 0);
      }

      return attemptsByPhase.getOrDefault(phase, 0) <= budget;
   }

   static AiOperation init_AiOperation_JsonObject(JsonObject args) {
      final Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         final int timeoutSeconds = Math.max(600, Math.min(14400, ag.init_I_JsonObject(args, "timeoutSeconds", 7200)));
         aG.init_V_Z(true);
         aA.init_Z = false;
         aB.init_Z = false;
         return new AiOperation() {
            final bJ init_bJ = bJ.init_bJ();
            final Object init_Obj = bK.init_Obj();
            final String init_Str = cL.init(mc);
            final UUID init_UUID = mc.player.getUUID();
            final Object load_Obj = mc.getConnection();
            final long init_J = ca.load_J();
            final int init_I = autismclient.util.ck.init_ck().state_I();
            final long load_J = System.nanoTime();
            final long read_J = this.load_J + timeoutSeconds * 1000000000L;
            final Map<cl.Inner_BufferHelper, Integer> init_Map = new LinkedHashMap<>();
            final Map<String, Integer> load_Map = new LinkedHashMap<>();
            final Map<cl.Inner_BufferHelper, Integer> read_Map = new LinkedHashMap<>();
            final Map<cl.Inner_BufferHelper, Long> write_Map = new LinkedHashMap<>();
            final Map<cl.Inner_BufferHelper, Integer> update_Map = new LinkedHashMap<>();
            final Map<cl.Inner_BufferHelper, Long> sync_Map = new LinkedHashMap<>();
            final JsonArray init_JsonArray = new JsonArray();
            AiOperation init_AiOperation;
            cl.Inner_BufferHelper init_cl_BufferHelper;
            long write_J;
            boolean init_Z;
            boolean load_Z;
            boolean read_Z;
            boolean write_Z;
            boolean update_Z;
            boolean sync_Z;
            final cl.Inner_DataUtil init_cl_DataUtil = new cl.Inner_DataUtil();
            int load_I;
            long update_J;
            String load_Str = cL.load(mc);
            int read_I;
            int write_I;
            int update_I;
            int sync_I;
            int flush_I;
            int close_I;
            boolean flush_Z;
            long sync_J = -1L;
            boolean close_Z;
            boolean open_Z;
            int open_I;
            int poll_I;
            long flush_J;
            long close_J;
            final Y init_Y = new Y(
               X.Inner_CachePool.standard()
                  .keeping(
                     List.of(
                        "minecraft:blaze_rod",
                        "minecraft:ender_pearl",
                        "minecraft:ender_eye",
                        "minecraft:diamond",
                        "minecraft:gold_ingot",
                        "minecraft:iron_ingot"
                     )
                  ),
               "speedrun"
            );
            String read_Str = "";
            float init_F = -1.0F;
            int push_I = -1;
            int pop_I;
            final ArrayDeque<String> init_ArrayDeque = new ArrayDeque<>();
            boolean poll_Z;
            String write_Str = "";
            long open_J;
            boolean push_Z;
            int tick_I;
            int run_I;
            boolean pop_Z;
            AiOperation load_AiOperation;
            boolean tick_Z;
            boolean run_Z;
            int get_I;
            long poll_J;
            BlockPos init_BlockPos;
            int set;
            boolean get_Z;

            void init_V_26() {
               float health = mc.player.getHealth();
               int food = mc.player.getFoodData().getFoodLevel();
               if (this.init_F >= 0.0F && health < this.init_F - 0.01F) {
                  DamageSource source = mc.player.getLastDamageSource();
                  cl.init_V_Str_ObjArr(
                     "[AI] speedrun hurt {} -> {} by {} in {} at {}",
                     String.format("%.1f", this.init_F),
                     String.format("%.1f", health),
                     source == null
                        ? "?"
                        : source.getMsgId()
                           + (source.getEntity() == null ? "" : " " + BuiltInRegistries.ENTITY_TYPE.getKey(source.getEntity().getType()).getPath()),
                     this.init_cl_BufferHelper,
                     mc.player.blockPosition().toShortString()
                  );
               }

               if (food != this.push_I && (food <= 14 || this.push_I <= 14)) {
                  cl.init_V_Str_ObjArr("[AI] speedrun food {} (health {})", food, String.format("%.1f", health));
               }

               this.init_F = health;
               this.push_I = food;
               Options o = mc.options;
               this.init_ArrayDeque
                  .addLast(
                     String.format(
                        "%d %.1f,%.1f,%.1f %s%s%s%s%s%s%s%s",
                        mc.player.tickCount,
                        mc.player.getX(),
                        mc.player.getY(),
                        mc.player.getZ(),
                        mc.player.onGround() ? "g" : "a",
                        o.keyUp.isDown() ? "W" : "",
                        o.keyLeft.isDown() ? "A" : "",
                        o.keyDown.isDown() ? "S" : "",
                        o.keyRight.isDown() ? "D" : "",
                        o.keyJump.isDown() ? "J" : "",
                        o.keyShift.isDown() ? "C" : "",
                        o.keySprint.isDown() ? "R" : ""
                     )
                  );

               while (this.init_ArrayDeque.size() > 30) {
                  this.init_ArrayDeque.removeFirst();
               }

               if (!mc.player.onGround() && mc.player.fallDistance > 6.0 && !this.poll_Z) {
                  this.poll_Z = true;
                  cl.init_V_Str_ObjArr(
                     "[AI] speedrun falling {} in {} during '{}': {}",
                     String.format("%.1f", mc.player.fallDistance),
                     this.init_cl_BufferHelper,
                     cl.init_Str_Str(this.write_Str),
                     String.join(" | ", this.init_ArrayDeque)
                  );
               } else if (mc.player.onGround()) {
                  this.poll_Z = false;
               }
            }

            @Override
            public AiOperation.Result tick() {
               this.init_bJ.init(this.init_cl_BufferHelper == null ? "START" : this.init_cl_BufferHelper.name(), this.write_Str);
               if (mc.player != null && mc.level != null) {
                  if (!this.init_Str.equals(cL.init(mc)) || this.load_Obj != mc.getConnection() || !this.init_UUID.equals(mc.player.getUUID())) {
                     return this.init(AiOperation.Status.CANCELLED, "World or player identity changed; revalidate before resuming");
                  }

                  if (mc.player.isAlive() && ca.load_J() == this.init_J) {
                     bK.init_V();
                     if (this.init_cl_BufferHelper != null) {
                        W.init(this, 0, "Beating the game", W.load(this.init_cl_BufferHelper.name()), "");
                     }

                     String dimension = cL.load(mc);
                     if (!this.load_Str.equals(dimension)) {
                        this.load_Str = dimension;
                        this.read_Z = false;
                        this.get_I = 0;
                        this.flush_I = 0;
                     }

                     if (System.nanoTime() >= this.update_J) {
                        this.update_J = System.nanoTime() + 10000000000L;
                        this.init_V_5("RUNNING");
                     }

                     if (System.nanoTime() > this.read_J) {
                        return this.init(
                           this.canInterruptNow() ? AiOperation.Status.TIMED_OUT : AiOperation.Status.UNCERTAIN,
                           "Out of time in phase " + this.init_cl_BufferHelper
                        );
                     }

                     this.init_V_26();
                     if (++this.pop_I >= 600) {
                        this.pop_I = 0;
                        cl.init_V_Str_ObjArr(
                           "[AI] speedrun heartbeat {} at {} health {} after {} s: {} [endermen {}, piglins {}, pearls {}, rods {}, eyes {}, food {}, time {}]",
                           this.init_cl_BufferHelper,
                           mc.player.blockPosition().toShortString(),
                           String.format("%.1f", mc.player.getHealth()),
                           this.init_J_2(),
                           cl.init_Str_Str(this.write_Str),
                           cO.init_List_Str_D("minecraft:enderman", 64.0).size(),
                           cO.init_List_Str_D("minecraft:piglin", 64.0).size(),
                           e.init_I("minecraft:ender_pearl"),
                           e.init_I("minecraft:blaze_rod"),
                           e.init_I("minecraft:ender_eye"),
                           this.load_I_13(),
                           mc.level.getOverworldClockTime() % 24000L
                        );
                     }

                     AiOperation pearlOp = this.init_AiOperation != null && !this.push_Z && this.init_Z_24() && this.init_AiOperation.canInterruptNow()
                        ? O.read(32.0)
                        : null;
                     if (pearlOp != null) {
                        cl.init_V_Str_ObjArr(
                           "[AI] speedrun: an enderman within {} blocks in {}; hunting it for a pearl ({} held)",
                           16,
                           this.init_cl_BufferHelper,
                           e.init_I("minecraft:ender_pearl")
                        );
                        this.init_AiOperation.cancel();
                        this.init_AiOperation = null;
                        this.open_J = System.nanoTime() + 60000000000L;
                        this.push_Z = true;
                        return this.init(this.init_cl_BufferHelper, pearlOp, null);
                     }

                     if (this.init_AiOperation != null && this.load_Z_14()) {
                        cl.init_V_Str_ObjArr(
                           "[AI] speedrun: health {} in {} with nothing hostile near; healing before going on",
                           String.format("%.1f", mc.player.getHealth()),
                           this.init_cl_BufferHelper
                        );
                        this.init_AiOperation.cancel();
                        this.init_AiOperation = null;
                        this.close_J = 0L;
                     }

                     if (this.init_AiOperation != null
                        && this.load_AiOperation == null
                        && this.init_cl_BufferHelper != cl.Inner_BufferHelper.close
                        && this.init_cl_BufferHelper != cl.Inner_BufferHelper.open
                        && this.init_cl_BufferHelper != cl.Inner_BufferHelper.poll) {
                        this.load_AiOperation = bJ.init_AiOperation_Str("survival", "campaign emergency", Y.init_AiOperation_Minecraft(mc));
                        if (this.load_AiOperation != null) {
                           this.tick_Z = this.init_AiOperation.requiresReconciliationAfterEmergency();
                           this.init_AiOperation.cancel();
                           this.init_AiOperation = null;

                           for (KeyMapping key : new KeyMapping[]{
                              mc.options.keyUp,
                              mc.options.keyDown,
                              mc.options.keyLeft,
                              mc.options.keyRight,
                              mc.options.keyJump,
                              mc.options.keyShift,
                              mc.options.keySprint,
                              mc.options.keyAttack,
                              mc.options.keyUse
                           }) {
                              key.setDown(false);
                           }

                           cl.init_V_Str_ObjArr(
                              "[AI] speedrun: {} at {} in {}; answering it first",
                              mc.player.isInLava() ? "in lava" : (EventBus.init(mc.player) ? "out of air" : "on fire"),
                              mc.player.blockPosition().toShortString(),
                              this.init_cl_BufferHelper
                           );
                        }
                     }

                     if (this.load_AiOperation != null) {
                        AiOperation.Result e = this.load_AiOperation.tick();
                        if (!e.terminal()) {
                           return this.init_AiOperation_Result_13(this.init_cl_BufferHelper + ": " + e.message());
                        }

                        this.load_AiOperation.cancel();
                        this.load_AiOperation = null;
                        if (this.tick_Z || e.status() == AiOperation.Status.UNCERTAIN) {
                           return this.init(AiOperation.Status.UNCERTAIN, "Emergency handled; interrupted action requires fresh reconciliation before resuming");
                        }
                     }

                     if (this.init_AiOperation != null && this.pop_Z) {
                        AiOperation.Result g = this.init_Y.init_AiOperation_Result_AiOperation(this.init_AiOperation, () -> {
                           this.init_AiOperation.cancel();
                           this.init_AiOperation = null;
                           this.pop_Z = false;
                        }, this::read_I_6);
                        if (g != null) {
                           return g.terminal() ? this.init(g.status(), g.message()) : g;
                        }
                     }

                     if (this.init_AiOperation != null) {
                        AiOperation.Result r = this.init_AiOperation.tick();
                        if (!r.terminal()) {
                           this.write_Str = r.message();
                           return this.init_AiOperation_Result_13(this.init_cl_BufferHelper + ": " + r.message());
                        }

                        this.init_AiOperation.cancel();
                        this.init_AiOperation = null;
                        if (this.init_cl_BufferHelper == cl.Inner_BufferHelper.update) {
                           this.sync_I = r.status() == AiOperation.Status.SUCCEEDED ? 0 : this.sync_I + 1;
                        }

                        cl.init_V_Str_ObjArr("[AI] speedrun step {} in {}: {}", r.status(), this.init_cl_BufferHelper, cl.init_Str_Str(r.message()));
                        if (this.close_Z) {
                           this.close_Z = false;
                           if (r.status() != AiOperation.Status.SUCCEEDED) {
                              this.sync_J = mc.level.getOverworldClockTime() / 24000L;
                           }
                        }

                        this.init_JsonArray
                           .add(
                              ag.init_JsonObject_ObjArr(
                                 "phase",
                                 this.init_cl_BufferHelper.name(),
                                 "status",
                                 r.status().name(),
                                 "message",
                                 cl.init_Str_Str(r.message()),
                                 "seconds",
                                 (System.nanoTime() - this.poll_J) / 1000000000L,
                                 "tick",
                                 autismclient.util.ck.init_ck().state_I(),
                                 "data",
                                 bK.load_JsonObject(r.dataJson())
                              )
                           );

                        while (this.init_JsonArray.size() > 256) {
                           this.init_JsonArray.remove(0);
                        }

                        bL.init_V_Str_JsonObject("speedrun_leg", this.init_JsonArray.get(this.init_JsonArray.size() - 1).getAsJsonObject());
                        if (r.status() == AiOperation.Status.CANCELLED) {
                           return this.init(AiOperation.Status.CANCELLED, "Cancelled during " + this.init_cl_BufferHelper);
                        }

                        if (r.status() == AiOperation.Status.UNCERTAIN
                           && cl.init_Z_Map(this.read_Map, this.write_Map, this.update_Map, this.init_cl_BufferHelper, 3, System.nanoTime())) {
                           cl.init_V_Str_ObjArr(
                              "[AI] speedrun: {} could not be established in {} ({} of {}); observing the world again",
                              cl.init_Str_Str(r.message()),
                              this.init_cl_BufferHelper,
                              this.read_Map.get(this.init_cl_BufferHelper),
                              3
                           );
                           return this.load_AiOperation_Result_16();
                        }

                        if (r.status() == AiOperation.Status.UNCERTAIN) {
                           return this.init(AiOperation.Status.UNCERTAIN, this.init_cl_BufferHelper + ": " + r.message());
                        }

                        JsonObject legEvidence = bK.init_JsonObject(r.dataJson());
                        boolean pearlRouteChange = this.init_cl_BufferHelper == cl.Inner_BufferHelper.open
                           && r.status() == AiOperation.Status.FAILED
                           && ag.init_Z_JsonObject(legEvidence, "pearlRouteChange", false);
                        if (pearlRouteChange) {
                           if (this.open_I >= 1) {
                              return this.init(
                                 AiOperation.Status.FAILED,
                                 "Pearl supplies remain short after an Overworld search and a Nether recovery; the route needs a new strategy"
                              );
                           }

                           this.open_I++;
                           this.poll_I = cl.init_I_I(
                              e.init_I("minecraft:ender_eye"), e.init_I("minecraft:ender_pearl"), ag.init_I_JsonObject(legEvidence, "pearlsNeeded", 1)
                           );
                           this.flush_Z = true;
                           cl.init_V_Str_ObjArr("[AI] speedrun: bounded surface pearl search exhausted; Nether recovery for {} total eyes/pearls", this.poll_I);
                        }

                        if (this.init_cl_BufferHelper == cl.Inner_BufferHelper.poll
                           && r.status() == AiOperation.Status.SUCCEEDED
                           && !this.run_Z
                           && !this.push_Z
                           && mc.level.dimension() == Level.OVERWORLD) {
                           return this.init(
                              AiOperation.Status.SUCCEEDED, "Beat the game: the dragon is dead and the exit portal taken, in " + this.init_J_2() + " s"
                           );
                        }

                        boolean lowHealth = r.status() != AiOperation.Status.SUCCEEDED
                           && this.init_cl_BufferHelper != cl.Inner_BufferHelper.sync
                           && (r.failure() == A.start || mc.player.getHealth() <= 6.0F);
                        if (lowHealth) {
                           this.close_J = 0L;
                        }

                        boolean wasPearlLeg = this.push_Z || this.run_Z;
                        this.push_Z = false;
                        this.run_Z = false;
                        boolean quickFail = r.status() != AiOperation.Status.SUCCEEDED
                           && r.status() != AiOperation.Status.CANCELLED
                           && System.nanoTime() - this.poll_J < 5000000000L;
                        BlockPos feetNow = mc.player.blockPosition();
                        boolean stuck = quickFail && feetNow.equals(this.init_BlockPos);
                        this.init_BlockPos = quickFail ? feetNow : null;
                        if (stuck && this.set < 6 && !wasPearlLeg) {
                           this.set++;
                           this.init_BlockPos = null;
                           cl.init_V_Str_ObjArr(
                              "[AI] speedrun: {} failed at once twice from {}; moving off first ({} of {})",
                              this.init_cl_BufferHelper,
                              feetNow.toShortString(),
                              this.set,
                              6
                           );
                           this.run_Z = true;
                           return this.init(this.init_cl_BufferHelper, this.init_AiOperation_4(), null);
                        }

                        if (this.init_cl_BufferHelper == cl.Inner_BufferHelper.sync
                           && r.status() != AiOperation.Status.SUCCEEDED
                           && r.status() != AiOperation.Status.CANCELLED) {
                           this.run_I++;
                        }

                        if (r.status() != AiOperation.Status.SUCCEEDED
                           && !lowHealth
                           && this.init_cl_BufferHelper != cl.Inner_BufferHelper.load
                           && this.init_cl_BufferHelper != cl.Inner_BufferHelper.sync
                           && this.init_cl_BufferHelper != cl.Inner_BufferHelper.update
                           && !wasPearlLeg
                           && !pearlRouteChange) {
                           this.read_Str = this.init_cl_BufferHelper + ": " + cl.init_Str_Str(r.message());
                           int n = this.init_Map.merge(this.init_cl_BufferHelper, 1, Integer::sum);
                           int same = this.load_Map.merge(this.init_cl_BufferHelper + ": " + cl.init_Str_Str(r.message()), 1, Integer::sum);
                           if (n >= 6 || same >= 3) {
                              return this.init(
                                 AiOperation.Status.FAILED,
                                 "Phase "
                                    + this.init_cl_BufferHelper
                                    + " failed "
                                    + (same >= 3 ? same + " times the same way" : n + " times")
                                    + ": "
                                    + cl.init_Str_Str(r.message())
                              );
                           }
                        }
                     }

                     if (!mc.player.onGround() && !mc.player.isInWater() && !mc.player.onClimbable()) {
                        return this.init_AiOperation_Result_13("Landing");
                     } else {
                        AiOperation.Result g = this.init_Y.init_AiOperation_Result_AiOperation(null, () -> {}, this::read_I_6);
                        if (g != null) {
                           this.write_Str = g.message();
                           return g;
                        } else {
                           AiOperation.Result heal = this.init_AiOperation_Result_34();
                           return heal != null ? heal : this.load_AiOperation_Result_16();
                        }
                     }
                  } else {
                     return this.init(
                        AiOperation.Status.FAILED, "The deathless attempt ended: server/player death was observed in " + this.init_cl_BufferHelper
                     );
                  }
               } else {
                  return this.init(AiOperation.Status.CANCELLED, "Left the world");
               }
            }

            boolean init_Z_24() {
               if (this.init_cl_BufferHelper != cl.Inner_BufferHelper.flush
                  && this.init_cl_BufferHelper != cl.Inner_BufferHelper.load
                  && this.init_cl_BufferHelper != cl.Inner_BufferHelper.update) {
                  return false;
               }

               if (mc.level.dimension() == Level.OVERWORLD && this.read_Z_10() && !(mc.player.getHealth() < 14.0F)) {
                  if (System.nanoTime() < this.open_J) {
                     return false;
                  }

                  if (e.init_I("minecraft:ender_pearl") + e.init_I("minecraft:ender_eye") >= 16) {
                     return false;
                  }

                  if (O.init_Entity_Minecraft(mc, "minecraft:enderman", 16.0) != null) {
                     return true;
                  }

                  for (Entity e : cO.init_List_Str_D("minecraft:enderman", 32.0)) {
                     if (mc.player.hasLineOfSight(e)) {
                        return true;
                     }
                  }

                  return false;
               } else {
                  return false;
               }
            }

            boolean load_Z_14() {
               return this.init_cl_BufferHelper != cl.Inner_BufferHelper.sync
                  && this.init_cl_BufferHelper != cl.Inner_BufferHelper.load
                  && mc.level.dimension() == Level.OVERWORLD
                  && mc.player.getHealth() < 10.0F
                  && System.nanoTime() > this.close_J
                  && mc.player.onGround()
                  && X.init_X_BufferHelper().hostilesNear() == 0
                  && this.init_AiOperation.canInterruptNow();
            }

            AiOperation.Result init_AiOperation_Result_34() {
               if (mc.player.getHealth() >= 12.0F) {
                  this.run_I = 0;
               }

               if (!(mc.player.getHealth() >= 12.0F) && System.nanoTime() >= this.close_J) {
                  if (mc.player.getFoodData().getFoodLevel() < 18 && cq.init(mc.player.getInventory()) >= 0 && this.tick_I++ < 3) {
                     AiOperation.Result meal = this.init(cl.Inner_BufferHelper.sync, cq.init(ag.init_JsonObject_ObjArr("food", 20, "timeoutSeconds", 20)), null);
                     this.pop_Z = true;
                     return meal;
                  } else {
                     this.tick_I = 0;
                     if (mc.player.getFoodData().getFoodLevel() < 18
                        && cq.init(mc.player.getInventory()) < 0
                        && mc.level.dimension() == Level.OVERWORLD
                        && System.nanoTime() > this.flush_J) {
                        this.flush_J = System.nanoTime() + 180000000000L;
                        return this.init(cl.Inner_BufferHelper.load, cl.init_AiOperation_I_I(10, 240), null);
                     } else if (this.run_I > 0) {
                        this.close_J = System.nanoTime() + 10000000000L;
                        AiOperation.Result rest = this.init(cl.Inner_BufferHelper.sync, ValueSink.init(200), null);
                        this.pop_Z = true;
                        return rest;
                     } else {
                        this.close_J = System.nanoTime() + 60000000000L;
                        return (mc.player.getFoodData().getFoodLevel() >= 18 || mc.player.getHealth() <= 6.0F) && mc.level.dimension() != Level.END
                           ? this.init(
                              cl.Inner_BufferHelper.sync,
                              X.init(RingBuffer.init(ag.init_JsonObject_ObjArr("healTo", 18, "stay", true, "timeoutSeconds", 180))),
                              null
                           )
                           : null;
                     }
                  }
               } else {
                  return null;
               }
            }

            AiOperation init_AiOperation_4() {
               BlockPos feet = mc.player.blockPosition();
               int[][] dirs = new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
               int[] best = dirs[this.set % 4];
               int bestOpen = -1;

               for (int[] d : dirs) {
                  int open = 0;

                  for (int i = 1; i <= 6; i++) {
                     BlockPos c = feet.offset(d[0] * i, 0, d[1] * i);
                     if (mc.level.getBlockState(c).getCollisionShape(mc.level, c).isEmpty() && mc.level.getFluidState(c).isEmpty()) {
                        open++;
                     }
                  }

                  if (open > bestOpen) {
                     bestOpen = open;
                     best = d;
                  }
               }

               int x = feet.getX() + best[0] * 6;
               int z = feet.getZ() + best[1] * 6;
               return HashUtil.init(
                  new BlockPos(x, feet.getY(), z),
                  2.0,
                  true,
                  true,
                  true,
                  40,
                  autismclient.ai.a.ClassCache.Inner_IoWorker.load,
                  true,
                  2.0,
                  new aB.Inner_NetHelper(x, z, 3),
                  HashUtil.Inner_BufferHelper.DEFAULT
               );
            }

            BlockPos init_BlockPos_15() {
               BlockPos feet = mc.player.blockPosition();
               List<BlockPos> portals = new ArrayList<>();

               for (BlockPos c : BlockPos.betweenClosed(feet.offset(-11, -5, -11), feet.offset(11, 6, 11))) {
                  if (mc.level.getBlockState(c).is(Blocks.NETHER_PORTAL)) {
                     portals.add(c.immutable());
                  }
               }

               if (!portals.isEmpty() && this.init(feet, portals, 1)) {
                  BlockPos best = null;
                  double bestDistance = 1.7976931348623157E308;

                  for (BlockPos c : BlockPos.betweenClosed(feet.offset(-8, -3, -8), feet.offset(8, 3, 8))) {
                     if (!this.init(c, portals, 2)) {
                        BlockPos floor = c.below();
                        BlockState floorState = mc.level.getBlockState(floor);
                        if (!floorState.getCollisionShape(mc.level, floor).isEmpty()
                           && !floorState.is(Blocks.MAGMA_BLOCK)
                           && !floorState.is(BlockTags.LEAVES)
                           && floorState.getFluidState().isEmpty()) {
                           boolean open = true;

                           for (BlockPos body : List.of(c, c.above())) {
                              BlockState state = mc.level.getBlockState(body);
                              if (!state.getCollisionShape(mc.level, body).isEmpty() || !state.getFluidState().isEmpty() || state.is(BlockTags.FIRE)) {
                                 open = false;
                                 break;
                              }
                           }

                           if (open) {
                              double distance = c.distSqr(feet);
                              if (distance < bestDistance) {
                                 bestDistance = distance;
                                 best = c.immutable();
                              }
                           }
                        }
                     }
                  }

                  return best;
               } else {
                  return null;
               }
            }

            boolean init(BlockPos c, List<BlockPos> cells, int range) {
               for (BlockPos p : cells) {
                  if (Math.abs(p.getX() - c.getX()) <= range && Math.abs(p.getY() - c.getY()) <= range + 1 && Math.abs(p.getZ() - c.getZ()) <= range) {
                     return true;
                  }
               }

               return false;
            }

            AiOperation.Result load_AiOperation_Result_16() {
               ResourceKey<Level> dimension = mc.level.dimension();
               if (dimension != Level.END && this.get_I < 3) {
                  BlockPos clear = this.init_BlockPos_15();
                  if (clear != null) {
                     this.get_I++;
                     this.run_Z = true;
                     cl.init_V_Str_ObjArr("[AI] speedrun: stepping clear of the portal to {}", clear.toShortString());
                     return this.init(
                        this.init_cl_BufferHelper == null ? cl.Inner_BufferHelper.init : this.init_cl_BufferHelper,
                        HashUtil.init_AiOperation_BlockPos(clear, 0.6, true, false, false, 20),
                        null
                     );
                  }

                  this.get_I = 0;
               }

               if (dimension == Level.END) {
                  return !aB.load_Z()
                     ? this.init(
                        AiOperation.Status.UNCERTAIN,
                        "Already in the End, but the required barter and blaze-spawner route cannot be verified for this campaign"
                     )
                     : this.init(cl.Inner_BufferHelper.poll, u.init(ag.init_JsonObject_ObjArr("timeoutSeconds", Math.min(3600, this.read_I_6()))), null);
               }

               if (this.flush_Z && e.init_I("minecraft:ender_eye") + e.init_I("minecraft:ender_pearl") >= this.poll_I) {
                  this.flush_Z = false;
               }

               if (dimension == Level.NETHER) {
                  return this.init(
                     cl.Inner_BufferHelper.close,
                     aB.init_AiOperation(
                        ag.init_JsonObject_ObjArr(
                           "rods",
                           8,
                           "pearls",
                           this.flush_Z ? this.poll_I : 16,
                           "pearlRecovery",
                           this.flush_Z,
                           "timeoutSeconds",
                           Math.min(5400, this.read_I_6())
                        )
                     ),
                     null
                  );
               }

               if (dimension != Level.OVERWORLD) {
                  return this.init(AiOperation.Status.UNCERTAIN, "Campaign entered an unsupported dimension; revalidate the route");
               }

               if (aB.init_Z && this.init_Map_5().isEmpty() && this.read_Z_10()) {
                  long dayTime = mc.level.getOverworldClockTime() % 24000L;
                  boolean night = dayTime > 13000L && dayTime < 23000L;
                  if (this.load_I_13() < 10 && night && this.read_I >= 1 && this.write_I < 1 && RingBuffer.init()) {
                     this.write_I++;
                     cl.init_V_Str_ObjArr("[AI] speedrun: home for food at night (day time {}); a hole until dawn", dayTime);
                     return this.init(cl.Inner_BufferHelper.load, RingBuffer.init(ag.init_JsonObject_ObjArr("timeoutSeconds", 600)), null);
                  }

                  if (this.load_I_13() < 10 && this.read_I < 3) {
                     this.read_I++;
                     return this.init(cl.Inner_BufferHelper.load, cl.init_AiOperation_I_I(10, 240), null);
                  }

                  AiOperation.Result cook = this.read_AiOperation_Result_11();
                  if (cook != null) {
                     return cook;
                  }

                  if (bK.read() < 32) {
                     return this.flush_I++ >= 4
                        ? this.init(AiOperation.Status.FAILED, "Nether revisit blocked: safe food reserve is still insufficient")
                        : this.init(cl.Inner_BufferHelper.load, cl.init_AiOperation_I_I(this.load_I_13() + Math.max(4, (32 - bK.read() + 5) / 6), 240), null);
                  }

                  if (e.init_I("minecraft:water_bucket") < 2) {
                     return this.init(cl.Inner_BufferHelper.read, aD.init(2, Math.min(180, this.read_I_6())), null);
                  }

                  if (bK.update() < 64) {
                     return this.init(
                        cl.Inner_BufferHelper.read,
                        aE.init(ag.init_JsonObject_ObjArr("item", "minecraft:cobblestone", "count", 64, "timeoutSeconds", Math.min(180, this.read_I_6()))),
                        null
                     );
                  }

                  this.read_I = 0;
                  this.write_I = 0;
                  return this.init(
                     cl.Inner_BufferHelper.flush,
                     aA.init(ag.init_JsonObject_ObjArr("skipVillage", true, "beds", 0, "timeoutSeconds", Math.min(3600, this.read_I_6()))),
                     null
                  );
               } else {
                  boolean strongholdLeft = aB.init_Z() && bK.init_I() + e.init_I("minecraft:ender_pearl") > 0;
                  if (this.flush_Z
                     || !strongholdLeft && (!aB.load_Z() || bK.init_I() < 16 && this.open_I <= 0 || !this.init_Map_5().isEmpty() || !this.read_Z_10())) {
                     Map<String, Integer> missing = this.init_Map_5();
                     if (!missing.isEmpty()) {
                        int nutritionGoal = cl.init_I_Map(missing);
                        if (!this.init_Z && this.init_I_34() == 0 && af.init_I(af.load_af_DataUtil("log")) < 16) {
                           this.init_Z = true;
                           return this.init(
                              cl.Inner_BufferHelper.init, aE.init(ag.init_JsonObject_ObjArr("item", "log", "count", 16, "timeoutSeconds", 300)), null
                           );
                        }

                        if (!this.update_Z
                           && !cl.init_Z_Minecraft(mc)
                           && bK.read() < 40
                           && 4 * af.init_I(af.load_af_DataUtil("log")) + af.init_I(af.load_af_DataUtil("planks")) >= 12) {
                           this.update_Z = true;
                           return this.init(
                              cl.Inner_BufferHelper.read,
                              aE.init(ag.init_JsonObject_ObjArr("item", "minecraft:wooden_axe", "count", 1, "staged", true, "timeoutSeconds", 60)),
                              null
                           );
                        }

                        if (bK.read() < nutritionGoal && this.close_I < 2) {
                           AiOperation bread = cl.load(Math.max(1, (nutritionGoal - bK.read() + 4) / 5), Math.min(45, this.read_I_6()));
                           if (bread != null) {
                              this.close_I++;
                              return this.init(cl.Inner_BufferHelper.update, bread, null);
                           }
                        }

                        AiOperation.Result cooked = this.read_AiOperation_Result_11();
                        if (cooked != null) {
                           return cooked;
                        }

                        Map<String, Integer> bootstrap = cl.init_Map_bx_CachePool(bx.init_bx_CachePool(mc), e.init_I("minecraft:iron_pickaxe"), missing);
                        if (cl.init_Z_Z(this.sync_Z, bK.read(), this.read_I_6(), missing, bootstrap)) {
                           this.sync_Z = true;
                           String first = bootstrap.containsKey("minecraft:iron_pickaxe") ? "minecraft:iron_pickaxe" : "minecraft:shield";
                           int firstGoal = bootstrap.getOrDefault(first, Math.max(1, e.init_I(first)));
                           JsonObject earlyKit = new JsonObject();
                           bootstrap.forEach((id, n) -> {
                              if (!id.equals(first)) {
                                 earlyKit.addProperty(id, n);
                              }
                           });
                           cl.init_V_Str_ObjArr(
                              "[AI] speedrun: prepare required iron gear before the diamond food reserve ({} nutrition, {} s maximum)",
                              bK.read(),
                              Math.min(600, this.read_I_6())
                           );
                           return this.init(
                              cl.Inner_BufferHelper.read,
                              bx.init_AiOperation_JsonObject(
                                 ag.init_JsonObject_ObjArr(
                                    "kit",
                                    earlyKit,
                                    "kitPlanks",
                                    bootstrap.containsKey("minecraft:shield") ? 6 : 0,
                                    "timeoutSeconds",
                                    Math.min(600, this.read_I_6())
                                 ),
                                 first,
                                 firstGoal
                              ),
                              null
                           );
                        }

                        if (bK.read() < nutritionGoal
                           && this.load_I > 0
                           && mc.level.getOverworldClockTime() % 24000L > 13000L
                           && mc.level.getOverworldClockTime() % 24000L < 23000L
                           && this.sync_J != mc.level.getOverworldClockTime() / 24000L
                           && !cl.init_Z_Minecraft_D(mc, 48.0)
                           && RingBuffer.init()) {
                           this.close_Z = true;
                           return this.init(
                              cl.Inner_BufferHelper.load, RingBuffer.init(ag.init_JsonObject_ObjArr("timeoutSeconds", Math.min(600, this.read_I_6()))), null
                           );
                        }

                        if (bK.read() < nutritionGoal && this.load_I < 4) {
                           this.load_I++;
                           this.load_Z = true;
                           int foodGoal = this.load_I_13() + Math.max(4, (nutritionGoal - bK.read() + 5) / 6);
                           return this.init(cl.Inner_BufferHelper.load, cl.init_AiOperation_I_I(foodGoal, 240), null);
                        }

                        if (bK.read() < Math.min(nutritionGoal, 12)) {
                           return this.init(
                              AiOperation.Status.FAILED,
                              "Food preparation could not secure the " + nutritionGoal + " nutrition required for the remaining kit work"
                           );
                        }

                        if (bK.read() < nutritionGoal && !this.open_Z) {
                           this.open_Z = true;
                           cl.init_V_Str_ObjArr(
                              "[AI] speedrun: {} nutrition of the {} wanted after {} food trips; going on with the kit", bK.read(), nutritionGoal, this.load_I
                           );
                        }

                        String main = "minecraft:diamond_pickaxe";
                        JsonObject kit = new JsonObject();
                        missing.forEach((id, n) -> {
                           if (!id.equals(main)) {
                              kit.addProperty(id, n);
                           }
                        });
                        return this.init(
                           cl.Inner_BufferHelper.read,
                           bx.init_AiOperation_JsonObject(
                              ag.init_JsonObject_ObjArr("kit", kit, "kitPlanks", 8, "timeoutSeconds", this.read_I_6()), main, missing.getOrDefault(main, 1)
                           ),
                           null
                        );
                     } else if (this.read_Z_10()) {
                        AiOperation carryTable = this.init_cl_DataUtil
                           .init(e.init_I("minecraft:crafting_table"), this.read_I_6(), seconds -> bx.init(seconds, true));
                        if (carryTable != null) {
                           return this.init(cl.Inner_BufferHelper.read, carryTable, null);
                        }

                        if (this.load_I_13() < (cl.init_Z_Minecraft_I(mc, 8) ? 6 : 2) && System.nanoTime() > this.flush_J) {
                           this.flush_J = System.nanoTime() + 600000000000L;
                           return this.init(cl.Inner_BufferHelper.load, cl.init_AiOperation_I_I(10, 240), null);
                        }

                        AiOperation.Result cook = this.read_AiOperation_Result_11();
                        if (cook != null) {
                           return cook;
                        }

                        if (bK.read() < 32) {
                           if (this.flush_I++ >= 4) {
                              return this.init(AiOperation.Status.FAILED, "Nether entry blocked: safe food reserve could not be replenished");
                           }

                           int goal = this.load_I_13() + Math.max(4, (32 - bK.read() + 5) / 6);
                           return this.init(cl.Inner_BufferHelper.load, cl.init_AiOperation_I_I(goal, 240), null);
                        } else if (e.init_I("minecraft:water_bucket") < 2) {
                           return this.init(cl.Inner_BufferHelper.read, aD.init(2, Math.min(180, this.read_I_6())), null);
                        } else {
                           return bK.update() < 64
                              ? this.init(
                                 cl.Inner_BufferHelper.read,
                                 aE.init(
                                    ag.init_JsonObject_ObjArr("item", "minecraft:cobblestone", "count", 64, "timeoutSeconds", Math.min(180, this.read_I_6()))
                                 ),
                                 null
                              )
                              : this.init(
                                 cl.Inner_BufferHelper.flush,
                                 aA.init(ag.init_JsonObject_ObjArr("skipVillage", true, "beds", 0, "timeoutSeconds", Math.min(3600, this.read_I_6()))),
                                 null
                              );
                        }
                     } else {
                        if (this.read_Z) {
                           return this.init(AiOperation.Status.FAILED, "Required armor or golden boots were not equipped; the Nether gate remains closed");
                        }

                        this.read_Z = true;
                        return this.init(cl.Inner_BufferHelper.write, w.init(ag.init_JsonObject_ObjArr("what", "armor")), null);
                     }
                  } else {
                     aB.init_V();
                     return this.init(cl.Inner_BufferHelper.open, co.init(ag.init_JsonObject_ObjArr("timeoutSeconds", Math.min(5400, this.read_I_6()))), null);
                  }
               }
            }

            AiOperation.Result read_AiOperation_Result_11() {
               if (cl.init_Map().isEmpty()) {
                  return null;
               }

               if (this.sync_I >= 4) {
                  return this.init(AiOperation.Status.FAILED, "Cooking repeatedly failed; raw food cannot satisfy the safe nutrition reserve");
               }

               AiOperation cooking = O.init_AiOperation_I(Math.min(600, this.read_I_6()));
               return cooking == null ? null : this.init(cl.Inner_BufferHelper.update, cooking, null);
            }

            Map<String, Integer> init_Map_5() {
               return bK.init_Map();
            }

            boolean read_Z_10() {
               return bK.init_Z();
            }

            boolean init(EquipmentSlot slot, String id) {
               ItemStack stack = mc.player.getItemBySlot(slot);
               return !stack.isEmpty() && BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().equals(id);
            }

            int init_I_34() {
               int n = 0;

               for (String id : bx.init_List) {
                  n += e.init_I(id);
               }

               return n;
            }

            int load_I_13() {
               return bK.load_I();
            }

            AiOperation.Result init(cl.Inner_BufferHelper next, AiOperation op, String notBuilt) {
               if (next != this.init_cl_BufferHelper) {
                  if (this.init_cl_BufferHelper != null) {
                     this.sync_Map.merge(this.init_cl_BufferHelper, System.nanoTime() - this.write_J, Long::sum);
                  }

                  this.init_cl_BufferHelper = next;
                  this.write_J = System.nanoTime();
                  cl.init_V_Str_ObjArr("[AI] speedrun phase {}: at {} after {} s", next, mc.player.blockPosition().toShortString(), this.init_J_2());
                  bL.init_V_Str_JsonObject(
                     "speedrun_phase",
                     ag.init_JsonObject_ObjArr(
                        "phase",
                        next.name(),
                        "world",
                        this.init_Str,
                        "dimension",
                        cL.load(mc),
                        "at",
                        mc.player.blockPosition().toShortString(),
                        "elapsedSeconds",
                        this.init_J_2(),
                        "resources",
                        bK.init_JsonObject()
                     )
                  );
               }

               if (notBuilt != null) {
                  return this.init(AiOperation.Status.FAILED, "Reached phase " + next + ", but " + notBuilt);
               }

               this.init_AiOperation = bJ.init_AiOperation_Str("campaign", next.name(), op);
               this.pop_Z = false;
               this.poll_J = System.nanoTime();
               return this.init_AiOperation_Result_13(next + ": starting");
            }

            int read_I_6() {
               return (int)Math.max(60L, (this.read_J - System.nanoTime()) / 1000000000L);
            }

            long init_J_2() {
               return (System.nanoTime() - this.load_J) / 1000000000L;
            }

            JsonObject init_JsonObject_51() {
               JsonObject phases = new JsonObject();
               this.sync_Map.forEach((p, n) -> phases.addProperty(p.name(), n / 1000000000L));
               if (this.init_cl_BufferHelper != null) {
                  phases.addProperty(
                     this.init_cl_BufferHelper.name(),
                     (this.sync_Map.getOrDefault(this.init_cl_BufferHelper, 0L) + System.nanoTime() - this.write_J) / 1000000000L
                  );
               }

               return ag.init_JsonObject_ObjArr(
                  "phase",
                  this.init_cl_BufferHelper == null ? "" : this.init_cl_BufferHelper.name(),
                  "elapsedSeconds",
                  this.init_J_2(),
                  "phases",
                  phases,
                  "steps",
                  this.init_JsonArray,
                  "lastFailure",
                  this.read_Str,
                  "world",
                  this.init_Str,
                  "dimension",
                  cL.load(mc),
                  "simulationTicks",
                  autismclient.util.ck.init_ck().state_I() - this.init_I,
                  "resources",
                  bK.init_JsonObject(),
                  "memoryCapabilities",
                  cM.init_JsonObject(),
                  "requiredNetherRouteVerified",
                  aB.load_Z(),
                  "seedFree",
                  aG.init_Z(),
                  "ironBootstrapTried",
                  this.sync_Z,
                  "pearlRecoveryPending",
                  this.flush_Z,
                  "pearlRecoveryGoal",
                  this.poll_I,
                  "pearlRecoveryReturns",
                  this.open_I,
                  "deathless",
                  mc.player != null && mc.player.isAlive() && ca.load_J() == this.init_J
               );
            }

            void init_V_5(String status) {
               cc service = cc.load_cc();
               if (service != null) {
                  JsonObject snapshot = this.init_JsonObject_51();
                  snapshot.addProperty("status", status);
                  service.load_V_JsonObject(snapshot);
               }
            }

            AiOperation.Result init_AiOperation_Result_13(String message) {
               return R.init_AiOperation_Result(AiOperation.Status.RUNNING, message, this.init_JsonObject_51());
            }

            AiOperation.Result init(AiOperation.Status status, String message) {
               this.get_Z = true;
               W.init(this);
               JsonObject debug = this.init_bJ
                  .init_JsonObject_Str(status.name(), this.init_cl_BufferHelper == null ? "START" : this.init_cl_BufferHelper.name(), message);
               if (this.init_AiOperation != null) {
                  this.init_AiOperation.cancel();
                  this.init_AiOperation = null;
               }

               this.init_Y.init_V();
               if (this.load_AiOperation != null) {
                  this.load_AiOperation.cancel();
                  this.load_AiOperation = null;
               }

               this.init_V_5(status.name());
               JsonObject finalData = this.init_JsonObject_51();
               finalData.add("diagnostics", debug);
               bL.init_V_Str_JsonObject("speedrun_outcome", ag.init_JsonObject_ObjArr("status", status.name(), "message", message, "report", finalData));
               this.load_V_13();
               cl.init_V_Str_ObjArr("[AI] speedrun {} after {} s: {}", status, this.init_J_2(), message);
               return R.init_AiOperation_Result(status, message, finalData);
            }

            void load_V_13() {
               if (!this.write_Z) {
                  this.write_Z = true;
                  aG.init_V_Z(false);
                  bK.init(this.init_Obj);
               }
            }

            @Override
            public boolean canInterruptNow() {
               return (this.init_AiOperation == null || this.init_AiOperation.canInterruptNow())
                  && (this.load_AiOperation == null || this.load_AiOperation.canInterruptNow())
                  && this.init_Y.load();
            }

            @Override
            public boolean requiresReconciliationAfterEmergency() {
               return this.init_AiOperation != null && this.init_AiOperation.requiresReconciliationAfterEmergency()
                  || this.load_AiOperation != null && this.load_AiOperation.requiresReconciliationAfterEmergency()
                  || this.init_Y.read();
            }

            @Override
            public void cancel() {
               W.init(this);
               if (!this.get_Z) {
                  this.get_Z = true;
                  this.init_bJ.init_JsonObject_Str("CANCELLED", this.init_cl_BufferHelper == null ? "START" : this.init_cl_BufferHelper.name(), this.write_Str);
                  if (this.init_AiOperation != null) {
                     this.init_AiOperation.cancel();
                     this.init_AiOperation = null;
                  }

                  this.init_Y.init_V();
                  if (this.load_AiOperation != null) {
                     this.load_AiOperation.cancel();
                     this.load_AiOperation = null;
                  }

                  this.init_V_5("CANCELLED");
                  this.load_V_13();
               }
            }
         };
      } else {
         throw new IllegalArgumentException("A speedrun needs a connected living player");
      }
   }

   static boolean init_Z_Minecraft(Minecraft mc) {
      af.Inner_DataUtil swords = af.init_af_DataUtil("#minecraft:swords");
      af.Inner_DataUtil axes = af.init_af_DataUtil("#minecraft:axes");

      for (int slot = 0; slot < 36; slot++) {
         ItemStack stack = mc.player.getInventory().getItem(slot);
         if ((swords.matches(stack) || axes.matches(stack)) && bK.init(stack, 8)) {
            return true;
         }
      }

      return false;
   }

   static AiOperation init_AiOperation_D(double range, int items, int seconds) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         Entity best = null;

         for (Entity e : mc.level.entitiesForRendering()) {
            if (init_Z_Minecraft(mc, e, range) && (best == null || e.distanceToSqr(mc.player) < best.distanceToSqr(mc.player))) {
               best = e;
            }
         }

         if (best == null) {
            return null;
         }

         String type = BuiltInRegistries.ENTITY_TYPE.getKey(best.getType()).toString();
         String drop = load_Map.get(type);
         int herd = 0;

         for (Entity e : mc.level.entitiesForRendering()) {
            if (e.getType() == best.getType() && init_Z_Minecraft(mc, e, range)) {
               herd++;
            }
         }

         int count = e.init_I(drop) + 1;
         init_V_Str_ObjArr("[AI] speedrun food: {} {} in sight, hunting for {} {}", herd, type, count, drop);
         return aE.init(
            ag.init_JsonObject_ObjArr(
               "item", drop, "count", count, "allowCombat", true, "staged", true, "radius", (int)Math.ceil(range), "timeoutSeconds", seconds
            )
         );
      } else {
         return null;
      }
   }

   private static AiOperation load(int needed, int seconds) {
      if (seconds < 15) {
         return null;
      } else {
         g.Inner_StreamUtil graph = e.init_g_StreamUtil();
         g.Inner_CachePool plan = init_g_CachePool_g_StreamUtil_I(graph, needed);
         if (plan == null) {
            return null;
         } else {
            return plan.requiresTable() && !e.load() && e.init_I("minecraft:crafting_table") == 0 && !g.init(graph, "minecraft:crafting_table", 1).complete()
               ? null
               : e.init(ag.init_JsonObject_ObjArr("item", "minecraft:bread", "count", plan.target(), "timeoutSeconds", seconds));
         }
      }
   }

   private static cl.Inner_StreamUtil init_cl_StreamUtil_Minecraft_Set(Minecraft mc, Set<Long> attempted) {
      cl.Inner_StreamUtil[] nearest = new cl.Inner_StreamUtil[]{null};
      cu.init(
         mc,
         List.of(Blocks.HAY_BLOCK),
         24,
         pos -> {
            if (!attempted.contains(pos.asLong()) && !(pos.distSqr(mc.player.blockPosition()) > 576.0)) {
               for (Direction side : List.of(Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)) {
                  for (int dy = -2; dy <= 1; dy++) {
                     BlockPos stand = pos.relative(side).offset(0, dy, 0);
                     if (Math.abs(stand.getY() - mc.player.getBlockY()) <= 3
                        && mc.level.hasChunkAt(stand)
                        && HashUtil.init_Z(stand)
                        && mc.level.getFluidState(stand).isEmpty()
                        && mc.level.getFluidState(stand.above()).isEmpty()) {
                        BlockState floor = mc.level.getBlockState(stand.below());
                        if (!floor.is(BlockTags.LEAVES) && !floor.is(BlockTags.LOGS)) {
                           nearest[0] = new cl.Inner_StreamUtil(pos.immutable(), stand.immutable());
                           return true;
                        }
                     }
                  }
               }

               return false;
            } else {
               return false;
            }
         }
      );
      return nearest[0];
   }

   static AiOperation init_AiOperation_I_I(final int items, final int seconds) {
      final Minecraft mc = Minecraft.getInstance();
      return new AiOperation() {
         long init_J = System.nanoTime() + seconds * 1000000000L;
         boolean init_Z;
         final BlockPos init_BlockPos = mc.player.blockPosition();
         final Object init_Obj = mc.level;
         final Object load_Obj = mc.getConnection();
         final Object read_Obj = mc.player;
         final Y init_Y = new Y(X.Inner_CachePool.standard(), "food trip");
         final cl.Inner_CachePool init_cl_CachePool = new cl.Inner_CachePool();
         AiOperation.Result init_AiOperation_Result;
         AiOperation init_AiOperation;
         Supplier<AiOperation> init_Supplier;
         String init_Str = "";
         int init_I;
         int load_I;
         int read_I;
         int write;
         int update;
         final Set<Long> init_Set = new HashSet<>();
         cl.Inner_StreamUtil init_cl_StreamUtil;
         boolean load_Z;

         @Override
         public AiOperation.Result tick() {
            if (this.load_Z || mc.player == null || mc.level != this.init_Obj || mc.getConnection() != this.load_Obj || mc.player != this.read_Obj) {
               return this.init(AiOperation.Status.CANCELLED, "Food trip cancelled or world/player changed");
            }

            if (!mc.player.isAlive()) {
               return this.init(AiOperation.Status.FAILED, "Player died during the food trip");
            }

            int have = bK.load_I();
            if (have >= items && this.canInterruptNow() && !this.requiresReconciliationAfterEmergency() && !this.init_Y.init_Z()) {
               return this.init(AiOperation.Status.SUCCEEDED, "Food in the bag: " + have);
            }

            if (System.nanoTime() > this.init_J) {
               return this.init(
                  this.requiresReconciliationAfterEmergency()
                     ? AiOperation.Status.UNCERTAIN
                     : (have > 0 ? AiOperation.Status.SUCCEEDED : AiOperation.Status.FAILED),
                  "Food trip out of time with " + have
               );
            }

            if (!this.init_Str.equals("cook") && !this.init_Y.init_Z() && mc.player.getFoodData().getFoodLevel() <= 16) {
               X.Inner_BufferHelper vitals = X.init_X_BufferHelper();
               int raw = cl.init_Map().values().stream().mapToInt(Integer::intValue).sum();
               boolean quiet = this.init_Y.load()
                  && !this.init_Y.read()
                  && vitals.hostilesNear() == 0
                  && !vitals.underAttack()
                  && mc.player.getHealth() >= 10.0F
                  && !mc.player.isOnFire()
                  && !ak.init_Z(mc)
                  && !EventBus.init(mc.player);
               if (cl.init_Z_I(vitals.food(), bK.read(), raw, this.read_I, this.init_I_35(), quiet, this.init_AiOperation)) {
                  AiOperation cooking = this.init_AiOperation_5();
                  if (cooking != null) {
                     this.init_V_27();
                     this.init_Supplier = this::init_AiOperation_5;
                     this.init_AiOperation = cooking;
                     this.init_Str = "cook";
                     return this.init("Cooking the gathered meat before spending raw rations");
                  }
               }
            }

            AiOperation.Result guarded = this.init_Y.init_AiOperation_Result_AiOperation(this.init_AiOperation, this::init_V_27, this::init_I_35);
            if (guarded != null) {
               this.init_AiOperation_Result = guarded;
               if (!guarded.terminal()) {
                  return this.init(guarded.message());
               }

               JsonObject receipt = this.init_JsonObject_52();
               receipt.add("survival", bK.init_JsonObject(guarded.dataJson()));
               return this.init(guarded.status(), guarded.message(), receipt, guarded);
            } else {
               if (this.init_AiOperation == null && this.init_Supplier != null) {
                  this.init_AiOperation = this.init_Supplier.get();
                  if (this.init_AiOperation != null) {
                     return this.init("Resuming " + this.init_Str + " after survival care");
                  }

                  this.init_Supplier = null;
               }

               if (this.init_AiOperation != null) {
                  long before = System.nanoTime();
                  boolean transferWork = this.init_Str.equals("cook") || this.init_Str.equals("bread") || this.init_Str.equals("hay-mine");
                  AiOperation.Result r = this.init_cl_CachePool.init(this.init_AiOperation, transferWork);
                  if (this.init_Str.equals("surface")) {
                     this.init_J = this.init_J + System.nanoTime() - before + 50000000L;
                  }

                  if (r.status() == AiOperation.Status.UNCERTAIN) {
                     return this.init(AiOperation.Status.UNCERTAIN, this.init_Str + ": " + r.message());
                  }

                  boolean seen = this.init_Str.startsWith("walk")
                     && this.init_AiOperation.canInterruptNow()
                     && !this.init_AiOperation.requiresReconciliationAfterEmergency()
                     && cl.init_Z_Minecraft_D(mc, 48.0);
                  if (!r.terminal() && !seen) {
                     return this.init(this.init_Str + ": " + r.message());
                  }

                  boolean gatherHay = this.init_Str.equals("hay-walk") && r.status() == AiOperation.Status.SUCCEEDED;
                  this.init_cl_CachePool.load(this.init_AiOperation);
                  this.init_AiOperation = null;
                  this.init_Supplier = null;
                  this.init_Str = "";
                  if (gatherHay
                     && this.init_cl_StreamUtil != null
                     && this.init_I_35() >= 20
                     && mc.level.getBlockState(this.init_cl_StreamUtil.block()).is(Blocks.HAY_BLOCK)
                     && mc.player.blockPosition().distSqr(this.init_cl_StreamUtil.stand()) <= 4.0) {
                     int needed = Math.max(1, (items - bK.load_I() + 2) / 3);
                     int target = e.init_I("minecraft:hay_block") + Math.min(4, needed);
                     this.init_Supplier = () -> bJ.init_AiOperation_Str(
                        "food_trip",
                        "gather observed hay",
                        autismclient.ai.a.BufferHelper.init_AiOperation(
                           ag.init_JsonObject_ObjArr(
                              "block",
                              "minecraft:hay_block",
                              "drop",
                              "minecraft:hay_block",
                              "held",
                              target,
                              "radius",
                              16,
                              "explore",
                              false,
                              "timeoutSeconds",
                              Math.min(45, this.init_I_35())
                           )
                        )
                     );
                     this.init_AiOperation = this.init_Supplier.get();
                     this.init_Str = "hay-mine";
                     return this.init("Gathering nearby hay for bread");
                  }
               }

               AiOperation bread = this.load();
               if (bread != null) {
                  this.init_Supplier = this::load;
                  this.init_AiOperation = bread;
                  this.init_Str = "bread";
                  return this.init("Making bread from gathered food");
               }

               if (cl.init_Z_I_I(this.update, this.init_I_35()) && (this.init_cl_StreamUtil = cl.init_cl_StreamUtil_Minecraft_Set(mc, this.init_Set)) != null) {
                  this.update++;
                  this.init_Set.add(this.init_cl_StreamUtil.block().asLong());
                  BlockPos stand = this.init_cl_StreamUtil.stand();
                  this.init_Supplier = () -> bJ.init_AiOperation_Str(
                     "food_trip",
                     "walk to observed hay",
                     HashUtil.init(
                        stand,
                        1.0,
                        false,
                        false,
                        false,
                        Math.min(30, this.init_I_35()),
                        autismclient.ai.a.ClassCache.Inner_IoWorker.load,
                        true,
                        2.0,
                        autismclient.ai.a.ClassCache.Inner_JsonUtil.init_ClassCache_JsonUti_BlockPos_D(stand, 1.0),
                        new HashUtil.Inner_BufferHelper(true, false, 3, true, false, true)
                     )
                  );
                  this.init_AiOperation = this.init_Supplier.get();
                  this.init_Str = "hay-walk";
                  return this.init("Nearby hay can provide bread");
               }

               N.Inner_DataUtil admission = N.init(this.init_J, System.nanoTime(), this.init_Z);
               if (admission.admitted()) {
                  AiOperation hunt = cl.init_AiOperation_D(48.0, Math.max(1, items - have), admission.childSeconds());
                  if (hunt != null) {
                     this.init_J = admission.deadlineNanos();
                     this.init_Z = admission.graceUsed();
                     this.init_AiOperation = bJ.init_AiOperation_Str("food_trip", "hunt " + have + "/" + items, hunt);
                     this.init_Str = "hunt";
                     return this.init("Hunting food");
                  }

                  if (!cl.init_Z_Minecraft_I(mc, 5) && this.load_I < 2) {
                     this.load_I++;
                     this.init_Supplier = () -> bJ.init_AiOperation_Str(
                        "food_trip",
                        "surface for animals",
                        autismclient.ai.a.d.init(ag.init_JsonObject_ObjArr("timeoutSeconds", Math.min(120, this.init_I_35())))
                     );
                     this.init_AiOperation = this.init_Supplier.get();
                     this.init_Str = "surface";
                     return this.init("Up to open sky for food animals");
                  }

                  if (this.init_I >= 6) {
                     return this.init(have > 0 ? AiOperation.Status.SUCCEEDED : AiOperation.Status.FAILED, "No food animal found; " + have + " food held");
                  }

                  long dayTime = mc.level.getOverworldClockTime() % 24000L;
                  if (mc.level.dimension() == Level.OVERWORLD && dayTime > 13000L && dayTime < 23000L) {
                     return this.init(
                        have > 0 ? AiOperation.Status.SUCCEEDED : AiOperation.Status.FAILED, "Night: no walk out for food; " + have + " food held"
                     );
                  }

                  double a = this.init_I++ * 2.399;
                  int x = this.init_BlockPos.getX() + (int)Math.round(Math.cos(a) * 64.0);
                  int z = this.init_BlockPos.getZ() + (int)Math.round(Math.sin(a) * 64.0);
                  BlockPos surface = cl.init_BlockPos_BlockPos(mc.player.blockPosition(), x, z, column -> cl.init_BlockPos_Minecraft_BlockPos(mc, column));
                  if (surface == null) {
                     return this.init("No observed safe surface in this direction; trying another bearing");
                  }

                  this.init_Supplier = () -> bJ.init_AiOperation_Str(
                     "food_trip",
                     "explore " + x + "," + z,
                     HashUtil.init(
                        surface,
                        2.0,
                        false,
                        false,
                        false,
                        Math.min(60, this.init_I_35()),
                        autismclient.ai.a.ClassCache.Inner_IoWorker.load,
                        true,
                        2.0,
                        cl.init_ClassCache_JsonUti_BlockPos(surface),
                        HashUtil.Inner_BufferHelper.DEFAULT
                     )
                  );
                  this.init_Str = "walk";
                  this.init_AiOperation = this.init_Supplier.get();
                  return this.init("Looking for food animals");
               } else {
                  return !N.init(this.canInterruptNow() && autismclient.ai.a.RefQueue.flush_Z(), this.requiresReconciliationAfterEmergency())
                     ? this.init("Waiting for the food trip's safe handoff")
                     : this.init(
                        have > 0 ? AiOperation.Status.SUCCEEDED : AiOperation.Status.FAILED, "No time for another complete hunt; " + have + " food held"
                     );
               }
            }
         }

         AiOperation.Result init(String message) {
            return R.init_AiOperation_Result(AiOperation.Status.RUNNING, message, this.init_JsonObject_52());
         }

         JsonObject init_JsonObject_52() {
            return ag.init_JsonObject_ObjArr(
               "foodHeld",
               bK.load_I(),
               "target",
               items,
               "leg",
               this.init_Str,
               "explorationLegs",
               this.init_I,
               "surfaceAttempts",
               this.load_I,
               "cookingSteps",
               this.read_I,
               "breadSteps",
               this.write,
               "hayTrips",
               this.update,
               "survivalActive",
               this.init_Y.init_Z()
            );
         }

         int init_I_35() {
            return (int)Math.max(1L, (this.init_J - System.nanoTime()) / 1000000000L);
         }

         AiOperation init_AiOperation_5() {
            if (this.read_I < 6 && this.init_I_35() >= 60) {
               AiOperation cooking = O.init_AiOperation_I(Math.min(120, this.init_I_35()));
               if (cooking == null) {
                  return null;
               }

               this.read_I++;
               return bJ.init_AiOperation_Str("food_trip", "cook gathered meat", cooking);
            } else {
               return null;
            }
         }

         AiOperation load() {
            if (this.write >= 2) {
               return null;
            }

            AiOperation bread = cl.load(Math.max(1, items - bK.load_I()), Math.min(45, this.init_I_35()));
            if (bread == null) {
               return null;
            }

            this.write++;
            return bJ.init_AiOperation_Str("food_trip", "bread from held wheat or hay", bread);
         }

         void init_V_27() {
            if (this.init_AiOperation != null) {
               this.init_cl_CachePool.load(this.init_AiOperation);
               this.init_AiOperation = null;
            }
         }

         AiOperation.Result init(AiOperation.Status status, String message) {
            return this.init(status, message, this.init_JsonObject_52(), null);
         }

         AiOperation.Result init(AiOperation.Status status, String message, JsonObject receipt, AiOperation.Result actual) {
            this.init_cl_CachePool.init_V_AiOperation(this.init_AiOperation);
            this.init_cl_CachePool.init(this.init_Y.read(), this.init_AiOperation_Result);
            AiOperation.Result result = this.init_cl_CachePool.init(status, message, receipt, actual);
            this.cancel();
            cl.init_V_Str_ObjArr("[AI] speedrun food trip {}: {}", result.status(), result.message());
            return result;
         }

         @Override
         public boolean canInterruptNow() {
            return !this.init_cl_CachePool.init() && this.init_Y.load() && (this.init_AiOperation == null || this.init_AiOperation.canInterruptNow());
         }

         @Override
         public boolean requiresReconciliationAfterEmergency() {
            return this.init_cl_CachePool.init()
               || this.init_Y.read()
               || this.init_AiOperation != null && this.init_AiOperation.requiresReconciliationAfterEmergency();
         }

         @Override
         public void cancel() {
            this.init_cl_CachePool.init_V_AiOperation(this.init_AiOperation);
            this.init_cl_CachePool.init(this.init_Y.read(), this.init_AiOperation_Result);
            this.load_Z = true;
            this.init_Y.init_V();
            this.init_V_27();
            this.init_Supplier = null;
         }
      };
   }

   static boolean init_Z_Minecraft(Minecraft mc, Entity e, double range) {
      if (mc.player != null && e != null && e.isAlive() && !(e.distanceToSqr(mc.player) > range * range)) {
         String id = BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).toString();
         return load_Map.containsKey(id) && !(e instanceof AgeableMob mob && mob.isBaby()) && ValueSink.init_Z_Minecraft_Entity(mc, e);
      } else {
         return false;
      }
   }

   static boolean init_Z_Minecraft_D(Minecraft mc, double range) {
      if (mc.player != null && mc.level != null) {
         for (Entity e : mc.level.entitiesForRendering()) {
            if (init_Z_Minecraft(mc, e, range)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   static boolean init_Z_Minecraft_I(Minecraft mc, int radius) {
      BlockPos feet = mc.player.blockPosition();

      for (int dx = -radius; dx <= radius; dx++) {
         for (int dz = -radius; dz <= radius; dz++) {
            if (mc.level.canSeeSky(feet.offset(dx, 1, dz))) {
               return true;
            }
         }
      }

      return false;
   }

   static String init_Str_I(int items) {
      long needed = items * 200L;

      for (String coal : List.of("minecraft:coal", "minecraft:charcoal")) {
         if ((e.init_I(coal) - bK.init_I(coal)) * 1600L >= needed) {
            return coal;
         }
      }

      for (Item plank : af.load_af_DataUtil("planks").items()) {
         String id = BuiltInRegistries.ITEM.getKey(plank).toString();
         if ((e.init_I(id) - bK.init_I(id)) * 300L >= needed) {
            return id;
         }
      }

      return null;
   }

   static Map<String, Integer> init_Map() {
      Map<String, Integer> out = new LinkedHashMap<>();

      for (String raw : read_Map.keySet()) {
         int n = e.init_I(raw);
         if (n > 0) {
            out.put(raw, n);
         }
      }

      return out;
   }

   static void init_V_Str_ObjArr(String format, Object... args) {
      AutismClientAddon.LOG.info(format, args);
   }

   static String init_Str_Str(String s) {
      return s == null ? "" : (s.length() > 300 ? s.substring(0, 300) + "..." : s);
   }

   enum Inner_BufferHelper {
      init,
      load,
      read,
      write,
      update,
      sync,
      flush,
      close,
      open,
      poll;
   }

   static final class Inner_CachePool {
      private AiOperation init_AiOperation;
      private AiOperation.Result init_AiOperation_Result;
      private AiOperation.Result load;
      private boolean init_Z;

      AiOperation.Result init(AiOperation child, boolean transfer) {
         boolean before = transfer && child.requiresReconciliationAfterEmergency();
         AiOperation.Result result = child.tick();
         boolean after = child.requiresReconciliationAfterEmergency();
         if (result.terminal()) {
            result = Y.init_AiOperation_Result_Z_AiOperation_Result(
               transfer && (after || result.status() != AiOperation.Status.SUCCEEDED && before) || init_Z_AiOperation_Result(result), result
            );
         }

         this.init_AiOperation = child;
         this.init_AiOperation_Result = result;
         if (result.terminal() && init_Z_AiOperation_Result(result)) {
            this.init(true, result);
         }

         return result;
      }

      void init_V_AiOperation(AiOperation child) {
         if (child != null) {
            AiOperation.Result actual = this.init_AiOperation == child ? this.init_AiOperation_Result : null;
            this.init(child.requiresReconciliationAfterEmergency(), actual);
            if (this.init_AiOperation != child) {
               this.init_AiOperation = child;
               this.init_AiOperation_Result = null;
            }
         }
      }

      void init(boolean needsObservation, AiOperation.Result actual) {
         if (needsObservation || init_Z_AiOperation_Result(actual)) {
            this.init_Z = true;
            if (actual != null && (this.load == null || actual == this.init_AiOperation_Result)) {
               this.load = actual;
            }
         }
      }

      void load(AiOperation child) {
         if (child != null) {
            this.init_V_AiOperation(child);
            child.cancel();
         }
      }

      boolean init() {
         return this.init_Z;
      }

      AiOperation.Result init(AiOperation.Status status, String message, JsonObject phaseData, AiOperation.Result actual) {
         JsonObject data = phaseData.deepCopy();
         if (this.load != null) {
            data.add("pendingChild", bK.load_JsonObject(this.load.dataJson()));
         }

         if (actual == null) {
            actual = this.init_AiOperation_Result != null ? this.init_AiOperation_Result : this.load;
         }

         AiOperation.Result result = aB.init_AiOperation_Result_AiOperation_Status(status, message, data, this.init_Z, actual);
         return actual == null ? result : new AiOperation.Result(result.status(), result.message(), result.dataJson(), actual.evidence(), actual.failure());
      }

      private static boolean init_Z_AiOperation_Result(AiOperation.Result result) {
         if (result == null) {
            return false;
         }

         JsonObject data = bK.init_JsonObject(result.dataJson());
         return ag.init_Z_JsonObject(data, "interruptedActionNeedsReconciliation", false) || ag.init_Z_JsonObject(data, "reobserveBeforeResuming", false);
      }
   }

   static final class Inner_DataUtil {
      private boolean init;

      AiOperation init(int held, int secondsLeft, IntFunction<AiOperation> observed) {
         if (!this.init && held <= 0 && secondsLeft > 0) {
            AiOperation operation = observed.apply(Math.min(90, secondsLeft));
            if (operation != null) {
               this.init = true;
            }

            return operation;
         } else {
            return null;
         }
      }
   }

   record Inner_StreamUtil(BlockPos block, BlockPos stand) {
   }
}
