package autismclient.ai;

import autismclient.ai.a.HashUtil;
import autismclient.api.ai.AiOperation;
import autismclient.compat263.Compat263;
import autismclient.mixin.accessor.AutismClientLevelPredictionAccessor;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.Plane;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.EyeOfEnder;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.shapes.VoxelShape;

final class co {
   static final long init_J = 180000000000L;
   static final int init_I = 40;
   static final long load_J = 480000000000L;
   static final int load_I = 160;
   static final int read_I = 640;
   static final int write_I = 12;
   static final long read_J = 600000000000L;
   static final float[] init_FArr = new float[]{-25.0F, -45.0F, -65.0F, -85.0F};
   static final int update = 120;
   private static String init_Str = "";
   private static Object init_Obj;
   private static z init_z;
   private static Vec3 init_Vec3;
   private static int sync;
   private static long write_J;
   private static BlockPos init_BlockPos;

   private co() {
   }

   static AiOperation.Result init(AiOperation.Status status, String message, JsonObject data, boolean pending, AiOperation.Result child) {
      JsonObject evidence = data.deepCopy();
      if (child != null) {
         JsonObject receipt = bK.init_JsonObject(child.dataJson());
         evidence.add("terminalChild", bK.load_JsonObject(child.dataJson()));
         evidence.addProperty("terminalChildStatus", child.status().name());
         pending |= ag.init_Z_JsonObject(receipt, "interruptedActionNeedsReconciliation", false)
            || ag.init_Z_JsonObject(receipt, "reobserveBeforeResuming", false);
      }

      return Y.init_AiOperation_Result_Z_AiOperation_Result(pending, R.init_AiOperation_Result(status, message, evidence));
   }

   private static z init(Minecraft mc) {
      String world = cL.init(mc);
      if (init_z == null || !world.equals(init_Str) || init_Obj != mc.getConnection()) {
         init_Str = world;
         init_Obj = mc.getConnection();
         init_z = new z();
         init_Vec3 = null;
         sync = 0;
         write_J = 0L;
         init_BlockPos = null;
      }

      return init_z;
   }

