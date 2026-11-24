package autismclient.ai;

import autismclient.ai.a.HashUtil;
import autismclient.api.ai.AiOperation;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.EntityHitResult;

final class JsonUtil {
   private JsonUtil() {
   }

   static AiOperation init(JsonObject args) {
      final Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         final int target = Math.max(0, Math.min(64, ag.init_I_JsonObject(args, "pearls", 0)));
         boolean additional = ag.init_Z_JsonObject(args, "additional", false);
         final int baseline = e.init_I("minecraft:ender_pearl");
         final int pearlGoal = target + (additional ? baseline : 0);
         final int partners = Math.max(1, Math.min(4, ag.init_I_JsonObject(args, "partners", 3)));
         final int budget = NetHelper.init(Math.max(0, ag.init_I_JsonObject(args, "gold", 0)), e.init_I("minecraft:gold_ingot"));
         if (budget == 0 && (target == 0 || baseline < pearlGoal)) {
            return R.init_AiOperation(
               AiOperation.Status.FAILED,
               "No gold ingots available for adult bartering",
               ag.init_JsonObject_ObjArr("missing", ag.init_JsonObject_ObjArr("minecraft:gold_ingot", 1), "goldSpent", 0, "bartered", 0)
            );
         }

         final int seconds = Math.max(10, Math.min(900, ag.init_I_JsonObject(args, "timeoutSeconds", 300)));
         return new AiOperation() {
            final Object init_Obj = mc.level;
            final int init_I = mc.player.getInventory().getSelectedSlot();
            final long init_J = System.nanoTime() + seconds * 1000000000L;
            final autismclient.ai.a.DataUtil init_DataUtil = new autismclient.ai.a.DataUtil();
            final Y init_Y = new Y(X.Inner_CachePool.standard().withoutFlee().keeping(List.of("minecraft:gold_ingot", "minecraft:ender_pearl")), "barter");
            final Map<UUID, JsonUtil.Inner_CachePool> init_Map = new LinkedHashMap<>();
            final Set<UUID> init_Set = new HashSet<>();
            final Map<String, Integer> load_Map = JsonUtil.init();
            final Map<String, Integer> read_Map = new HashMap<>();
            final Map<String, Integer> write_Map = new HashMap<>();
            final Map<UUID, Integer> update_Map = new HashMap<>(JsonUtil.init_Map(mc));
            final Map<String, Integer> sync_Map = new HashMap<>(this.load_Map);
            final JsonUtil.Inner_DataUtil init_JsonUtil_DataUtil = new JsonUtil.Inner_DataUtil();
            AiOperation init_AiOperation;
            Piglin init_Piglin;
            Piglin load_Piglin;
            Map<UUID, Integer> flush_Map;
            Map<String, Integer> close;
            int load_I;
            int read_I;
            int write_I;
            int update_I;
            int sync_I;
            int flush_I;
            boolean init_Z;
            boolean load_Z;
            final AiOperation load_AiOperation = new AiOperation() {
               @Override
               public AiOperation.Result tick() {
                  throw new IllegalStateException("Transfer boundary is observation-only");
               }

               @Override
               public boolean canInterruptNow() {
                  return !init_Z_4();
               }

               @Override
               public boolean requiresReconciliationAfterEmergency() {
                  return init_Z_4();
               }

               @Override
               public void cancel() {
               }
            };

            @Override
            public AiOperation.Result tick() {
               if (this.init_Z || mc.player == null || mc.level != this.init_Obj) {
                  return this.init(AiOperation.Status.CANCELLED, "Barter world changed");
               }

               if (!mc.player.isAlive()) {
                  return this.init(AiOperation.Status.FAILED, "Player died while bartering");
               }

               this.load_I++;
               if (System.nanoTime() > this.init_J) {
                  return this.init(
                     AiOperation.Status.TIMED_OUT, "Barter budget expired; observed pearls " + e.init_I("minecraft:ender_pearl") + "/" + pearlGoal
                  );
               }

               if (target > 0 && e.init_I("minecraft:ender_pearl") >= pearlGoal && this.init_Piglin == null && this.init_Map.isEmpty()) {
                  return this.init(AiOperation.Status.SUCCEEDED, "Pearl inventory target met after " + this.write_I + " confirmed barters");
               }

               if (cO.init_List_Str_D("minecraft:piglin_brute", 12.0).isEmpty() && cO.init_List_Str_D("minecraft:hoglin", 8.0).isEmpty()) {
                  if (JsonUtil.init_Z(mc)) {
                     AiOperation.Result survival = this.init_Y
                        .init_AiOperation_Result_AiOperation(this.init_Z_4() ? this.load_AiOperation : this.init_AiOperation, () -> {
                           if (this.init_AiOperation != null) {
                              this.init_AiOperation.cancel();
                              this.init_AiOperation = null;
                              this.load_Piglin = null;
                           }
                        }, () -> Math.max(1, (int)((this.init_J - System.nanoTime()) / 1000000000L)));
                     if (survival != null) {
                        this.init_DataUtil.init_V();
                        return survival.terminal() ? this.init(survival.status(), survival.message()) : survival;
                     }
                  }

                  if (this.init_AiOperation != null) {
                     AiOperation.Result result = this.init_AiOperation.tick();
                     if (!result.terminal()) {
                        return this.init(result.message());
                     }

                     boolean pendingLeg = this.init_AiOperation.requiresReconciliationAfterEmergency();
                     this.init_AiOperation.cancel();
                     this.init_AiOperation = null;
                     if (result.status() == AiOperation.Status.UNCERTAIN || pendingLeg) {
                        return this.init(AiOperation.Status.UNCERTAIN, "Barter child needs reconciliation: " + result.message());
                     }

                     if (result.status() == AiOperation.Status.CANCELLED) {
                        return this.init(AiOperation.Status.CANCELLED, result.message());
                     }

                     if (this.load_Piglin != null) {
                        if (result.status() != AiOperation.Status.SUCCEEDED) {
                           aC.init_V(mc, this.load_Piglin);
                        }

                        this.load_Piglin = null;
                     } else if (result.status() != AiOperation.Status.SUCCEEDED && ++this.flush_I > 4) {
                        return this.init(AiOperation.Status.FAILED, "Barter preparation or collection repeatedly failed: " + result.message());
                     }
                  }

                  if (!JsonUtil.init_Z(mc)) {
                     if (this.load_Z) {
                        return this.init(AiOperation.Status.FAILED, "Wear a usable gold armor piece before bartering");
                     }

                     this.load_Z = true;
                     this.init_AiOperation = w.init(ag.init_JsonObject_ObjArr("what", "armor", "prefer", "golden_boots"));
                     return this.init("Equipping gold armor for piglin neutrality");
                  } else {
                     if (this.init_Piglin != null) {
                        boolean admired = this.init_Piglin.isAlive() && this.init_Piglin.getOffhandItem().is(Items.GOLD_INGOT);
                        JsonUtil.Inner_StreamUtil outcome = this.init_JsonUtil_DataUtil
                           .init(e.init_I("minecraft:gold_ingot"), admired, ca.init_J(), this.load_I);
                        if (outcome != JsonUtil.Inner_StreamUtil.load) {
                           if (outcome == JsonUtil.Inner_StreamUtil.read) {
                              this.read_I = this.read_I + this.init_JsonUtil_DataUtil.init_I();
                              return this.init(AiOperation.Status.UNCERTAIN, "Gold changed by more than one ingot during a single adult interaction");
                           }

                           if (outcome == JsonUtil.Inner_StreamUtil.write) {
                              return this.init(
                                 AiOperation.Status.UNCERTAIN, "Adult interaction was not acknowledged; reobserve gold and offhand before spending again"
                              );
                           }

                           return this.init("Waiting for one adult's ingot acknowledgement");
                        }

                        this.read_I++;
                        JsonUtil.Inner_CachePool trade = new JsonUtil.Inner_CachePool(this.init_Piglin, this.flush_Map, this.close, this.sync_I);
                        trade.init_Z = admired;
                        this.init_Map.put(this.init_Piglin.getUUID(), trade);
                        this.init_Piglin = null;
                     }

                     for (JsonUtil.Inner_CachePool trade : new ArrayList<>(this.init_Map.values())) {
                        Piglin piglin = trade.init_Piglin;
                        if (piglin.isAlive()
                           && mc.level.getEntity(piglin.getId()) == piglin
                           && !piglin.isConverting()
                           && !piglin.isAggressive()
                           && trade.init_UUID.equals(piglin.getUUID())) {
                           trade.init_Z = trade.init_Z | piglin.getOffhandItem().is(Items.GOLD_INGOT);

                           for (Entity entity : mc.level.entitiesForRendering()) {
                              if (entity instanceof ItemEntity drop
                                 && drop.isAlive()
                                 && !drop.getItem().is(Items.GOLD_INGOT)
                                 && !(drop.distanceToSqr(piglin) > 144.0)) {
                                 int quantity = drop.getItem().getCount();
                                 int before = Math.max(trade.init_Map.getOrDefault(drop.getUUID(), 0), this.update_Map.getOrDefault(drop.getUUID(), 0));
                                 if (quantity > before) {
                                    String id = BuiltInRegistries.ITEM.getKey(drop.getItem().getItem()).toString();
                                    this.update_Map.put(drop.getUUID(), quantity);
                                    this.read_Map.merge(id, quantity - before, Integer::sum);
                                    trade.load_Z = true;
                                 }
                              }
                           }

                           if (trade.init_Z && this.load_I - trade.init_I >= 100 && !piglin.getOffhandItem().is(Items.GOLD_INGOT)) {
                              JsonUtil.init().forEach((idxx, count) -> {
                                 int gained = count - Math.max(trade.load_Map.getOrDefault(idxx, 0), this.sync_Map.getOrDefault(idxx, 0));
                                 if (gained > 0 && JsonUtil.init_Z(idxx)) {
                                    trade.load_Z = true;
                                    this.read_Map.putIfAbsent(idxx, 0);
                                    this.write_Map.merge(idxx, Math.max(0, count - this.load_Map.getOrDefault(idxx, 0)), Math::max);
                                    this.sync_Map.put(idxx, count);
                                 }
                              });
                           }

                           if (this.load_I - trade.init_I >= 100 && !piglin.getOffhandItem().is(Items.GOLD_INGOT) && trade.init_Z && trade.load_Z) {
                              this.write_I++;
                              this.init_Map.remove(trade.init_UUID);
                           } else if (this.load_I - trade.init_I > 360) {
                              this.init_Map.remove(trade.init_UUID);
                              this.init_Set.add(trade.init_UUID);
                              this.update_I++;
                           }
                        } else {
                           this.init_Map.remove(trade.init_UUID);
                           this.init_Set.add(trade.init_UUID);
                           this.update_I++;
                        }
                     }

                     if (this.init_Piglin != null || af.init_I() >= 2 || this.read_I >= budget && this.read_Map.isEmpty()) {
                        if (this.init_Map.size() < partners
                           && this.read_I < budget
                           && e.init_I("minecraft:gold_ingot") > 0
                           && (target == 0 || e.init_I("minecraft:ender_pearl") < pearlGoal)) {
                           Piglin partner = JsonUtil.init(mc, this.init_Map.keySet(), this.init_Set);
                           if (partner != null) {
                              if (!(partner.distanceToSqr(mc.player) > 9.0) && mc.player.hasLineOfSight(partner)) {
                                 if (mc.player.getMainHandItem().is(Items.GOLD_INGOT)) {
                                    this.init_DataUtil.init_V_Vec3(partner.getBoundingBox().getCenter());
                                    this.init_DataUtil.load_V();
                                    if (this.init_DataUtil.load_Z() && this.init_DataUtil.load_Z_Entity(partner)) {
                                       this.flush_Map = JsonUtil.init_Map(mc);
                                       this.close = JsonUtil.init();
                                       this.init_Piglin = partner;
                                       this.sync_I = this.load_I;
                                       this.init_JsonUtil_DataUtil.init(e.init_I("minecraft:gold_ingot"), ca.init_J(), this.load_I);
                                       mc.gameMode
                                          .interact(
                                             mc.player, partner, new EntityHitResult(partner, partner.getBoundingBox().getCenter()), InteractionHand.MAIN_HAND
                                          );
                                       return this.init("Offering exactly one ingot to an adult");
                                    }

                                    return this.init("Aiming at an adult barter partner");
                                 }

                                 this.init_AiOperation = w.init(ag.init_JsonObject_ObjArr("what", "minecraft:gold_ingot"));
                              } else if (this.init_Map.isEmpty()) {
                                 this.load_Piglin = partner;
                                 this.init_AiOperation = HashUtil.init_AiOperation_BlockPos(partner.blockPosition(), 2.5, true, false, false, 40);
                              }

                              if (this.init_AiOperation != null) {
                                 return this.init("Preparing the next safe barter partner");
                              }
                           }
                        }

                        String collect = null;

                        for (String id : this.read_Map.keySet().stream().sorted(Comparator.comparingInt(JsonUtil::init_I)).toList()) {
                           if (JsonUtil.init(mc, id)) {
                              collect = id;
                              break;
                           }
                        }

                        if (collect == null || this.init_Map.size() < partners && this.read_I < budget && this.load_I % 40 != 0) {
                           if (this.init_Map.isEmpty() && this.init_Piglin == null && this.init_AiOperation == null) {
                              if (this.read_I >= budget || e.init_I("minecraft:gold_ingot") <= 0) {
                                 return this.init(
                                    target == 0 && this.write_I > 0 ? AiOperation.Status.SUCCEEDED : AiOperation.Status.FAILED,
                                    "Gold batch exhausted; pearls " + e.init_I("minecraft:ender_pearl") + "/" + pearlGoal + "; loot is random"
                                 );
                              }

                              if (JsonUtil.init(mc, this.init_Map.keySet(), this.init_Set) == null) {
                                 return this.init(AiOperation.Status.FAILED, "No safe unprovoked adult piglin available");
                              }
                           }

                           return this.init("Waiting on " + this.init_Map.size() + " genuine barters");
                        } else {
                           this.init_AiOperation = T.write(
                              ag.init_JsonObject_ObjArr("item", collect, "count", 1, "additional", true, "radius", 12, "timeoutSeconds", 12)
                           );
                           return this.init("Collecting observed barter output " + collect);
                        }
                     } else {
                        this.init_AiOperation = cx.init(
                           ag.init_JsonObject_ObjArr(
                              "action",
                              "drop",
                              "freeSlots",
                              3,
                              "keep",
                              "gold_ingot,ender_pearl,obsidian,string,spectral_arrow,potion,splash_potion,fire_charge"
                           )
                        );
                        return this.init("Making capacity for useful barter outputs");
                     }
                  }
               } else {
                  return this.init(AiOperation.Status.FAILED, "Barter station unsafe: brute or hoglin nearby; choose a protected adult partner");
               }
            }

            JsonObject init_JsonObject_11() {
               JsonObject gains = new JsonObject();
               JsonUtil.init().forEach((id, count) -> {
                  int gained = count - this.load_Map.getOrDefault(id, 0);
                  if (gained > 0 && this.read_Map.containsKey(id)) {
                     gains.addProperty(id, gained);
                  }
               });
               return ag.init_JsonObject_ObjArr(
                  "bartered",
                  this.write_I,
                  "goldSpent",
                  this.read_I,
                  "goldBudget",
                  budget,
                  "goldLeft",
                  e.init_I("minecraft:gold_ingot"),
                  "partners",
                  partners,
                  "pendingBarters",
                  this.init_Map.size(),
                  "unconfirmedOrLost",
                  this.update_I,
                  "pearlsTarget",
                  pearlGoal,
                  "pearlsGained",
                  e.init_I("minecraft:ender_pearl") - baseline,
                  "pearlsMet",
                  e.init_I("minecraft:ender_pearl") >= pearlGoal,
                  "observedOutputs",
                  ag.init.toJsonTree(this.read_Map),
                  "outputAttribution",
                  "aggregate nearby drop and inventory evidence; individual output attribution may be ambiguous",
                  "inventoryConfirmedOutputs",
                  ag.init.toJsonTree(this.write_Map),
                  "collectedOutputs",
                  gains,
                  "lootWeightTotal",
                  469,
                  "pearlWeight",
                  10,
                  "pearlRollMin",
                  2,
                  "pearlRollMax",
                  4,
                  "lootTableAssumption",
                  "vanilla_26.2; server outputs remain authoritative"
               );
            }

            AiOperation.Result init(String message) {
               return R.init_AiOperation_Result(AiOperation.Status.RUNNING, message, this.init_JsonObject_11());
            }

            boolean init_Z_4() {
               return this.init_JsonUtil_DataUtil.init_Z() || !this.init_Map.isEmpty();
            }

            AiOperation.Result init(AiOperation.Status status, String message) {
               AiOperation.Result result = Y.init_AiOperation_Result_Z_AiOperation_Result(
                  this.requiresReconciliationAfterEmergency(), R.init_AiOperation_Result(status, message, this.init_JsonObject_11())
               );
               this.cancel();
               return result;
            }

            @Override
            public boolean canInterruptNow() {
               return !this.init_Z_4() && (this.init_AiOperation == null || this.init_AiOperation.canInterruptNow()) && this.init_Y.load();
            }

            @Override
            public boolean requiresReconciliationAfterEmergency() {
               return this.init_Z_4() || this.init_AiOperation != null && this.init_AiOperation.requiresReconciliationAfterEmergency() || this.init_Y.read();
            }

            @Override
            public void cancel() {
               if (!this.init_Z) {
                  this.init_Z = true;
                  if (this.init_AiOperation != null) {
                     this.init_AiOperation.cancel();
                     this.init_AiOperation = null;
                  }

                  this.init_Y.init_V();
                  this.init_DataUtil.init_V();
                  if (mc.player != null && mc.level == this.init_Obj) {
                     af.load(this.init_I);
                  }
               }
            }
         };
      } else {
         throw new IllegalArgumentException("Bartering requires a connected living player");
      }
   }

   private static int init_I(String id) {
      byte var10000;
      switch (id) {
         case "minecraft:ender_pearl":
            var10000 = 0;
            break;
         case "minecraft:obsidian":
            var10000 = 1;
            break;
         case "minecraft:string":
            var10000 = 2;
            break;
         case "minecraft:potion":
         case "minecraft:splash_potion":
            var10000 = 3;
            break;
         case "minecraft:spectral_arrow":
            var10000 = 4;
            break;
         default:
            var10000 = 5;
      }

      return var10000;
   }

   private static boolean init_Z(String id) {
      return Set.of(
            "minecraft:ender_pearl",
            "minecraft:obsidian",
            "minecraft:crying_obsidian",
            "minecraft:string",
            "minecraft:spectral_arrow",
            "minecraft:fire_charge",
            "minecraft:potion",
            "minecraft:splash_potion",
            "minecraft:iron_nugget",
            "minecraft:quartz",
            "minecraft:leather",
            "minecraft:gravel",
            "minecraft:blackstone",
            "minecraft:soul_sand",
            "minecraft:nether_brick",
            "minecraft:iron_boots",
            "minecraft:enchanted_book",
            "minecraft:dried_ghast"
         )
         .contains(id);
   }

   private static boolean init_Z(Minecraft mc) {
      for (EquipmentSlot slot : List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
         ItemStack stack = mc.player.getItemBySlot(slot);
         if (bK.init(stack, 8) && n.load_Set.contains(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString())) {
            return true;
         }
      }

      return false;
   }

   private static Piglin init(Minecraft mc, Set<UUID> busy, Set<UUID> skipped) {
      Piglin best = null;
      double distance = 1024.0;

      for (Entity entity : mc.level.entitiesForRendering()) {
         if (entity instanceof Piglin piglin
            && piglin.isAdult()
            && piglin.isAlive()
            && !piglin.isConverting()
            && !piglin.isAggressive()
            && piglin.getOffhandItem().isEmpty()
            && !busy.contains(piglin.getUUID())
            && !skipped.contains(piglin.getUUID())
            && aC.init_Z(mc, piglin)
            && ValueSink.init_Z_Minecraft_BlockPos(mc, piglin.blockPosition())
            && !init(mc, piglin.blockPosition())) {
            double d = piglin.distanceToSqr(mc.player);
            if (d < distance) {
               distance = d;
               best = piglin;
            }
         }
      }

      return best;
   }

   private static boolean init(Minecraft mc, BlockPos at) {
      for (BlockPos p : BlockPos.betweenClosed(at.offset(-2, -2, -2), at.offset(2, 0, 2))) {
         if (mc.level.getFluidState(p).is(FluidTags.LAVA)) {
            return true;
         }
      }

      return false;
   }

   private static Map<UUID, Integer> init_Map(Minecraft mc) {
      Map<UUID, Integer> out = new HashMap<>();

      for (Entity entity : mc.level.entitiesForRendering()) {
         if (entity instanceof ItemEntity drop) {
            out.put(drop.getUUID(), drop.getItem().getCount());
         }
      }

      return out;
   }

   private static boolean init(Minecraft mc, String id) {
      for (Entity entity : mc.level.entitiesForRendering()) {
         if (entity instanceof ItemEntity drop
            && drop.isAlive()
            && drop.distanceToSqr(mc.player) < 144.0
            && id.equals(BuiltInRegistries.ITEM.getKey(drop.getItem().getItem()).toString())) {
            return true;
         }
      }

      return false;
   }

   private static Map<String, Integer> init() {
      Map<String, Integer> out = new HashMap<>();
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null) {
         for (int slot = 0; slot < mc.player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = mc.player.getInventory().getItem(slot);
            if (!stack.isEmpty()) {
               out.merge(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString(), stack.getCount(), Integer::sum);
            }
         }
      }

      return out;
   }

   private static final class Inner_CachePool {
      final Piglin init_Piglin;
      final UUID init_UUID;
      final Map<UUID, Integer> init_Map;
      final Map<String, Integer> load_Map;
      final int init_I;
      boolean init_Z;
      boolean load_Z;

      Inner_CachePool(Piglin piglin, Map<UUID, Integer> beforeDrops, Map<String, Integer> beforeInventory, int began) {
         this.init_Piglin = piglin;
         this.init_UUID = piglin.getUUID();
         this.init_Map = beforeDrops;
         this.init_I = began;
         this.load_Map = beforeInventory;
      }
   }

   static final class Inner_DataUtil {
      private int init_I;
      private int load;
      private int read;
      private long init_J;
      private boolean init_Z;

      void init(int gold, long revision, int tick) {
         if (this.init_Z) {
            throw new IllegalStateException("A gold offer is already pending");
         }

         this.init_I = gold;
         this.init_J = revision;
         this.load = tick;
         this.read = 0;
         this.init_Z = true;
      }

      JsonUtil.Inner_StreamUtil init(int gold, boolean admiring, long revision, int tick) {
         if (!this.init_Z) {
            throw new IllegalStateException("No gold offer is pending");
         }

         int consumed = this.init_I - gold;
         this.read = Math.max(0, consumed);
         if (consumed <= 0 || !admiring && revision <= this.init_J) {
            return tick - this.load > 60 ? JsonUtil.Inner_StreamUtil.write : JsonUtil.Inner_StreamUtil.init;
         }

         if (consumed != 1) {
            return JsonUtil.Inner_StreamUtil.read;
         }

         this.init_Z = false;
         return JsonUtil.Inner_StreamUtil.load;
      }

      boolean init_Z() {
         return this.init_Z;
      }

      int init_I() {
         return this.read;
      }
   }

   enum Inner_StreamUtil {
      init,
      load,
      read,
      write;
   }
}
