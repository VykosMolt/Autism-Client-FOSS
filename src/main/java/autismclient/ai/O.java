package autismclient.ai;

import autismclient.AutismClientAddon;
import autismclient.ai.a.HashUtil;
import autismclient.api.ai.AiOperation;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;

final class O {
   static final int init_I = 7;
   static final int load = 24;
   static final List<String> init_List = List.of(
      "white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"
   );
   static final int read = 8;
   static final int write = 24;

   private O() {
   }

   static int init_I() {
      return af.init_I(af.init_af_DataUtil("bed"));
   }

   static boolean init_Z() {
      boolean bow = bK.init("minecraft:bow", 24) > 0;
      return bow && (bK.write() >= 24 || init_I() >= 7 && bK.write() >= 12);
   }

   static boolean load() {
      return init_I() >= 4 || bK.init("minecraft:bow", 12) > 0 && bK.write() >= 12;
   }

   static AiOperation init_AiOperation_I(int seconds) {
      Minecraft mc = Minecraft.getInstance();
      Map<String, Integer> raw = cl.init_Map();
      if (!raw.isEmpty() && mc.player != null && mc.level != null) {
         Entry<String, Integer> meat = raw.entrySet().stream().max(Entry.comparingByValue()).orElseThrow();
         if (e.init_I("minecraft:furnace") == 0 && !ci.init_Z(mc, "minecraft:furnace")) {
            if (af.init_I() < 2) {
               return cx.init(ag.init_JsonObject_ObjArr("action", "drop", "freeSlots", 2, "timeoutSeconds", Math.min(60, seconds)));
            } else {
               g.Inner_StreamUtil graph = e.init_g_StreamUtil();
               boolean table = e.load() || e.init_I("minecraft:crafting_table") > 0 || g.init(graph, "minecraft:crafting_table", 1).complete();
               if (!g.init(graph, "minecraft:furnace", 1).complete()) {
                  return aE.init(ag.init_JsonObject_ObjArr("item", "minecraft:furnace", "count", 1, "timeoutSeconds", Math.min(180, seconds)));
               } else {
                  return table
                     ? e.init(ag.init_JsonObject_ObjArr("item", "minecraft:furnace", "count", 1, "timeoutSeconds", Math.min(60, seconds)))
                     : aE.init(ag.init_JsonObject_ObjArr("item", "minecraft:crafting_table", "count", 1, "timeoutSeconds", Math.min(180, seconds)));
               }
            }
         } else {
            String fuel = cl.init_Str_I(meat.getValue());
            if (fuel != null) {
               String output = cl.read_Map.get(meat.getKey());
               return ci.load(
                  ag.init_JsonObject_ObjArr(
                     "item",
                     output,
                     "count",
                     e.init_I(output) + meat.getValue(),
                     "input",
                     meat.getKey(),
                     "fuel",
                     fuel,
                     "timeoutSeconds",
                     Math.max(30, Math.min(seconds, 60 + 12 * meat.getValue()))
                  )
               );
            } else {
               return af.init_I(af.load_af_DataUtil("log")) > 0
                  ? e.init(
                     ag.init_JsonObject_ObjArr(
                        "item",
                        "planks",
                        "count",
                        af.init_I(af.load_af_DataUtil("planks")) + Math.max(4, (meat.getValue() * 200 + 299) / 300),
                        "timeoutSeconds",
                        45
                     )
                  )
                  : aE.init(
                     ag.init_JsonObject_ObjArr(
                        "item", "minecraft:coal", "count", Math.max(2, (meat.getValue() + 7) / 8), "timeoutSeconds", Math.min(180, seconds)
                     )
                  );
            }
         }
      } else {
         return null;
      }
   }

   static AiOperation init_AiOperation_D(double range) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         if (bK.load_I() < 8) {
            AiOperation hunt = cl.init_AiOperation_D(range, 4, 45);
            if (hunt != null) {
               return hunt;
            }
         }