   static AiOperation init(JsonObject args) {
      final Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         final int timeoutSeconds = Math.max(120, Math.min(5400, ag.init_I_JsonObject(args, "timeoutSeconds", 2400)));
         return new AiOperation() {
            final long init_J = System.nanoTime() + timeoutSeconds * 1000000000L;
            final JsonArray init_JsonArray = new JsonArray();
            final autismclient.ai.a.DataUtil init_DataUtil = new autismclient.ai.a.DataUtil();
            final z init_z = co.init(mc);
            final cp.Inner_DataUtil init_cp_DataUtil = new cp.Inner_DataUtil();
            final String init_Str = cL.init(mc);
            final Object init_Obj = mc.getConnection();
            final UUID init_UUID = mc.player.getUUID();
            final long load_J = ca.load_J();
            BlockPos init_BlockPos;
            JsonObject init_JsonObject;
            int init_I;
            boolean init_Z;
            final Set<UUID> init_Set = new HashSet<>();
            final Set<Integer> load_Set = new HashSet<>();
            Vec3 init_Vec3;
            boolean load_Z;
            int load_I;
            int read_I;
            AiOperation init_AiOperation;
            final co.Inner_DataUtil init_co_DataUtil = new co.Inner_DataUtil();
            AiOperation.Result init_AiOperation_Result;
            String load_Str = "";
            Vec3 load_Vec3;
            Vec3 read_Vec3;
            EyeOfEnder init_EyeOfEnder;
            co.Inner_StreamUtil init_co_StreamUtil = new co.Inner_StreamUtil();
            Vec3 write_Vec3;
            z.Inner_StreamUtil init_z_StreamUtil;
            int write_I;
            int update_I;
            int sync_I;
            int flush_I;
            int close;
            int open;
            int poll;
            int push;
            boolean read_Z;
            long read_J;
            long write_J;
            long update_J;
            BlockPos load_BlockPos;
            final co.Inner_CachePool init_co_CachePool = new co.Inner_CachePool();
            boolean write_Z;
            BlockPos read_BlockPos;
            long sync_J;
            int pop;
            int tick;
            int run;
            int get;
            int set;
            boolean update_Z;
            int put;
            boolean sync_Z;
            boolean flush_Z;
            int add;
            final AiOperation load_AiOperation;
            int clear;
            int reset;
            int start;
            BlockPos write_BlockPos;
            int stop;
            int next;
            int prev;
            final Y init_Y;
            long flush_J;

            {
               this.load_Vec3 = co.init_Vec3;
               this.read_Vec3 = this.load_Vec3 == null ? null : mc.player.position();
               this.sync_I = co.sync;
               this.read_J = co.write_J;
               this.load_BlockPos = co.init_BlockPos;
               this.load_AiOperation = new AiOperation() {
                  @Override
                  public AiOperation.Result tick() {
                     return read();
                  }

                  @Override
                  public boolean canInterruptNow() {
                     return !sync_Z && !init_co_StreamUtil.init_Z();
                  }

                  @Override
                  public boolean requiresReconciliationAfterEmergency() {
                     return init_co_StreamUtil.init_Z();
                  }

                  @Override
                  public void cancel() {
                     sync_Z = false;
                     init_DataUtil.init_V();
                  }
               };
               this.init_Y = new Y(
                  X.Inner_CachePool.standard()
                     .keeping(List.of("minecraft:blaze_rod", "minecraft:blaze_powder", "minecraft:ender_pearl", "minecraft:ender_eye")),
                  "stronghold"
               );
            }

            Vec3 init_Vec3_2(BlockPos frame) {
               return Vec3.atLowerCornerOf(frame).add(0.5, 0.81, 0.5);
            }

            boolean init(Vec3 eye, BlockPos frame) {
               return t.init(mc, eye, frame, mc.player.blockInteractionRange() - 0.3) != null;
            }

            BlockPos init_BlockPos_6(BlockPos frame) {
               BlockPos best = null;
               double bestD = 1.7976931348623157E308;

               for (BlockPos p : BlockPos.betweenClosed(frame.offset(-3, -1, -3), frame.offset(3, 2, 3))) {
                  if (cu.init(mc.level, p) && cu.init(mc.level, p.below())) {
                     VoxelShape floor = mc.level.getBlockState(p.below()).getCollisionShape(mc.level, p.below());
                     if (!floor.isEmpty()
                        && mc.level.getBlockState(p).getCollisionShape(mc.level, p).isEmpty()
                        && mc.level.getBlockState(p.above()).getCollisionShape(mc.level, p.above()).isEmpty()
                        && mc.level.getFluidState(p).isEmpty()
                        && mc.level.getFluidState(p.above()).isEmpty()
                        && !mc.level.getBlockState(p).is(Blocks.END_PORTAL)) {
                        Vec3 eye = new Vec3(p.getX() + 0.5, p.getY() - 1 + floor.max(Axis.Y) + 1.62, p.getZ() + 0.5);
                        if (this.init(eye, frame)) {
                           double d = p.distToCenterSqr(mc.player.position());
                           if (d < bestD) {
                              bestD = d;
                              best = p.immutable();
                           }
                        }
                     }
                  }
               }

               return best;
            }

            BlockPos load(BlockPos portal) {
               BlockPos best = null;
               double bestD = 1.7976931348623157E308;

               for (BlockPos p : BlockPos.betweenClosed(portal.offset(-3, 0, -3), portal.offset(3, 0, 3))) {
                  if (mc.level.getBlockState(p).is(Blocks.END_PORTAL)) {
                     for (Direction dir : Plane.HORIZONTAL) {
                        BlockPos frame = p.relative(dir);
                        if (mc.level.getBlockState(frame).is(Blocks.END_PORTAL_FRAME)) {
                           BlockPos s = frame.relative(dir);
                           if (!mc.level.getBlockState(s.below()).getCollisionShape(mc.level, s.below()).isEmpty()
                              && mc.level.getBlockState(s).getCollisionShape(mc.level, s).isEmpty()
                              && mc.level.getBlockState(s.above()).getCollisionShape(mc.level, s.above()).isEmpty()) {
                              double d = mc.player.distanceToSqr(s.getX() + 0.5, s.getY(), s.getZ() + 0.5);
                              if (d < bestD) {
                                 bestD = d;
                                 best = s.immutable();
                              }
                           }
                        }
                     }
                  }
               }

               return best;
            }

            BlockPos read(BlockPos stance) {
               BlockPos best = null;
               double bestD = 1.7976931348623157E308;

               for (BlockPos p : BlockPos.betweenClosed(stance.offset(-2, 0, -2), stance.offset(2, 0, 2))) {
                  if (mc.level.getBlockState(p).is(Blocks.END_PORTAL)) {
                     double d = p.distSqr(stance);
                     if (d < bestD) {
                        bestD = d;
                        best = p.immutable();
                     }
                  }
               }

               return best == null ? stance : best;
            }

            boolean init_Z_2(int blocks) {
               int top = mc.level.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, mc.player.getBlockX(), mc.player.getBlockZ());
               return top - mc.player.getBlockY() <= blocks;
            }

            @Override
            public AiOperation.Result tick() {
               if (mc.player != null && mc.level != null) {
                  if (!this.init_Str.equals(cL.init(mc)) || this.init_Obj != mc.getConnection()) {
                     return this.init(AiOperation.Status.CANCELLED, "World or connection changed during stronghold navigation");
                  }

                  if (!mc.player.getUUID().equals(this.init_UUID)) {
                     return this.init(AiOperation.Status.UNCERTAIN, "Player changed during stronghold navigation");
                  }

                  if (mc.player.isAlive() && this.load_J == ca.load_J()) {
                     long timeOfDay = Math.floorMod(mc.level.getOverworldClockTime(), 24000);
                     this.init_co_CachePool.init(System.nanoTime(), this.write_Z && mc.level.dimension() == Level.OVERWORLD && timeOfDay >= 12500L);
                     if (mc.level.dimension() == Level.END) {
                        this.init_V_28();
                        this.init_DataUtil.init_V();
                        if (this.init_BlockPos == null || !this.init_Z) {
                           return this.init(AiOperation.Status.UNCERTAIN, "Entered the End outside a ready, verified portal traversal");
                        }

                        if (++this.init_I < 20) {
                           return this.load("Verifying loaded, supported End arrival");
                        }

                        String hazard = aY.init_Str();
                        aY.init("minecraft:overworld", this.init_BlockPos, "minecraft:the_end", mc.player.blockPosition(), hazard == null, this.init_JsonObject);
                        return this.init(
                           hazard == null ? AiOperation.Status.SUCCEEDED : AiOperation.Status.UNCERTAIN,
                           hazard == null ? "Verified End arrival after " + this.sync_I + " throws" : "End arrival requires recovery: " + hazard
                        );
                     } else {
                        if (mc.level.dimension() != Level.OVERWORLD) {
                           return this.init(AiOperation.Status.UNCERTAIN, "Stronghold navigation requires the Overworld; unexpected dimension observed");
                        }

                        if (System.nanoTime() > this.init_J) {
                           return this.init(
                              this.canInterruptNow() ? AiOperation.Status.TIMED_OUT : AiOperation.Status.UNCERTAIN, "Out of time finding the stronghold"
                           );
                        }

                        AiOperation.Result g = this.init_Y
                           .init_AiOperation_Result_AiOperation(this.sync_Z ? this.load_AiOperation : this.init_AiOperation, () -> {
                              if (this.sync_Z) {
                                 this.load_AiOperation.cancel();
                              } else {
                                 this.init_V_28();
                              }
                           }, () -> (int)Math.max(10L, (this.init_J - System.nanoTime()) / 1000000000L));
                        if (g != null) {
                           this.init_AiOperation_Result = g;
                           if (this.sync_Z) {
                              this.sync_Z = false;
                              this.init_DataUtil.init_V();
                           }

                           return g.terminal() ? this.init(g.status(), g.message(), g) : g;
                        } else {
                           if (this.init_co_DataUtil.init()) {
                              return this.init(AiOperation.Status.UNCERTAIN, "Interrupted Stronghold action needs observation before resuming");
                           }

                           if (this.sync_Z) {
                              return this.read();
                           }

                           if (this.init_AiOperation != null) {
                              AiOperation.Result r = this.init_co_DataUtil.init_AiOperation_Result(this.init_AiOperation);
                              if (!r.terminal()) {
                                 return this.load(this.load_Str + ": " + r.message());
                              }

                              r = Y.init_AiOperation_Result_Z_AiOperation_Result(this.init_AiOperation.requiresReconciliationAfterEmergency(), r);
                              this.init_V_28();
                              cl.init_V_Str_ObjArr("[AI] stronghold step {}: {} - {}", r.status(), this.load_Str, cl.init_Str_Str(r.message()));
                              this.init_JsonArray
                                 .add(ag.init_JsonObject_ObjArr("step", this.load_Str, "status", r.status().name(), "message", cl.init_Str_Str(r.message())));
                              if (r.status() == AiOperation.Status.CANCELLED) {
                                 return this.init(AiOperation.Status.CANCELLED, "Cancelled at " + this.load_Str, r);
                              }

                              if (r.status() == AiOperation.Status.UNCERTAIN) {
                                 return this.init(AiOperation.Status.UNCERTAIN, this.load_Str + ": " + r.message(), r);
                              }

                              if (this.load_Str.startsWith("forage") && r.status() != AiOperation.Status.SUCCEEDED && ++this.close >= 4) {
                                 this.sync_J = System.nanoTime() + 180000000000L;
                              }

                              if (this.load_Str.startsWith("walk to the portal frame")) {
                                 this.push = r.status() == AiOperation.Status.SUCCEEDED ? 0 : this.push + 1;
                              }

                              if (this.load_Str.startsWith("follow the bearing") && r.status() != AiOperation.Status.SUCCEEDED) {
                                 this.read_Z = true;
                              }

                              if (this.load_Str.startsWith("forage the End kit")
                                 && r.status() != AiOperation.Status.SUCCEEDED
                                 && System.nanoTime() - this.update_J < 1000000000L) {
                                 this.write_J = System.nanoTime() + 5000000000L;
                              }

                              if (this.load_Str.startsWith("hunt") && r.status() != AiOperation.Status.SUCCEEDED) {
                                 this.open++;
                              }

                              if (this.load_Str.startsWith("up to the sky") && r.status() != AiOperation.Status.SUCCEEDED) {
                                 this.reset++;
                              }

                              if (this.load_Str.startsWith("hunt") && r.status() == AiOperation.Status.SUCCEEDED) {
                                 this.open = 0;
                              }

                              if (this.load_Str.equals("secure travel food")) {
                                 this.add++;
                              }

                              boolean eyeSent = ag.init_Z_JsonObject(bK.init_JsonObject(r.dataJson()), "sent", true);
                              if (this.load_Str.startsWith("put an eye") && r.status() == AiOperation.Status.SUCCEEDED) {
                                 this.get = 0;
                                 this.set = 0;
                              }

                              if (this.load_Str.startsWith("put an eye") && r.status() == AiOperation.Status.FAILED && !eyeSent && ++this.set >= 12) {
                                 return this.init(AiOperation.Status.FAILED, "No stance found that sees the frame's top; twelve clicks met another block");
                              }

                              if (this.load_Str.startsWith("put an eye")
                                 && r.status() != AiOperation.Status.SUCCEEDED
                                 && (eyeSent || r.status() == AiOperation.Status.UNCERTAIN)
                                 && ++this.load_I >= 3) {
                                 return this.init(AiOperation.Status.UNCERTAIN, "Three frame insertions lacked confirmation; remaining Eyes are protected");
                              }

                              if (this.load_Str.startsWith("put an eye") && r.status() == AiOperation.Status.UNCERTAIN) {
                                 return this.init(
                                    AiOperation.Status.UNCERTAIN,
                                    "Frame insertion lacked inventory confirmation; stop and reobserve before spending another Eye"
                                 );
                              }
                           }

                           return !mc.player.onGround() && !mc.player.isInWater() && !mc.player.onClimbable()
                              ? this.load("Landing")
                              : this.init_AiOperation_Result_35();
                        }
                     }
                  } else {
                     return this.init(AiOperation.Status.FAILED, "Player died during stronghold navigation");
                  }
               } else {
                  return this.init(AiOperation.Status.CANCELLED, "Left the world");
               }
            }

            AiOperation.Result init_AiOperation_Result_35() {
               if (bK.load_Z() && bK.read() < 24) {
                  AiOperation cooking = O.init_AiOperation_I(180);
                  if (cooking != null) {
                     return this.init("cook safe travel food", cooking);
                  }

                  if (!this.load_Z_2(5)) {
                     return this.init("up to the surface for travel food", autismclient.ai.a.d.init(ag.init_JsonObject_ObjArr("timeoutSeconds", 120)));
                  }

                  long dayTime = mc.level.getOverworldClockTime() % 24000L;
                  if (dayTime > 13000L && dayTime < 23000L && !cl.init_Z_Minecraft_D(mc, 48.0)) {
                     return this.init(
                        "shelter until daylight for travel food",
                        RingBuffer.init(
                           ag.init_JsonObject_ObjArr("timeoutSeconds", Math.max(60L, Math.min(600L, (this.init_J - System.nanoTime()) / 1000000000L)))
                        )
                     );
                  }

                  if (this.add >= 3) {
                     return this.init(AiOperation.Status.FAILED, "Travel food reserve could not be restored after three supply trips");
                  }

                  int goal = bK.load_I() + Math.max(4, (32 - bK.read() + 5) / 6);
                  return this.init("secure travel food", cl.init_AiOperation_I_I(goal, 120));
               } else {
                  this.add = 0;
                  if (this.prev < 2 && !cO.init_List_Str_D("minecraft:silverfish", 12.0).isEmpty()) {
                     BlockPos spawner = cO.init_List_Str_I("minecraft:silverfish", 2)
                        .stream()
                        .filter(p -> p.distSqr(mc.player.blockPosition()) <= 144.0)
                        .findFirst()
                        .orElse(null);
                     if (spawner != null) {
                        this.prev++;
                        return this.init(
                           "disable the threatening silverfish spawner",
                           R.init(
                              "break",
                              ag.init_JsonObject_ObjArr(
                                 "x", spawner.getX(), "y", spawner.getY(), "z", spawner.getZ(), "requireDrops", false, "timeoutSeconds", 40
                              )
                           )
                        );
                     }
                  }

                  BlockPos portal = cO.init_BlockPos_Predicate_I(s -> s.is(Blocks.END_PORTAL), 4);
                  if (portal != null) {
                     t.Inner_StreamUtil verified = t.init_t_StreamUtil(portal);
                     if (verified == null) {
                        if (portal.distSqr(mc.player.blockPosition()) > 36.0) {
                           return this.init("inspect the portal without entering", HashUtil.init_AiOperation_BlockPos(portal, 3.0, true, true, true, 90));
                        }

                        return this.init(AiOperation.Status.UNCERTAIN, "End portal lacks a fully loaded inward-facing ring and nine active cells");
                     }

                     this.init_BlockPos = portal.immutable();
                     this.init_JsonObject = ag.init_JsonObject_ObjArr(
                        "kind",
                        "end_portal",
                        "world",
                        this.init_Str,
                        "dimension",
                        "minecraft:overworld",
                        "center",
                        verified.center().toShortString(),
                        "inwardFrames",
                        12,
                        "filledFrames",
                        12,
                        "activeInteriorCells",
                        9,
                        "observedTick",
                        cv.init_J(),
                        "geometryVerified",
                        true
                     );
                  }

                  long nowKit = System.nanoTime();
                  boolean kitSearching = this.read_J != 0L && nowKit - this.read_J < 480000000000L;
                  JsonObject deficits = bK.init(bK.load_Z());
                  if ((portal != null || kitSearching) && !deficits.isEmpty() && mc.level.dimension() == Level.OVERWORLD) {
                     long now = nowKit;
                     if (this.read_J == 0L || this.load_BlockPos == null) {
                        this.read_J = now;
                        this.load_BlockPos = portal != null ? portal : mc.player.blockPosition();
                        co.write_J = this.read_J;
                        co.init_BlockPos = this.load_BlockPos;
                     }

                     boolean searchTime = now - this.read_J < 480000000000L;
                     JsonObject hard = deficits.deepCopy();
                     hard.remove("fightSupplies");
                     hard.remove("crystalFallback");
                     if (searchTime || !hard.isEmpty() && this.put++ < 10) {
                        if (bK.load_Z() && !bK.init_Map().isEmpty()) {
                           Map<String, Integer> missing = bK.init_Map();
                           int pickGoal = missing.getOrDefault("minecraft:diamond_pickaxe", 1);
                           missing.remove("minecraft:diamond_pickaxe");
                           return this.init(
                              "replace worn End equipment",
                              bx.init_AiOperation_JsonObject(
                                 ag.init_JsonObject_ObjArr("kit", ag.init.toJsonTree(missing), "kitPlanks", 8, "timeoutSeconds", 300),
                                 "minecraft:diamond_pickaxe",
                                 pickGoal
                              )
                           );
                        }

                        if (bK.load_Z() && !bK.init_Z()) {
                           return this.init("equip End armor", w.init(ag.init_JsonObject_ObjArr("what", "armor")));
                        }

                        if (mc.player.getFoodData().getFoodLevel() < 18 && cq.init(mc.player.getInventory()) >= 0) {
                           return this.init("eat before entering the End", cq.init(ag.init_JsonObject_ObjArr("food", 20, "timeoutSeconds", 20)));
                        }

                        if (mc.player.getHealth() < 16.0F && mc.player.getFoodData().getFoodLevel() >= 18) {
                           return this.init("heal before entering the End", d.init(ag.init_JsonObject_ObjArr("healTo", 18, "timeoutSeconds", 90)));
                        }

                        if (bK.read() < 32) {
                           AiOperation cooking = O.init_AiOperation_I(180);
                           if (cooking != null) {
                              return this.init("cook safe End food", cooking);
                           }

                           int foodGoal = bK.load_I() + Math.max(4, (32 - bK.read() + 5) / 6);
                           return this.init("secure End food", cl.init_AiOperation_I_I(foodGoal, 120));
                        }

                        if (bK.update() < 32) {
                           return this.init(
                              "secure End building blocks",
                              aE.init(ag.init_JsonObject_ObjArr("item", "minecraft:cobblestone", "count", 32, "timeoutSeconds", 120))
                           );
                        }

                        if (deficits.has("fallbackWeapon")) {
                           return this.init(
                              "secure a fallback weapon", aE.init(ag.init_JsonObject_ObjArr("item", "minecraft:iron_sword", "count", 1, "timeoutSeconds", 120))
                           );
                        }

                        if (deficits.has("waterBuckets")) {
                           return this.init("refill both End water buckets", aD.init(2, 120));
                        }
                     }

                     if (searchTime) {
                        AiOperation errand = now < this.write_J ? null : O.init_AiOperation_D(48.0);
                        if (errand != null) {
                           return this.init("forage the End kit before the portal (" + O.init_Str() + ")", errand);
                        }

                        if (!this.load_Z_2(5)) {
                           return this.init("up to the surface for the End kit", autismclient.ai.a.d.init(ag.init_JsonObject_ObjArr("timeoutSeconds", 120)));
                        }

                        double a = this.poll++ * 2.399;
                        int x = this.load_BlockPos.getX() + (int)Math.round(Math.cos(a) * 72.0);
                        int z = this.load_BlockPos.getZ() + (int)Math.round(Math.sin(a) * 72.0);
                        return this.init(
                           "look for the End kit round the portal (" + O.init_Str() + ", leg " + this.poll + ")",
                           HashUtil.init(
                              new BlockPos(x, mc.player.getBlockY(), z),
                              4.0,
                              true,
                              true,
                              true,
                              60,
                              autismclient.ai.a.ClassCache.Inner_IoWorker.load,
                              true,
                              2.0,
                              new aB.Inner_NetHelper(x, z, 6),
                              HashUtil.Inner_BufferHelper.DEFAULT
                           )
                        );
                     }

                     if (!hard.isEmpty()) {
                        return this.init(AiOperation.Status.FAILED, "End entry blocked by unresolved supplies: " + hard);
                     }

                     if (!this.update_Z) {
                        this.update_Z = true;
                        cl.init_V_Str_ObjArr("[AI] stronghold: End kit search over ({}); entering with melee and towers", O.init_Str());
                     }
                  }

                  boolean portalNear = portal != null
                     && Math.hypot(mc.player.getX() - (portal.getX() + 0.5), mc.player.getZ() - (portal.getZ() + 0.5)) <= 6.0
                     && Math.abs(mc.player.getY() - portal.getY()) <= 4.0;
                  if (portal != null && !portalNear) {
                     this.tick = 0;
                  }

                  if (portalNear) {
                     t.Inner_StreamUtil openRing = t.init_t_StreamUtil(portal);
                     if (openRing == null
                        || !openRing.active()
                        || Math.abs(openRing.center().getX() - portal.getX()) > 1
                        || Math.abs(openRing.center().getZ() - portal.getZ()) > 1
                        || openRing.center().getY() != portal.getY()) {
                        return this.init(
                           AiOperation.Status.UNCERTAIN, "End entry requires a complete inward-facing frame ring and its nine active interior cells"
                        );
                     }
                  }

                  if (portal != null && portalNear && this.tick++ >= 1) {
                     BlockPos stance = this.load(portal);
                     if (stance == null
                        || this.run >= 3
                        || !(Math.hypot(mc.player.getX() - (stance.getX() + 0.5), mc.player.getZ() - (stance.getZ() + 0.5)) > 0.8)
                           && !(Math.abs(mc.player.getY() - stance.getY()) > 0.6)) {
                        BlockPos into = stance == null ? portal : this.read(stance);
                        return this.init("onto the end portal at " + into.toShortString(), u.init(mc, into));
                     } else {
                        this.run++;
                        return this.init(
                           "to the portal frame at " + stance.toShortString(),
                           HashUtil.init(
                              stance,
                              0.5,
                              true,
                              true,
                              true,
                              60,
                              autismclient.ai.a.ClassCache.Inner_IoWorker.load,
                              true,
                              2.0,
                              null,
                              HashUtil.Inner_BufferHelper.DEFAULT
                           )
                        );
                     }
                  } else {
                     if (portal != null) {
                        return this.init(
                           "into the end portal at " + portal.toShortString(),
                           HashUtil.init(
                              portal,
                              0.6,
                              true,
                              true,
                              true,
                              120,
                              autismclient.ai.a.ClassCache.Inner_IoWorker.load,
                              true,
                              2.0,
                              null,
                              HashUtil.Inner_BufferHelper.DEFAULT
                           )
                        );
                     }

                     BlockPos frame = cO.init_BlockPos_Predicate_I(s -> s.is(Blocks.END_PORTAL_FRAME), 12);
                     if (frame != null) {
                        if (this.write_BlockPos == null) {
                           cl.init_V_Str_ObjArr("[AI] stronghold: portal frame seen at {} after {} throws", frame.toShortString(), this.sync_I);
                        }

                        this.write_BlockPos = frame;
                        t.Inner_StreamUtil ring = t.load(frame);
                        if (ring != null) {
                           this.stop = 0;
                           List<BlockPos> empty = ring.empty();
                           int eyes = e.init_I("minecraft:ender_eye");
                           if (!empty.isEmpty() && eyes < empty.size()) {
                              int canMake = Math.min(
                                 e.init_I("minecraft:ender_pearl"), e.init_I("minecraft:blaze_powder") + 2 * e.init_I("minecraft:blaze_rod")
                              );
                              if (canMake > 0) {
                                 return this.init_AiOperation_Result(Math.min(canMake, empty.size() - eyes));
                              }

                              this.write_Z = true;
                              if (this.init_co_CachePool.init(600000000000L)) {
                                 return this.load_AiOperation_Result(empty.size() - eyes);
                              }

                              if (e.init_I("minecraft:blaze_powder") + e.init_I("minecraft:blaze_rod") > 0
                                 && this.open < 8
                                 && O.init_Entity_Minecraft(mc, "minecraft:enderman", 48.0) != null) {
                                 return this.init("hunt an enderman for a pearl (" + empty.size() + " frames empty, " + eyes + " eyes)", O.load(48.0));
                              }

                              if (e.init_I("minecraft:blaze_powder") + e.init_I("minecraft:blaze_rod") <= 0 || this.init_co_CachePool.init(600000000000L)) {
                                 return this.init(AiOperation.Status.FAILED, empty.size() + " frames are empty and only " + eyes + " eyes can be made");
                              }

                              if (!this.load_Z_2(5)) {
                                 return this.init("up to the surface for endermen", autismclient.ai.a.d.init(ag.init_JsonObject_ObjArr("timeoutSeconds", 120)));
                              }

                              double a = this.pop++ * 2.399;
                              int x = frame.getX() + (int)Math.round(Math.cos(a) * 80.0);
                              int z = frame.getZ() + (int)Math.round(Math.sin(a) * 80.0);
                              return this.init(
                                 "look for endermen near the room (" + empty.size() + " frames empty, " + eyes + " eyes)",
                                 HashUtil.init(
                                    new BlockPos(x, mc.player.getBlockY(), z),
                                    4.0,
                                    true,
                                    true,
                                    true,
                                    60,
                                    autismclient.ai.a.ClassCache.Inner_IoWorker.load,
                                    true,
                                    2.0,
                                    new aB.Inner_NetHelper(x, z, 6),
                                    HashUtil.Inner_BufferHelper.DEFAULT
                                 )
                              );
                           } else {
                              this.write_Z = false;
                              BlockPos target = empty.isEmpty() ? frame : empty.get(0);
                              if (mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(target)) > 16.0) {
                                 if (this.push >= 12) {
                                    return this.init(AiOperation.Status.FAILED, "No supported route into the observed portal room after bounded approaches");
                                 }

                                 if (this.push >= 4 && this.push % 4 == 0) {
                                    this.push++;
                                    BlockPos feet = mc.player.blockPosition();
                                    BlockPos escape = null;

                                    for (BlockPos p : BlockPos.betweenClosed(feet.offset(-3, 0, -3), feet.offset(3, 1, 3))) {
                                       if (!(p.distSqr(feet) < 4.0)
                                          && cu.init(mc.level, p)
                                          && cu.init(mc.level, p.below())
                                          && ValueSink.init_Z_Minecraft_BlockPos(mc, p)
                                          && (escape == null || p.distSqr(target) < escape.distSqr(target))) {
                                          escape = p.immutable();
                                       }
                                    }

                                    return escape != null
                                       ? this.init(
                                          "leave the pocket along supported terrain", HashUtil.init_AiOperation_BlockPos(escape, 1.0, true, true, false, 30)
                                       )
                                       : this.init(
                                          AiOperation.Status.UNCERTAIN, "Portal approach is enclosed; no observed supported escape, so no blind downward dig"
                                       );
                                 } else {
                                    return this.init(
                                       "walk to the portal frame at " + target.toShortString(),
                                       HashUtil.init(
                                          target,
                                          2.5,
                                          true,
                                          true,
                                          true,
                                          300,
                                          this.push % 2 == 1
                                             ? autismclient.ai.a.ClassCache.Inner_IoWorker.init
                                             : autismclient.ai.a.ClassCache.Inner_IoWorker.load,
                                          true,
                                          2.0,
                                          null,
                                          HashUtil.Inner_BufferHelper.DEFAULT
                                       )
                                    );
                                 }
                              } else {
                                 if (empty.isEmpty()) {
                                    return !ring.active() && ++this.read_I > 100
                                       ? this.init(AiOperation.Status.UNCERTAIN, "Twelve filled inward-facing frames did not produce nine active portal blocks")
                                       : this.load("Waiting for the portal to open");
                                 }

                                 if (!this.init_Z_5("minecraft:ender_eye")) {
                                    return this.init("hold an eye", w.init(ag.init_JsonObject_ObjArr("what", "minecraft:ender_eye")));
                                 }

                                 if (!this.init(mc.player.getEyePosition(), target) && mc.player.onGround() && this.get < 8) {
                                    BlockPos stance = this.init_BlockPos_6(target);
                                    if (stance != null) {
                                       this.get++;
                                       return this.init(
                                          "to a stance over the frame at " + target.toShortString(),
                                          HashUtil.init(
                                             stance,
                                             0.4,
                                             true,
                                             true,
                                             true,
                                             60,
                                             autismclient.ai.a.ClassCache.Inner_IoWorker.load,
                                             true,
                                             2.0,
                                             null,
                                             HashUtil.Inner_BufferHelper.DEFAULT
                                          )
                                       );
                                    }
                                 }

                                 return this.init("put an eye in the frame at " + target.toShortString(), t.init_AiOperation(target));
                              }
                           }
                        } else {
                           return this.stop++ < 3 && mc.player.blockPosition().distSqr(frame) > 36.0
                              ? this.init("load the complete portal room", HashUtil.init_AiOperation_BlockPos(frame, 3.0, true, true, true, 120))
                              : this.init(AiOperation.Status.UNCERTAIN, "Observed frame is not a complete loaded inward-facing twelve-frame ring");
                        }
                     } else {
                        if (mc.level.dimension() == Level.OVERWORLD
                           && e.init_I("minecraft:ender_eye") + e.init_I("minecraft:ender_pearl") < 13
                           && this.open < 6
                           && O.init_Entity_Minecraft(mc, "minecraft:enderman", 40.0) != null) {
                           return this.init("hunt an enderman for a pearl", O.load(40.0));
                        }

                        if (this.close >= 4 && System.nanoTime() > this.sync_J) {
                           this.close = 0;
                        }

                        if (this.close < 4 && mc.level.dimension() == Level.OVERWORLD) {
                           AiOperation errand = O.init_AiOperation_D(32.0);
                           if (errand != null) {
                              return this.init("forage for the End (" + O.init_Str() + ")", errand);
                           }
                        }

                        z.Inner_DataUtil located = this.init_z.init_z_DataUtil();
                        if (located != null && (located.uncertainty() >= 160.0 || this.next >= 2)) {
                           located = null;
                        }

                        int makeable = Math.min(e.init_I("minecraft:ender_pearl"), e.init_I("minecraft:blaze_powder") + 2 * e.init_I("minecraft:blaze_rod"));
                        boolean spareEye = e.init_I("minecraft:ender_eye") > 12 || makeable > 0;
                        if (this.load_Vec3 == null || this.init_D_3() > 640.0 && located == null && spareEye) {
                           if (e.init_I("minecraft:ender_eye") <= 12) {
                              int canMake = makeable;
                              if (canMake == 0) {
                                 this.write_Z = true;
                                 if (this.init_co_CachePool.init(600000000000L)) {
                                    return this.load_AiOperation_Result(Math.max(1, 16 - e.init_I("minecraft:ender_eye")));
                                 }

                                 if (e.init_I("minecraft:blaze_powder") + e.init_I("minecraft:blaze_rod") > 0 && mc.level.dimension() == Level.OVERWORLD) {
                                    if (this.read_BlockPos == null) {
                                       this.read_BlockPos = mc.player.blockPosition();
                                    }

                                    if (!this.init_co_CachePool.init(600000000000L)) {
                                       if (!this.load_Z_2(5)) {
                                          return this.init(
                                             "up to the surface for endermen", autismclient.ai.a.d.init(ag.init_JsonObject_ObjArr("timeoutSeconds", 120))
                                          );
                                       }

                                       double a = this.pop++ * 2.399;
                                       int x = this.read_BlockPos.getX() + (int)Math.round(Math.cos(a) * 80.0);
                                       int z = this.read_BlockPos.getZ() + (int)Math.round(Math.sin(a) * 80.0);
                                       return this.init(
                                          "look for endermen for the eyes (no pearls)",
                                          HashUtil.init(
                                             new BlockPos(x, mc.player.getBlockY(), z),
                                             4.0,
                                             true,
                                             true,
                                             true,
                                             60,
                                             autismclient.ai.a.ClassCache.Inner_IoWorker.load,
                                             true,
                                             2.0,
                                             new aB.Inner_NetHelper(x, z, 6),
                                             HashUtil.Inner_BufferHelper.DEFAULT
                                          )
                                       );
                                    }
                                 }

                                 return this.init(AiOperation.Status.FAILED, "No eye of ender to throw and none to make");
                              }

                              if (canMake > 0) {
                                 return this.init_AiOperation_Result(Math.min(canMake, Math.max(1, 16 - e.init_I("minecraft:ender_eye"))));
                              }
                           }

                           if (e.init_I("minecraft:ender_eye") <= 12) {
                              return this.init(AiOperation.Status.FAILED, "Navigation Eyes exhausted; the twelve activation Eyes remain reserved");
                           } else {
                              this.write_Z = false;
                              if (!mc.level.canSeeSky(BlockPos.containing(mc.player.getEyePosition())) && this.reset < 2 && this.init_Z_2(24)) {
                                 return this.init("up to the sky for a throw", autismclient.ai.a.d.init(ag.init_JsonObject_ObjArr("timeoutSeconds", 120)));
                              } else if (this.clear >= 3 && this.load_Vec3 != null) {
                                 this.clear = 0;
                                 Vec3 at = mc.player.position();
                                 int tx = (int)Math.floor(at.x + this.load_Vec3.x * 12.0);
                                 int tz = (int)Math.floor(at.z + this.load_Vec3.z * 12.0);
                                 return this.init(
                                    "step out from under the trees",
                                    aB.init(HashUtil.init_HashUtil_CachePool_BlockPos(new BlockPos(tx, (int)at.y, tz), new aB.Inner_NetHelper(tx, tz, 3), 30))
                                 );
                              } else {
                                 return !this.init_Z_5("minecraft:ender_eye")
                                    ? this.init("hold an eye", w.init(ag.init_JsonObject_ObjArr("what", "minecraft:ender_eye")))
                                    : this.load();
                              }
                           }
                        } else {
                           if (!this.init_Z_2(12) && this.start < 3) {
                              this.start++;
                              return this.init("up to the surface for the walk", autismclient.ai.a.d.init(ag.init_JsonObject_ObjArr("timeoutSeconds", 180)));
                           }

                           Vec3 at = mc.player.position();
                           if (this.init_D_3() < 32.0 && this.init_z.init_z_DataUtil() == null && this.sync_I < 5) {
                              Vec3 side = this.init_z.init_Vec3_Vec3_Vec3(at, this.load_Vec3);
                              if (side != null) {
                                 this.load_Z = true;
                                 this.read_Vec3 = null;
                                 int sx = (int)Math.floor(side.x);
                                 int sz = (int)Math.floor(side.z);
                                 return this.init(
                                    "establish a lateral Eye baseline",
                                    aB.init(HashUtil.init_HashUtil_CachePool_BlockPos(new BlockPos(sx, (int)at.y, sz), new aB.Inner_NetHelper(sx, sz, 8), 90))
                                 );
                              }
                           }

                           z.Inner_DataUtil estimate = this.init_z.init_z_DataUtil();
                           if (estimate != null && estimate.uncertainty() < 160.0) {
                              Vec3 toward = estimate.position().subtract(at);
                              double length = Math.hypot(toward.x, toward.z);
                              if (length < Math.max(64.0, estimate.uncertainty()) && this.next < 2) {
                                 this.next++;
                                 return this.init(
                                    "explore the observed stronghold corridors",
                                    cp.init(
                                       estimate.position(),
                                       (int)Math.max(30L, Math.min(360L, (this.init_J - System.nanoTime()) / 1000000000L)),
                                       this.init_cp_DataUtil
                                    )
                                 );
                              }

                              if (length > 24.0) {
                                 this.load_Vec3 = new Vec3(toward.x / length, 0.0, toward.z / length);
                              }
                           }

                           int tx = (int)Math.floor(at.x + this.load_Vec3.x * 160.0);
                           int tz = (int)Math.floor(at.z + this.load_Vec3.z * 160.0);
                           HashUtil.Inner_BufferHelper dry = new HashUtil.Inner_BufferHelper(true, true, 3, !this.read_Z, true, true);
                           this.read_Z = false;
                           return this.init(
                              "follow the bearing toward " + tx + ", " + tz,
                              aB.init(HashUtil.init_HashUtil_CachePool_BlockPos(new BlockPos(tx, (int)at.y, tz), new aB.Inner_NetHelper(tx, tz, 12), 120, dry))
                           );
                        }
                     }
                  }
               }
            }

            double init_D_3() {
               return this.read_Vec3 == null ? 1.7976931348623157E308 : Math.hypot(mc.player.getX() - this.read_Vec3.x, mc.player.getZ() - this.read_Vec3.z);
            }

            AiOperation.Result load() {
               if (this.init_co_StreamUtil.init_Z()) {
                  return this.init(AiOperation.Status.UNCERTAIN, "The previous Eye throw still needs a server receipt");
               }

               this.init_co_StreamUtil = new co.Inner_StreamUtil();
               this.sync_Z = true;
               this.flush_Z = false;
               this.update_I = 0;
               this.init_EyeOfEnder = null;
               this.write_Vec3 = null;
               this.init_Vec3 = mc.player.position();
               this.init_Set.clear();
               this.load_Set.clear();

               for (Entity entity : mc.level.entitiesForRendering()) {
                  if (entity instanceof EyeOfEnder) {
                     this.init_Set.add(entity.getUUID());
                  }

                  if (entity instanceof ItemEntity item && item.getItem().is(Items.ENDER_EYE)) {
                     this.load_Set.add(item.getId());
                  }
               }

               return this.load("Throwing an eye of ender");
            }

            AiOperation.Result read() {
               this.update_I++;
               if (!this.flush_Z) {
                  float pitch = co.init_FArr[Math.min(co.init_FArr.length - 1, (this.update_I - 1) / 6)];
                  this.init_DataUtil.init_V_ce_DataUtil(new autismclient.util.ce.Inner_DataUtil(mc.player.getYRot(), pitch));
                  this.init_DataUtil.load_V();
                  boolean clear = mc.hitResult == null
                     || mc.hitResult.getType() != Type.BLOCK
                     || !mc.level.canSeeSky(BlockPos.containing(mc.player.getEyePosition()))
                        && mc.hitResult instanceof BlockHitResult blockHit
                        && !mc.level.getBlockState(blockHit.getBlockPos()).is(Blocks.END_PORTAL_FRAME);
                  if (this.update_I >= 4 && clear && Math.abs(mc.player.getXRot() - pitch) < 2.0F) {
                     if (!this.init_Z_5("minecraft:ender_eye")) {
                        this.sync_Z = false;
                        this.init_DataUtil.init_V();
                        return this.load("Reequipping the Eye before its throw");
                     }

                     this.init_Vec3 = mc.player.position();
                     this.init_Set.clear();

                     for (Entity entity : mc.level.entitiesForRendering()) {
                        if (entity instanceof EyeOfEnder) {
                           this.init_Set.add(entity.getUUID());
                        }
                     }

                     BlockStatePredictionHandler prediction = ((AutismClientLevelPredictionAccessor)mc.level).autism$getBlockPredictionHandler();
                     this.init_co_StreamUtil
                        .init(
                           e.init_I("minecraft:ender_eye"),
                           mc.player.getMainHandItem().getCount(),
                           ca.init_J(),
                           ca.load_J_I(mc.player.inventoryMenu.containerId),
                           prediction.currentSequence()
                        );
                     mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
                     this.init_co_StreamUtil.init(prediction.currentSequence());
                     Compat263.swing(mc.player, InteractionHand.MAIN_HAND);
                     this.sync_I++;
                     this.flush_Z = true;
                     this.update_I = 6;
                     cl.init_V_Str_ObjArr("[AI] stronghold: throw {} at pitch {}", this.sync_I, pitch);
                     return this.load("Throwing an eye of ender");
                  } else if (this.update_I > co.init_FArr.length * 6) {
                     this.sync_Z = false;
                     this.init_DataUtil.init_V();
                     this.clear++;
                     cl.init_V_Str_ObjArr("[AI] stronghold: no clear view up to throw from {}", mc.player.blockPosition().toShortString());
                     return this.load("No clear view to throw");
                  } else {
                     return this.load("Looking up for a clear throw");
                  }
               } else {
                  boolean firstObservation = this.init_EyeOfEnder == null;
                  ArrayList<EyeOfEnder> candidates = new ArrayList<>();
                  if (firstObservation) {
                     for (Entity e : mc.level.entitiesForRendering()) {
                        if (e instanceof EyeOfEnder found
                           && !this.init_Set.contains(found.getUUID())
                           && found.isAlive()
                           && found.position().distanceToSqr(this.init_Vec3) < 256.0) {
                           candidates.add(found);
                        }
                     }
                  } else {
                     candidates.add(this.init_EyeOfEnder);
                  }

                  co.Inner_BufferHelper outcome = this.init(candidates.isEmpty() ? null : candidates.getFirst().getUUID());

                  for (int i = 1; i < candidates.size(); i++) {
                     outcome = this.init(candidates.get(i).getUUID());
                  }

                  if (outcome == co.Inner_BufferHelper.write) {
                     return this.init(AiOperation.Status.UNCERTAIN, "The Eye throw has ambiguous inventory or entity evidence; reobserve before another throw");
                  }

                  if (firstObservation) {
                     if (!candidates.isEmpty()) {
                        this.init_EyeOfEnder = candidates.getFirst();
                     }

                     if (this.init_EyeOfEnder == null) {
                        if (this.update_I - 6 > 120) {
                           if (this.init_co_StreamUtil.init_Z()) {
                              return this.init(AiOperation.Status.UNCERTAIN, "The sent Eye throw lacks a server receipt; reobserve before spending another Eye");
                           }

                           if (outcome == co.Inner_BufferHelper.load) {
                              this.flush_I++;
                           }

                           return this.init_AiOperation_Result_14(
                              outcome == co.Inner_BufferHelper.load
                                 ? "One Eye was consumed, but no usable flight was observed; retry navigation from the receipt"
                                 : "The server acknowledged an unchanged Eye inventory after the use; retry navigation from the receipt"
                           );
                        } else {
                           return this.load("Waiting for the Eye flight and server receipt");
                        }
                     } else {
                        this.write_Vec3 = this.init_EyeOfEnder.position();
                        this.init_z_StreamUtil = new z.Inner_StreamUtil(this.write_Vec3);
                        this.write_I = this.update_I;
                        return this.load("Watching the eye");
                     }
                  } else {
                     Vec3 now = this.init_EyeOfEnder.position();
                     double moved = Math.hypot(now.x - this.write_Vec3.x, now.z - this.write_Vec3.z);
                     Vec3 fitted = this.init_z_StreamUtil.init(now);
                     if (fitted == null
                        && (!(moved >= 4.0) || this.update_I - this.write_I <= 40)
                        && this.init_EyeOfEnder.isAlive()
                        && this.update_I - this.write_I <= 70) {
                        return this.load("Watching the eye (" + String.format("%.1f", moved) + " blocks)");
                     }

                     if (this.init_co_StreamUtil.init_Z()) {
                        return this.update_I - 6 > 120
                           ? this.init(AiOperation.Status.UNCERTAIN, "The observed Eye flight lacks its inventory receipt; reobserve before another throw")
                           : this.load("Waiting for the Eye throw inventory receipt");
                     }

                     if (moved >= 1.0) {
                        Vec3 line = fitted != null ? fitted : (moved >= 4.0 ? this.init_z_StreamUtil.init_Vec3() : null);
                        this.load_Vec3 = line != null ? line : new Vec3((now.x - this.write_Vec3.x) / moved, 0.0, (now.z - this.write_Vec3.z) / moved);
                        this.read_Vec3 = mc.player.position();
                        co.init_Vec3 = this.load_Vec3;
                        co.sync = this.sync_I;
                        this.init_z
                           .init_V_Vec3_Vec3(
                              line != null ? this.init_z_StreamUtil.load() : this.write_Vec3,
                              line != null ? line.scale(Math.max(4.0, moved)) : now.subtract(this.write_Vec3)
                           );
                        bL.init_V_Str_JsonObject(
                           "eye_bearing",
                           ag.init_JsonObject_ObjArr(
                              "throw",
                              this.sync_I,
                              "origin",
                              this.init_Vec3.toString(),
                              "bearing",
                              this.load_Vec3.toString(),
                              "navigation",
                              this.init_z.init_JsonObject()
                           )
                        );
                        cl.init_V_Str_ObjArr(
                           "[AI] stronghold: throw {} from {} points {} (yaw {})",
                           this.sync_I,
                           mc.player.blockPosition().toShortString(),
                           String.format("%.3f, %.3f", this.load_Vec3.x, this.load_Vec3.z),
                           Math.round(Math.toDegrees(Math.atan2(-this.load_Vec3.x, this.load_Vec3.z)))
                        );
                     }

                     this.sync_Z = false;
                     this.init_DataUtil.init_V();
                     this.init_AiOperation = this.init_AiOperation_6();
                     this.load_Str = "pick the eye back up";
                     return this.load("Bearing read");
                  }
               }
            }