         AiOperation water = init(mc, range);
         if (water != null) {
            return water;
         }

         if (init_Z()) {
            return null;
         }

         if (init_I() < 7 && af.init_I() < 2) {
            return cx.init(ag.init_JsonObject_ObjArr("action", "drop", "freeSlots", 3));
         }

         int stringForBow = bK.init("minecraft:bow", 24) > 0 ? 0 : 3;
         if (init_I() < 7 && e.init_I("minecraft:string") - stringForBow >= 4 && e.init_I("minecraft:white_wool") < 3) {
            return e.init(ag.init_JsonObject_ObjArr("item", "minecraft:white_wool", "count", e.init_I("minecraft:white_wool") + 1, "timeoutSeconds", 30));
         }

         for (String color : init_List) {
            if (init_I() < 7 && e.init_I("minecraft:" + color + "_wool") >= 3) {
               if (af.init_I(af.load_af_DataUtil("planks")) < 3) {
                  return aE.init(ag.init_JsonObject_ObjArr("item", "planks", "count", 3, "timeoutSeconds", 60));
               }

               return e.init(
                  ag.init_JsonObject_ObjArr("item", "minecraft:" + color + "_bed", "count", e.init_I("minecraft:" + color + "_bed") + 1, "timeoutSeconds", 30)
               );
            }
         }

         if (bK.init("minecraft:bow", 24) == 0 && e.init_I("minecraft:string") >= 3) {
            return e.init_I("minecraft:stick") < 3
               ? aE.init(ag.init_JsonObject_ObjArr("item", "minecraft:stick", "count", 3, "timeoutSeconds", 60))
               : e.init(ag.init_JsonObject_ObjArr("item", "minecraft:bow", "count", e.init_I("minecraft:bow") + 1, "timeoutSeconds", 30));
         }

         if (bK.write() < 24 && e.init_I("minecraft:flint") > 0 && e.init_I("minecraft:feather") > 0) {
            return e.init_I("minecraft:stick") < 1
               ? aE.init(ag.init_JsonObject_ObjArr("item", "minecraft:stick", "count", 1, "timeoutSeconds", 60))
               : e.init(ag.init_JsonObject_ObjArr("item", "minecraft:arrow", "count", e.init_I("minecraft:arrow") + 4, "timeoutSeconds", 30));
         }

         if (bK.init("minecraft:bow", 24) > 0 && bK.write() < 24 && e.init_I("minecraft:feather") > 0 && e.init_I("minecraft:flint") == 0) {
            BlockPos gravel = cO.init_BlockPos_Predicate_I(s -> s.is(Blocks.GRAVEL), 2);
            if (gravel != null && gravel.distSqr(mc.player.blockPosition()) <= range * range) {
               int want = Math.min(4, Math.min(e.init_I("minecraft:feather"), (24 - bK.write() + 3) / 4));
               return aE.init(ag.init_JsonObject_ObjArr("item", "minecraft:flint", "count", want, "radius", (int)Math.ceil(range), "timeoutSeconds", 45));
            }
         }

         if (init_I() < 7) {
            BlockPos bed = cO.init_BlockPos_Predicate_I(s -> s.getBlock() instanceof BedBlock && s.getValue(BedBlock.PART) == BedPart.HEAD, 4);
            if (bed != null && bed.distSqr(mc.player.blockPosition()) <= range * range * 4.0) {
               String id = BuiltInRegistries.ITEM.getKey(mc.level.getBlockState(bed).getBlock().asItem()).toString();
               int want = e.init_I(id) + 1;
               return aB.init_AiOperation_AiOperation_Supplier(
                  R.init("break", ag.init_JsonObject_ObjArr("x", bed.getX(), "y", bed.getY(), "z", bed.getZ(), "timeoutSeconds", 60, "requireDrops", false)),
                  () -> T.write(ag.init_JsonObject_ObjArr("item", id, "count", want, "radius", 8, "timeoutSeconds", 15))
               );
            }

            Entity sheep = init_Entity_Minecraft(mc, "minecraft:sheep", range);
            if (sheep != null) {
               return init_AiOperation_Str("minecraft:sheep", "wool", range);
            }
         }