            co.Inner_BufferHelper init(UUID identity) {
               int container = mc.player.inventoryMenu.containerId;
               co.Inner_BufferHelper outcome = this.init_co_StreamUtil
                  .init(
                     e.init_I("minecraft:ender_eye"),
                     ca.init_J(),
                     ca.init_OptionalInt_I(container, this.init_co_StreamUtil.load_J, "minecraft:ender_eye"),
                     ca.load_J_I(container),
                     autismclient.util.h.DataUtil.init_Z(mc.level, this.init_co_StreamUtil.write_I),
                     identity,
                     this.update_I - 6
                  );
               return outcome == co.Inner_BufferHelper.init && this.init_co_StreamUtil.read_J >= 0L
                  ? this.init_co_StreamUtil.init(ca.init_OptionalInt_I(container, this.init_co_StreamUtil.read_J, "minecraft:ender_eye"), this.update_I - 6)
                  : outcome;
            }

            AiOperation.Result init_AiOperation_Result_14(String message) {
               AiOperation.Result stopped = this.init(AiOperation.Status.FAILED, message);
               return new AiOperation.Result(
                  stopped.status(),
                  stopped.message(),
                  stopped.dataJson(),
                  stopped.evidence(),
                  stopped.status() == AiOperation.Status.UNCERTAIN ? A.value : A.init_A
               );
            }

            AiOperation init_AiOperation_6() {
               final int before = e.init_I("minecraft:ender_eye");
               return aB.init_AiOperation_I_AiOperation(
                  20,
                  new AiOperation() {
                     AiOperation init_AiOperation;
                     int init_I;

                     @Override
                     public AiOperation.Result tick() {
                        if (e.init_I("minecraft:ender_eye") > before) {
                           return R.init_AiOperation_Result(
                              AiOperation.Status.SUCCEEDED, "An Eye pickup is confirmed in inventory", ag.init_JsonObject_ObjArr("recovered", true)
                           );
                        }

                        if (this.init_AiOperation == null) {
                           boolean inAir = false;
                           inAir = init_EyeOfEnder != null && init_EyeOfEnder.isAlive() && mc.level.getEntity(init_EyeOfEnder.getId()) == init_EyeOfEnder;
                           if (inAir && ++this.init_I < 120) {
                              return R.init_AiOperation_Result(AiOperation.Status.RUNNING, "Waiting for the eye to come down", new JsonObject());
                           }

                           boolean dropped = false;

                           for (Entity e : mc.level.entitiesForRendering()) {
                              if (e instanceof ItemEntity item
                                 && item.getItem().is(Items.ENDER_EYE)
                                 && !load_Set.contains(item.getId())
                                 && item.distanceToSqr(mc.player) < 576.0
                                 && (init_EyeOfEnder == null || Math.hypot(item.getX() - init_EyeOfEnder.getX(), item.getZ() - init_EyeOfEnder.getZ()) < 5.0)) {
                                 dropped = true;
                                 break;
                              }
                           }

                           if (!dropped) {
                              flush_I++;
                              return R.init_AiOperation_Result(
                                 AiOperation.Status.SUCCEEDED,
                                 "Eye not recovered; shatter or lost drop remains unconfirmed",
                                 ag.init_JsonObject_ObjArr("recovered", false)
                              );
                           }

                           this.init_AiOperation = T.write(
                              ag.init_JsonObject_ObjArr("item", "minecraft:ender_eye", "count", before + 1, "radius", 24, "timeoutSeconds", 20)
                           );
                        }

                        AiOperation.Result r = this.init_AiOperation.tick();
                        if (r.terminal() && e.init_I("minecraft:ender_eye") <= before) {
                           flush_I++;
                        }

                        return r;
                     }

                     @Override
                     public boolean canInterruptNow() {
                        return this.init_AiOperation == null || this.init_AiOperation.canInterruptNow();
                     }

                     @Override
                     public boolean requiresReconciliationAfterEmergency() {
                        return this.init_AiOperation != null && this.init_AiOperation.requiresReconciliationAfterEmergency();
                     }

                     @Override
                     public void cancel() {
                        if (this.init_AiOperation != null) {
                           this.init_AiOperation.cancel();
                        }
                     }
                  }
               );
            }