         boolean bow = bK.init("minecraft:bow", 24) > 0;
         if (!bow && e.init_I("minecraft:string") < 3) {
            for (String spider : List.of("minecraft:spider", "minecraft:cave_spider")) {
               if (init_Entity_Minecraft(mc, spider, range / 2.0) != null) {
                  return init_AiOperation_Str(spider, "minecraft:string", range / 2.0);
               }
            }
         }

         if (bK.write() < 24) {
            if (init_Entity_Minecraft(mc, "minecraft:skeleton", range / 2.0) != null) {
               return init_AiOperation_Str("minecraft:skeleton", "minecraft:arrow", range / 2.0);
            }

            if (e.init_I("minecraft:feather") < 6 && init_Entity_Minecraft(mc, "minecraft:chicken", range / 2.0) != null) {
               return init_AiOperation_Str("minecraft:chicken", "minecraft:feather", range / 2.0);
            }
         }

         return null;
      } else {
         return null;
      }
   }

   static AiOperation init(Minecraft mc, double range) {
      return init_AiOperation_Minecraft(mc, range, 2);
   }

   static AiOperation init_AiOperation_Minecraft(Minecraft mc, double range, int want) {
      if (!mc.level.environmentAttributes().getDimensionValue(EnvironmentAttributes.WATER_EVAPORATES)
         && e.init_I("minecraft:water_bucket") < want
         && e.init_I("minecraft:bucket") != 0) {
         BlockPos feet = mc.player.blockPosition();
         BlockPos best = null;
         double bestD = Math.min(range, 24.0) * Math.min(range, 24.0);

         for (BlockPos p : cO.init(s -> s.getFluidState().isSource() && s.getFluidState().is(FluidTags.WATER) && s.getBlock() == Blocks.WATER, 2, 8, 64)) {
            if (mc.level.getBlockState(p.above()).isAir()) {
               double d = p.distSqr(feet);
               if (d < bestD) {
                  bestD = d;
                  best = p;
               }
            }
         }

         if (best == null) {
            return null;
         }

         BlockPos source = best;
         return aB.init_AiOperation_AiOperation_Supplier(
            HashUtil.init(source.above(), 3.0, false, false, false, 30, autismclient.ai.a.ClassCache.Inner_IoWorker.init, true),
            () -> TextFormat.init(ag.init_JsonObject_ObjArr("action", "fill", "fluid", "water", "timeoutSeconds", 10))
         );
      } else {
         return null;
      }
   }

   static Entity init_Entity_Minecraft(Minecraft mc, String type, double range) {
      for (Entity e : cO.init_List_Str_D(type, range)) {
         if (!(e instanceof AgeableMob mob && mob.isBaby()) && ValueSink.init_Z_Minecraft_Entity(mc, e)) {
            return e;
         }
      }

      return null;
   }

   static AiOperation init_AiOperation_Str(String type, String drop, double range) {
      return aB.init(
         ValueSink.init(
            ag.init_JsonObject_ObjArr("target", type, "count", 1, "radius", (int)Math.ceil(range), "useShield", true, "retreatHealth", 8, "timeoutSeconds", 40)
         ),
         drop,
         () -> T.write(ag.init_JsonObject_ObjArr("item", drop, "count", 64, "radius", 10, "timeoutSeconds", 10))
      );
   }

   static AiOperation init_AiOperation_I(final int enderman, final Predicate<BlockPos> safe, final Runnable refused) {
      final Minecraft mc = Minecraft.getInstance();
      return new AiOperation() {
         final autismclient.ai.a.DataUtil init_DataUtil = new autismclient.ai.a.DataUtil();
         final Object init_Obj = mc.level;
         final Object load_Obj = mc.getConnection();
         final UUID init_UUID = mc.level != null && mc.level.getEntity(enderman) != null ? mc.level.getEntity(enderman).getUUID() : null;
         AiOperation init_AiOperation;
         boolean init_Z;
         int init_I;
         int load_I;

         @Override
         public AiOperation.Result tick() {
            if (!this.init_Z
               && mc.player != null
               && mc.level != null
               && mc.level == this.init_Obj
               && mc.getConnection() == this.load_Obj
               && mc.player.isAlive()) {
               Entity observed = mc.level.getEntity(enderman);
               if (observed != null && !observed.getUUID().equals(this.init_UUID)) {
                  this.cancel();
                  return R.init_AiOperation_Result(AiOperation.Status.FAILED, "Enderman identity changed", new JsonObject());
               }

               this.load_I = mc.player.onGround() && !safe.test(mc.player.blockPosition()) ? this.load_I + 1 : 0;
               if (this.load_I > 10) {
                  this.cancel();
                  return R.init_AiOperation_Result(AiOperation.Status.FAILED, "Off safe ground in the hunt", new JsonObject());
               }

               if (this.init_AiOperation == null) {
                  if (!(mc.level.getEntity(enderman) instanceof Enderman man) || !man.isAlive()) {
                     return R.init_AiOperation_Result(AiOperation.Status.FAILED, "The enderman is gone", new JsonObject());
                  }

                  if (!man.isCreepy()) {
                     if (++this.init_I <= 80 && mc.player.hasLineOfSight(man)) {
                        mc.options.keyUp.setDown(false);
                        mc.options.keyDown.setDown(false);
                        mc.options.keyLeft.setDown(false);
                        mc.options.keyRight.setDown(false);
                        mc.options.keyJump.setDown(false);
                        mc.options.keySprint.setDown(false);
                        this.init_DataUtil.init_DataUtil_I(enderman).init_V_Vec3(man.getEyePosition());
                        this.init_DataUtil.load_V();
                        return R.init_AiOperation_Result(AiOperation.Status.RUNNING, "Looking the enderman in the eye", new JsonObject());
                     }

                     refused.run();
                     this.init_DataUtil.init_V();
                     return R.init_AiOperation_Result(AiOperation.Status.FAILED, "The enderman would not turn", new JsonObject());
                  }

                  this.init_DataUtil.init_V();
                  AutismClientAddon.LOG
                     .info(
                        "[AI] pearl hunt: the enderman at {} turned on the player after {} ticks; holding ground",
                        man.blockPosition().toShortString(),
                        this.init_I
                     );
                  this.init_AiOperation = ValueSink.init(
                     ag.init_JsonObject_ObjArr(
                        "entityId", enderman, "count", 1, "radius", 48, "useShield", true, "retreatHealth", 6, "hold", true, "timeoutSeconds", 40
                     )
                  );
               }

               AiOperation.Result r = this.init_AiOperation.tick();
               if (r.terminal() && !r.message().startsWith("Killed")) {
                  refused.run();
               }

               return r;
            } else {
               this.cancel();
               return R.init_AiOperation_Result(AiOperation.Status.CANCELLED, "Pearl hunt player or world changed", new JsonObject());
            }
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
            this.init_Z = true;
            if (this.init_AiOperation != null) {
               this.init_AiOperation.cancel();
            }

            this.init_DataUtil.init_V();
         }
      };
   }

   static boolean init(Minecraft mc, BlockPos feet) {
      if (mc.level.getBlockState(feet.below()).getCollisionShape(mc.level, feet.below()).isEmpty()) {
         return false;
      }

      for (Direction d : Plane.HORIZONTAL) {
         BlockPos side = feet.relative(d);
         if (mc.level.getBlockState(side).getCollisionShape(mc.level, side).isEmpty()) {
            return false;
         }
      }

      for (BlockPos c : BlockPos.betweenClosed(feet.offset(-1, -1, -1), feet.offset(1, 1, 1))) {
         if (mc.level.getFluidState(c).is(FluidTags.LAVA)) {
            return false;
         }
      }

      return true;
   }

   static boolean load(Minecraft mc, BlockPos feet) {
      BlockPos cell = feet.below();
      BlockState state = mc.level.getBlockState(cell);
      if (!state.getCollisionShape(mc.level, cell).isEmpty()
         && !(state.getDestroySpeed(mc.level, cell) < 0.0F)
         && !(state.getDestroySpeed(mc.level, cell) > 3.0F)
         && !state.hasBlockEntity()) {
         for (BlockPos c : BlockPos.betweenClosed(cell.offset(-1, -1, -1), cell.offset(1, 1, 1))) {
            if (!mc.level.getFluidState(c).isEmpty()) {
               return false;
            }
         }

         if (mc.level.getBlockState(cell.below()).getCollisionShape(mc.level, cell.below()).isEmpty()) {
            return false;
         }

         for (Direction d : Plane.HORIZONTAL) {
            BlockPos side = cell.relative(d);
            if (mc.level.getBlockState(side).getCollisionShape(mc.level, side).isEmpty()) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   static boolean read(Minecraft mc, BlockPos at) {
      if (init(mc, at)) {
         return true;
      }

      for (int dx = -3; dx <= 3; dx++) {
         for (int dz = -3; dz <= 3; dz++) {
            boolean ground = false;

            for (int dy = 1; dy >= -6 && !ground; dy--) {
               BlockPos c = at.offset(dx, dy, dz);
               if (!cu.init(mc.level, c)) {
                  return false;
               }

               if (mc.level.getFluidState(c).is(FluidTags.LAVA)) {
                  return false;
               }

               if (!mc.level.getBlockState(c).getCollisionShape(mc.level, c).isEmpty()) {
                  ground = true;
               }
            }

            if (!ground) {
               return false;
            }
         }
      }

      return true;
   }

   static AiOperation load(double range) {
      AiOperation provoked = read(range);
      return provoked != null ? provoked : init_AiOperation_Str("minecraft:enderman", "minecraft:ender_pearl", range);
   }

   static AiOperation read(double range) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.player.onGround() && write(mc, mc.player.blockPosition())) {
         for (Entity e : cO.init_List_Str_D("minecraft:enderman", range)) {
            if (Math.abs(e.getY() - mc.player.getY()) <= 12.0 && mc.player.hasLineOfSight(e)) {
               return aB.init(
                  init_AiOperation_I(e.getId(), p -> write(mc, p), () -> {}),
                  "minecraft:ender_pearl",
                  () -> T.write(ag.init_JsonObject_ObjArr("item", "minecraft:ender_pearl", "count", 64, "radius", 16, "timeoutSeconds", 15))
               );
            }
         }
      }

      return null;
   }

   static boolean write(Minecraft mc, BlockPos feet) {
      if (init(mc, feet)) {
         return true;
      }

      for (int dx = -1; dx <= 1; dx++) {
         for (int dz = -1; dz <= 1; dz++) {
            boolean ground = false;

            for (int dy = 1; dy >= -8; dy--) {
               BlockPos c = feet.offset(dx, dy - 1, dz);
               if (!cu.init(mc.level, c)) {
                  return false;
               }

               if (mc.level.getFluidState(c).is(FluidTags.LAVA)) {
                  return false;
               }

               if (!mc.level.getBlockState(c).getCollisionShape(mc.level, c).isEmpty()) {
                  ground = dy >= -3;
                  break;
               }
            }

            if (!ground) {
               return false;
            }
         }
      }

      return true;
   }

   static String init_Str() {
      return init_I() + " beds, " + (e.init_I("minecraft:bow") > 0 ? "a bow" : "no bow") + ", " + e.init_I("minecraft:arrow") + " arrows";
   }
}