            AiOperation.Result init_AiOperation_Result(int count) {
               if (mc.player.getInventory().getFreeSlot() < 0 && e.init_I("minecraft:ender_eye") == 0 && System.nanoTime() - this.flush_J > 30000000000L) {
                  this.flush_J = System.nanoTime();
                  JsonArray keep = new JsonArray();

                  for (String id : List.of(
                     "minecraft:ender_eye",
                     "minecraft:ender_pearl",
                     "minecraft:blaze_rod",
                     "minecraft:blaze_powder",
                     "minecraft:bow",
                     "minecraft:arrow",
                     "minecraft:water_bucket",
                     "minecraft:bucket",
                     "minecraft:flint_and_steel",
                     "minecraft:string",
                     "minecraft:white_bed",
                     "minecraft:white_wool"
                  )) {
                     keep.add(id);
                  }

                  return this.init("tidy the bag for the eyes", cx.init(ag.init_JsonObject_ObjArr("action", "drop", "freeSlots", 4, "keep", keep)));
               } else {
                  return this.init(
                     "craft " + count + " eyes of ender",
                     e.init(ag.init_JsonObject_ObjArr("item", "minecraft:ender_eye", "count", e.init_I("minecraft:ender_eye") + count, "timeoutSeconds", 60))
                  );
               }
            }

            boolean load_Z_2(int radius) {
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

            boolean init_Z_5(String id) {
               return BuiltInRegistries.ITEM.getKey(mc.player.getMainHandItem().getItem()).toString().equals(id);
            }

            AiOperation.Result init(String label, AiOperation op) {
               this.init_Z = label.startsWith("into the end portal") || label.startsWith("onto the end portal");
               this.load_Str = label;
               this.update_J = System.nanoTime();
               cl.init_V_Str_ObjArr("[AI] stronghold: {}", label);
               this.init_AiOperation = bJ.init_AiOperation_Str("stronghold", label, op);
               return this.load(label);
            }

            JsonObject init_JsonObject_53() {
               return ag.init_JsonObject_ObjArr(
                  "throws",
                  this.sync_I,
                  "eyesLost",
                  this.flush_I,
                  "eyes",
                  e.init_I("minecraft:ender_eye"),
                  "bearing",
                  this.load_Vec3 == null ? "" : String.format("%.3f,%.3f", this.load_Vec3.x, this.load_Vec3.z),
                  "frame",
                  this.write_BlockPos == null ? "" : this.write_BlockPos.toShortString(),
                  "navigation",
                  this.init_z.init_JsonObject(),
                  "endEntrance",
                  this.init_BlockPos == null ? "" : this.init_BlockPos.toShortString(),
                  "entryAuthorized",
                  this.init_Z,
                  "arrivalTicks",
                  this.init_I,
                  "resources",
                  bK.init(16, 12, 0).data(),
                  "pearlSearchDarkSeconds",
                  this.init_co_CachePool.init() / 1000000000L,
                  "eyeThrow",
                  this.init_co_StreamUtil.init_JsonObject(),
                  "steps",
                  this.init_JsonArray
               );
            }

            AiOperation.Result load_AiOperation_Result(int missing) {
               AiOperation.Result stopped = this.init(
                  AiOperation.Status.FAILED, "The bounded dark-time pearl search is exhausted; change the pearl acquisition route"
               );
               JsonObject evidence = bK.init_JsonObject(stopped.dataJson());
               evidence.addProperty("pearlRouteChange", bK.load_Z() && aB.load_Z());
               evidence.addProperty("pearlsNeeded", missing);
               return new AiOperation.Result(stopped.status(), stopped.message(), evidence.toString(), stopped.evidence(), A.pop);
            }

            AiOperation.Result load(String message) {
               return R.init_AiOperation_Result(AiOperation.Status.RUNNING, message, this.init_JsonObject_53());
            }

            AiOperation.Result init(AiOperation.Status status, String message) {
               return this.init(status, message, null);
            }

            AiOperation.Result init(AiOperation.Status status, String message, AiOperation.Result child) {
               boolean pending = this.requiresReconciliationAfterEmergency();
               this.init_co_DataUtil.init_V(this.init_AiOperation);
               this.init_co_DataUtil.init(pending, child);
               this.init_co_DataUtil.init(this.init_Y.read(), this.init_AiOperation_Result);
               AiOperation.Result result = this.init_co_DataUtil.init(status, message, this.init_JsonObject_53(), child);
               this.init_V_28();
               this.init_Y.init_V();
               this.init_DataUtil.init_V();
               return result;
            }

            void init_V_28() {
               if (this.init_AiOperation != null) {
                  this.init_co_DataUtil.load(this.init_AiOperation);
                  this.init_AiOperation = null;
               }
            }

            @Override
            public boolean canInterruptNow() {
               return !this.init_co_DataUtil.init()
                  && !this.sync_Z
                  && !this.init_co_StreamUtil.init_Z()
                  && (this.init_AiOperation == null || this.init_AiOperation.canInterruptNow())
                  && this.init_Y.load();
            }

            @Override
            public boolean requiresReconciliationAfterEmergency() {
               return this.init_co_DataUtil.init()
                  || this.init_co_StreamUtil.init_Z()
                  || this.init_AiOperation != null && this.init_AiOperation.requiresReconciliationAfterEmergency()
                  || this.init_Y.read();
            }

            @Override
            public void cancel() {
               this.init_co_DataUtil.init(this.init_Y.read(), this.init_AiOperation_Result);
               this.init_V_28();
               this.init_Y.init_V();
               this.init_DataUtil.init_V();
            }
         };
      } else {
         throw new IllegalArgumentException("The stronghold run needs a connected living player");
      }
   }

   enum Inner_BufferHelper {
      init,
      load,
      read,
      write;
   }

   static final class Inner_CachePool {
      private long init_J = -9223372036854775808L;
      private long load;
      private boolean init_Z;

      void init(long now, boolean darkSearch) {
         if (this.init_J == -9223372036854775808L || now >= this.init_J) {
            if (this.init_J != -9223372036854775808L && this.init_Z) {
               this.load = this.load + (now - this.init_J);
            }

            this.init_J = now;
            this.init_Z = darkSearch;
         }
      }

      long init() {
         return this.load;
      }

      boolean init(long limit) {
         return this.load >= limit;
      }
   }

   static final class Inner_DataUtil {
      private AiOperation init_AiOperation;
      private AiOperation.Result init_AiOperation_Result;
      private AiOperation.Result load;
      private boolean init_Z;

      AiOperation.Result init_AiOperation_Result(AiOperation child) {
         boolean before = child.requiresReconciliationAfterEmergency();
         this.init_AiOperation = child;
         this.init_AiOperation_Result = child.tick();
         if (this.init_AiOperation_Result.terminal()) {
            JsonObject data = bK.init_JsonObject(this.init_AiOperation_Result.dataJson());
            boolean needsObservation = child.requiresReconciliationAfterEmergency()
               || this.init_AiOperation_Result.status() != AiOperation.Status.SUCCEEDED && before
               || ag.init_Z_JsonObject(data, "interruptedActionNeedsReconciliation", false)
               || ag.init_Z_JsonObject(data, "reobserveBeforeResuming", false);
            this.init_AiOperation_Result = Y.init_AiOperation_Result_Z_AiOperation_Result(needsObservation, this.init_AiOperation_Result);
            this.init(needsObservation, this.init_AiOperation_Result);
         }

         return this.init_AiOperation_Result;
      }

      void init_V(AiOperation child) {
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
         JsonObject receipt = actual == null ? new JsonObject() : bK.init_JsonObject(actual.dataJson());
         if (needsObservation
            || ag.init_Z_JsonObject(receipt, "interruptedActionNeedsReconciliation", false)
            || ag.init_Z_JsonObject(receipt, "reobserveBeforeResuming", false)) {
            this.init_Z = true;
            if (actual != null && (this.load == null || actual == this.init_AiOperation_Result)) {
               this.load = actual;
            }
         }
      }

      void load(AiOperation child) {
         if (child != null) {
            this.init_V(child);
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

         AiOperation.Result result = co.init(status, message, data, this.init_Z, actual);
         return actual == null ? result : new AiOperation.Result(result.status(), result.message(), result.dataJson(), actual.evidence(), actual.failure());
      }
   }

   static final class Inner_StreamUtil {
      int init_I;
      int load_I;
      int read_I;
      int write_I = -1;
      int update_I;
      long init_J;
      long load_J;
      long read_J = -1L;
      long write_J;
      long update_J;
      int sync = -1;
      UUID init_UUID;
      boolean init_Z;
      boolean load_Z;
      boolean read_Z;
      boolean write_Z;
      boolean update_Z;

      void init(int eyes, int held, long inventory, long replies, int sequence) {
         if (this.init_Z) {
            throw new IllegalStateException("An Eye throw receipt cannot send another use");
         }

         this.init_I = this.update_I = eyes;
         this.load_I = held;
         this.init_J = this.write_J = inventory;
         this.load_J = this.update_J = replies;
         this.read_I = sequence;
         this.init_Z = true;
      }

      void init(int sequence) {
         if (sequence > this.read_I) {
            this.write_I = sequence;
         }
      }

      co.Inner_BufferHelper init(int eyes, long inventory, OptionalInt snapshot, long replies, boolean acknowledged, UUID identity, int elapsedTicks) {
         if (!this.init_Z) {
            return co.Inner_BufferHelper.init;
         }

         this.update_I = eyes;
         this.write_J = inventory;
         this.update_J = replies;
         boolean freshSnapshot = snapshot.isPresent() && replies > this.load_J;
         this.sync = freshSnapshot ? snapshot.getAsInt() : -1;
         this.update_Z = this.update_Z | (acknowledged && this.write_I >= 0);
         if (identity != null) {
            if (this.init_UUID == null) {
               this.init_UUID = identity;
            } else if (!this.init_UUID.equals(identity)) {
               this.write_Z = true;
            }
         }

         if (this.update_Z && this.read_J < 0L) {
            this.read_J = replies;
         }

         if (this.write_Z) {
            return co.Inner_BufferHelper.write;
         } else if (this.load_Z) {
            return co.Inner_BufferHelper.load;
         } else if (this.read_Z) {
            return co.Inner_BufferHelper.read;
         } else {
            int serverEyes = freshSnapshot ? Math.min(eyes, snapshot.getAsInt()) : eyes;
            if (this.init_I - serverEyes > 1) {
               this.write_Z = true;
               return co.Inner_BufferHelper.write;
            } else if (this.init_I > 0
               && this.load_I > 0
               && (eyes == this.init_I - 1 && inventory > this.init_J || freshSnapshot && snapshot.getAsInt() == this.init_I - 1)) {
               this.load_Z = true;
               return co.Inner_BufferHelper.load;
            } else {
               return co.Inner_BufferHelper.init;
            }
         }
      }

      co.Inner_BufferHelper init(OptionalInt snapshotAfterAcknowledgement, int elapsedTicks) {
         if (this.write_Z) {
            return co.Inner_BufferHelper.write;
         } else if (this.load_Z) {
            return co.Inner_BufferHelper.load;
         } else if (this.read_Z) {
            return co.Inner_BufferHelper.read;
         } else if (this.init_Z
            && elapsedTicks > 120
            && this.update_Z
            && this.read_J >= 0L
            && this.update_J > this.read_J
            && snapshotAfterAcknowledgement.isPresent()
            && snapshotAfterAcknowledgement.getAsInt() == this.init_I
            && this.update_I == this.init_I
            && this.init_UUID == null) {
            this.read_Z = true;
            return co.Inner_BufferHelper.read;
         } else {
            return co.Inner_BufferHelper.init;
         }
      }

      boolean init_Z() {
         return this.init_Z && (this.write_Z || !this.load_Z && !this.read_Z);
      }

      JsonObject init_JsonObject() {
         return ag.init_JsonObject_ObjArr(
            "sent",
            this.init_Z,
            "beforeEyes",
            this.init_I,
            "heldEyes",
            this.load_I,
            "observedEyes",
            this.update_I,
            "inventoryBefore",
            this.init_J,
            "repliesBefore",
            this.load_J,
            "sequenceBefore",
            this.read_I,
            "sentSequence",
            this.write_I,
            "repliesAtAcknowledgement",
            this.read_J,
            "observedInventoryRevision",
            this.write_J,
            "observedContainerReplies",
            this.update_J,
            "serverEyeSnapshot",
            this.sync,
            "useAcknowledged",
            this.update_Z,
            "eyeIdentity",
            this.init_UUID == null ? "" : this.init_UUID.toString(),
            "consumed",
            this.load_Z,
            "rejected",
            this.read_Z,
            "ambiguous",
            this.write_Z,
            "pending",
            this.init_Z()
         );
      }
   }
}
